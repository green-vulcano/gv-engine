package it.greenvulcano.gvesb.console.api.utility;


import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;


@XmlRootElement(name="errorManagement")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.PROPERTY, property = "@class")
public class ErrorManagement implements Serializable {

	private static final long serialVersionUID = 138906093119631510L;
	
	private String errorCode;
	private String errorDescription;
	
	
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
	
	
	@Override
	public String toString() {
		return "ErrorManagement [errorCode=" + errorCode
				+ ", errorDescription=" + errorDescription + "]";
	}

}
