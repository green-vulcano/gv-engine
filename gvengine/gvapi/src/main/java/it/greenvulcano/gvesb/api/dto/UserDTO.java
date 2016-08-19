package it.greenvulcano.gvesb.api.dto;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.greenvulcano.gvesb.iam.domain.Role;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.domain.UserInfo;

public class UserDTO {

	@JsonIgnore
	private final User user;
	
	@JsonCreator
	public UserDTO(@JsonProperty("username") String username, 
					@JsonProperty("expired") boolean expired, 
					@JsonProperty("enabled") boolean enabled,
					@JsonProperty("userInfo") UserInfo userInfo,
					@JsonProperty("roles") Set<Role> roles) {
		user = new User();
		user.setUsername(username);;
		user.setExpired(expired);
		user.setEnabled(enabled);
		user.setUserInfo(userInfo);
		if (roles!=null) user.getRoles().addAll(roles);
	}
	
	@JsonIgnore
	public UserDTO(User user) {		
		this.user = Objects.requireNonNull(user);
	}
	
	public String getUsername() {
		return user.getUsername();
	}	

	public boolean isExpired() {
		return user.isExpired();
	}

	public boolean isEnabled() {
		return user.isEnabled();
	}
		
	public Set<Role> getRoles() {
		return user.getRoles();
	}	

	public UserInfo getUserInfo() {
		return Optional.ofNullable(user.getUserInfo()).orElse(new UserInfo()) ;
	}	

}