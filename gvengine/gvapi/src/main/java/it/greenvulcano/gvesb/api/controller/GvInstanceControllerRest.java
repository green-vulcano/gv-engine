package it.greenvulcano.gvesb.api.controller;

import java.io.IOException;

import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import it.greenvulcano.gvesb.api.GvInstanceController;
import it.greenvulcano.gvesb.osgi.bus.BusLink;

@Path("/instance")
public class GvInstanceControllerRest implements GvInstanceController<Response> {

	private BusLink busLink;
	
	public void setBusLink(BusLink busLink) {
		this.busLink = busLink;
	}	
	
	@Path("/link")
	@PUT
	@Override
	public Response bind(@QueryParam(value="apikey") String key) {
		Response response = null;
		
		if (key==null || key.trim().equals("")) {
			response = Response.status(Status.NOT_ACCEPTABLE).entity("Missing required param apikey").build();
		} else {
			 try {
				response = Response.status(Status.CREATED).entity(busLink.connect(key)).build();
			} catch (IOException e) {
				response = Response.status(Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build();
			}
		}
		return response;
		
	}
	
	@Path("/link")
	@DELETE
	@Override
	public Response unbind() {
		Response response = null; 
		try {
			response = Response.status(Status.OK).entity(busLink.connect("undefined")).build();
		} catch (IOException e) {
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build();
		}
		return response;
	}

}
