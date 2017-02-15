package it.greenvulcano.gvesb.console.api.utility;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
     

@XmlRootElement
public class BaseResponse implements Serializable
{
	private static final long serialVersionUID = 3481727766446104008L;

	public static final int SUCCESS 	= 0;
	public static final int FAIL 		= 1;
	public static final int PARTIALFAIL = 2;
	private String transactionId;
	private int resultCode;
	
	private ErrorManagement errorManagement;

	public BaseResponse() {
		this.resultCode = 0;
		this.errorManagement = new ErrorManagement();
	}

	public BaseResponse(String transactionId) {
		this.transactionId = transactionId;
		this.resultCode = 0;

		this.errorManagement = new ErrorManagement();
	}

	public String getTransactionId() {
		return this.transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public int getResultCode() {
		return this.resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public ErrorManagement getErrorManagement() {
		return this.errorManagement;
	}

	public void setErrorManagement(ErrorManagement errorManagement) {
		this.errorManagement = errorManagement;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BaseResponse [  transactionId = ");
		builder.append(this.transactionId);
		builder.append("   resultCode = ");
		builder.append(this.resultCode);
		builder.append("   errorManagement = ");
		builder.append(this.errorManagement);
		builder.append("  ]");
		return builder.toString();
	}
}