package it.greenvulcano.gvesb.properties.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.greenvulcano.gvesb.properties.model.GVProperties;
import it.greenvulcano.gvesb.properties.service.ConfProperties;
 
@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true)
public class PropertiesRest {
	
	private final static Logger LOG = LoggerFactory.getLogger(PropertiesRest.class);
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	private ConfProperties confProperties;
	
	public void setConfProperties(ConfProperties confProperties) {
		this.confProperties = confProperties;
	}
	
	
	@Path("/properties")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getProperties() throws JsonProcessingException{
		
		Response response = null;
		
		if(confProperties.getProperties() == null) {
			response = Response.status(Status.NOT_FOUND).build();
			LOG.error("File properties not found");
		}else {
			GVProperties gvProperties = confProperties.getProperties();
			response = Response.ok(OBJECT_MAPPER.writeValueAsString(gvProperties)).build();
		}
		
		
		return response;
		
	}
	
	@POST
	@Path("/properties")
	@Consumes(MediaType.TEXT_PLAIN)
	public void saveProperties(String content) {
		
		confProperties.saveProperties(content);
		
	}
	
	@GET
	@Path("/property/{key}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProperty(@PathParam("key") String key) throws JsonProcessingException{
		
		Response response = null;
		String property = confProperties.getProperty(key);
		
		if(property != null) {
			response = Response.ok(OBJECT_MAPPER.writeValueAsString(property)).build();
		}else {
			response = Response.status(Status.NOT_FOUND).build();
			LOG.error("Property not found");
		}
		
		return response;
	}
	
}
