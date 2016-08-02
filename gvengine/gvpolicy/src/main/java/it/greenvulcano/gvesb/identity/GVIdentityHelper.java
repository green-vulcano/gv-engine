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
package it.greenvulcano.gvesb.identity;

import it.greenvulcano.util.thread.ThreadMap;

import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.net.util.SubnetUtils;

/**
 * @version 3.2.0 01/feb/2012
 * @author GreenVulcano Developer Team
 */
public class GVIdentityHelper
{
    public static final String IDENTITY_HELPER_THREAD_KEY = "IDENTITY_HELPER_THREAD_KEY";

    @SuppressWarnings("unchecked")
    public static void push(IdentityInfo identity)
    {
        Stack<IdentityInfo> identities = (Stack<IdentityInfo>) ThreadMap.get(IDENTITY_HELPER_THREAD_KEY);
        if (identities == null) {
            identities = new Stack<IdentityInfo>();
            ThreadMap.put(IDENTITY_HELPER_THREAD_KEY, identities);
        }
        identity.setParent((identities.size() > 0) ? identities.peek() : null);
        identities.push(identity);
    }

    @SuppressWarnings("unchecked")
    public static IdentityInfo pop()
    {
        IdentityInfo identity = null;
        Stack<IdentityInfo> identities = (Stack<IdentityInfo>) ThreadMap.get(IDENTITY_HELPER_THREAD_KEY);
        if (identities != null) {
            identity = identities.isEmpty()? null : identities.pop();
            if (identities.size() == 0) {
                ThreadMap.remove(IDENTITY_HELPER_THREAD_KEY);
            }
        }
        return identity;
    }

    @SuppressWarnings("unchecked")
    private static IdentityInfo peek()
    {
        IdentityInfo identity = null;
        Stack<IdentityInfo> identities = (Stack<IdentityInfo>) ThreadMap.get(IDENTITY_HELPER_THREAD_KEY);
        if (identities != null) {
            identity = (identities.size() > 0) ? identities.peek() : null;
        }
        return identity;
    }

    public static String getName()
    {
        IdentityInfo identity = peek();
        return (identity != null) ? identity.getName() : "NONE";
    }

    public static boolean isInRole(String role)
    {
        IdentityInfo identity = peek();
        return (identity != null) ? identity.isInRole(role) : false;
    }

    public static boolean isInRole(String[] roles)
    {
        IdentityInfo identity = peek();
        return (identity != null) ? identity.isInRole(roles) : false;
    }

    public static boolean isInRole(List<String> roles)
    {
        IdentityInfo identity = peek();
        return (identity != null) ? identity.isInRole(roles) : false;
    }

    public static boolean isInRole(Set<String> roles)
    {
        IdentityInfo identity = peek();
        return (identity != null) ? identity.isInRole(roles) : false;
    }

    public static boolean matchAddress(String address)
    {
        IdentityInfo identity = peek();
        return (identity != null) ? identity.matchAddress(address) : false;
    }

    public static boolean matchAddress(String[] addresses)
    {
        IdentityInfo identity = peek();
        return (identity != null) ? identity.matchAddress(addresses) : false;
    }

    public static boolean matchAddress(List<String> addresses)
    {
        IdentityInfo identity = peek();
        return (identity != null) ? identity.matchAddress(addresses) : false;
    }

    public static boolean matchAddress(Set<String> addresses)
    {
        IdentityInfo identity = peek();
        return (identity != null) ? identity.matchAddress(addresses) : false;
    }

    public static boolean matchAddressMask(Set<SubnetUtils.SubnetInfo> addresses)
    {
        IdentityInfo identity = peek();
        return (identity != null) ? identity.matchAddressMask(addresses) : false;
    }
}
