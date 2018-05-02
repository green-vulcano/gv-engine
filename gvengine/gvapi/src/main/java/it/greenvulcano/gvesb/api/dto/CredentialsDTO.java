package it.greenvulcano.gvesb.api.dto;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.greenvulcano.gvesb.iam.domain.Credentials;

public class CredentialsDTO {
	
	@JsonProperty(value="token_type")
	private final String tokenType = "Bearer"; 
	
	@JsonProperty(value="access_token")
	private final String accessToken;
	
	@JsonProperty(value="refresh_token")
	private final String refreshToken;
	
	@JsonProperty(value="expires_in")
	private final long expiresIn;
	
	@JsonProperty(value="issue_date")
	private final String issueDate;
	
	public CredentialsDTO(Credentials credentials) {
		accessToken = credentials.getAccessToken();
		refreshToken = credentials.getRefreshToken();
		expiresIn = credentials.getLifeTime();		
		issueDate = DateTimeFormatter.ISO_OFFSET_DATE_TIME
				                     .withZone(ZoneId.systemDefault())
				                     .format(credentials.getIssueTime().toInstant());		
	}	
	
	public String getAccessToken() {
		return accessToken;
	}
		
	public String getRefreshToken() {
		return refreshToken;
	}
		
	public long getExpiresIn() {
		return expiresIn;
	}	

	public String getIssueDate() {
		return issueDate;
	}
	
	public String getTokenType() {
		return tokenType;
	}	
	

}
