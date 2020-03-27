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

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;
import org.json.JSONObject;

public class CredentialsBson extends it.greenvulcano.gvesb.iam.domain.Credentials {

    public static final String COLLECTION_NAME = "credentials";
    private static final JsonWriterSettings JSON_SETTINGS = JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build();

    private ObjectId objectId;
    private String accessToken;
    private String refreshToken;
    private Instant issueTime;
    private Long lifeTime;

    private UserBson client;
    private UserBson resourceOwner;
    
    public CredentialsBson() {
        
    }
    
    public CredentialsBson(Document credentials, UserBson client, UserBson resourceOwner) {
        
        this.objectId = new ObjectId(credentials.getString("_id"));
        
        JSONObject credentialsJson = new JSONObject(credentials.toJson(JSON_SETTINGS));

        this.accessToken = (String) credentialsJson.opt("access_token");
        this.refreshToken = (String) credentialsJson.opt("refresh_token");
        String issueTime = (String) credentialsJson.opt("issue_date");
        this.issueTime =  Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(issueTime));
        this.lifeTime = (Long) credentialsJson.opt("expires_in");
        
        this.client = client;
        this.resourceOwner = resourceOwner;
        
    }
    
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
        return new Date(issueTime.toEpochMilli());
    }
    
    public void setIssueTime(Instant issueTime) {
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
    public UserBson getClient() {

        return client;
    }

    public void setClient(UserBson client) {

        this.client = client;
    }

    @Override
    public UserBson getResourceOwner() {

        return resourceOwner;
    }

    public void setResourceOwner(UserBson resourceOwner) {

        this.resourceOwner = resourceOwner;
    }

    public boolean isValid() {

        return issueTime.plusMillis(lifeTime).isAfter(Instant.now());
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
        CredentialsBson other = (CredentialsBson) obj;
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
           return toJSONObject().toString();
    }
    
    public JSONObject toJSONObject() {
        JSONObject credentials = new JSONObject();
        
        credentials.put("_id", objectId.toHexString())
            .put("access_token", accessToken)
            .put("refresh_token", refreshToken)
            .put("issue_date", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(issueTime))
            .put("expires_in", lifeTime)
            .put("client", client.getUsername())
            .put("resource_owner", resourceOwner.getUsername());
        
           return credentials;
    }

}
