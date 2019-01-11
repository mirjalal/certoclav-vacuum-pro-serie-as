package com.certoclav.app;


/**
 * The Constants class defines the constant fields used in all application files.
 *
 * @author Iulia Rasinar &lt;iulia.rasinar@nordlogic.com&gt;
 */
public interface AppConstants {

    Boolean APPLICATION_DEBUGGING_MODE = false;
    String DEFAULT_ADMIN_PASSWORD = "admin";
    String DEFAULT_CLOUD_ADMIN_PASSWORD = "master@certocloud";
    String DEFAULT_SUPER_ADMIN_PASSWORD = "raypa@super@admin";
    boolean SHOW_LOGS = true;
    boolean SHOW_DEBUG_LOGS = false;
    String PREFERENCE_KEY_SERVER_TYPE = "servertype";
    int PREFERENCE_KEY_SERVER_CERTOCLOUD = 1;
    int PREFERENCE_KEY_SERVER_MANUAL = 2;
    int PREFERENCE_KEY_SERVER_LOCAL = 3;
    String PREFERENCE_KEY_SERVER_IP = "serverurl";
    String PREFERENCE_KEY_SERVER_NAME = "servername";
    String PREFERENCE_KEY_SERVER_PORT = "serverport";
    String PREFERENCE_KEY_INDICATOR_TEST = "switch_indicator_check";
    String PREFERENCE_KEY_AUTOCLAVE_MODEL = "preferences_autoclave_model";
    String TAG = "RAYPA";
    boolean CHECK_CHECKSUM = false;
    int TEMP_MAX_INT = 135;
    int TEMP_MIN_INT = 105;

    int FORCE_STOP_DELAY = 5000;

    String DELETED_PROFILE_NAME = "VOID";
    int MAX_PROGRAM_COUNT = 50;
    Boolean TABLET_HAS_ROOT = false;
    Boolean IS_CERTOASSISTANT = false;
    String TABLET_TYPE_SAMSUNGT113W = "samsungt113w";
    String TABLET_TYPE_LILLIPUT = "lilliput2"; //android 4.3
    String TABLET_TYPE_FAYTECH = "faytech";
    String MODEL_LAFOMED = "lafomed_v3"; //first autoclave with media sensor
    String MODEL_RAYPA_TLV = "raypa_tlv"; //TLV autoclave series from Raypa

    /*TABLET AND AUTOCLAVE SELECTION*/

    String TABLET_TYPE = TABLET_TYPE_FAYTECH;//TABLET_TYPE_LILLIPUT
    String MODEL_CURRENT = MODEL_RAYPA_TLV;
    Boolean isIoSimulated = false;
    String MODEL = "TLV-150FA";
    String SIMULATED_SAVETY_KEY = "FF000003957204830";//63FBE2987481D9B6A";

    String URL_UPDATE = "http://lvps46-163-113-210.dedicated.hosteurope.de:80/files/public-docs/";
    String DOWNLOAD_LINK = AppConstants.URL_UPDATE + MODEL_CURRENT + "/" + AppConstants.TABLET_TYPE + "/update.zip";

    /**
     * CertoCloud REST API
     * Routes that can be accessed by everyone
     */
    String SERVER_URL = "https://pure-plains-82009.herokuapp.com";//www.ng-certocloud.rhcloud.com";
    String REST_API_POST_LOGIN = "/login";// auth.login);
    String REST_API_POST_SIGNUP = "/signup";// auth.signup);
    String REST_API_POST_SIGNUP_EXIST = "/signup/exist";// auth.userExist);

    /**
     * Routes that can be accessed only by autheticated users
     */
    String REST_API_POST_PROTOCOLS = "/api/protocols/";//, devices.getAll);
    String REST_API_GET_DEVICES = "/api/devices/";//, devices.getAll);
    String REST_API_POST_DEVICE = "/api/devices/";// devices.create);
    String REST_API_PUT_DEVICE_RENAME = "/api/devices/:devicekey";// devices.rename);
    String REST_API_DELETE_DEVICE = "/api/devices/:devicekey";// devices.delete);
    String REST_API_GET_PROFILES = "/api/programs/";// programs.getAll);
    String REST_API_POST_PROFILE = "/api/programs/";// programs.create);
    String REST_API_DELETE_PROFILE = "/api/programs/:id";// programs.delete);

