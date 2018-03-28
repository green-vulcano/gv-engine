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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.fasterxml.jackson.core.JsonProcessingException;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.GVConfigurationManager;
import it.greenvulcano.gvesb.GVConfigurationManager.Authority;
import it.greenvulcano.gvesb.api.dto.ServiceDTO;
import it.greenvulcano.util.xml.XMLUtils;

@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true, exposeHeaders={"Content-Type", "Content-Range", "X-Auth-Status"})
public class GvConfigurationControllerRest extends BaseControllerRest {
	 private final static Logger LOG = LoggerFactory.getLogger(GvConfigurationControllerRest.class);	
	
	 private final DocumentBuilder documentBuilder;
	 private GVConfigurationManager gvConfigurationManager;
	 
	 public GvConfigurationControllerRest() throws ParserConfigurationException {
		 DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		 documentBuilderFactory.setValidating(true);
		 documentBuilderFactory.setFeature("http://xml.org/sax/features/namespaces", false);
		 documentBuilderFactory.setFeature("http://xml.org/sax/features/validation", false);
		 documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		 documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		 documentBuilder = documentBuilderFactory.newDocumentBuilder();
	 }	 
	 
	 public void setConfigurationManager(GVConfigurationManager gvConfigurationManager) {
		this.gvConfigurationManager = gvConfigurationManager;
	 }
	 
	 
	 @GET
	 @Path("/configuration/")
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public Response getConfigurationHistory(){
		
		 JSONArray history = new JSONArray();
		
		 try {		 
		
			 gvConfigurationManager.getHistory().stream().map(f-> {
				 JSONObject configEntry = new JSONObject();
				 configEntry.put("id", f.getName().split("\\.(?=[^\\.]+$)")[0]);
				 configEntry.put("time", f.lastModified());
				 
				 return configEntry; 
			 }).forEach(history::put);
		 } catch (IOException e) {
			 LOG.error("Failed to retrieve configuration history",e);
			 throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
			 
		 }
		 
		 
		 return Response.ok(history.toString()).build();
	 }
	 
	 @POST
	 @Path("/configuration/{configId}")	
	 @Consumes(MediaType.MULTIPART_FORM_DATA)
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public void installConfiguration(@PathParam("configId") String id,
			            @Multipart(value="gvconfiguration") Attachment config) {
		 
		 File currentConfig = new File(XMLConfig.getBaseConfigPath());
		 if (id.equals(currentConfig.getName()) && !id.endsWith("-debug") ) {
			 throw new WebApplicationException(Response.status(Response.Status.CONFLICT).build());
		 }
		 
		 MediaType contentType = Optional.ofNullable(config.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_TYPE);
			 
		 switch(contentType.getSubtype()) {
		 
		 case "zip":
		 case "x-zip-compressed":
		 			 		 
			 try (InputStream inputData = config.getDataHandler().getInputStream()){							 
				 
				 gvConfigurationManager.install(id, IOUtils.toByteArray(inputData));				 
				 
			 } catch (IllegalStateException e) {
				 LOG.error("Failed to install configuraton, operation already in progress",e);
				 throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build());
			 
			 } catch (Exception e) {
				LOG.error("Failed to install configuraton, something bad appened",e); 						
				throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build());
				
			 }
			 
			 break;
		 
