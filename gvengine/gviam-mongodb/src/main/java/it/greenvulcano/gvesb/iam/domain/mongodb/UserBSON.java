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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.util.JSON;

import it.greenvulcano.gvesb.iam.domain.Role;

public class UserBSON extends it.greenvulcano.gvesb.iam.domain.User {

    public static final String COLLECTION_NAME = "users";
    
    
    private ObjectId objectId;
    
    private Long id;
    private String username;
    private String password;
    private boolean expired;
    private boolean enabled;
    private Long passwordTime;
    private int version;
    private Long creationTime;
    private Long updateTime;

    private final Set<RoleBSON> roles = new HashSet<>();

    private UserInfoBSON userInfo;
    
    public UserBSON(Document user) {
        
        this.objectId = new ObjectId(user.getString("_id"));
        
        JSONObject userJson = new JSONObject(JSON.serialize(user));
        
        this.id = (Long) userJson.opt("userid");        
        this.username = (String) userJson.opt("username");
        this.password = (String) userJson.opt("password");
        this.expired = userJson.optBoolean("expired");
        this.enabled = userJson.optBoolean("enabled");
        this.passwordTime = (Long) userJson.opt("passwordTime");        
        this.creationTime = (Long) userJson.opt("creationTime");
        this.updateTime = (Long) userJson.opt("updateTime");
        this.userInfo = Optional.ofNullable(userJson.optJSONObject("userInfo")).map(UserInfoBSON::new).orElseGet(UserInfoBSON::new);
        this.version = userJson.optInt("version");
        
        Optional.ofNullable(userJson.optJSONArray("roles")).ifPresent(r -> { 
            IntStream.range(0, r.length())
                     .mapToObj(i -> Optional.ofNullable(r.optJSONObject(i)) )
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .map(RoleBSON::new)
                     .forEach(roles::add);
                     
                    
              
        });
    }
    
    public static UserBSON newUser(String username, String password, boolean expired, boolean enabled, UserInfoBSON userInfo) {

        UserBSON newuser = new UserBSON();
        
        Instant creationInstant = Instant.now();
        
        newuser.objectId = ObjectId.get();
        newuser.id = Long.parseUnsignedLong(newuser.objectId.toHexString().substring(8), 16);
        newuser.username = username;
        newuser.password = password;
        newuser.expired = expired;
        newuser.enabled = enabled;
        newuser.passwordTime = creationInstant.toEpochMilli();        
        newuser.creationTime = creationInstant.toEpochMilli();
        newuser.updateTime = creationInstant.toEpochMilli();
        newuser.userInfo = userInfo;
        
        newuser.version = 0;
        
        return newuser;
    } 

    public UserBSON() {
        
    }

    @Override
    public Long getId() {        
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    public Long getPasswordTime() {
        return passwordTime;
    }

    public void setPasswordTime(Date passwordTime) {
        this.passwordTime = passwordTime.getTime();
    }
    
    public void setPasswordTime(Long passwordTime) {
        this.passwordTime = passwordTime;
    }

    @Override
    public boolean isExpired() {
        return expired;
    }

    @Override
    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(new HashSet<>(roles));
    }

    @Override
    public it.greenvulcano.gvesb.iam.domain.UserInfo getUserInfo() {
        if (userInfo == null) {
            userInfo = new UserInfoBSON();
        }
        return userInfo;
    }

    @Override
    public void setUserInfo(it.greenvulcano.gvesb.iam.domain.UserInfo userInfo) {
        if (userInfo != null) {
            this.userInfo = new UserInfoBSON();
            this.userInfo.setEmail(userInfo.getEmail());
            this.userInfo.setFullname(userInfo.getFullname());
        } else {
            this.userInfo = null;
        }

    }

    public Long getCreationTime() {
        return creationTime;
    }
    
    public Long getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Date updateTime) {    
        this.updateTime = updateTime.getTime();
    }
    
    public void setUpdateTime(Long updateTime) {    
        this.updateTime = updateTime;
    }

    public int getVersion() {
        return version;
    }
    
    public void setVersion(int version) {        
        this.version = version;
    }
  

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((creationTime == null) ? 0 : creationTime.hashCode());
        result = prime * result + (enabled ? 1231 : 1237);
        result = prime * result + (expired ? 1231 : 1237);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((passwordTime == null) ? 0 : passwordTime.hashCode());
        result = prime * result + ((roles == null) ? 0 : roles.hashCode());
        result = prime * result + ((updateTime == null) ? 0 : updateTime.hashCode());
        result = prime * result + ((userInfo == null) ? 0 : userInfo.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        result = prime * result + version;
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
        UserBSON other = (UserBSON) obj;
        if (creationTime == null) {
            if (other.creationTime != null)
                return false;
        } else if (!creationTime.equals(other.creationTime))
            return false;
        if (enabled != other.enabled)
            return false;
        if (expired != other.expired)
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (passwordTime == null) {
            if (other.passwordTime != null)
                return false;
        } else if (!passwordTime.equals(other.passwordTime))
            return false;
        if (roles == null) {
            if (other.roles != null)
                return false;
        } else if (!roles.equals(other.roles))
            return false;
        if (updateTime == null) {
            if (other.updateTime != null)
                return false;
        } else if (!updateTime.equals(other.updateTime))
            return false;
        if (userInfo == null) {
            if (other.userInfo != null)
                return false;
        } else if (!userInfo.equals(other.userInfo))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        if (version != other.version)
            return false;
        return true;
    }

    @Override
    public String toString() {
           return toJSONObject().toString();
    }
    
    public JSONObject toJSONObject() {
        JSONObject user = new JSONObject();
        
        user.put("_id", objectId.toHexString())
            .put("userid", id)
            .put("username", username)
            .put("password", password)
            .put("passwordTime", passwordTime)
            .put("creationTime", creationTime)
            .put("updateTime", updateTime)
            .put("expired", expired)
            .put("enabled", enabled)
            .put("version", version)
            .put("userInfo",  UserInfoBSON.class.cast(getUserInfo()).toJSONObject())
            .put("roles", roles.stream().map(RoleBSON::toJSONObject)
                                        .collect(JSONArray::new, (a, r)-> a.put(r), (a1, a2) ->  {}));
        
           return user;
    }

    @Override
    public void addRole(Role role) {

        if (role != null) {
            RoleBSON r = new RoleBSON(role.getName(), role.getDescription());
            roles.add(r);
        }

    }

    @Override
    public void addRoles(Collection<Role> roles) {

        if (roles != null) {
            roles.forEach(this::addRole);
        }

    }

    @Override
    public void removeRole(String name) {
        roles.stream().filter(r -> r.getName().equals(name)).findAny().ifPresent(roles::remove);
    }

    @Override
    public void clearRoles() {
        roles.clear();

    }

}
