package com.certoclav.app.service;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android_serialport_api.MessageReceivedListener;
import android_serialport_api.SerialService;

import com.certoclav.app.AppConstants;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Profile;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.library.application.ApplicationController;



public class ReadAndParseSerialService implements MessageReceivedListener {
	//ACK_DAT 2017.01.27,16:32:13,12,2,20.1,158.6,105,-3,001010000101,0,22,81
	//        date,time,indexOfProgram,cyclenumber,tempSteam,tempMedia,targetPress,pressure,digital,error,version,checksum
	//			0    1        2            3           4        5           6           7     8       9     10       11

	//ACK_DAT 2016.10.12,16:05:55,9,149,48.0,.0,204,-9,001000000100,0,22,91\r\n"
	//        0         ,1       ,2,3  ,4   ,5 ,6  ,7 ,8           ,9,10,11
	
	public static final int INDEX_GET_DAT_DATE = 0;
	public static final int INDEX_GET_DAT_TIME = 1;
	public static final int INDEX_GET_DAT_PROGRAM_INDEX = 2;
	public static final int INDEX_GET_DAT_CYCLE = 3;
	public static final int INDEX_GET_DAT_TEMP_STEAM = 4;
	public static final int INDEX_GET_DAT_TEMP_MEDIA = 5;
	public static final int INDEX_GET_DAT_PRESS_TARGET = 6;
	public static final int INDEX_GET_DAT_PRESS = 7;
	public static final int INDEX_GET_DAT_DIGITAL = 8;
	public static final int INDEX_GET_DAT_ERRORCODE = 9;
	public static final int INDEX_GET_DAT_VERSION = 10;
	public static final int INDEX_GET_DAT_CHECKSUM = 11;
	public static final int GET_DAT_NUMBER_OF_RESPONSE_PARAMETERS = 12;
	
	Double offsetTemp1 = 0d;
	Double offsetTemp2 = 0d;
	Double offsetTemp3 = 0d;
	Double offsetPress = 0d;
	Double offsetPara4 = 0d;
	
	Profile userDefinedProgram = null;
	
	private static final int HANDLER_MSG_CALIBRATION = 1;
	private static final int HANDLER_MSG_DATA = 2;
	private static final int HANDLER_MSG_USER_PROGRAM = 3;
	private SerialService serialService = null;


	
	private static ReadAndParseSerialService instance = new ReadAndParseSerialService();

	public static ReadAndParseSerialService getInstance(){
		return instance;
	}

	public void sendStartCommand(int programIndex){
	commandQueue.add("CMD_STAR " + programIndex + "\r\n");
	}
	public void sendStopCommand(){
		commandQueue.add("CMD_STOP\r\n");
	}
	
	public void sendPreheatCommand(boolean preheatEnabled){
		if(preheatEnabled == true){
			commandQueue.add("CMD_PREH 1\r\n");
		}else{
			commandQueue.add("CMD_PREH 0\r\n");
		}
	}
	
	public void sendKeepTemperatureCommand(boolean keepTempEnabled){
		if(keepTempEnabled == true){
			commandQueue.add("CMD_KEEP 1\r\n");
		}else{
			commandQueue.add("CMD_KEEP 0\r\n");
		}
	}
	public void sendGetUserProgramCommand(){
			commandQueue.add("GET_USER\r\n");
			if(AppConstants.isIoSimulated){
				Autoclave.getInstance().setUserDefinedProgram(new Profile("",1,"user defined prog",2,10,105,204,0,5,"user def",true,true,null,7));
			}
	}
	/*
	 * parameter vacuumTimes: [01-10]
	 * parameter sterilizationTemp: [105-134]
	 * parameter sterilizationTime: [01-25]
	 * parameter dryingTime: [01-25]
	 */
	public void sendPutUserProgramCommand(Integer vacuumTimes, Integer sterilizationTemperature, Integer sterilizationTime, Integer dryingTime){
		commandQueue.add(String.format("CMD_USER %02d,%03d,%02d,%02d\r\n", vacuumTimes,sterilizationTemperature,sterilizationTime,dryingTime ));
	}
	
	public void sendGetAdjustParameterCommand(){
		commandQueue.add("GET_ADJU\r\n");
		if(AppConstants.isIoSimulated == true){
			Autoclave.getInstance().setAdjustParameters(0.1,-0.1,0.2,-10);
		}
	}

	public void sendPutAdjustParameterCommand(Double offsetTemp1, Double offsetTemp2, Double offsetTemp3, Integer offsetPress, Double offsetMedia){
		commandQueue.add(String.format("CMD_ADJU %.1f,%.1f,%.1f,%02d,%.1f\r\n", offsetTemp1,offsetTemp2,offsetTemp3,offsetPress,offsetMedia));
	}
	
