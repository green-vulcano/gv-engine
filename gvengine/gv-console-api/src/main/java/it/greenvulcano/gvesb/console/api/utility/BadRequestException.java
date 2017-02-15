package it.greenvulcano.gvesb.console.api.utility;


public class BadRequestException extends Exception {

	private static final long serialVersionUID = -3163532485761714021L;

	private String errorCode;
	private String errorDescription;
	private String transactionId;
	private String resultCode;
		
	
	public BadRequestException() {
		super();
	} 
	
	public BadRequestException(String errorCode, String errorDescription) {

		super(Constants.EXCEPTION_MSG_CODE + errorCode + Constants.EXCEPTION_MSG_DESC + errorDescription + "]");
		this.errorCode = errorCode;
		this.errorDescription = errorDescription;
	}

	public BadRequestException(String errorCode, String errorDescription, String transactionId, String resultCode) {

		super(Constants.EXCEPTION_MSG_CODE + errorCode + Constants.EXCEPTION_MSG_DESC + errorDescription + "]");
		this.errorCode = errorCode;
		this.errorDescription = errorDescription;
		this.transactionId = transactionId;
		this.resultCode = resultCode;
	}
	
	public BadRequestException(String errorCode, String errorDescription, Throwable cause) {

		super(Constants.EXCEPTION_MSG_CODE + errorCode + Constants.EXCEPTION_MSG_DESC + errorDescription + "]", cause);
		this.errorCode = errorCode;
		this.errorDescription = errorDescription;
	}


	public BadRequestException(String errorCode, String errorDescription, String transactionId, Throwable cause) {

		super(Constants.EXCEPTION_MSG_CODE + errorCode + Constants.EXCEPTION_MSG_DESC + errorDescription + "]", cause);
		this.errorCode = errorCode;
		this.errorDescription = errorDescription;
		this.transactionId = transactionId;
	}
	
	
	public BadRequestException(String errorCode, String errorDescription, String transactionId, String resultCode, Throwable cause) {

		super(Constants.EXCEPTION_MSG_CODE + errorCode + Constants.EXCEPTION_MSG_DESC + errorDescription + "]", cause);
		this.errorCode = errorCode;
		this.errorDescription = errorDescription;
		this.transactionId = transactionId;
		this.resultCode = resultCode;
	}
	
	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
	
}
