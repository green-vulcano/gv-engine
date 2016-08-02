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
import java.security.Principal;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

import org.apache.cxf.common.security.SimplePrincipal;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.security.SecurityContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.iam.domain.Role;
import it.greenvulcano.gvesb.iam.service.SecurityManager;

public class GVSecurityFilter implements ContainerRequestFilter {
	
	private final static Logger LOG = LoggerFactory.getLogger(GVSecurityFilter.class);

	private List<ServiceReference<SecurityManager>> securityManagerReferences;
	
	public void setGvSecurityManagerReferences(List<ServiceReference<SecurityManager>> securityManagerReferences) {
		this.securityManagerReferences = securityManagerReferences;		
	}
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		
		Optional<ServiceReference<SecurityManager>> securityManagerRef = securityManagerReferences==null ? Optional.empty() :
																	     securityManagerReferences.stream().findAny();
			
		if (securityManagerRef.isPresent()) {
			BundleContext context = securityManagerRef.get().getBundle().getBundleContext();
			SecurityManager securityManager = (SecurityManager) context.getService(securityManagerRef.get());
			LOG.debug("SecurityManager found, handling authentication");
			
			String authorization = Optional.ofNullable(requestContext.getHeaderString("Authorization")).orElse("");
	        String[] parts = authorization.split(" ");
	        if (parts.length == 2 && "Basic".equals(parts[0])) {
	        	try {
	        		String[] credentials = new String(Base64.getDecoder().decode(parts[1])).split(":");
	        
	        		securityManager.validateUser(credentials[0], credentials[1]);        		       		
	        		SecurityContext securityContext = new GVSecurityContext(credentials[0], securityManager.getUserRoles(credentials[0]));
	        		        		
	        		JAXRSUtils.getCurrentMessage().put(SecurityContext.class, securityContext);
	        		LOG.debug("User authenticated");
	        	} catch (Exception e) {
	        		LOG.warn("Authentication process failed", e);
	        		requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
				}
	        }  else {
	        	LOG.debug("Basic authentication token not found");
	        }
		} else {
			LOG.debug("SecurityManager not available");
		}
	}
	
	private class GVSecurityContext implements SecurityContext {

		private final Principal userPrincipal;
		private final Set<Role> roles = new HashSet<>();
		
		GVSecurityContext(String username, Set<Role> roles) {
			this.userPrincipal = new SimplePrincipal(username);
			this.roles.addAll(roles);
		}
		
		@Override
		public Principal getUserPrincipal() {			
			return userPrincipal;
		}

		@Override
		public boolean isUserInRole(String role) {			
			return roles.stream().anyMatch(r -> r.getName().equals(role));
		}
		
	}

}
