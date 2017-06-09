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
package it.greenvulcano.gvesb.gviamx.service.internal;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.gviamx.domain.EmailChangeRequest;
import it.greenvulcano.gvesb.gviamx.domain.UserActionRequest;
import it.greenvulcano.gvesb.gviamx.repository.UserActionRepository;
import it.greenvulcano.gvesb.gviamx.service.NotificationManager;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.service.UsersManager;

public class EmailChangeManager {
	
	private final static Logger LOG = LoggerFactory.getLogger(EmailChangeManager.class);
	
	private final ExecutorService executor = Executors.newWorkStealingPool();	
	private final SecureRandom secureRandom = new SecureRandom();
		
	
	private final List<NotificationManager> notificationServices = new ArrayList<>();
	
	private UserActionRepository repository;
	private UsersManager usersManager;
	private Long expireTime = 60*60*1024L;
	
	public void setNotificationServices(List<NotificationManager> notificationServices){
		this.notificationServices.clear();
		if (notificationServices!=null && !notificationServices.isEmpty()) {
			this.notificationServices.addAll(notificationServices);
		}
	}
	
	public void setRepository(UserActionRepository repository) {
		this.repository = repository;
	}
	
	public void setUsersManager(UsersManager usersManager) {
		this.usersManager = usersManager;
	}
	
	public UsersManager getUsersManager() {
		return usersManager;
	}
	
	public void setExpireTime(Long expireTime) {
		this.expireTime = expireTime;
	}
	
	public void createEmailChangeRequest(String currentEmailAddress, String newEmailAddress) throws UserNotFoundException {		
		
		if (newEmailAddress == null ||  !newEmailAddress.matches(UserActionRequest.EMAIL_PATTERN)) {
			throw new IllegalArgumentException("Invalid email: "+newEmailAddress);
		}
		
		User user = usersManager.getUser(currentEmailAddress);			
		
	    EmailChangeRequest request = repository.get(newEmailAddress, EmailChangeRequest.class).orElseGet(EmailChangeRequest::new);
	    request.setUser(user);
	    request.setEmail(newEmailAddress);
	    request.setIssueTime(new Date());
	    request.setExpireTime(expireTime);
			    
		
		byte[] token = new byte[4]; 
		secureRandom.nextBytes(token);
		
		String clearTextToken = String.format(Locale.US, "%02x%02x%02x%02x", IntStream.range(0, token.length).mapToObj(i->Byte.valueOf(token[i])).toArray());
		request.setToken(DigestUtils.sha256Hex(clearTextToken));	
		
		repository.add(request);
		
		request.setToken(clearTextToken);
		notificationServices.stream()
							.map( n -> new NotificationManager.NotificationTask(n, request, "update"))
							.forEach(executor::submit);
		
		
	}
	
	public EmailChangeRequest retrieveEmailChangeRequest(String email, String token) {
		
		EmailChangeRequest request = repository.get(email, EmailChangeRequest.class).orElseThrow(()->new IllegalArgumentException("No password reset request found for this email"));
						
		if (DigestUtils.sha256Hex(token).equals(request.getToken())) {
			
			if (System.currentTimeMillis() > request.getIssueTime().getTime()+request.getExpireTime()) {
				repository.remove(request);
				throw new SecurityException("No password reset request found for this email");
			}
			
			return request;
						
		} else {
			throw new SecurityException("Token missmatch");
		}
		
	}
	
	public void consumeEmailChangeRequest(EmailChangeRequest request) {

		try {
			
			repository.remove(request);
		} catch (Exception fatalException) {
			LOG.error("Fail to process  password reset request with id "+request.getId(), fatalException);
			
			throw fatalException;
		}
	}
	

}
