package it.greenvulcano.gvesb.iam.domain;

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
@Table(name="oauth2_identities")
public class OAuth2Identity {
	
	@Id @Column(name="access_token", nullable=false, length=36, unique=true, updatable=false)
	private String accessToken;
	
	@Column(name="refresh_token", nullable=false, length=36, updatable=false)
	private String refreshToken;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="issue_time", nullable=false)
	private Date issueTime;
	
	@Column(name="life_time")
	private Long lifeTime;
	
	@ManyToOne(optional=false, fetch=FetchType.EAGER)
	@JoinColumn(name="client_id", nullable=false, foreignKey=@ForeignKey(name = "CLIENT_FK"))
	private User client;
	
	@ManyToOne(optional=false, fetch=FetchType.EAGER)
	@JoinColumn(name="resource_owner_id", nullable=false, foreignKey=@ForeignKey(name = "OWNER_FK"))
	private User resourceOwner;
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	public String getRefreshToken() {
		return refreshToken;
	}
	
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	public Date getIssueTime() {
		return issueTime;
	}
	
	public void setIssueTime(Date issueTime) {
		this.issueTime = issueTime;
	}
	
	public Long getLifeTime() {
		return lifeTime;
	}
	
	public void setLifeTime(Long lifeTime) {
		this.lifeTime = lifeTime;
	}
	
	public User getClient() {
		return client;
	}
	
	public void setClient(User client) {
		this.client = client;
	}
	
	public User getResourceOwner() {
		return resourceOwner;
	}
	
	public void setResourceOwner(User resourceOwner) {
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
		OAuth2Identity other = (OAuth2Identity) obj;
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
