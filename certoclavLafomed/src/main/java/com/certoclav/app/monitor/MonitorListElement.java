package com.certoclav.app.monitor;


public class MonitorListElement  {

	public MonitorListElement(boolean isSelected, String text) {
		
		this.isSelected = isSelected;
		this.text = text;
	}
	private boolean isSelected;
	private String text;
	
	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}


}

