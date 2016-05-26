package it.greenvulcano.gvesb.osgi.repository;

import org.w3c.dom.Document;

import it.greenvulcano.gvesb.osgi.repository.exception.GVResourceException;

public interface GVConfigurationRepository {
	
	Document retrieveConfiguration(String uuid) throws GVResourceException;

}
