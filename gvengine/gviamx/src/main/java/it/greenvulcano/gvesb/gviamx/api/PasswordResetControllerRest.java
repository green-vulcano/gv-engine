package it.greenvulcano.gvesb.gviamx.api;

import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import it.greenvulcano.gvesb.gviamx.domain.PasswordResetRequest;
import it.greenvulcano.gvesb.gviamx.service.internal.PasswordResetManager;
import it.greenvulcano.gvesb.iam.exception.InvalidPasswordException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;

public class PasswordResetControllerRest {
	
	private PasswordResetManager passwordResetManager;
	
	public void setPasswordResetManager(PasswordResetManager passwordResetManager) {
		this.passwordResetManager = passwordResetManager;
	}	
	
	@Path("/restore")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response submitResetPasswordRequest(@FormParam("email") String email){
		Response response = null;
		try {
			
			passwordResetManager.createPasswordResetRequest(Optional.ofNullable(email).orElseThrow(()->new IllegalArgumentException("Required parameter: email")));
			response = Response.ok().build();
			
		} catch (UserNotFoundException e) {
			response = Response.status(Status.NOT_FOUND).entity("No matching account found").build();
		} catch (IllegalArgumentException e) {
			response = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();		
		}
		
		return response;
				
	}
	
	@Path("/restore")
	@PUT
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response consumeResetPasswordRequest(@FormParam("email")String email, @FormParam("token")String token, @FormParam("password")String password) {
		
		Response response = null;
		try {
			Optional.ofNullable(email).orElseThrow(()->new IllegalArgumentException("Required parameter: email"));
			Optional.ofNullable(token).orElseThrow(()->new IllegalArgumentException("Required parameter: token"));
			Optional.ofNullable(password).orElseThrow(()->new IllegalArgumentException("Required parameter: password"));
			
			PasswordResetRequest passwordResetRequest =  passwordResetManager.retrievePasswordResetRequest(email,token);
			
			passwordResetManager.getUsersManager().resetUserPassword(email, password);
			passwordResetManager.getUsersManager().setUserExpiration(email, false);
			
			passwordResetManager.consumePasswordResetRequest(passwordResetRequest);
			response = Response.ok().build();
			
		} catch (UserNotFoundException e) {
			response = Response.status(Status.NOT_FOUND).entity("No matching account found").build();
		} catch (InvalidPasswordException e) {
			response = Response.status(Status.FORBIDDEN).entity("Invalid password").build();
		} catch (IllegalArgumentException e) {
			response = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();		
		}
		
		return response;
		
		
	}

}
