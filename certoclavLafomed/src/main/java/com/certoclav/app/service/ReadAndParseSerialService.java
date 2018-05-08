package com.certoclav.app.service;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.certoclav.app.AppConstants;
import com.certoclav.app.database.Profile;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.util.MyCallback;

import java.util.ArrayList;
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
    public static final int INDEX_DAT_ERRORCODE = 12;
    public static final int INDEX_DAT_CHECKSUM = 13;
    public static final int NUMBER_OF_DAT_RESPONSE_PARAMETERS = 14;
    public static final int NUMBER_OF_PROGRAM_RESPONSE_PARAMETERS = 8;
    //Errors
    public static final int ERROR_NOT_DEFINED = -1;
    public static final int ERROR_CHECKSUM_WRONG = 0;
    public static final int ERROR_PARSING = 0;
    public static final int ERROR_TIMEOUT = 1;
    //Program Parameters
    public static final int INDEX_PROGRAM_NUM = 0;
    public static final int INDEX_PROGRAM_NAME = 1;
    public static final int INDEX_PROGRAM_TEMP = 2;
    public static final int INDEX_PROGRAM_IS_LIQUID_PROGRAM = 3;
    public static final int INDEX_PROGRAM_DRYING_TIME = 4;
    public static final int INDEX_PROGRAM_STERILIZATION_TIME = 5;
    public static final int INDEX_PROGRAM_PULSE_VACUUM = 6;
    public static final int INDEX_PROGRAM_CHECKSUM = 7;
    Double offsetSteam = 0d;

    Double offsetHeater = 0d;
    Double offsetSteamGenerator = 0d;
    Double offsetMedia = 0d;
    Double offsetPress = 0d;



    private static final int HANDLER_ERROR = -1;
    private static final int HANDLER_MSG_CALIBRATION = 1;
    private static final int HANDLER_MSG_DATA = 2;
    private static final int HANDLER_MSG_USER_PROGRAM = 3;
    private static final int HANDLER_MSG_ACK_PROGRAM = 4;
    private SerialService serialService = null;
    private List<MyCallback> callbacks;

    private Handler handlerGetData = new Handler();
    private Boolean runnableGetDataIsAlive = false;



    private Runnable runnableGetData = new Runnable() {
        @Override
        public void run() {
            try {
                runnableGetDataIsAlive = true;
                if (AppConstants.isIoSimulated)
                    simulateMessage();

                if (commandQueue.size() == 0)
                    serialService.sendMessage(COMMANDS.CREATE(COMMANDS.GET_DATA));
                else
                    commandQueue.clear();

                handlerGetData.postDelayed(this, 1000);
            }catch (Exception e){
                e.printStackTrace();
                runnableGetDataIsAlive = false;
            }
        }
    };



    static class COMMANDS {
        //Checksum will add automatically, shouldn't add to end of the commands
        final static String NEWLINE = "\n";
        final static String START = "CMD_STAR %s,";
        final static String CMD_STOP = "CMD_STOP";
        final static String GET_DATA = "GET_DATA";
        final static String GET_PROGRAMS = "GET_PROG %d,";
        final static String CONFIRM_ERROR = "CMD_CNFE";

        public static String CREATE(String command, Object... args) {
            if (command.lastIndexOf(",") == command.length() - 1) {
                String commandFormatted = String.format(command, args);
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
        String CONFIRM_ERROR = "ACK_CNFE";
    }







    private void sendCommand(String command) {
        commandQueue.add(command);

        if (commandQueue.size() > 0) {
            serialService.sendMessage(commandQueue.get(0));
            Log.e("Serialservice", "CREATE: " + commandQueue.get(0));
            commandQueue.remove(0);
            handlerGetData.removeCallbacks(runnableGetData);
            handlerGetData.postDelayed(runnableGetData, 1000);
        }
    }




    public void checkGetDataRunnableIsAlive(){

        Log.e("ReadAndParse", "runnableIsAlive: " + runnableGetDataIsAlive);
        if(runnableGetDataIsAlive == false){
            handlerGetData.postDelayed(runnableGetData, 1000);
        }

    }




    private static ReadAndParseSerialService instance = new ReadAndParseSerialService();

    public static ReadAndParseSerialService getInstance() {
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

    public void sendStopCommand() {
        sendCommand(COMMANDS.CREATE(COMMANDS.CMD_STOP));
    }

    public void sendGetUserProgramCommand() {
        commandQueue.add("GET_USER\r\n");
        if (AppConstants.isIoSimulated) {
            //     Autoclave.getInstance().setUserDefinedProgram(new Profile("", 1, "user defined prog", 2, 10, 105, 204, 0, 5, "user def", true, true, null, 7));
        }
    }

    public void getProgram(int index) {
        sendCommand(COMMANDS.CREATE(COMMANDS.GET_PROGRAMS, index));
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
    boolean[] digitalData = new boolean[AppConstants.NUMBER_OF_DIGITAL_BITS];
    private String errorCode = "00000000";
    private String date = "";
    private String time = "";
    private int indexOfRunningProgram = 1;
    private String firmwareVersion = "";
    float[] pressures = new float[2];
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

                    Autoclave.getInstance().setProgramStep(programStep);
                    Autoclave.getInstance().setErrorCode(errorCode);
                    Autoclave.getInstance().setDate(date);
                    Autoclave.getInstance().setTime(time);
                    Autoclave.getInstance().setIndexOfRunningProgram(indexOfRunningProgram);
                    Autoclave.getInstance().setMicrocontrollerReachable(true);
                    Autoclave.getInstance().getController().setCycleNumber(cycleNumber);
                    Autoclave.getInstance().getController().setFirmwareVersion(firmwareVersion);

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
        if(AppConstants.TABLET_TYPE.equals(AppConstants.TABLET_TYPE_FAYTECH)) {
            serialPathAutoclave = "/dev/ttyS4";
        }else{
            serialPathAutoclave = "/dev/ttymxc3";
        }
        if(AppConstants.MODEL_CURRENT.equals(AppConstants.MODEL_RAYPA_TLV)) {
            serialBaudAutoclave = 38400;
        }else{
            serialBaudAutoclave = 9600;
        }

        serialService = new SerialService(serialPathAutoclave, serialBaudAutoclave);
        handlerGetData.post(runnableGetData);

//        serialThread.start();
        if (AppConstants.isIoSimulated == false) {
            serialService.setOnMessageReceivedListener(this);
            serialService.startReadSerialThread();
        }
    }


    @Override
    public void onMessageReceived(String message) {

        try {
            message = new String(message.getBytes(), "UTF-8");
        }catch (Exception e){
            Log.e("ReadAndParse", "error transform to utf-8");
        }
           Log.e("ReadAndParse", "received: " + message);
        //TODO if need add a checksum error
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
                //TODO prase 2
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
                case RESPONSES.ACK_DATA:

                    if (responseParameters.length == NUMBER_OF_DAT_RESPONSE_PARAMETERS) {
                        date = responseParameters[INDEX_DAT_DATE];
                        time = responseParameters[INDEX_DAT_TIME];
                        indexOfRunningProgram = Integer.parseInt(responseParameters[INDEX_DAT_PROGRAM_INDEX]);
                        cycleNumber = Integer.parseInt(responseParameters[INDEX_DAT_CYCLE]);
                        temperatures[0] = Float.parseFloat(responseParameters[INDEX_DAT_TEMP_STEAM]);
                        temperatures[1] = Float.parseFloat(responseParameters[INDEX_DAT_TEMP_MEDIA]);
                        temperatures[2] = Float.parseFloat(responseParameters[INDEX_DAT_TEMP_OPTIONAL_1]);
                        temperatures[3] = Float.parseFloat(responseParameters[INDEX_DAT_TEMP_OPTIONAL_2]);
                        pressures[0] = Float.parseFloat(responseParameters[INDEX_DAT_PRESSURE]);
                        pressures[1] = Float.parseFloat(responseParameters[INDEX_DAT_PRESSURE_OPTIONAL]);


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
                        Log.e("ReadAndParseService", "temp: " + temperatures[0] + "\n" +
                                "closed: " + isDoorClosed + "\n" +
                                "locked: " + isDoorLocked + "\n" +
                                "finished: " + isProgramFinished + "\n" +
                                "running: " + isProgramRunning + "\n" +
                                "isWaterLevelLow: " + isWaterLevelSourceLow + "\n" +
                                "isBinFull: " + isWaterLevelBinFull + "\n" +
                                "errorCode: " + errorCode + "\n" +
                                "press: " + pressures[0] + " and " + pressures[1] + "\n" +
                                "cylce: " + cycleNumber + "\n" +
                                "date: " + date + "\n" +
                                "time: " + time + "\n" +
                                "index of program: " + indexOfRunningProgram + "\n");
                    }

                    break;

                case RESPONSES.ACK_PROG:

                    Float pressure = 0f;
                    if(Float.valueOf(responseParameters[INDEX_PROGRAM_TEMP])>=100){
                        pressure = (float) (0.006112*Math.exp(   (17.62*Float.valueOf(responseParameters[INDEX_PROGRAM_TEMP]) )/(243.12+Float.valueOf(responseParameters[INDEX_PROGRAM_TEMP]))   )  -1);
                        pressure = ((int) (pressure*100))/100.0f;
                    }


                    if (responseParameters.length == NUMBER_OF_PROGRAM_RESPONSE_PARAMETERS) {
                        Profile profile = new Profile("",
                                1,
                                responseParameters[INDEX_PROGRAM_NAME].replaceAll("[^ -~]", "").replace("_"," "),
                                Integer.valueOf(responseParameters[INDEX_PROGRAM_PULSE_VACUUM]),
                                Integer.valueOf(responseParameters[INDEX_PROGRAM_STERILIZATION_TIME]),
                                Float.valueOf(responseParameters[INDEX_PROGRAM_TEMP]),
                                pressure,
                                0,
                                10,
                                null,
                                true,
                                true,
                                Integer.valueOf(responseParameters[INDEX_PROGRAM_IS_LIQUID_PROGRAM]) == 1,
                                null,
                                Integer.valueOf(responseParameters[INDEX_PROGRAM_NUM]));
                        Message msg = new Message();
                        msg.what = HANDLER_MSG_ACK_PROGRAM;
                        msg.obj = profile;
                        Log.e("ReadAndParseSerialS", "PROGRAM PARSED: " + profile.getName() + " " + profile.getSterilisationTemperature());
                        handler.sendMessage(msg);
                    } else {
                        publishResult(new ErrorModel(null, ERROR_PARSING), false, HANDLER_MSG_ACK_PROGRAM);
                    }
                    break;

            }
        } catch (Exception e) {
            Log.e("onMessageReceived", e.toString());
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
        temperatures[2] = 0;
        firmwareVersion = "SIM V1";
        digitalData[AppConstants.DIGITAL_DOOR_CLOSED_INDEX] = isDoorClosed;
        digitalData[AppConstants.DIGITAL_DOOR_LOCKED_INDEX] = isDoorLocked;
        digitalData[AppConstants.DIGITAL_PROGRAM_FINISHED_INDEX] = isProgramFinished;
        digitalData[AppConstants.DIGITAL_PROGRAM_RUNNING_INDEX] = isProgramRunning;
        digitalData[AppConstants.DIGITAL_WATER_LVL_LOW_INDEX] = isWaterLevelSourceLow;
        digitalData[AppConstants.DIGITAL_WATER_LVL_FULL_INDEX] = isWaterLevelBinFull;
        digitalData[AppConstants.DIGITAL_FAIL_WATER_QUALITY] = isWaterQualityBad;
        //TODO simulate manaul stop error
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
}
