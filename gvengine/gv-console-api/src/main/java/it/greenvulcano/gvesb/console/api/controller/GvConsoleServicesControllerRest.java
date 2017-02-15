package it.greenvulcano.gvesb.console.api.controller;

import it.greenvulcano.gvesb.console.api.GvConsoleServicesController;
import it.greenvulcano.gvesb.console.api.dto.ServiceStatsThroughput;
import it.greenvulcano.gvesb.console.api.dto.ServiceStatsTimeDTO;
import it.greenvulcano.gvesb.console.api.dto.TraceLevelServiceDTO;
import it.greenvulcano.gvesb.console.api.model.ServiceInstance;
import it.greenvulcano.gvesb.console.api.repository.dao.ServiceInstanceDao;
import it.greenvulcano.gvesb.console.api.repository.dao.TraceLevelServiceDao;
import it.greenvulcano.gvesb.console.api.utility.DateParam;

import java.util.List;
import java.util.NoSuchElementException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GvConsoleServicesControllerRest implements GvConsoleServicesController<Response>
{
	private static final Logger LOG = LoggerFactory.getLogger(GvConsoleServicesControllerRest.class);

	@Context
	private UriInfo uriInfo;
	
	
	private ServiceInstanceDao serviceInstanceRepo;
	
	private TraceLevelServiceDao traceLevelServiceRepo;

	@Path("/probe")
	@GET
	public String probe(){
		return "It works";
	}


	@Path("/serviceinstance/{serviceinstanceid}")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)	
	@Override
	public Response findByServiceInstanceId(@PathParam("serviceinstanceid") String serviceinstanceid)
	{
		LOG.debug("findByServiceInstanceId - START - "+serviceinstanceid);
		String response = null;
		try
		{
			List<ServiceInstance> serviceInstanceList = getServiceInstanceRepo().getByServiceInstanceId(serviceinstanceid);
			response = new ObjectMapper().writeValueAsString(serviceInstanceList);

		} catch (NoSuchElementException noSuchElementException) {
			new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Service not found").build());
		} catch (JsonProcessingException xmlConfigException) {
			new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Configuration error").build());
		}
		LOG.debug("findByServiceInstanceId - END - "+serviceinstanceid);
		
		return Response.ok(response).build();
	}
	
	@Path("/stats/{servicename}/{startdate}/{enddate}")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)	
	@Override
	public Response getStatsByServiceName(@PathParam("servicename") String servicename, @PathParam("startdate") DateParam startdate, @PathParam("enddate") DateParam enddate)
	{
		LOG.debug("getStatsByServiceName - START - " + servicename + " - startdate: " + startdate + " - enddate: " + enddate);
		String response = null;
		try
		{
			ServiceStatsTimeDTO serviceStatsTimeDTO = getServiceInstanceRepo().getStatsByServiceName(servicename, startdate.getDate(), enddate.getDate());
			response = new ObjectMapper().writeValueAsString(serviceStatsTimeDTO);

		} catch (NoSuchElementException noSuchElementException) {
			new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Service not found").build());
		} catch (JsonProcessingException xmlConfigException) {
			new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Configuration error").build());
		}
		LOG.debug("getStatsByServiceName - END - "+servicename);
		
		return Response.ok(response).build();
	}
	
	@Path("/stats/throughput/{servicename}/{startdate}/{enddate}")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)	
	@Override
	public Response getThroughputByServiceName(@PathParam("servicename") String servicename, @PathParam("startdate") DateParam startdate, @PathParam("enddate") DateParam enddate)
	{
		LOG.debug("getThroughputByServiceName - START - " + servicename + " - startdate: " + startdate + " - enddate: " + enddate);
		String response = null;
		try
		{
			ServiceStatsThroughput serviceStatsThroughput = getServiceInstanceRepo().getThroughputRequestsByServiceName(servicename, startdate.getDate(), enddate.getDate());
			response = new ObjectMapper().writeValueAsString(serviceStatsThroughput);

		} catch (NoSuchElementException noSuchElementException) {
			new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Service not found").build());
		} catch (JsonProcessingException xmlConfigException) {
			new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Configuration error").build());
		}
		LOG.debug("getThroughputByServiceName - END - "+servicename);
		
		return Response.ok(response).build();
	}
	
	/**********************************************************************************************/
	/**********************************  TRACE LEVEL SERVICE **************************************/
	/**********************************************************************************************/
	@Path("/tracelevel/{servicename}")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)	
	@Override
	public Response findTraceLevelByServiceName(@PathParam("servicename") String servicename)
	{
		LOG.debug("findTraceLevelByServiceName - START - "+servicename);
		String response = null;
		try
		{
			List<TraceLevelServiceDTO> traceLevelListDTO = getTraceLevelServiceRepo().getByServiceName(servicename);
			response = new ObjectMapper().writeValueAsString(traceLevelListDTO);

		} catch (NoSuchElementException noSuchElementException) {
			new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Service not found").build());
		} catch (JsonProcessingException xmlConfigException) {
			new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Configuration error").build());
		}
		LOG.debug("findTraceLevelByServiceName - END - "+servicename);
		
		return Response.ok(response).build();
	}
	
	@Path("/tracelevel")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)	
	@Override
	public Response findAllTraceLevelByServiceName()
	{
		LOG.debug("findAllTraceLevelByServiceName - START");
		String response = null;
		try
		{
			List<TraceLevelServiceDTO> traceLevelListDTO = getTraceLevelServiceRepo().getByServiceName(null);
			response = new ObjectMapper().writeValueAsString(traceLevelListDTO);

		} catch (NoSuchElementException noSuchElementException) {
			new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Service not found").build());
		} catch (JsonProcessingException xmlConfigException) {
			new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Configuration error").build());
		}
		LOG.debug("findTraceLevelByServiceName - END");
		
		return Response.ok(response).build();
	}
	
	@Path("/tracelevel")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)	
	@Override
	public Response updateTraceLevel(TraceLevelServiceDTO traceLevelServiceDTO)
	{
		LOG.debug("updateTraceLevel - START");
		Response response = null;
		try
		{
			response = getTraceLevelServiceRepo().update(traceLevelServiceDTO);

		} catch (NoSuchElementException noSuchElementException) {
			new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Service not found").build());
		}
		LOG.debug("updateTraceLevel - END");
		
		return response;
	}
	
	@Path("/tracelevel")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)	
	@Override
	public Response createTraceLevel(TraceLevelServiceDTO traceLevelServiceDTO)
	{
		LOG.debug("createTraceLevel - START");
		try
		{
			getTraceLevelServiceRepo().save(traceLevelServiceDTO);

		} catch (NoSuchElementException noSuchElementException) {
			new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Service not found").build());
		}
		LOG.debug("createTraceLevel - END");
		
		return Response.ok().build();
	}
	
	@Path("/tracelevel/{tracelevelid}")
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)	
	@Override
	public Response deleteTraceLevel(@PathParam("tracelevelid") String tracelevelid)
	{
		LOG.debug("deleteTraceLevel - START - tracelevelid: " + tracelevelid);
		try
		{
			TraceLevelServiceDTO traceLevelDTO = new TraceLevelServiceDTO();
			traceLevelDTO.setId(tracelevelid);
			getTraceLevelServiceRepo().delete(traceLevelDTO);

		} catch (NoSuchElementException noSuchElementException) {
			new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Service not found").build());
		}
		LOG.debug("deleteTraceLevel - END - tracelevelid: " + tracelevelid);
		
		return Response.ok().build();
	}
	

	public ServiceInstanceDao getServiceInstanceRepo() {
		return serviceInstanceRepo;
	}


	public void setServiceInstanceRepo(ServiceInstanceDao serviceInstanceRepo) {
		this.serviceInstanceRepo = serviceInstanceRepo;
	}


	public TraceLevelServiceDao getTraceLevelServiceRepo() {
		return traceLevelServiceRepo;
	}


	public void setTraceLevelServiceRepo(TraceLevelServiceDao traceLevelServiceRepo) {
		this.traceLevelServiceRepo = traceLevelServiceRepo;
	}
	

}