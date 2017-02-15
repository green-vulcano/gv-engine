package it.greenvulcano.gvesb.console.api.dto;

import java.io.Serializable;

public class ServiceStatsTimeDTO implements Serializable {

	private static final long serialVersionUID = 8066876446983655026L;

	private String serviceName;
	private Long tMin;
	private Long tMax;
	private Long tAvg;
	
	
	public ServiceStatsTimeDTO() {
		super();
	}
	
	public ServiceStatsTimeDTO(String serviceName, Long tMin, Long tMax,
			Long tAvg) {
		super();
		this.serviceName = serviceName;
		this.tMin = tMin;
		this.tMax = tMax;
		this.tAvg = tAvg;
	}
	
	public String getServiceName() {
		return serviceName;
	}
	
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public Long gettMin() {
		return tMin;
	}
	
	public void settMin(Long tMin) {
		this.tMin = tMin;
	}
	
	public Long gettMax() {
		return tMax;
	}
	
	public void settMax(Long tMax) {
		this.tMax = tMax;
	}
	
	public Long gettAvg() {
		return tAvg;
	}
	
	public void settAvg(Long tAvg) {
		this.tAvg = tAvg;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServiceStatsTimeDTO [serviceName=");
		builder.append(serviceName);
		builder.append(", tMin=");
		builder.append(tMin);
		builder.append(", tMax=");
		builder.append(tMax);
		builder.append(", tAvg=");
		builder.append(tAvg);
		builder.append("]");
		return builder.toString();
	}
}
