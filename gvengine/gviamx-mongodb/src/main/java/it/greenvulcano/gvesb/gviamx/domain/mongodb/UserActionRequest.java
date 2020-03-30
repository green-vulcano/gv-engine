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

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;

public abstract class UserActionRequest {

    public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public enum NotificationStatus {
                                    PENDING,
                                    SENT,
                                    FAILED
    }

    private Long id;

    private Date issueTime;

    private Long expireTime;

    private String email;

    private String token;

    private String clearToken;

    private NotificationStatus notificationStatus;

    private Date updateTime;

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

    public Date getUpdateTime() {

        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {

        this.updateTime = updateTime;
    }

    public abstract Map<String, Object> getActionData();

    public JSONObject toJSONObject() {

        JSONObject userRequest = new JSONObject();
        userRequest.put("email", email)
                   .put("token", Optional.ofNullable(clearToken).orElse(token))
                   .put("issueTime", issueTime.getTime())
                   .put("expireTime", issueTime.getTime() + expireTime); 
        
        return userRequest;
    }
    
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((expireTime == null) ? 0 : expireTime.hashCode());
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

}
