package it.greenvulcano.gvesb.console.api.dto;

import java.io.Serializable;
import java.util.Date;

public class ServiceStatsThroughput implements Serializable {

	private static final long serialVersionUID = -588199545723389311L;

	private String serviceName;
	private Integer throughput;
	private Date firstStart;
	private Date lastEnd;
	private Date inStartDate;
	private Date inEndDate;
	private Long interval;
	
	
	
	public ServiceStatsThroughput() {
		super();
	}

	public ServiceStatsThroughput(String serviceName, Integer throughput,
			Date firstStart, Date lastEnd, Date inStartDate, Date inEndDate) {
		super();
		this.serviceName = serviceName;
		this.throughput = throughput;
		this.firstStart = firstStart;
		this.lastEnd = lastEnd;
		this.inStartDate = inStartDate;
		this.inEndDate = inEndDate;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Integer getThroughput() {
		return throughput;
	}

	public void setThroughput(Integer throughput) {
		this.throughput = throughput;
	}

	public Date getFirstStart() {
		return firstStart;
	}

	public void setFirstStart(Date firstStart) {
		this.firstStart = firstStart;
	}

	public Date getLastEnd() {
		return lastEnd;
	}

	public void setLastEnd(Date lastEnd) {
		this.lastEnd = lastEnd;
	}

	public Date getInStartDate() {
		return inStartDate;
	}

	public void setInStartDate(Date inStartDate) {
		this.inStartDate = inStartDate;
	}

	public Date getInEndDate() {
		return inEndDate;
	}

	public void setInEndDate(Date inEndDate) {
		this.inEndDate = inEndDate;
	}
	
	public Long getInterval() {
		return interval;
	}

	public void setInterval(Long interval) {
		this.interval = interval;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServiceStatsThroughput [serviceName=");
		builder.append(serviceName);
		builder.append(", throughput=");
		builder.append(throughput);
		builder.append(", firstStart=");
		builder.append(firstStart);
		builder.append(", lastEnd=");
		builder.append(lastEnd);
		builder.append(", inStartDate=");
		builder.append(inStartDate);
		builder.append(", inEndDate=");
		builder.append(inEndDate);
		builder.append(", interval=");
		builder.append(interval);
		builder.append("]");
		return builder.toString();
	}
	
}
