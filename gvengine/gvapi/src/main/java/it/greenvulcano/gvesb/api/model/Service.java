/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.api.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Service {

	private final String idService, groupName;
    private final boolean statisticsEnabled, enabled;
	private final Map<String, Operation> operations;
	
    public Service(String idService, String groupName, boolean enabled, boolean statisticsEnabled) {
		this.idService = Objects.requireNonNull(idService);
		this.groupName = Objects.requireNonNull(groupName);
		this.statisticsEnabled = statisticsEnabled;
		this.enabled = enabled;
		this.operations = new LinkedHashMap<>();
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
		
	public Map<String, Operation> getOperations() {
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
