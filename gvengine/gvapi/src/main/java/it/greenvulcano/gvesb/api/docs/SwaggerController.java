package it.greenvulcano.gvesb.api.docs;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwaggerController {
	
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerController.class);
	
	private static final String UI_ROOT = "META-INF/resources/webjars/swagger-ui/3.2.2/"; 
    private static final Map<String, String> MEDIA_TYPES;
    
	static {
         MEDIA_TYPES = new HashMap<>();
         MEDIA_TYPES.put("html", "text/html");
         MEDIA_TYPES.put("png", "image/png");
         MEDIA_TYPES.put("gif", "image/gif");
         MEDIA_TYPES.put("css", "text/css");
         MEDIA_TYPES.put("js", "application/javascript");
         MEDIA_TYPES.put("eot", "application/vnd.ms-fontobject");
         MEDIA_TYPES.put("ttf", "application/font-sfnt");
         MEDIA_TYPES.put("svg", "image/svg+xml");
         MEDIA_TYPES.put("woff", "application/font-woff");
         MEDIA_TYPES.put("woff2", "application/font-woff2");
    }
	
	private final String specJSON, specYAML;
			
	public SwaggerController(String specJSON, String specYAML) {
		super();
		this.specJSON = specJSON;
		this.specYAML = specYAML;
	}
	
	public SwaggerController() {
		super();
		this.specJSON = "swagger.json";
		this.specYAML = "swagger.yaml";
	}

	@GET 
	@Path("swagger.json")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJSON() {
		
		try  {		
			return Response.ok(Thread.currentThread().getContextClassLoader().getResourceAsStream(specJSON)).build();
		} catch (Exception e) {
			LOG.error("Failed to retrieve swager.json", e);
			throw new NotFoundException(e);
		}
	}
	
	
	@GET
	@Path("swagger.yaml")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getYAML() {
		
		try  {			
			
			return Response.ok(Thread.currentThread().getContextClassLoader().getResourceAsStream(specYAML)).build();
		} catch (Exception e) {
			LOG.error("Failed to retrieve swager.json", e);
			throw new NotFoundException(e);
		}
	}
	
	@GET
    @Path("api-docs")
    public Response getIndex() {		
		return getResource(null);
	}
	
	@GET
    @Path("api-docs/{resource:.*}")
    public Response getResource(@PathParam("resource") String resourcePath) {
		
		
		if (resourcePath==null || resourcePath.trim().isEmpty() || resourcePath.trim().equals("/")) {        
			return Response.temporaryRedirect(URI.create("./api-docs/index.html?url=../swagger.json")).build();           
        }
       
        if (resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }
        
        try {
                  
            String mediaType = null;
            int ind = resourcePath.lastIndexOf('.');
            if (ind != -1 && ind < resourcePath.length()) {
                String resourceExt = resourcePath.substring(ind + 1);
                mediaType = MEDIA_TYPES.get(resourceExt);
            }
           
            ResponseBuilder rb = Response.ok(getClass().getClassLoader().getResource(UI_ROOT + resourcePath).openStream());
            if (mediaType != null) {
                rb.type(mediaType);
            }
            return rb.build();
        } catch (Exception ex) {        	
            throw new NotFoundException(ex);
        }
    }

}
