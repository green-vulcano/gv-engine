package it.greenvulcano.gvesb.api.dto;

import it.greenvulcano.gvesb.iam.domain.UserInfo;

public class UserInfoDTO extends UserInfo {

	private String fullname, email;
	
	@Override
	public String getFullname() {		
		return fullname;
	}

	@Override
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public void setEmail(String email) {
		this.email = email;

	}

}
