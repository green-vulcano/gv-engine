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
package it.greenvulcano.util.xpath.search;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class executes an XPath over a Node. <br/>
 * NOTE: the <code>XPathAPI</code> return a node also if the result of the XPath
 * is a string or number. Such node is a TextNode.
 * <p/>
 * <b>ATTENTION:</b> because the XPathAPI contains a lower level object that
 * needs to be reset when the document changes, please call the
 * <code>reset()</code> method each time the document changes.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XPathAPI
{
    /**
     * Register an XPath extension function.
     *
     * @param namespace
     * @param name
     * @param function
     */
    public static void installFunction(String namespace, String name, XPathFunction function)
    {
        XPathAPIFactory.instance().installFunction(namespace, name, function);
    }

    /**
     * Register a namespace to be used in XPath.
     *
     * @param prefix
     * @param namespace
     */
    public static void installNamespace(String prefix, String namespace)
    {
        XPathAPIFactory.instance().installNamespace(prefix, namespace);
    }

    private XPathAPIImpl implementation;

    /**
     *
     */
    public XPathAPI()
    {
        implementation = XPathAPIFactory.instance().newXPathAPIImpl();
        reset();
    }

    /**
     * Return the value for a node.
     *
     * @param node
     *        input Node.
     *
     * @return the node value. The value for an Element is the concatenation of
     *         children values. For other nodes the value is
     *         <code>node.getNodeValue()</code>.
     */
    public static String getNodeValue(Node node)
    {
        if (node instanceof Element) {
            StringBuilder buf = new StringBuilder();
            Node child = node.getFirstChild();
            while (child != null) {
                String val = getNodeValue(child);
                if (val != null) {
                    buf.append(val);
                }
                child = child.getNextSibling();
            }
            return buf.toString();
        }
        return node.getNodeValue();
    }

    /**
     * Return the value of a NodeList as concatenation of values of all nodes
     * contained in the list.
     *
     * @param node
     *        the node list
     * @return the nodes value
     */
    public static String getNodeValue(NodeList node)
    {
        StringBuilder buf = new StringBuilder();
        NodeList list = node;
        int n = list.getLength();
        for (int i = 0; i < n; ++i) {
            String val = getNodeValue(list.item(i));
            if (val != null) {
                buf.append(val);
            }
        }
        return buf.toString();
    }

    /**
     * Reset the XPath. Some implementations want to reset some data structures
     * when the underlying document changes.
     */
    public final void reset()
    {
        implementation.reset();
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
    public final Object selectNodeIterator(Object contextNode, XPath xpath) throws TransformerException
    {
        return implementation.selectNodeIterator(contextNode, getXPath(contextNode, xpath));
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
    public final Object selectNodeIterator(Object contextNode, XPath xpath, Object namespaceNode)
            throws TransformerException
    {
        return implementation.selectNodeIterator(contextNode, getXPath(contextNode, xpath), namespaceNode);
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
    public final Object selectNodeList(Object contextNode, XPath xpath) throws TransformerException
    {
        return implementation.selectNodeList(contextNode, getXPath(contextNode, xpath));
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
    public final Object selectNodeList(Object contextNode, XPath xpath, Object namespaceNode) throws TransformerException
    {
        return implementation.selectNodeList(contextNode, getXPath(contextNode, xpath), namespaceNode);
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
    public final Object selectSingleNode(Object contextNode, XPath xpath) throws TransformerException
    {
        return implementation.selectSingleNode(contextNode, getXPath(contextNode, xpath));
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
    public final Object selectSingleNode(Object contextNode, XPath xpath, Object namespaceNode) throws TransformerException
    {
        return implementation.selectSingleNode(contextNode, getXPath(contextNode, xpath), namespaceNode);
    }

    /**
     * Ritorna l'XPath di basso livello.
     *
     * @param contextNode
     *        nodo di contesto per eseguire l'eventuale translate()
     * @param xpath
     *        XPath
     * @return l'XPath di basso livello.
     * @throws TransformerException
     *         se l'XPath � errato
     */
    public final Object getXPath(Object contextNode, XPath xpath) throws TransformerException
    {
        return xpath.getXPath();
    }

    /**
     * @param baseConfigPath
     */
    public static void setBaseConfigPath(String baseConfigPath)
    {
        XPathAPIFactory.setBaseConfigPath(baseConfigPath);
    }
}
