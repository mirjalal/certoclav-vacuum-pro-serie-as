package com.certoclav.app.model;

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
        Double value = (double) currentValue;
        return Integer.toString(value.intValue());
    }


}
