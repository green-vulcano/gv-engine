/*******************************************************************************
 * Copyright (c) 2009, 2020 GreenVulcano ESB Open Source Project.
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

import org.json.JSONObject;

public class UserInfoBson extends it.greenvulcano.gvesb.iam.domain.UserInfo {

    private String fullname;
    private String email;
    
    public UserInfoBson() {

    }

    public UserInfoBson(JSONObject info) {
        this.fullname = info.optString("fullname", null);
        this.email = info.optString("email", null);
        
    }

    
    @Override
    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((fullname == null) ? 0 : fullname.hashCode());
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
        UserInfoBson other = (UserInfoBson) obj;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (fullname == null) {
            if (other.fullname != null)
                return false;
        } else if (!fullname.equals(other.fullname))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return toJSONObject().toString();
        
    }
    
    public JSONObject toJSONObject() {
        JSONObject info = new JSONObject();
        info.put("fullname", fullname)
            .put("email", email);
        
        return info;
    }

}
