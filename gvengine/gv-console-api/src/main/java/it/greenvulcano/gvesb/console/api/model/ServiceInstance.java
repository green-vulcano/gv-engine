package it.greenvulcano.gvesb.console.api.model;

import java.util.Date;
import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("service_instance")
public class ServiceInstance {

	@Id private String id;

	//@Field("serviceInstanceId")
	private String serviceInstanceId;
	
	//@Field("serviceName")
	private String serviceName;
	
	//@Field("operationName")
	private String operationName;
	
	//@Field("suboperationName")
	private String suboperationName;
	
	//@Field("system")
	private String system;
	
	//@Field("subsystem")
	private String subsystem;
	
	private String threadName;
		
	private Date startDate;
	
	private Date endDate;
	
	//@Field("inputObjectType")
	private String inputObjectType;
	
	//@Serialized
	//private Object inputObject;
	private String inputObject;
	
	private Long inputObjectSize;
		
	private List<String> properties;
	
	private String outputObjectType;
	
//	private Object outputObject;
	
	private List<String> outputProperties;
	
	
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
//	public Object getInputObject() {
//		return inputObject;
//	}
//	public void setInputObject(Object inputObject) {
//		this.inputObject = inputObject;
//	}
		
	public String getInputObject() {
		return inputObject;
	}
	public void setInputObject(String inputObject) {
		this.inputObject = inputObject;
	}
		
	public Long getInputObjectSize() {
		return inputObjectSize;
	}
	public void setInputObjectSize(Long inputObjectSize) {
		this.inputObjectSize = inputObjectSize;
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
	
//	public Object getOutputObject() {
//		return outputObject;
//	}
//	public void setOutputObject(Object outputObject) {
//		this.outputObject = outputObject;
//	}
	public List<String> getOutputProperties() {
		return outputProperties;
	}
	public void setOutputProperties(List<String> outputProperties) {
		this.outputProperties = outputProperties;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServiceInstance [id=");
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
		builder.append(", inputObjectSize=");
		builder.append(inputObjectSize);
//		builder.append(", inputObject=");
//		builder.append(inputObject);
		builder.append(", properties=");
		builder.append(properties);
		builder.append(", outputObjectType=");
		builder.append(outputObjectType);
//		builder.append(", outputObject=");
//		builder.append(outputObject);
		builder.append(", outputProperties=");
		builder.append(outputProperties);
		builder.append("]");
		return builder.toString();
	}
	
}
