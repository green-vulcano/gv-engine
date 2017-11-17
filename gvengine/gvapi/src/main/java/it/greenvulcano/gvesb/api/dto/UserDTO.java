package it.greenvulcano.gvesb.api.dto;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
	public UserDTO( @JsonProperty("id") Long id,
					@JsonProperty("username") String username, 
					@JsonProperty("expired") boolean expired, 
					@JsonProperty("enabled") boolean enabled,
					@JsonProperty("userInfo") UserInfoDTO userInfo,
					@JsonProperty("roles") Map<String, RoleDTO> roles) {
		
		user = new User() {
			
			Long _id = id;
			String _username = username;
			boolean _expired = expired;
			boolean _enabled = enabled;			
			UserInfo _userInfo = userInfo;
			Set<Role> _roles = new LinkedHashSet<>();
			
			
			@Override
			public void setUsername(String username) {
				this._username = username;
				
			}
			
			@Override
			public void setUserInfo(UserInfo userInfo) {
				this._userInfo = userInfo;
				
			}
			
			@Override
			public void setPasswordTime(Date date) {
								
			}
			
			@Override
			public void setPassword(String password) {
							
			}
			
			@Override
			public void setExpired(boolean expired) {
				this._expired = expired;				
			}
			
			@Override
			public void setEnabled(boolean enabled) {
				this._enabled = enabled;
				
			}
			
			@Override
			public boolean isExpired() {				
				return _expired;
			}
			
			@Override
			public boolean isEnabled() {				
				return _enabled;
			}
			
			@Override
			public String getUsername() {				
				return _username;
			}
			
			@Override
			public UserInfo getUserInfo() {				
				return _userInfo;
			}
			
			@Override
			public Set<Role> getRoles() {				
				return _roles;
			}
			
			@Override
			public String getPassword() {				
				return null;
			}
			
			@Override
			public Long getId() {				
				return _id;
			}

			@Override
			public void addRole(Role role) {
				_roles.add(role);
				
			}

			@Override
			public void addRoles(Collection<Role> roles) {
				_roles.addAll(roles);
				
			}

			@Override
			public void removeRole(String name) {
				_roles.stream().filter(r -> r.getName().equals(name)).findAny().ifPresent(_roles::remove);
				
			}

			@Override
			public void clearRoles() {
				_roles.clear();
				
			}
		};
		
		if (roles!=null) user.getRoles().addAll(roles.values());
	}
	
	@JsonIgnore
	public UserDTO(User user) {		
		this.user = Objects.requireNonNull(user);
	}
	
	public Long getId() {
		return this.user.getId();
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
		
	public Map<String, Role> getRoles() {
		return user.getRoles().stream().collect(Collectors.toMap(Role::getName, Function.identity()));
	}
	
	@JsonIgnore
	public Set<Role> getGrantedRoles() {
		return user.getRoles();
	}

	public UserInfo getUserInfo() {
		return user.getUserInfo();
	}	

}