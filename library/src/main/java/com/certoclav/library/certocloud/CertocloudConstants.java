package com.certoclav.library.certocloud;



public class CertocloudConstants {

	/**
	 * CertoCloud REST API
	 * Routes that can be accessed by everyone
	 */
	public final static String SERVER_URL = "http://api-certocloud.rhcloud.com";//www.ng-certocloud.rhcloud.com";
	public final static String REST_API_POST_LOGIN = "/login";// auth.login);
	public final static String REST_API_POST_SIGNUP = "/signup";// auth.signup);
	public final static String REST_API_POST_SIGNUP_EXIST = "/signup/exist";// auth.userExist);
	public final static String REST_API_POST_SIGNUP_ACTIVATE = "/signup/activate";// auth.userExist);
	public final static String REST_API_POST_SIGNUP_RESEND_KEY = "/signup/resend";//
 
	/** 
	 * CertoCloud REST API
	 * Routes that can be accessed only by autheticated users
	 */
	public final static String REST_API_POST_PROTOCOLS = "/api/protocols/";//, devices.getAll);
	public final static String REST_API_GET_DEVICES = "/api/devices/";//, devices.getAll);
	public final static String REST_API_GET_CONDITIONS = "/api/conditions/"; //+safetykey
	public final static String REST_API_GET_IEMS = "/api/balanceitem/"; //+safetykey
	public final static String REST_API_POST_CONDITIONS_CREATE = "/api/conditions/"; //+safetykey //warning: only create condition if there is no
	public final static String REST_API_POST_CONDITIONS_UPDATE = "/api/conditions/update/";
	public final static String REST_API_POST_DEVICE = "/api/devices/";// devices.create);
	public final static String REST_API_PUT_DEVICE_RENAME = "/api/devices/";// devices.rename);
	public final static String REST_API_DELETE_DEVICE = "/api/devices/";// devices.delete);
	public final static String REST_API_GET_PROFILES = "/api/programs/";// programs.getAll);
	public final static String REST_API_POST_PROFILE = "/api/programs/";// programs.create);
	public final static String REST_API_DELETE_PROFILE = "/api/programs/";// programs.delete);
	public final static String REST_API_GET_PROTOCOLS = "/api/protocols/";

	public final static String REST_POST_SUPPORT = "/api/support";// support.send); send email to certosupport
	public final static String REST_API_POST_EMAIL = "/api/support/email/"; //send notification email to a specific email address
	public final static String REST_API_POST_SMS = "/api/support/sms/"; // send notification sms to a specific phone

	/**
	 * Routes that can be accessed only by admin users
	 */
	public final static String REST_POST_CREATE_DEVICE = "/api/admin/devices/";// devices.createAdmin);
	





	


}