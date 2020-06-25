package it.greenvulcano.gvesb.api.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;


@Priority(Priorities.HEADER_DECORATOR)
public class CorrelationFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        Optional<String> requestId = Optional.ofNullable(requestContext.getHeaderString("X-Request-ID"));
        requestId.ifPresent(id -> responseContext.getHeaders().put("X-Request-ID", Arrays.asList(id)));

    }

}
