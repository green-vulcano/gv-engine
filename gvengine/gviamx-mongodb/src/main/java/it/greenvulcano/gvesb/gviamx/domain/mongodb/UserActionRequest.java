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
package it.greenvulcano.gvesb.gviamx.domain.mongodb;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bson.Document;
import org.json.JSONObject;

public class UserActionRequest {

    public enum NotificationStatus { PENDING, SENT, FAILED };    
    public enum Action { SIGNUP, RESET, UPDATE };
    
    public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        
    private String id;
    private Instant issueTime;    
    private Instant updateTime;
    private Long expiresIn;
    
    private String email;
    private String token;
    private String clearToken;
    
    private Long userid;
    private String request;
    private JSONObject requestObject;
    
    private NotificationStatus notificationStatus;
    private Action action;
    
    
    public UserActionRequest() {
        this.id = UUID.randomUUID().toString();
    }
    
    public UserActionRequest(Document userActionRequest) {
        
        this.id = userActionRequest.getString("_id");
        this.email = userActionRequest.getString("email");
        this.issueTime = Instant.ofEpochMilli(userActionRequest.getLong("issue_time"));
        this.expiresIn = userActionRequest.getLong("expires_in");
        this.token = userActionRequest.getString("token");
        this.updateTime = Instant.ofEpochMilli(userActionRequest.getLong("update_time"));
        this.notificationStatus = NotificationStatus.valueOf(userActionRequest.getString("status"));
        this.action = Action.valueOf(userActionRequest.getString("action"));
        
        this.request = userActionRequest.getString("data");
        this.userid = userActionRequest.getLong("userid");
        
    }
    
    public static UserActionRequest fromDocument(Document userActionRequest) {
        return new UserActionRequest(userActionRequest);   
    }

    public String getId() {
        return id;
    }

    public Instant getIssueTime() {
        return issueTime;
    }

    public void setIssueTime(Instant issueTime) {
        this.issueTime = issueTime;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Instant getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Instant updateTime) {
        this.updateTime = updateTime;
    }
    
    public String getToken() {
        return clearToken != null ? clearToken : token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setClearToken(String clearToken) {
        this.clearToken = clearToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(NotificationStatus notificationStatus) {
        this.notificationStatus = notificationStatus;
    }
    
    public Action getAction() {
        return action;
    }
    
    
    public void setAction(Action action) {
        this.action = action;
    }
    
    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        setRequest(request.getBytes());
    }
    
    public void setRequest(byte[] request) {
        this.request = Base64.getEncoder().encodeToString(request);
    }
    
    public Long getUserId() {
        return userid;
    }

    public void setUserId(Long userid) {
        this.userid = userid;
    }

    public JSONObject getRequestObject() {

        synchronized (this) {
            if (requestObject == null) {
                try {
                    requestObject = new JSONObject(Base64.getDecoder().decode(request));
                } catch (Exception e) {
                    requestObject = new JSONObject();
                }
            }
        }
        
        return requestObject;
    }

    public Map<String, Object> getActionData() {       
        return getRequestObject().toMap();
    }

    public JSONObject toJSONObject() {

        JSONObject userRequest = new JSONObject();
        userRequest.put("email", email)
                   .put("token", Optional.ofNullable(clearToken).orElse(token))
                   .put("issue_time", issueTime.toEpochMilli())
                   .put("update_time", updateTime.toEpochMilli())
                   .put("expires_in", expiresIn)
                   .put("status", notificationStatus.toString())
                   .put("action", action.toString())
                   .put("data", request)
                   .put("userid", userid);    
        
        return userRequest;
    }
    
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((expiresIn == null) ? 0 : expiresIn.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((issueTime == null) ? 0 : issueTime.hashCode());
        result = prime * result + ((notificationStatus == null) ? 0 : notificationStatus.hashCode());
        result = prime * result + ((token == null) ? 0 : token.hashCode());
        result = prime * result + ((updateTime == null) ? 0 : updateTime.hashCode());
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
        UserActionRequest other = (UserActionRequest) obj;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (expiresIn == null) {
            if (other.expiresIn != null)
                return false;
        } else if (!expiresIn.equals(other.expiresIn))
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
        if (notificationStatus != other.notificationStatus)
            return false;
        if (token == null) {
            if (other.token != null)
                return false;
        } else if (!token.equals(other.token))
            return false;
        if (updateTime == null) {
            if (other.updateTime != null)
                return false;
        } else if (!updateTime.equals(other.updateTime))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return toJSONObject().toString();
    }

}
