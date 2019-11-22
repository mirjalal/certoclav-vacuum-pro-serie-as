package com.certoclav.app.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.database.Controller;
import com.certoclav.app.database.Profile;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.database.User;
import com.certoclav.app.listener.AutoclaveStateListener;
import com.certoclav.app.listener.CalibrationListener;
import com.certoclav.app.listener.ConnectionStatusListener;
import com.certoclav.app.listener.ControllerInfoListener;
import com.certoclav.app.listener.ProfileListener;
import com.certoclav.app.listener.ProtocolListener;
import com.certoclav.app.listener.SensorDataListener;
import com.certoclav.app.listener.UserProgramListener;
import com.certoclav.app.listener.WifiListener;
import com.certoclav.app.util.ProfileSyncedListener;
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.models.DeviceModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import android_serialport_api.SerialService;


/**
 * This class is the Autoclave class model. It is a singleton class.
 */
public class Autoclave extends Observable {

    private int programsInRowTotal = 0;
    private int currentProgramCounter = 0;
    private SerialService serialService = null;
    private SerialService serialServiceLabelPrinter = null;
    private ArrayList<String> listContent = new ArrayList<String>();
    private boolean isDebugMode;
    private boolean isDoorLocked;
    private String[] debugData;
    private String warningList;
    private User selectedAdminUser;

    public enum PROGRAM_STEPS {
        ATMOSPHERIC_PRESSURE("PA"),
        PRE_HEATING("PH"),
        WARMING_UP("SW"),
        VACUUM_PULSE_1("SF11"),
        VACUUM_PULSE_1_("SF12"),
        VACUUM_PULSE_2("SF21"),
        VACUUM_PULSE_2_("SF22"),
        VACUUM_PULSE_3("SF31"),
        VACUUM_PULSE_3_("SF32"),
        HEATING("SH"),
        STABILIZATION("ST"),
        STERILIZATION("SS"),
        DISCHARGE("SDC"),
        DRYING("SD"),
        VENTILATION("SV"),
        LEVELING("SL"),
        FINISHED("SE"),
        COOLING_DOWN("SC"),
        MAINTAIN_TEMP("SM"),
        START_PROCESS("VI"),
        EMPTY_WAIT("VW"),
        EMPTY_TEST("VT"),
        V_LEVELING("VL"),
        NOT_DEFINED("-1");

        private String value;

        PROGRAM_STEPS(final String value) {
            this.value = value;
        }

        private static final Map<String, PROGRAM_STEPS> map = new HashMap<>();

        static {
            for (PROGRAM_STEPS en : values()) {
                map.put(en.value, en);
            }
        }

        public static PROGRAM_STEPS valueFor(String name) {
            return map.get(name);
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return this.getValue();
        }


    }


    private String programStep = "";
    private float timeOrPercent = 0;
    private SerialService serialServiceProtocolPrinter = null;
    private SerialService serialServiceInternalPrinter = null;
    private List<ProfileSyncedListener> profileSyncedListeners;

    public ArrayList<String> getListContent() {
        return listContent;
    }


    public SerialService getSerialsServiceProtocolPrinter() {
        if (serialServiceProtocolPrinter == null) {
            if (AppConstants.TABLET_TYPE.equals(AppConstants.TABLET_TYPE_FAYTECH)) {
                serialServiceProtocolPrinter = new SerialService("/dev/ttyS5", 9600);//COM1
            } else {
                serialServiceProtocolPrinter = new SerialService("/dev/ttymxc0", 9600);//COM1
            }
        }
        return serialServiceProtocolPrinter;
    }

    public SerialService getSerialsServiceInternalPrinter() {
        if (serialServiceInternalPrinter == null) {
            if (AppConstants.TABLET_TYPE.equals(AppConstants.TABLET_TYPE_FAYTECH)) {
                serialServiceInternalPrinter = new SerialService("/dev/ttyS0", 9600);//COM4
            } else {
                serialServiceInternalPrinter = new SerialService("/dev/ttyS4", 9600);
            }
        }
        return serialServiceInternalPrinter;
    }


