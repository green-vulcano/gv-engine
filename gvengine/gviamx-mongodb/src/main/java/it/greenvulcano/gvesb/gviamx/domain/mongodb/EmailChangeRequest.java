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

import java.io.Serializable;
import java.util.Map;

import org.json.JSONObject;

import it.greenvulcano.gvesb.iam.domain.User;

public class EmailChangeRequest extends UserActionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private User user;

    public User getUser() {

        return user;
    }

    public void setUser(User user) {

        this.user = user;
    }

    public JSONObject toJSONObject() {

        JSONObject userRequest = super.toJSONObject().put("action", new JSONObject().put("type", "emailChange").put("data", JSONObject.NULL));

        return userRequest;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        EmailChangeRequest other = (EmailChangeRequest) obj;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        return true;
    }

    @Override
    public Map<String, Object> getActionData() {

        return null;
    }

}
