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
package it.greenvulcano.gvesb.policy.impl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.identity.GVIdentityHelper;
import it.greenvulcano.gvesb.policy.ACLEntry;
import it.greenvulcano.gvesb.policy.ACLException;
import it.greenvulcano.gvesb.policy.ACLManager;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.net.util.SubnetUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.2.0 01/feb/2012
 * @author GreenVulcano Developer Team
 */
public class ACLResource implements ACLEntry
{
    private Set<String>                 roles       = new HashSet<String>();
    private Set<String>                 addresses   = new HashSet<String>();
    private Set<SubnetUtils.SubnetInfo> addressMask = new HashSet<SubnetUtils.SubnetInfo>();

    ACLResource()
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.policy.ACLEntry#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws ACLException
    {
        try {
            NodeList nl = XMLConfig.getNodeList(node, "ACL/RoleRef");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                roles.add(XMLConfig.get(n, "@name"));
            }
            nl = XMLConfig.getNodeList(node, "ACL/AddressSetRef");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                String sn = XMLConfig.get(n, "@name");
                NodeList al = XMLConfig.getNodeList(ACLManager.CFG_FILE_NAME, "/GVPolicy/Addresses/AddressSet[@name='"
                        + sn + "']/Address");
                for (int j = 0; j < al.getLength(); j++) {
                    Node a = al.item(j);
                    String addr = XMLConfig.get(a, "@address");
                    addresses.add(addr);
                    if (addr.indexOf("/") == -1) {
                        addr += "/32";
                    }
                    SubnetUtils snet = new SubnetUtils(addr);
                    snet.setInclusiveHostCount(true);
                    addressMask.add(snet.getInfo());
                }
            }
        }
        catch (XMLConfigException exc) {
            throw new ACLException("Error initializing ACLResource", new String[][]{{"exc", "" + exc}});
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.policy.ACLEntry#canAccess()
     */
    @Override
    public boolean canAccess() throws ACLException
    {
        boolean roleOK = false;
        if (roles.isEmpty()) {
            roleOK = true;
        }
        else {
            roleOK = GVIdentityHelper.isInRole(roles);
        }
        boolean addressOK = false;
        if (addresses.isEmpty()) {
            addressOK = true;
        }
        else {
            addressOK = GVIdentityHelper.matchAddressMask(addressMask);
        }
        return roleOK && addressOK;
    }

    @Override
    public String toString()
    {
        return "ACLResource: Roles " + roles + " - Addresses " + addresses;
    }
}