    public void setListContent(ArrayList<String> listContent) {
        this.listContent = listContent;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public void setDebugMode(boolean debugMode) {
        isDebugMode = debugMode;
    }

    public int getProgramsInRowTotal() {
        return programsInRowTotal;
    }


    public void setProgramsInRowTotal(int programsInRowTotal) {
        this.programsInRowTotal = programsInRowTotal;
    }


    public int getCurrentProgramCounter() {
        return currentProgramCounter;
    }


    public void setCurrentProgramCounter(int currentProgramCounter) {
        this.currentProgramCounter = currentProgramCounter;
    }


    public float getTimeOrPercent() {
        return timeOrPercent;
    }

    public void setTimeOrPercent(float timeOrPercent) {
        this.timeOrPercent = timeOrPercent;
    }

    private boolean preheat = false;

    public boolean isPreheat() {
        return preheat;
    }


    public void setPreheat(boolean preheat) {
        this.preheat = preheat;
    }


    public boolean isKeepTemp() {
        return keepTemp;
    }


    public void setKeepTemp(boolean keepTemp) {
        this.keepTemp = keepTemp;
    }


    private boolean keepTemp = false;

    private boolean wifiConnected = false;
    private Controller controller = null;

    private long secondsSinceStart = 0;
    private Date dDate = new Date();
    private String errorCode = "00000000";
    private String date = "";
    private String time = "";
    private int indexOfRunningProgram = 0;
    private Profile userDefinedProgram = null;

    public ArrayList<Profile> getProfilesFromAutoclave() {

        if (AppConstants.isIoSimulated) {
            updateSimulatedPrograms();
        }
        return profilesFromAutoclave;
    }

    public Profile getProfileByIndex(int index) {
        if (profilesFromAutoclave.indexOf(new Profile(index)) == -1) {
            return new Profile(
                    "",
                    1,
                    ApplicationController.getContext().getString(R.string.default_program_name),
                    3,
                    10,
                    121f,
                    200f,
                    1,
                    10,
                    "",
                    true,
                    true,
                    true,
                    Autoclave.getInstance().getController(),
                    index,
                    false,
                    false,
                    false,
                    false,
                    10,
                    5f,
                    10f);
        }
        return profilesFromAutoclave.get(profilesFromAutoclave.indexOf(new Profile(index)));
    }

    public int getUnusedProfileIndex() {
        for (int i = 1; i <= AppConstants.MAX_PROGRAM_COUNT; i++) {
            if (profilesFromAutoclave.indexOf(new Profile(i)) == -1)
                return i;
        }
        return AppConstants.MAX_PROGRAM_COUNT;
    }


    private ArrayList<Profile> profilesFromAutoclave = new ArrayList<>();

/*
    public Profile getUserDefinedProgram() {
        if (userDefinedProgram == null) {
            try {
                DatabaseService db = new DatabaseService(ApplicationController.getContext());
                List<Profile> profilesFromDb = db.getProfiles();
                for (Profile profile : profilesFromDb) {
                    if (profile.getIndex() == 7) {
                        userDefinedProgram = profile;
                        break;
                    }
                }
            } catch (Exception e) {
                userDefinedProgram = new Profile("", 1, "USER DEFINED", 3, 5, 134, 210, 0, 10, "Vacuum times: User defined\nSterilization temperature: User defined\nDry time: User defined", true, true, false, controller, 7);
            }
        }
        return userDefinedProgram;
    }
*/

/*
    public void setUserDefinedProgram(Profile userDefinedProgram) {
        this.userDefinedProgram = userDefinedProgram;
        for (UserProgramListener listener : userProgramListeners) {
            listener.onUserProgramReceived();
        }
    }
*/

    public void setSelectedAdminUser(User selectedAdminUser) {
        this.selectedAdminUser = selectedAdminUser;
    }

    public User getSelectedAdminUser() {
        return selectedAdminUser;
    }

    public long getSecondsSinceStart() {
        return secondsSinceStart;
    }

    public PROGRAM_STEPS getProgramStep() {
        PROGRAM_STEPS program_step = PROGRAM_STEPS.valueFor(programStep);
        if (program_step == null)
            return PROGRAM_STEPS.NOT_DEFINED;
        return program_step;
    }

    public void setProgramStep(String programStep) {

        //If Step doesn't change it means that state also hasn't changed
        if (this.programStep.equals(programStep))
            return;

        this.programStep = programStep;
        for (AutoclaveStateListener listener : autoclaveStateListeners) {
            listener.onAutoclaveStateChange(getState()); //maybe this causes to problems because state didnt change but observers called
        }
    }

    private boolean isEmailSentForCycle = false;

    public void setEmailSentForCycle(boolean value) {
        isEmailSentForCycle = value;
    }

    boolean isEmailSentForCycle() {
        return isEmailSentForCycle;
    }

    private boolean isAuditLogWritten = false;

    public void setAuditLogWritten(boolean value) {
        isAuditLogWritten = value;
    }

    public boolean isAuditLogWritten() {
        return isAuditLogWritten;
    }

    public void setSecondsSinceStart(long secondsSinceStart) {
        this.secondsSinceStart = secondsSinceStart;
    }

    public SerialService getSerialsService() {
        if (serialService == null) {
            if (AppConstants.TABLET_TYPE.equals(AppConstants.TABLET_TYPE_FAYTECH)) {
                serialService = new SerialService("/dev/ttyUSB0", 38400);
            } else {
                serialService = new SerialService("/dev/ttymxc3", 9600);
            }
        }
        return serialService;
    }

    public SerialService getSerialsServiceLabelPrinter() {
        if (serialServiceLabelPrinter == null) {
            if (AppConstants.TABLET_TYPE.equals(AppConstants.TABLET_TYPE_FAYTECH)) {
                serialServiceLabelPrinter = new SerialService("/dev/ttyS4", 9600);//COM0
            } else {
                serialServiceLabelPrinter = new SerialService("/dev/ttymxc1", 9600);//COM2
            }
        }
        return serialServiceLabelPrinter;
    }

    public void setController(Controller controller) {
        this.controller = controller;

        for (ControllerInfoListener listener : controllerInfoListeners) {
            listener.onControllerInfoReceived();
        }

    }


    public boolean isWifiConnected() {
        return wifiConnected;
    }


    ArrayList<ConnectionStatusListener> connectionStatusListeners = new ArrayList<ConnectionStatusListener>();
    ArrayList<SensorDataListener> sensorDataListeners = new ArrayList<SensorDataListener>();
    ArrayList<AutoclaveStateListener> autoclaveStateListeners = new ArrayList<>();
    ArrayList<ProfileListener> profileListeners = new ArrayList<ProfileListener>();
    ArrayList<ProtocolListener> protocolListeners = new ArrayList<ProtocolListener>();
    ArrayList<WifiListener> wifiListeners = new ArrayList<WifiListener>();
    ArrayList<ControllerInfoListener> controllerInfoListeners = new ArrayList<ControllerInfoListener>();
    ArrayList<UserProgramListener> userProgramListeners = new ArrayList<UserProgramListener>();
    ArrayList<CalibrationListener> calibraionListeners = new ArrayList<CalibrationListener>();

    //Data from/for Database table
    private User user = null;


    private Protocol protocol = null;
    private Profile profile = null;


    //get_profile_steps_command
    private int mStoredProfileSteps = 0;

    //total_number_of_cycles_command
    private int mOperationCycles = 0;


    //connectionStates
    private boolean microcontrollerReachable = false;
    private AutoclaveData data = null;

    public AutoclaveData getData() {
        return data;
    }

    //General Parameters
    private Parameter parameterHeaterPID = new Parameter(AppConstants.PARAMETER_INDEX_PID);
    private Parameter parameterHeaterPOW = new Parameter(AppConstants.PARAMETER_INDEX_POW);
    private Parameter parameterSavety = new Parameter(AppConstants.PARAMETER_INDEX_SAVETY);


    public boolean isMulticontrolOrEssential() {
        return true;
/*
        if(BluetoothAdapter.getDefaultAdapter() != null){
			if(!BluetoothAdapter.getDefaultAdapter().isEnabled()){
			//	Log.e("Autoclave","BLUETOOTH FALSE");
				return false;
				
			}
			//Log.e("Autoclave","BLUETOOTH TRUE");
		return true;
		}else{
			return false;
		}
	*/
    }

    public Parameter getParameterHeaterPID() {
        return parameterHeaterPID;
    }

    public void setParameterHeaterPID(Parameter parameterHeaterPID) {
        this.parameterHeaterPID = parameterHeaterPID;
    }

    public Parameter getParameterHeaterPOW() {
        return parameterHeaterPOW;
    }

    public void setParameterHeaterPOW(Parameter parameterHeaterPOW) {
        this.parameterHeaterPOW = parameterHeaterPOW;
    }

    public Parameter getParameterSavety() {
        return parameterSavety;
    }

    public void setParameterSavety(Parameter parameterSavety) {
        this.parameterSavety = parameterSavety;
    }

    private AutoclaveState state = AutoclaveState.LOCKED;

    public AutoclaveState getState() {
        return state;
    }

    public void setState(AutoclaveState state) {
        this.state = state;

        for (AutoclaveStateListener listener : autoclaveStateListeners) {
            listener.onAutoclaveStateChange(state);
        }
    }


    public void setDoorLocked(boolean doorLocked) {
        isDoorLocked = doorLocked;
    }

    public boolean isDoorLocked() {
        return isDoorLocked;
    }

    private Date mLastSelfTest = null;//Date

    //valveEnabled == false if valve is locked and is not able to blow steam out
    private boolean valveEnabled = true;


    private static Autoclave instance = new Autoclave();

    private Autoclave() {
        data = new AutoclaveData();
        controller = new Controller("unknown", "unknown", "unknown", "unknown", 0, "unknown");
        profileSyncedListeners = new ArrayList<>();
    }

    public boolean isFDAEnabled() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationController.getContext());
        return prefs.getBoolean(AppConstants.PREFERENCE_KEY_ENABLE_FDA, false);
    }

    private void updateSimulatedPrograms() {
        Context context = ApplicationController.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        profilesFromAutoclave.clear();
        if (prefs.getString(AppConstants.PREFERENCE_KEY_AUTOCLAVE_MODEL, "TLV-50").equals("TLV-75FA")) {
            //default profiles
            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_1_name),
                    0,
                    3,
                    134.0f,
                    2.1f,
                    0,
                    0,
                    ApplicationController.getContext().getString(R.string.program_1_description),
                    true,
                    true,
                    false,
                    null,
                    1,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_2_name),
                    1,
                    3,
                    134f,
                    2.1f,
                    0,
                    0,
                    ApplicationController.getContext().getString(R.string.program_2_description),
                    true,
                    true,
                    false,
                    null,
                    2,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_3_name),
                    0,
                    15,
                    121.0f,
                    1.1f,
                    0,
                    0,
                    ApplicationController.getContext().getString(R.string.program_3_description),
                    true,
                    true,
                    false,
                    null,
                    1,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_4_name),
                    1,
                    15,
                    121.0f,
                    1.1f,
                    0,
                    0,
                    ApplicationController.getContext().getString(R.string.program_4_description),
                    true,
                    true,
                    false,
                    null,
                    1,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_5_name),
                    0,
                    15,
                    121.0f,
                    1.1f,
                    0,
                    00,
                    ApplicationController.getContext().getString(R.string.program_5_description),
                    true,
                    true,
                    true,
                    null,
                    1,
                    false,
                    false,
                    false, false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_6_name),
                    1,
                    15,
                    121.0f,
                    1.1f,
                    0,
                    0,
                    ApplicationController.getContext().getString(R.string.program_6_description),
                    true,
                    true,
                    true,
                    null,
                    1,
                    false,
                    false,
                    false, false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_7_name),
                    0,
                    30,
                    121.0f,
                    1.1f,
                    0,
                    0,
                    ApplicationController.getContext().getString(R.string.program_7_description),
                    true,
                    true,
                    true,
                    null,
                    1,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_8_name),
                    1,
                    30,
                    121.0f,
                    1.1f,
                    0,
                    0,
                    ApplicationController.getContext().getString(R.string.program_8_description),
                    true,
                    true,
                    true,
                    null,
                    1,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));
        } else {

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_13_name),
                    3,
                    4,
                    134.0f,
                    2.1f,
                    0,
                    4,
                    ApplicationController.getContext().getString(R.string.program_13_description),
                    true,
                    true,
                    false,
                    null,
                    1,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_14_name),
                    1,
                    0,
                    0f,
                    -0.8f,
                    15,
                    0,
                    ApplicationController.getContext().getString(R.string.program_14_description),
                    true,
                    true,
                    false,
                    null,
                    2,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_15_name),
                    3,
                    4,
                    134.0f,
                    2.1f,
                    0,
                    10,
                    ApplicationController.getContext().getString(R.string.program_15_description),
                    true,
                    true,
                    false,
                    null,
                    1,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_16_name),
                    3,
                    18,
                    134.0f,
                    2.1f,
                    0,
                    10,
                    ApplicationController.getContext().getString(R.string.program_16_description),
                    true,
                    true,
                    false,
                    null,
                    1,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_17_name),
                    3,
                    20,
                    121.0f,
                    1.1f,
                    0,
                    10,
                    ApplicationController.getContext().getString(R.string.program_17_description),
                    true,
                    true,
                    false,
                    null,
                    1,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_18_name),
                    3,
                    4,
                    134.0f,
                    2.1f,
                    0,
                    5,
                    ApplicationController.getContext().getString(R.string.program_18_description),
                    true,
                    true,
                    false,
                    null,
                    1,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_19_name),
                    3,
                    20,
                    121.0f,
                    1.1f,
                    0,
                    5,
                    ApplicationController.getContext().getString(R.string.program_19_description),
                    true,
                    true,
                    false,
                    null,
                    1,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_20_name),
                    1,
                    4,
                    134.0f,
                    2.1f,
                    0,
                    5,
                    ApplicationController.getContext().getString(R.string.program_20_description),
                    true,
                    true,
                    false,
                    null,
                    1,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_9_name),
                    1,
                    20,
                    121.0f,
                    1.1f,
                    0,
                    5,
                    ApplicationController.getContext().getString(R.string.program_9_description),
                    true,
                    true,
                    false,
                    null,
                    1,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_10_name),
                    1,
                    4,
                    134.0f,
                    2.1f,
                    0,
                    5,
                    ApplicationController.getContext().getString(R.string.program_10_description),
                    true,
                    true,
                    false,
                    null,
                    1,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_11_name),
                    3,
                    20,
                    121.0f,
                    1.1f,
                    0,
                    5,
                    ApplicationController.getContext().getString(R.string.program_11_description),
                    true,
                    true,
                    false,
                    null,
                    1,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

            profilesFromAutoclave.add(new Profile("",
                    1,
                    ApplicationController.getContext().getString(R.string.program_12_name),
                    3,
                    3,
                    134.0f,
                    2.1f,
                    0,
                    1,
                    ApplicationController.getContext().getString(R.string.program_12_description),
                    true,
                    true,
                    false,
                    null,
                    1,
                    false,
                    false,
                    false,
                    false,
                    10,
                    121f,
                    10));

        }
    }

    public static synchronized Autoclave getInstance() {
        return instance;

    }

    public boolean isOnlineMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(AppConstants.PREFERENCE_KEY_ONLINE_MODE, false);
    }

    public void setOnCalibrationListener(CalibrationListener listener) {
        this.calibraionListeners.add(listener);
    }

    public void removeOnCalibrationListener(CalibrationListener listener) {
        this.calibraionListeners.remove(listener);
    }

    public void setOnControllerInfoListener(ControllerInfoListener listener) {
        this.controllerInfoListeners.add(listener);
    }

    public void removeOnControllerInfoListener(ControllerInfoListener listener) {
        this.controllerInfoListeners.remove(listener);
    }

    public void setOnConnectionStatusListener(ConnectionStatusListener listener) {
        this.connectionStatusListeners.add(listener);
    }

    public void remvoeOnConnectionStatusListener(ConnectionStatusListener listener) {
        this.connectionStatusListeners.remove(listener);
    }

    public void setOnSensorDataListener(SensorDataListener listener) {
        this.sensorDataListeners.add(listener);
    }

    public void removeOnSensorDataListener(SensorDataListener listener) {
        this.sensorDataListeners.remove(listener);
    }

    public void setOnProtocolListener(ProtocolListener listener) {
        this.protocolListeners.add(listener);
    }

    public void removeOnProtocolListener(ProtocolListener listener) {
        this.protocolListeners.remove(listener);
    }

    public void setOnUserProgramListener(UserProgramListener listener) {
        this.userProgramListeners.add(listener);
    }

    public void removeOnUserProgramListener(UserProgramListener listener) {
        this.userProgramListeners.remove(listener);
    }

    public void setOnWifiListener(WifiListener listener) {
        this.wifiListeners.add(listener);
    }

    public void removeOnWifiListener(WifiListener listener) {
        this.wifiListeners.remove(listener);
    }

    public void setOnProfileListener(ProfileListener listener) {
        this.profileListeners.add(listener);
    }

    public void removeOnProfileListener(ProfileListener listener) {
        this.profileListeners.remove(listener);
    }


    public void setOnAutoclaveStateListener(AutoclaveStateListener listener) {
        this.autoclaveStateListeners.add(listener);
    }

    public void removeOnAutoclaveStateListener(AutoclaveStateListener listener) {
        this.autoclaveStateListeners.remove(listener);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;

        for (ProfileListener listener : profileListeners) {
            listener.onProfileChange(profile);
        }

    }


    public void notifyProfilesHasBeenSynced() {
        for (ProfileSyncedListener listener : profileSyncedListeners) {
            listener.onProfileSynced();
        }
    }

    public void setOnProfileSyncedListener(ProfileSyncedListener listener) {
        this.profileSyncedListeners.add(listener);
    }

    public void removeOnProfileSyncedListener(ProfileSyncedListener listener) {
        this.profileSyncedListeners.remove(listener);
    }

    public Date getmLastSelfTest() {
        return mLastSelfTest;
    }

    public void setmLastSelfTest(Date mLastSelfTest) {
        this.mLastSelfTest = mLastSelfTest;
    }


    public void setmStoredProfileSteps(int mStoredProfileSteps) {
        this.mStoredProfileSteps = mStoredProfileSteps;
    }


    public void setmOperationCycles(int mOperationCycles) {
        this.mOperationCycles = mOperationCycles;
    }


    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }


    /*
     *
     * Data, which is always up to date with 1second intervall
     *
     */
    public void setSensorsData(float[] temperatures, float[] pressures, boolean[] digitalData) {


        data.getTemp1().setCurrentValue(temperatures[0]);
        data.getTemp2().setCurrentValue(temperatures[1]);
        data.getTemp3().setCurrentValue(temperatures[3]);
        data.getTemp4().setCurrentValue(temperatures[2]);
        data.getPress().setCurrentValue(pressures[0]);
        data.getPress2().setCurrentValue(pressures[1]);


        data.setProgramFinishedSucessfully(digitalData[AppConstants.DIGITAL_PROGRAM_FINISHED_INDEX]);
        data.setProgramRunning(digitalData[AppConstants.DIGITAL_PROGRAM_RUNNING_INDEX]);
        data.setDoorClosed(digitalData[AppConstants.DIGITAL_DOOR_CLOSED_INDEX]);
        data.setDoorLocked(digitalData[AppConstants.DIGITAL_DOOR_LOCKED_INDEX]);
//        data.setFailStoppedByUser(digitalData[AppConstants.DIGITAL_FAIL_STOPPED_BY_USER]);
        data.setWaterLvlFull(digitalData[AppConstants.DIGITAL_WATER_LVL_FULL_INDEX]);
//        data.setWaterLvlLow(digitalData[AppConstants.DIGITAL_WATER_LVL_LOW_INDEX]);
        data.setWaterQuality(digitalData[AppConstants.DIGITAL_FAIL_WATER_QUALITY]);

        for (SensorDataListener listener : sensorDataListeners) {
            try {
                listener.onSensorDataChange(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        int year, month, day, hour, minute, second;
        try {
            String[] sDate = Autoclave.getInstance().getDate().split("\\.");
            String[] sTime = Autoclave.getInstance().getTime().split(":");
            year = Integer.parseInt(sDate[0]);
            month = Integer.parseInt(sDate[1]);
            day = Integer.parseInt(sDate[2]);
            hour = Integer.parseInt(sTime[0]);
            minute = Integer.parseInt(sTime[1]);
            second = Integer.parseInt(sTime[2]);
            dDate = new Date(year - 1900, month - 1, day, hour, minute, second);
        } catch (Exception e) {
            Log.e("Autoclave", "error generating date object from string" + e.toString());
            long millis = dDate.getTime();
            millis = millis + 1000;
            dDate.setTime(millis);
        }
        if (AppConstants.isIoSimulated == true) {
            dDate = new Date();
        }


    }


    public AutoclaveData getSensorsData() {
        return data;
    }


    public float getTemperature() {
        return data.getTemp1().getCurrentValue();
    }

    public float getPressure() {
        return data.getPress().getCurrentValue();
    }


    public int getmStoredProfileSteps() {
        return mStoredProfileSteps;
    }


    public int getmOperationCycles() {
        return mOperationCycles;
    }


    public boolean isMicrocontrollerReachable() {
        return microcontrollerReachable;
    }

    public void setMicrocontrollerReachable(boolean microcontrollerReachable) {

        this.microcontrollerReachable = microcontrollerReachable;

        for (ConnectionStatusListener listener : connectionStatusListeners) {
            listener.onConnectionStatusChange(this.microcontrollerReachable);
        }


    }

    public void setValveEnabled(boolean b) {
        valveEnabled = b;

    }


//	public void setDeviceId(String id) {

//		controller.setSavetyKey(id);
//	}

    public void setWifiConnected(boolean wifiConnected) {
        this.wifiConnected = wifiConnected;

        for (WifiListener listener : wifiListeners) {
            listener.onWifiConnectionChange(wifiConnected);
        }

    }

    /**
     * Checks if user (local admin) can change date/time or not.
     * Date/time change action allowed when this function returns
     * `true`, otherwise disallowed.
     *
     * @return `true` if preference value is less than & not equals to 3,
     *         `false` otherwise.
     */
    public boolean canChangeDateTime() {
        return PreferenceManager
                .getDefaultSharedPreferences(ApplicationController.getContext())
                .getInt(AppConstants.PREFERENCE_KEY_TIMES_DATE_TIME_UPDATED, 0) < 3;
    }

    public Controller getController() {

        return controller;
    }

    public DeviceModel getDevice() {
        DeviceModel deviceModel = new DeviceModel();
        deviceModel.setDeviceKey(controller.getSavetyKey());
        deviceModel.setSerial(controller.getSerialnumber());
        deviceModel.setModel(controller.getDeviceModel());
        return deviceModel;
    }


    public boolean isModelConnect() {
        return !isMulticontrolOrEssential();
    }


    public String getErrorCode() {
        return errorCode;
    }


    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }


    public Date getDateObject() {
        return dDate;

    }

    public String getDate() {
        return date;
    }


    public void setDate(String date) {
        this.date = date;
    }


    public String getTime() {
        return time;
    }


    public void setTime(String time) {
        this.time = time;
    }


    public int getIndexOfRunningProgram() {
        return indexOfRunningProgram;
    }


    public void setIndexOfRunningProgram(int indexOfRunningProgram) {
        this.indexOfRunningProgram = indexOfRunningProgram;
    }


    public void setAdjustParameters(double offsetSteamSensor, double offsetMediaSensor, double offsetHeaterSensor, double offsetSteamGeneratorSensor, double offsetPress) {
        getData().getTemp1().setOffset(offsetSteamSensor); //steam
        getData().getTemp2().setOffset(offsetMediaSensor); //media
        getData().getTemp3().setOffset(offsetHeaterSensor); //heating element
        getData().getTemp4().setOffset(offsetSteamGeneratorSensor); //steam generator sensor
        getData().getPress().setOffset(offsetPress);

        for (CalibrationListener listener : calibraionListeners) {
            listener.onCalibrationParameterReceived();
        }

    }


    public void increaseCurrentProgramCounter() {
        if (currentProgramCounter < 0) {
            currentProgramCounter = 0;
        }
        this.currentProgramCounter++;

    }

    public void setWarningList(String warningList) {
        this.warningList = warningList;
    }


    public String getWarningList() {
        return warningList;
    }

    public void setDebugData(String[] debugData) {
        try {
            if (debugData == null) return;
            if (this.debugData == null || (!debugData[0].equals(this.debugData[0]) || !debugData[1].equals(this.debugData[1]))) {
                this.debugData = new String[]{debugData[0], debugData[1]};
                for (ProtocolListener listener : protocolListeners)
                    listener.onDebugChanged();
                return;
            }
            this.debugData = new String[]{debugData[0], debugData[1]};
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] getDebugData() {
        return debugData;
    }
}

