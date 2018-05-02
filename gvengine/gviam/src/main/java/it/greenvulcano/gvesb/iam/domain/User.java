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
package it.greenvulcano.gvesb.iam.domain;


import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

/**
 * 
 * Represent an identified subject in a security context
 * 
 * @author GreenVulcano Technologies
 *
 */
public abstract class User {
	
	public static final String USERNAME_PATTERN = "(?=^.{4,256}$)^[a-zA-Z][a-zA-Z0-9._@-]*[a-zA-Z0-9]+$";
	public static final String PASSWORD_PATTERN = "(?=^.{4,128}$)^[a-zA-Z0-9._@&$#!?-]+$";		
			        
	public abstract Long getId();
	
	public abstract String getUsername();
	public abstract void setUsername(String username);
	
	public abstract Optional<String> getPassword();
	public abstract void setPassword(String password);

	public abstract boolean isExpired();
	public abstract void setExpired(boolean expired);

	public abstract boolean isEnabled();
	public abstract void setEnabled(boolean enabled);
		
	public  abstract UserInfo getUserInfo();
	public abstract void setUserInfo(UserInfo userInfo);
	
	public abstract Set<Role> getRoles();

	public abstract void addRole(Role role);
	
	public abstract void addRoles(Collection<Role> roles);
	
	public abstract void removeRole(String name);
	
	public abstract void clearRoles();
	
	public abstract void setPasswordTime(Date date);
}