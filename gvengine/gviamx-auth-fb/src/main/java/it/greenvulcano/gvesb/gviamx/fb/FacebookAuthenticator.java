package it.greenvulcano.gvesb.gviamx.fb;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;

import it.greenvulcano.gvesb.iam.exception.UnverifiableUserException;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.service.ExternalAuthenticationService;
import it.greenvulcano.gvesb.iam.service.ExternalAutheticatedUser;

public class FacebookAuthenticator implements ExternalAuthenticationService {

    private final static Logger LOG = LoggerFactory.getLogger(FacebookAuthenticator.class);

    private String endpoint;

    public void setEndpoint(String endpoint) {

        this.endpoint = endpoint;
    }

    @Override
    public String getProviderID() {

        return "facebook";
    }

    @Override
    public ExternalAutheticatedUser authenticate(String credentials) throws UserNotFoundException, UserExpiredException, UnverifiableUserException {

        ExternalAutheticatedUser user = new ExternalAutheticatedUser();
        
        String facebookAPIUrl = endpoint.concat("/me?fields=id,name,email");
        int responseCode = -1;
        
        LOG.info("Validating credentials on endpoint {}",  facebookAPIUrl);
        try {
            URL fbURL = new URL(facebookAPIUrl +"&access_token="+credentials);
    
            LOG.debug("Opening connection to on endpoint {}",  fbURL.toString());
            HttpsURLConnection fbURLConnection = (HttpsURLConnection) fbURL.openConnection();
    
            fbURLConnection.setRequestMethod("GET");
            fbURLConnection.setConnectTimeout(30000);
            fbURLConnection.setReadTimeout(10000);
    
            fbURLConnection.connect();
    
            InputStream responseStream = null;
            
            try {
                responseCode = fbURLConnection.getResponseCode();
                responseStream = fbURLConnection.getInputStream();
            } catch (IOException connectionFail) {
                responseStream = fbURLConnection.getErrorStream();
                responseCode = fbURLConnection.getResponseCode();
            }
       
            String response = new String(responseStream.readAllBytes(), "UTF-8");
                
            if (responseCode==200) {
                                               
                JSONObject responseObject = new JSONObject(response);                
                user.setAuthenticationInfo(responseObject);                
                user.setEmail(responseObject.getString("email"));
                                
                LOG.info("Credentials validation OK ");
                LOG.debug("Credentials validation response: "+response);
                
            } else {                
                throw new RuntimeException("Autentication failed - code= "+responseCode
                                                               + " ; message= "+response);
            }
    
            fbURLConnection.disconnect();
             
        } catch (RuntimeException e) {
            LOG.warn("Credentials validation fails", e);
            throw  new UserNotFoundException(credentials);
            
        } catch (Exception e) {
            LOG.error("Failed to perform credentials validation", e);
            throw new UnverifiableUserException(credentials, e);
        }
        
        return user;
    }

}
