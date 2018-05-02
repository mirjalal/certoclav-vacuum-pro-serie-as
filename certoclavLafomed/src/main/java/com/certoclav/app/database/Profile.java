package com.certoclav.app.database;


import com.certoclav.app.R;
import com.certoclav.library.application.ApplicationController;
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

    // id is generated by the database and set on the object automatically
    @DatabaseField(generatedId = true, columnName = "profile_id")
    private int profile_id;

    @DatabaseField(columnName = "is_visible")
    private Boolean isVisible;

    @DatabaseField(columnName = "is_liquid_program")
    private Boolean isLiquidProgram;

    @DatabaseField(columnName = FIELD_CLOUD_ID)
    private String cloudId;

    @DatabaseField(columnName = "version")
    private int version;

    @DatabaseField(columnName = "name")
    @SerializedName("title")
    private String name;


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


    @DatabaseField(columnName = FIELD_LAST_USED_TIME)
    private long recentUsedDate = new Date().getTime();


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
        return sterilisationTime;
    }

    public void setSterilisationTime(int sterilisationTime) {
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

    public void setSterilisationTemperature(int sterilisationTemperature) {
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
    private Boolean isLocal;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "controller_id")
    private Controller controller;


    /*
     * Profile index between [1 and 7] . Only neccassary for Multicontrol and Essential version
     */
    @DatabaseField(columnName = FIELD_INDEX, unique = true)
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


    public Boolean getIsLocal() {
        return isLocal;
    }

    public void setIsLocal(Boolean isLocal) {
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
/*        try {
            switch (getIndex()) {
                case 1:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_1_name);
                case 2:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_2_name);
                case 3:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_3_name);
                case 4:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_4_name);
                case 5:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_5_name);
                case 6:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_6_name);
                case 7:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_7_name);
                case 8:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_8_name);
                case 9:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_9_name);
                case 10:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_10_name);
                case 11:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_11_name);
                case 12:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_12_name);
                default:
                    return name;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
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

    public Profile(String cloudId, int version, String name, int vacuumTimes, int sterilisationTime, float sterilisationTemperature, float sterilisationPressure, int vacuumPersistTime, int dryTime, String description, Boolean isLocal, Boolean isVisible, boolean isLiquidProgram, Controller controller, Integer index) {
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
    }


    public String getDescription() {
        try {
            switch (getIndex()) {
                case 1:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_1_description);
                case 2:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_2_description);
                case 3:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_3_description);
                case 4:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_4_description);
                case 5:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_5_description);
                case 6:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_6_description);
                case 7:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_7_description);
                case 8:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_8_description);
                case 9:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_9_description);
                case 10:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_10_description);
                case 11:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_11_description);
                case 12:
                    return ApplicationController.getInstance().getApplicationContext().getString(R.string.program_12_description);
                default:
                    return description == null ? "" : description;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}




