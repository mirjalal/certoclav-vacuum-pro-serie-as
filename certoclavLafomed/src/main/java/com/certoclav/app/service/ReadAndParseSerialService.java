package com.certoclav.app.service;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.database.Profile;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.AutoclaveParameter;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.model.Log;
import com.certoclav.app.util.AuditLogger;
import com.certoclav.app.util.AutoclaveModelManager;
import com.certoclav.app.util.Helper;
import com.certoclav.app.util.MyCallback;
import com.certoclav.library.application.ApplicationController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android_serialport_api.MessageReceivedListener;
import android_serialport_api.SerialService;


public class ReadAndParseSerialService implements MessageReceivedListener {
    //ACK_DAT 2017.01.27,16:32:13,12,2,20.1,158.6,105,-3,001010000101,0,22,81
    //        date,time,indexOfProgram,cyclenumber,tempSteam,tempMedia,targetPress,pressure,digital,error,version,checksum
    //			0    1        2            3           4        5           6           7     8       9     10       11

    //ACK_DAT 2016.10.12,16:05:55,9,149,48.0,.0,204,-9,001000000100,0,22,91\r\n"
    //        0         ,1       ,2,3  ,4   ,5 ,6  ,7 ,8           ,9,10,11

    public static final int INDEX_DAT_DATE = 0;
    public static final int INDEX_DAT_TIME = 1;
    public static final int INDEX_DAT_PROGRAM_INDEX = 2;
    public static final int INDEX_DAT_CYCLE = 3;
    public static final int INDEX_DAT_TEMP_STEAM = 4;
    public static final int INDEX_DAT_TEMP_MEDIA = 5;
    public static final int INDEX_DAT_TEMP_OPTIONAL_1 = 6;
    public static final int INDEX_DAT_TEMP_OPTIONAL_2 = 7;
    public static final int INDEX_DAT_PRESSURE = 8;
    public static final int INDEX_DAT_PRESSURE_OPTIONAL = 9;
    public static final int INDEX_DAT_DIGITAL = 10;
    public static final int INDEX_DAT_PROGRAM_STEP = 11;
    public static final int INDEX_DAT_TIME_OR_PERCENT = 12;
    public static final int INDEX_DAT_ERRORCODE = 13;
    public static final int INDEX_DAT_DEBUG_INPUT = 14;
    public static final int INDEX_DAT_DEBUG_OUTPUT = 15;
    public static final int INDEX_DAT_WARNING = 16;
    public static final int INDEX_DAT_CHECKSUM = 17;
    public static final int NUMBER_OF_DAT_RESPONSE_PARAMETERS = 18;
    public static final int NUMBER_OF_PROGRAM_RESPONSE_PARAMETERS = 15;
    private static final int DELAY_PROGRAM_RUNNING = 600;
    private static final int DELAY_PROGRAM_NOT_RUNNING = 800;
    private static Context mContext;
    private int delayForGetData = DELAY_PROGRAM_RUNNING;
    //Errors
    public static final int ERROR_NOT_DEFINED = -1;
    public static final int ERROR_CHECKSUM_WRONG = 0;
    public static final int ERROR_PARSING = 0;
    public static final int ERROR_TIMEOUT = 1;
    //Program Parameters
    public static final int INDEX_PROGRAM_NUM = 0;
    public static final int INDEX_PROGRAM_NAME = 1;
    public static final int INDEX_PROGRAM_TEMP = 2;
    public static final int INDEX_PROGRAM_FINAL_TEMP = 3;
    public static final int INDEX_PROGRAM_IS_MAINTAIN = 4;
    public static final int INDEX_PROGRAM_IS_LIQUID_PROGRAM = 5;
    public static final int INDEX_PROGRAM_IS_CONT_BY_FLEX_PROBE_1 = 6;
    public static final int INDEX_PROGRAM_IS_CONT_BY_FLEX_PROBE_2 = 7;
    public static final int INDEX_PROGRAM_DRYING_TIME = 8;
    public static final int INDEX_PROGRAM_STERILIZATION_TIME = 9;
    public static final int INDEX_PROGRAM_PULSE_VACUUM = 10;
    public static final int INDEX_PROGRAM_IS_F0_ENABLED = 11;
    public static final int INDEX_PROGRAM_F0_VALUE = 12;
    public static final int INDEX_PROGRAM_Z_VALUE = 13;
    public static final int INDEX_PROGRAM_CHECKSUM = 14;
    Double offsetSteam = 0d;
    private int currentCommand = -1;

    Double offsetHeater = 0d;
    Double offsetSteamGenerator = 0d;
    Double offsetMedia = 0d;
    Double offsetPress = 0d;


    public static final int HANDLER_ERROR = -1;
    public static final int HANDLER_MSG_CALIBRATION = 1;
    public static final int HANDLER_MSG_DATA = 2;
    public static final int HANDLER_MSG_USER_PROGRAM = 3;
    public static final int HANDLER_MSG_ACK_PROGRAM = 4;
    public static final int HANDLER_MSG_ACK_PROGRAMS = 5;
    public static final int HANDLER_MSG_ACK_GET_PARAMETER = 6;
    public static final int HANDLER_MSG_ACK_SET_PARAMETER = 7;
    public static final int HANDLER_MSG_ACK_GET_PARAMETERS = 8;
    public static final int HANDLER_MSG_CMD_UTF = 9;
    private SerialService serialService = null;
    private List<MyCallback> callbacks;

