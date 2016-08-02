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
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.greenvulcano.gvesb.GVConfigurationManager;

@Command(scope = "gvesb", name = "reload", description = "Reload all the configuration files")
@Service
public class GVReloader implements Action {

	private final Logger LOG = LoggerFactory.getLogger(getClass());
		
	@Reference
	private GVConfigurationManager configurationManager;
			
	public void setConfigurationManager(GVConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}

	@Override
	public Object execute() throws Exception {
		String message = null;		
		
		try {		
			
			configurationManager.reload();			
			message = "Configuration reloaded";
			
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
			LOG.error("GVReloader - Reload failed", exception);
			message = "Fail to reload configuration";
		}		
		
		return message;
	}

}
