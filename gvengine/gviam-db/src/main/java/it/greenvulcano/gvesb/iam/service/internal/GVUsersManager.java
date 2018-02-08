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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.iam.domain.Role;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.domain.UserInfo;
import it.greenvulcano.gvesb.iam.domain.jpa.RoleJPA;
import it.greenvulcano.gvesb.iam.domain.jpa.UserJPA;
import it.greenvulcano.gvesb.iam.exception.GVSecurityException;
import it.greenvulcano.gvesb.iam.exception.InvalidPasswordException;
import it.greenvulcano.gvesb.iam.exception.InvalidRoleException;
import it.greenvulcano.gvesb.iam.exception.InvalidUsernameException;
import it.greenvulcano.gvesb.iam.exception.PasswordMissmatchException;
import it.greenvulcano.gvesb.iam.exception.UnverifiableUserException;
import it.greenvulcano.gvesb.iam.exception.UserExistException;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.repository.hibernate.RoleRepositoryHibernate;
import it.greenvulcano.gvesb.iam.repository.hibernate.UserRepositoryHibernate;
import it.greenvulcano.gvesb.iam.service.SearchCriteria;
import it.greenvulcano.gvesb.iam.service.SearchResult;
import it.greenvulcano.gvesb.iam.service.UsersManager;

public class GVUsersManager implements UsersManager{
	
	private UserRepositoryHibernate userRepository;
	private RoleRepositoryHibernate roleRepository;
	
	
	public void setUserRepository(UserRepositoryHibernate userRepository) {
		this.userRepository = userRepository;
	}
	
	public void setRoleRepository(RoleRepositoryHibernate roleRepository) {
		this.roleRepository = roleRepository;
	}
			
	@Override
	public User createUser(String username, String password)
			throws InvalidUsernameException, InvalidPasswordException, UserExistException {		
		if (!username.matches(User.USERNAME_PATTERN)) throwException(new InvalidUsernameException(username));
		if (!password.matches(User.PASSWORD_PATTERN)) throwException(new InvalidPasswordException(password));
					
		UserJPA user = new UserJPA();		
		user.setUsername(username);
		user.setPassword(DigestUtils.sha256Hex(password));
		user.setPasswordTime(new Date());
		user.setEnabled(true);
				
		try {
			userRepository.add(user);
		} catch (org.hibernate.StaleObjectStateException|ConstraintViolationException constraintViolationException) {			
			throwException(new UserExistException(username));
		}
		
		return userRepository.get(username).get();
	}
	
	@Override
	public Role createRole(String name, String description) throws InvalidRoleException {
		if (!name.matches(Role.ROLE_PATTERN)) throw new InvalidRoleException(name);
		
		Role role = new RoleJPA(name, description);
		roleRepository.add(role);
		return role;
	}
	

	@Override
	public void updateUser(String username, UserInfo userInfo, Set<Role> grantedRoles, boolean enabled, boolean expired) throws UserNotFoundException, InvalidRoleException {
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
		user.setUserInfo(userInfo);
		user.setEnabled(enabled);
		user.setExpired(expired);
		user.clearRoles();
		if (grantedRoles!=null){
			
			Predicate<Role> roleIsValid = role-> Optional.ofNullable(role.getName()).orElse("").matches(Role.ROLE_PATTERN);
			
			Optional<Role> notValidRole = grantedRoles.stream().filter(roleIsValid.negate()).findAny();
			if (notValidRole.isPresent()){
				throw new InvalidRoleException(notValidRole.get().getName());
			}
			
			for (Role r : grantedRoles) {
				user.addRole(roleRepository.get(r.getName()).orElse(r));
			}
			
		}
		userRepository.add(user);
	}

