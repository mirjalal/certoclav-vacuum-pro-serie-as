package com.certoclav.library.bluetooth.chooser;


public class BluetoothDeviceElement {
	
private String name;
private String address;
private int rssi;
private int status;



public int getStatus() {
	return status;
}

public void setStatus(int status) {
	this.status = status;
}

public BluetoothDeviceElement(String address, String name, Integer rssi, int status) {
	this.name = name;
	this.address = address;
	this.rssi = rssi;
	this.status = status;
}

public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}

public String getAddress() {
	return address;
}

public void setAddress(String address) {
	this.address = address;
}

public int getRssi() {
	return rssi;
}

public void setRssi(int rssi) {
	this.rssi = rssi;
}





}
