/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.api.security;

import java.io.IOException;

import java.util.List;
import java.util.Optional;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.security.SecurityContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.iam.exception.CredentialsExpiredException;
import it.greenvulcano.gvesb.iam.exception.InvalidCredentialsException;
import it.greenvulcano.gvesb.iam.exception.PasswordMissmatchException;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.modules.SecurityModule;

@Priority(Priorities.AUTHENTICATION)
public class GVSecurityFilter implements ContainerRequestFilter {
	
	private final static Logger LOG = LoggerFactory.getLogger(GVSecurityFilter.class);

	private List<ServiceReference<SecurityModule>> securityModulesReferences;
	
	public void setGvSecurityModulesReferences(List<ServiceReference<SecurityModule>> securityModulesReferences) {
		this.securityModulesReferences = securityModulesReferences;		
	}
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		
		String authorization = Optional.ofNullable(requestContext.getHeaderString("Authorization")).orElse("");
		
		if (securityModulesReferences!=null && !securityModulesReferences.isEmpty()) {				
			LOG.debug("SecurityManager found, handling authentication");
			
			String wwwAuthenticate = "unknown";
			Optional<String> xRequestedWith = Optional.ofNullable(requestContext.getHeaderString("X-Requested-With"));
			try {
				
				for (ServiceReference<SecurityModule> securityModuleRef :  securityModulesReferences) {
					
					SecurityModule securityModule = securityModuleRef.getBundle().getBundleContext().getService(securityModuleRef);
					wwwAuthenticate = securityModule.getSchema() + " realm="+ securityModule.getRealm();
					if (xRequestedWith.isPresent()) {
						wwwAuthenticate = xRequestedWith.get() + "+" + wwwAuthenticate;
					}
					
					Optional<SecurityContext> securityContext = securityModule.resolve(authorization).map(GVSecurityContext::new);
					
					if (securityContext.isPresent()) {
						
		        		JAXRSUtils.getCurrentMessage().put(SecurityContext.class, securityContext.get());
		        		LOG.debug("User authenticated: "+securityContext.get().getUserPrincipal().getName());
		        		
		        		break;
					}					
					
				}
			} catch (UserExpiredException|CredentialsExpiredException userExpiredException) {
				
				Response expiredResponse = Response.status(Response.Status.UNAUTHORIZED)
						                 .header("X-Auth-Status", "Expired")
						                 .header("WWW-Authenticate", wwwAuthenticate)
						                 .entity("Credentials expired").build();
				
        		requestContext.abortWith(expiredResponse);
			} catch (PasswordMissmatchException|UserNotFoundException|InvalidCredentialsException unauthorizedException){
				
				Response errorResponse = Response.status(Response.Status.UNAUTHORIZED)
		                 .header("X-Auth-Status", "Denied")
		                 .header("WWW-Authenticate", wwwAuthenticate)
		                 .entity("Access denied").build();

				LOG.warn("Failed to authenticate user", unauthorizedException);
				requestContext.abortWith(errorResponse);
				
        	} catch (Exception e) {
        		LOG.warn("Authentication process failed", e);
        		requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
			}
		}
		
	}	
	

}
