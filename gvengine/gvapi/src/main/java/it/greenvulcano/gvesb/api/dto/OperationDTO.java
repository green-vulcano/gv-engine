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
package it.greenvulcano.gvesb.api.dto;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.core.jmx.OperationInfo;
import it.greenvulcano.gvesb.core.jmx.ServiceOperationInfo;

public class OperationDTO {

	private final String name;
	private final boolean enabled;
	
	private final long successes;
	private final long failures;
	
	public OperationDTO(String name, boolean enabled) {	
		this.name = Objects.requireNonNull(name);
		this.enabled = enabled;
		this.successes = 0;
		this.failures = 0;
		
	}
	
	public OperationDTO(String name, boolean enabled, long successes, long failures) {	
		this.name = Objects.requireNonNull(name);
		this.enabled = enabled;
		this.successes = successes;
		this.failures = failures;
		
	}

	public String getName() {
		return name;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public long getSuccesses() {
		return successes;
	}
	
	public long getFailures() {
		return failures;
	}
	
	public static Optional<OperationDTO> buildOperationFromConfig(Node config, Optional<ServiceOperationInfo> serviceOperationInfo) {
		try {
			
			String operationName = XMLConfig.get(config, "@forward-name", XMLConfig.get(config, "@name") );
			
			OperationDTO operation = null;			
			
			try {
				OperationInfo operationInfo = serviceOperationInfo.get().getOperationInfo(operationName, false) ;
				operation = new OperationDTO(operationInfo.getOperation(), operationInfo.getOperationActivation(), operationInfo.getTotalSuccess(), operationInfo.getTotalFailure());
								
			} catch (Exception e) {
				operation = new OperationDTO( operationName,  XMLConfig.get(config, "@operation-activation").equals("on")) ;
			}											
			
			return Optional.of(operation);
			
		} catch (NullPointerException|XMLConfigException xmlConfigException){
			LoggerFactory.getLogger(OperationDTO.class).error("Error reading operation configuration", xmlConfigException);
		}
		
		return Optional.empty();
		
	}
	
}
