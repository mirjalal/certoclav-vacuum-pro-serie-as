package com.certoclav.app.model;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

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
import com.certoclav.app.listener.SensorDataListener;
import com.certoclav.app.monitor.MonitorNotificationActivity;
import com.certoclav.app.service.CloudSocketService;
import com.certoclav.app.service.ReadAndParseSerialService;
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.bcrypt.BCrypt;
import com.certoclav.library.certocloud.CloudDatabase;
import com.certoclav.library.certocloud.Condition;
import com.certoclav.library.certocloud.NotificationService;

import java.util.ArrayList;


public class AutoclaveMonitor implements SensorDataListener, ConnectionStatusListener, AutoclaveStateListener {

    Context mContext = ApplicationController.getContext();


    public static final int INDEX_ERROR_SUCCESSFUL = 0;
    public static final int INDEX_ERROR_STOPPED_BY_ERROR = 1;
    public static final int INDEX_ERROR_STOPPED_BY_USER = 2;
    public static final int INDEX_ERROR_CONNECTION_LOST = 3;

    private long nanoTimeAtLastMessageReceived = 0;


    ArrayList<AlertListener> alertListeners = new ArrayList<AlertListener>();
    private ArrayList<Error> errorList = new ArrayList<Error>(); //list of current errors
    private DatabaseService databaseService;
    private boolean startButtonClicked = false;
    private long nanoTimeAtLastStopCommand = 0;

    private long nanoTimeAtLastServiceCheck = 0;
    private long nanosAtProgramStart = 0;
    private long nanoTimeAtLastStartCommand = 0;
    long secondsSinceStart = 0;
    private long secondsOnLastRecord = 5; //init not to 0
    private long nanoIgnoreErrorTemporary = 0;


    private Integer indexOfProfile = null;


    //IO SIMULATION
    public static boolean SimulatedLockSwitch = true;
    public static boolean PowerOffDeviceAutomatically = false;
    public static boolean SimulatedOvertempSwitch = true;
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
        Autoclave.getInstance().setOnSensorDataListener(this);
        Autoclave.getInstance().setOnConnectionStatusListener(this);
        databaseService = new DatabaseService(ApplicationController.getContext());
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

    public void updateStateMachine() {

        //check if read services are still running
        if ((System.nanoTime() - nanoTimeAtLastServiceCheck) > (1000000000L * 3)) { //3 seconds past

            nanoTimeAtLastServiceCheck = System.nanoTime();

            Intent intent2 = new Intent(ApplicationController.getContext(), CloudSocketService.class);
            ApplicationController.getContext().startService(intent2);

        }


        updateErrorList();
        Log.e("MONITOR", "State: " + Autoclave.getInstance().getState().toString() + " " + secondsSinceStart + " " + "numErrors: " + errorList.size() + "isDoorClosed() " + Autoclave.getInstance().getData().isDoorClosed() + "isDoorLocked" + Autoclave.getInstance().getData().isDoorLocked());
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
            long nanos = System.nanoTime() - nanosAtProgramStart;
            secondsSinceStart = ((long) (nanos / 1000000000));
        } else {
            secondsSinceStart = 0;
        }
        Autoclave.getInstance().setSecondsSinceStart(secondsSinceStart);


