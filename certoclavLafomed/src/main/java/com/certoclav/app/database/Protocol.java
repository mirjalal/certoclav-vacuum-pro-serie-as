package com.certoclav.app.database;


import android.os.Environment;
import android.util.Log;

import com.certoclav.app.model.Autoclave;
import com.certoclav.app.util.Helper;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * A simple demonstration object we are creating and persisting to the database.
 */
@DatabaseTable(tableName = "protocols")
public class Protocol {

    public static final String FIELD_PROTOCOL_ID = "protocol_id";
    public static final String FIELD_PROTOCOL_NAME_LOCAL = "profileNameLocal";
    public static final String FIELD_PROGRAM_NAME = "profileStepsLocal";
    public static final String FIELD_PROTOCOL_START_TIME = "startTime";
    public static final String FIELD_USER_EMAIL = "user_email";
    public static final String FIELD_PROTOCOL_UPLOADED = "uploaded";
    public static final String FIELD_PROTOCOL_CLOUD_ID = "cloud_id";
    public static final String FIELD_PROGRAM_STATUS = "errorId";
    public static final String FIELD_TEMP_UNIT = "temp_unit";
    public static final String FIELD_IS_CONT_BY_FLEX_PROBE_1 = "is_cont_by_flex_probe_1";
    public static final String FIELD_IS_CONT_BY_FLEX_PROBE_2 = "is_cont_by_flex_probe_2";
    // id is generated by the database and set on the object automatically
    @DatabaseField(generatedId = true, columnName = FIELD_PROTOCOL_ID)
    private int protocol_id;

    @DatabaseField(columnName = "zyklusNumber")
    @SerializedName("cycle")
    private int zyklusNumber;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "controller_id")
    private Controller controller;

    @ForeignCollectionField(orderColumnName = "timestamp")//bei abfrage nach timeStamp sortieren
    private ForeignCollection<ProtocolEntry> protocolEntry;

    @DatabaseField(columnName = FIELD_PROGRAM_STATUS)
    @SerializedName("errcode")
    private int errorCode;

    @DatabaseField(columnName = FIELD_IS_CONT_BY_FLEX_PROBE_1)
    @SerializedName("is_cont_by_flex_probe_1")
    private Boolean isContByFlexProbe1;

    @DatabaseField(columnName = FIELD_IS_CONT_BY_FLEX_PROBE_2)
    @SerializedName("is_cont_by_flex_probe_2")
    private Boolean isContByFlexProbe2;

    @DatabaseField(columnName = FIELD_PROTOCOL_START_TIME, dataType = DataType.DATE)
    @SerializedName("start")
    private Date startTime;

    @DatabaseField(columnName = "endTime", dataType = DataType.DATE)
    @SerializedName("end")
    private Date endTime;

    @DatabaseField(columnName = FIELD_PROTOCOL_UPLOADED)
    private boolean uploaded;

    @DatabaseField(columnName = FIELD_USER_EMAIL)
    private String userEmail;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "user_id")
