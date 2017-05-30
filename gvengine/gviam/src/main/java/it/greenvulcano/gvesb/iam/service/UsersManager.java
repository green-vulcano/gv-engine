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
package it.greenvulcano.gvesb.iam.service;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import it.greenvulcano.gvesb.iam.domain.Role;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.domain.UserInfo;
import it.greenvulcano.gvesb.iam.exception.InvalidPasswordException;
import it.greenvulcano.gvesb.iam.exception.InvalidRoleException;
import it.greenvulcano.gvesb.iam.exception.InvalidUsernameException;
import it.greenvulcano.gvesb.iam.exception.PasswordMissmatchException;
import it.greenvulcano.gvesb.iam.exception.UserExistException;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;

/**
 * 
 * Manage sucurity-related business operations supporting subject direct interaction
 * 
 *   (Yes, it is for login and user management)
 * 
 */
public interface UsersManager {
	
		public final static class Authority {
				
			public final static String ADMINISTRATOR = "gvadmin";
			public final static String MANAGER = "gvmanager_account";
			public final static String CLIENT = "gvclient_account";
			public final static String GUEST = "gvguest_account";
			
			public final static Set<String> entries;
			
			static {
				Set<String> e = new LinkedHashSet<>();
				e.add(ADMINISTRATOR);
				e.add(MANAGER);
				e.add(CLIENT);
				e.add(GUEST);
				
				entries = Collections.unmodifiableSet(e);
			}
			
		}	
	
		void checkManagementRequirements();
	
		User createUser(String username, String password) throws InvalidUsernameException, InvalidPasswordException, UserExistException;
		
		Role createRole(String name, String description) throws InvalidRoleException;
		
		User getUser(Integer id) throws UserNotFoundException;
		
		User getUser(String username) throws UserNotFoundException;
		
		User validateUser(String username, String password) throws UserNotFoundException, UserExpiredException, PasswordMissmatchException;
		
<<<<<<< HEAD
		Set<User> getUsers();
=======
		void addRole(String username, String role) throws InvalidRoleException, UserNotFoundException;
		
		void revokeRole(String username, String role) throws UserNotFoundException;
>>>>>>> 556629a... Improved role management
		
		Set<User> getUsers(String fullname, String email, Boolean enabled, Boolean expired, String role);
		
		Set<Role> getRoles();
				
		void updateUser(String username, UserInfo userInfo, Set<Role> grantedRoles, boolean enabled, boolean expired) throws UserNotFoundException, InvalidRoleException;
			
		User enableUser(String username, boolean enable) throws UserNotFoundException;		
		
		User switchUserStatus(String username) throws UserNotFoundException;
		
		User setUserExpiration(String username, boolean expired) throws UserNotFoundException;
		
		User resetUserPassword(String username, String defaultPassword) throws UserNotFoundException, InvalidPasswordException;
		
		User changeUserPassword(String username, String oldPassword, String newPassword) throws UserNotFoundException, PasswordMissmatchException, InvalidPasswordException;
						
		void deleteUser(String username);
			
		void deleteRole(String roleName);		

}
