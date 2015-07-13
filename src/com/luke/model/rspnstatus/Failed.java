package com.luke.model.rspnstatus;

public class Failed extends ResponseStatus {
	public Failed() {
		super(0, null, null);
	}
	
	public Failed(String info) {
		super(0, info, null);
	}
	
	public Failed(String info, Object data) {
		super(0, info, data);
	}
}
