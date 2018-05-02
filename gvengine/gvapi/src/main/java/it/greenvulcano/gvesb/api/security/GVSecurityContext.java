package it.greenvulcano.gvesb.api.security;

import java.security.Principal;
import org.apache.cxf.common.security.SimplePrincipal;
import org.apache.cxf.security.SecurityContext;

import it.greenvulcano.gvesb.iam.modules.Identity;

public class GVSecurityContext implements SecurityContext {

	private final Principal userPrincipal;
	private final Identity identity;
	
	GVSecurityContext(Identity identity) {
		this.userPrincipal = new SimplePrincipal(identity.getName());
		this.identity = identity;
	}
	
	@Override
	public Principal getUserPrincipal() {			
		return userPrincipal;
	}

	@Override
	public boolean isUserInRole(String role) {			
		return identity.getRoles().contains(role);
	}
			
	public Identity getIdentity(){
		return identity;
	}
	
}