//nach string firstName kann man nicht sortieren
    private User user;

    @DatabaseField(columnName = FIELD_PROTOCOL_CLOUD_ID)
    @SerializedName("_id")
    private String cloudId;

    @DatabaseField(columnName = "version")
    private int version;

    @DatabaseField(columnName = "vacuum_times")
    private int vacuumTimes;

    @DatabaseField(columnName = "sterilisation_time")
    private int sterilisationTime;

    @DatabaseField(columnName = "sterilisation_temp")
    private float sterilisationTemperature;

    @DatabaseField(columnName = "sterilisation_pressure")
    private float sterilisationPressure;

    @DatabaseField(columnName = "vaccum_persist_time")
    private int vacuumPersistTime;

    @DatabaseField(columnName = "vaccum_persist_temp")
    private float vacuumPersistTemperature;

    @DatabaseField(columnName = "dry_time")
    private int dryTime;

    @DatabaseField(columnName = FIELD_PROGRAM_NAME)
    private String profileName;

    @DatabaseField(columnName = "profile_description")
    private String profileDescription;

    private List<Profile> program;


    public Profile getProgram() {
        return (program != null && program.size() > 0) ? program.get(0) : null;
    }

    public int getVacuumTimes() {
        return vacuumTimes;
    }

    public void setVacuumTimes(int vacuumTimes) {
        this.vacuumTimes = vacuumTimes;
    }

    public int getSterilisationTime() {
        return sterilisationTime;
    }

    public void setSterilisationTime(int sterilisationTime) {
        this.sterilisationTime = sterilisationTime;
    }

    public float getSterilisationTemperature() {
        return sterilisationTemperature;
    }

    public void setSterilisationTemperature(int sterilisationTemperature) {
        this.sterilisationTemperature = sterilisationTemperature;
    }

    public float getSterilisationPressure() {
        return sterilisationPressure;
    }

    public void setSterilisationPressure(int sterilisationPressure) {
        this.sterilisationPressure = sterilisationPressure;
    }

    public int getVacuumPersistTime() {
        return vacuumPersistTime;
    }

    public void setVacuumPersistTime(int vacuumPersistTime) {
        this.vacuumPersistTime = vacuumPersistTime;
    }

    public float getVacuumPersistTemperature() {
        return vacuumPersistTemperature;
    }

    public void setVacuumPersistTemperature(int vacuumPersistTemperature) {
        this.vacuumPersistTemperature = vacuumPersistTemperature;
    }

    public int getDryTime() {
        return dryTime;
    }

    public void setDryTime(int dryTime) {
        this.dryTime = dryTime;
    }


    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileDescription() {
        try {
            String description = this.profileDescription;
            String temp = description.substring(description.indexOf("[") + 1, description.indexOf("]"));
            return description.replaceAll("\\[(.*?)\\]",
                    Helper.getInstance().celsiusToCurrentUnit(Float.valueOf(temp))
                            + " " + Helper.getInstance().getTemperatureUnitText(null) + " ");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return profileDescription;
    }

    public void setProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }


    public void setProtocol_id(int protocol_id) {
        this.protocol_id = protocol_id;
    }

    public void setProtocolEntry(ForeignCollection<ProtocolEntry> protocolEntry) {
        this.protocolEntry = protocolEntry;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }


    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }


    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCloudId() {
        return cloudId;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }


    public Boolean isContByFlexProbe1() {
        return isContByFlexProbe1 != null && isContByFlexProbe1;
    }

    public Boolean isContByFlexProbe2() {
        return isContByFlexProbe1 != null && isContByFlexProbe2 != null && isContByFlexProbe1 && isContByFlexProbe2;
    }

    public void setContByFlexProbe1(Boolean contByFlexProbe1) {
        isContByFlexProbe1 = contByFlexProbe1;
    }

    public void setContByFlexProbe2(Boolean contByFlexProbe2) {
        isContByFlexProbe2 = contByFlexProbe2;
    }

    //Protocol can be selected (protocoloverview.java)
    boolean selected = false;


    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    Protocol() {
        // needed by ormlite
    }

    public Protocol(String cloudId, int version, Date startTime, Date endTime, int zykklusNumber,
                    Controller controller, User user, Profile profile, int errorCode, boolean uploaded) {
        this.cloudId = cloudId;
        this.version = version;
        this.startTime = startTime;
        this.endTime = endTime;
        this.zyklusNumber = zykklusNumber;
        this.controller = controller;
        this.user = user;

        if (profile != null) {
            try {
                this.isContByFlexProbe1 = profile.isContByFlexProbe1Enabled();
                this.isContByFlexProbe2 = profile.isContByFlexProbe2Enabled();
            } catch (Exception e) {
                //It means that the protocols have been downloaded from cloud
            }
            //copy of profile, because the original profile will be deleted after a while
            StringBuilder sb = new StringBuilder();
            sb.append(profile.getDescription(false));
            if (!uploaded) {
                HashMap<String, Integer> contents = new HashMap<>();
                for (String contentString : Autoclave.getInstance().getListContent()) {
                    contents.put(contentString, contents.containsKey(contentString) ? contents.get(contentString) + 1 : 1);
                }
                for (String key : contents.keySet())
                    sb.append("\n").append(contents.get(key) + " x ").append(key);
            }

            this.profileDescription = sb.toString();
            this.profileName = profile.getName();
            this.sterilisationPressure = profile.getSterilisationPressure();
            this.sterilisationTemperature = profile.getSterilisationTemperature(true);
            this.sterilisationTime = profile.getSterilisationTime();
            this.vacuumPersistTemperature = profile.getVacuumPersistTemperature();
            this.vacuumPersistTime = profile.getVacuumPersistTime();
            this.vacuumTimes = profile.getVacuumTimes();
            this.dryTime = profile.getDryTime();
        }
        if (user != null) {
            this.userEmail = user.getEmail();
        }
        this.errorCode = errorCode;
        this.uploaded = uploaded;


    }

    public int getLog_id() {
        return protocol_id;
    }

    public void setLog_id(int log_id) {
        this.protocol_id = log_id;
    }

    public int getErrorCode() {
        Log.e("protocol.java", "return errorcode");
        return errorCode;

    }


    public int getZyklusNumber() {
        return zyklusNumber + 1;
    }

    public void setZyklusNumber(int zyklusNumber) {
        this.zyklusNumber = zyklusNumber;
    }


    public void setUser(User user) {
        this.user = user;
    }


    public Date getStartTime() {

        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }


    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getProtocol_id() {
        return protocol_id;
    }

    public ForeignCollection<ProtocolEntry> getProtocolEntry() {
        return protocolEntry;
    }

    @Override
    public String toString() {
        String test = "test";
        return test;

    }

    public String getFileNameTxt() {
        StringBuilder sb = new StringBuilder();
        return sb.append("protocol").append(getProtocol_id()).append(".txt").toString();
    }

    public String getFileNameBmp() {
        StringBuilder sb = new StringBuilder();
        return sb.append("protocol").append(getProtocol_id()).append(".bmp").toString();
    }

    public String getFileNameZip() {
        StringBuilder sb = new StringBuilder();
        return sb.append("protocol").append(getProtocol_id()).append(".zip").toString();
    }

    public String getPathStringZip() {
        StringBuilder sb = new StringBuilder();
        return sb.append(Environment.getExternalStorageDirectory() + File.separator + getFileNameZip()).toString();
    }


    public String getFormatedStartTime() {

        return new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(startTime);


    }

    public String getFormatedStartTimeHoursMinutes() {

        return new SimpleDateFormat("kk:mm").format(startTime);


    }

    public String getFormatedStartDate() {

        return new SimpleDateFormat("yyyy-MM-dd").format(startTime);


    }

    public String getFormatedEndTimeLong() {
        if (endTime != null) {

            return new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(endTime);
        }
        String noTime = null;
        noTime = "";

        return noTime;

    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setProtocolEntries(List<ProtocolEntry> protocolEntries) {
        this.protocolEntries = protocolEntries;
    }

    @SerializedName("entries")
    private List<ProtocolEntry> protocolEntries;


    public List<ProtocolEntry> getProtocolEntries() {
        return protocolEntries;
    }


}




