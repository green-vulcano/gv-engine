package it.greenvulcano.gvesb;

import org.w3c.dom.Document;

import it.greenvulcano.configuration.XMLConfigException;

public interface GVConfigurationManager {
	
	void updateConfiguration(Document xmlConfiguration) throws XMLConfigException;
	
	void reload() throws XMLConfigException;

}
