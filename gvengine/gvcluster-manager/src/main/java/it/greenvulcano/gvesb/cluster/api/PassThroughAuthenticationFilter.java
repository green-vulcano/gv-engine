package it.greenvulcano.gvesb.cluster.api;

import java.io.IOException;
import java.util.Optional;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;

import org.apache.cxf.common.security.SimpleSecurityContext;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.security.SecurityContext;

public class PassThroughAuthenticationFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        Optional.ofNullable(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION))
                .ifPresent(a -> JAXRSUtils.getCurrentMessage().put(SecurityContext.class, new SimpleSecurityContext(a)));

    }

}
