package it.greenvulcano.gvesb.gviamx.service;

import it.greenvulcano.gvesb.iam.domain.Credentials;
import it.greenvulcano.gvesb.iam.exception.GVSecurityException;

public interface ExternalCredentialsManager {
	
	String getID();
	
	Credentials create(String externalCredentials) throws GVSecurityException;

}
