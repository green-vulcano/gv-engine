package it.greenvulcano.gvesb.api.controller;

import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;

import it.greenvulcano.gvesb.iam.domain.Property;
import it.greenvulcano.gvesb.iam.service.PropertyManager;

@CrossOriginResourceSharing(allowAllOrigins = true, allowCredentials = true, exposeHeaders = { "Content-Type", "Content-Range", "X-Auth-Status" })
public class GvStoreControllerRest {

    private PropertyManager propertyManager;

    public void setPropertyManager(PropertyManager propertyManager) {

        this.propertyManager = propertyManager;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "gvstore" })
    public Response getStoredProperties() {

        if (propertyManager != null) {

            String response = propertyManager.retrieveAll()
                                             .stream()
                                             .map(Property::toString)
                                             .collect(Collectors.joining(",", "[", "]"));

            return Response.ok(response).build();

        } else {
            throw new WebApplicationException(Status.NOT_IMPLEMENTED);
        }
    }

    @GET
    @Path("/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "gvstore" })
    public Response getStoredProperty(@PathParam("key") String key) {

        if (propertyManager != null) {

            String response = propertyManager.retrieve(key)
                                             .orElseThrow(() -> new WebApplicationException(Status.NOT_FOUND))
                                             .toString();

            return Response.ok(response).build();
        } else {
            throw new WebApplicationException(Status.NOT_IMPLEMENTED);
        }
    }

    @POST
    @Path("/{key}")
    @Consumes(MediaType.TEXT_PLAIN)
    @RolesAllowed({ "gvstore" })
    public void saveProperty(@Context MessageContext jaxrsContext, @PathParam("key") String key, String value) {

        if (propertyManager != null) {
            
            try {
                propertyManager.store(key, value, jaxrsContext.getSecurityContext().getUserPrincipal().getName(), jaxrsContext.getHttpServletRequest().getLocalAddr());
            
            } catch (Exception e) {                
                throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
            }

        } else {

            throw new WebApplicationException(Status.NOT_IMPLEMENTED);
        }

    }

    @DELETE
    @Path("/{key}")
    @RolesAllowed({ "gvstore" })
    public void deleteProperty(@Context MessageContext jaxrsContext, @PathParam("key") String key, String value) {

        if (propertyManager != null) {
            
            propertyManager.retrieve(key)
                           .orElseThrow(() -> new WebApplicationException(Status.NOT_FOUND));
            
            propertyManager.delete(key);

        } else {

            throw new WebApplicationException(Status.NOT_IMPLEMENTED);
        }

    }

}
