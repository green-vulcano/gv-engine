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
package it.greenvulcano.gvesb.iam.domain.jpa;


import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
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
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import it.greenvulcano.gvesb.iam.domain.Role;

@Entity
@Table(name="users")
public class UserJPA extends it.greenvulcano.gvesb.iam.domain.User implements Serializable {
	private static final long serialVersionUID = 1L;   	        
      
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@Column(nullable=false, length=256, unique=true, updatable=true)
	private String username;
	
	@Column(nullable=true, length=512)
	private String password;
	
	@Column(name="expired", nullable=false)
	private boolean expired;
	
	@Column(name="enabled", nullable=false)
	private boolean enabled;    
	
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="pwd_time", nullable=true)
    private Date passwordTime;
    
    @Version
    private int version;
    
    @CreationTimestamp @Temporal(TemporalType.TIMESTAMP)
    @Column(name="creation_time")
    private Date creationTime;
    
    @UpdateTimestamp @Temporal(TemporalType.TIMESTAMP)
    @Column(name="update_time")
    private Date updateTime;
       
    @ManyToMany(fetch=FetchType.EAGER, targetEntity = RoleJPA.class)
    @Cascade({CascadeType.SAVE_UPDATE})
    @JoinTable(
            name="user_roles",  
            joinColumns = { @JoinColumn( name="user_id", referencedColumnName="id", nullable=false, updatable=false)},
            inverseJoinColumns = @JoinColumn( name="role_id", referencedColumnName="id",  nullable=false, updatable=true)            
    ) 
    private final Set<RoleJPA> roles = new HashSet<>();
    
    @Embedded
    private UserInfoJPA userInfo;
       
    @Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public void setUsername(String username) {
		this.username = username;
	}

	@Transient @Override
	public Optional<String> getPassword() {
		return Optional.ofNullable(password);
	}	

	@Override
	public void setPassword(String password) {
		this.password = password;
	}

	public Date getPasswordTime() {
		return passwordTime;
	}

	public void setPasswordTime(Date passwordTime) {
		this.passwordTime = passwordTime;
	}	
	
	@Override
	public boolean isExpired() {
		return expired;
	}

	@Override
	public void setExpired(boolean expired) {
		this.expired = expired;
	}	
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
		
	@Override
	public Set<Role> getRoles() {
		return Collections.unmodifiableSet(new HashSet<>(roles));
	}	

	@Override
	public it.greenvulcano.gvesb.iam.domain.UserInfo getUserInfo() {
		if (userInfo==null) {
			userInfo = new UserInfoJPA();
		}
		return  userInfo;
	}

	@Override
	public void setUserInfo(it.greenvulcano.gvesb.iam.domain.UserInfo userInfo) {
		
		if (userInfo!=null) {
			this.userInfo = new UserInfoJPA();
			this.userInfo.setEmail(userInfo.getEmail());
			this.userInfo.setFullname(userInfo.getFullname());
		} else {
			this.userInfo = null;
		}
		
	}	

	public Date getCreationTime() {
		return creationTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}
	
	public int getVersion() {
		return version;
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
		UserJPA other = (UserJPA) obj;
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

	@Override
	public void addRole(Role role) {
		if (role!=null) {
			RoleJPA r = new RoleJPA(role.getName(), role.getDescription());
			r.setId(role.getId());
			roles.add(r);
		}
		
	}

	@Override
	public void addRoles(Collection<Role> roles) {
		if (roles!=null) {
			roles.forEach(this::addRole);
		}
		
	}

	@Override
	public void removeRole(String name) {
		roles.stream().filter(r -> r.getName().equals(name)).findAny().ifPresent(roles::remove);		
	}

	@Override
	public void clearRoles() {
		roles.clear();
		
	}
	   
}