        switch (Autoclave.getInstance().getState()) {
            case NOT_RUNNING:
                if (Autoclave.getInstance().getCurrentProgramCounter() < Autoclave.getInstance().getProgramsInRowTotal()) {
                    Log.e("AutoclaveMonitor", "CALL PREPARE TO RUN AUTOMATICALLY: ");
                    if (Autoclave.getInstance().getCurrentProgramCounter() != 0) {
                        nanoTimeAtLastStartCommand = System.nanoTime(); //60 seconds delay for next start
                        nanoTimeAtLastStopCommand = System.nanoTime();
                    }
                    prepareToRun(Autoclave.getInstance().getProfile().getIndex());
                }

                // IF PROGRAM HAS BEEN STARTED REMOTELY - CHANGE TO PREPARE TO RUN STATE
                if (Autoclave.getInstance().getData().isDoorLocked()) {
                    Autoclave.getInstance().setProgramsInRowTotal(1);
                    Autoclave.getInstance().setCurrentProgramCounter(0);
                    Autoclave.getInstance().setState(AutoclaveState.PREPARE_TO_RUN);
                    startMonitorActivity();
                }


                if (AppConstants.isIoSimulated) {
                    SimulatedFailStoppedByUser = false;
                }

                if (startButtonClicked == true) {
                    if (Autoclave.getInstance().getCurrentProgramCounter() != 0) {
                        nanoTimeAtLastStartCommand = System.nanoTime(); //60 seconds delay for next start
                        nanoTimeAtLastStopCommand = System.nanoTime();
                    }
                    Autoclave.getInstance().setState(AutoclaveState.PREPARE_TO_RUN);
                    startButtonClicked = false;
                }


                break;
            case LOCKED:
                if (AppConstants.IS_CERTOASSISTANT) {
                    if (Autoclave.getInstance().getData().isProgramRunning()) {
                        Autoclave.getInstance().setState(AutoclaveState.PREPARE_TO_RUN);
                    }
                }
                break;

            case PREPARE_TO_RUN:

                startButtonClicked = false;

                if (AppConstants.isIoSimulated) {
                    SIMUTALTE_STATE_RUNNING = true;
                }

                if (errorList.size() == 0) {


                    //check if autoclave is already running
                    if (Autoclave.getInstance().getData().isDoorLocked() || SIMUTALTE_STATE_RUNNING) {
                        Autoclave.getInstance().increaseCurrentProgramCounter();
                        Autoclave.getInstance().setState(AutoclaveState.RUNNING);

                        Log.e("AutoclaveMonitor", "vor create protocol");
                        if (Autoclave.getInstance().getController() == null) {
                            Log.e("AutoclaveMonitor", "controller == null");
                        }

                        int cycleNumber = Autoclave.getInstance().getController().getCycleNumber();

                        try {
                            if (AppConstants.isIoSimulated) {
                                DatabaseService db = new DatabaseService(mContext);
                                for (Protocol protocol : db.getProtocols()) {
                                    if (cycleNumber < protocol.getZyklusNumber()) {
                                        cycleNumber = protocol.getZyklusNumber();
                                    }
                                }
                                cycleNumber++;
                            }//end if io simulated
                        }catch (Exception e){
                            cycleNumber = 1;
                        }


//set current Profile into Autoclave model
                        int indexOfRunningProgram = 1;
                        try {
                            indexOfRunningProgram = Autoclave.getInstance().getIndexOfRunningProgram();
                            if (indexOfRunningProgram > 12) indexOfRunningProgram = 12;
                            if (indexOfRunningProgram < 1) indexOfRunningProgram = 1;
                        }catch(Exception e){
                            indexOfRunningProgram = 1;
                            Autoclave.getInstance().setIndexOfRunningProgram(1);
                        }

                        Profile runningProfile = databaseService.getProfileByIndex(indexOfRunningProgram).get(0);
                        Autoclave.getInstance().setProfile(runningProfile);
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
                                    "",
                                    "",
                                    "",
                                    "admin",
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    BCrypt.hashpw("1234", BCrypt.gensalt()),
                                    Autoclave.getInstance().getDateObject(),
                                    false,
                                    true));
                        }
                        if (Autoclave.getInstance().getUser() == null) {
                            for (User user : databaseService.getUsers()) {
                                if (user.getEmail().equals("admin")) {
                                    Autoclave.getInstance().setUser(user);
                                }
                            }
                        }
//set controller into autoclave model

                        startMonitorActivity();

                        Protocol protocol = new Protocol("",
                                1,
                                Autoclave.getInstance().getDateObject(),
                                Autoclave.getInstance().getDateObject(),
                                cycleNumber, //??
                                Autoclave.getInstance().getController(),
                                Autoclave.getInstance().getUser(),
                                Autoclave.getInstance().getProfile(),
                                -2, //error id for connection loss
                                false);
                        Autoclave.getInstance().setProtocol(protocol);
                        int retval = databaseService.insertProtocol(protocol);

