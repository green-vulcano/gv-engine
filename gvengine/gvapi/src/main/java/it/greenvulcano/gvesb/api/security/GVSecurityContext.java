package it.greenvulcano.gvesb.api.security;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import org.apache.cxf.common.security.SimplePrincipal;
import org.apache.cxf.security.SecurityContext;

import it.greenvulcano.gvesb.iam.domain.Role;

public class GVSecurityContext implements SecurityContext {

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
