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
package it.greenvulcano.gvesb.gviamx.domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.json.JSONObject;

@Entity
@DiscriminatorValue(value = "SIGNUP")
public class SignUpRequest extends UserActionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(nullable = true)
    @Lob()
    @Type(type = "org.hibernate.type.BinaryType")
    @Basic(fetch = FetchType.LAZY)
    private byte[] request;

    @Transient
    private JSONObject requestObject;

    public byte[] getRequest() {

        return request;
    }

    public void setRequest(byte[] request) {

        this.request = request;
    }

    @Transient
    @Override
    public JSONObject toJSONObject() {

        JSONObject userRequest = super.toJSONObject()
                   .put("action", new JSONObject().put("type", "signup")
                                                  .put("data", getRequestObject()) ); 
        
        return userRequest;
    }

    @Transient
    public JSONObject getRequestObject() {

        synchronized (this) {
            if (requestObject == null) {
                try {
                    requestObject = new JSONObject(new String(request, "UTF-8"));
                } catch (Exception e) {
                    requestObject = new JSONObject();
                }
            }
        }
        
        return requestObject;
    }

    @Transient
    public Map<String, Object> getActionData() {
       
        return getRequestObject().toMap();
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(request);
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
        SignUpRequest other = (SignUpRequest) obj;
        if (!Arrays.equals(request, other.request))
            return false;
        return true;
    }

}