                        nanosAtProgramStart = System.nanoTime();


                    } else {//no program is running

                        if (Autoclave.getInstance().getCurrentProgramCounter() > 0) {
                            if (System.nanoTime() - nanoTimeAtLastStopCommand > (1000000000L * 15)) {
                                nanoTimeAtLastStopCommand = (System.nanoTime() + (1000000000L * 15)); // + 15 seconds shift = 30 secoonds
                                Log.e("AutoclaveMonitor", "SEND --->   CMD_STOP");
                                ReadAndParseSerialService.getInstance().sendStopCommand();
                            }
                        }

                        if (System.nanoTime() - nanoTimeAtLastStartCommand > (1000000000L * 30)) {
                            nanoTimeAtLastStartCommand = System.nanoTime();
                            Log.e("AutoclaveMonitor", "SEND --->" + "CMD_STAR " + indexOfProfile + "\r\n");
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
                                    Autoclave.getInstance().getData().getPress().getCurrentValue(),
                                    Autoclave.getInstance().getProtocol()));
                        }
                    }
                }


                //Zeige Errormeldung, schlie?e Protokoll ab falls Error von Mikrocontroller gemeldet wurde
                if (secondsSinceStart > 6) { //errorlist detection timeoffset is 3 seconds. It follows, that detection about the end of zycle must have an offset > 3

                    if (errorList.size() > 0) {//Autoclave.getInstance().getData().isFailStoppedByUser() || Autoclave.getInstance().isMicrocontrollerReachable()==false){
                        Autoclave.getInstance().setState(AutoclaveState.RUN_CANCELED);
                        cancelProgram(errorList.get(0).getErrorID());
                    } else {
                        if (Autoclave.getInstance().getData().isDoorLocked() == false) { //Autoclave.getInstance().getData().isProgramRunning() == false &&  program finished and door is ready to open (unlocked)

                            if (Autoclave.getInstance().getData().isProgramRunning() == true) {
                                //bug in firmware. this is not possible after program has started => stop program manually
                                ReadAndParseSerialService.getInstance().sendStopCommand();
                            }

                            finishProgram();
                            Autoclave.getInstance().setState(AutoclaveState.PROGRAM_FINISHED);

                        }
                    }


                }


                break;

            case PROGRAM_FINISHED:

                if (Autoclave.getInstance().getCurrentProgramCounter() < Autoclave.getInstance().getProgramsInRowTotal()) {
                    Autoclave.getInstance().setState(AutoclaveState.NOT_RUNNING);
                } else {
                    if (Autoclave.getInstance().getData().isDoorClosed() == false) {//autoklav wurde ge?ffnet
                        Autoclave.getInstance().setState(AutoclaveState.NOT_RUNNING);
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

                if (mCodeEntered == true || Autoclave.getInstance().getData().isDoorClosed() == false || Autoclave.getInstance().getData().isDoorLocked() == false) {
                    Autoclave.getInstance().setState(AutoclaveState.NOT_RUNNING);
                    mCodeEntered = false;
                }


                break;
            default:
                break;
        }

        //falls jemand zuh?rt, wird ihm die aktuelle Fehlerliste geschickt.

        updateErrorList();

        for (AlertListener listener : alertListeners) {
            listener.onWarnListChange(errorList);
        }
        if (errorList.size() > 0) {
            startAlertActivity();
        }
    }

    public void onSensorDataChange(AutoclaveData data) {
        nanoTimeAtLastMessageReceived = System.nanoTime();
    }


    private void finishProgram() {
        Log.e("monitor", "finishprogramcalled");
        Autoclave.getInstance().getProtocol().setEndTime(Autoclave.getInstance().getDateObject());
        databaseService.updateProtocolEndTime(Autoclave.getInstance().getProtocol().getProtocol_id(), Autoclave.getInstance().getDateObject());
        Autoclave.getInstance().getProtocol().setErrorCode(INDEX_ERROR_SUCCESSFUL);
        databaseService.updateProtocolErrorMessage(Autoclave.getInstance().getProtocol().getProtocol_id(), INDEX_ERROR_SUCCESSFUL);
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

        Log.e("MONITOR", "seconds since start: " + secondsSinceStart);
        errorList.clear();


        //Connection loss is always an error.
        if (AppConstants.isIoSimulated == false) {
            if (Autoclave.getInstance().isMicrocontrollerReachable() == false) {
                errorList.add(new Error("ERROR: " + "-2",
                        mContext.getResources().getString(R.string.path_video_power),
                        Error.TYPE_ERROR,
                        -2));
            }
        }

        switch (Autoclave.getInstance().getState()) {
            case RUNNING:
                if (secondsSinceStart > 3) { //time delay, because autoclave actualises data slowly
                    if (Autoclave.getInstance().getData().isFailStoppedByUser()) {
                        errorList.add(new Error("ERROR: " + "-1 (Program cancelled manually)",
                                "",
                                Error.TYPE_ERROR,
                                -1));
                    }
                    if (Autoclave.getInstance().getErrorCode() != 0) {
                        errorList.add(new Error("ERROR " + Autoclave.getInstance().getErrorCode(),
                                "",
                                Error.TYPE_ERROR,
                                Autoclave.getInstance().getErrorCode()));
                    }
                }
                break;
            case RUN_CANCELED:
            case WAITING_FOR_CONFIRMATION:


                if (Autoclave.getInstance().getData().isFailStoppedByUser()) {
                    errorList.add(new Error("ERROR: -1 (Program cancelled by manually)",
                            "",//mContext.getResources().getString(R.string.path_video_place_item),
                            Error.TYPE_ERROR,
                            -1));
                }
                if (Autoclave.getInstance().getErrorCode() != 0) {
                    errorList.add(new Error("ERROR " + Autoclave.getInstance().getErrorCode(),
                            "",//mContext.getResources().getString(R.string.path_video_place_item),
                            Error.TYPE_ERROR,
                            Autoclave.getInstance().getErrorCode()));
                }


                break;

            case NOT_RUNNING:
            case LOCKED:
                if (Autoclave.getInstance().getData().isWaterLvlLow()) {
                    errorList.add(new Error("Please fill water container",
                            mContext.getString(R.string.path_video_fill_water),
                            Error.TYPE_WARNING,
                            0));
                }
                if (Autoclave.getInstance().getData().isWaterLvlFull()) {
                    errorList.add(new Error("Please empty the water waste container",
                            mContext.getString(R.string.path_video_empty_water),
                            Error.TYPE_WARNING,
                            0));
                }


                break;


            case PREPARE_TO_RUN:
                try {
                    if (Autoclave.getInstance().getProfile().getIndex() == 9 && Autoclave.getInstance().getData().getTemp1().getCurrentValue() > 50) {
                        errorList.add(new Error("Unable to start the vacuum test. Please wait until the chamber is cooled down",
                                "",
                                Error.TYPE_WARNING,
                                0));
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
        //Autoclave.getInstance().setProfile(Autoclave.getInstance().getUserDefinedProgram());
        //Intent intent = new Intent(ApplicationController.getContext(), MonitorActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //ApplicationController.getContext().startActivity(intent);
    }


    /**
     * cancelProgramm is called when any critical error occured, which forces the shutdown of sterilisation-cycle
     * This happens if Flag StoppedByUser or StoppedByError is true.
     * Finishs the Protocol with errormessage
     * Sets the State of Autoclave to RUN_CANCELED
     *
     * @param errorMessage Message, which will be added at the end of the protocol
     */
    private void cancelProgram(int errorMessage) {

        Log.e("monitor", "cancel program called");


        Autoclave.getInstance().getProtocol().setEndTime(Autoclave.getInstance().getDateObject());
        databaseService.updateProtocolEndTime(Autoclave.getInstance().getProtocol().getProtocol_id(), Autoclave.getInstance().getDateObject());
        databaseService.updateProtocolErrorMessage(Autoclave.getInstance().getProtocol().getProtocol_id(), errorMessage);
        Autoclave.getInstance().getProtocol().setErrorCode(errorMessage);
    }


    public void sendStopCommand() {

        ReadAndParseSerialService.getInstance().sendStopCommand();

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

        try {
            return "ERROR ID: " + Integer.toString(errorCode);
        } catch (Exception e) {

        }
        return "Error: 00";

    }

    public void cancelPrepareToRun() {
        if (Autoclave.getInstance().getState() == AutoclaveState.PREPARE_TO_RUN) {
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
                                        "", "Automatic generated notification from autoclave SN: " + Autoclave.getInstance().getController().getSerialnumber(),
                                        "The program" + " " +
                                                Autoclave.getInstance().getProfile().getName() + " " +
                                                "has been cancelled right now.");
                            }
                            if (condition.getSMSNumber().isEmpty() == false) {
                                Log.e("AutoclaveMonitor", "Tyring to send sms now");
                                notificationService.executePostSmsTask(condition.getSMSNumber(),
                                        "Automatic generated notification from autoclave with SN " +
                                                Autoclave.getInstance().getController().getSerialnumber() +
                                                "\n" +
                                                "The program" +
                                                " " +
                                                Autoclave.getInstance().getProfile().getName() +
                                                " " +
                                                "has been cancelled just now.");
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
                                Log.e("AutoclaveMonitor", "Tyring to send mail now");
                                notificationService.executePostEmailTask(condition.getEmailAddress(),
                                        "", "Automatic generated notification from autoclave SN: " + Autoclave.getInstance().getController().getSerialnumber(),
                                        "The program" +
                                                " " +
                                                Autoclave.getInstance().getProfile().getName() +
                                                " " +
                                                "finished successfully just now.");
                            }
                            if (condition.getSMSNumber().isEmpty() == false) {
                                Log.e("AutoclaveMonitor", "Tyring to send sms now");
                                notificationService.executePostSmsTask(condition.getSMSNumber(),
                                        "Automatic generated notification from autoclave with SN " +
                                                Autoclave.getInstance().getController().getSerialnumber() +
                                                "\n" +
                                                "The program" +
                                                " " +
                                                Autoclave.getInstance().getProfile().getName() +
                                                " " +
                                                "finished successfully just now."
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
        // TODO Auto-generated method stub

    }


}
