package com.certoclav.app;


/**
 * The Constants class defines the constant fields used in all application files.
 *
 * @author Iulia Rasinar &lt;iulia.rasinar@nordlogic.com&gt;
 */
public class AppConstants {

    public static final Boolean APPLICATION_DEBUGGING_MODE = false;
    public static final String DEFAULT_ADMIN_PASSWORD = "admin";
    public static final String DEFAULT_CLOUD_ADMIN_PASSWORD = "master@certocloud";
    public static final boolean SHOW_LOGS = false;
    public static final String PREFERENCE_KEY_SERVER_TYPE = "servertype";
    public static final int PREFERENCE_KEY_SERVER_CERTOCLOUD = 1;
    public static final int PREFERENCE_KEY_SERVER_MANUAL = 2;
    public static final int PREFERENCE_KEY_SERVER_LOCAL = 3;
    public static final String PREFERENCE_KEY_SERVER_IP = "serverurl";
    public static final String PREFERENCE_KEY_SERVER_NAME = "servername";
    public static final String PREFERENCE_KEY_SERVER_PORT = "serverport";
    public static Boolean isIoSimulated = true;
    public static Boolean TABLET_HAS_ROOT = false;
    public static final Boolean IS_CERTOASSISTANT = false;
    public static final String TABLET_TYPE_SAMSUNGT113W = "samsungt113w";
    public static final String TABLET_TYPE_LILLIPUT = "lilliput2"; //android 4.3
    //public static final String TABLET_TYPE = TABLET_TYPE_SAMSUNGT113W;
    public static final String TABLET_TYPE = TABLET_TYPE_LILLIPUT;
    public static final String MODEL_LAFOMED = "lafomed_v3"; //first autoclave with media sensor
    public static final String MODEL_CURRENT = MODEL_LAFOMED;
    public final static String MODEL = "Certoclav 18 Vac Pro";
    public static final String SIMULATED_SAVETY_KEY = "AAAAAAAAAAAAAAAAA";//63FBE2987481D9B6A";
    public static final String URL_UPDATE = "http://lvps46-163-113-210.dedicated.hosteurope.de:80/files/public-docs/";
    public static final String DOWNLOAD_LINK = AppConstants.URL_UPDATE + MODEL_CURRENT + "/" + AppConstants.TABLET_TYPE + "/update.zip";

    /**
     * CertoCloud REST API
     * Routes that can be accessed by everyone
     */
    public final static String SERVER_URL = "https://pure-plains-82009.herokuapp.com";//www.ng-certocloud.rhcloud.com";
    public final static String REST_API_POST_LOGIN = "/login";// auth.login);
    public final static String REST_API_POST_SIGNUP = "/signup";// auth.signup);
    public final static String REST_API_POST_SIGNUP_EXIST = "/signup/exist";// auth.userExist);

    /**
     * Routes that can be accessed only by autheticated users
     */
    public final static String REST_API_POST_PROTOCOLS = "/api/protocols/";//, devices.getAll);
    public final static String REST_API_GET_DEVICES = "/api/devices/";//, devices.getAll);
    public final static String REST_API_POST_DEVICE = "/api/devices/";// devices.create);
    public final static String REST_API_PUT_DEVICE_RENAME = "/api/devices/:devicekey";// devices.rename);
    public final static String REST_API_DELETE_DEVICE = "/api/devices/:devicekey";// devices.delete);
    public final static String REST_API_GET_PROFILES = "/api/programs/";// programs.getAll);
    public final static String REST_API_POST_PROFILE = "/api/programs/";// programs.create);
    public final static String REST_API_DELETE_PROFILE = "/api/programs/:id";// programs.delete);

    public final static String REST_POST_SUPPORT = "/api/support";// support.send);

    /**
     * Routes that can be accessed only by admin users
     */
    public final static String REST_POST_CREATE_DEVICE = "/api/admin/devices/";// devices.createAdmin);

    public static final String SYSTEM_APP_FOLDER = "priv-app";


