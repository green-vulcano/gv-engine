package it.greenvulcano.gvesb.properties.service;

import it.greenvulcano.gvesb.properties.model.GVProperties;

public interface ConfProperties {
	
	GVProperties getProperties();
	
	void saveProperties(String content);
	
	String getProperty(String key);

}
