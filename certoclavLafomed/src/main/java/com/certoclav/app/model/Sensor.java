package com.certoclav.app.model;

import java.util.Locale;

public class Sensor {

    private float currentValue = 0;
    private Double offset = 0d;


    public Sensor() {
        super();
    }

    public Double getOffset() {
        return offset;
    }

    public void setOffset(Double offset) {
        this.offset = offset;
    }

    public float getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(float currentValue) {
        this.currentValue = currentValue;
    }


    public String getValueString() {
        return String.format(Locale.US,"%.2f", currentValue);
    }


}