	public ArrayList<String> getCommandQueue() {
		return commandQueue;
	}

	public void setCommandQueue(ArrayList<String> commandQueue) {
		this.commandQueue = commandQueue;
	}

	private ArrayList<String> commandQueue = new ArrayList<String>();
	
	
	

	//data parsed from GET_DATA
	private int cycleNumber = 0;
    boolean[] digitalData = new boolean[AppConstants.NUMBER_OF_DIGITAL_BITS];
    private int errorCode = 0;
    private String date = "";
    private String time = "";
    private int indexOfRunningProgram = 0;
    private String firmwareVersion = "";
    Float pressureCurrent = (float) 0;
	float[] temperatures = new float[3];
	
	private Handler handler = new Handler(Looper.getMainLooper()) {
	    @Override
	    public void handleMessage(Message msg) {
	    	  switch (msg.what) {
			  case HANDLER_MSG_USER_PROGRAM:
				  Autoclave.getInstance().setUserDefinedProgram(userDefinedProgram);
			  break;
		        case HANDLER_MSG_DATA:
					Autoclave.getInstance().setSensorsData(
							temperatures, 
							pressureCurrent, 
							digitalData);  
					
					Autoclave.getInstance().setErrorCode(errorCode);
					Autoclave.getInstance().setDate(date);
					Autoclave.getInstance().setTime(time);
					Autoclave.getInstance().setIndexOfRunningProgram(indexOfRunningProgram);
					Autoclave.getInstance().setMicrocontrollerReachable(true);
					Autoclave.getInstance().getController().setCycleNumber(cycleNumber);
					Autoclave.getInstance().getController().setFirmwareVersion(firmwareVersion);
		        	
		        	break;
		        case HANDLER_MSG_CALIBRATION:
		       	 Autoclave.getInstance().setAdjustParameters(offsetTemp1, offsetTemp2, offsetTemp3, offsetPress);
		       	 
		            break;
		 
		        default:

		           // super.handleMessage(msg);
		    }      	
	    }
	};

	
     

	private int counter = 0;


