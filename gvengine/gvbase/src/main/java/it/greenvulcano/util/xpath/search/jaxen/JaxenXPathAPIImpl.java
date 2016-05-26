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

import it.greenvulcano.util.xpath.search.XPathAPIImpl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.axiom.om.OMNode;
import org.apache.commons.collections.iterators.ListIteratorWrapper;
import org.jaxen.JaxenException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Executes Jaxen XPaths.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class JaxenXPathAPIImpl implements XPathAPIImpl
{
    private final boolean STDOUT_LOGGING_ENABLED = false;

    /**
     * @see it.greenvulcano.util.xpath.search.XPathAPIImpl#reset()
     */
    public void reset()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.util.xpath.search.XPathAPIImpl#selectNodeIterator(Object,
     *      Object)
     */
    public Object selectNodeIterator(Object contextNode, Object xpath) throws TransformerException
    {
        return selectNodeIterator(contextNode, xpath, null);
    }

    /**
     * @see it.greenvulcano.util.xpath.search.XPathAPIImpl#selectNodeIterator(Object,
     *      Object, Object)
     */    
    public Object selectNodeIterator(Object contextNode, Object xpath, Object namespaceNode)
            throws TransformerException
    {
        JaxenXPath lowlevelXPath = (JaxenXPath) xpath;
        try {
            CurrentFunction.putCurrent(contextNode);
            List list = lowlevelXPath.selectNodes(contextNode);
            return contextNode instanceof OMNode ? new ListIteratorWrapper(list.iterator()) : new JaxenNodeIterator(
                    (Node) contextNode, list);
        }
        catch (JaxenException e) {
            throw new TransformerException(e);
        }
        finally {
            CurrentFunction.removeCurrent();
        }
    }

    /**
     * @see it.greenvulcano.util.xpath.search.XPathAPIImpl#selectNodeList(Object,
     *      Object)
     */
    public Object selectNodeList(Object contextNode, Object xpath) throws TransformerException
    {
        return selectNodeList(contextNode, xpath, null);
    }

    /**
     * @see it.greenvulcano.util.xpath.search.XPathAPIImpl#selectNodeList(Object,
     *      Object, Object)
     */
    public Object selectNodeList(Object contextNode, Object xpath, Object namespaceNode) throws TransformerException
    {
        JaxenXPath lowlevelXPath = (JaxenXPath) xpath;
        try {
            CurrentFunction.putCurrent(contextNode);
            List<Object> list = lowlevelXPath.selectNodes(contextNode);
            return contextNode instanceof OMNode ? list : new JaxenNodeList((Node) contextNode, list);
        }
        catch (JaxenException e) {
            throw new TransformerException(e);
        }
        catch (RuntimeException exc) {
            exc.printStackTrace();
            throw new RuntimeException("nodeName="
                    + (contextNode instanceof Node ? ((Node) contextNode).getNodeName() : "unknown") + ", xpath="
                    + lowlevelXPath.toString(), exc);
        }
        finally {
            CurrentFunction.removeCurrent();
        }
    }

    /**
     * @see it.greenvulcano.util.xpath.search.XPathAPIImpl#selectSingleNode(Object,
     *      Object)
     */
    public Object selectSingleNode(Object contextNode, Object xpath) throws TransformerException
    {
        return selectSingleNode(contextNode, xpath, null);
    }

    /**
     * @see it.greenvulcano.util.xpath.search.XPathAPIImpl#selectSingleNode(Object,
     *      Object, Object)
     */
    public Object selectSingleNode(Object contextNode, Object xpath, Object namespaceNode) throws TransformerException
    {
        JaxenXPath lowlevelXPath = (JaxenXPath) xpath;
        try {
            CurrentFunction.putCurrent(contextNode);
            List<Object> ret = lowlevelXPath.selectNodes(contextNode);
            if ((ret == null) || (ret.size() == 0)) {
                return null;
            }
            return extractNode(contextNode, ret);
        }
        catch (JaxenException e) {
            throw new TransformerException(e);
        }
        finally {
            CurrentFunction.removeCurrent();
        }
    }

    private Object extractNode(Object contextNode, Object obj)
    {
        if (obj == null) {
            return null;
        }
        if (obj instanceof OMNode) {
            return obj;
        }
        if (obj instanceof Node) {
            return obj;
        }
        else if (obj instanceof NodeList) {
            NodeList nodeList = (NodeList) obj;
            if (nodeList.getLength() == 0) {
                return null;
            }
            return nodeList.item(0);
        }
        else if (obj instanceof Collection) {
            Iterator<Object> it = ((Collection<Object>) obj).iterator();
            if (it.hasNext()) {
                return extractNode(contextNode, it.next());
            }
            return null;
        }
        else {
            log("WARN: Unespected object type: " + obj.getClass());
            Class[] interfaces = obj.getClass().getInterfaces();
            for (Class cls : interfaces) {
                log("WARN:   interface: " + cls);
            }
            log("WARN:   Creating TextNode");
            if (contextNode instanceof Node) {
                return ((Node) contextNode).getOwnerDocument().createTextNode(obj.toString());
            }
            else if (contextNode instanceof OMNode) {
                return ((OMNode) contextNode).getOMFactory().createOMText(obj.toString());
            }
            log("WARN:   Wrong context node: " + contextNode.getClass());
            return null;
        }
    }

    private void log(String message)
    {
        if (STDOUT_LOGGING_ENABLED) {
            System.out.println(message);
        }
    }

}