    public static final String PREFREENCE_KEY_KEEP_TEMP = "switch_keep_temp";
    public static final String PREFREENCE_KEY_PREHEAT = "switch_preheat";
    public static final String PREFREENCE_KEY_USER_DEFINED = "preferences_program_user_defined";
    public static final String PREFERENCE_KEY_DOWNLOAD_PROTOCOLS = "preferences_download_protocols";
    public static final String PREFERENCE_KEY_ONLINE_MODE = "switch_snchronization";
    public static final String PREFERENCE_KEY_STEP_BY_STEP = "switch_step_by_step";
    public static final String PREFERENCE_KEY_MATERIAL_TEST = "switch_material_testing";
    public static final String PREFERENCE_KEY_SCAN_ITEM_ENABLED = "switch_scan_items";
    public static final String PREFERENCE_KEY_PRINT_ENTITES = "switch_print_entities";
    public static final String PREFERENCE_KEY_WIFI_ENABLED = "switch_wlan";
    public static final String PREFERENCE_KEY_CHOOSE_SERVER = "button_choose_server";
    public static final String PREFERENCE_KEY_PRINT_LABEL = "preferences_print_labels";
    public static final String PREFERENCE_KEY_BLUETOOTH_ENABLED = "switch_bluetooth";
    public static final String PREFERENCE_KEY_WIFI_MANAGER = "button_wifi";
    public static final String PREFERENCE_KEY_LAN_MANAGER = "button_lan";
    public static final String PREFERENCE_KEY_BLUETOOTH_MANAGER = "button_bluetooth";
    public static final String PREFERENCE_KEY_PROGRAMOPTIONS_VISIBLE = "options_programs_visible";
    public static final String PREFERENCE_KEY_STORAGE = "preferences_storage";
    public static final String PREFERENCE_KEY_DATE = "preferences_date";
    public static final String PREFERENCE_KEY_RESET = "preferences_reset";
    public static final String PREFERENCE_KEY_LANGUAGE = "preferences_language";
    public static final String PREFERENCE_KEY_HELP_PHONE = "preferences_help_phone";
    public static final String PREFERENCE_KEY_HELP_EMAIL = "preferences_help_email";
    public static final String PREFERENCE_KEY_PRODUCT_NAME = "preferences_pruduct_name";
    public static final String PREFERENCE_KEY_DEVICE_KEY = "preferences_device_key";
    public static final String PREFERENCE_KEY_ADMIN_PASSWORD = "preferences_admin_password";
    public static final String PREFERENCE_KEY_BATCH_NUMBER = "preferences_batch_number";
    public static final String PREFERENCE_KEY_SOFTWARE_UPDATE = "preferences_software_update";
    public static final String PREFERENCE_KEY_EXPORT_USB = "preferences_export_usb";
    public static final String PREFERENCE_KEY_EXPORT_SD = "preferences_export_sdcard";
    public static final String PREFERENCE_KEY_VERSION = "preferences_software_version";
    public static final String PREFERENCE_KEY_LANGUAGE_WAS_SET = "preferences_language_was_set";
    public static final String PREFERENCE_KEY_INTRODUCTION_DONE = "preference_intro_done";
    public static final String PREFERENCE_KEY_SOFTWARE_UPDATE_USB = "preferences_software_update_usb";
    public static final String PREFERENCE_KEY_SOFTWARE_UPDATE_SDCARD = "preferences_software_update_sdcard";
    public static final String PREFERENCE_KEY_SERIAL_NUMBER = "preferences_autoclave_serial_number";
    public static final String PREFERENCE_KEY_FIRMWARE_VERSION = "preferences_autoclave_firmware_version";
    public static final String PREFERENCE_KEY_CYCLE_NUMBER = "preferences_cycle_number";
    public static final String PREFERENCE_KEY_LIST_GRAPH = "islistgrapp";
    public static final String PREFERENCE_KEY_ID_OF_LAST_USER = "idoflastuser";


