package it.greenvulcano.gvesb.api.controller;

import java.net.URI;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
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
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.api.dto.CredentialsDTO;
import it.greenvulcano.gvesb.api.dto.UserDTO;
import it.greenvulcano.gvesb.iam.domain.Role;
import it.greenvulcano.gvesb.iam.exception.CredentialsExpiredException;
import it.greenvulcano.gvesb.iam.exception.InvalidCredentialsException;
import it.greenvulcano.gvesb.iam.exception.InvalidPasswordException;
import it.greenvulcano.gvesb.iam.exception.InvalidUsernameException;
import it.greenvulcano.gvesb.iam.exception.PasswordMissmatchException;
import it.greenvulcano.gvesb.iam.exception.UserExistException;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.service.CredentialsManager;
import it.greenvulcano.gvesb.iam.service.UsersManager;

@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true)
public class GvSecurityControllerRest extends BaseControllerRest {
			
	private final static Logger LOG = LoggerFactory.getLogger(GvSecurityControllerRest.class);	
			
	private UsersManager gvUsersManager;
	private CredentialsManager gvCredentialsManager;
	
	public void setUsersManager(UsersManager gvSecurityManager) {
		this.gvUsersManager = gvSecurityManager;
	}
	
	public void setCredentialsManager(CredentialsManager gvCredentialsManager) {
		this.gvCredentialsManager = gvCredentialsManager;
	}
	
	public void init(){
		try {
			gvUsersManager.checkManagementRequirements();
		} catch (Exception e) {
			LOG.error("Error creating default user",e);
		}
	}
	
	@Path("/authenticate")
	@POST @Produces(MediaType.APPLICATION_JSON)
	public Response authenticate(@Context SecurityContext securityContext) {
		
		Response response = null;
		
		try {
		
			String username = securityContext.getUserPrincipal().getName();
			UserDTO currentUser = new UserDTO(gvUsersManager.getUser(username));
			
			response = Response.ok(toJson(currentUser)).build();	
		} catch (Exception e) {
			LOG.error("GVAPI_Excepiton - Authentication",e);
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

				UserDTO currentUser = new UserDTO(gvUsersManager.changeUserPassword(credentials[0], credentials[1], credentials[2]));
				
				response = Response.ok(toJson(currentUser)).build();
	        } else {
	        	throw new SecurityException();
	        }
		} catch (Exception e) {
			LOG.error("GVAPI_Exception - Change password",e);
			response = Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", "GV_RENEW").build();
		}
			
