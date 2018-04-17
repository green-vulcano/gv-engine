package it.greenvulcano.gvesb.iam.exception;

public class InvalidCredentialsException extends GVSecurityException {
	private static final long serialVersionUID = -2594597514556029155L;

	public InvalidCredentialsException() {
		super("No match found for supplied credentials");		
	}

}
