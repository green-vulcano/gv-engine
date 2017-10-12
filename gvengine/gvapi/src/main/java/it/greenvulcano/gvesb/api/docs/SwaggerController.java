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
package it.greenvulcano.gvesb.api.docs;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public class SwaggerController {
	
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerController.class);
	
	private static final String UI_ROOT = "META-INF/resources/webjars/swagger-ui/3.2.2/"; 
    private static final Map<String, String> MEDIA_TYPES;
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper YAML_MAPPER = new YAMLMapper();
    
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
	
	private final String specs;
			
	public SwaggerController(String specs) {
		super();
		this.specs = specs;
	}
	
	public SwaggerController() {
		super();
		this.specs = "swagger.json";		
	}

	private JsonNode getSpecs(String basePath) throws JsonProcessingException, IOException {
		JsonNode jsonSpecs = OBJECT_MAPPER.readTree(Thread.currentThread().getContextClassLoader().getResourceAsStream(specs));		
		((ObjectNode)jsonSpecs).put("basePath", basePath);
		return jsonSpecs;
	}
	
	@GET 
	@Path("swagger.json")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJSON(@Context UriInfo uriInfo) {
		
		try {
			String basePath = uriInfo.getBaseUri().getPath().toString();			
			return Response.ok(OBJECT_MAPPER.writeValueAsString(getSpecs(basePath))).build();
		} catch (Exception e) {
			LOG.error("Failed to retrieve swager.json", e);
			throw new NotFoundException(e);
		}
	}
	
	
	@GET
	@Path("swagger.yaml")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getYAML(@Context UriInfo uriInfo) {
		
		try  {
			String basePath = uriInfo.getBaseUri().getPath().toString();			
			return Response.ok(YAML_MAPPER.writeValueAsString(getSpecs(basePath))).build();
		} catch (Exception e) {
			LOG.error("Failed to retrieve swager.json", e);
			throw new NotFoundException(e);
		}
	}
	
	@GET
    @Path("api-docs")
    public Response getIndex(@Context UriInfo uriInfo) {		
		return getResource(uriInfo, null);
	}
	
	@GET
    @Path("api-docs/{resource:.*}")
    public Response getResource(@Context UriInfo uriInfo, @PathParam("resource") String resourcePath) {
		
		
		if (resourcePath==null || resourcePath.trim().isEmpty() || resourcePath.trim().equals("/")) {
			String basePath = uriInfo.getBaseUri().toString();
			return Response.temporaryRedirect(URI.create("./api-docs/index.html?url="+basePath+"/swagger.json" )).build();           
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