		return response;
	}
	
	@Path("/oauth2/access_token")
	@POST	
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createAccessToken(@Context HttpHeaders headers, @FormParam("grant_type")String grantType, @FormParam("username")String username, @FormParam("password")String password ){
		
		Response response = null;
		try {
			if (Optional.ofNullable(grantType).orElse("").equals("password")) {
				String authorization = Optional.ofNullable(headers.getHeaderString(HttpHeaders.AUTHORIZATION)).orElse("");
				String[] parts = authorization.split(" ");
		        if (parts.length == 2 && "basic".equalsIgnoreCase(parts[0])) {
		        	String[] clientCredentials = new String(Base64.getDecoder().decode(parts[1])).split(":");
		        	
		        	CredentialsDTO credentials = new CredentialsDTO(gvCredentialsManager.create(username, password, clientCredentials[0], clientCredentials[1]));
		        	response = Response.ok(toJson(credentials)).build();
		        } else {
		        	throw new SecurityException();
		        }
			} else {
				throw new IllegalArgumentException("unsupported grant_type");
			}
		} catch (IllegalArgumentException e) {
			response = Response.status(Status.BAD_REQUEST).entity(toJson(e)).build();			
		} catch (UserNotFoundException e) {
			response = Response.status(Status.NOT_FOUND).entity(toJson(e)).build();
		} catch (UserExpiredException e) {
			response = Response.status(Status.FORBIDDEN).entity(toJson(e)).build();
		} catch (PasswordMissmatchException e) {
			response = Response.status(Status.UNAUTHORIZED).entity(toJson(e)).build();		
		} catch (Exception e) {
			LOG.error("GVAPI_Exception - OAuth2 token creation",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(toJson(e)).build();
		}
		
		return response;
	}
	
	@Path("/oauth2/refresh_token")
	@POST	
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response refreshAccessToken(@Context HttpHeaders headers, @FormParam("grant_type")String grantType, @FormParam("access_token")String accessToken, @FormParam("refresh_token")String refreshToken ){
		
		Response response = null;
		try {
			if (Optional.ofNullable(grantType).orElse("").equals("refresh")) {	        
		        	
	        	CredentialsDTO credentials = new CredentialsDTO(gvCredentialsManager.refresh(refreshToken, accessToken));
	        	response = Response.ok(toJson(credentials)).build();
		      
			} else {
				throw new IllegalArgumentException("unsupported grant_type");
			}
		} catch (IllegalArgumentException e) {
			response = Response.status(Status.BAD_REQUEST).entity(toJson(e)).build();		

		} catch (CredentialsExpiredException|InvalidCredentialsException e) {
			response = Response.status(Status.FORBIDDEN).entity(toJson(e)).build();		
		} catch (Exception e) {
			LOG.error("GVAPI_Exception - OAuth2 token refresh",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(toJson(e)).build();
		}
		
		return response;
	}
	
	@Path("/admin/users")
	@GET @Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("gvadmin")
	public Response getUsers() {
		
		Response response = null;
		
		try {			
			
			Set<UserDTO> users = gvUsersManager.getUsers().stream().map(UserDTO::new).collect(Collectors.toSet());			
			response = Response.ok(toJson(users)).build();
		} catch (Exception e) {
			LOG.error("GVAPI_Exception - Retrieving users",e);
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
			
			Set<Role> roles = gvUsersManager.getRoles();
			
			response = Response.ok(toJson(roles)).build();	
		} catch (Exception e) {
			LOG.error("GVAPI_Exception - Retrieving roles",e);
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
			
			UserDTO user = new UserDTO(gvUsersManager.getUser(username));
			
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
			gvUsersManager.createUser(user.getUsername(), defaultPassword);
			gvUsersManager.updateUser(user.getUsername(), user.getUserInfo(), user.getGrantedRoles(), user.isEnabled());
					
			response = Response.created(URI.create("/admin/users/"+user.getUsername())).build();
			
		} catch (InvalidUsernameException|InvalidPasswordException e) {
			response = Response.status(Status.NOT_ACCEPTABLE).entity(toJson(e)).build();		
		} catch (UserExistException e) {
			response = Response.status(Status.CONFLICT).entity(toJson(e)).build();
		} catch (Exception e) {
			LOG.error("GVAPI_Exception - Create user",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(toJson(e)).build();
		}		
		
		return response;
		
	}
	
	@Path("/admin/users/{username}")
	@PUT @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("gvadmin")
	public Response editUser(@PathParam("username") String username, String data) {
		Response response = null;
		
		try {
			UserDTO user = parseJson(data, UserDTO.class);			
			gvUsersManager.updateUser(username, user.getUserInfo(), user.getGrantedRoles(), user.isEnabled());
			
			response = Response.ok().build();
		
		} catch (UserNotFoundException e) {
			response = Response.status(Status.NOT_FOUND).entity(toJson(e)).build();	

		} catch (Exception e) {
			LOG.error("GVAPI_Exception - Edit user",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(toJson(e)).build();
		}		
		
		return response;
		
	}
	
	@Path("/admin/users/{username}/enabled")
	@PATCH 
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("gvadmin")
	public Response switchUserStatus(@PathParam("username") String username){
		Response response = null;
		
		try {
	
			UserDTO user = new UserDTO(gvUsersManager.switchUserStatus(username));
								
			response = Response.ok(toJson(user)).build();
	
		} catch (UserNotFoundException e) {
			response = Response.status(Status.NOT_FOUND).entity(toJson(e)).build();
		} catch (Exception e) {
			LOG.error("GVAPI_Exception - Edit user status",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(toJson(e)).build();
		}		
		
		return response;
		
	}
	
	@Path("/admin/users/{username}/password")
	@PATCH 
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("gvadmin")
	public Response resetUserPassword(@PathParam("username") String username) {
		Response response = null;
		
		try {
			UserDTO user = new UserDTO(gvUsersManager.resetUserPassword(username));
			
			response = Response.ok(toJson(user)).build();
			
		} catch (UserNotFoundException e) {
			response = Response.status(Status.NOT_FOUND).entity(toJson(e)).build();
		} catch (Exception e) {
			LOG.error("GVAPI_Exception - Reset user password",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(toJson(e)).build();
		}		
		
		return response;
		
	}
	
	@Path("/admin/users/{username}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("gvadmin")
	public Response deleteUser(@PathParam("username") String username) {
		Response response = null;
		
		try {
		
			gvUsersManager.deleteUser(username);			
					
			response = Response.accepted().build();
		
		} catch (Exception e) {
			LOG.error("GVAPI_Exception - Delete user",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(toJson(e)).build();
		}		
		
		return response;
		
	}
	

}
