package com.certoclav.library.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by musaq on 11/10/2017.
 */

public class DeviceModel {
    private String serial;
    private String model;
    @SerializedName("devicekey")
    private String deviceKey;

    public DeviceModel() {
    }

    public DeviceModel(String serial, String model, String deviceKey) {
        this.serial = serial;
        this.model = model;
        this.deviceKey = deviceKey;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }
}
