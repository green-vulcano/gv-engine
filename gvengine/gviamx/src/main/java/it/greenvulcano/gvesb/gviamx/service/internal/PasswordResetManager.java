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

import it.greenvulcano.gvesb.gviamx.domain.PasswordResetRequest;
import it.greenvulcano.gvesb.gviamx.repository.PasswordResetRepository;
import it.greenvulcano.gvesb.gviamx.service.NotificationManager;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.service.UsersManager;

public class PasswordResetManager {
	
	private final static Logger LOG = LoggerFactory.getLogger(PasswordResetManager.class);
	
	private final ExecutorService executor = Executors.newWorkStealingPool();	
	private final SecureRandom secureRandom = new SecureRandom();
		
	
	private final List<NotificationManager> notificationServices = new ArrayList<>();
	
	private PasswordResetRepository repository;
	private UsersManager usersManager;
	private Long expireTime = 60*60*1024L;
	
	public void setNotificationServices(List<NotificationManager> notificationServices){
		if (notificationServices!=null && !notificationServices.isEmpty()) {
			this.notificationServices.addAll(notificationServices);
		}
	}
	
	public void setRepository(PasswordResetRepository repository) {
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
	
	public void createPasswordResetRequest(String email) throws UserNotFoundException {		
			
		User user = usersManager.getUser(email);			
		
	    PasswordResetRequest passwordResetRequest = repository.get(email).orElseGet(PasswordResetRequest::new);
	    passwordResetRequest.setUser(user);
	    passwordResetRequest.setEmail(email);
	    passwordResetRequest.setIssueTime(new Date());
	    passwordResetRequest.setExpireTime(expireTime);
			    
		
		byte[] token = new byte[4]; 
		secureRandom.nextBytes(token);
		
		String clearTextToken = String.format(Locale.US, "%02x%02x%02x%02x", IntStream.range(0, token.length).mapToObj(i->Byte.valueOf(token[i])).toArray());
		passwordResetRequest.setToken(DigestUtils.sha256Hex(clearTextToken));	
		
		repository.add(passwordResetRequest);
		
		passwordResetRequest.setToken(clearTextToken);
		notificationServices.stream()
							.map( n -> new NotificationManager.NotificationTask(n, passwordResetRequest, "reset"))
							.forEach(executor::submit);
		
		
	}
	
	public PasswordResetRequest retrievePasswordResetRequest(String email, String token) {
		
		PasswordResetRequest signupRequest = repository.get(email).orElseThrow(()->new IllegalArgumentException("No password reset request found for this email"));
						
		if (DigestUtils.sha256Hex(token).equals(signupRequest.getToken())) {
			
			if (System.currentTimeMillis() > signupRequest.getIssueTime().getTime()+signupRequest.getExpireTime()) {
				repository.remove(signupRequest);
				throw new IllegalArgumentException("No password reset request found for this email");
			}
			try {
				signupRequest.setUser(usersManager.getUser(email));
			} catch (UserNotFoundException e) {
				throw new IllegalArgumentException("Token missmatch");
			}
			return signupRequest;
						
		} else {
			throw new IllegalArgumentException("Token missmatch");
		}
		
	}
	
	public void consumePasswordResetRequest(PasswordResetRequest passwordResetRequest) {

		try {
			
			repository.remove(passwordResetRequest);
		} catch (Exception fatalException) {
			LOG.error("Fail to process  password reset request with id "+passwordResetRequest.getId(), fatalException);
			
			throw fatalException;
		}
	}
	

}