    public static final String TERMINATOR_COMMAND_EEPROM = ":00000001FF\n\r";
    public static final String INTENT_EXTRA_USER_ID = "user_id";
    public static final String INTENT_EXTRA_VIDEOFULLSCREENACTIVITY_VIDEO_PATH = "video_path";
    public static final String INTENT_EXTRA_RESUME_FROM_HUD = "intent_extra_resume_from_hud";
    public static final String MODE_MAINTAIN = "MAINTAIN";
    public static final String MODE_ACHIEVE = "ACHIEVE";
    public static final String MODE_FINISH = "FINISH";
    public static final String MODE_ADJUST = "ADJUST";
    public static final String MODE_VOID = "VOID";

    public static final int MODE_ACHIEVE_INT = 20;
    public static final int MODE_MAINTAIN_INT = 40;
    public static final int MODE_FINISH_INT = 60;

    public static final String SENSOR_TEMP_1 = "TEMP_1";
    public static final String SENSOR_TEMP_2 = "TEMP_2";
    public static final String SENSOR_TEMP_3 = "TEMP_3";
    public static final String SENSOR_PRESS = "PRESS";
    public static final String SENSOR_VOID = "VOID";

    public static final String VALVE_OPEN = "VLV_OPEN";
    public static final String VALVE_CLOSED = "VLV_CLOSED";
    public static final String VALVE_VOID = "VOID";

    public static final int VALUE_VACUUM = -120; //kPa
    public static final int VALUE_DRY_TEMPERATURE = 100; //kPa


    //GENERAL_STATUS WERTE VON GET_DIGI
    public static final int DIGITAL_PROGRAM_FINISHED_INDEX = 0;
    public static final int DIGITAL_PROGRAM_RUNNING_INDEX = 1;
    public static final int DIGITAL_DOOR_LOCKED_INDEX = 3;
    public static final int DIGITAL_DOOR_CLOSED_INDEX = 2;
    public static final int DIGITAL_WATER_LVL_LOW_INDEX = 4;
    public static final int DIGITAL_WATER_LVL_FULL_INDEX = 5;
    public static final int DIGITAL_FAIL_STOPPED_BY_USER = 6;
    public static final int DIGITAL_PREHEAT_ENABLED = 7;
    public static final int DIGITAL_KEEP_TEMP_ENABLED = 8;
    public static final int DIGITAL_FAIL_WATER_QUALITY = 9;
    public static final int DIGITAL_STERILIZATION_COUNTDOWN_STARTED = 10;
    public static final int DIGITAL_MEDIA_SENSOR_ENABLED = 11;
    public static final int NUMBER_OF_DIGITAL_BITS = 12;


    public static final String COUNTDOWN_STATE_REMAINING = "state_remaining";
    public static final String COUNTDOWN_STATE_ELAPSED = "state_elapsed";
    public static final String PREFS_NAME = "certoclav_preferences";
    public static final String SETTINGS_LAST_SELECTED_PROFILE = "settings_last_selected_profile";
    public static final String SETTINGS_LAST_SAVED_PATTERN = "settings_last_saved_pattern";

    public static final String STEP_CALLBACK_INDEX = "step_callback_index";
    public static final String MODE_CALLBACK_INDEX = "mode_callback_index";
    public static final String VALUE_CALLBACK_INDEX = "value_callback_index";
    public static final String CODE_CALLBACK_INDEX = "code_callback_index";
    public static final String PARA_CALLBACK_INDEX = "para_callback_index";
    public static final String DIGITAL_CALLBACK_INDEX = "digital_callback_index";
    public static final String TEMPERATURE_CALLBACK_INDEX = "temperature_callback_index";
    public static final String TIMESTAMP_CALLBACK_INDEX = "timestamp_callback_index";
    public static final String PRESSURE_CALLBACK_INDEX = "pressure_callback_index";
    public static final String CONNECTION_STATUS_CALLBACK_INDEX = "connection_status_callback_index";
    public static final String LAST_PROFILE_CALLBACK_INDEX = "protocol_id_callback_index";

