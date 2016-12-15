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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.configuration.XMLConfig;

@Command(scope = "gvesb", name = "deploy", description = "For deploy the configuration")
@Service
public class GVDeployConfiguration implements Action {
	
	@Option(name = "-id", aliases = "--id_config", description = "The id of configuration to deploy",
			required = true, multiValued = false)
	String id = null;
	
	@Option(name = "-file", aliases = "--base_file", description = "Insert the path and name for the file (Example: /home/dir/file.txt) ",
			required = true, multiValued = false)
	String baseFile = null;
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Override
	public Object execute() throws Exception {
		String message = null;
		
		try {
			
			
			LOG.debug("Deploying configuration with id " + id);
			
			File currentConfig = new File(XMLConfig.getBaseConfigPath());
			String baseDir = currentConfig.getParent();
			
			InputStream file = new FileInputStream(baseFile);
			ZipInputStream zipFile = new ZipInputStream(file);
			
			Path dest = Paths.get(baseDir, id);
			String destination = dest.toString();
			
			if (Files.notExists(dest)){
				Files.createDirectories(dest);
			}
			
			LOG.debug("Deploy started on path " + destination);
			ZipEntry zipEntry = null;
			
			while ((zipEntry=zipFile.getNextEntry())!=null) {
				
				Path entryPath = Paths.get(destination, zipEntry.getName());
				LOG.debug("Adding resource: "+entryPath);
				if (zipEntry.isDirectory()) {
					Files.createDirectories(entryPath);
				} else {
					
					Files.copy(zipFile, entryPath, StandardCopyOption.REPLACE_EXISTING);					
				}	
			}
			
			LOG.debug("Deploy complete");
			message = "Deploy complete";
		
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
			LOG.error("GVDeployConfiguration - Deploy configuration failed", exception);
			message = "Fail to deploy configuration";
		}
		
		return message;
	}

}
