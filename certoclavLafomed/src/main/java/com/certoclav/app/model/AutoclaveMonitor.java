package com.certoclav.app.model;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.SparseArray;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Profile;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.database.ProtocolEntry;
import com.certoclav.app.database.User;
import com.certoclav.app.listener.AlertListener;
import com.certoclav.app.listener.AutoclaveStateListener;
import com.certoclav.app.listener.ConnectionStatusListener;
import com.certoclav.app.listener.ProtocolListener;
import com.certoclav.app.listener.SensorDataListener;
import com.certoclav.app.monitor.MonitorActivity;
import com.certoclav.app.monitor.MonitorNotificationActivity;
import com.certoclav.app.service.CloudSocketService;
import com.certoclav.app.service.PostProtocolsService;
import com.certoclav.app.service.ReadAndParseSerialService;
import com.certoclav.app.util.AuditLogger;
import com.certoclav.app.util.AutoclaveModelManager;
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.bcrypt.BCrypt;
import com.certoclav.library.certocloud.CloudDatabase;
import com.certoclav.library.certocloud.Condition;
import com.certoclav.library.certocloud.NotificationService;
import com.certoclav.library.util.FileUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.certoclav.app.model.AutoclaveState.PREPARE_TO_RUN;


public class AutoclaveMonitor implements SensorDataListener, ConnectionStatusListener, AutoclaveStateListener, ProtocolListener {

    Context mContext = ApplicationController.getContext();
    private SparseArray<String> errorMap = new SparseArray<String>();
    private SparseArray<String> warningMap = new SparseArray<String>();
    private SparseArray<String> errorVideoMap = new SparseArray<String>();


    public static final int ERROR_CODE_SUCCESSFULL = 0;
    public static final int ERROR_CODE_INDICATOR_FAILED = 50;
    public static final int ERROR_CODE_INDICATOR_SUCCESS = 51;
    public static final int ERROR_CODE_INDICATOR_NOT_COMPLETED = 52;
    public static final int ERROR_CODE_CONNECTION_LOST = -2;
    public static final int ERROR_CODE_CANCELLED_BY_ERROR = -3;
    public static final int ERROR_CODE_POWER_LOSS = -4;


    // 11111111111111111111000011111111
    // error code range is from 1 to 32 (index in binary stirng + 1)
//    public static final int WATER_FLOATING_SYSTEM_FAILURE = 32;
//    public static final int FILLING_WATER_TIME_EXCEEDED = 31;
//    public static final int REFILLING_WATER_FAILURE = 30;
//    public static final int PHASE_1_HEATING_FAILURE = 29;
//    public static final int PHASE_1_PRESSURE_FAILURE = 28;
//    public static final int PHASE_1_TIME_EXCEEDED = 27;
//    public static final int PHASE_2_TIME_EXCEEDED = 26;
//    public static final int STERILIZATION_OVERTEMPERATURE = 25;
//    public static final int STERILIZATION_UNDERTEMPERATURE = 24;
//    public static final int STERILIZATION_OVERPRESSURE = 23;
//    public static final int STERILIZATION_UNDERPRESSURE = 22;
//    public static final int PRESSURE_LEVELING_TIMEOUT = 21;
//    public static final int TEMPERATURE_PROBE_FAILURE = 20;
//    public static final int DIFFERENCE_BETWEEN_PROBES_EXCEEDED = 19;
//    public static final int DOOR_OPEN = 18;
//    public static final int LOW_PRESSURE_IN_PULSES = 17;
//    public static final int STABILIZATION_PHASE_TIMEOUT = 16;
//    public static final int WATER_PUMP_FAILURE = 15;
//    public static final int COOLING_WATER_FAILURE = 14;
//    public static final int VACUUM_TEST_FAILURE = 8;
//    public static final int VACUUM_TEST_UNDERPRESSURE = 7;
//    public static final int VACUUM_TEST_UNDERTEMPERATURE = 6;
//    public static final int VACUUM_TEST_FINAL_PRESSURE_EXCEEDED = 5;
//    public static final int MANUAL_PHASE_CHANGE = 4;
//    public static final int POWER_SUPPLY_FAILURE = 3;
//    public static final int ERROR_CODE_CANCELLED_BY_USER = 2;
//    public static final int FATAL_ERROR_PROCESS_STOPPED = 1;
    public static final int ERROR_TEMP_OUT_OF_RANGE = 1;
    public static final int ERROR_TIMEOUT_STEAM_PULSE = 2;
    public static final int ERROR_TIMEOUT_STABILIZATION = 3;
    public static final int ERROR_HEART_PROBES_EXC_DIF = 4;
    //    public static final int WARNING_PROCESS_ERROR = 5;
    public static final int ERROR_HEATING_FAILURE = 6;
    public static final int ERROR_HEATING_FAILURE_2 = 7;
    public static final int ERROR_STERILIZATION_OVERTEMPERATURE = 8;
    public static final int ERROR_STERILIZATION_UNDERTEMPERATURE = 9;
    public static final int ERROR_STERILIZATION_OVERPRESSURE = 10;
    public static final int ERROR_STERILIZATION_UNDERPRESSURE = 11;
    public static final int ERROR_PRESSURE_LEVELING_TIMEOUT = 12;
    public static final int ERROR_PROBE_FAILURE = 13;
    public static final int ERROR_DIFFERENCE_BETWEEN_PROBES_EXCEEDED = 14;
    public static final int ERROR_DOOR_OPEN = 15;
    public static final int ERROR_NO_WATER = 20;
    public static final int ERROR_RESET_THERMOSTAT = 21;
    //    public static final int ERROR_NO_WATER = 24;
//    public static final int ERROR_RESET_SAFETY_THERMOSTAT = 25;
    public static final int ERROR_VACUUM_TEST_UNDERPRESSURE = 26;
    public static final int ERROR_VACUUM_TEST_UNDERTEMPERATURE = 27;
    public static final int ERROR_VACUUM_TEST_FINAL_PRESSURE_EXCEEDED = 28;
    public static final int ERROR_POWER_SUPPLY_FAILURE = 30;
    public static final int ERROR_CYCLE_CANCELLED_MANUALLY = 31;