    private Handler handlerGetData = new Handler();
    private Boolean runnableGetDataIsAlive = false;
    private List<SerialReadWriteListener> listeners;

    //the last called get data
    long lastGetDataCalled = 0;


    private Runnable runnableGetData = new Runnable() {
        @Override
        public void run() {
            try {
                runnableGetDataIsAlive = true;
                if (AppConstants.isIoSimulated) {
                    simulateMessage();
                } else {

                    if (commandQueue.size() == 0) {
//                        commandSent(COMMANDS.CREATE(COMMANDS.GET_DATA));
                        serialService.sendMessage(COMMANDS.CREATE(COMMANDS.GET_DATA));
                        commandSent(COMMANDS.CREATE(COMMANDS.GET_DATA));

                    }
                }
                handlerGetData.removeCallbacks(this);
                handlerGetData.postDelayed(this, delayForGetData);
                lastGetDataCalled = System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
                runnableGetDataIsAlive = false;
            }
        }
    };

    private Runnable runnableOtherCommands = new Runnable() {
        @Override
        public void run() {
            try {

                handlerGetData.removeCallbacks(runnableGetData);
                commandSent(commandQueue.get(0));
//                handlerGetData.postDelayed(runnableGetData, 2000);
                serialService.sendMessage(commandQueue.get(0));
                handler.postDelayed(runnableTimeout, 4000);
                Log.e("Serialservice", "CREATE: " + commandQueue.get(0));
                commandQueue.remove(0);

            } catch (Exception e) {
                e.printStackTrace();
                runnableGetDataIsAlive = false;
            }
        }
    };

    private Runnable runnableTimeout = new Runnable() {
        @Override
        public void run() {
            publishResult(ERROR_TIMEOUT, false, -1);
        }
    };


    static class COMMANDS {
        //Checksum will add automatically, shouldn't add to end of the commands
        final static String NEWLINE = "\n";
        final static String START = "CMD_STAR %s,";
        final static String CMD_STOP = "CMD_STOP %d,";
        final static String CMD_FLASH_USB = "CMD_STOP";
        final static String GET_DATA = "GET_DATA";
        final static String GET_PROGRAM = "GET_PROG %d,";
        final static String SET_PROGRAM = "SET_PROG %d,%s,%.1f,%.1f,%d,%d,%d,%d,%d,%d,%d,%d,%.1f,%.1f,";
        final static String GET_PROGRAMS = "GET_PROGS";
        final static String GET_PARAS = "GET_PARAS";
        final static String CMD_UTF = "CMD_UTF";
        final static String SET_PARAMETER = "SET_PARA %d,%s,";
        final static String GET_PARAMETER = "GET_PARA %d,";
        final static String CONFIRM_ERROR = "CMD_CNFE";

        public static String CREATE(String command, Object... args) {
            if (command.lastIndexOf(",") == command.length() - 1) {
                String commandFormatted = String.format(Locale.US, command, args);
                return commandFormatted + getChecksum(commandFormatted) + NEWLINE;
            }
            return command + NEWLINE;
        }

        private static int getChecksum(String command) {
            byte[] bytes = command.getBytes();
            int sum = 0;
            for (byte b : bytes) {
                sum += b;
            }
            return sum % 256;
        }
    }

    interface RESPONSES {
        String START = "ACK_STAR";
        String STOP = "ACK_STOP";
        String ACK_DATA = "ACK_DATA";
        String ACK_PROG = "ACK_PROG";
        String ACK_PARA = "ACK_PARA";
        String ACK_PARAS = "ACK_PARAS";
        String ACK_STAR = "ACK_STAR";
        String ACK_STOP = "ACK_STOP";
        String ACK_PROGS = "ACK_PROGS";
        String ACK_UTF = "ACK_UTF";
        String CONFIRM_ERROR = "ACK_CNFE";
    }


    private void sendCommand(String command) {

        commandQueue.add(command);
        if (AutoclaveModelManager.getInstance().getModel() == null) {
            commandQueue.clear();
            commandQueue.add(COMMANDS.CREATE(COMMANDS.GET_PARAMETER, 1));
        } else if (AutoclaveModelManager.getInstance().getSerialNumber() == null) {
            commandQueue.clear();
            commandQueue.add(COMMANDS.CREATE(COMMANDS.GET_PARAMETER, 3));
        } else if (AutoclaveModelManager.getInstance().getPCBSerialNumber() == null) {
            commandQueue.clear();
            commandQueue.add(COMMANDS.CREATE(COMMANDS.GET_PARAMETER, 4));
        } else if (AutoclaveModelManager.getInstance().getStMaxVersion() == null) {
            commandQueue.clear();
            commandQueue.add(COMMANDS.CREATE(COMMANDS.GET_PARAMETER, 10));
        }

        handler.removeCallbacks(runnableOtherCommands);
        handlerGetData.removeCallbacks(runnableGetData);
        handler.removeCallbacks(runnableTimeout);
        if (commandQueue.size() > 0)
            handler.postDelayed(runnableOtherCommands, 500);

    }