		 default:
			 throw new WebApplicationException(Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build());
			 
		 }
		 
		 
	 }	 
	 
	 @DELETE
	 @Path("/configuration/{configId}")	
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public void deleteConfiguration (@PathParam("configId") String id) {
	 	try {
	 		gvConfigurationManager.delete(id);
	 	} catch (Exception e) {
			LOG.error("Failed to delete configuraton, something bad appened",e); 						
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build());
			
		}
	 }
	 
	 @GET
	 @Path("/configuration/{configId}/GVCore.xml")
	 @Produces(MediaType.APPLICATION_XML)
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public Response getArchivedConfig(@PathParam("configId") String id){
		 		 
		 
			try {
				 byte[] gvcore = gvConfigurationManager.extract(id, "GVCore.xml");
				 return Response.ok(gvcore).build();
			} catch (Exception e) {
				if (e.getCause() instanceof FileNotFoundException) {
					throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("<error><![CDATA[File not found: "+id+"/GVCore.xml]]></error>").build());
				}
				
				LOG.error("File to retrieve GVCore.xml in "+id, e);			
				throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("<error><![CDATA["+e.getMessage()+"]]></error>").build());
			}
		 
	 }
	 
	 @GET
	 @Path("/configuration/{configId}/properties")
	 @Produces(MediaType.APPLICATION_JSON)
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public Response getArchivedConfigProperties(@PathParam("configId") String id) {
		 Response response = null;
		 
		 
		 try {
			 byte[] gvcore = gvConfigurationManager.extract(id, "GVCore.xml");
			 if (gvcore!=null && gvcore.length>0) {
				 
				 
				 JSONArray properties = new JSONArray();
				 
				 String xml = new String(gvcore, "UTF-8");
				 
				 String pattern = "xmlp\\{\\{([-a-zA-Z0-9._]+)\\}\\}";
				 Pattern p = Pattern.compile(pattern);
				 Matcher m = p.matcher(xml);
					
				 while(m.find()) {
					properties.put(m.group(1));
				 }
				 response = Response.ok(properties.toString()).build();
			 }
		 } catch (Exception e) {
			LOG.error("Error reading services configuration", e);
			response = Response.status(Response.Status.NOT_FOUND).build();
		 }
		 
		 
		 return response;
	 }
	 
	 @GET
	 @Path("/configuration/{configId}")
	 @Produces(MediaType.APPLICATION_JSON)
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public Response getArchivedConfigServices(@PathParam("configId") String id) {
				
		 try {
			 byte[] gvcore = gvConfigurationManager.extract(id, "GVCore.xml");
			
			 if (gvcore!=null && gvcore.length>0) {
						 
					Document gvcoreDocument  = documentBuilder.parse(new ByteArrayInputStream(gvcore));
					
					NodeList serviceNodes = XMLConfig.getNodeList(gvcoreDocument, "//Service");
										
					Map<String, ServiceDTO> services = IntStream.range(0, serviceNodes.getLength())
									 .mapToObj(serviceNodes::item)
									 .map(ServiceDTO::buildServiceFromConfig)
									 .filter(Optional::isPresent)
									 .map(Optional::get)
									 .collect(Collectors.toMap(ServiceDTO::getIdService, Function.identity()));
					
					LOG.debug("Services found "+serviceNodes.getLength());
					return Response.ok(toJson(services)).build();
				
			 }
			 
			 return Response.status(Response.Status.NOT_FOUND).build();
			 
		 } catch (XMLConfigException | JsonProcessingException xmlConfigException){
				LOG.error("Error reading services configuration", xmlConfigException);
				throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(toJson(xmlConfigException)).build());
		 } catch (Exception e) {
			LOG.error("Error reading services configuration", e);
			return Response.status(Response.Status.NOT_FOUND).build();
		 
		 }		 
		 
	 }
	 
	 @GET
	 @Path("/configuration/{configId}/{serviceId}")
	 @Produces(MediaType.APPLICATION_JSON)
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public Response getArchivedConfigServices(@PathParam("configId") String id, @PathParam("serviceId") String service ) {
		 
		 try {
			 byte[] gvcore = gvConfigurationManager.extract(id, "GVCore.xml");
			 if (gvcore!=null && gvcore.length>0) {
				 
				
				 Document gvcoreDocument  = documentBuilder.parse( new ByteArrayInputStream(gvcore) );
									
				 Node serviceNode = Optional.ofNullable(XMLConfig.getNode(gvcoreDocument, "//Service[@id-service='"+service+"']"))
						   .orElseThrow(NoSuchElementException::new);

				 ServiceDTO svc = ServiceDTO.buildServiceFromConfig(serviceNode).orElseThrow(NoSuchElementException::new);				

				 return Response.ok(toJson(svc)).build();
			 }
			 
			 return Response.status(Response.Status.NOT_FOUND).build();
		
		 } catch (NoSuchElementException noSuchElementException) {
			 throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Service not found").build());
			 
		 } catch (XMLConfigException | JsonProcessingException xmlConfigException){
				LOG.error("Error reading services configuration", xmlConfigException);
				throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(toJson(xmlConfigException)).build());
		 } catch (Exception e) {
			LOG.error("Error reading services configuration", e);
			return Response.status(Response.Status.NOT_FOUND).build();
		 
		 }		 
	
		 
		 
	 }
	 
	 @GET
	 @Path("/configuration/{configId}/{serviceId}/{operationId}")
	 @Produces(MediaType.APPLICATION_JSON)
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public Response getArchivedConfigFlows(@PathParam("configId") String id, @PathParam("serviceId") String service, @PathParam("operationId")String operation ) {
		 
		 try {
			 byte[] gvcore = gvConfigurationManager.extract(id, "GVCore.xml");
			 if (gvcore!=null && gvcore.length>0) {
				 
				 
				 Document gvcoreDocument  = documentBuilder.parse( new ByteArrayInputStream(gvcore) );
									
				 Node operationNode = Optional.ofNullable(XMLConfig.getNode(gvcoreDocument, "//Service[@id-service='"+service+"']/Operation[@name='"+operation+"']" ))
	                     .orElseThrow(NoSuchElementException::new);

				 byte[] operationNodeData = XMLUtils.serializeDOMToByteArray_S(operationNode);

				 String response = XML.toJSONObject( new String(operationNodeData), true).toString();	

				 return Response.ok(response).build();
			 }
			 
			 return Response.status(Response.Status.NOT_FOUND).build();
		
		 } catch (NoSuchElementException noSuchElementException) {
			 throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Service not found").build());
			 
		 } catch (XMLConfigException | JsonProcessingException xmlConfigException){
				LOG.error("Error reading services configuration", xmlConfigException);
				throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(toJson(xmlConfigException)).build());
		 } catch (Exception e) {
			return Response.status(Response.Status.NOT_FOUND).build();
		 
		 }		 
		 
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
	 
	 @PUT
	 @Path("/deploy")
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public void reloadConfiguraiton(){
		 try {
			gvConfigurationManager.reload();
		} catch (XMLConfigException e) {
			 LOG.error("Export failed",e); 
			 throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	 }
	 
	 @GET
	 @Path("/deploy/export")
	 @Produces(MediaType.APPLICATION_OCTET_STREAM)
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public Response exportConfiguration() {
		 try {
			String currentConfig = gvConfigurationManager.getCurrentConfigurationName();
			return Response.ok(gvConfigurationManager.export(currentConfig))
						   .header("Content-Disposition", "attachment; filename="+currentConfig+".zip")
						   .build();
			 
		 } catch (IOException e) {
			 LOG.error("Export failed",e); 
			 throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		 }	 
		 
	 }
	 
	 @POST
	 @Path("/deploy/{configId}")
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public void deploy(@PathParam("configId") String id) {
		 				 		 		 
		 try {		 
			 gvConfigurationManager.deploy(id);
			 gvConfigurationManager.reload();
			 
		 } catch (IllegalStateException e) {
			 LOG.error("Deploy failed, a deploy is already in progress",e);
			 throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build());
		 
		 } catch (Exception e) {
			LOG.error("Deploy failed, something bad appened",e); 						
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build());
			
		 }		 
	 }
	 
	 @GET
	 @Path("/deploy/xml")
	 @Produces(MediaType.APPLICATION_JSON)
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public Response getConfigurationFileList() {		 	 
		 JSONArray files = new JSONArray(XMLConfig.getLoadedFiles());
		 return Response.ok(files.toString()).build();
	 }
	 
	 @GET
	 @Path("/deploy/xml/{name}")
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
			
			LOG.error("Failed to retrieve configuration file "+name,e);			
			throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("<error><![CDATA["+e.getMessage()+"]]></error>").build());
		}
		 
		if (Objects.isNull(document)) throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("<error><![CDATA[File not found: "+name+"]]></error>").build());
		
		return document;
		 
	 }
	 
	 @GET
	 @Path("/property")
	 @Produces(MediaType.APPLICATION_JSON)
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public Response getConfigProperties(){
		 
		 Response response = null;
		 
		 try {
			 Properties configProperties = gvConfigurationManager.getXMLConfigProperties();
			 
			 JSONObject configJson = new JSONObject();			 
			 configProperties.keySet().stream().map(Object::toString).forEach(k->configJson.put(k, configProperties.getProperty(k)));
			 
			 response = Response.ok(configJson.toString()).build();
		
		 } catch (FileNotFoundException e) {
			 response = Response.status(Response.Status.NOT_FOUND).build();
		 } catch (Exception e) {
			 LOG.error("Failed to retrieve XMLConfigProperties ",e);
			 response = Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
		 }
		 
		 return response;
		 
	 }
	 
	 @GET
	 @Path("/property/{key}")
	 @Produces(MediaType.TEXT_PLAIN)
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public String getConfigProperty(@PathParam("key") String key){
		 	 
		 try {
			 Properties configProperties = gvConfigurationManager.getXMLConfigProperties();
			 		 
			 return Optional.ofNullable(configProperties.getProperty(key))
					        .orElseThrow(()-> new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build()) );
		 
		 } catch (FileNotFoundException e) {
			 throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
			 
		 } catch (IOException e) {
			 LOG.error("Failed to retrieve XMLConfigProperties ",e);
			 throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).build());
		 }
		
	 }
	 
	 @POST
	 @Path("/property")
	 @Consumes(MediaType.APPLICATION_JSON)
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public void createProperties(String properties) {
		 try {
			 JSONObject configJson = new JSONObject(properties);
			 Properties configProperties = new Properties();
			 
			 configJson.keySet().stream().filter(k-> !configJson.isNull(k)).forEach(k -> configProperties.put(k, configJson.get(k).toString()));			 		 		 
			 
			 gvConfigurationManager.saveXMLConfigProperties(configProperties);
		 
		 } catch (JSONException e) {
			 throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());
		
		 } catch (Exception e) {
			 LOG.error("Failed to update XMLConfigProperties ",e);
			 throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).build());
		}
	 }
	 
	 @PUT
	 @Path("/property")
	 @Consumes(MediaType.APPLICATION_JSON)
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public void updateProperties(String properties) {
		 try {
			 JSONObject configJson = new JSONObject(properties);
			 
			 Properties configProperties = new Properties();			 
			 configProperties.putAll(gvConfigurationManager.getXMLConfigProperties());
			 			 
			 configJson.keySet().stream().filter(k-> !configJson.isNull(k)).forEach(k -> configProperties.put(k, configJson.get(k).toString()));			 		 		 
			 
			 gvConfigurationManager.saveXMLConfigProperties(configProperties);
		 
		 } catch (JSONException e) {
			 throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());
		 } catch (Exception e) {
			 LOG.error("Failed to update XMLConfigProperties ",e);
			 throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).build());
		}
	 }
	 
	 @PUT
	 @Path("/property/{key}")
	 @Consumes(MediaType.TEXT_PLAIN)
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public void setProperty(@PathParam("key")String key, String value) {
		 
		 
		 try {
			
			 Properties configProperties;
			 try {
				 configProperties = gvConfigurationManager.getXMLConfigProperties();
			 } catch (FileNotFoundException e) {
				 configProperties = new Properties();
			 }
			
			 configProperties.put(key, value);
			 
			 gvConfigurationManager.saveXMLConfigProperties(configProperties);
		 } catch (Exception e) {
			 LOG.error("Failed to update XMLConfigProperties ",e);
			 throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).build());
		}
	 }
	 
	 @DELETE
	 @Path("/property/{key}")	
	 @RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	 public void deleteProperty(@PathParam("key")String key) {		 
		 
		 try {
			
			 Properties configProperties = gvConfigurationManager.getXMLConfigProperties();			 			
			 configProperties.remove(key);			 
			 
			 gvConfigurationManager.saveXMLConfigProperties(configProperties);
			 
		 } catch (FileNotFoundException e) {
			 throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
		 } catch (Exception e) {
			 LOG.error("Failed to update XMLConfigProperties ",e);
			 throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).build());
		}
	 }	

}