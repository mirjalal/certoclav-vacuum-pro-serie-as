package com.certoclav.library.certocloud;



public class CloudUser {
	
	
	
	static CloudUser instance = null;
	
	static public CloudUser getInstance(){
		if(instance == null){
			instance = new CloudUser();
		}
		return instance;
		
	}


	private boolean isLoggedIn = false;
	private String token = "";
	private String email = "";
	private boolean isPremiumAccount = false;
	private String currentDeviceKey = "";

	public boolean isLoggedIn() {
		return isLoggedIn;
	}
	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public boolean isPremiumAccount() {
		return isPremiumAccount;
	}
	public void setPremiumAccount(boolean isPremiumAccount) {
		this.isPremiumAccount = isPremiumAccount;
	}
	public String getCurrentDeviceKey() {
		return currentDeviceKey;
	}
	public void setCurrentDeviceKey(String currentDeviceKey) {
		this.currentDeviceKey = currentDeviceKey;
	}
	
	
	

}
