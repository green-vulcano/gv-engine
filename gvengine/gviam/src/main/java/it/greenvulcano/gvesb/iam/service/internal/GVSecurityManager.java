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
package it.greenvulcano.gvesb.iam.service.internal;

import java.util.Date;
import java.util.Set;

import it.greenvulcano.gvesb.iam.domain.Role;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.domain.UserInfo;
import it.greenvulcano.gvesb.iam.exception.InvalidPasswordException;
import it.greenvulcano.gvesb.iam.exception.InvalidUsernameException;
import it.greenvulcano.gvesb.iam.exception.PasswordMissmatchException;
import it.greenvulcano.gvesb.iam.exception.UserExistException;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.jaas.GVBackingEngine;
import it.greenvulcano.gvesb.iam.jaas.GVBackingEngineFactory;
import it.greenvulcano.gvesb.iam.repository.RoleRepository;
import it.greenvulcano.gvesb.iam.repository.UserRepository;
import it.greenvulcano.gvesb.iam.service.SecurityManager;

public class GVSecurityManager implements SecurityManager {

	private GVBackingEngine jaasEngine;
	private UserRepository userRepository;
	private RoleRepository roleRepository;
	
	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	public void setRoleRepository(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}
	
	public void setJaasEngineFactory(GVBackingEngineFactory gvBackingEngineFactory) {
		this.jaasEngine = (GVBackingEngine) gvBackingEngineFactory.build();
	}
	
	@Override
	public User createUser(String username, String password)
			throws InvalidUsernameException, InvalidPasswordException, UserExistException {		
		jaasEngine.addUser(username, password);
		
		return userRepository.get(username).get();
	}

	@Override
	public void saveUserInfo(String username, UserInfo userInfo) throws UserNotFoundException {
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
		user.setUserInfo(userInfo);
		
		userRepository.add(user);
	}

	@Override
	public User getUser(String username) throws UserNotFoundException {
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
		return user;
	}

	@Override
	public void deleteUser(String username) {
		userRepository.get(username).ifPresent(userRepository::remove);
	}

	@Override
	public void resetUserPassword(String username) throws UserNotFoundException {
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
		
		user.setPassword(jaasEngine.getEncryptedPassword(username));
		user.setPasswordTime(new Date());
		user.setExpired(true);

	}

	@Override
	public void changeUserPassword(String username, String oldPassword, String newPassword)
			throws UserNotFoundException, PasswordMissmatchException {
		
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
		if (!newPassword.matches(User.PASSWORD_PATTERN)) throw new InvalidPasswordException(newPassword);
				
		user.setUsername(username);
		user.setPassword(jaasEngine.getEncryptedPassword(newPassword));
		user.setPasswordTime(new Date());
		user.setExpired(false);
		
		userRepository.add(user);

	}

	@Override
	public User validateUser(String username, String password)
			throws UserNotFoundException, PasswordMissmatchException {
		
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
		if(user.getPassword().equals(jaasEngine.getEncryptedPassword(password))) {
			if (user.isExpired()) {
				throw new UserExpiredException(username);				
			} else if (!user.isEnabled()) {
				throw new UserNotFoundException(username);
			}
		} else {
			throw new PasswordMissmatchException(username);
		}
		
		return user;

	}

	@Override
	public void addRole(String username, Role role) throws UserNotFoundException {
		jaasEngine.addRole(username, role.getName());

	}

	@Override
	public void revokeRole(String username, String roleName) throws UserNotFoundException {
		jaasEngine.deleteRole(username, roleName);

	}

	@Override
	public void deleteRole(String roleName) {		
		roleRepository.get(roleName).ifPresent(roleRepository::remove);
		
	}

	@Override
	public Set<Role> getRoles() {		
		return roleRepository.getAll();
	}

	@Override
	public Set<User> getUsers() {		
		return userRepository.getAll();
	}

	@Override
	public void enableUser(String username, boolean enable) throws UserNotFoundException {
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
		user.setEnabled(enable);
		userRepository.add(user);
	}

}
