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
package it.greenvulcano.gvesb.iam.modules.internal;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.iam.domain.Role;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.exception.PasswordMissmatchException;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.modules.Identity;
import it.greenvulcano.gvesb.iam.modules.SecurityModule;
import it.greenvulcano.gvesb.iam.service.UsersManager;

/**
 * 
 * {@link SecurityModule} implementation to handle HTTP Basic authentication
 * 
 * 
 */
public class GVBasicAuthenticationSecurityModule implements SecurityModule {

	private final static Logger LOG = LoggerFactory.getLogger(GVBasicAuthenticationSecurityModule.class);
	
	private UsersManager usersManager;
	
	public void setUsersManager(UsersManager usersManager) {
		this.usersManager = usersManager;
	}
	
	
	@Override
	public Optional<Identity> resolve(String authorization)  throws UserNotFoundException, UserExpiredException, PasswordMissmatchException {
		
		LOG.debug("GVBasicAuthenticationSecurityModule - Check for Basic Authentication: "+authorization);
		
        String[] parts = Optional.ofNullable(authorization).orElse("").split(" ");
        if (parts.length == 2 && "Basic".equalsIgnoreCase(parts[0])) {
        	
        	LOG.debug("GVBasicAuthenticationSecurityModule - Resolving identity");
        	String[] credentials = new String(Base64.getDecoder().decode(parts[1])).split(":");            
    	    		
        	return Optional.of(getIdentity(credentials[0], credentials[1]));
    		    		
        }
        
        LOG.debug("GVBasicAuthenticationSecurityModule - Basic Authentication not found");
		return Optional.empty();
	}


	@Override
	public Optional<Identity> resolve(String type, Map<String, Object> authorization) throws UserNotFoundException, UserExpiredException, PasswordMissmatchException {
		if ("Basic".equalsIgnoreCase(type) && authorization!=null){
			
			return Optional.of(getIdentity((String)authorization.get("username"), (String)authorization.get("password")));
			
		}
		
		return Optional.empty();
	}
	
	private Identity getIdentity(String username, String password) throws UserNotFoundException, UserExpiredException, PasswordMissmatchException {
		User user = usersManager.validateUser(username, password);
		return new Identity(user.getId(), user.getUsername(), user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
		
	}


	@Override
	public Optional<Identity> resolve(String type, String... authorization)	throws UserNotFoundException, UserExpiredException, PasswordMissmatchException {
		if ("Basic".equalsIgnoreCase(type) && authorization!=null && authorization.length>1){
			
			return Optional.of(getIdentity(authorization[0], authorization[1]));
			
		}
		
		return Optional.empty();
	}
	
	

}
