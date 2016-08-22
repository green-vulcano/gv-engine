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

import java.util.Set;

import it.greenvulcano.gvesb.iam.domain.Role;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.domain.UserInfo;
import it.greenvulcano.gvesb.iam.exception.InvalidPasswordException;
import it.greenvulcano.gvesb.iam.exception.InvalidRoleException;
import it.greenvulcano.gvesb.iam.exception.InvalidUsernameException;
import it.greenvulcano.gvesb.iam.exception.PasswordMissmatchException;
import it.greenvulcano.gvesb.iam.exception.UserExistException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;

public interface SecurityManager {
	
		
		void checkManagementRequirements();
	
		User createUser(String username, String password) throws InvalidUsernameException, InvalidPasswordException, UserExistException;
		
		Role createRole(String name, String description) throws InvalidRoleException;
		
		User getUser(String username) throws UserNotFoundException;
		
		User validateUser(String username, String password) throws UserNotFoundException, PasswordMissmatchException;
		
		Set<User> getUsers();
		
		Set<Role> getRoles();
				
		void saveUserInfo(String username, UserInfo userInfo) throws UserNotFoundException;
			
		User enableUser(String username, boolean enable) throws UserNotFoundException;		
		
		User switchUserStatus(String username) throws UserNotFoundException;
		
		User resetUserPassword(String username) throws UserNotFoundException;
		
		User changeUserPassword(String username, String oldPassword, String newPassword) throws UserNotFoundException, PasswordMissmatchException;
		
		void addRole(String username, Role role) throws UserNotFoundException, InvalidRoleException;
		
		void revokeRole(String username, String roleName) throws UserNotFoundException;
		
		void deleteUser(String username);
			
		void deleteRole(String roleName);		

}