    //    public static final int WARNING_TEMP_OUT_OF_RANGE = 1;
//    public static final int WARNING_TIMEOUT_STEAM_PULSE = 2;
//    public static final int WARNING_TIMEOUT_STABILIZATION = 3;
//    public static final int WARNING_HEART_PROBES_EXC_DIF = 4;
//    public static final int WARNING_PROCESS_ERROR = 5;
    public static final int WARNING_PLEASE_CLOSE_DOOR = 6;
    //    public static final int OPEN_DOOR = 7;
    public static final int WARNING_HIGH_TEMP_FOR_THE_TEST = 8;
    public static final int WARNING_ERROR_TEMP_SENSOR_1 = 9;
    public static final int WARNING_ERROR_TEMP_SENSOR_2 = 10;
    public static final int WARNING_ERROR_TEMP_SENSOR_3 = 11;
    public static final int WARNING_ERROR_TEMP_SENSOR_4 = 12;
    public static final int WARNING_ERROR_PRES_SENSOR_1 = 13;
    public static final int WARNING_ERROR_PRES_SENSOR_2 = 14;
    //    public static final int READ_ATM_PRESSURE = 15;
    public static final int WARNING_ATMOSPHERIC_PRESS_FAILURE = 16;
    //    public static final int DOOR_OPEN_WARN = 17;
    public static final int WARNING_TEST_MAX_TEMP = 18;
    public static final int WARNING_GENERAL_FAILURE = 19;
    //    public static final int VACUUM_TEST_NOK = 20;
//    public static final int STERILIZATION_NOK = 21;
//    public static final int VACUUM_TEST_OK = 22;
//    public static final int STERILIZATION_OK = 23;
    public static final int WARNING_NO_WATER = 24;
    public static final int WARNING_RESET_SAFETY_THERMOSTAT = 25;
    public static final int WARNING_CHECKING_RECOMMENDED = 26;
    public static final int WARNING_BACTERIOL_FILTER_CHANGE = 27;
    public static final int WARNING_BOILER_WITHOUT_WATER = 28;
    public static final int WARNING_WARNING_ERROR_APROX__SENSOR = 29;
    public static final int WARNING_ERROR_CLOSE_DOOR_1 = 30;
    public static final int WARNING_ERROR_CLOSE_DOOR_2 = 31;
    public static final int WARNING_ERROR_CLOSE_DOOR_3 = 32;
    public static final int WARNING_ERROR_CLOSE_DOOR_4 = 33;
    public static final int WARNING_ERROR_CLOSE_DOOR_5 = 34;
    public static final int WARNING_ERROR_OPEN_DOOR_1 = 35;
    public static final int WARNING_ERROR_OPEN_DOOR_2 = 36;
    public static final int WARNING_ERROR_OPEN_DOOR_3 = 37;
    public static final int WARNING_NO_WATTER_FOR_PUMP = 38;
    public static final int WARNING_NO_COOLING_WATTER = 39;

    private long nanoTimeAtLastMessageReceived = 0;


    ArrayList<AlertListener> alertListeners = new ArrayList<AlertListener>();
    private ArrayList<Error> errorList = new ArrayList<Error>(); //list of current errors
    private ArrayList<Error> warningList = new ArrayList<Error>(); //list of current errors
    private DatabaseService databaseService;
    private boolean startButtonClicked = false;
    private long nanoTimeAtLastStopCommand = 0;

    private long nanoTimeAtLastServiceCheck = 0;
    private Date dateAtProgramStart = Autoclave.getInstance().getDateObject();
    private long nanoTimeAtLastStartCommand = 0;
    long secondsSinceStart = 0;
    private long secondsOnLastRecord = 5; //init not to 0
    private long nanoIgnoreErrorTemporary = 0;
    private FileUtils fileUtils = new FileUtils();


    private Integer indexOfProfile = null;


    //IO SIMULATION
    public static boolean PowerOffDeviceAutomatically = false;
    public static boolean SimulatedFailStoppedByUser = false;
    boolean SIMUTALTE_STATE_RUNNING = false;