    public void checkGetDataRunnableIsAlive() {

        Log.e("ReadAndParse", "runnableIsAlive: " + runnableGetDataIsAlive);
        if (System.currentTimeMillis() - lastGetDataCalled > 10000) {
            if (commandQueue.size() > 0) {
                sendCommand(commandQueue.remove(0));
            } else {
                handlerGetData.removeCallbacks(runnableGetData);
                handlerGetData.post(runnableGetData);
            }
        }

    }


    private static ReadAndParseSerialService instance = new ReadAndParseSerialService();

    public static ReadAndParseSerialService getInstance() {
        mContext = ApplicationController.getContext();
        return instance;
    }

    public void sendStartCommand(int programIndex) {
        sendCommand(COMMANDS.CREATE(COMMANDS.START, programIndex));
    }

    public void confirmError() {
        if (AppConstants.isIoSimulated)
            AutoclaveMonitor.SimulatedFailStoppedByUser = false;
        sendCommand(COMMANDS.CREATE(COMMANDS.CONFIRM_ERROR));
    }

    public void sendStopCommand(boolean isForce) {
        sendCommand(COMMANDS.CREATE(COMMANDS.CMD_STOP, isForce ? 1 : 0));
    }

    public void sendGetUserProgramCommand() {
        commandQueue.add("GET_USER\r\n");
        if (AppConstants.isIoSimulated) {
            //     Autoclave.getInstance().setUserDefinedProgram(new Profile("", 1, "user defined prog", 2, 10, 105, 204, 0, 5, "user def", true, true, null, 7));
        }
    }

    public void getProgram(int index) {
        sendCommand(COMMANDS.CREATE(COMMANDS.GET_PROGRAM, index));
    }

    public void getPrograms() {
        sendCommand(COMMANDS.CREATE(COMMANDS.GET_PROGRAMS));
    }

    public void requestForFirmwareUpdate() {
        sendCommand(COMMANDS.CREATE(COMMANDS.CMD_UTF));
    }

    public void getParameters() {
        sendCommand(COMMANDS.CREATE(COMMANDS.GET_PARAS));
    }

    public void getParameter(int paramId) {
        sendCommand(COMMANDS.CREATE(COMMANDS.GET_PARAMETER, paramId));
    }

    public void setParameter(int paramId, Object value) {
        String valueStr = value.toString();
        if (value instanceof Boolean)
            valueStr = (Boolean) value ? "1" : "0";
        if (value instanceof Date) {
            SimpleDateFormat format = new SimpleDateFormat("YYMMDDhhmmss");
            valueStr = format.format(value);
        }
        sendCommand(COMMANDS.CREATE(COMMANDS.SET_PARAMETER, paramId, valueStr));
    }


    public void setProgram(Profile profile) {
        sendCommand(COMMANDS.CREATE(COMMANDS.SET_PROGRAM,
                profile.getIndex(),
                profile.getName().replaceAll(" ", "_"),
                profile.getSterilisationTemperature(true),
                profile.getFinalTemp(true),
                profile.isMaintainEnabled() ? 1 : 0,
                profile.isLiquidProgram() ? 1 : 0,
                profile.isContByFlexProbe1Enabled() ? 1 : 0,
                profile.isContByFlexProbe2Enabled() ? 1 : 0,
                profile.getDryTime(),
                profile.getSterilisationTime(),
                profile.getVacuumTimes(),
                profile.isF0Enabled() ? 1 : 0,
                profile.getF0Value(),
                profile.getzValue(true)));
    }

    /*
     * parameter vacuumTimes: [01-10]
     * parameter sterilizationTemp: [105-134]
     * parameter sterilizationTime: [01-25]
     * parameter dryingTime: [01-25]
     */
    public void sendPutUserProgramCommand(Integer vacuumTimes, Integer sterilizationTemperature, Integer sterilizationTime, Integer dryingTime) {
        commandQueue.add(String.format("CMD_USER %02d,%03d,%02d,%02d\r\n", vacuumTimes, sterilizationTemperature, sterilizationTime, dryingTime));
    }

    public void sendGetAdjustParameterCommand() {
        commandQueue.add("GET_ADJU\r\n");
        if (AppConstants.isIoSimulated == true) {
            Autoclave.getInstance().setAdjustParameters(-1.0, 0.1, 0.2, 0.3, -10); //steam, heater, steamGenerator, mediasensor
        }
    }

    public void sendPutAdjustParameterCommand(Double offsetTemp1, Double offsetTemp2, Double offsetTemp3, Integer offsetPress, Double offsetMedia) {
        commandQueue.add(String.format(Locale.ENGLISH, "CMD_ADJU %.1f,%.1f,%.1f,%02d,%.1f\r\n", offsetTemp1, offsetTemp2, offsetTemp3, offsetPress, offsetMedia));
        //if locale is not Locale.ENGLISH there can be comma instad of point!
    }

    private ArrayList<String> commandQueue = new ArrayList<String>();

