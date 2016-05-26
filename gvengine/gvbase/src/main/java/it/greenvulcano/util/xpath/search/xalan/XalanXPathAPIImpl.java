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
package it.greenvulcano.util.xpath.search.xalan;

import it.greenvulcano.util.xpath.search.XPathAPIImpl;

import javax.xml.transform.TransformerException;

import org.apache.xpath.NodeSet;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * Executes Xalan XPaths.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
class XalanXPathAPIImpl implements XPathAPIImpl
{
    /**
     * Low level support for the XPath.
     */
    private XPathContext xpathContext;

    /**
     * Build a new XalanXPathAPIImpl.
     */
    public XalanXPathAPIImpl()
    {
        // do nothing
    }

    /**
     *
     */
    public void reset()
    {
        xpathContext = new XPathContext(ExtensionsManager.instance());
    }

    /**
     * @param contextNode
     *        context node for the XPath evaluation
     * @param xpath
     *        XPath to evaluate
     * @return an iterator for the selected nodes
     * @throws TransformerException
     *         if an error occurs
     */
    public Object selectNodeIterator(Object contextNode, Object xpath) throws TransformerException
    {
        try {
            ExtensionsManager.instance().startEvaluation((Node) contextNode);
            XObject xobj = eval((Node) contextNode, xpath, (Node) contextNode);
            if (xobj.getType() == XObject.CLASS_NODESET) {
                return xobj.nodeset();
            }

            return new NodeSet(createTextNode((Node) contextNode, xobj));
        }
        finally {
            ExtensionsManager.instance().endEvaluation();
        }
    }

    /**
     * @param contextNode
     *        context node for the XPath evaluation
     * @param xpath
     *        XPath to evaluate
     * @param namespaceNode
     *        namespace node
     * @return an iterator for the selected nodes
     * @throws TransformerException
     *         if an error occurs
     */
    public Object selectNodeIterator(Object contextNode, Object xpath, Object namespaceNode)
            throws TransformerException
    {
        try {
            ExtensionsManager.instance().startEvaluation((Node) contextNode);
            XObject xobj = eval((Node) contextNode, xpath, (Node) namespaceNode);
            if (xobj.getType() == XObject.CLASS_NODESET) {
                return xobj.nodeset();
            }

            return new NodeSet(createTextNode((Node) contextNode, xobj));
        }
        finally {
            ExtensionsManager.instance().endEvaluation();
        }
    }

    /**
     * @param contextNode
     *        context node for the XPath evaluation
     * @param xpath
     *        XPath to evaluate
     * @return the selected nodes
     * @throws TransformerException
     *         if an error occurs
     */
    public Object selectNodeList(Object contextNode, Object xpath) throws TransformerException
    {
        try {
            ExtensionsManager.instance().startEvaluation((Node) contextNode);
            XObject xobj = eval((Node) contextNode, xpath, (Node) contextNode);
            if (xobj.getType() == XObject.CLASS_NODESET) {
                return xobj.nodelist();
            }
            NodeSet nodeSet = new NodeSet(createTextNode((Node) contextNode, xobj));
            nodeSet.setShouldCacheNodes(true);
            return nodeSet;
        }
        finally {
            ExtensionsManager.instance().endEvaluation();
        }
    }

    /**
     * @param contextNode
     *        context node for the XPath evaluation
     * @param xpath
     *        XPath to evaluate
     * @param namespaceNode
     *        namespace node
     * @return the selected nodes
     * @throws TransformerException
     *         if an error occurs
     */
    public Object selectNodeList(Object contextNode, Object xpath, Object namespaceNode) throws TransformerException
    {
        try {
            ExtensionsManager.instance().startEvaluation((Node) contextNode);
            XObject xobj = eval((Node) contextNode, xpath, (Node) namespaceNode);
            if (xobj.getType() == XObject.CLASS_NODESET) {
                return xobj.nodelist();
            }
            NodeSet nodeSet = new NodeSet(createTextNode((Node) contextNode, xobj));
            nodeSet.setShouldCacheNodes(true);
            return nodeSet;
        }
        finally {
            ExtensionsManager.instance().endEvaluation();
        }
    }

    /**
     * @param contextNode
     *        context node for the XPath evaluation
     * @param xpath
     *        XPath to evaluate
     * @return the selected Node
     * @throws TransformerException
     *         if an error occurs
     */
    public final Object selectSingleNode(Object contextNode, Object xpath) throws TransformerException
    {
        try {
            ExtensionsManager.instance().startEvaluation((Node) contextNode);
            NodeIterator iterator = (NodeIterator) selectNodeIterator(contextNode, xpath);
            if (iterator != null) {
                return iterator.nextNode();
            }
            return null;
        }
        finally {
            ExtensionsManager.instance().endEvaluation();
        }
    }

    /**
     * @param contextNode
     *        context node for the XPath evaluation
     * @param xpath
     *        XPath to evaluate
     * @param namespaceNode
     *        namespace node
     * @return the selected Node
     * @throws TransformerException
     *         if an error occurs
     */
    public final Object selectSingleNode(Object contextNode, Object xpath, Object namespaceNode) throws TransformerException
    {
        try {
            ExtensionsManager.instance().startEvaluation((Node) contextNode);
            NodeIterator iterator = (NodeIterator) selectNodeIterator(contextNode, xpath, namespaceNode);
            if (iterator != null) {
                return iterator.nextNode();
            }
            return null;
        }
        finally {
            ExtensionsManager.instance().endEvaluation();
        }
    }

    /**
     * Evaluate a XPath. <br/>
     *
     * @param contextNode
     *        context node for the XPath evaluation
     * @param xpath
     *        XPath to evaluate
     * @param namespaceNode
     *        namespace node
     * @return the low level result of the XPath
     * @throws TransformerException
     *         if an error occurs
     */
    private XObject eval(Node contextNode, Object xpathObject, Node namespaceNode) throws TransformerException
    {
        XPath xpath = (XPath) xpathObject;
        int ctxtNode = xpathContext.getDTMHandleFromNode(contextNode);
        return xpath.execute(xpathContext, ctxtNode, PrefixResolver.instance());
    }

    /**
     * Build a text node from an XObject. The contained text is the
     * XObject.toString() return value.
     *
     * @param contextNode
     *        a node of the Document
     * @param xobj
     *        XObject to convert in a String
     * @return the new created Node
     */
    private static Node createTextNode(Node contextNode, XObject xobj)
    {
        Document dom = contextNode.getOwnerDocument();
        return dom.createTextNode(xobj.toString());
    }
}
