package it.greenvulcano.gvesb.api.security;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.cxf.binding.soap.interceptor.SoapHeaderInterceptor;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.transport.Conduit;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.service.SecurityManager;

public class GVSecurityInterceptor extends SoapHeaderInterceptor {
	private final static Logger LOG = LoggerFactory.getLogger(GVSecurityInterceptor.class);
	
	private List<ServiceReference<SecurityManager>> securityManagerReferences;
	
	public void setGvSecurityManagerReferences(List<ServiceReference<SecurityManager>> securityManagerReferences) {
		this.securityManagerReferences = securityManagerReferences;		
	}
	
	@Override
	public void handleMessage(Message message) throws Fault {
	
		Optional<ServiceReference<SecurityManager>> securityManagerRef = securityManagerReferences==null ? Optional.empty() :
		     securityManagerReferences.stream().findAny();

		AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);
		
		if (securityManagerRef.isPresent()) {
						
			BundleContext context = securityManagerRef.get().getBundle().getBundleContext();
			SecurityManager securityManager = (SecurityManager) context.getService(securityManagerRef.get());
			LOG.debug("SecurityManager found, handling authentication");
			
			if (Objects.nonNull(policy) && "Basic".equalsIgnoreCase(policy.getAuthorizationType())) {
				try {
					
					User user = securityManager.validateUser(policy.getUserName(), policy.getPassword());        		       		
					SecurityContext securityContext = new GVSecurityContext(user.getUsername(), user.getRoles());
					
					message.put(SecurityContext.class, securityContext);
					LOG.debug("User authenticated: "+securityContext.getUserPrincipal().getName());
				} catch (UserExpiredException userExpiredException) {	        		
					sendErrorResponse(message, 403);
										        	
				} catch (Exception e) {
					LOG.warn("Authentication process failed", e);
					sendErrorResponse(message, 401);
				}
			}  else {
				LOG.debug("Basic authentication token not found");
			}
		} else {
			LOG.debug("SecurityManager not available");
			if(Objects.nonNull(policy)) {
				LOG.error("No SecurityManager available to handle authentication");
				sendErrorResponse(message, 401);
			}
		}		
		
	}
	
	private void sendErrorResponse(Message message, int responseCode) {
        Message outMessage = getOutMessage(message);
        outMessage.put(Message.RESPONSE_CODE, responseCode);
        
        // Set the response headers
        @SuppressWarnings("unchecked")
		Map<String, List<String>> responseHeaders =
            (Map<String, List<String>>)message.get(Message.PROTOCOL_HEADERS);
        if (responseHeaders != null) {
            responseHeaders.put("WWW-Authenticate", Arrays.asList(new String[]{"Basic realm=gvesb"}));
            responseHeaders.put("Content-Length", Arrays.asList(new String[]{"0"}));
        }
        message.getInterceptorChain().abort();
        try {
            getConduit(message).prepare(outMessage);
            close(outMessage);
        } catch (IOException e) {
            LOG.warn(e.getMessage(), e);
        }
    }
    
    private Message getOutMessage(Message inMessage) {
        Exchange exchange = inMessage.getExchange();
        Message outMessage = exchange.getOutMessage();
        if (outMessage == null) {
            Endpoint endpoint = exchange.get(Endpoint.class);
            outMessage = endpoint.getBinding().createMessage();
            exchange.setOutMessage(outMessage);
        }
        outMessage.putAll(inMessage);
        return outMessage;
    }
    
    private Conduit getConduit(Message inMessage) throws IOException {
        Exchange exchange = inMessage.getExchange();     
        Conduit conduit = exchange.getDestination().getBackChannel(inMessage);        
        exchange.setConduit(conduit);
        return conduit;
    }
    
    private void close(Message outMessage) throws IOException {
        OutputStream os = outMessage.getContent(OutputStream.class);
        os.flush();
        os.close();
    }
	
	

}
