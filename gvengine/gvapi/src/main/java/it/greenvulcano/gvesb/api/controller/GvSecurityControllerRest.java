package it.greenvulcano.gvesb.api.controller;

import java.net.URI;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.PATCH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.api.dto.UserDTO;
import it.greenvulcano.gvesb.iam.domain.Role;
import it.greenvulcano.gvesb.iam.exception.InvalidPasswordException;
import it.greenvulcano.gvesb.iam.exception.InvalidRoleException;
import it.greenvulcano.gvesb.iam.exception.InvalidUsernameException;
import it.greenvulcano.gvesb.iam.exception.UserExistException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.service.SecurityManager;

public class GvSecurityControllerRest extends BaseControllerRest {
			
	private final static Logger LOG = LoggerFactory.getLogger(GvSecurityControllerRest.class);	
			
	private SecurityManager gvSecurityManager;
		
	public void setSecurityManager(SecurityManager gvSecurityManager) {
		this.gvSecurityManager = gvSecurityManager;
	}
	
	@Path("/authenticate")
	@POST @Produces(MediaType.APPLICATION_JSON)
	public Response authenticate(@Context SecurityContext securityContext) {
		
		Response response = null;
		
		try {
		
			String username = securityContext.getUserPrincipal().getName();
			UserDTO currentUser = new UserDTO(gvSecurityManager.getUser(username));
			
			response = Response.ok(toJson(currentUser)).build();	
		} catch (Exception e) {
			response = Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", "Basic").build();
		}
			
		return response;
	}
	
	@Path("/authenticate")
	@PATCH
	@Produces(MediaType.APPLICATION_JSON)
	public Response changePassword(@Context HttpHeaders headers) {
		
		Response response = null;
		
		try {
			
			String authorization = Optional.ofNullable(headers.getHeaderString(HttpHeaders.AUTHORIZATION)).orElse("");
	        String[] parts = authorization.split(" ");
	        if (parts.length == 2 && "GV_RENEW".equals(parts[0])) {	
	        	
	        	String[] credentials = new String(Base64.getDecoder().decode(parts[1])).split(":");

				UserDTO currentUser = new UserDTO(gvSecurityManager.changeUserPassword(credentials[0], credentials[1], credentials[2]));
				
				response = Response.ok(toJson(currentUser)).build();
	        }
		} catch (Exception e) {
			LOG.error("API_CALL_ERROR",e);
			response = Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", "GV_RENEW").build();
		}
			
		return response;
	}
	
	@Path("/admin/users")
	@GET @Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("gvadmin")
	public Response getUsers() {
		
		Response response = null;
		
		try {			
			
			Set<UserDTO> users = gvSecurityManager.getUsers().stream().map(UserDTO::new).collect(Collectors.toSet());			
			response = Response.ok(toJson(users)).build();
		} catch (Exception e) {
			LOG.error("API_CALL_ERROR",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(toJson(e)).build();
		}		
		
		return response;
	}
	
	@Path("/admin/roles")
	@GET @Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("gvadmin")
	public Response getRoles() {
		
		Response response = null;
		
		try {			
			
			Set<Role> roles = gvSecurityManager.getRoles();
			
			response = Response.ok(toJson(roles)).build();	
		} catch (Exception e) {
			LOG.error("API_CALL_ERROR",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(toJson(e)).build();
		}		
		
		return response;
	}
	
	@Path("/admin/users/{username}")
	@GET @Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("gvadmin")
	public Response getUser(@PathParam("username") String username) {
		
		Response response = null;
		
		try {			
			
			UserDTO user = new UserDTO(gvSecurityManager.getUser(username));
			
			response = Response.ok(toJson(user)).build();	
		} catch (UserNotFoundException userNotFoundException) {			
			response = Response.status(Status.NOT_FOUND).build();		
		} catch (Exception e) {
			LOG.error("API_CALL_ERROR",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(toJson(e)).build();
		}		
		
		return response;
	}
	
	@Path("/admin/users")
	@POST @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("gvadmin")
	public Response createUser(String data) {
		Response response = null;
		
		try {
			UserDTO user = parseJson(data, UserDTO.class);
			
			String defaultPassword = user.getUsername();			
			gvSecurityManager.createUser(user.getUsername(), defaultPassword);
			gvSecurityManager.saveUserInfo(user.getUsername(), user.getUserInfo());
			
			for (Role role : user.getRoles()) {
				try {
					gvSecurityManager.addRole(user.getUsername(), role);
				} catch (InvalidRoleException invalidRoleException) {
					LOG.warn("Creating new user "+user.getUsername() + " skipped role creation: "+invalidRoleException.getMessage());
				}	
			}
					
			response = Response.created(URI.create("/admin/users/"+user.getUsername())).build();
			
		} catch (InvalidUsernameException|InvalidPasswordException e) {
			response = Response.status(Status.NOT_ACCEPTABLE).entity(toJson(e)).build();		
		} catch (UserExistException e) {
			response = Response.status(Status.CONFLICT).entity(toJson(e)).build();
		} catch (Exception e) {
			LOG.error("API_CALL_ERROR",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(toJson(e)).build();
		}		
		
		return response;
		
	}
	
	@Path("/admin/users")
	@PUT @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("gvadmin")
	public Response editUser(String data) {
		Response response = null;
		
		try {
			UserDTO user = parseJson(data, UserDTO.class);			
			
			gvSecurityManager.saveUserInfo(user.getUsername(), user.getUserInfo());
			gvSecurityManager.enableUser(user.getUsername(), user.isEnabled());
			for (Role role : user.getRoles()) {
				try {
					gvSecurityManager.addRole(user.getUsername(), role);
				} catch (InvalidRoleException invalidRoleException) {
					LOG.warn("Creating new user "+user.getUsername() + " skipped role creation: "+invalidRoleException.getMessage());
				}	
			}
					
			response = Response.ok().build();
			
		} catch (InvalidUsernameException|InvalidPasswordException e) {
			response = Response.status(Status.NOT_ACCEPTABLE).entity(toJson(e)).build();		
		} catch (UserExistException e) {
			response = Response.status(Status.CONFLICT).entity(toJson(e)).build();
		} catch (Exception e) {
			LOG.error("API_CALL_ERROR",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(toJson(e)).build();
		}		
		
		return response;
		
	}
	

}
