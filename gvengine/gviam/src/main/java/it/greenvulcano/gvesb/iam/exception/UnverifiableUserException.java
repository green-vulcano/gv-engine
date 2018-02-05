package it.greenvulcano.gvesb.iam.exception;

public class UnverifiableUserException extends GVSecurityException {
	private static final long serialVersionUID = 1L;

	public UnverifiableUserException(String username) {
		super("User '"+username+"'"+" cannot be verified");		
	}

}
