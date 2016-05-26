/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.greenvulcano.util.xpath.search;

import javax.xml.transform.TransformerException;

/**
 * Interface that the actual implementation must implement.
 *
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
*/
public interface XPathAPIImpl {
    /**
     * Reset the XPath. Some implementations want to reset some data structures
     * when the underlying document changes.
     */
    void reset();

    /**
     * @param contextNode
     *            context node for the XPath evaluation
     * @param xpath
     *            implementation level XPath to evaluate
     * @return an iterator for the selected nodes
     * @throws TransformerException
     *             if an error occurs
     */
    Object selectNodeIterator(Object contextNode, Object xpath) throws TransformerException;

    /**
     * @param contextNode
     *            context node for the XPath evaluation
     * @param xpath
     *            implementation level XPath to evaluate
     * @param namespaceNode
     *            namespace node
     * @return an iterator for the selected nodes
     * @throws TransformerException
     *             if an error occurs
     */
    Object selectNodeIterator(Object contextNode, Object xpath, Object namespaceNode) throws TransformerException;

    /**
     * @param contextNode
     *            context node for the XPath evaluation
     * @param xpath
     *            implementation level XPath to evaluate
     * @return the selected nodes
     * @throws TransformerException
     *             if an error occurs
     */
    Object selectNodeList(Object contextNode, Object xpath) throws TransformerException;

    /**
     * @param contextNode
     *            context node for the XPath evaluation
     * @param xpath
     *            implementation level XPath to evaluate
     * @param namespaceNode
     *            namespace node
     * @return the selected nodes
     * @throws TransformerException
     *             if an error occurs
     */
    Object selectNodeList(Object contextNode, Object xpath, Object namespaceNode) throws TransformerException;

    /**
     * @param contextNode
     *            context node for the XPath evaluation
     * @param xpath
     *            implementation level XPath to evaluate
     * @return the selected Node
     * @throws TransformerException
     *             if an error occurs
     */
    Object selectSingleNode(Object contextNode, Object xpath) throws TransformerException;

    /**
     * @param contextNode
     *            context node for the XPath evaluation
     * @param xpath
     *            implementation level XPath to evaluate
     * @param namespaceNode
     *            namespace node
     * @return the selected Node
     * @throws TransformerException
     *             if an error occurs
     */
    Object selectSingleNode(Object contextNode, Object xpath, Object namespaceNode) throws TransformerException;
}
