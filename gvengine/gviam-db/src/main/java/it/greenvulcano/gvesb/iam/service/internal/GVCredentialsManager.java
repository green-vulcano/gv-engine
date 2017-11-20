package it.greenvulcano.gvesb.iam.service.internal;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Locale;
import java.util.stream.IntStream;

import org.apache.commons.codec.digest.DigestUtils;

import it.greenvulcano.gvesb.iam.domain.Credentials;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.domain.jpa.CredentialsJPA;
import it.greenvulcano.gvesb.iam.domain.jpa.UserJPA;
import it.greenvulcano.gvesb.iam.exception.CredentialsExpiredException;
import it.greenvulcano.gvesb.iam.exception.InvalidCredentialsException;
import it.greenvulcano.gvesb.iam.exception.PasswordMissmatchException;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.repository.hibernate.CredentialsRepositoryHibernate;
import it.greenvulcano.gvesb.iam.service.CredentialsManager;
import it.greenvulcano.gvesb.iam.service.UsersManager;

public class GVCredentialsManager implements CredentialsManager {
	
	private final static String TOKEN_PATTERN = "%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x";
		
	private final SecureRandom secureRandom = new SecureRandom();
		
	private UsersManager usersManager;
	private CredentialsRepositoryHibernate credentialsRepository;
	
	private long tokenLifeTime = 24 * 60 * 60 * 1000;
	
	public void setUsersManager(UsersManager usersManager) {
		this.usersManager = usersManager;
	}
	
	public void setCredentialsRepository(CredentialsRepositoryHibernate credentialsRepository) {
		this.credentialsRepository = credentialsRepository;
	}
	
	public void setTokenLifeTime(long tokenLifeTime) {
		this.tokenLifeTime = tokenLifeTime;
	}

	@Override
	public Credentials create(String username, String password, String clientUsername, String clientPassword) throws UserNotFoundException, UserExpiredException, PasswordMissmatchException {
				
		User client = usersManager.validateUser(clientUsername, clientPassword);
		User resourceOwner = usersManager.validateUser(username, password);		
		
		CredentialsJPA credentials = new CredentialsJPA();
		credentials.setClient(UserJPA.class.cast(client));
		credentials.setResourceOwner(UserJPA.class.cast(resourceOwner));
		
		String accessToken = generateToken();
		
		String refreshToken = generateToken();
		
		credentials.setAccessToken(DigestUtils.sha256Hex(accessToken));	
		credentials.setRefreshToken(DigestUtils.sha256Hex(refreshToken));
		
		credentials.setIssueTime(new Date());
		credentials.setLifeTime(tokenLifeTime);
		
		credentialsRepository.add(credentials);
		
		credentials.setAccessToken(accessToken);	
		credentials.setRefreshToken(refreshToken);
		
		return credentials;
	}

	@Override
	public Credentials check(String accessToken) throws InvalidCredentialsException, CredentialsExpiredException {
		
		Credentials credentials = credentialsRepository.get(DigestUtils.sha256Hex(accessToken)).orElseThrow(InvalidCredentialsException::new);
		
		long tokenLife = System.currentTimeMillis() - credentials.getIssueTime().getTime();
		if (tokenLife>credentials.getLifeTime()){
			throw new CredentialsExpiredException();
		}
		 
		return credentials;
	}

	@Override
	public Credentials refresh(String refreshToken, String accessToken) throws InvalidCredentialsException {
		Credentials lastCredentials = credentialsRepository.get(DigestUtils.sha256Hex(accessToken)).orElseThrow(InvalidCredentialsException::new);
		
		
		if (lastCredentials.getRefreshToken().equals(DigestUtils.sha256Hex(refreshToken))) {
			CredentialsJPA credentials = new CredentialsJPA();
			credentials.setClient(UserJPA.class.cast(lastCredentials.getClient()));
			credentials.setResourceOwner(UserJPA.class.cast(lastCredentials.getResourceOwner()));
			
			String newAccessToken = generateToken();
			String newRefreshToken = generateToken();
			
			credentials.setAccessToken(DigestUtils.sha256Hex(newAccessToken));	
			credentials.setRefreshToken(DigestUtils.sha256Hex(newRefreshToken));
			
			credentials.setIssueTime(new Date());
			credentials.setLifeTime(tokenLifeTime);
			
			credentialsRepository.add(credentials);
			
			credentialsRepository.remove(lastCredentials);
			
			credentials.setAccessToken(newAccessToken);	
			credentials.setRefreshToken(newRefreshToken);
			
			return credentials;
		} else {
			throw new InvalidCredentialsException();
		}		
		
	}
	
	
	private String generateToken() {
		byte[] token = new byte[16];
		secureRandom.nextBytes(token);
		return String.format(Locale.US, TOKEN_PATTERN, IntStream.range(0, token.length).mapToObj(i->Byte.valueOf(token[i])).toArray() );				
	}

}