	private Thread serialThread = new Thread(new Runnable() {
		@Override
		public void run() {
			while(true) {
				if(AppConstants.isIoSimulated == true){
					simulateMessage();
				}else {
					try {
							if (commandQueue.size() >0) {
								serialService.sendMessage(commandQueue.get(0));
								Log.e("Serialservice", "SEND: " + commandQueue.get(0));
								commandQueue.remove(0);
							} else {
								serialService.sendMessage("GET_DAT\n");
								Log.e("Serialservice", "SEND: GET_DAT");
							}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}


				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	});

	
	private ReadAndParseSerialService() {
		serialService = new SerialService("/dev/ttymxc3",9600);
		serialThread.start();
		if(AppConstants.isIoSimulated == false){
			serialService.setOnMessageReceivedListener(this);
			serialService.startReadSerialThread();
		}
	}
	


	@Override
	public void onMessageReceived(String message) {
		
		Log.e("ReadAndParse", "received: " + message);
		String[] response = null;
		String[] responseParameters = null;
		try{
			response = message.split(" ");
			if(response.length == 2){
				responseParameters = response[1].split(",");
			}
		}catch(Exception e){
			Log.e("ReadAndParseSerial", "exception parsing response");
			return;
		}
		
		try{
			switch(response[0]){
			case "ACK_USER":
				// ACK_USER 03,121,20,08

				if(responseParameters == null){
					return;
				}
				if(responseParameters.length != 4){
					return;
				}
				Integer vacuumTimes = Integer.parseInt(responseParameters[0]);
				Integer sterilizationTemperature = Integer.parseInt(responseParameters[1]);
				Integer sterilizationTime = Integer.parseInt(responseParameters[2]);
				Integer dryingTime = Integer.parseInt(responseParameters[3].replace("\n", "").replace("\r", ""));
				
				userDefinedProgram = Autoclave.getInstance().getUserDefinedProgram();
				userDefinedProgram.setVacuumTimes(vacuumTimes);
				userDefinedProgram.setSterilisationTemperature(sterilizationTemperature);
				userDefinedProgram.setSterilisationTime(sterilizationTime);
				userDefinedProgram.setDryTime(dryingTime);
				handler.sendEmptyMessage(HANDLER_MSG_USER_PROGRAM);
				
				break;
			
				
			case "ACK_ADJU":
				//ACK_ADJU -1.0,0.0,0.0,00,-0.1
				//           0   1   2   3   4

				if(responseParameters == null){
					Log.e("ReadAndParseSerial", "parameters == null");
					return;
				}
				if(responseParameters.length != 5){
					Log.e("ReadAndParseSerial", "parameter length wrong " + responseParameters.length);
					return;
				}
				offsetTemp1 = Double.parseDouble(responseParameters[0]);
				offsetTemp2 = Double.parseDouble(responseParameters[1]);
				offsetTemp3 = Double.parseDouble(responseParameters[2]);
				offsetPress = Double.parseDouble(responseParameters[3]);
				offsetPara4 = Double.parseDouble(responseParameters[4]);
				Log.e("ReadAndParseSerial", "parameters: " + offsetTemp1 + " "+ offsetTemp2 + " " + offsetTemp3 + " "+ offsetPress);
				handler.sendEmptyMessage(HANDLER_MSG_CALIBRATION);
				
				
				break;
			case "ACK_DAT":
	
				if(responseParameters.length==GET_DAT_NUMBER_OF_RESPONSE_PARAMETERS){
					date = responseParameters[INDEX_GET_DAT_DATE];
					time = responseParameters[INDEX_GET_DAT_TIME];
					indexOfRunningProgram = Integer.parseInt(responseParameters[INDEX_GET_DAT_PROGRAM_INDEX]);
					cycleNumber = Integer.parseInt(responseParameters[INDEX_GET_DAT_CYCLE]);
					temperatures[0] = Float.parseFloat(responseParameters[INDEX_GET_DAT_TEMP_STEAM]);
					temperatures[1] = Float.parseFloat(responseParameters[INDEX_GET_DAT_TEMP_MEDIA]);
					pressureCurrent = Float.parseFloat(responseParameters[INDEX_GET_DAT_PRESS]);
					firmwareVersion = responseParameters[INDEX_GET_DAT_VERSION];
					boolean isProgramFinished = (responseParameters[INDEX_GET_DAT_DIGITAL].charAt(AppConstants.DIGITAL_PROGRAM_FINISHED_INDEX) == '1');
					boolean isProgramRunning = 	(responseParameters[INDEX_GET_DAT_DIGITAL].charAt(AppConstants.DIGITAL_PROGRAM_RUNNING_INDEX) == '1');
					boolean isDoorLocked = 		(responseParameters[INDEX_GET_DAT_DIGITAL].charAt(AppConstants.DIGITAL_DOOR_LOCKED_INDEX) == '1');
					boolean isDoorClosed = 		(responseParameters[INDEX_GET_DAT_DIGITAL].charAt(AppConstants.DIGITAL_DOOR_CLOSED_INDEX) == '1');
					boolean isWaterLevelSourceLow = (responseParameters[INDEX_GET_DAT_DIGITAL].charAt(AppConstants.DIGITAL_WATER_LVL_LOW_INDEX) == '1');
					boolean isWaterLevelBinFull = (responseParameters[INDEX_GET_DAT_DIGITAL].charAt(AppConstants.DIGITAL_WATER_LVL_FULL_INDEX) == '1');
					boolean isStopedByUser = (responseParameters[INDEX_GET_DAT_DIGITAL].charAt(AppConstants.DIGITAL_FAIL_STOPPED_BY_USER) == '1');
					boolean isPreheatOn= (responseParameters[INDEX_GET_DAT_DIGITAL].charAt(AppConstants.DIGITAL_PREHEAT_ENABLED) == '1');
					boolean isKeepTempOn= (responseParameters[INDEX_GET_DAT_DIGITAL].charAt(AppConstants.DIGITAL_KEEP_TEMP_ENABLED) == '1');
					boolean isWaterQualityBad= (responseParameters[INDEX_GET_DAT_DIGITAL].charAt(AppConstants.DIGITAL_FAIL_WATER_QUALITY) == '1');
					boolean isCountdownSterilizationStarted= (responseParameters[INDEX_GET_DAT_DIGITAL].charAt(AppConstants.DIGITAL_STERILIZATION_COUNTDOWN_STARTED) == '1');
					boolean isMediaSensorEnabled= (responseParameters[INDEX_GET_DAT_DIGITAL].charAt(AppConstants.DIGITAL_MEDIA_SENSOR_ENABLED) == '1');
					
					try{
						errorCode = Integer.parseInt(responseParameters[INDEX_GET_DAT_ERRORCODE]);
					}catch(Exception e){
						errorCode  = 0;
					}
					String checksum = responseParameters[9];
					
	
					digitalData[AppConstants.DIGITAL_DOOR_CLOSED_INDEX] = isDoorClosed;
					digitalData[AppConstants.DIGITAL_DOOR_LOCKED_INDEX] = isDoorLocked;
					digitalData[AppConstants.DIGITAL_PROGRAM_FINISHED_INDEX] = isProgramFinished;
					digitalData[AppConstants.DIGITAL_PROGRAM_RUNNING_INDEX] = isProgramRunning;
					digitalData[AppConstants.DIGITAL_WATER_LVL_LOW_INDEX] = isWaterLevelSourceLow;
				    digitalData[AppConstants.DIGITAL_WATER_LVL_FULL_INDEX] = isWaterLevelBinFull;
				    digitalData[AppConstants.DIGITAL_FAIL_STOPPED_BY_USER] = isStopedByUser;
				    digitalData[AppConstants.DIGITAL_KEEP_TEMP_ENABLED] = isKeepTempOn;
				    digitalData[AppConstants.DIGITAL_PREHEAT_ENABLED] = isPreheatOn;
				   // digitalData[AppConstants.DIGITAL_FAIL_WATER_QUALITY]
				    
				    handler.sendEmptyMessage(HANDLER_MSG_DATA);
				    Log.e("ReadAndParseService", "temp: " + temperatures[0] + "\n"+
				    							  "closed: " + isDoorClosed +"\n"+
				    							  "locked: " + isDoorLocked +"\n"+
				    							  "finished: " + isProgramFinished+"\n"+
				    							  "running: " + isProgramRunning +"\n"+
				    							  "isWaterLevelLow: " + isWaterLevelSourceLow+"\n"+
				    							  "isBinFull: " + isWaterLevelBinFull +"\n"+
				    							  "isStopedByUser: " + isStopedByUser +"\n"+
				    							  "errorCode: " + errorCode +"\n"+
				    							  "press: " + pressureCurrent +"\n"+
				    							  "cylce: " + cycleNumber +"\n" +
				    							  "date: " + date +"\n"+
				    							  "time: " + time +"\n" +
				    							  "index of program: " + indexOfRunningProgram + "\n");
				    
				break;
			}
			}	
			}catch(Exception e){
				Log.e("onMessageReceived", e.toString());
			}
		
		
				

			
	}
	
	private void simulateMessage(){
		
	
		Log.e("ReadAndParseSerialService", "simulate serial");

		cycleNumber = 1;

		Float temperature;
	
		
		pressureCurrent = (float) 1.0;
		boolean isProgramFinished = true;
		boolean isProgramRunning = false;
		boolean isDoorLocked = 		false;
		boolean isDoorClosed = 		true;
		boolean isWaterLevelSourceLow = false;
		boolean isWaterLevelBinFull = false;
		boolean isStopedByUser  = false;


		temperature = (float) (60 + (30.0* Math.sin(((double)counter)*0.02)));
		counter++;
		isDoorClosed = true;
		isDoorLocked = false;
		isProgramFinished = false;
		if(Autoclave.getInstance().getState() == AutoclaveState.PREPARE_TO_RUN
				|| Autoclave.getInstance().getState() == AutoclaveState.RUNNING
				|| Autoclave.getInstance().getState() == AutoclaveState.RUN_CANCELED){
			isProgramRunning = true;
			isDoorLocked = true;
		}else{
			isProgramRunning = false;
			isDoorLocked = false;
		}
		if(AutoclaveMonitor.getInstance().SimulatedFailStoppedByUser == true){
			isProgramRunning = false;
			isDoorLocked = false;
			isStopedByUser = true;
		}else{
			isStopedByUser = false;
		}
		isWaterLevelSourceLow = false;
		isWaterLevelBinFull = false;
		
		temperatures[0] = temperature;
		temperatures[1] = 0;
		temperatures[2] = 0;
		firmwareVersion = "SIM V1";
		digitalData[AppConstants.DIGITAL_DOOR_CLOSED_INDEX] = isDoorClosed;
		digitalData[AppConstants.DIGITAL_DOOR_LOCKED_INDEX] = isDoorLocked;
		digitalData[AppConstants.DIGITAL_PROGRAM_FINISHED_INDEX] = isProgramFinished;
		digitalData[AppConstants.DIGITAL_PROGRAM_RUNNING_INDEX] = isProgramRunning;
		digitalData[AppConstants.DIGITAL_WATER_LVL_LOW_INDEX] = isWaterLevelSourceLow;
	    digitalData[AppConstants.DIGITAL_WATER_LVL_FULL_INDEX] = isWaterLevelBinFull;
	    digitalData[AppConstants.DIGITAL_FAIL_STOPPED_BY_USER] = isStopedByUser;
	    
	    Log.e("ReadAndParseSerialService", "Handler sending message now:");
	    
	   handler.sendEmptyMessage(HANDLER_MSG_DATA);
	   
	    
		    
	}
}
