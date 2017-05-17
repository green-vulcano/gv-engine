package it.greenvulcano.gvesb.gviamx.domain;

import org.json.JSONObject;

public abstract class UserActionRequest {
	
	
	public abstract Long getId();
	
	public abstract String getFullname();
	
	public abstract String getEmail();
	
	public abstract JSONObject getRequestObject();
		
}
