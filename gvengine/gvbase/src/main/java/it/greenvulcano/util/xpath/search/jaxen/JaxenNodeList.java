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
package it.greenvulcano.util.xpath.search.jaxen;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A simple implementation for org.w3c.dom.NodeList.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JaxenNodeList implements NodeList
{
    /**
     * Nodes into the list.
     */
    private Node[] nodes = null;

    /**
     * Creates a new NodeList starting from a List of Node.
     *
     * @param contextNode
     *
     * @param list
     *        List of Node
     */
    public JaxenNodeList(Node contextNode, List<Object> list)
    {
        nodes = new Node[list.size()];
        if (!list.isEmpty()) {
            if (list.get(0) instanceof Node) {
                list.toArray(nodes);
            }
            else {
                Document doc = (contextNode.getOwnerDocument() == null)
                        ? (Document) contextNode
                        : contextNode.getOwnerDocument();
                int ll = list.size();
                for (int i = 0; i < ll; i++) {
                    nodes[i] = doc.createTextNode("" + list.get(i));
                }
            }
        }
    }

    /**
     * @return the number of Node into the list.
     */
    public int getLength()
    {
        return nodes.length;
    }

    /**
     * @param index
     *        index of the required Node
     * @return the Node of given index
     */
    public Node item(int index)
    {
        return nodes[index];
    }
}
