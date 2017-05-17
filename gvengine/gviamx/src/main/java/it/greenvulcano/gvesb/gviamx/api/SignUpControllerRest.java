package it.greenvulcano.gvesb.gviamx.api;

import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.gviamx.domain.SignUpRequest;
import it.greenvulcano.gvesb.gviamx.service.internal.SignUpManager;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.domain.UserInfo;
import it.greenvulcano.gvesb.iam.exception.InvalidPasswordException;
import it.greenvulcano.gvesb.iam.exception.InvalidUsernameException;
import it.greenvulcano.gvesb.iam.exception.UserExistException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.util.crypto.CryptoHelper;

@CrossOriginResourceSharing
public class SignUpControllerRest {
	private final static Logger LOG = LoggerFactory.getLogger(SignUpControllerRest.class);
	
	private SignUpManager signupManager;
	
	public void setSignupManager(SignUpManager signupManager) {
		this.signupManager = signupManager;
	}
	
	@Path("/signup")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkResgistrationStatus(@QueryParam("check") String email) {
			
		JSONObject status = new JSONObject();
		if (email != null &&  email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
			status.put("email", email);
		} else {
			return Response.status(Status.BAD_REQUEST).entity("Missing or invalid value for parameter 'check' ").build();
			
		}
		
		try {
			signupManager.getUsersManager().getUser(email);
			status.put("status", "CONFIRMED");
		} catch (UserNotFoundException userNotFoundException) {
			
			try {
				signupManager.retrieveSignUpRequest(email, "");
				status.put("status", "PENDING");
			} catch (IllegalArgumentException signupRequestNotExistException) {
				status.put("status", "UNKNOWN");
			}
		} catch (Exception e) {
			LOG.error("Fatal error checking signup status", e);
			
			return Response.serverError().build();
		}
		
		return Response.ok(status.toString()).build();
		
	}
	
	@Path("/signup")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)	
	public Response submitSignUpRequest(String request) {
		Response response = null;
		
		try {
			JSONObject jsonData = new JSONObject(request);
			String password = jsonData.optString("password", "") ;
						
		    if (!password.matches(User.PASSWORD_PATTERN)) {
		    	throw new InvalidPasswordException(password);
		    }
			
			jsonData.put("password", CryptoHelper.encrypt(CryptoHelper.DEFAULT_KEY_ID, password, true ));
			signupManager.createSignUpRequest(jsonData.optString("email"), password, jsonData.toString().getBytes());
		
			response = Response.ok().build();
		} catch (InvalidPasswordException e) {
			response = Response.status(Status.FORBIDDEN).entity(e.getMessage()).build();			
		} catch (JSONException jsonException) {
			response = Response.status(Status.BAD_REQUEST).entity("Invalid json").build();
		} catch (IllegalArgumentException e) {
			response = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();		
		} catch (UserExistException e) {
			response = Response.status(Status.CONFLICT).entity("Email address in use ").build();
		} catch (Exception e) {
			LOG.error("Fatal error creating signup request", e);
			
			response = Response.serverError().build();
		}
		
		return response;
		
	}
	
	@Path("/signup")
	@PUT
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response confirmSignUpRequest(@FormParam("email")String email, @FormParam("token")String token){
				
		Response response = null;
		
		try {
			
			Optional.ofNullable(email).orElseThrow(()->new IllegalArgumentException("Required parameter: email"));
			Optional.ofNullable(token).orElseThrow(()->new IllegalArgumentException("Required parameter: token"));
						
			SignUpRequest signupRequest = signupManager.retrieveSignUpRequest(email, token);			
			
			String password = CryptoHelper.decrypt(CryptoHelper.DEFAULT_KEY_ID, signupRequest.getRequestObject().getString("password"), false) ;
			
			User user = signupManager.getUsersManager().createUser(email, password);
		
			user.setUserInfo(new UserInfo());
			user.getUserInfo().setEmail(email);
			user.getUserInfo().setFullname(signupRequest.getFullname());
			signupManager.getUsersManager().updateUser(email, user.getUserInfo(), user.getRoles(), true, false);
			
			signupManager.consumeSignUpRequest(signupRequest);
			response = Response.ok().build();
		} catch (IllegalArgumentException|SecurityException e) {
			response = Response.status(Status.NOT_FOUND).build();	
		} catch (InvalidUsernameException e) {
			response = Response.status(Status.BAD_REQUEST).entity("Invalid email").build();
		} catch (InvalidPasswordException e) {
			response = Response.status(Status.BAD_REQUEST).entity("Invalid password").build();		
		} catch (Exception e) {
			LOG.error("Fatal error performing signup", e);
			signupManager.getUsersManager().deleteUser(email);
			response = Response.serverError().build();
		}
		
		return response;
	}

}
