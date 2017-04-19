package com.certoclav.app.model;

import java.util.ArrayList;

import android.util.Log;

public class Parameter {

	private int Index;
	public int getIndex() {
		return Index;
	}

	public void setIndex(int index) {
		Index = index;
	}

	private ArrayList<Float> coefficients = new ArrayList<Float>();

public Parameter(){
	coefficients.add((float)0.0);
	coefficients.add((float)0.0);
	coefficients.add((float)0.0);
	coefficients.add((float)0.0);
}



public ArrayList<Float> getCoefficients() {
		return coefficients;
	}

	public void setCoefficients(ArrayList<Float> coefficients) {
		this.coefficients = coefficients;
	}
	public void setCoefficients(float coeff1, float coeff2, float coeff3) {
		coefficients.clear();
		coefficients.add(coeff1);
		coefficients.add(coeff2);
		coefficients.add(coeff3);
	}
	
	public void setCoefficients(float coeff1) {
		coefficients.clear();
		coefficients.add(coeff1);
	}
	
	public void setCoefficients(float coeff1, float coeff2, float coeff3, float coeff4) {
		coefficients.clear();
		coefficients.add(coeff1);
		coefficients.add(coeff2);
		coefficients.add(coeff3);
		coefficients.add(coeff4);
	}

Parameter(int parameterIndex){
     this.Index = parameterIndex;
}





	public String getSendString(){
		StringBuilder sb = new StringBuilder();
		sb.append("PUT_PARA ").append(Index);
	
		for(int i = 0; i< coefficients.size();i++){
			sb.append(",").append(coefficients.get(i));
		}
		sb.append("\n");
		Log.e("Parameter.java", sb.toString());
		return sb.toString();
	}
	

	

	

}
