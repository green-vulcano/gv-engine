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
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.zip.ZipInputStream;

import javax.annotation.security.RolesAllowed;
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
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.GVConfigurationManager;
import it.greenvulcano.gvesb.GVConfigurationManager.Authority;


@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true, exposeHeaders={"Content-type", "Content-Range", "X-Auth-Status"})
public class GvConfigurationControllerRest {
	 private final static Logger LOG = LoggerFactory.getLogger(GvConfigurationControllerRest.class);	
	
	 private GVConfigurationManager gvConfigurationManager;
	 
	 public void setConfigurationManager(GVConfigurationManager gvConfigurationManager) {
		this.gvConfigurationManager = gvConfigurationManager;
	 }
	 
	
	 @GET
	 @Path("/deploy")
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER, Authority.GUEST})
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
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
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
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public void deploy(@PathParam("configId") String id,
			            @Multipart(value="gvconfiguration") Attachment config) {
		 
		 switch(config.getHeader("Content-Type")) {
		 
		 case "application/zip":
		 case "application/x-zip-compressed":
		 
		 LOG.debug("Deploying configuration with id "+id);
		 
		 File currentConfig = new File(XMLConfig.getBaseConfigPath());			        
		 String baseDir = currentConfig.getParent();
		 
		 try {
						 
			 ZipInputStream compressedConfig = new ZipInputStream(config.getDataHandler().getInputStream());
			 gvConfigurationManager.deployConfiguration(compressedConfig, Paths.get(baseDir, id));
			 gvConfigurationManager.reload();			 
			 
		 } catch (IllegalStateException e) {
			 LOG.error("Deploy failed, a deploy is already in progress",e);
			 throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build());
		 
		 } catch (Exception e) {
			LOG.error("Deploy failed, something bad appened",e); 						
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build());
			
		 }
		 
		 	break;
		 
		 default:
			 throw new WebApplicationException(Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build());
			 
		 }
		 
		 
	 }
	 
	 @GET
	 @Path("/configuration")
	 @Produces(MediaType.APPLICATION_JSON)
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public Response getConfigurationFileList() {		 	 
		 JSONArray files = new JSONArray(XMLConfig.getLoadedFiles());	
		 
		 return Response.ok(files.toString()).build();
	 }
	 
	 @GET
	 @Path("/configuration/{name}")
	 @Produces(MediaType.APPLICATION_XML)
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public Document getConfigurationFile(@PathParam("name") String name){
		
		Document document = null;
		 
		try {
			 document = XMLConfig.getDocument(name);
		} catch (XMLConfigException e) {
			if (e.getCause() instanceof FileNotFoundException) {
				throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("<error><![CDATA[File not found: "+name+"]]></error>").build());
			}
			
			LOG.error("File to retrieve configuration file "+name,e);			
			throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("<error><![CDATA["+e.getMessage()+"]]></error>").build());
		}
		 
		if (Objects.isNull(document)) throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("<error><![CDATA[File not found: "+name+"]]></error>").build());
		
		return document;
		 
	 }
	

}