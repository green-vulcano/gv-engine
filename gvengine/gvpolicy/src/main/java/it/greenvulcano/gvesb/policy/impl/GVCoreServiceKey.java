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
import it.greenvulcano.gvesb.policy.ACLException;
import it.greenvulcano.gvesb.policy.ResourceKey;

import org.w3c.dom.Node;

/**
 * @version 3.2.0 01/feb/2012
 * @author GreenVulcano Developer Team
 */
public class GVCoreServiceKey implements ResourceKey
{
    private String group     = null;
    private String service   = null;
    private String operation = null;

    GVCoreServiceKey()
    {
        // do nothing
    }

    /**
     * @param group
     * @param service
     * @param operation
     */
    public GVCoreServiceKey(String group, String service, String operation)
    {
        this.group = (group == null) ? "" : group;
        this.service = (service == null) ? "" : service;
        this.operation = (operation == null) ? "" : operation;
    }

    @Override
    public void init(Node node) throws ACLException
    {
        this.group = XMLConfig.get(node, "@group", "");
        this.service = XMLConfig.get(node, "@service", "");
        this.operation = XMLConfig.get(node, "@operation", "");
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.policy.ResourceKey#getKey()
     */
    @Override
    public String getKey()
    {
        return "SVC#" + group + "#" + service + "#" + operation;
    }

    @Override
    public int hashCode()
    {
        return getKey().hashCode();
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.policy.ResourceKey#match(it.greenvulcano.gvesb.policy.ResourceKey)
     */
    @Override
    public boolean match(ResourceKey resource)
    {
        if (resource instanceof GVCoreServiceKey) {
            if (!"".equals(group)) {
                if (!group.equals(((GVCoreServiceKey) resource).group)) {
                    return false;
                }
            }
            if (!"".equals(service)) {
                if (!service.equals(((GVCoreServiceKey) resource).service)) {
                    return false;
                }
            }
            if (!"".equals(operation)) {
                if (!operation.equals(((GVCoreServiceKey) resource).operation)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return getKey();
    }
}
