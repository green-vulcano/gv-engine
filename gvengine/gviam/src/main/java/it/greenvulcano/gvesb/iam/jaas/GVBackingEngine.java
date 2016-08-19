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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.modules.BackingEngine;
import org.apache.karaf.jaas.modules.Encryption;
import org.apache.karaf.jaas.modules.encryption.EncryptionSupport;
import org.hibernate.exception.ConstraintViolationException;

import it.greenvulcano.gvesb.iam.repository.RoleRepository;
import it.greenvulcano.gvesb.iam.repository.UserRepository;
import it.greenvulcano.gvesb.iam.domain.Role;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.exception.InvalidPasswordException;
import it.greenvulcano.gvesb.iam.exception.InvalidRoleException;
import it.greenvulcano.gvesb.iam.exception.InvalidUsernameException;
import it.greenvulcano.gvesb.iam.exception.UserExistException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;

public class GVBackingEngine implements BackingEngine {

	private UserRepository userRepository;
	private RoleRepository roleRepository;
	private EncryptionSupport encryptionSupport;
	
	public GVBackingEngine(UserRepository userRepository, RoleRepository roleRepository, EncryptionSupport encryptionSupport) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.encryptionSupport = encryptionSupport;
	}
		
	@Override
	public void addUser(String username, String password) {
		
		if (!username.matches(User.USERNAME_PATTERN)) throw new InvalidUsernameException(username);
		if (!password.matches(User.PASSWORD_PATTERN)) throw new InvalidPasswordException(password);
					
		User user = new User();
		user.setUsername(username);
		user.setPassword(getEncryptedPassword(password));
		user.setPasswordTime(new Date());
		user.setEnabled(true);
		user.setExpired(true);
		
		try {
			userRepository.add(user);
		} catch (org.hibernate.StaleObjectStateException|ConstraintViolationException constraintViolationException) {
			throw new UserExistException(username);
		}
	}

	@Override
	public void deleteUser(String username) {
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
		userRepository.remove(user);
	}	

	@Override
	public void addRole(String username, String rolename) {
		if (!rolename.matches(Role.ROLE_PATTERN)) throw new InvalidRoleException(rolename);
		
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
		
		Role role = roleRepository.get(username).orElse(new Role(rolename, "Created by JAAS"));
		user.getRoles().add(role);
		
		userRepository.add(user);

	}

	@Override
	public void deleteRole(String username, String rolename) {
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
		roleRepository.get(rolename).ifPresent(user.getRoles()::remove);
				
		userRepository.add(user);

	}	

	@Override
	public List<UserPrincipal> listUsers() {
		
		return userRepository.getAll().stream()
							 .map(User::getUsername)
							 .map(UserPrincipal::new)
							 .collect(Collectors.toList());
	}	

	@Override
	public List<RolePrincipal> listRoles(Principal principal) {		
		return userRepository.get(principal.getName()).orElseThrow(()->new UserNotFoundException(principal.getName()))
							 .getRoles().stream()
							 			.map(Role::getName)
							 			.map(RolePrincipal::new)
							 			.collect(Collectors.toList());
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
        Encryption encryption = encryptionSupport.getEncryption();
        String encryptionPrefix = encryptionSupport.getEncryptionPrefix();
        String encryptionSuffix = encryptionSupport.getEncryptionSuffix();

        if (encryption == null) {
            return password;
        } else {
            boolean prefix = encryptionPrefix == null || password.startsWith(encryptionPrefix);
            boolean suffix = encryptionSuffix == null || password.endsWith(encryptionSuffix);
            if (prefix && suffix) {
                return password;
            } else {
                String p = encryption.encryptPassword(password);
                if (encryptionPrefix != null) {
                    p = encryptionPrefix + p;
                }
                if (encryptionSuffix != null) {
                    p = p + encryptionSuffix;
                }
                return p;
            }
        }
	}

}
