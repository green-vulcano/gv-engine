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
package it.greenvulcano.gvesb.iam.domain.mongodb;

import java.io.Serializable;
import java.util.Date;

public class CredentialsBSON extends it.greenvulcano.gvesb.iam.domain.Credentials implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String accessToken;
	
	private String refreshToken;
	
	private Date issueTime;
	
	private Long lifeTime;
	
	private UserBSON client;
	
	private UserBSON resourceOwner;
	
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
	public UserBSON getClient() {
		return client;
	}
		
	public void setClient(UserBSON client) {
		this.client = client;
	}
	
	@Override
	public UserBSON getResourceOwner() {
		return resourceOwner;
	}
	
	public void setResourceOwner(UserBSON resourceOwner) {
		this.resourceOwner = resourceOwner;
	}
	
	public boolean isValid() {
	    return (issueTime.getTime() + lifeTime) > System.currentTimeMillis();
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
		CredentialsBSON other = (CredentialsBSON) obj;
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
