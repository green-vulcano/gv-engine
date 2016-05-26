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

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

/**
 * JaxenNodeIterator class
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JaxenNodeIterator implements NodeIterator
{
    private LinkedList<Node>   nodes;
    private Node               root;
    private ListIterator<Node> iterator;

    /**
     * @param root
     * @param nodes
     */
    public JaxenNodeIterator(Node root, List<Node> nodes)
    {
        this.root = root;
        this.nodes = new LinkedList<Node>(nodes);
        iterator = nodes.listIterator();
    }

    /**
     * @see org.w3c.dom.traversal.NodeIterator#getWhatToShow()
     */
    public int getWhatToShow()
    {
        return NodeFilter.SHOW_ALL;
    }

    /**
     * @see org.w3c.dom.traversal.NodeIterator#detach()
     */
    public void detach()
    {
        root = null;
        nodes = null;
    }

    /**
     * @see org.w3c.dom.traversal.NodeIterator#getExpandEntityReferences()
     */
    public boolean getExpandEntityReferences()
    {
        return false;
    }

    /**
     * @see org.w3c.dom.traversal.NodeIterator#getRoot()
     */
    public Node getRoot()
    {
        return root;
    }

    /**
     * @see org.w3c.dom.traversal.NodeIterator#nextNode()
     */
    public Node nextNode() throws DOMException
    {
        if ((root == null) && (nodes == null)) {
            throw new DOMException(DOMException.INVALID_STATE_ERR, "Invalid state error");
        }

        if (iterator.hasNext()) {
            return iterator.next();
        }

        return null;
    }

    /**
     * @see org.w3c.dom.traversal.NodeIterator#previousNode()
     */
    public Node previousNode() throws DOMException
    {
        if ((root == null) && (nodes == null)) {
            throw new DOMException(DOMException.INVALID_STATE_ERR, "Invalid state error");
        }

        if (iterator.hasPrevious()) {
            return iterator.previous();
        }

        return null;
    }

    /**
     * @see org.w3c.dom.traversal.NodeIterator#getFilter()
     */
    public NodeFilter getFilter()
    {
        return null;
    }
}