    String REST_POST_SUPPORT = "/api/support";// support.send);

    /**
     * Routes that can be accessed only by admin users
     */
    String REST_POST_CREATE_DEVICE = "/api/admin/devices/";// devices.createAdmin);

    String SYSTEM_APP_FOLDER = "priv-app";


    String PREFREENCE_KEY_KEEP_TEMP = "switch_keep_temp";
    //    public static final String PREFREENCE_KEY_PREHEAT = "switch_preheat";
    String PREFREENCE_KEY_USER_DEFINED = "preferences_program_user_defined";
    String PREFERENCE_KEY_DOWNLOAD_PROTOCOLS = "preferences_download_protocols";
    String PREFERENCE_KEY_ONLINE_MODE = "switch_snchronization";
    String PREFERENCE_KEY_STEP_BY_STEP = "switch_step_by_step";
    String PREFERENCE_KEY_MATERIAL_TEST = "switch_material_testing";
    String PREFERENCE_KEY_SCAN_ITEM_ENABLED = "switch_scan_items";
    String PREFERENCE_KEY_PRINT_ENTITES = "switch_print_entities";
    String PREFERENCE_KEY_WIFI_ENABLED = "switch_wlan";
    String PREFERENCE_KEY_CHOOSE_SERVER = "button_choose_server";
    String PREFERENCE_KEY_PRINT_LABEL = "preferences_print_labels";
    String PREFERENCE_KEY_BLUETOOTH_ENABLED = "switch_bluetooth";
    String PREFERENCE_KEY_WIFI_MANAGER = "button_wifi";
    String PREFERENCE_KEY_LAN_MANAGER = "button_lan";
    String PREFERENCE_KEY_BLUETOOTH_MANAGER = "button_bluetooth";
    String PREFERENCE_KEY_PROGRAMOPTIONS_VISIBLE = "options_programs_visible";
    String PREFERENCE_KEY_STORAGE = "preferences_storage";
    String PREFERENCE_KEY_DATE = "preferences_date";
    String PREFERENCE_KEY_RESET = "preferences_reset";
    String PREFERENCE_KEY_LANGUAGE = "preferences_language";
    String PREFERENCE_KEY_HELP_PHONE = "preferences_help_phone";
    String PREFERENCE_KEY_HELP_EMAIL = "preferences_help_email";
    String PREFERENCE_KEY_PRODUCT_NAME = "preferences_pruduct_name";
    String PREFERENCE_KEY_DEVICE_KEY = "preferences_device_key";
    String PREFERENCE_KEY_ADMIN_PASSWORD = "preferences_admin_password";
    String PREFERENCE_KEY_BATCH_NUMBER = "preferences_batch_number";
    String PREFERENCE_KEY_SOFTWARE_UPDATE = "preferences_software_update";
    String PREFERENCE_KEY_EXPORT_USB = "preferences_export_usb";
    String PREFERENCE_KEY_EXPORT_SD = "preferences_export_sdcard";
    String PREFERENCE_KEY_VERSION = "preferences_software_version";
    String PREFERENCE_KEY_LANGUAGE_WAS_SET = "preferences_language_was_set";
    String PREFERENCE_KEY_INTRODUCTION_DONE = "preference_intro_done";
    String PREFERENCE_KEY_SOFTWARE_UPDATE_USB = "preferences_software_update_usb";
    String PREFERENCE_KEY_SOFTWARE_UPDATE_SDCARD = "preferences_software_update_sdcard";
    String PREFERENCE_KEY_SERIAL_NUMBER = "preferences_autoclave_serial_number";
    String PREFERENCE_KEY_FIRMWARE_VERSION = "preferences_autoclave_firmware_version";
    String PREFERENCE_KEY_CYCLE_NUMBER = "preferences_cycle_number";
    String PREFERENCE_KEY_LIST_GRAPH = "islistgrapportrace";
    String PREFERENCE_KEY_ID_OF_LAST_USER = "idoflastuser";


