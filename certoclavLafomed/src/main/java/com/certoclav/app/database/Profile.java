package com.certoclav.app.database;


import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

/**
 * A simple demonstration object we are creating and persisting to the database.
 */
public class Profile {
    public static final String FIELD_INDEX = "index";
    public static final String FIELD_CLOUD_ID = "cloud_id";
    public static final String FIELD_IS_LOCAL = "isLocal";
    public static final String FIELD_LAST_USED_TIME = "lastusedtime";
    public static final String FIELD_IS_F0_ENABLED = "is_f0_enabled";
    public static final String FIELD_F0_VALUE = "f0_value";
    public static final String FIELD_IS_MAINTAIN = "is_maintain_enabled";
    public static final String FIELD_IS_CONT_BY_FLEX_PROBE_1 = "is_cont_by_flex_probe_1";
    public static final String FIELD_IS_CONT_BY_FLEX_PROBE_2 = "is_cont_by_flex_probe_2";
    public static final String FIELD_FINAL_TEMP = "final_temp";
    public static final String FIELD_Z_VALUE = "z_value";

    // id is generated by the database and set on the object automatically
    @DatabaseField(generatedId = true, columnName = "profile_id")
    private int profile_id;

    @DatabaseField(columnName = "is_visible")
    private Boolean isVisible;

    @DatabaseField(columnName = "is_liquid_program")
    @SerializedName("is_liquid")
    private Boolean isLiquidProgram;

    @DatabaseField(columnName = FIELD_IS_F0_ENABLED)
    @SerializedName("use_f_function")
    private Boolean isF0Enabled;

    @DatabaseField(columnName = FIELD_IS_MAINTAIN)
    @SerializedName("is_maintain_enabled")
    private Boolean isMaintainEnabled;

    @DatabaseField(columnName = FIELD_IS_CONT_BY_FLEX_PROBE_1)
    @SerializedName("is_cont_by_flex_probe_1")
    private Boolean isContByFlexProbe1;

    @DatabaseField(columnName = FIELD_IS_CONT_BY_FLEX_PROBE_2)
    @SerializedName("is_cont_by_flex_probe_2")
    private Boolean isContByFlexProbe2;

    @DatabaseField(columnName = FIELD_F0_VALUE)
    @SerializedName("f0_value")
    private float f0Value;

    @DatabaseField(columnName = FIELD_Z_VALUE)
    @SerializedName("z_value")
    private float zValue;

    @DatabaseField(columnName = FIELD_FINAL_TEMP)
    @SerializedName("final_temp")
    private float finalTemp;

    @DatabaseField(columnName = FIELD_CLOUD_ID)
    private String cloudId;

    @DatabaseField(columnName = "version")
    private int version;

    @DatabaseField(columnName = "name")
    @SerializedName("title")
    private String name;


    @DatabaseField(columnName = "vacuum_times")
    @SerializedName("vacuum_pulse")
    private int vacuumTimes;

    @DatabaseField(columnName = "sterilisation_time")
    private int sterilisationTime;

    @DatabaseField(columnName = "sterilisation_temp")
    @SerializedName("tmp")
    private float sterilisationTemperature;

    @DatabaseField(columnName = "sterilisation_pressure")
    private float sterilisationPressure;

    @DatabaseField(columnName = "vaccum_persist_time")
    private int vacuumPersistTime;

    @DatabaseField(columnName = "vaccum_persist_temp")
    private float vacuumPersistTemperature;

    @DatabaseField(columnName = "dry_time")
    private int dryTime;


    @DatabaseField(columnName = FIELD_LAST_USED_TIME)
    private long recentUsedDate = new Date().getTime();

    private Duration dur;

    private class Duration {
        int h;
        int m;
    }


    public void setRecentUsedDate(long recentUsedDate) {
        this.recentUsedDate = recentUsedDate;
    }

    public long getRecentUsedDate() {
        return recentUsedDate;
    }

    public int getVacuumTimes() {
        return vacuumTimes;
    }

    public void setVacuumTimes(int vacuumTimes) {
        this.vacuumTimes = vacuumTimes;
    }

    public int getSterilisationTime() {
        if (dur != null) {
            sterilisationTime = dur.h * 60 + dur.m;
            dur = null;
        }
        return sterilisationTime;
    }

    public void setSterilisationTime(int sterilisationTime) {
        //Remove cloud value
        dur = null;
        this.sterilisationTime = sterilisationTime;
    }

