/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.virtual;

import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.util.xpath.XPathFinder;

import org.w3c.dom.Node;

/**
 * VCLOperationKey class
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author Gianluca Di Maio
 */
public class VCLOperationKey extends OperationKey
{
    private String key  = "";
    private String file = "VCLInternal.conf";
    private Node   node = null;

    /**
     * @param node
     */
    public VCLOperationKey(Node node)
    {
        this.node = node;
        key = XPathFinder.buildXPath(node);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.OperationKey#getKey()
     */
    @Override
    public String getKey()
    {
        return key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.OperationKey#getFile()
     */
    @Override
    public String getFile()
    {
        return file;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.OperationKey#getNode()
     */
    @Override
    public Node getNode() throws GVException
    {
        return node;
    }

}
