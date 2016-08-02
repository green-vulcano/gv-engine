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
package it.greenvulcano.gvesb.osgi.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import it.greenvulcano.gvesb.GVConfigurationManager;
import it.greenvulcano.gvesb.osgi.repository.GVConfigurationRepository;

@Command(scope = "gvesb", name = "deploy", description = "Deploy a project developed with VulCon.io")
@Service
public class GVDeployer implements Action {

	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Argument(name = "uuid", description = "The UUID of the project to install", required = false, multiValued = false)
	private String uuid;
	
	@Reference
	private GVConfigurationManager configurationManager;
	
	@Reference
	private GVConfigurationRepository configurationRepository;
		
	public void setConfigurationManager(GVConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}

	public void setConfigurationRepository(GVConfigurationRepository configurationRepository) {
		this.configurationRepository = configurationRepository;
	}
		
	@Override
	public Object execute() throws Exception {
		String message = null;
		if (uuid==null || uuid.trim().equals("")) {
			message = "An UUID is required to retrieve configuration";
		} else {
		
			try {
			
				Document config = configurationRepository.retrieveConfiguration(uuid);			
				configurationManager.updateConfiguration(config);
				
				message = "Configuration deployed";
				
			} catch (Exception exception) {
				System.err.println(exception.getMessage());
				LOG.error("GVDeployer - Deployment failed", exception);
				message = "Deployment failed";
			}		
		}
		return message;
	}

}