    String TERMINATOR_COMMAND_EEPROM = ":00000001FF\n\r";
    String INTENT_EXTRA_USER_ID = "user_id";
    String INTENT_EXTRA_VIDEOFULLSCREENACTIVITY_VIDEO_PATH = "video_path";
    String INTENT_EXTRA_RESUME_FROM_HUD = "intent_extra_resume_from_hud";
    String MODE_MAINTAIN = "MAINTAIN";
    String MODE_ACHIEVE = "ACHIEVE";
    String MODE_FINISH = "FINISH";
    String MODE_ADJUST = "ADJUST";
    String MODE_VOID = "VOID";

    int MODE_ACHIEVE_INT = 20;
    int MODE_MAINTAIN_INT = 40;
    int MODE_FINISH_INT = 60;

    String SENSOR_TEMP_1 = "TEMP_1";
    String SENSOR_TEMP_2 = "TEMP_2";
    String SENSOR_TEMP_3 = "TEMP_3";
    String SENSOR_PRESS = "PRESS";
    String SENSOR_VOID = "VOID";

    String VALVE_OPEN = "VLV_OPEN";
    String VALVE_CLOSED = "VLV_CLOSED";
    String VALVE_VOID = "VOID";

    int VALUE_VACUUM = -120; //kPa
    int VALUE_DRY_TEMPERATURE = 100; //kPa


    //GENERAL_STATUS WERTE VON GET_DIGI
    int DIGITAL_PROGRAM_FINISHED_INDEX = 0;
    int DIGITAL_PROGRAM_RUNNING_INDEX = 1;
    int DIGITAL_DOOR_CLOSED_INDEX = 2;
    int DIGITAL_DOOR_LOCKED_INDEX = 3;
    int DIGITAL_WATER_LVL_LOW_INDEX = 4;
    int DIGITAL_WATER_LVL_FULL_INDEX = 5;
    int DIGITAL_FAIL_WATER_QUALITY = 6;
    int NUMBER_OF_DIGITAL_BITS = 7;
    //    public static final int DIGITAL_FAIL_STOPPED_BY_USER = 6;
//    public static final int DIGITAL_PREHEAT_ENABLED = 7;
//    public static final int DIGITAL_KEEP_TEMP_ENABLED = 8;
//    public static final int DIGITAL_STERILIZATION_COUNTDOWN_STARTED = 10;
//    public static final int DIGITAL_MEDIA_SENSOR_ENABLED = 11;


    String COUNTDOWN_STATE_REMAINING = "state_remaining";
    String COUNTDOWN_STATE_ELAPSED = "state_elapsed";
    String PREFS_NAME = "certoclav_preferences";
    String SETTINGS_LAST_SELECTED_PROFILE = "settings_last_selected_profile";
    String SETTINGS_LAST_SAVED_PATTERN = "settings_last_saved_pattern";

    String STEP_CALLBACK_INDEX = "step_callback_index";
    String MODE_CALLBACK_INDEX = "mode_callback_index";
    String VALUE_CALLBACK_INDEX = "value_callback_index";
    String CODE_CALLBACK_INDEX = "code_callback_index";
    String PARA_CALLBACK_INDEX = "para_callback_index";
    String DIGITAL_CALLBACK_INDEX = "digital_callback_index";
    String TEMPERATURE_CALLBACK_INDEX = "temperature_callback_index";
    String TIMESTAMP_CALLBACK_INDEX = "timestamp_callback_index";
    String PRESSURE_CALLBACK_INDEX = "pressure_callback_index";
    String CONNECTION_STATUS_CALLBACK_INDEX = "connection_status_callback_index";
    String LAST_PROFILE_CALLBACK_INDEX = "protocol_id_callback_index";

