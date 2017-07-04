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

    public float getCurrentValueInKPa() {
        return currentValue * 100;
    }

    public void setCurrentValue(float currentValue) {
        this.currentValue = currentValue;
    }

    public String getValueIntString() {
        Double value = (double) currentValue;
        return Integer.toString(value.intValue()); //runden auf eine Nachkommastelle
    }

    public String getValueString() {
        return Float.toString((float) (((float) ((int) (currentValue * 100))) / 100.0)); //runden auf eine Nachkommastelle
    }


}