    //data parsed from GET_DATA
    private int cycleNumber = 0;
    private float timeOrPercent = 0f;
    boolean[] digitalData = new boolean[AppConstants.NUMBER_OF_DIGITAL_BITS];
    private String errorCode = "00000000";
    private String date = "";
    private String time = "";
    private int indexOfRunningProgram = 1;
    private String firmwareVersion = "";
    float[] pressures = new float[2];
    String[] debugData = new String[2];
    String warningList;
    float[] temperatures = new float[4];
    private String programStep = "";

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Autoclave.getInstance().setMicrocontrollerReachable(true);

            switch (msg.what) {
                case HANDLER_MSG_USER_PROGRAM:
                    //no user defined program
                    break;
                case HANDLER_MSG_DATA:
                    Autoclave.getInstance().setSensorsData(
                            temperatures,
                            pressures,
                            digitalData);

                    Autoclave.getInstance().setDoorLocked(digitalData[AppConstants.DIGITAL_DOOR_LOCKED_INDEX]);
                    Autoclave.getInstance().setProgramStep(programStep);
                    Autoclave.getInstance().setErrorCode(errorCode);
                    Autoclave.getInstance().setDate(date);
                    Autoclave.getInstance().setTime(time);
                    Autoclave.getInstance().setTimeOrPercent(timeOrPercent);
                    Autoclave.getInstance().setIndexOfRunningProgram(indexOfRunningProgram);
                    Autoclave.getInstance().setMicrocontrollerReachable(true);
                    Autoclave.getInstance().getController().setCycleNumber(cycleNumber);
                    Autoclave.getInstance().getController().setFirmwareVersion(firmwareVersion);
                    Autoclave.getInstance().setDebugData(debugData);
                    Autoclave.getInstance().setWarningList(warningList);

                    break;
                case HANDLER_MSG_CALIBRATION:
                    Autoclave.getInstance().setAdjustParameters(offsetSteam, offsetMedia, offsetHeater, offsetSteamGenerator, offsetPress);
                    break;
                case HANDLER_MSG_ACK_PROGRAM:
                    if (msg.obj != null)
                        publishResult(msg.obj, true, HANDLER_MSG_ACK_PROGRAM);
                    else
                        publishResult(ERROR_NOT_DEFINED, false, HANDLER_MSG_ACK_PROGRAM);
                    break;
                case HANDLER_MSG_CMD_UTF:
                    if (msg.obj != null)
                        publishResult(msg.obj, true, HANDLER_MSG_CMD_UTF);
                    else
                        publishResult(ERROR_NOT_DEFINED, false, HANDLER_MSG_CMD_UTF);
                    break;
                case HANDLER_MSG_ACK_PROGRAMS:
                    if (msg.obj != null)
                        publishResult(msg.obj, true, HANDLER_MSG_ACK_PROGRAMS);
                    else
                        publishResult(ERROR_NOT_DEFINED, false, HANDLER_MSG_ACK_PROGRAMS);
                    break;
                case HANDLER_MSG_ACK_GET_PARAMETERS:
                    if (msg.obj != null)
                        publishResult(msg.obj, true, HANDLER_MSG_ACK_GET_PARAMETERS);
                    else
                        publishResult(ERROR_NOT_DEFINED, false, HANDLER_MSG_ACK_GET_PARAMETERS);
                    break;
                case HANDLER_MSG_ACK_SET_PARAMETER:
                    if (msg.obj != null)
                        publishResult(msg.obj, true, HANDLER_MSG_ACK_SET_PARAMETER);
                    else
                        publishResult(ERROR_NOT_DEFINED, false, HANDLER_MSG_ACK_PROGRAMS);
                    break;
                case HANDLER_MSG_ACK_GET_PARAMETER:
                    if (msg.obj != null)
                        publishResult(msg.obj, true, HANDLER_MSG_ACK_GET_PARAMETER);
                    else
                        publishResult(ERROR_NOT_DEFINED, false, HANDLER_MSG_ACK_PROGRAMS);
                    break;
                case HANDLER_ERROR:
                    publishResult(msg.obj != null ? msg.obj : ERROR_NOT_DEFINED, false, -1);
                    break;
                default:

                    // super.handleMessage(msg);
            }
        }
    };


    private int counter = 0;

    private ReadAndParseSerialService() {

        String serialPathAutoclave = "";
        int serialBaudAutoclave = 9600;
        if (AppConstants.TABLET_TYPE.equals(AppConstants.TABLET_TYPE_FAYTECH)) {
            serialPathAutoclave = "/dev/ttyUSB0";
        } else if (AppConstants.TABLET_TYPE.equals(AppConstants.TABLET_TYPE_FAYTECH_RS_232)) {
            serialPathAutoclave = "/dev/ttyS4";
        } else {
            serialPathAutoclave = "/dev/ttymxc3";
        }
        if (AppConstants.MODEL_CURRENT.equals(AppConstants.MODEL_RAYPA_TLV)) {
            serialBaudAutoclave = 38400;
        } else {
            serialBaudAutoclave = 9600;
        }

        listeners = new ArrayList<>();
        serialService = new SerialService(serialPathAutoclave, serialBaudAutoclave);
        // serialService = Autoclave.getInstance().getSerialsService();
        handlerGetData.post(runnableGetData);

//        serialThread.start();
        if (AppConstants.isIoSimulated == false) {
            serialService.setOnMessageReceivedListener(this);
            serialService.startReadSerialThread();
        }
    }


    @Override
    public void onMessageReceived(String message) {
        responseRead(message);

        handler.removeCallbacks(runnableOtherCommands);
        if (commandQueue.size() > 0)
            handler.postDelayed(runnableOtherCommands, 500);
        //Wait 1 seconds before sending GET_DATA command after sending other commands
        handlerGetData.removeCallbacks(runnableGetData);
        handlerGetData.postDelayed(runnableGetData, delayForGetData);
        handler.removeCallbacks(runnableTimeout);

        try {
            message = new String(message.getBytes(), "UTF-8");
        } catch (Exception e) {
            Log.e("ReadAndParse", "error transform to utf-8");
        }
        Log.e("ReadAndParse", "received: " + message);
        if (AppConstants.CHECK_CHECKSUM && !checkChecksum(message)) {
            com.certoclav.app.model.Log.e("Checksum failed!\n" + message);
            Message mesg = new Message();
            mesg.what = HANDLER_ERROR;
            mesg.obj = ERROR_CHECKSUM_WRONG;
            handler.sendMessage(mesg);
            return;
        }
        String[] response = null;
        String[] responseParameters = null;
        try {
            response = message.split(" ");
            if (response.length == 2) {
                responseParameters = response[1].split(",");
            }
        } catch (Exception e) {
            Log.e("ReadAndParseSerial", "exception parsing response");
            return;
        }
        try {
            switch (response[0]) {
                case "ACK_ADJU":
                    //ACK_ADJU -1.0,0.0,0.0,00,-0.1
                    //           0   1   2   3   4

                    if (responseParameters == null) {
                        Log.e("ReadAndParseSerial", "parameters == null");
                        return;
                    }
                    if (responseParameters.length != 5) {
                        Log.e("ReadAndParseSerial", "parameter length wrong " + responseParameters.length);
                        return;
                    }
                    offsetSteam = Double.parseDouble(responseParameters[0]);
                    offsetHeater = Double.parseDouble(responseParameters[1]);
                    offsetSteamGenerator = Double.parseDouble(responseParameters[2]);
                    offsetPress = Double.parseDouble(responseParameters[3]);
                    offsetMedia = Double.parseDouble(responseParameters[4]);
                    Log.e("ReadAndParseSerial", "parameters: " + offsetSteam + " " + offsetHeater + " " + offsetSteamGenerator + " " + offsetPress + " " + offsetMedia);
                    handler.sendEmptyMessage(HANDLER_MSG_CALIBRATION);
                    break;
                case RESPONSES.ACK_STAR:
                    delayForGetData = DELAY_PROGRAM_RUNNING;
                    handlerGetData.removeCallbacks(runnableGetData);
                    handlerGetData.postDelayed(runnableGetData, delayForGetData);
                    if (responseParameters[0] != null && responseParameters[0].equals("1"))
                        AuditLogger.getInstance().addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_EMPTY,
                                AuditLogger.ACTION_PROGRAM_STARTED,
                                AuditLogger.OBJECT_EMPTY,
                                Autoclave.getInstance().getProfile().getName() +
                                        " (" + mContext.getString(R.string.cycle) + " " + (Autoclave.getInstance().getController().getCycleNumber() + 1) + ")", false);
                    break;
                case RESPONSES.ACK_STOP:
                    delayForGetData = DELAY_PROGRAM_RUNNING;
                    handlerGetData.removeCallbacks(runnableGetData);
                    if (responseParameters[0] != null && responseParameters[0].equals("1")) {

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                AuditLogger.getInstance().addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_EMPTY,
                                        AuditLogger.ACTION_PROGRAM_CANCELED,
                                        AuditLogger.OBJECT_EMPTY,
                                        Autoclave.getInstance().getProfile().getName() +
                                                " (" + mContext.getString(R.string.cycle) + " " + (Autoclave.getInstance().getController().getCycleNumber()) + ")", true);
                            }
                        });
                    }
                    handlerGetData.postDelayed(runnableGetData, delayForGetData);
                    Intent intent5 = new Intent(ApplicationController.getContext(), PostProtocolsService.class);
                    ApplicationController.getContext().startService(intent5);
                    break;
                case RESPONSES.ACK_DATA:

                    if (responseParameters.length == NUMBER_OF_DAT_RESPONSE_PARAMETERS) {
                        date = responseParameters[INDEX_DAT_DATE];
                        time = responseParameters[INDEX_DAT_TIME];
                        indexOfRunningProgram = Integer.parseInt(responseParameters[INDEX_DAT_PROGRAM_INDEX]);
                        cycleNumber = Integer.parseInt(responseParameters[INDEX_DAT_CYCLE]);
                        timeOrPercent = Float.parseFloat(responseParameters[INDEX_DAT_TIME_OR_PERCENT]);


                        temperatures[0] = Float.parseFloat(responseParameters[INDEX_DAT_TEMP_STEAM]);
                        temperatures[1] = Float.parseFloat(responseParameters[INDEX_DAT_TEMP_MEDIA]);
                        temperatures[2] = Float.parseFloat(responseParameters[INDEX_DAT_TEMP_OPTIONAL_1]);
                        temperatures[3] = Float.parseFloat(responseParameters[INDEX_DAT_TEMP_OPTIONAL_2]);

                        if (AutoclaveModelManager.getInstance().isFahrenheit()) {
                            for (int i = 0; i < 4; i++)
                                temperatures[i] = Helper.getInstance().celsiusToCurrentUnit(temperatures[i]);
                        }
                        pressures[0] = Float.parseFloat(responseParameters[INDEX_DAT_PRESSURE]);
                        pressures[1] = Float.parseFloat(responseParameters[INDEX_DAT_PRESSURE_OPTIONAL]);
                        debugData[0] = responseParameters[INDEX_DAT_DEBUG_INPUT];
                        debugData[1] = responseParameters[INDEX_DAT_DEBUG_OUTPUT];
                        warningList = responseParameters[INDEX_DAT_WARNING];

                        Log.e("warning", warningList);

                        String digitalFlags = responseParameters[INDEX_DAT_DIGITAL];
                        boolean isProgramFinished = digitalFlags.charAt(AppConstants.DIGITAL_PROGRAM_FINISHED_INDEX) == '1';
                        boolean isProgramRunning = digitalFlags.charAt(AppConstants.DIGITAL_PROGRAM_RUNNING_INDEX) == '1';
                        boolean isDoorLocked = digitalFlags.charAt(AppConstants.DIGITAL_DOOR_LOCKED_INDEX) == '1';
                        boolean isDoorClosed = digitalFlags.charAt(AppConstants.DIGITAL_DOOR_CLOSED_INDEX) == '1';
                        boolean isWaterLevelSourceLow = digitalFlags.charAt(AppConstants.DIGITAL_WATER_LVL_LOW_INDEX) == '1';
                        boolean isWaterLevelBinFull = digitalFlags.charAt(AppConstants.DIGITAL_WATER_LVL_FULL_INDEX) == '1';
                        boolean isWaterQualityBad = digitalFlags.charAt(AppConstants.DIGITAL_FAIL_WATER_QUALITY) == '1';


                        try {
                            errorCode = responseParameters[INDEX_DAT_ERRORCODE];
                        } catch (Exception e) {
                            errorCode = "00000000";
                        }

                        delayForGetData = isProgramRunning || (errorCode != null &&
                                !errorCode.equals("00000000")) ? DELAY_PROGRAM_RUNNING : DELAY_PROGRAM_NOT_RUNNING;

                        String checksum = responseParameters[INDEX_DAT_CHECKSUM];


                        digitalData[AppConstants.DIGITAL_DOOR_CLOSED_INDEX] = isDoorClosed;
                        digitalData[AppConstants.DIGITAL_DOOR_LOCKED_INDEX] = isDoorLocked;
                        digitalData[AppConstants.DIGITAL_PROGRAM_FINISHED_INDEX] = isProgramFinished;
                        digitalData[AppConstants.DIGITAL_PROGRAM_RUNNING_INDEX] = isProgramRunning;
                        digitalData[AppConstants.DIGITAL_WATER_LVL_LOW_INDEX] = isWaterLevelSourceLow;
                        digitalData[AppConstants.DIGITAL_WATER_LVL_FULL_INDEX] = isWaterLevelBinFull;
                        digitalData[AppConstants.DIGITAL_WATER_LVL_FULL_INDEX] = isWaterLevelBinFull;
                        digitalData[AppConstants.DIGITAL_FAIL_WATER_QUALITY] = isWaterQualityBad;

                        programStep = responseParameters[INDEX_DAT_PROGRAM_STEP];
                        // digitalData[AppConstants.DIGITAL_FAIL_WATER_QUALITY]
                        handler.sendEmptyMessage(HANDLER_MSG_DATA);

                    }

                    break;

                case RESPONSES.ACK_PROG:


                    handlerGetData.removeCallbacks(runnableGetData);
                    handlerGetData.postDelayed(runnableGetData, delayForGetData);


                    if (responseParameters.length == 2 && Integer.valueOf(responseParameters[0]) == 1) {
                        Message msg = new Message();
                        msg.what = HANDLER_MSG_ACK_PROGRAM;
                        msg.obj = 1;
                        handler.sendMessage(msg);
                    } else {
                        publishResult(new ErrorModel(null, ERROR_PARSING), false, HANDLER_MSG_ACK_PROGRAM);
                    }
                    break;

                case RESPONSES.ACK_UTF:


                    handlerGetData.removeCallbacks(runnableGetData);
                    handlerGetData.postDelayed(runnableGetData, delayForGetData);


                    if (responseParameters.length == 2) {
                        Message msg = new Message();
                        msg.what = HANDLER_MSG_CMD_UTF;
                        msg.obj = Integer.valueOf(responseParameters[0]);
                        handler.sendMessage(msg);
                    } else {
                        publishResult(new ErrorModel(null, ERROR_PARSING), false, HANDLER_MSG_CMD_UTF);
                    }
                    break;
                case RESPONSES.ACK_PROGS:

                    handlerGetData.removeCallbacks(runnableGetData);
                    handlerGetData.postDelayed(runnableGetData, delayForGetData);
                    String[] parametersParameters = response[1].split(";");
                    List<Profile> programs = new ArrayList<>();
                    for (String program : parametersParameters) {
                        program = program.replace(";", "");
                        responseParameters = program.split(",");
                        Float pressure = 0f;
                        if (Float.valueOf(responseParameters[INDEX_PROGRAM_TEMP]) >= 100) {
                            pressure = (float) (0.006112 * Math.exp((17.62 * Float.valueOf(responseParameters[INDEX_PROGRAM_TEMP])) / (243.12 + Float.valueOf(responseParameters[INDEX_PROGRAM_TEMP]))) - 1);
                            pressure = ((int) (pressure * 100)) / 100.0f;
                        }


                        if (responseParameters.length == NUMBER_OF_PROGRAM_RESPONSE_PARAMETERS) {

                            //Program is not defined skip it
                            if (responseParameters[INDEX_PROGRAM_NAME] == null || responseParameters[INDEX_PROGRAM_NAME].equals("VOID"))
                                continue;

                            Profile profile = new Profile("",
                                    1,
                                    responseParameters[INDEX_PROGRAM_NAME].replaceAll("[^ -~]", "").replace("_", " "),
                                    Integer.valueOf(responseParameters[INDEX_PROGRAM_PULSE_VACUUM]),
                                    Integer.valueOf(responseParameters[INDEX_PROGRAM_STERILIZATION_TIME]),
                                    Float.valueOf(responseParameters[INDEX_PROGRAM_TEMP]),
                                    pressure,
                                    0,
                                    Integer.valueOf(responseParameters[INDEX_PROGRAM_DRYING_TIME]),
                                    null,
                                    true,
                                    true,
                                    responseParameters[INDEX_PROGRAM_IS_LIQUID_PROGRAM].equals("1"),
                                    null,
                                    Integer.valueOf(responseParameters[INDEX_PROGRAM_NUM]),
                                    responseParameters[INDEX_PROGRAM_IS_F0_ENABLED].equals("1"),
                                    responseParameters[INDEX_PROGRAM_IS_MAINTAIN].equals("1"),
                                    responseParameters[INDEX_PROGRAM_IS_CONT_BY_FLEX_PROBE_1].equals("1"),
                                    responseParameters[INDEX_PROGRAM_IS_CONT_BY_FLEX_PROBE_2].equals("1"),
                                    Float.valueOf(responseParameters[INDEX_PROGRAM_FINAL_TEMP]),
                                    Float.valueOf(responseParameters[INDEX_PROGRAM_F0_VALUE]),
                                    Float.valueOf(responseParameters[INDEX_PROGRAM_Z_VALUE])
                            );
                            programs.add(profile);

                        } else {
                            publishResult(new ErrorModel(null, ERROR_PARSING), false, HANDLER_MSG_ACK_PROGRAMS);
                            return;
                        }
                    }
                    Message msg = new Message();
                    msg.what = HANDLER_MSG_ACK_PROGRAMS;
                    msg.obj = programs;
                    handler.sendMessage(msg);
                    break;
                case RESPONSES.ACK_PARA:

                    handlerGetData.removeCallbacks(runnableGetData);
                    handlerGetData.postDelayed(runnableGetData, delayForGetData);

                    if (responseParameters.length == 2) {
                        msg = new Message();
                        msg.what = HANDLER_MSG_ACK_SET_PARAMETER;
                        msg.obj = Integer.valueOf(responseParameters[0]);
                        handler.sendMessage(msg);
                    } else if (responseParameters.length == 3) {
                        msg = new Message();
                        msg.what = HANDLER_MSG_ACK_GET_PARAMETER;
                        msg.obj = new AutoclaveParameter(Integer.valueOf(responseParameters[0]), responseParameters[1]);
                        handler.sendMessage(msg);
                    } else {
                        publishResult(new ErrorModel(null, ERROR_PARSING), false, HANDLER_MSG_ACK_PROGRAM);
                    }
                    break;

                case RESPONSES.ACK_PARAS:

                    handlerGetData.removeCallbacks(runnableGetData);
                    handlerGetData.postDelayed(runnableGetData, delayForGetData);
                    parametersParameters = response[1].split(";");
                    List<AutoclaveParameter> parameters = new ArrayList<>();
                    for (String parameter : parametersParameters) {
                        parameter = parameter.replace(";", "");
                        responseParameters = parameter.split(",");

                        if (responseParameters.length == 3) {

                            AutoclaveParameter autoclaveParameter = new AutoclaveParameter(Integer.valueOf(responseParameters[0]), responseParameters[1]);
                            parameters.add(autoclaveParameter);

                        } else {
                            publishResult(new ErrorModel(null, ERROR_PARSING), false, HANDLER_MSG_ACK_PROGRAMS);
                            return;
                        }
                    }
                    msg = new Message();
                    msg.what = HANDLER_MSG_ACK_GET_PARAMETERS;
                    msg.obj = parameters;
                    handler.sendMessage(msg);
                    break;

            }
        } catch (Exception e) {
            Log.e("onMessageReceived", e.toString());
            e.printStackTrace();
        }


    }

    private void simulateMessage() {


        //	Log.e("ReadAndParseSerialSer", "simulate serial");

        cycleNumber = 1;

        Float temperature;


        pressures[0] = (float) 1.0;
        boolean isProgramFinished = true;
        boolean isProgramRunning = false;
        boolean isDoorLocked = false;
        boolean isDoorClosed = true;
        boolean isWaterLevelSourceLow = false;
        boolean isWaterLevelBinFull = false;
        boolean isWaterQualityBad = false;
        boolean isStopedByUser = false;
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy.MM.dd");
        SimpleDateFormat simpleTime = new SimpleDateFormat("hh:mm:ss");
        date = simpleDate.format(new Date());
        time = simpleTime.format(new Date());

        temperature = (float) (60 + (30.0 * Math.sin(((double) counter) * 0.02)));
        counter++;
        isDoorClosed = true;
        isProgramFinished = false;
        if (Autoclave.getInstance().getState() == AutoclaveState.PREPARE_TO_RUN
                || Autoclave.getInstance().getState() == AutoclaveState.RUNNING
                || Autoclave.getInstance().getState() == AutoclaveState.RUN_CANCELED) {
            isProgramRunning = true;
            isDoorLocked = true;
        } else {
            isProgramRunning = false;
            isDoorLocked = false;
        }
        if (AutoclaveMonitor.getInstance().SimulatedFailStoppedByUser == true) {
            isProgramRunning = false;
            isDoorLocked = false;
            errorCode = "40000000";
        } else {
            errorCode = "00000000";
        }
        isWaterLevelSourceLow = false;
        isWaterLevelBinFull = false;

        temperatures[0] = temperature;
        temperatures[1] = temperature - 3;
        temperatures[2] = temperature - 4;
        firmwareVersion = "SIM V1";
        programStep = "SF11";

        digitalData[AppConstants.DIGITAL_DOOR_CLOSED_INDEX] = isDoorClosed;
        digitalData[AppConstants.DIGITAL_DOOR_LOCKED_INDEX] = isDoorLocked;
        digitalData[AppConstants.DIGITAL_PROGRAM_FINISHED_INDEX] = isProgramFinished;
        digitalData[AppConstants.DIGITAL_PROGRAM_RUNNING_INDEX] = isProgramRunning;
        digitalData[AppConstants.DIGITAL_WATER_LVL_LOW_INDEX] = isWaterLevelSourceLow;
        digitalData[AppConstants.DIGITAL_WATER_LVL_FULL_INDEX] = isWaterLevelBinFull;
        digitalData[AppConstants.DIGITAL_FAIL_WATER_QUALITY] = isWaterQualityBad;
        //digitalData[AppConstants.DIGITAL_FAIL_STOPPED_BY_USER] = isStopedByUser;
        indexOfRunningProgram = 1;
        try {
            indexOfRunningProgram = Autoclave.getInstance().getProfile().getIndex();
        } catch (Exception e) {
            indexOfRunningProgram = 1;
        }
        Autoclave.getInstance().setIndexOfRunningProgram(indexOfRunningProgram);


        handler.sendEmptyMessage(HANDLER_MSG_DATA);


    }

    private boolean checkChecksum(String command) {
        command = command.trim();
        if (AppConstants.isIoSimulated)
            return true;
        int lastIndex = command.lastIndexOf(",");
        if (lastIndex >= 0)
            try {
                return COMMANDS.getChecksum(command.substring(0, lastIndex + 1)) == Integer.valueOf(command.substring(lastIndex + 1, command.length()));
            } catch (Exception e) {
                e.printStackTrace();
                //Parsing the checksum failed
            }
        return false;
    }

    public void addCallback(MyCallback callback) {
        if (callbacks == null)
            callbacks = new ArrayList<>();
        callbacks.add(callback);
    }

    public void removeCallback(MyCallback callback) {
        if (callbacks != null) {
            callbacks.remove(callback);
        }
    }

    private void publishResult(Object response, boolean isSuccess, int requestId) {
        for (MyCallback callback : callbacks) {
            if (isSuccess)
                callback.onSuccess(response, requestId);
            else
                callback.onError(new ErrorModel(null, Integer.valueOf(response.toString())), requestId);
        }
    }

    public interface SerialReadWriteListener {
        void onRead(String message);

        void onWrote(String message);
    }

    public void addSerialReadWriteListener(SerialReadWriteListener listener) {
        listeners.clear();
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeSerialReadWriteListener(SerialReadWriteListener listener) {
        if (listeners.contains(listener))
            listeners.remove(listener);
    }

    private void commandSent(String message) {
        Log.e("COMMANDS SENT", message);
        for (SerialReadWriteListener listener : listeners)
            listener.onWrote(message);
    }

    private void responseRead(String message) {
        for (SerialReadWriteListener listener : listeners)
            listener.onRead(message);
    }
}
