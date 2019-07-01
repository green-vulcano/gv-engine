package it.greenvulcano.gvesb.gviamx.api;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.gviamx.service.internal.SignUpManager;
import it.greenvulcano.gvesb.iam.domain.Credentials;
import it.greenvulcano.gvesb.iam.exception.PasswordMissmatchException;
import it.greenvulcano.gvesb.iam.exception.UnverifiableUserException;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.service.CredentialsManager;
import it.greenvulcano.gvesb.iam.service.UsersManager.Authority;

@Path("/oauth2")
@CrossOriginResourceSharing
public class GVExternalAccountControllerRest {

    private final static Logger LOG = LoggerFactory.getLogger(GVExternalAccountControllerRest.class);

    private CredentialsManager credentialsManager;
    private SignUpManager signupManager;

    public void setCredentialsManager(CredentialsManager credentialsManager) {

        this.credentialsManager = credentialsManager;
    }

    public void setSignupManager(SignUpManager signupManager) {

        this.signupManager = signupManager;
    }

    @POST
    @Path("/exchange_token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccessTokenForExternalUser(@FormParam("token") String externalCredentials, @FormParam("provider") String externalCredentialsProvider) {

        Response response = null;

        try {

            Credentials internalCredentials = credentialsManager.create(externalCredentials, externalCredentialsProvider);

            JSONObject responsePayload = new JSONObject().put("access_token", internalCredentials.getAccessToken())
                                                         .put("refresh_token", internalCredentials.getRefreshToken())
                                                         .put("token_type", "Bearer")
                                                         .put("expires_in", internalCredentials.getLifeTime())
                                                         .put("issue_date", DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault())
                                                                                                                  .format(internalCredentials.getIssueTime().toInstant()));

            if (internalCredentials.getResourceOwner().getRoles().isEmpty()) {
                signupManager.getUsersManager().addRole(internalCredentials.getResourceOwner().getUsername(), Authority.NOT_AUTHORATIVE);
    
                for (String role : signupManager.getDefaultRoles()) {
                    signupManager.getUsersManager().addRole(internalCredentials.getResourceOwner().getUsername(), role);
                }

            }
            response = Response.ok(responsePayload.toString()).build();

        } catch (IllegalArgumentException e) {
            response = Response.status(Status.BAD_REQUEST).entity(toJson(e).toString()).build();
        } catch (UserNotFoundException e) {
            response = Response.status(Status.NOT_FOUND).entity(toJson(e).toString()).build();
        } catch (UserExpiredException e) {
            response = Response.status(Status.FORBIDDEN).entity(toJson(e).toString()).build();
        } catch (PasswordMissmatchException | UnverifiableUserException e) {
            response = Response.status(Status.UNAUTHORIZED).entity(toJson(e).toString()).build();
        } catch (Exception e) {
            LOG.error("GVAPI_Exception - OAuth2 token creation", e);
            response = Response.status(Status.SERVICE_UNAVAILABLE).entity(toJson(e).toString()).build();
        }

        return response;
    }

    private Object toJson(Exception e) {

        return new JSONObject().put("type", e.getClass().getName()).put("message", e.getMessage());
    }

}
