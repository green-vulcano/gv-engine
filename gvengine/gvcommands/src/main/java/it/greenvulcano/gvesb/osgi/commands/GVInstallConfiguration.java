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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.GVConfigurationManager;

@Command(scope = "gvesb", name = "deploy", description = "Deploy a configuration zip package")
@Service
public class GVInstallConfiguration implements Action {
	
	@Argument(index=0, name = "id", description = "The id of configuration to deploy", required = true, multiValued = false)
	String id = null;
	
	@Argument(index=1, name = "file", description = "The configuration archive full path  (Example: /home/dir/config.zip) ", required = true, multiValued = false)
	String baseFile = null;
	
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
			
			LOG.debug("Installing configuration with id " + id);
						
			InputStream file = new FileInputStream(baseFile);
			ZipInputStream zipFile = new ZipInputStream(file);
			
			System.out.println("Installing configuration with id " + id + " ...");
			
			configurationManager.install(id, zipFile); 
			configurationManager.reload();
			message = "Installation complete";		
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
			LOG.error("GVDeployConfiguration - Installation configuration failed", exception);
			message = "Installation failed";
		}
		
		return message;
	}

}
