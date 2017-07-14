package com.certoclav.app.database;


import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple demonstration object we are creating and persisting to the database.
 */
@DatabaseTable(tableName = "protocolEntrys")
public class ProtocolEntry {

    // id is generated by the database and set on the object automatically
    @DatabaseField(generatedId = true, columnName = "protocolEntry_id")
    private int protocolEntry_id;

    @DatabaseField(columnName = "timestamp", dataType = DataType.DATE)
    private Date timestamp;

    @SerializedName("ts")
    private float ts;

    public float getTs() {
        return ts;
    }


    @DatabaseField(columnName = "temperature")
    @SerializedName("tmp")
    private float temperature;

    @DatabaseField(columnName = "media_temp")
    @SerializedName("mtmp")
    private float mediaTemperature;

    public float getMediaTemperature() {
        return mediaTemperature;
    }

    public void setMediaTemperature(float mediaTemperature) {
        this.mediaTemperature = mediaTemperature;
    }

    @DatabaseField(columnName = "pressure")
    @SerializedName("prs")
    private float pressure;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "protocol_id")
    private Protocol protocol;

    private String timeStampWithMin;


    public int getProtocolEntry_id() {
        return protocolEntry_id;
    }

    public void setProtocolEntry_id(int protocolEntry_id) {
        this.protocolEntry_id = protocolEntry_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }


    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }


    ProtocolEntry() {
        // needed by ormlite
    }

    public ProtocolEntry(Date timeStamp, float temperature, float mediaTemperature, float pressure, Protocol protocol) {
        this.timestamp = timeStamp;
        this.temperature = temperature;
        this.mediaTemperature = mediaTemperature;
        this.pressure = pressure;
        this.protocol = protocol;
    }


    @Override
    public String toString() {
        String test = "test";
        return test;

    }


    public String getFormatedTimeStampLong() {

        return new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(timestamp);


    }

    public float getPressureKPa() {
        return pressure * 100;
    }

    public String getFormatedTimeStampShort() {

        return new SimpleDateFormat("kk:mm:ss").format(timestamp);

    }

    public String getTimeStampWithMin() {
        if (timeStampWithMin != null)
            return timeStampWithMin;
        return timeStampWithMin = String.format("%.2f", (getTimestamp().getTime() -
                getProtocol().getStartTime().getTime()) / 60000f);

    }
}




