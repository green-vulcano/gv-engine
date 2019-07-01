package it.greenvulcano.gvesb.iam.exception;

public class UnverifiableUserException extends GVSecurityException {
	private static final long serialVersionUID = 1L;

	public UnverifiableUserException(String username) {
		super("Authentication for '"+username+"'"+" cannot be verified");		
	}
	
	public UnverifiableUserException(String username, Exception cause) {
            super("Authentication for '"+username+"'"+" cannot be verified", cause);
            
    }

}
