package com.app.test.event;

/**
 * btn.setOnClickListener(listener);
 * 
 * ui = btn;
 * listener = listener;
 * callbackName = onClick;
 * jump = true? onClick contains startActivity statement: false.
 * 
 * */
public class UIEvent {
	
	// ui element
	//there are two type of ui elements: View, Dialog.
	public Object ui;
	
	//listener 
	public Object listener;
	
	//callback
	public String callbackName;
	
	// is activity jumping event
	public boolean jump;
	
	public UIEvent(Object ui, Object listener, String rm, boolean jump) {
		this.ui = ui;
		this.listener = listener;
		this.callbackName = rm;
		this.jump = jump;
	}

	public boolean isJump() {
		return jump;
	}

	public Object getListener() {
		return listener;
	}

	public Object getUi() {
		return ui;
	}
	
	public String getCallback() {
		return callbackName;
	}
}
