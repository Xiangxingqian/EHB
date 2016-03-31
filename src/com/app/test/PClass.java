package com.app.test;

public class PClass {
	
	String str = "Qian";
	private void setStr(String string){
		this.str = string;
	}
	private PClass() {
	}
	
	public String getString(){
		return str;
	}
}
