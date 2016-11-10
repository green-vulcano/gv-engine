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
package it.greenvulcano.gvesb.api.controller;

import java.io.File;
import java.util.Objects;
import java.util.zip.ZipInputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.GVConfigurationManager;

public class GvConfigurationControllerRest {
	 private final static Logger LOG = LoggerFactory.getLogger(GvConfigurationControllerRest.class);	
	
	 private GVConfigurationManager gvConfigurationManager;
	 
	 public void setConfigurationManager(GVConfigurationManager gvConfigurationManager) {
		this.gvConfigurationManager = gvConfigurationManager;
	 }
	 
	 @POST
	 @Path("/deploy/{configId}")
	 @Consumes(MediaType.MULTIPART_FORM_DATA)
	 public void deploy(@PathParam("configId") String id,
			            @Multipart(value="gvconfiguration", type="application/zip") Attachment config) {
		 
		 LOG.debug("Deploying configuration with id "+id);
		 
		 File currentConfig = new File(XMLConfig.getBaseConfigPath());			        
		 String baseDir = currentConfig.getParent();
		 
		 try {
			 			 
			 XMLConfig.setBaseConfigPath(baseDir + File.separator + Objects.requireNonNull(id, "cofiguration id required"));
			 
			 ZipInputStream compressedConfig = new ZipInputStream(config.getDataHandler().getInputStream());
			 gvConfigurationManager.deployConfiguration(compressedConfig);
			 gvConfigurationManager.reload();			 
		 } catch (Exception e) {
			LOG.error("Deploy failed, rollback to previous configuration",e); 
			XMLConfig.setBaseConfigPath(currentConfig.toString());
			
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build());
			
		 }		 
		 
	 }
	

}