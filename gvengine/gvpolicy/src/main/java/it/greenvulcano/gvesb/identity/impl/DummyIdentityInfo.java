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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.net.util.SubnetUtils.SubnetInfo;

/**
 * @version 3.2.0 02/feb/2012
 * @author GreenVulcano Developer Team
 */
public class DummyIdentityInfo extends BaseIdentityInfo
{
    private String      name      = "";
    private Set<String> roles     = new HashSet<String>();
    private Set<String> addresses = new HashSet<String>();

    public DummyIdentityInfo(String name, String roles, String addresses)
    {
        super();
        this.name = name;

        String[] rl = roles.split(",");
        for (int i = 0; i < rl.length; i++) {
            this.roles.add(rl[i]);
        }

        String[] ad = addresses.split(",");
        for (int i = 0; i < ad.length; i++) {
            if (ad[i].trim().length() > 8) {
                this.addresses.add(ad[i]);
            }
        }
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    protected boolean subIsInRole(String role)
    {
        if (role == null) {
            return false;
        }
        boolean res = roles.contains(role);
        System.out.println("DummyIdentityInfo[" + getName() + "]: Role[" + role + "] -> " + res);
        return res;
    }

    @Override
    protected boolean subMatchAddress(String address)
    {
        if (address == null) {
            return false;
        }
        boolean res = addresses.contains(address);
        System.out.println("DummyIdentityInfo[" + getName() + "]: Address[" + addresses + ": " + address + "] -> "
                + res);
        return res;
    }

    @Override
    protected boolean subMatchAddressMask(SubnetInfo addressMask)
    {
        if (addressMask == null) {
            return false;
        }
        boolean res = false;
        String address = null;
        for (String a : addresses) {
            address = a;
            res = addressMask.isInRange(address);
            System.out.println("[" + address + "] -> " + addressMask + " : " + res);
            if (res) {
                break;
            }
        }
        System.out.println("DummyIdentityInfo[" + getName() + "]: AddressMask[" + addressMask.getCidrSignature() + ": "
                + address + "] -> " + res);
        return res;
    }

    @Override
    public String toString()
    {
        return "DummyIdentityInfo[" + getName() + "]";
    }
}
