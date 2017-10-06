package it.greenvulcano.gvesb.gviamx.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwaggerSpecProvider {
	
	private final static Logger LOG = LoggerFactory.getLogger(SwaggerSpecProvider.class);
	
	@Path("/swagger.json")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJSON() {
		
		try (BufferedReader reader =  new BufferedReader( new InputStreamReader(getClass().getClassLoader().getResourceAsStream("swagger.json"), "UTF-8"))) {			
			String specs = reader.lines().collect(Collectors.joining("\n"));			
			return Response.ok(specs, MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			LOG.error("Failed to retrieve swager.json", e);
			throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
		}
	}

}