    private boolean mCodeEntered = false;


    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        //This Thread will be called every 0.5 seconds.
        @Override
        public void run() {
            updateStateMachine();
            timerHandler.postDelayed(this, 500);
        }
    };


    //singleton class
    private static AutoclaveMonitor instance = new AutoclaveMonitor();


    public static synchronized AutoclaveMonitor getInstance() {
        return instance;
    }


    private AutoclaveMonitor() {
        //map a error description to the error code.
        errorMap.put(ERROR_CODE_SUCCESSFULL, mContext.getString(R.string.progr_finished_successfully));
        errorMap.put(ERROR_CODE_CONNECTION_LOST, mContext.getString(R.string.connection_lost_during_record));
        errorMap.put(ERROR_CODE_CANCELLED_BY_ERROR, mContext.getString(R.string.error_cycle_cancelled_error));
        errorMap.put(ERROR_CODE_POWER_LOSS, mContext.getString(R.string.power_loss_during_record));
        errorMap.put(ERROR_CODE_INDICATOR_FAILED, mContext.getString(R.string.indicator_failed));
        errorMap.put(ERROR_CODE_INDICATOR_SUCCESS, mContext.getString(R.string.indicator_success));
        errorMap.put(ERROR_CODE_INDICATOR_NOT_COMPLETED, mContext.getString(R.string.indicator_not_completed));

        errorMap.put(ERROR_HEART_PROBES_EXC_DIF, mContext.getString(R.string.error_heart_probes_exc_dif));
        errorMap.put(ERROR_HEATING_FAILURE, mContext.getString(R.string.error_heating_failure));
        errorMap.put(ERROR_HEATING_FAILURE_2, mContext.getString(R.string.error_heating_failure_2));
        errorMap.put(ERROR_STERILIZATION_OVERTEMPERATURE, mContext.getString(R.string.error_sterilization_overtemperature));
        errorMap.put(ERROR_STERILIZATION_UNDERTEMPERATURE, mContext.getString(R.string.error_sterilization_undertemperature));
        errorMap.put(ERROR_STERILIZATION_OVERPRESSURE, mContext.getString(R.string.error_sterilization_overpressure));
        errorMap.put(ERROR_STERILIZATION_UNDERPRESSURE, mContext.getString(R.string.error_sterilization_underpressure));
        errorMap.put(ERROR_PRESSURE_LEVELING_TIMEOUT, mContext.getString(R.string.error_pressure_leveling_timeout));
        errorMap.put(ERROR_PROBE_FAILURE, mContext.getString(R.string.error_probe_failure));
        errorMap.put(ERROR_DIFFERENCE_BETWEEN_PROBES_EXCEEDED, mContext.getString(R.string.error_difference_between_probes_exceeded));
        errorMap.put(ERROR_DOOR_OPEN, mContext.getString(R.string.error_door_open));
        errorMap.put(ERROR_NO_WATER, mContext.getString(R.string.error_no_water));
        errorMap.put(ERROR_RESET_THERMOSTAT, mContext.getString(R.string.error_reset_safety_thermostat));
        errorMap.put(ERROR_VACUUM_TEST_UNDERPRESSURE, mContext.getString(R.string.error_vacuum_test_underpressure));
        errorMap.put(ERROR_VACUUM_TEST_UNDERTEMPERATURE, mContext.getString(R.string.error_vacuum_test_undertemperature));
        errorMap.put(ERROR_VACUUM_TEST_FINAL_PRESSURE_EXCEEDED, mContext.getString(R.string.error_vacuum_test_final_pressure_exceeded));
        errorMap.put(ERROR_POWER_SUPPLY_FAILURE, mContext.getString(R.string.error_power_supply_failure));
        errorMap.put(ERROR_CYCLE_CANCELLED_MANUALLY, mContext.getString(R.string.error_cycle_cancelled_manually));


        //map a error description to the error code.
//        warningMap.put(WARNING_TEMP_OUT_OF_RANGE, mContext.getString(R.string.warning_temp_out_of_range));
//        warningMap.put(WARNING_TIMEOUT_STEAM_PULSE, mContext.getString(R.string.warning_timeout_steam_pulse));
//        warningMap.put(WARNING_TIMEOUT_STABILIZATION, mContext.getString(R.string.warning_timeout_stabilization));
//        warningMap.put(WARNING_HEART_PROBES_EXC_DIF, mContext.getString(R.string.warning_heart_probes_exc_dif));
//        warningMap.put(WARNING_PROCESS_ERROR, mContext.getString(R.string.warning_process_error));
        warningMap.put(WARNING_PLEASE_CLOSE_DOOR, mContext.getString(R.string.warning_please_close_door));
//        warningMap.put(OPEN_DOOR, "OPEN DOOR");
        warningMap.put(WARNING_HIGH_TEMP_FOR_THE_TEST, mContext.getString(R.string.warning_high_temp_for_the_test));
        warningMap.put(WARNING_ERROR_TEMP_SENSOR_1, mContext.getString(R.string.warning_error_temp_sensor_1));
        warningMap.put(WARNING_ERROR_TEMP_SENSOR_2, mContext.getString(R.string.warning_error_temp_sensor_2));
        warningMap.put(WARNING_ERROR_TEMP_SENSOR_3, mContext.getString(R.string.warning_error_temp_sensor_3));
        warningMap.put(WARNING_ERROR_TEMP_SENSOR_4, mContext.getString(R.string.warning_errror_temp_senor_4));
        warningMap.put(WARNING_ERROR_PRES_SENSOR_1, mContext.getString(R.string.warning_error_pres_sensor_1));
        warningMap.put(WARNING_ERROR_PRES_SENSOR_2, mContext.getString(R.string.warning_error_press_sensor_2));
//        warningMap.put(READ_ATM_PRESSURE, "READ ATM PRESSURE");
        warningMap.put(WARNING_ATMOSPHERIC_PRESS_FAILURE, mContext.getString(R.string.warning_atmosperic_press_failure));
//        warningMap.put(DOOR_OPEN, mContext.getString(R.string.warning_door_open));
        warningMap.put(WARNING_TEST_MAX_TEMP, mContext.getString(R.string.warning_test_max_temp));
        warningMap.put(WARNING_GENERAL_FAILURE, mContext.getString(R.string.warning_general_failure));
//        warningMap.put(VACUUM_TEST_NOK, "VACUUM TEST NOK");
//        warningMap.put(STERILIZATION_NOK, "STERILIZATION NOK");
//        warningMap.put(VACUUM_TEST_OK, "VACUUM TEST OK");
//        warningMap.put(STERILIZATION_OK, "STERILIZATION OK");
        warningMap.put(WARNING_NO_WATER, mContext.getString(R.string.warning_no_water));
        warningMap.put(WARNING_RESET_SAFETY_THERMOSTAT, mContext.getString(R.string.warning_reset_safety_thermostat));
        warningMap.put(WARNING_CHECKING_RECOMMENDED, mContext.getString(R.string.warning_checking_recommended));
        warningMap.put(WARNING_BACTERIOL_FILTER_CHANGE, mContext.getString(R.string.warning_baxterial_filter_change));
        warningMap.put(WARNING_BOILER_WITHOUT_WATER, mContext.getString(R.string.warning_boiler_without_water));
        warningMap.put(WARNING_WARNING_ERROR_APROX__SENSOR, mContext.getString(R.string.warning_error_aprox_sensor));
        warningMap.put(WARNING_ERROR_CLOSE_DOOR_1, mContext.getString(R.string.warning_error_close_door_1));
        warningMap.put(WARNING_ERROR_CLOSE_DOOR_2, mContext.getString(R.string.warning_error_close_door_2));
        warningMap.put(WARNING_ERROR_CLOSE_DOOR_3, mContext.getString(R.string.warning_error_close_door_3));
        warningMap.put(WARNING_ERROR_CLOSE_DOOR_4, mContext.getString(R.string.warning_error_close_door_4));
        warningMap.put(WARNING_ERROR_CLOSE_DOOR_5, mContext.getString(R.string.warning_error_close_door_5));
        warningMap.put(WARNING_ERROR_OPEN_DOOR_1, mContext.getString(R.string.warning_error_open_door_1));
        warningMap.put(WARNING_ERROR_OPEN_DOOR_2, mContext.getString(R.string.warning_error_open_door_2));
        warningMap.put(WARNING_ERROR_OPEN_DOOR_3, mContext.getString(R.string.warning_error_open_door_3));
        warningMap.put(WARNING_NO_WATTER_FOR_PUMP, mContext.getString(R.string.warning_no_water_for_pump));
        warningMap.put(WARNING_NO_COOLING_WATTER, mContext.getString(R.string.warning_no_cooling_water));

        warningMap.put(ERROR_TEMP_OUT_OF_RANGE, mContext.getString(R.string.error_temp_out_of_range));
        warningMap.put(ERROR_TIMEOUT_STEAM_PULSE, mContext.getString(R.string.error_timeout_steam_pulse));
        warningMap.put(ERROR_TIMEOUT_STABILIZATION, mContext.getString(R.string.error_timeout_stabilization));


        Autoclave.getInstance().setOnSensorDataListener(this);
        Autoclave.getInstance().setOnConnectionStatusListener(this);
        Autoclave.getInstance().setOnAutoclaveStateListener(this);
        Autoclave.getInstance().setOnProtocolListener(this);
        databaseService = DatabaseService.getInstance();
        nanoTimeAtLastMessageReceived = System.nanoTime();
        timerHandler.postDelayed(timerRunnable, 0);
        ReadAndParseSerialService.getInstance();

    }

    public void setOnAlertListener(AlertListener listener) {
        this.alertListeners.add(listener);
    }

    /*
     *
     * sets the autoclave in PREPARE_TO_RUN state.
     * if an error occures, the user will be notified and can solve the error
     * After all errors are solved, the autoclav will change his state to RUNNING
     *
     */
    public void prepareToRun(Integer eIndexOfProfile) {
        if (Autoclave.getInstance().getState() == AutoclaveState.NOT_RUNNING) {
            if (Autoclave.getInstance().getCurrentProgramCounter() < Autoclave.getInstance().getProgramsInRowTotal()) {
                indexOfProfile = eIndexOfProfile;
                startButtonClicked = true;
            }
        }


    }

    public void stopSocketService() {
        Intent intent2 = new Intent(ApplicationController.getContext(), CloudSocketService.class);
        ApplicationController.getContext().stopService(intent2);
    }

    public void updateStateMachine() {

        Log.e("AutoclaveMonitor", Autoclave.getInstance().getState().toString());

        //check if read services are still running
        if ((System.nanoTime() - nanoTimeAtLastServiceCheck) > (1000000000L * 3)) { //3 seconds past
            nanoTimeAtLastServiceCheck = System.nanoTime();

            Intent intent2 = new Intent(ApplicationController.getContext(), CloudSocketService.class);
            if (Autoclave.getInstance().isOnlineMode(ApplicationController.getContext())) {
                ApplicationController.getContext().startService(intent2);
            } else {
                ApplicationController.getContext().stopService(intent2);
            }

            ReadAndParseSerialService.getInstance().checkGetDataRunnableIsAlive();
        }

        updateErrorList();
        if (errorList.size() > 0) {
            Log.e("MONITOR", "Error: " + errorList.get(0).getMsg());
        }

        //after 10 seconds of communications failures, set state of microcontroller to disconnected
        if ((System.nanoTime() - nanoTimeAtLastMessageReceived) > (1000000000L * 30)) {
            Autoclave.getInstance().setMicrocontrollerReachable(false);
        } else {
            Autoclave.getInstance().setMicrocontrollerReachable(true);
        }

        //count how long a program is running since start. (In seconds) And store it into Autoclave model
        if (Autoclave.getInstance().getState() == AutoclaveState.RUNNING) {
            long diffInMs = Autoclave.getInstance().getDateObject().getTime() - dateAtProgramStart.getTime();
            secondsSinceStart = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
        } else {
            if (Autoclave.getInstance().getState() == AutoclaveState.NOT_RUNNING
                    || Autoclave.getInstance().getState() == AutoclaveState.PREPARE_TO_RUN)
                secondsSinceStart = 0;
        }
        Autoclave.getInstance().setSecondsSinceStart(secondsSinceStart);


        switch (Autoclave.getInstance().getState()) {
            case NOT_RUNNING:
                if (Autoclave.getInstance().getCurrentProgramCounter() < Autoclave.getInstance().getProgramsInRowTotal()) {
                    if (Autoclave.getInstance().getCurrentProgramCounter() != 0) {
                        nanoTimeAtLastStartCommand = System.nanoTime(); //60 seconds delay for next start
                        nanoTimeAtLastStopCommand = System.nanoTime();
                    }
                    prepareToRun(Autoclave.getInstance().getProfile().getIndex());
                }

                // IF PROGRAM HAS BEEN STARTED REMOTELY - CHANGE TO PREPARE TO RUN STATE
                if (AppConstants.IS_CERTOASSISTANT) {
                    if (Autoclave.getInstance().getData().isProgramRunning()) {
                        Autoclave.getInstance().setProgramsInRowTotal(1);
                        Autoclave.getInstance().setCurrentProgramCounter(0);
                        Autoclave.getInstance().setState(PREPARE_TO_RUN);
                        startMonitorActivity();
                    }
                }


                if (AppConstants.isIoSimulated) {
                    SimulatedFailStoppedByUser = false;
                }

                if (startButtonClicked == true) {
                    if (Autoclave.getInstance().getCurrentProgramCounter() != 0) {
                        nanoTimeAtLastStartCommand = System.nanoTime(); //60 seconds delay for next start
                        nanoTimeAtLastStopCommand = System.nanoTime();
                    }
                    Autoclave.getInstance().setState(PREPARE_TO_RUN);
                    startButtonClicked = false;
                }


                break;
            case LOCKED:
                if (AppConstants.IS_CERTOASSISTANT) {
                    if (Autoclave.getInstance().getData().isProgramRunning()) {
                        Autoclave.getInstance().setProgramsInRowTotal(1);
                        Autoclave.getInstance().setCurrentProgramCounter(0);
                        Autoclave.getInstance().setState(PREPARE_TO_RUN); //state machine sets user in the prepareToRun state
                        startMonitorActivity();
                    }
                }
                break;

            case PREPARE_TO_RUN:

                startButtonClicked = false;

                if (AppConstants.isIoSimulated) {
                    SIMUTALTE_STATE_RUNNING = true;
                }

                if (errorList.size() == 0) {

                    if (AppConstants.IS_CERTOASSISTANT) {
                        startMonitorActivity();
                    }


                    //check if autoclave is already running
                    if (Autoclave.getInstance().getData().isProgramRunning() || SIMUTALTE_STATE_RUNNING) {
                        Autoclave.getInstance().increaseCurrentProgramCounter();
                        Autoclave.getInstance().setState(AutoclaveState.RUNNING);

                        Log.e("AutoclaveMonitor", "vor create protocol");
                        if (Autoclave.getInstance().getController() == null) {
                            Log.e("AutoclaveMonitor", "controller == null");
                        }

                        int cycleNumber = Autoclave.getInstance().getController().getCycleNumber();

                        try {
                            if (AppConstants.isIoSimulated) {
                                DatabaseService db = DatabaseService.getInstance();
                                for (Protocol protocol : db.getProtocols()) {
                                    if (cycleNumber < protocol.getZyklusNumber()) {
                                        cycleNumber = protocol.getZyklusNumber();
                                    }
                                }
                                cycleNumber++;
                            }//end if io simulated
                        } catch (Exception e) {
                            cycleNumber = 1;
                        }


//set current Profile into Autoclave model


                        try {
                            indexOfProfile = Autoclave.getInstance().getProfile().getIndex();
                        } catch (Exception e) {
                            indexOfProfile = Autoclave.getInstance().getIndexOfRunningProgram();
                        }
                        if (indexOfProfile > 12) indexOfProfile = 12;
                        if (indexOfProfile < 1) indexOfProfile = 1;

                        if (Autoclave.getInstance().getProfile() == null) {
                            Profile runningProfile = databaseService.getProfileByIndex(indexOfProfile).get(0);
                            Autoclave.getInstance().setProfile(runningProfile);
                        }


//set current User into Autoclave model
                        if (Autoclave.getInstance().getUser() == null) {
                            for (User user : databaseService.getUsers()) {
                                if (user.getEmail().equals("admin")) {
                                    Autoclave.getInstance().setUser(user);
                                }
                            }
                        }
                        if (Autoclave.getInstance().getUser() == null) {
                            databaseService.insertUser(new User(
                                    "Admin",
                                    "",
                                    "Admin",
                                    "Admin",
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    BCrypt.hashpw("1234", BCrypt.gensalt()),
                                    Autoclave.getInstance().getDateObject(),
                                    true,
                                    true));
                        }
                        if (Autoclave.getInstance().getUser() == null) {
                            for (User user : databaseService.getUsers()) {
                                if (user.isAdmin() == true) {
                                    Autoclave.getInstance().setUser(user);
                                }
                            }
                        }
//set controller into autoclave model

                        startMonitorActivity();

                        Protocol protocol = new Protocol("",
                                1,
                                Autoclave.getInstance().getDateObject(),
                                null,
                                cycleNumber, //??
                                Autoclave.getInstance().getController(),
                                Autoclave.getInstance().getUser(),
                                Autoclave.getInstance().getProfile(),
                                ERROR_CODE_CONNECTION_LOST,
                                false);
                        Autoclave.getInstance().setProtocol(protocol);
                        databaseService.insertProtocol(protocol);

                        dateAtProgramStart = new Date(Autoclave.getInstance().getDateObject().getTime() - 2000);


                    } else {//no program is running

                        if (Autoclave.getInstance().getCurrentProgramCounter() > 0) {
                            if (System.nanoTime() - nanoTimeAtLastStopCommand > (1000000000L * 15)) {
                                nanoTimeAtLastStopCommand = (System.nanoTime() + (1000000000L * 15)); // + 15 seconds shift = 30 secoonds
                                Log.e("AutoclaveMonitor", "CREATE --->   CMD_STOP");
                                ReadAndParseSerialService.getInstance().sendStopCommand(true);
                            }
                        }

                        if (System.nanoTime() - nanoTimeAtLastStartCommand > (1000000000L * 30)) {
                            nanoTimeAtLastStartCommand = System.nanoTime();
                            Log.e("AutoclaveMonitor", "CREATE --->" + "CMD_STAR " + indexOfProfile + "\r\n");
                            ReadAndParseSerialService.getInstance().sendStartCommand(indexOfProfile);
                        }

                    }

                } else { //errors found
                    //wait until errors resolved
                }
                break;

            case RUNNING:

                if (Autoclave.getInstance().getData().isProgramRunning() || SIMUTALTE_STATE_RUNNING) {
                    if (Autoclave.getInstance().getProtocol() != null) {
                        if (secondsSinceStart % 20 == 0 && (secondsSinceStart != secondsOnLastRecord)) {
                            secondsOnLastRecord = secondsSinceStart;
                            databaseService.insertProtocolEntry(new ProtocolEntry(
                                    Autoclave.getInstance().getDateObject(),
                                    Autoclave.getInstance().getData().getTemp1().getCurrentValue(),
                                    Autoclave.getInstance().getData().getTemp2().getCurrentValue(),
                                    Autoclave.getInstance().getData().getTemp3().getCurrentValue(),
                                    Autoclave.getInstance().getData().getPress().getCurrentValue(),
                                    Autoclave.getInstance().getProtocol(),
                                    Autoclave.getInstance().getDebugData()[0],
                                    Autoclave.getInstance().getDebugData()[1]));
                        }
                    }
                }


                //Zeige Errormeldung, schlie?e Protokoll ab falls Error von Mikrocontroller gemeldet wurde
                if (secondsSinceStart > 6) { //errorlist detection timeoffset is 3 seconds. It follows, that detection about the end of zycle must have an offset > 3

                    if (errorList.size() > 0) {//Autoclave.getInstance().getData().isFailStoppedByUser() || Autoclave.getInstance().isMicrocontrollerReachable()==false){
                        Autoclave.getInstance().setState(AutoclaveState.RUN_CANCELED);
                        AuditLogger.getInstance().addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_EMPTY,
                                AuditLogger.ACTION_PROGRAM_FAILED,
                                AuditLogger.OBJECT_EMPTY,
                                Autoclave.getInstance().getProfile().getName() + " (" +
                                        mContext.getString(R.string.cycle) + " " + (Autoclave.getInstance().getController().getCycleNumber()) + ", " +
                                        errorList.get(0).getMsg() + " (ID: " + errorList.get(0).getErrorID() + "))", false);
                        Log.e("AutoclaveMonitor", "ERROR ID STORED INTO PROTOCOL: " + errorList.get(0).getErrorID());
                        cancelProgram(errorList.get(0).getErrorID());
                    } else if ((warningList.size() > 0 && !Autoclave.getInstance().getData().isDoorLocked()) || (!Autoclave.getInstance().getData().isProgramRunning() &&
                            !Autoclave.getInstance().getData().isProgramFinishedSucessfully()
                    )) {
                        AuditLogger.getInstance().addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_EMPTY,
                                AuditLogger.ACTION_PROGRAM_FAILED,
                                AuditLogger.OBJECT_EMPTY,
                                Autoclave.getInstance().getProfile().getName() + " (" +
                                        mContext.getString(R.string.cycle) + " " + (Autoclave.getInstance().getController().getCycleNumber()) + ", " +
                                        warningList.get(0).getMsg() + " (ID: " + warningList.get(0).getErrorID() + "))", false);
                        cancelProgram(warningList.get(0).getErrorID());
                        Autoclave.getInstance().setState(AutoclaveState.NOT_RUNNING);
                    } else {
                        if (Autoclave.getInstance().getData().isProgramFinishedSucessfully()
                                && Autoclave.getInstance().getState() != AutoclaveState.PROGRAM_FINISHED) {
                            finishProgram();
                            Autoclave.getInstance().setState(AutoclaveState.PROGRAM_FINISHED);
                        }
                    }


                }


                break;

            case DOOR_UNLOCKED:
            case PROGRAM_FINISHED:

                if (Autoclave.getInstance().getCurrentProgramCounter() < Autoclave.getInstance().getProgramsInRowTotal()) {
                    Autoclave.getInstance().setState(AutoclaveState.NOT_RUNNING);
                } else {
                    if (Autoclave.getInstance().getData().isDoorClosed() == false) {//autoklav wurde ge?ffnet
                        Autoclave.getInstance().setState(AutoclaveState.NOT_RUNNING);
                    } else if (Autoclave.getInstance().getData().isDoorLocked() == false &&
                            Autoclave.getInstance().getState() != AutoclaveState.DOOR_UNLOCKED) {//autoklav wurde ge?ffnet
                        Autoclave.getInstance().setState(AutoclaveState.DOOR_UNLOCKED);
                    }
                }


                break;
            case RUN_CANCELED:


                //disable automatic program execution
                Autoclave.getInstance().setProgramsInRowTotal(0);
                Autoclave.getInstance().setCurrentProgramCounter(0);

                SIMUTALTE_STATE_RUNNING = false;
                Autoclave.getInstance().setState(AutoclaveState.WAITING_FOR_CONFIRMATION);
                // alert-activity is currently running: wait until alert is resolved
                break;

            case WAITING_FOR_CONFIRMATION:

                if (Autoclave.getInstance().getErrorCode().equals("00000000")
                        && !Autoclave.getInstance().getData().isProgramRunning()
                        && !Autoclave.getInstance().isDoorLocked()
                        && !Autoclave.getInstance().getData().isDoorClosed())
                    Autoclave.getInstance().setState(AutoclaveState.NOT_RUNNING);
                else
                    Autoclave.getInstance().setState(AutoclaveState.WAITING_FOR_CONFIRMATION);
               /* if (mCodeEntered == true || Autoclave.getInstance().getData().isDoorClosed() == false || Autoclave.getInstance().getData().isDoorLocked() == false) {
                    Autoclave.getInstance().setState(AutoclaveState.NOT_RUNNING);
                    mCodeEntered = false;
                }*/


                break;
            default:
                break;
        }

        //falls jemand zuh?rt, wird ihm die aktuelle Fehlerliste geschickt.

        updateErrorList();

        for (AlertListener listener : alertListeners) {
            listener.onWarnListChange(errorList, warningList);
        }
        if (errorList.size() > 0 || warningList.size() > 0) {
            startAlertActivity();
        }
    }

    public void onSensorDataChange(AutoclaveData data) {
        nanoTimeAtLastMessageReceived = System.nanoTime();
    }


    private void finishProgram() {
        Log.e("monitor", "finishprogramcalled");
        Autoclave.getInstance().getProtocol().setEndTime(Autoclave.getInstance().getDateObject());
        databaseService.updateProtocolEndTime(Autoclave.getInstance().getProtocol().getProtocol_id(),
                Autoclave.getInstance().getDateObject());
        boolean isIndicatorEnabled = PreferenceManager.getDefaultSharedPreferences(ApplicationController.getContext())
                .getBoolean(AppConstants.PREFERENCE_KEY_INDICATOR_TEST, false);
        Autoclave.getInstance().getProtocol().setErrorCode(isIndicatorEnabled ?
                ERROR_CODE_INDICATOR_NOT_COMPLETED :
                ERROR_CODE_SUCCESSFULL);
        databaseService.updateProtocolErrorCode(Autoclave.getInstance().getProtocol().getProtocol_id(),
                isIndicatorEnabled ? ERROR_CODE_INDICATOR_NOT_COMPLETED : ERROR_CODE_SUCCESSFULL);
    }

    public ArrayList<Error> getErrorList() {
        return errorList;
    }


    /*
     *
     * returns true if error
     * updates errorList with list of errors
     *
     *
     */
    private void updateErrorList() {


        //Connection loss is always an error.
        if (AppConstants.isIoSimulated == false) {
            if (Autoclave.getInstance().isMicrocontrollerReachable() == false) {
                errorList.clear();
                errorList.add(new Error("ERROR: " + getErrorString(ERROR_CODE_CONNECTION_LOST),
                        fileUtils.getVideoDirectory() + mContext.getResources().getString(R.string.path_video_power),
                        Error.TYPE_WARNING,
                        ERROR_CODE_CONNECTION_LOST));
                return;
            }
        }

        switch (Autoclave.getInstance().getState()) {
            case RUNNING:
                if (secondsSinceStart > 3) { //time delay, because autoclave actualises data slowly
                    checkErrors();
                }
                break;
            case RUN_CANCELED:
            case WAITING_FOR_CONFIRMATION:
                checkErrors();
                break;

            case NOT_RUNNING:
            case LOCKED:
                checkErrors();
                if (Autoclave.getInstance().getData().isWaterLvlLow()) {
                    errorList.add(new Error(mContext.getString(R.string.fill_water),
                            mContext.getString(R.string.path_video_fill_water),
                            Error.TYPE_WARNING,
                            0));
                }
                if (Autoclave.getInstance().getData().isWaterLvlFull()) {
                    errorList.add(new Error(mContext.getString(R.string.remove_water),
                            mContext.getString(R.string.path_video_empty_water),
                            Error.TYPE_WARNING,
                            0));
                }


                break;


            case PREPARE_TO_RUN:
                checkErrors();
                try {
                    if (AppConstants.IS_CERTOASSISTANT == false) {
                        if (Autoclave.getInstance().getProfile().getIndex() == AutoclaveModelManager.getInstance().getVacuumProgramIndex()
                                && Autoclave.getInstance().getData().getTemp1().getCurrentValue() > 50) {
                            errorList.add(new Error(mContext.getString(R.string.unable_to_start_vacuum_test),
                                    "",
                                    Error.TYPE_WARNING,
                                    0));
                        }
                    }
                } catch (Exception e) {

                }
                if (Autoclave.getInstance().getData().isDoorClosed() == false) {
                    errorList.add(new Error(mContext.getString(R.string.close_door),
                            mContext.getString(R.string.path_video_door_close),
                            Error.TYPE_WARNING,
                            0));
                }
                if (Autoclave.getInstance().getData().isWaterLvlFull()) {
                    errorList.add(new Error(mContext.getString(R.string.remove_water),
                            mContext.getString(R.string.path_video_empty_water),
                            Error.TYPE_WARNING,
                            0));
                }

                if (Autoclave.getInstance().getData().isWaterLvlLow()) {
                    errorList.add(new Error(mContext.getString(R.string.fill_water),
                            mContext.getString(R.string.path_video_fill_water),
                            Error.TYPE_WARNING,
                            0));
                }


                break;


            default:


                break;
        }


    }


    private void startAlertActivity() {
        if ((System.nanoTime() - nanoIgnoreErrorTemporary) > (1000000000L * 30)) { //3 seconds past
            Intent intent = new Intent(ApplicationController.getContext(), MonitorNotificationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            ApplicationController.getContext().startActivity(intent);
        }
    }

    private void startMonitorActivity() {
        List<Profile> profileList = Autoclave.getInstance().getProfilesFromAutoclave();
        for (int i = 0; i < profileList.size(); i++) {
            if (Autoclave.getInstance().getIndexOfRunningProgram() == profileList.get(i).getIndex()) {
                Autoclave.getInstance().setProfile(profileList.get(i));
            }
        }
        Intent intent = new Intent(ApplicationController.getContext(), MonitorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        ApplicationController.getContext().startActivity(intent);
    }


    /**
     * cancelProgramm is called when any critical error occured, which forces the shutdown of sterilisation-cycle
     * This happens if Flag StoppedByUser or StoppedByError is true.
     * Finishs the Protocol with errormessage
     * Sets the State of Autoclave to RUN_CANCELED
     *
     * @param errorCode Message, which will be added at the end of the protocol
     */
    private void cancelProgram(int errorCode) {

        Log.e("monitor", "cancel program called");


        Autoclave.getInstance().getProtocol().setEndTime(Autoclave.getInstance().getDateObject());
        databaseService.updateProtocolEndTime(Autoclave.getInstance().getProtocol().getProtocol_id(), Autoclave.getInstance().getDateObject());
        databaseService.updateProtocolErrorCode(Autoclave.getInstance().getProtocol().getProtocol_id(), errorCode);
        Autoclave.getInstance().getProtocol().setErrorCode(errorCode);

        Intent intent5 = new Intent(ApplicationController.getContext(), PostProtocolsService.class);
        ApplicationController.getContext().startService(intent5);
    }


    public void sendStopCommand(boolean isForce) {

        ReadAndParseSerialService.getInstance().sendStopCommand(isForce);

        if (AppConstants.isIoSimulated) {
            SimulatedFailStoppedByUser = true;
        }

    }


    public void codeEnterded() {
        mCodeEntered = true;
        //onSensorDataChange(Autoclave.getInstance().getData());


    }

    public void removeOnAlertListener(AlertListener listener) {
        this.alertListeners.remove(listener);

    }

    public String getErrorString(int errorCode) {
        return errorMap.get(errorCode, warningMap.get(errorCode, mContext.getString(R.string.cycle_cancelled_because_of_error)));
    }

    public String getWarningString(int errorCode) {
        return warningMap.get(errorCode, null);
    }

    public String getErrorVideo(int errorCode) {
        return errorMap.get(errorCode, "");
    }

    public void cancelPrepareToRun() {
        if (Autoclave.getInstance().getState() == PREPARE_TO_RUN) {
            Autoclave.getInstance().setProgramsInRowTotal(0);
            Autoclave.getInstance().setCurrentProgramCounter(0);
            Autoclave.getInstance().setState(AutoclaveState.NOT_RUNNING);
        }

    }

    @Override
    public void onAutoclaveStateChange(AutoclaveState state) {
        Log.e("AutoclaveMonitor", "onAutoclaveStateChange " + state.toString());
        NotificationService notificationService = new NotificationService();
        for (Condition condition : CloudDatabase.getInstance().getConditionList()) {
            Log.e("AutoclaveMonitor", "check notification id: " + condition.getIfCode());
            switch (condition.getIfCode()) {
                case Condition.ID_IF_ERROR:
                    Log.e("AutoclaveMonitor", "check state " + state.toString());
                    if (state == AutoclaveState.RUN_CANCELED) {
                        Log.e("AutoclaveMonitor", "Try to send notifications now");
                        try {
                            if (condition.getEmailAddress().isEmpty() == false) {
                                Log.e("AutoclaveMonitor", "Tyring to send mail now");
                                notificationService.executePostEmailTask(condition.getEmailAddress(),
                                        Autoclave.getInstance().getUser().getFirstName(), mContext.getString(R.string.auto_gen_notification)
                                                + Autoclave.getInstance().getController().getSavetyKey(),
                                        mContext.getString(R.string.the_program) + " " +
                                                Autoclave.getInstance().getProfile().getName() + " " +
                                                mContext.getString(R.string.has_been_cancelled));
                            }
                            if (condition.getSMSNumber().isEmpty() == false) {
                                Log.e("AutoclaveMonitor", "Tyring to send sms now");
                                notificationService.executePostSmsTask(condition.getSMSNumber(),
                                        mContext.getString(R.string.auto_gen_notification) +
                                                Autoclave.getInstance().getController().getSavetyKey() +
                                                "\n" +
                                                mContext.getString(R.string.the_program) +
                                                " " +
                                                Autoclave.getInstance().getProfile().getName() +
                                                " " +
                                                mContext.getString(R.string.has_been_cancelled));
                            }
                        } catch (Exception e) {
                            Log.e("AutoclaveMonitor", "Exception during senden sms or mail: " + e.toString());
                        }
                    }
                    break;

                case Condition.ID_IF_MAINTENANCE:

                    break;

                case Condition.ID_IF_SUCCESSFUL:
                    Log.e("AutoclaveMonitor", "check state " + state.toString());
                    if (state == AutoclaveState.PROGRAM_FINISHED) {

                        Log.e("AutoclaveMonitor", "Try to send notifications now");
                        try {
                            if (condition.getEmailAddress().isEmpty() == false) {
                                if (!Autoclave.getInstance().isEmailSentForCycle()) {
                                    Log.e("AutoclaveMonitor", "Tyring to send mail now");
                                    notificationService.executePostEmailTask(condition.getEmailAddress(),
                                            "", mContext.getString(R.string.auto_gen_notification) + Autoclave.getInstance().getController().getSavetyKey(),
                                            mContext.getString(R.string.the_program) +
                                                    " " +
                                                    Autoclave.getInstance().getProfile().getName() +
                                                    " " +
                                                    mContext.getString(R.string.finished_successfully));

                                    Autoclave.getInstance().setEmailSentForCycle(true);
                                }
                            }
                            if (condition.getSMSNumber().isEmpty() == false) {
                                Log.e("AutoclaveMonitor", "Tyring to send sms now");
                                notificationService.executePostSmsTask(condition.getSMSNumber(),
                                        mContext.getString(R.string.auto_gen_notification) +
                                                Autoclave.getInstance().getController().getSavetyKey() +
                                                "\n" +
                                                mContext.getString(R.string.the_program) +
                                                " " +
                                                Autoclave.getInstance().getProfile().getName() +
                                                " " +
                                                mContext.getString(R.string.finished_successfully)
                                );
                            }
                        } catch (Exception e) {
                            Log.e("AutoclaveMonitor", "Exception during senden sms or mail: " + e.toString());
                        }
                    }
                    break;

            }//end switch case
        }//end for

    }

    public void ignoreErrorsTemporary() {
        nanoIgnoreErrorTemporary = System.nanoTime();

    }

    @Override
    public void onConnectionStatusChange(boolean connectionStatus) {

    }


    private void checkErrors() {

        try {
            String binary = String.format("%32s", new BigInteger(Autoclave.getInstance().getErrorCode(), 16).toString(2)).replace(" ", "0");

            errorList = new ArrayList<>();
            int errorcode = 0;
            for (int i = 0; i < 32; i++)
                if (binary.charAt(31 - i) == '1') {
                    errorcode = i + 1;
                    errorList.add(new Error("ERROR: " + getErrorString(errorcode),
                            "",
                            Error.TYPE_ERROR,
                            errorcode));
                }
        } catch (Exception e) {
            e.printStackTrace();
        }

        checkWarnings();
    }

    private void checkWarnings() {

        try {
            if (Autoclave.getInstance().getWarningList() == null)
                return;
            String binary = String.format("%40s", new BigInteger(Autoclave.getInstance().getWarningList()
                    , 16).toString(2)).replace(" ", "0");

            Log.e("warning", binary);

            warningList = new ArrayList<>();
            int errorcode = 0;
            for (int i = 0; i < 40; i++)
                if (binary.charAt(39 - i) == '1') {
                    errorcode = i + 1;
                    if (getWarningString(errorcode) != null)
                        warningList.add(new Error("WARNING: " + getWarningString(errorcode),
                                "",
                                Error.TYPE_WARNING,
                                errorcode));
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDebugChanged() {
        if (Autoclave.getInstance().getData().isProgramRunning() && Autoclave.getInstance().getProtocol() != null) {
            Log.e("Debug Data", Autoclave.getInstance().getDebugData()[0] + " " + Autoclave.getInstance().getDebugData()[1]);
            databaseService.insertProtocolEntry(new ProtocolEntry(
                    Autoclave.getInstance().getDateObject(),
                    Autoclave.getInstance().getData().getTemp1().getCurrentValue(),
                    Autoclave.getInstance().getData().getTemp2().getCurrentValue(),
                    Autoclave.getInstance().getData().getTemp3().getCurrentValue(),
                    Autoclave.getInstance().getData().getPress().getCurrentValue(),
                    Autoclave.getInstance().getProtocol(),
                    Autoclave.getInstance().getDebugData()[0],
                    Autoclave.getInstance().getDebugData()[1]));
        }
    }
}
