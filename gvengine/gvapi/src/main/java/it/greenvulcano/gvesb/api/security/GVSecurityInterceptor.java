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
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.iam.exception.CredentialsExpiredException;
import it.greenvulcano.gvesb.iam.exception.InvalidCredentialsException;
import it.greenvulcano.gvesb.iam.exception.PasswordMissmatchException;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.modules.SecurityModule;

public class GVSecurityInterceptor extends SoapHeaderInterceptor {

    private final static Logger LOG = LoggerFactory.getLogger(GVSecurityInterceptor.class);

    private List<ServiceReference<SecurityModule>> securityModulesReferences;

    public void setGvSecurityModulesReferences(List<ServiceReference<SecurityModule>> securityModulesReferences) {

        this.securityModulesReferences = securityModulesReferences;
    }

    @Override
    public void handleMessage(Message message) throws Fault {

        AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);
        if (Objects.nonNull(policy) && Objects.nonNull(securityModulesReferences)) {

            for (ServiceReference<SecurityModule> securityModuleRef : securityModulesReferences) {
                try {

                    SecurityModule securityModule = securityModuleRef.getBundle().getBundleContext().getService(securityModuleRef);
                    Optional<SecurityContext> securityContext = securityModule.resolve(policy.getAuthorizationType(), policy.getUserName(), policy.getPassword())
                                                                              .map(GVSecurityContext::new);

                    message.put(SecurityContext.class, securityContext.get());
                    LOG.debug("User authenticated: " + securityContext.get().getUserPrincipal().getName());
                } catch (UserExpiredException | CredentialsExpiredException userExpiredException) {
                    sendErrorResponse(message, 403);

                } catch (PasswordMissmatchException | UserNotFoundException | InvalidCredentialsException unauthorizedException) {
                    LOG.warn("Failed to authenticate user", unauthorizedException);
                    sendErrorResponse(message, 401);

                    // Set the response headers
                    @SuppressWarnings("unchecked")
                    Map<String, List<String>> responseHeaders = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
                    if (responseHeaders != null) {
                        responseHeaders.put("WWW-Authenticate", Arrays.asList(new String[] { policy.getAuthorizationType() }));
                        responseHeaders.put("Content-Length", Arrays.asList(new String[] { "0" }));
                    }

                } catch (Exception e) {
                    LOG.warn("Authentication process failed", e);
                    sendErrorResponse(message, 500);
                }
            }
        } else {
            LOG.debug("AuthorizationPolicy token not found");
        }

    }

    private void sendErrorResponse(Message message, int responseCode) {

        Message outMessage = getOutMessage(message);
        outMessage.put(Message.RESPONSE_CODE, responseCode);

        message.getInterceptorChain().abort();
        try {
            getConduit(message).prepare(outMessage);
            close(outMessage);
        } catch (IOException e) {
            LOG.error("Fail to send message", e);
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
