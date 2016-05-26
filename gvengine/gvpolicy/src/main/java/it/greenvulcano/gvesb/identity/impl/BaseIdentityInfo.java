/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 */
package it.greenvulcano.gvesb.identity.impl;

import it.greenvulcano.gvesb.identity.IdentityInfo;

import java.util.List;
import java.util.Set;

import org.apache.commons.net.util.SubnetUtils.SubnetInfo;

/**
 * @version 3.2.0 02/feb/2012
 * @author GreenVulcano Developer Team
 */
public abstract class BaseIdentityInfo implements IdentityInfo
{
    protected boolean    debug  = false;
    private IdentityInfo parent = null;

    public BaseIdentityInfo()
    {
        debug = Boolean.getBoolean("it.greenvulcano.gvesb.identity.IdentityInfo.debug");
    }

    @Override
    public final void setParent(IdentityInfo parent)
    {
        System.out.println(this + " - setParent[" + parent + "]");
        this.parent = parent;
    }

    @Override
    public boolean isInRole(String[] roles)
    {
        if (roles == null) {
            return false;
        }
        for (int i = 0; i < roles.length; i++) {
            if (isInRole(roles[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isInRole(List<String> roles)
    {
        if (roles == null) {
            return false;
        }
        for (String role : roles) {
            if (isInRole(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isInRole(Set<String> roles)
    {
        if (roles == null) {
            return false;
        }
        for (String role : roles) {
            if (isInRole(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final boolean isInRole(String role)
    {
        if (role == null) {
            return false;
        }
        boolean res = subIsInRole(role);
        if (!res && (parent != null)) {
            res = parent.isInRole(role);
        }
        return res;
    }

    @Override
    public final boolean matchAddress(String address)
    {
        if (address == null) {
            return false;
        }
        boolean res = subMatchAddress(address);
        if (!res && (parent != null)) {
            res = parent.matchAddress(address);
        }
        return res;
    }

    @Override
    public final boolean matchAddress(String[] addresses)
    {
        if (addresses == null) {
            return false;
        }
        for (int i = 0; i < addresses.length; i++) {
            if (matchAddress(addresses[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final boolean matchAddress(List<String> addresses)
    {
        if (addresses == null) {
            return false;
        }
        for (String address : addresses) {
            if (matchAddress(address)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final boolean matchAddress(Set<String> addresses)
    {
        if (addresses == null) {
            return false;
        }
        for (String address : addresses) {
            if (matchAddress(address)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final boolean matchAddressMask(Set<SubnetInfo> addressMasks)
    {
        if (addressMasks == null) {
            return false;
        }
        for (SubnetInfo addressMask : addressMasks) {
            if (subMatchAddressMask(addressMask)) {
                return true;
            }
        }
        return false;
    }

    protected abstract boolean subIsInRole(String role);

    protected abstract boolean subMatchAddress(String address);

    protected abstract boolean subMatchAddressMask(SubnetInfo addressMask);
}
