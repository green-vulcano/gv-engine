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


import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 
 * Entity representing an identified subject in a security context
 * 
 * @author GreenVulcano Technologies
 *
 */
@Entity
@Table(name="users")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String USERNAME_PATTERN = "(?=^.{4,256}$)^[a-zA-Z][a-zA-Z0-9._@-]*[a-zA-Z0-9]+$";
	public static final String PASSWORD_PATTERN = "(?=^.{4,128}$)^[a-zA-Z0-9._@&$#!?-]+$";
		
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
    
	@Column(nullable=false,length=32, unique=true, updatable=true)
    private String username;
    
    @Column(nullable=false)
    private String password;                       
        
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="pwd_time", nullable=false)
    private Date passwordTime;

    @Column(name="expired", nullable=false)
    private boolean expired;
    
    @Column(name="enabled", nullable=false)
    private boolean enabled;
    
    @Version
    private int version;
    
    @CreationTimestamp @Temporal(TemporalType.TIMESTAMP)
    @Column(name="creation_time")
    private Date creationTime;
    
    @UpdateTimestamp @Temporal(TemporalType.TIMESTAMP)
    @Column(name="update_time")
    private Date updateTime;
   
    @Embedded
    private UserInfo userInfo;
        
    @ManyToMany(fetch=FetchType.EAGER)
    @Cascade({CascadeType.SAVE_UPDATE})
    @JoinTable(
            name="user_roles",  
            joinColumns = { @JoinColumn( name="user_id", referencedColumnName="id", nullable=false, updatable=false)},
            inverseJoinColumns = @JoinColumn( name="role_id", referencedColumnName="id", nullable=false, updatable=true)            
    ) 
    private final Set<Role> roles = new HashSet<>();
        
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getPasswordTime() {
		return passwordTime;
	}

	public void setPasswordTime(Date passwordTime) {
		this.passwordTime = passwordTime;
	}

	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
		
	public Set<Role> getRoles() {
		return roles;
	}	

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}	

	public Date getCreationTime() {
		return creationTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((creationTime == null) ? 0 : creationTime.hashCode());
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + (expired ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((passwordTime == null) ? 0 : passwordTime.hashCode());
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		result = prime * result + ((updateTime == null) ? 0 : updateTime.hashCode());
		result = prime * result + ((userInfo == null) ? 0 : userInfo.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		result = prime * result + version;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (creationTime == null) {
			if (other.creationTime != null)
				return false;
		} else if (!creationTime.equals(other.creationTime))
			return false;
		if (enabled != other.enabled)
			return false;
		if (expired != other.expired)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (passwordTime == null) {
			if (other.passwordTime != null)
				return false;
		} else if (!passwordTime.equals(other.passwordTime))
			return false;
		if (roles == null) {
			if (other.roles != null)
				return false;
		} else if (!roles.equals(other.roles))
			return false;
		if (updateTime == null) {
			if (other.updateTime != null)
				return false;
		} else if (!updateTime.equals(other.updateTime))
			return false;
		if (userInfo == null) {
			if (other.userInfo != null)
				return false;
		} else if (!userInfo.equals(other.userInfo))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		if (version != other.version)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("{");
		builder.append("\"id\":\"");
		builder.append(id);
		builder.append("\",\"username\":\"");
		builder.append(username);
		builder.append("\",\"password\":\"");
		builder.append(password);
		builder.append("\",\"userInfo\":\"");
		builder.append(userInfo);
		builder.append("\",\"passwordTime\":");
		builder.append(passwordTime);
		builder.append("\",\"creationTime\":");
		builder.append(creationTime);
		builder.append("\",\"updateTime\":");
		builder.append(updateTime);
		builder.append(",\"expired\":");
		builder.append(expired);
		builder.append(",\"enabled\":");
		builder.append(enabled);
		builder.append(",\"version\":");
		builder.append(version);
		builder.append(",\"roles\":{");		
		builder.append(roles.stream()
							.map(r->"{\""+r.getName()+"\":"+r)
							.collect(Collectors.joining(",")));
		builder.append("}");
		return builder.toString();
	}   
	   
}