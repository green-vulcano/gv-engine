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

public class RoleBSON extends it.greenvulcano.gvesb.iam.domain.Role {

    private String name;
    private String description;

    public RoleBSON() {

    }
    
    public RoleBSON(JSONObject role) {        
        this.name = role.optString("name", null);
        this.description = role.optString("description", null);

    }

    public RoleBSON(String name, String description) {

        this.name = name;
        this.description = description;
    }

    @Override
    public Integer getId() {

        if (name != null) {
            return name.hashCode();
        }

        return null;
    }

    @Override
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @Override
    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    @Override
    public int hashCode() {
        return (name == null) ? 0 : name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RoleBSON other = (RoleBSON) obj;        
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return toJSONObject().toString();
        
    }
    
    public JSONObject toJSONObject() {
        JSONObject role = new JSONObject();
        role.put("id", getId())
            .put("name", name)
            .put("description", description);
        
        return role;
    }

}