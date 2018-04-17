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
package it.greenvulcano.gvesb.iam.jaas;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.modules.BackingEngine;
import org.apache.karaf.jaas.modules.BackingEngineFactory;
import it.greenvulcano.gvesb.iam.domain.Role;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.exception.GVSecurityException;
import it.greenvulcano.gvesb.iam.exception.InvalidPasswordException;
import it.greenvulcano.gvesb.iam.exception.InvalidRoleException;
import it.greenvulcano.gvesb.iam.exception.InvalidUsernameException;
import it.greenvulcano.gvesb.iam.exception.UserExistException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;

import it.greenvulcano.gvesb.iam.service.SearchCriteria;
import it.greenvulcano.gvesb.iam.service.UsersManager;

public class GVBackingEngine implements BackingEngine, BackingEngineFactory {
	
	private UsersManager userManager;
	
	public void setUserManager(UsersManager userManager) {
		this.userManager = userManager;
	}

	private void throwException(GVSecurityException cause){
		throw new SecurityException(cause);
	}
		
	@Override
	public void addUser(String username, String password) {
		
		if (!username.matches(User.USERNAME_PATTERN)) throwException(new InvalidUsernameException(username));
		if (!password.matches(User.PASSWORD_PATTERN)) throwException(new InvalidPasswordException(password));
					
						
		try {
			userManager.createUser(username, password);
		} catch (UserExistException | InvalidUsernameException | InvalidPasswordException constraintViolationException) {			
			throwException(constraintViolationException);
		}
	}

	@Override
	public void addRole(String username, String rolename) {
		if (!rolename.matches(Role.ROLE_PATTERN)) throwException(new InvalidRoleException(rolename));
		try {
			User user = userManager.getUser(username);
			
			Role role = Optional.ofNullable(userManager.getRole(rolename)).orElse(userManager.createRole(rolename, "Created by JAAS"));
			user.addRole(role);
			
			userManager.updateUser(username, user.getUserInfo(), user.getRoles(), user.isEnabled(), user.isExpired());
		} catch ( UserNotFoundException | InvalidRoleException constraintViolationException) {			
			throwException(constraintViolationException);
		}	

	}
	
	@Override
	public void deleteUser(String username) {
		userManager.deleteUser(username);		
	}

	@Override
	public void deleteRole(String username, String rolename) {
		try {
			User user = userManager.getUser(username);			
			user.removeRole(rolename);
			
			userManager.updateUser(username, user.getUserInfo(), user.getRoles(), user.isEnabled(), user.isExpired());
		} catch ( UserNotFoundException | InvalidRoleException constraintViolationException) {			
			throwException(constraintViolationException);
		}

	}	

	@Override
	public List<UserPrincipal> listUsers() {
		
		return userManager.searchUsers(SearchCriteria.builder().build()).getFounds().stream()
							 .map(User::getUsername)
							 .map(UserPrincipal::new)
							 .collect(Collectors.toList());
	}	

	@Override
	public List<RolePrincipal> listRoles(Principal principal) {
		try {
			return  userManager.getUser(principal.getName())
								 .getRoles().stream()
								 			.map(Role::getName)
								 			.map(RolePrincipal::new)
								 			.collect(Collectors.toList());
		} catch ( UserNotFoundException constraintViolationException) {			
			throwException(constraintViolationException);
		}
		
		return null;
	}
	
	@Override
	public void addGroup(String username, String group) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void createGroup(String group) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void deleteGroup(String username, String group) {
		throw new UnsupportedOperationException();

	}
	
	@Override
	public void addGroupRole(String group, String role) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void deleteGroupRole(String group, String role) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public List<GroupPrincipal> listGroups(UserPrincipal user) {
		return new ArrayList<>();
	}

	@Override
	public Map<GroupPrincipal, String> listGroups() {
		return new HashMap<>();
	}
	
	public String getEncryptedPassword(String password) {
		return DigestUtils.sha256Hex(password);
	}

	@Override
	public String getModuleClass() {
		return GVLoginModule.class.getName();
	}

	@Override
	public BackingEngine build(Map<String, ?> options) {		
		return this;
	}

		

}
