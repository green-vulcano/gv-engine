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

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.modules.AbstractKarafLoginModule;
import org.apache.karaf.jaas.modules.BackingEngineFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.iam.domain.Role;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.repository.UserRepository;

/**
 * GreenVulcano implementation of Karaf JAAS facility
 * 
 * @see {@link AbstractKarafLoginModule}
 */
public class GVLoginModule extends AbstractKarafLoginModule{

	private final static Logger LOG = LoggerFactory.getLogger(GVLoginModule.class);
	
	final static Map<String, String> ENCRYPTION_SETTINGS = new HashMap<>();
	static {
		ENCRYPTION_SETTINGS.put("encryption.enabled","true");
		ENCRYPTION_SETTINGS.put("encryption.name","basic");
		ENCRYPTION_SETTINGS.put("encryption.algorithm","SHA-1");
		ENCRYPTION_SETTINGS.put("encryption.prefix",".x#");
		ENCRYPTION_SETTINGS.put("encryption.suffix","#x.");
		ENCRYPTION_SETTINGS.put("encryption.encoding","hexadecimal");
		
		Collections.unmodifiableMap(ENCRYPTION_SETTINGS);
	}
	

	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
		Map<String, Object> allOptions = new HashMap<>(options);	
		allOptions.putAll(GVLoginModule.ENCRYPTION_SETTINGS);
		
		super.initialize(subject, callbackHandler, allOptions);

	}

	@Override
	public boolean login() throws LoginException {
		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("Username: ");
		callbacks[1] = new PasswordCallback("Password: ", false);

		try {
			callbackHandler.handle(callbacks);
		} catch (IOException ioe) {
			throw new LoginException(ioe.getMessage());
		} catch (UnsupportedCallbackException uce) {
			throw new LoginException(uce.getMessage() + " not available to obtain information from user");
		}

		user = ((NameCallback) callbacks[0]).getName();

		char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
		if (tmpPassword == null) {
			tmpPassword = new char[0];
		}

		String password = new String(tmpPassword);
		principals = new HashSet<>();
					
		User loggingUser = getUserRepository().get(user)
											  .orElseThrow(() -> new LoginException("User " + user + " does not exist"));
		
		if (checkPassword(password, loggingUser.getPassword())) {
           
			if (loggingUser.isEnabled()) {
			
				if (loggingUser.isExpired()) {
					updateUserPassword(loggingUser);
				}
			
				principals.add(new UserPrincipal(user));
				List<RolePrincipal> roles = loggingUser.getRoles().stream()
													   .map(Role::getName)
													   .map(RolePrincipal::new)
													   .collect(Collectors.toList());
				
				principals.addAll(roles);
			
			} else {
	        	throw new LoginException("User disabled");	             
	        }	
        } else {
        	throw new LoginException("Password for " + user + " does not match");             
        }		
		
		return true;
	}	

	private void updateUserPassword(User loggingUser) throws LoginException {
		Callback[] callbacks = new Callback[]{new PasswordCallback("Password expired, enter new password: ",false)};
				
		try {
			callbackHandler.handle(callbacks);
		} catch (IOException ioe) {
			throw new LoginException(ioe.getMessage());
		} catch (UnsupportedCallbackException uce) {
			throw new LoginException(uce.getMessage() + " not available to obtain information from user");
		}
		
		char[] tmpPassword = ((PasswordCallback) callbacks[0]).getPassword();
		if (tmpPassword != null && String.valueOf(tmpPassword).matches(User.PASSWORD_PATTERN)) {
		
			String newPassword = new String(tmpPassword);
			
			loggingUser.setPassword(getEncryptedPassword(newPassword));
			loggingUser.setExpired(false);
			loggingUser.setPasswordTime(new Date());
			
			try {
				getUserRepository().add(loggingUser);
			} catch (Exception exception) {
				LOG.error("Password update fail for user "+user, exception);
				throw new LoginException("Failed  to update password"); 
			}
		} else {
			throw new LoginException("Invalid password");
		}
		
	}

	@Override
	public boolean abort() throws LoginException {
	    return true;
	}
	
	@Override
	public boolean logout() throws LoginException {
	    subject.getPrincipals().removeAll(principals);
	    principals.clear();
	    if (debug) {
	        LOG.debug("logout");
	    }
	    return true;
	}
	
	private UserRepository getUserRepository() {
		BundleContext context = FrameworkUtil.getBundle(GVLoginModule.class).getBundleContext();

		ServiceReference<?>[] serviceReference;
		try {
			serviceReference = context.getServiceReferences(BackingEngineFactory.class.getName(), "(name=GVBackingEngineFactory)");
			GVBackingEngineFactory engineFactory = (GVBackingEngineFactory) context.getService(serviceReference[0]);
			
			return engineFactory.getUserRepository();
		
		} catch (InvalidSyntaxException e) {
			LOG.error("Error getting service reference ",e);
		}
		
		return null;
		
	}

}
