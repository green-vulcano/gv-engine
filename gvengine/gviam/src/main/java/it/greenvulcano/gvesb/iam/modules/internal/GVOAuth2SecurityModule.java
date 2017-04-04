package it.greenvulcano.gvesb.iam.modules.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import it.greenvulcano.gvesb.iam.domain.Credentials;
import it.greenvulcano.gvesb.iam.domain.Role;
import it.greenvulcano.gvesb.iam.exception.CredentialsExpiredException;
import it.greenvulcano.gvesb.iam.exception.InvalidCredentialsException;
import it.greenvulcano.gvesb.iam.exception.PasswordMissmatchException;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.modules.Identity;
import it.greenvulcano.gvesb.iam.modules.SecurityModule;
import it.greenvulcano.gvesb.iam.service.CredentialsManager;

public class GVOAuth2SecurityModule implements SecurityModule {

	private CredentialsManager credentialsManager;
	
	public void setCredentialsManager(CredentialsManager credentialsManager) {
		this.credentialsManager = credentialsManager;
	}
	
	@Override
	public Optional<Identity> resolve(String authorization)
			throws InvalidCredentialsException, CredentialsExpiredException {
		String authData[] = Optional.of(authorization).orElse("").split(" ");
		
		if (authData.length>1 && authData[0].equalsIgnoreCase("Bearer")) {
			Credentials credentidals = credentialsManager.check(authData[1]);
			Identity identity = new Identity(credentidals.getResourceOwner().getId(), credentidals.getResourceOwner().getUsername(), 
											 credentidals.getResourceOwner().getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
			
			return Optional.of(identity);
			
		}
		return Optional.empty();
	}

	@Override
	public Optional<Identity> resolve(String type, Map<String, Object> authorization)
			throws UserNotFoundException, UserExpiredException, PasswordMissmatchException, InvalidCredentialsException, CredentialsExpiredException {
		
		if (Optional.ofNullable(type).orElse("").equalsIgnoreCase("Bearer") && Optional.ofNullable(authorization).orElse(new HashMap<>()).containsKey("access_token") ) {
			Credentials credentidals = credentialsManager.check(authorization.get("access_token").toString());
			Identity identity = new Identity(credentidals.getResourceOwner().getId(), credentidals.getResourceOwner().getUsername(), 
											 credentidals.getResourceOwner().getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
			
			return Optional.of(identity);
			
		}
		return Optional.empty();
	}

	@Override
	public Optional<Identity> resolve(String type, String... authorization)
			throws UserNotFoundException, UserExpiredException, PasswordMissmatchException, InvalidCredentialsException, CredentialsExpiredException {
		
		if (Optional.ofNullable(type).orElse("").equalsIgnoreCase("Bearer") && Optional.ofNullable(authorization).orElse(new String[]{}).length>0 ) {
			Credentials credentidals = credentialsManager.check(authorization[0]);
			Identity identity = new Identity(credentidals.getResourceOwner().getId(), credentidals.getResourceOwner().getUsername(), 
											 credentidals.getResourceOwner().getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
			
			return Optional.of(identity);
			
		}
		return Optional.empty();
	}

}
