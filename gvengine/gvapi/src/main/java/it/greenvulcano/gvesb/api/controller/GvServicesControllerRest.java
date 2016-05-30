package it.greenvulcano.gvesb.api.controller;

import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.api.GvServicesController;
import it.greenvulcano.gvesb.api.model.Operation;
import it.greenvulcano.gvesb.api.model.Service;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.buffer.GVPublicException;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolException;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;

public class GvServicesControllerRest implements GvServicesController{
	private static final Logger LOG = LoggerFactory.getLogger(GvServicesControllerRest.class);
	
	private final static Optional<GreenVulcanoPool> gvpool;
	
	@Context
	private UriInfo uriInfo;
	
	
	static {
		GreenVulcanoPool gvpoolInstance = null;
		try {
			gvpoolInstance = GreenVulcanoPoolManager.instance().getGreenVulcanoPool("gvapi");
		} catch (Exception e) {
			LOG.error("gvcoreapi - Error retriving a GreenVulcanoPool instance for subsystem HttpInboundGateway", e);						
		}
		gvpool = Optional.ofNullable(gvpoolInstance);
	}
	
	@Path("/probe")
	@GET
	public String probe(){
		return "It works";
	}
	
	@Path("/")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override public Response getServices() {
		
		String response = null;
		try {
			 
			NodeList serviceNodes = XMLConfig.getNodeList("GVServices.xml", "//Service");
			
			
			List<Service> services = IntStream.range(0, serviceNodes.getLength())
							 .mapToObj(serviceNodes::item)
							 .map(this::buildServiceFromConfig)
							 .filter(Optional::isPresent)
							 .map(Optional::get)
							 .collect(Collectors.toList());
			LOG.debug("Services found "+serviceNodes.getLength());
			response = new ObjectMapper().writeValueAsString(services);
		} catch (XMLConfigException | JsonProcessingException xmlConfigException){
			LOG.error("Error reading services configuration", xmlConfigException);
			new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Configuration error").build());
		}	
		
		return Response.ok(response).build();
		
	}
	
	@Path("/{service}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override public Response getOperations(@PathParam("service") String service) {
		String response = null;
		
		try {
			Node serviceNode = Optional.ofNullable(XMLConfig.getNode("GVServices.xml", "//Service[@id-service='"+service+"']"))
									   .orElseThrow(NoSuchElementException::new);
			
		    List<Operation>	 operations = getOperations(serviceNode); 
		    response = new ObjectMapper().writeValueAsString(operations);		   
			
		} catch (NoSuchElementException noSuchElementException) {
			new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Service not found").build());
		} catch (XMLConfigException | JsonProcessingException xmlConfigException) {
			new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Configuration error").build());
		}		
		
		return Response.ok(response).build();
	}
	
	private List<Operation> getOperations(Node serviceNode) throws XMLConfigException{
		NodeList operationNodes = XMLConfig.getNodeList(serviceNode, "./Operation");
		return IntStream.range(0, operationNodes.getLength())
							  .mapToObj(operationNodes::item)
							  .map(this::buildOperationFromConfig)
							  .filter(Optional::isPresent)
							  .map(Optional::get)
							  .collect(Collectors.toList());
	}
	
	@Path("/{service}/{operation}")
	@POST @GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)	
	@Override public Response execute(@PathParam("service") String service, 
						  			  @PathParam("operation")String operation,						  			 
						  			  String data) {
		
		String response = null;
		
		GVBuffer input = null;
		try {
			input = new GVBuffer();
			
			for (Entry<String, List<String>> prop : uriInfo.getQueryParameters().entrySet()){
				input.setProperty(prop.getKey(), prop.getValue().stream().collect(Collectors.joining(";")));
			}
			
			input.setService(service);
			input.setObject(data);
		} catch (GVException e) {
			LOG.error("gvcoreapi - Error building GVBuffer", e);
			throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("GVBuffer creation failed").build());
		}
		
		try {
			
			GVBuffer output = gvpool.orElseThrow(()-> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("GreenVulcanoPool not available for subsystem HttpInboundGateway").build()))
									.forward(input, operation);
			if (output.getObject() instanceof String) {
				response = output.getObject().toString();
			} else if (output.getObject() instanceof org.json.JSONObject) {
				response = org.json.JSONObject.class.cast(output.getObject()).toString();
			}  else if (output.getObject() instanceof org.json.JSONArray) {
				response = org.json.JSONArray.class.cast(output.getObject()).toString();
			} else if (Objects.nonNull(output.getObject())) {
				
				response = new ObjectMapper().writeValueAsString(output.getObject());
								
			} else {
				return Response.ok().build();
			}
			
		
		} catch (GVPublicException e) {			
			LOG.error("gvcoreapi - Error performing operation "+operation+" on "+service+" service", e);
			
			if (e.getMessage().contains("GV_SERVICE_NOT_FOUND_ERROR")){
				throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Service "+service+" not found").build());
			} else if (e.getMessage().contains("GVCORE_BAD_GVOPERATION_NAME_ERROR")) {
				throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Operation "+operation+" not found").build());
			}
			
			throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Operation failed").build());
		
		} catch (GreenVulcanoPoolException e) {
			LOG.error("gvcoreapi - Error performing forward on GreenVulcanoPool instance for subsystem HttpInboundGateway", e);
			throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("GreenVulcanoPool not available for subsystem HttpInboundGateway").build());			
		
		} catch (JsonProcessingException e) {
			LOG.error("gvcoreapi - Unparsable response data", e);
			throw new WebApplicationException(Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).entity("JSON parsing failed").build());			
		}
		
		return Response.ok(response).build();
	}	
		
	private Optional<Service> buildServiceFromConfig(Node config) {
		try {
			Service service = new Service(XMLConfig.get(config, "@id-service"), 
										  XMLConfig.get(config, "@group-name"), 
										  XMLConfig.get(config, "@service-activation").equals("on"), 
										  XMLConfig.get(config, "@statistics").equals("on"));
						
			List<Operation> operations = getOperations(config);
			if (!operations.isEmpty()) {
				service.getOperations().addAll(operations);
			}
			return Optional.of(service);
		} catch (NullPointerException|XMLConfigException xmlConfigException){
			LOG.error("Error reading service configuration", xmlConfigException);
		}
		
		return Optional.empty();
		
	}
	
	private Optional<Operation> buildOperationFromConfig(Node config) {
		try {
			Operation operation = new Operation(XMLConfig.get(config, "@forward-name", XMLConfig.get(config, "@name") ), 
										  XMLConfig.get(config, "@operation-activation").equals("on"));
			return Optional.of(operation);
		} catch (NullPointerException|XMLConfigException xmlConfigException){
			LOG.error("Error reading operation configuration", xmlConfigException);
		}
		
		return Optional.empty();
		
	}
	
	
}