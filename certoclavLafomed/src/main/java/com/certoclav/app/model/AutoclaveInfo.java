package com.certoclav.app.model;


public class AutoclaveInfo {

	private int cycleNumber;
	private String serialNumber;



public int getCycleNumber() {
		return cycleNumber;
	}



	public void setCycleNumber(int cycleNumber) {
		this.cycleNumber = cycleNumber;
	}



	public String getSerialNumber() {
		return serialNumber;
	}



	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}



public AutoclaveInfo(){
 cycleNumber = 0;
 serialNumber = "";
}





}
