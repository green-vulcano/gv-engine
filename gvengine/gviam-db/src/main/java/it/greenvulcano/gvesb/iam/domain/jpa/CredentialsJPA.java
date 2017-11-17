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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.ForeignKey;

@Entity
@Table(name="credentials")
public class CredentialsJPA extends it.greenvulcano.gvesb.iam.domain.Credentials implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id @Column(name="access_token", nullable=false, length=64, unique=true, updatable=false)
	private String accessToken;
	
	@Column(name="refresh_token", nullable=false, length=64, updatable=false)
	private String refreshToken;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="issue_time", nullable=false)
	private Date issueTime;
	
	@Column(name="life_time")
	private Long lifeTime;
	
	@ManyToOne(optional=false, fetch=FetchType.EAGER)
	@JoinColumn(name="client_id", nullable=false, foreignKey=@ForeignKey(name = "CLIENT_FK"))
	private UserJPA client;
	
	@ManyToOne(optional=false, fetch=FetchType.EAGER)
	@JoinColumn(name="resource_owner_id", nullable=false, foreignKey=@ForeignKey(name = "OWNER_FK"))
	private UserJPA resourceOwner;
	
	@Override
	public String getAccessToken() {
		return accessToken;
	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	@Override
	public String getRefreshToken() {
		return refreshToken;
	}
	
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	@Override
	public Date getIssueTime() {
		return issueTime;
	}
	
	public void setIssueTime(Date issueTime) {
		this.issueTime = issueTime;
	}
	
	@Override
	public Long getLifeTime() {
		return lifeTime;
	}
	
	public void setLifeTime(Long lifeTime) {
		this.lifeTime = lifeTime;
	}
	
	@Override
	public UserJPA getClient() {
		return client;
	}
		
	public void setClient(UserJPA client) {
		this.client = client;
	}
	
	@Override
	public UserJPA getResourceOwner() {
		return resourceOwner;
	}
	
	public void setResourceOwner(UserJPA resourceOwner) {
		this.resourceOwner = resourceOwner;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accessToken == null) ? 0 : accessToken.hashCode());
		result = prime * result + ((client == null) ? 0 : client.hashCode());
		result = prime * result + ((issueTime == null) ? 0 : issueTime.hashCode());
		result = prime * result + ((lifeTime == null) ? 0 : lifeTime.hashCode());
		result = prime * result + ((refreshToken == null) ? 0 : refreshToken.hashCode());
		result = prime * result + ((resourceOwner == null) ? 0 : resourceOwner.hashCode());
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
		CredentialsJPA other = (CredentialsJPA) obj;
		if (accessToken == null) {
			if (other.accessToken != null)
				return false;
		} else if (!accessToken.equals(other.accessToken))
			return false;
		if (client == null) {
			if (other.client != null)
				return false;
		} else if (!client.equals(other.client))
			return false;
		if (issueTime == null) {
			if (other.issueTime != null)
				return false;
		} else if (!issueTime.equals(other.issueTime))
			return false;
		if (lifeTime == null) {
			if (other.lifeTime != null)
				return false;
		} else if (!lifeTime.equals(other.lifeTime))
			return false;
		if (refreshToken == null) {
			if (other.refreshToken != null)
				return false;
		} else if (!refreshToken.equals(other.refreshToken))
			return false;
		if (resourceOwner == null) {
			if (other.resourceOwner != null)
				return false;
		} else if (!resourceOwner.equals(other.resourceOwner))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AccessToken [accessToken=" + accessToken + ", refreshToken=" + refreshToken + ", issueTime=" + issueTime
				+ ", lifeTime=" + lifeTime + ", client=" + client + ", resourceOwner=" + resourceOwner + "]";
	}	

}
