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
public class VCLOperationKey implements ResourceKey
{
    private String system    = null;
    private String channel   = null;
    private String operation = null;

    VCLOperationKey()
    {
        // do nothing
    }

    /**
     * @param system
     * @param service
     * @param operation
     */
    public VCLOperationKey(String system, String channel, String operation)
    {
        this.system = (system == null) ? "" : system;
        this.channel = (channel == null) ? "" : channel;
        this.operation = (operation == null) ? "" : operation;
    }

    @Override
    public void init(Node node) throws ACLException
    {
        this.system = XMLConfig.get(node, "@system", "");
        this.channel = XMLConfig.get(node, "@channel", "");
        this.operation = XMLConfig.get(node, "@operation", "");
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.policy.ResourceKey#getKey()
     */
    @Override
    public String getKey()
    {
        return "VCL#" + system + "#" + channel + "#" + operation;
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
        if (resource instanceof VCLOperationKey) {
            if (!"".equals(system)) {
                if (!system.equals(((VCLOperationKey) resource).system)) {
                    return false;
                }
            }
            if (!"".equals(channel)) {
                if (!channel.equals(((VCLOperationKey) resource).channel)) {
                    return false;
                }
            }
            if (!"".equals(operation)) {
                if (!operation.equals(((VCLOperationKey) resource).operation)) {
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