    public static final String GET_TEMPERATURE_COMMAND = "GET_TEMP\n";
    public static final String GET_EEPROM_COMMAND = "GET_EEPR\n";
    public static final String PUT_EEPROM_COMMAND = "PUT_EEPR\n";
    public static final String GET_PRESSURE_COMMAND = "GET_PRES\n";
    public static final String GET_STATE_COMMAND = "GET_STAT\n";
    public static final String GET_INFO_COMMAND = "GET_INFO\n";
    public static final String GET_SAVETY_KEY_COMMAND = "GET_SKEY\n";
    public static final String START_COMMAND = "CMD_STAR\n";
    public static final String STOP_COMMAND = "CMD_STOP\n";
    public static final String RESUME_COMMAND = "CMD_RES\n";
    public static final String PRNR_COMMAND = "GET_PRNR\n";
    public static final String GET_DIGITAL_COMMAND = "GET_DIGI\n";
    public static final String GET_PARAMETER_COMMAND = "GET_PARA"; // addition data after GET_PARA
    public static final String RESET_CYCLE_COMMAND = "CMD_CYCL";
    public static final String PUT_SERIAL_COMMAND = "PUT_SERI";

    public static final int SENSORSDATA_IDENTIFIER = 3;
    public static final int STATE_IDENTIFIER = 4;
    // public static final int TEMPERATURE_IDENTIFIER = 0;
    // public static final int PRESSURE_IDENTIFIER = 1;
    public static final int CONNECTION_STATUS_IDENTIFIER = 2;

    public static final int PARAMETER_INDEX_TEMP1 = 1;
    public static final int PARAMETER_INDEX_TEMP2 = 2;
    public static final int PARAMETER_INDEX_TEMP3 = 3;
    public static final int PARAMETER_INDEX_PRESS_FIRST = 4;
    public static final int PARAMETER_INDEX_PRESS_SECOND = 6;
    public static final int PARAMETER_INDEX_PID = 7;
    public static final int PARAMETER_INDEX_POW = 8;
    public static final int PARAMETER_INDEX_SAVETY = 10;
    public static final int PARAMETER_INDEX_POL_CONTAINER = 13;
    public static final int PARAMETER_INDEX_POL_FLOAT_BOT = 14;
    public static final int PARAMETER_INDEX_POL_FLOAT_TOP = 15;
    public static final int PARAMETER_INDEX_POL_CLOSING = 16;
    public static final int PARAMETER_INDEX_POL_LOCKING = 17;

    //SterilisationAssistantSteps
    public static final int STEP_1_OPEN_LID = 1;
    public static final int STEP_2_FILL_UP_WATER = 2;
    public static final int STEP_3_PLACE_ITEMS = 3;
    public static final int STEP_4_CLOSE_LID = 4;
    public static final int STEP_5_PRESS_START = 5;


    /* will get values from string resources for localization */
    public static final String COUNT_DOWN_TIME_EXEPTION_TEXT = "CountDownTimerPausable is already in pause state, start counter before pausing it.";
    public static final String CANCEL_PROCESS_MESSAGE = "Process was cancelled!";
    public static final String NOT_CANCEL_PROCESS_MESSAGE = "Sorry, you need a valid pattern to stop process!";
    public static final String ACTION_BAR_TITLE = "Program Selection & Control";
    public static final String INFO_MESSAGE_RUNNING_PROFILE = "The User Interface is locked while process is runnig.";
    public static final String INTENT_EXTRA_PROFILE_ID = "extra_profile_id";
    public static final String PACKAGENAME = "com.certoclav.app";
    public static final String PREFERENCE_KEY_SERIAL_DEVICE = "DEVICE";
    public static final String PREFERENCE_KEY_SERIAL_BAUDRATE = "BAUDRATE";


    public static final String TVConfigurationID = "pff75tf";
    public static final String TVCongigurationToken = "4340c134-d721-860b-2432-9d38b8ac7b99";

}