    public float getSterilisationTemperature() {
        return sterilisationTemperature;
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

    public void setSterilisationTemperature(float sterilisationTemperature) {
        this.sterilisationTemperature = sterilisationTemperature;
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

    @DatabaseField(columnName = "description")
    @SerializedName("note")
    private String description;

    @DatabaseField(columnName = FIELD_IS_LOCAL)
    private boolean isLocal;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "controller_id")
    private Controller controller;


    /*
     * Profile index between [1 and 7] . Only neccassary for Multicontrol and Essential version
     */
    @DatabaseField(columnName = FIELD_INDEX, unique = true)
    @SerializedName("id")
    private int index;


    //Ein ForeigenCollectionField ist ein extra feature
    //Es erlaubt eine Collection von Commands zu der Profil-Tabelle hinzuzuf�gen:
    //Immer wenn ein Profil bei einer query zur�ckgegeben wird, wird eine zweite Anfrage �ber
    //die Command-Tabelle ausgef�hrt. Diese ermittelte Collection von fremden Commands wird
    //dem zur�ckgegebenen Account hinzugef�gt.
    //
    //eager==false, also haben wir eine lazy connection
    //bei lazy connections sollte man nur den iterator() und den toArray() mehtoden benutzen wegen performance
    //
    //wenn von einem Profil aus auf ein command zugegriffen wird, der in ForeignCollection eingespeichert ist,
    //und man �ndert einen Wert von diesen command, dann reicht es nicht nur den profil-table upzudaten um die �nderung permanent zu speichern,
    //sondern die updatefunktion des foreigncollection.:
    //for(Command command : profil1.commands()){
    //command.changeTemp(130);
    //profil1.commands.update(command);
    //}


    public Boolean isLocal() {
        return isLocal;
    }

    public void setLocal(Boolean isLocal) {
        this.isLocal = isLocal;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }


    Profile() {
        // needed by ormlite
    }

    public Profile(String cloudId,
                   int version,
                   String name,
                   int vacuumTimes,
                   int sterilisationTime,
                   float sterilisationTemperature,
                   float sterilisationPressure,
                   int vacuumPersistTime,
                   int dryTime, String description,
                   Boolean isLocal, Boolean isVisible,
                   boolean isLiquidProgram,
                   Controller controller,
                   Integer index,
                   boolean isF0Enabled,
                   boolean isMaintainEnabled,
                   boolean isContByFlexProbe1,
                   boolean isContByFlexProbe2,
                   float finalTemp,
                   float f0Value,
                   float zValue) {
        this.cloudId = cloudId;
        this.version = version;
        this.description = description;
        this.name = name;
        this.vacuumTimes = vacuumTimes;
        this.sterilisationTime = sterilisationTime;
        this.sterilisationTemperature = sterilisationTemperature;
        this.sterilisationPressure = sterilisationPressure;
        this.vacuumPersistTime = vacuumPersistTime;
        this.dryTime = dryTime;
        this.isLocal = isLocal;
        this.index = index;
        this.isVisible = isVisible;
        this.controller = controller;
        this.isLiquidProgram = isLiquidProgram;
        this.isF0Enabled = isF0Enabled;
        this.zValue = zValue;
        this.isContByFlexProbe1 = isContByFlexProbe1;
        this.isContByFlexProbe2 = isContByFlexProbe2;
        this.isMaintainEnabled = isMaintainEnabled;
        this.finalTemp = finalTemp;
        this.f0Value = f0Value;
    }


    public Profile(int index) {
        this.index = index;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getVisible() {
        return isVisible;
    }

    public void setVisible(Boolean visible) {
        this.isVisible = visible;
    }

    public int getProfile_id() {
        return profile_id;
    }

    public void setProfile_id(int profile_id) {
        this.profile_id = profile_id;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setLiquidProgram(Boolean liquidProgram) {
        isLiquidProgram = liquidProgram;
    }

    public boolean isLiquidProgram() {
        return isLiquidProgram;
    }

    public Boolean isF0Enabled() {
        return isF0Enabled;
    }

    public float getzValue() {
        return zValue;
    }

    public void setzValue(float zValue) {
        this.zValue = zValue;
    }

    public Boolean isMaintainEnabled() {
        return isMaintainEnabled;
    }

    public void setMaintainEnabled(Boolean maintainEnabled) {
        isMaintainEnabled = maintainEnabled;
    }

    public Boolean isContByFlexProbe1Enabled() {
        return isContByFlexProbe1;
    }

    public Boolean isContByFlexProbe2Enabled() {
        return isContByFlexProbe2;
    }

    public void setContByFlexProbe1(Boolean contByFlexProbe1) {
        isContByFlexProbe1 = contByFlexProbe1;
    }

    public void setContByFlexProbe2(Boolean contByFlexProbe2) {
        isContByFlexProbe2 = contByFlexProbe2;
    }

    public float getF0Value() {
        return f0Value;
    }

    public void setF0Value(float f0Value) {
        this.f0Value = f0Value;
    }

    public float getFinalTemp() {
        return finalTemp;
    }

    public void setFinalTemp(float finalTemp) {
        this.finalTemp = finalTemp;
    }

    public void setF0Enabled(Boolean f0Enabled) {
        isF0Enabled = f0Enabled;
    }

    @Override
    public boolean equals(Object obj) {
        return ((Profile) obj).getIndex() == getIndex();
    }

    //Vacuum Test and BD Test is not editable, their index is 1 and 2.
    public boolean isEditable(){
        return index!=1 && index!=2;
    }
}




