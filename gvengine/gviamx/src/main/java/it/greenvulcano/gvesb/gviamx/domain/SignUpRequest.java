package it.greenvulcano.gvesb.gviamx.domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.json.JSONObject;

@Entity
@Table(name="signup_request")
public class SignUpRequest extends UserActionRequest implements Serializable{
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
	
	@Column(nullable=false)
	@Lob() @Basic(fetch=FetchType.LAZY)
	private byte[] request;
	
	@Transient
	private JSONObject requestObject;
	
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
	
	public byte[] getRequest() {
		return request;
	}
	public void setRequest(byte[] request) {
		this.request = request;
	}
	
	@Transient
	public JSONObject getRequestObject() {
		if (requestObject==null) {
			try {
				requestObject = new JSONObject(new String(request));
			} catch (Exception e) {
				return null;
			}
		}
		return requestObject;
	}
	
	@Transient @Override
	public String getFullname() {
				
		return Optional.ofNullable(getRequestObject()).orElseGet(JSONObject::new).optString("fullname", null);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((expireTime == null) ? 0 : expireTime.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((issueTime == null) ? 0 : issueTime.hashCode());
		result = prime * result + Arrays.hashCode(request);
		result = prime * result + ((token == null) ? 0 : token.hashCode());
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
		SignUpRequest other = (SignUpRequest) obj;
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
		if (!Arrays.equals(request, other.request))
			return false;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
	}
	
	
	

}
