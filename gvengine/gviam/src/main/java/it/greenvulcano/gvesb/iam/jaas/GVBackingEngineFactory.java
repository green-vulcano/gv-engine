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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.karaf.jaas.modules.BackingEngine;
import org.apache.karaf.jaas.modules.BackingEngineFactory;
import org.apache.karaf.jaas.modules.encryption.EncryptionSupport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.exception.UserExistException;
import it.greenvulcano.gvesb.iam.repository.RoleRepository;
import it.greenvulcano.gvesb.iam.repository.UserRepository;

public class GVBackingEngineFactory implements BackingEngineFactory {

	private UserRepository userRepository;
	private RoleRepository roleRepository;
	
	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	public void setRoleRepository(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}
	
	@Override
	public String getModuleClass() {		
		return GVLoginModule.class.getName();
	}
	
	public void init() {
		
		
	}

	public BackingEngine build() {
		HashMap<String, BundleContext> options = new HashMap<>();
		//EncryptionSupport requires the BundleContext in the options
		options.put(BundleContext.class.getName(), FrameworkUtil.getBundle(EncryptionSupport.class).getBundleContext());
		
		
		BackingEngine backingEngine = build(options);
		
		Set<String> roles = new HashSet<>();
		roles.add("admin");
		
		Set<User> admins = userRepository.find(null, null, null, roles);
		
		
		
		if (admins.isEmpty()) {
			
			try {
				backingEngine.addUser("gvadmin", "gvadmin");
			} catch (UserExistException e) {
				
			}						
			backingEngine.addRole("gvadmin", "admin");
			backingEngine.addRole("gvadmin", "manager");
			backingEngine.addRole("gvadmin", "viewer");
			backingEngine.addRole("gvadmin", "systembundles");
			
			User admin = userRepository.get("gvadmin").get();
			admin.setExpired(true);
			
			userRepository.add(admin);
		} 
		
		return backingEngine;
	}
	
	@Override
	public BackingEngine build(Map<String, ?> options) {
		
		Map<String, Object> allOptions = new HashMap<>(options);	
		allOptions.putAll(GVLoginModule.ENCRYPTION_SETTINGS);
		EncryptionSupport encryptionSupport = new EncryptionSupport(allOptions);
		
		return new GVBackingEngine(userRepository, roleRepository, encryptionSupport) ;
	}
	
	UserRepository getUserRepository() {
		return userRepository;
	}	

}