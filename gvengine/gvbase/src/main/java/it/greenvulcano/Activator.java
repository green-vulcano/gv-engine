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
package it.greenvulcano;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.configuration.XMLConfig;

public class Activator implements BundleActivator {

	private final static Logger LOG = LoggerFactory.getLogger(Activator.class);
		
	@Override
	public void start(BundleContext context) throws Exception {
		LOG.debug("****** GVBase started");
		
		ServiceReference<?> configurationAdminReference = context.getServiceReference(ConfigurationAdmin.class.getName());
        ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) context.getService(configurationAdminReference);        
        
        Configuration gvcfg = configurationAdmin.getConfiguration("it.greenvulcano.gvesb.bus");
        
        String userConfiguration = XMLConfig.DEFAULT_FOLDER;
        
        if (Objects.nonNull(gvcfg)){
        
        	userConfiguration = (String) Optional.ofNullable(gvcfg.getProperties().get("gvbus.apikey"))
        											 .filter(Objects::nonNull)
        											 .map(c->c.toString().trim())
        											 .filter(c-> c.length()>0)
        											 .filter(c-> !c.equalsIgnoreCase("undefined"))
        											 .orElse(XMLConfig.DEFAULT_FOLDER);
        }
        
		String configurationPath = System.getProperty("gv.app.home") + File.separator + userConfiguration;
		try {
			
			File configDir = new File(configurationPath);
			
			if(configDir.exists() && configDir.isDirectory()){
				XMLConfig.setBaseConfigPath(configurationPath);
				LOG.debug("Configuration path set to " + configurationPath);
			}			
		
		} catch (Exception exception) {
			LOG.error("Fail to set configuration path " + configurationPath);
		}
		
	}

	@Override
	public void stop(BundleContext context) throws Exception {	
		LOG.debug("****** GVBase stopped");
	}

}
