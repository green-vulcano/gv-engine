package it.greenvulcano.gvesb.api.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Service {

	private final String idService, groupName;
    private final boolean statisticsEnabled, enabled;
	private final List<Operation> operations;
	
    public Service(String idService, String groupName, boolean enabled, boolean statisticsEnabled) {
		this.idService = Objects.requireNonNull(idService);
		this.groupName = Objects.requireNonNull(groupName);
		this.statisticsEnabled = statisticsEnabled;
		this.enabled = enabled;
		this.operations = new LinkedList<>();
	}    
	    
    
	public String getIdService() {
		return idService;
	}

	public String getGroupName() {
		return groupName;
	}

	public boolean isStatisticsEnabled() {
		return statisticsEnabled;
	}

	public boolean isEnabled() {
		return enabled;
	}
		
	public List<Operation> getOperations() {
		return operations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result + ((idService == null) ? 0 : idService.hashCode());
		result = prime * result + (statisticsEnabled ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Service other = (Service) obj;
		if (enabled != other.enabled)
			return false;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		if (idService == null) {
			if (other.idService != null)
				return false;
		} else if (!idService.equals(other.idService))
			return false;
		if (statisticsEnabled != other.statisticsEnabled)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Service [idService=" + idService + ", groupName=" + groupName + ", statisticsEnabled="
				+ statisticsEnabled + ", enabled=" + enabled + "]";
	}    

}
