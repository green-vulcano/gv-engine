package it.greenvulcano.gvesb.gviamx.domain;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.json.JSONObject;

import it.greenvulcano.gvesb.iam.domain.User;

@Entity
@Table(name="password_reset_request")
public class PasswordResetRequest extends UserActionRequest implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="issue_time", nullable=false)
	private Date issueTime;
	
	@Column(name="expire_time", nullable=false)
	private Long expireTime;
	
	@Column(length=320, nullable=false)
	private String email;
	
	@Column(length=8, nullable=false)
	private String token;
	
	@Column(name="user_id", nullable=false)
	private Long userId;
	
	@Transient
	private User user;
			
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Date getIssueTime() {
		return issueTime;
	}
	public void setIssueTime(Date issueTime) {
		this.issueTime = issueTime;
	}
	
	public Long getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(Long expireTime) {
		this.expireTime = expireTime;
	}
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}	
	
	@Override
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public Long getUserId() {
		return userId;
	}	
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
		if (user!=null) {
			this.userId = user.getId();
		} else {
			this.userId = null;
		}
		
	}
	
	@Override
	public String getFullname(){
		if (user!=null && user.getUserInfo()!=null) {
			return this.user.getUserInfo().getFullname();
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((expireTime == null) ? 0 : expireTime.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((issueTime == null) ? 0 : issueTime.hashCode());
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
		PasswordResetRequest other = (PasswordResetRequest) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (expireTime == null) {
			if (other.expireTime != null)
				return false;
		} else if (!expireTime.equals(other.expireTime))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (issueTime == null) {
			if (other.issueTime != null)
				return false;
		} else if (!issueTime.equals(other.issueTime))
			return false;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
	
	@Override
	public JSONObject getRequestObject() {		
		return null;
	}	

}
