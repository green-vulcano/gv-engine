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
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.Objects;
import java.util.zip.ZipInputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.GVConfigurationManager;

public class GvConfigurationControllerRest {
	 private final static Logger LOG = LoggerFactory.getLogger(GvConfigurationControllerRest.class);	
	
	 private GVConfigurationManager gvConfigurationManager;
	 
	 public void setConfigurationManager(GVConfigurationManager gvConfigurationManager) {
		this.gvConfigurationManager = gvConfigurationManager;
	 }
	 
	
	 @GET
	 @Path("/deploy")
	 public Response getConfigurationInfo(){
		 File currentConfig = new File(XMLConfig.getBaseConfigPath());
		 
		 if (currentConfig.exists()) {
		 
			 JSONObject configInfo = new JSONObject();
			 configInfo.put("id", currentConfig.getName());
			 configInfo.put("path", currentConfig.getParent());
			 configInfo.put("time", currentConfig.lastModified());
			 
			 return Response.ok(configInfo.toString()).build();
		 } else {
			 return Response.status(Status.NOT_FOUND).build();
		 }
		 
	 }
	 
	 @GET
	 @Path("/deploy/{configId}")
	 @Produces(MediaType.APPLICATION_OCTET_STREAM)
	 public Response exportConfiguration(@PathParam("configId") String id) {
		 File currentConfig = new File(XMLConfig.getBaseConfigPath());
		 
		 if (currentConfig.exists() && currentConfig.getName().equals(id) ){
			 try {
				 
				return Response.ok(gvConfigurationManager.exportConfiguration())
							   .header("Content-Disposition", "attachment; filename="+id+".zip")
							   .build();
				 
			 } catch (XMLConfigException e) {
				 LOG.error("Export failed",e); 
				 throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
			 }				 
		 } else {
			 return Response.status(Status.NOT_FOUND).build();
		 }		 
		 
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
			 
		 } catch (IllegalStateException e) {
			 LOG.error("Deploy failed, a deploy is already in progress",e);
			 throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build());
		 
		 } catch (Exception e) {
			LOG.error("Deploy failed, rollback to previous configuration",e); 
			XMLConfig.setBaseConfigPath(currentConfig.toString());
			
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build());
			
		 }
		 
		 try {
			 Files.walk(currentConfig.toPath(), FileVisitOption.FOLLOW_LINKS)
			 .sorted(Comparator.reverseOrder())
			 .map(java.nio.file.Path::toFile)
			 .forEach(File::delete);
		 } catch (IOException e) {
			 LOG.error("Failed to delete old configuration",e); 
		 }
		 
	 }
	

}