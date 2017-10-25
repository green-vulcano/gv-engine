package it.greenvulcano.gvesb.properties.model;

import java.util.Properties;
import java.util.Set;


public class GVProperties {
	
	private Properties properties;
	
	private String content;
	
	public GVProperties(Properties properties, String content) {
		
		this.properties = properties;
		this.content = content;
		
	}
	
	public Set<Object> getKeys() {
		return properties.keySet();		
	}
	
	public String getValue(String key) {
		return properties.getProperty(key);
	}
	
	public String getContent() {
		return content;
	}

}
