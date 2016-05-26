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
package it.greenvulcano.util.xpath;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Extension of NodeList class for XPath handling. Necessary because implements
 * org.w3c.dom.NodeList, which is NOT derived from java.util.List
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class CNodeList implements NodeList
{

    /**
     * The internal data structure holding nodes
     */
    private ArrayList<Node> nodes = null;

    /**
     * Public constructor
     */
    public CNodeList()
    {
        nodes = new ArrayList<Node>(5);
    }

    /**
     * Appends a node to the list of nodes
     *
     * @param newNode
     *        Is the node to append
     *
     */
    public void append(Node newNode)
    {
        if (newNode == null) {
            return;
        }
        nodes.add(newNode);
    }

    /**
     * Get the i-th node of list at position <tt>index</tt>
     *
     * @param index
     *
     * @return Node at i-th position
     */
    public Node item(int index)
    {
        if (index >= nodes.size()) {
            return null;
        }
        return nodes.get(index);
    }

    /**
     * Get the length of list
     *
     * @return the size of the list
     */
    public int getLength()
    {
        return nodes.size();
    }

    /**
     * Clears the list
     */
    public void clear()
    {
        nodes.clear();
    }

}
