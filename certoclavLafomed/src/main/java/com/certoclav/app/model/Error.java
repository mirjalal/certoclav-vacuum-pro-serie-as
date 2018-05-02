package com.certoclav.app.model;









public class Error {
	public static final Integer TYPE_NOTIFICATION = 1;
	public static final Integer TYPE_WARNING = 2;
	public static final Integer TYPE_ERROR = 3;

	private String msg = "";
	private String pathVideo = "";


	private int type = 0;
	private int errorID =0;
	
	
	/*
	 * @param type: Can be TYPE_NOTIFICATION, TYPE_WARNING or TYPE_ERROR
	 * if type equals error <=> user must press ok button to hide the error
	 */
	Error(String msg, String pathVideo, int type, int errorID){
		this.msg = msg;
		this.pathVideo = pathVideo;
		this.type = type;
		this.errorID = errorID;
	}


	public String getMsg() {
		return msg;
	}


	public void setMsg(String msg) {
		this.msg = msg;
	}


	public String getPathVideo() {
		return pathVideo;
	}


	public void setPathVideo(String pathVideo) {
		this.pathVideo = pathVideo;
	}


	public int getType() {
		return type;
	}


	public void setType(int type) {
		this.type = type;
	}


	public int getErrorID() {
		return errorID;
	}
	
	public void setErrorID(int eErrorID) {
		errorID = eErrorID;
	}






}