    String GET_TEMPERATURE_COMMAND = "GET_TEMP\n";
    String GET_EEPROM_COMMAND = "GET_EEPR\n";
    String PUT_EEPROM_COMMAND = "PUT_EEPR\n";
    String GET_PRESSURE_COMMAND = "GET_PRES\n";
    String GET_STATE_COMMAND = "GET_STAT\n";
    String GET_INFO_COMMAND = "GET_INFO\n";
    String GET_SAVETY_KEY_COMMAND = "GET_SKEY\n";
    String START_COMMAND = "CMD_STAR\n";
    String STOP_COMMAND = "CMD_STOP\n";
    String RESUME_COMMAND = "CMD_RES\n";
    String PRNR_COMMAND = "GET_PRNR\n";
    String GET_DIGITAL_COMMAND = "GET_DIGI\n";
    String GET_PARAMETER_COMMAND = "GET_PARA"; // addition data after GET_PARA
    String RESET_CYCLE_COMMAND = "CMD_CYCL";
    String PUT_SERIAL_COMMAND = "PUT_SERI";

    int SENSORSDATA_IDENTIFIER = 3;
    int STATE_IDENTIFIER = 4;
    // public static final int TEMPERATURE_IDENTIFIER = 0;
    // public static final int PRESSURE_IDENTIFIER = 1;
    int CONNECTION_STATUS_IDENTIFIER = 2;

    int PARAMETER_INDEX_TEMP1 = 1;
    int PARAMETER_INDEX_TEMP2 = 2;
    int PARAMETER_INDEX_TEMP3 = 3;
    int PARAMETER_INDEX_PRESS_FIRST = 4;
    int PARAMETER_INDEX_PRESS_SECOND = 6;
    int PARAMETER_INDEX_PID = 7;
    int PARAMETER_INDEX_POW = 8;
    int PARAMETER_INDEX_SAVETY = 10;
    int PARAMETER_INDEX_POL_CONTAINER = 13;
    int PARAMETER_INDEX_POL_FLOAT_BOT = 14;
    int PARAMETER_INDEX_POL_FLOAT_TOP = 15;
    int PARAMETER_INDEX_POL_CLOSING = 16;
    int PARAMETER_INDEX_POL_LOCKING = 17;

    //SterilisationAssistantSteps
    int STEP_1_OPEN_LID = 1;
    int STEP_2_FILL_UP_WATER = 2;
    int STEP_3_PLACE_ITEMS = 3;
    int STEP_4_CLOSE_LID = 4;
    int STEP_5_PRESS_START = 5;


    /* will get values from string resources for localization */
    String COUNT_DOWN_TIME_EXEPTION_TEXT = "CountDownTimerPausable is already in pause state, start counter before pausing it.";
    String CANCEL_PROCESS_MESSAGE = "Process was cancelled!";
    String NOT_CANCEL_PROCESS_MESSAGE = "Sorry, you need a valid pattern to stop process!";
    String ACTION_BAR_TITLE = "Program Selection & Control";
    String INFO_MESSAGE_RUNNING_PROFILE = "The User Interface is locked while process is runnig.";
    String INTENT_EXTRA_PROFILE_ID = "extra_profile_id";
    String PACKAGENAME = "com.certoclav.app";
    String PREFERENCE_KEY_SERIAL_DEVICE = "DEVICE";
    String PREFERENCE_KEY_SERIAL_BAUDRATE = "BAUDRATE";


    String TVConfigurationID = "pff75tf";
    String TVCongigurationToken = "4340c134-d721-860b-2432-9d38b8ac7b99";


    //Parameters
    int PARAM_OFFSET_STEAM = 36;
    int PARAM_OFFSET_MEDIA = 37;
    int PARAM_OFFSET_PRESSURE_1 = 45;
    int PARAM_OFFSET_PRESSURE_2 = 46;

}