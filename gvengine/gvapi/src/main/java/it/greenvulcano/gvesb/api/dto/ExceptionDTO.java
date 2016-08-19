package it.greenvulcano.gvesb.api.dto;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ExceptionDTO {
	
	@JsonIgnore
	private final Exception exception;
		
	@JsonIgnore
	public ExceptionDTO(Exception exception) {		
		this.exception = exception;
	}

	public String getType() {
		return exception.getClass().getName();
	}
	
	public String getMessage() {
		return exception.getMessage();
	}
	
	@JsonIgnore
	public String getStackTrace() {
		StringWriter stacktrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(stacktrace));
		return stacktrace.toString();
	}

	@Override
	public String toString() {		
		return "{\"type\":\"" + getType() +"\""
				+ ",\"message\":"+ (getMessage()!=null ? "\""+getMessage() +"\"" : getMessage())
				//+ ",\"stackTrace\":" + "\""+getStackTrace() +"\""
				+"}";
	}
	
}
