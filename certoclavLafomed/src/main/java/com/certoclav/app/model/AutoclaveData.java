package com.certoclav.app.model;


public class AutoclaveData {

    AutoclaveData() {
        temp1 = new Sensor();
        temp2 = new Sensor();
        temp3 = new Sensor();
        temp4 = new Sensor();
        press = new Sensor();
    }

    //digitalData
    private int mCycleCounter = 0;
    private boolean programFinishedSucessfully = false;
    private boolean programRunning = false;
    private boolean doorClosed = false;
    private boolean doorLocked = false;
    private boolean waterLvlLow = false;
    private boolean waterLvlFull = false;

    private boolean failStoppedByUser = false;


    public boolean isFailStoppedByUser() {
        return failStoppedByUser;
    }

    public void setFailStoppedByUser(boolean failStoppedByUser) {
        this.failStoppedByUser = failStoppedByUser;
    }


    //temperature & pressure Data
    private Sensor temp1;
    private Sensor temp2;
    private Sensor temp3;
    private Sensor temp4;

    public Sensor getTemp4() {
        return temp4;
    }

    public void setTemp4(Sensor temp4) {
        this.temp4 = temp4;
    }


    private Sensor press;

    public int getmCycleCounter() {
        return mCycleCounter;
    }

    public void setmCycleCounter(int mCycleCounter) {
        this.mCycleCounter = mCycleCounter;
    }

    public boolean isProgramFinishedSucessfully() {
        return programFinishedSucessfully;
    }

    public void setProgramFinishedSucessfully(boolean programFinishedSucessfully) {
        this.programFinishedSucessfully = programFinishedSucessfully;
    }

    public boolean isProgramRunning() {
        return programRunning;
    }

    public void setProgramRunning(boolean programRunning) {
        this.programRunning = programRunning;
    }

    //if the internal locking mechanism is active, this variable is true. In this case the user is not able to open the door.
    public boolean isDoorClosed() {
        return doorClosed;
    }

    public void setDoorClosed(boolean doorClosed) {
        this.doorClosed = doorClosed;
    }

    //If the the door of the autoclave is open, this variable is true
    public boolean isDoorLocked() {
        return doorLocked;
    }

    public void setDoorLocked(boolean doorLocked) {
        this.doorLocked = doorLocked;
    }

    public boolean isWaterLvlLow() {
        return waterLvlLow;
    }

    public void setWaterLvlLow(boolean waterLvlLow) {
        this.waterLvlLow = waterLvlLow;
    }

    public boolean isWaterLvlFull() {
        return waterLvlFull;
    }

    public void setWaterLvlFull(boolean waterLvlFull) {
        this.waterLvlFull = waterLvlFull;
    }

    public Sensor getTemp2() {
        return temp2;
    }

    public void setTemp2(Sensor temp2) {
        this.temp2 = temp2;
    }

    public Sensor getTemp3() {
        return temp3;
    }

    public void setTemp3(Sensor temp3) {
        this.temp3 = temp3;
    }

    public Sensor getTemp1() {
        return temp1;
    }

    public void setTemp1(Sensor temp1) {
        this.temp1 = temp1;
    }

    public Sensor getPress() {
        return press;
    }

    public void setPress(Sensor press) {
        this.press = press;
    }

}
