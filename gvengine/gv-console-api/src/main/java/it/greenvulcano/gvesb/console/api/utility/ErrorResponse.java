package it.greenvulcano.gvesb.console.api.utility;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.google.gson.Gson;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "transactionId", "processExecutionId", "errorCode", "errorDescription" })
@XmlRootElement(name = "ProcessResponse")
public class ErrorResponse implements Serializable {

	private static final long serialVersionUID = -7394360697104822127L;

	@XmlElement(name = "TransactionId")
	private String transactionId;
	@XmlElement(name = "ProcessExecutionId")
	private String processExecutionId;
	@XmlElement(name = "ErrorCode")
	private String errorCode;
	@XmlElement(name = "ErrorDescription")
	private String errorDescription;

	
	/**
	 * @return the transactionId
	 */
	public String getTransactionId() {
		return transactionId;
	}

	/**
	 * @param transactionId
	 *            the transactionId to set
	 */
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	/**
	 * @return the processExecutionId
	 */
	public String getProcessExecutionId() {
		return processExecutionId;
	}

	/**
	 * @param processExecutionId
	 *            the processExecutionId to set
	 */
	public void setProcessExecutionId(String processExecutionId) {
		this.processExecutionId = processExecutionId;
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

	@Override
	public String toString() {
		return "[" + (transactionId != null ? "transactionId=" + transactionId + ", " : "") + (processExecutionId != null ? "processExecutionId=" + processExecutionId + ", " : "") + (errorDescription != null ? "errorDescription=" + errorDescription : "") + "]";
	}

	public String toJSONString() {

		Gson gson = new Gson();
		return gson.toJson(this);

	}

}