	@Override
	public User getUser(Long id) throws UserNotFoundException {
		User user = userRepository.get(id).orElseThrow(()->new UserNotFoundException(id.toString()));
		return user;
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
	public User resetUserPassword(String username, String defaultPassword) throws UserNotFoundException, InvalidPasswordException, UnverifiableUserException {
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
	
		if (!Objects.requireNonNull(defaultPassword, "A default password is required").matches(User.PASSWORD_PATTERN)) throw new InvalidPasswordException(defaultPassword);		
		if (!user.getPassword().isPresent()) throw new UnverifiableUserException(username);
		
		user.setPassword(DigestUtils.sha256Hex(defaultPassword));
		user.setPasswordTime(new Date());
		user.setExpired(true);
		userRepository.add(user);
		return user;

	}

	@Override
	public User changeUserPassword(String username, String oldPassword, String newPassword)
			throws UserNotFoundException, PasswordMissmatchException, InvalidPasswordException, UnverifiableUserException {
		
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
		if (!newPassword.matches(User.PASSWORD_PATTERN)) throw new InvalidPasswordException(newPassword);		
		if (!user.getPassword().isPresent()) throw new UnverifiableUserException(username);			
		
		
		user.setUsername(username);
		user.setPassword(DigestUtils.sha256Hex(newPassword));
		user.setPasswordTime(new Date());
		user.setExpired(false);
		
		userRepository.add(user);
		
		return user;
	}

	@Override
	public User validateUser(String username, String password)
			throws UserNotFoundException, PasswordMissmatchException, UserExpiredException, UnverifiableUserException {
		
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
		if(user.getPassword().orElseThrow(() -> new UnverifiableUserException(username)).equals(DigestUtils.sha256Hex(password))) {
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
	public void deleteRole(String roleName) {		
		roleRepository.get(roleName).ifPresent(roleRepository::remove);
		
	}

	@Override
	public Set<Role> getRoles() {		
		return roleRepository.getAll();
	}
	
	@Override
	public Role getRole(String name) {
		Optional<Role> role  = roleRepository.get(name);		
		if (role.isPresent()) {
			return role.get();
		}
		return null;
	}
	
	public Set<User> getUsers() {		
		return userRepository.getAll();
	}
	
	@Override
	public SearchResult searchUsers(SearchCriteria criteria) {	
		
		SearchResult result = new SearchResult();
		
		Map<UserRepositoryHibernate.Parameter, Object> parameters = criteria.getParameters()
				                                    .keySet().stream()
				                                    .map(UserRepositoryHibernate.Parameter::valueOf)
				                                    .filter(Objects::nonNull)
				                                    .collect(Collectors.toMap(Function.identity(), k->criteria.getParameters().get(k.name())));
		
		LinkedHashMap<UserRepositoryHibernate.Parameter, UserRepositoryHibernate.Order> order = new LinkedHashMap<>(criteria.getOrder().size());
		for (Entry<String, String>  e : criteria.getOrder().entrySet()) {
			Optional.ofNullable(UserRepositoryHibernate.Parameter.get(e.getKey())).ifPresent(p-> {
				order.put(p, UserRepositoryHibernate.Order.get(e.getValue()));
			});
		}
		
		result.setTotalCount(userRepository.count(parameters));
		if (criteria.getOffset()>result.getTotalCount()) {
			result.setFounds(new HashSet<>());
		} else {
			result.setFounds(userRepository.find(parameters, order, criteria.getOffset(), criteria.getLimit() ));
		}
		
		
		return result;
	}

	@Override
	public User enableUser(String username, boolean enable) throws UserNotFoundException {
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
		user.setEnabled(enable);
		userRepository.add(user);
		
		return user;
	}
	
	@Override
	public User setUserExpiration(String username, boolean expired) throws UserNotFoundException {
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
		user.setExpired(expired);
		userRepository.add(user);
		
		return user;
	}
	
	@Override
	public User switchUserStatus(String username) throws UserNotFoundException {
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
		user.setEnabled(!user.isEnabled());
		userRepository.add(user);
		
		return user;
	}

	@Override
	public void checkManagementRequirements() {
		final Logger logger = LoggerFactory.getLogger(getClass());
		
		Map<UserRepositoryHibernate.Parameter, Object> parameters = new HashMap<>(2);
		parameters.put(UserRepositoryHibernate.Parameter.role, Authority.ADMINISTRATOR);
		parameters.put(UserRepositoryHibernate.Parameter.enabled, Boolean.TRUE);
		
		int admins = userRepository.count(parameters);
		/**
		 * Adding default user 'gvadmin' if no present		
		 */
		if (admins==0) {
			logger.info("Creating a default 'gvadmin'");
			User admin;
			try {
				createUser("gvadmin", "gvadmin");				
			} catch (SecurityException | InvalidUsernameException | InvalidPasswordException | UserExistException e) {				
				logger.info("A user named 'gvadmin' exist: restoring his default settings", e);
				
				admin = userRepository.get("gvadmin").get();
				admin.setPassword(DigestUtils.sha256Hex("gvadmin"));
				admin.setPasswordTime(new Date());
				admin.setExpired(true);
				userRepository.add(admin);
				
			}
			
			admin = userRepository.get("gvadmin").get();
			admin.setEnabled(true);
			admin.clearRoles();
			admin.addRole(new RoleJPA(Authority.ADMINISTRATOR, "Created by GV"));

			//roles required to use karaf 
			admin.addRole(new RoleJPA("admin", "Created by GV"));
			admin.addRole(new RoleJPA("manager", "Created by GV"));
			admin.addRole(new RoleJPA("viewer", "Created by GV"));
			admin.addRole(new RoleJPA("systembundles", "Created by GV"));
						
			userRepository.add(admin);
		}	
		
	}

	@Override
	public void revokeRole(String username, String role) throws UserNotFoundException {
		try {
			User user = userRepository.get(username).orElseThrow(()->new SecurityException(new UserNotFoundException(username)));
			user.removeRole(role);				
			userRepository.add(user);
		} catch (SecurityException e) {
			if (e.getCause() instanceof UserNotFoundException) {
				throw (UserNotFoundException) e.getCause();
			} else {
				throw e;
		
			}
		}
	}

	@Override
	public void updateUsername(String username, String newUsername) throws UserNotFoundException, InvalidUsernameException {
		
		User user = userRepository.get(username).orElseThrow(()->new UserNotFoundException(username));
		if (username.matches(User.USERNAME_PATTERN)) {
			user.setUsername(newUsername);
		} else {
			throw new InvalidUsernameException(newUsername);
		}		
		
		userRepository.add(user);
	}

	private void throwException(GVSecurityException cause){
		throw new SecurityException(cause);
	}	

	@Override
	public void addRole(String username, String rolename) {
		if (!rolename.matches(Role.ROLE_PATTERN)) throwException(new InvalidRoleException(rolename));
		
		User user = userRepository.get(username).orElseThrow(()->new SecurityException(new UserNotFoundException(username)));
		
		Role role = roleRepository.get(rolename).orElse(new RoleJPA(rolename, "Created by JAAS"));
		user.addRole(role);
		
		userRepository.add(user);

	}	

}
