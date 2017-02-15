package it.greenvulcano.gvesb.console.api.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ServiceInstanceDTO implements Serializable {
	
	private static final long serialVersionUID = -2250824902915621350L;

	private String id;
	private String serviceInstanceId;
	private String serviceName;
	private String operationName;
	private String suboperationName;
	private String system;
	private String subsystem;
	private String threadName;
	private Date startDate;
	private Date endDate;
	private String inputObjectType;
	//	private Object inputObject;
	private List<String> properties;
	private String outputObjectType;
	//	private Object outputObject;
	private List<String> outputProperties;



	public ServiceInstanceDTO() {
		super();
	}

	public ServiceInstanceDTO(String id, String serviceInstanceId,
			String serviceName, String operationName, String suboperationName,
			String system, String subsystem, String threadName, Date startDate,
			Date endDate, String inputObjectType, List<String> properties,
			String outputObjectType, List<String> outputProperties) {
		super();
		this.id = id;
		this.serviceInstanceId = serviceInstanceId;
		this.serviceName = serviceName;
		this.operationName = operationName;
		this.suboperationName = suboperationName;
		this.system = system;
		this.subsystem = subsystem;
		this.threadName = threadName;
		this.startDate = startDate;
		this.endDate = endDate;
		this.inputObjectType = inputObjectType;
		this.properties = properties;
		this.outputObjectType = outputObjectType;
		this.outputProperties = outputProperties;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public String getSuboperationName() {
		return suboperationName;
	}

	public void setSuboperationName(String suboperationName) {
		this.suboperationName = suboperationName;
	}

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public String getSubsystem() {
		return subsystem;
	}

	public void setSubsystem(String subsystem) {
		this.subsystem = subsystem;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getInputObjectType() {
		return inputObjectType;
	}

	public void setInputObjectType(String inputObjectType) {
		this.inputObjectType = inputObjectType;
	}

	public List<String> getProperties() {
		return properties;
	}

	public void setProperties(List<String> properties) {
		this.properties = properties;
	}

	public String getOutputObjectType() {
		return outputObjectType;
	}

	public void setOutputObjectType(String outputObjectType) {
		this.outputObjectType = outputObjectType;
	}

	public List<String> getOutputProperties() {
		return outputProperties;
	}

	public void setOutputProperties(List<String> outputProperties) {
		this.outputProperties = outputProperties;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServiceInstanceDTO [id=");
		builder.append(id);
		builder.append(", serviceInstanceId=");
		builder.append(serviceInstanceId);
		builder.append(", serviceName=");
		builder.append(serviceName);
		builder.append(", operationName=");
		builder.append(operationName);
		builder.append(", suboperationName=");
		builder.append(suboperationName);
		builder.append(", system=");
		builder.append(system);
		builder.append(", subsystem=");
		builder.append(subsystem);
		builder.append(", threadName=");
		builder.append(threadName);
		builder.append(", startDate=");
		builder.append(startDate);
		builder.append(", endDate=");
		builder.append(endDate);
		builder.append(", inputObjectType=");
		builder.append(inputObjectType);
		builder.append(", properties=");
		builder.append(properties);
		builder.append(", outputObjectType=");
		builder.append(outputObjectType);
		builder.append(", outputProperties=");
		builder.append(outputProperties);
		builder.append("]");
		return builder.toString();
	}
}
