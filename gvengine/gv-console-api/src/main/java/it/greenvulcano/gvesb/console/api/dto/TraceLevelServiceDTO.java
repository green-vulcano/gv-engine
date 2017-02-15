package it.greenvulcano.gvesb.console.api.dto;

import java.io.Serializable;

public class TraceLevelServiceDTO implements Serializable {

	private static final long serialVersionUID = 1341167254150958496L;

	private String id;
	private String serviceName;
	private int enabled;
	private String traceLevel; // INFO/DEBUG
	
	
	public TraceLevelServiceDTO() {
		super();
	}

	public TraceLevelServiceDTO(String id, String serviceName, int enabled,
			String traceLevel) {
		super();
		this.id = id;
		this.serviceName = serviceName;
		this.enabled = enabled;
		this.traceLevel = traceLevel;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public int getEnabled() {
		return enabled;
	}

	public void setEnabled(int enabled) {
		this.enabled = enabled;
	}

	public String getTraceLevel() {
		return traceLevel;
	}

	public void setTraceLevel(String traceLevel) {
		this.traceLevel = traceLevel;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TraceLevelServiceDTO [id=");
		builder.append(id);
		builder.append(", serviceName=");
		builder.append(serviceName);
		builder.append(", enabled=");
		builder.append(enabled);
		builder.append(", traceLevel=");
		builder.append(traceLevel);
		builder.append("]");
		return builder.toString();
	}
}
