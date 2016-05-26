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

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * That class hel build a DOM from XPath.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XPathDOMBuilder
{

    /**
     *
     */
    public static final int CHILD_TYPE_ELEMENT = 0;
    /**
     *
     */
    public static final int CHILD_TYPE_TEXT    = 1;
    /**
     *
     */
    public static final int CHILD_TYPE_CDATA   = 2;

    /**
     * Empty constructor.
     */
    public XPathDOMBuilder()
    {
        // do nothing
    }

    /**
     * @return a new {@link Document}
     * @throws Exception
     */
    public Document createNewDocument() throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.newDocument();

        return document;
    }

    /**
     * Adds an <tt>Element</tt> to a given DOM. The element will match a given
     * XPath. IMPORTANT: All elements instance number within given XPath MUST be
     * specified (E.g. /ROOT[1]/ELEM1[3]/ELEM2[5] ...)
     *
     * @param document
     *        the DOM to which the new element must be added.
     * @param xPath
     *        the XPath matching the element to be added.
     * @return the element added to the DOM (as a Node).
     * @throws XPathUtilsException
     *         if any error occurs.
     */
    public Node addElement(Document document, String xPath) throws XPathUtilsException
    {

        int idx = xPath.lastIndexOf("/");
        String parentXPath = xPath.substring(0, idx);
        String nodeName = xPath.substring(idx + 1);
        int instance = 1;
        String elementName = nodeName;
        if (nodeName.indexOf("[") != -1) {
            int idx1 = nodeName.indexOf("[");
            int idx2 = nodeName.indexOf("]");
            instance = Integer.parseInt(nodeName.substring(idx1 + 1, idx2));
            elementName = nodeName.substring(0, idx1);
        }
        else {
            // No node instance specifier: assume first instance
            instance = 1;
        }

        return addElement(document, parentXPath, elementName, instance, CHILD_TYPE_ELEMENT, "");
    }

    /**
     * Adds an <tt>Text</tt> element to a given DOM. The <tt>Text</tt> element
     * will be the child of an <tt>Element</tt> matching a given XPath.
     * IMPORTANT: All elements instance number within given XPath MUST be
     * specified (E.g. /ROOT[1]/ELEM1[3]/ELEM2[5] ...)
     *
     * @param document
     *        the DOM to which the new element must be added.
     * @param xPath
     *        the XPath of the <tt>Element</tt> which will have the
     *        <tt>Text</tt> element as child.
     * @param elementValue
     *        the value (a text string) of the <tt>Text</tt> element.
     * @return the element added to the DOM (as a Node).
     * @throws XPathUtilsException
     *         if any error occurs.
     */
    public Node addTextElement(Document document, String xPath, String elementValue) throws XPathUtilsException
    {
        int idx = xPath.lastIndexOf("/");
        String parentXPath = xPath.substring(0, idx);
        String nodeName = xPath.substring(idx + 1);
        int instance = 1;
        String elementName = nodeName;
        if (nodeName.indexOf("[") != -1) {
            int idx1 = nodeName.indexOf("[");
            int idx2 = nodeName.indexOf("]");
            instance = Integer.parseInt(nodeName.substring(idx1 + 1, idx2));
            elementName = nodeName.substring(0, idx1);
        }
        else {
            // No node instance specifier: assume first instance
            instance = 1;
        }

        return addElement(document, parentXPath, elementName, instance, CHILD_TYPE_TEXT, elementValue);
    }

    /**
     * Adds an <tt>Attr</tt> node to given DOM. The corresponding attribute will
     * belong to an <tt>Element</tt> matching a given XPath.
     *
     * IMPORTANT: All elements instance number within given XPath MUST be
     * specified (E.g. /ROOT[1]/ELEM1[3]/ELEM2[5] ...)
     *
     * @param document
     *        the DOM to which the new element must be added.
     * @param xPath
     *        the XPath of the <tt>Element</tt> which will have the
     *        <tt>Attr</tt> node as an attribute.
     * @param attributeName
     *        the name of the new attribute.
     * @param attributeValue
     *        the value of the new attribute.
     * @return the <tt>Node</tt> added to the DOM
     * @throws XPathUtilsException
     *         if any error occurs.
     */
    public Node addAttribute(Document document, String xPath, String attributeName, String attributeValue)
            throws XPathUtilsException
    {
        NodeList nodeList = null;
        Node context = null;
        Attr newAttr = null;
        context = addElement(document, xPath);

        nodeList = new CNodeList();
        ((CNodeList) nodeList).append(context);

        nodeList = selectNodeWithAttribute(nodeList, attributeName, attributeValue);
        if ((nodeList != null) && (nodeList.getLength() > 0)) {
            return nodeList.item(0);
        }

        newAttr = document.createAttribute(attributeName);
        newAttr.setNodeValue(attributeValue);
        ((Element) context).setAttributeNode(newAttr);
        return newAttr;
    }

    /**
     * Adds an <tt>CDATASection</tt> element to a given DOM. The
     * <tt>CDATASection</tt> element will be the child of an <tt>Element</tt>
     * matching a given XPath. IMPORTANT: All elements instance number within
     * given XPath MUST be specified (E.g. /ROOT[1]/ELEM1[3]/ELEM2[5] ...)
     *
     * @param document
     *        the DOM to which the new element must be added.
     * @param xPath
     *        the XPath of the <tt>Element</tt> which will have the
     *        <tt>CDATASection</tt> element as child.
     * @param elementValue
     *        the value (a text string) of the <tt>Text</tt> element.
     * @return the element added to the DOM (as a Node).
     * @throws XPathUtilsException
     *         if any error occurs.
     */
    public Node addCDATAElement(Document document, String xPath, String elementValue) throws XPathUtilsException
    {
        int idx = xPath.lastIndexOf("/");
        String parentXPath = xPath.substring(0, idx);
        String nodeName = xPath.substring(idx + 1);
        int instance = 1;
        String elementName = nodeName;
        if (nodeName.indexOf("[") != -1) {
            int idx1 = nodeName.indexOf("[");
            int idx2 = nodeName.indexOf("]");
            instance = Integer.parseInt(nodeName.substring(idx1 + 1, idx2));
            elementName = nodeName.substring(0, idx1);
        }
        else {
            // No node instance specifier: assume first instance
            instance = 1;
        }

        return addElement(document, parentXPath, elementName, instance, CHILD_TYPE_CDATA, elementValue);
    }

    /**
     * Select attributes of context nodes.
     *
     * @param contextList
     * @param attrName
     * @param bSelectAll
     * @return attributes
     */
    protected NodeList selectAttribute(NodeList contextList, String attrName, boolean bSelectAll)
    {
        CNodeList nodeList = null;
        NamedNodeMap nm = null;
        Node c = null;
        Node n = null;
        int len = 0;
        int numCtx = contextList.getLength();
        nodeList = new CNodeList();

        for (int ic = 0; ic < numCtx; ic++) {
            c = contextList.item(ic);
            nm = c.getAttributes();

            if (nm == null) {
                continue;
            }

            len = nm.getLength();

            for (int i = 0; i < len; i++) {
                n = nm.item(i);
                if (bSelectAll) {
                    // Select all attributes
                    nodeList.append(n);
                }
                else if (n.getNodeName().equals(attrName)) {
                    // Insert only attribute of attrName name
                    nodeList.append(n);
                }
            }
        }
        return nodeList;
    }

    /**
     * Select the nodes in context that contains an attribute of name attrName and with value attrValue.
     *
     * @param contextList
     * @param attrName
     * @param attrValue
     * @return node with passed attribute
     */
    protected NodeList selectNodeWithAttribute(NodeList contextList, String attrName, String attrValue)
    {
        CNodeList nodeList = null;
        NamedNodeMap nm = null;
        Node c = null;
        Node n = null;
        int len = 0;
        int numCtx = contextList.getLength();

        nodeList = new CNodeList();
        for (int ic = 0; ic < numCtx; ic++) {
            c = contextList.item(ic);
            nm = c.getAttributes();

            if (nm == null) {
                continue;
            }
            len = nm.getLength();

            for (int i = 0; i < len; i++) {
                n = nm.item(i);

                if (n.getNodeName().equals(attrName)) {

                    if ((attrValue == null) || (attrValue.length() == 0)) {
                        // Select all attributes of name attrName
                        nodeList.append(c);
                        break;
                    }
                    else if ((n.getNodeValue() != null) && n.getNodeValue().equals(attrValue)) {
                        // Select only attributes of name attrName and value valore attrValue
                        nodeList.append(c);
                        break;
                    }
                }
            }
        }

        return nodeList;
    }

    /**
     * Select all children of context nodes.
     *
     * @param contextList
     * @param childName
     * @param bSelectAll
     * @return child nodes
     */
    protected NodeList selectChildren(NodeList contextList, String childName, boolean bSelectAll)
    {
        CNodeList nodeList = null;
        Node c = null;
        Node n = null;
        int numCtx = contextList.getLength();
        nodeList = new CNodeList();
        for (int ic = 0; ic < numCtx; ic++) {
            c = contextList.item(ic);
            if (c.getNodeType() == Node.ATTRIBUTE_NODE) {
                n = ((Document) c).getDocumentElement();
            }
            else {
                n = c.getFirstChild();
            }

            if (n == null) {
                continue;
            }

            while (n != null) {
                if (!n.getNodeName().equalsIgnoreCase("#text")) {
                    if (bSelectAll) {
                        nodeList.append(n); // select all children
                    }
                    else if (n.getNodeName().equals(childName)) {
                        nodeList.append(n); // select only children of name childName
                    }
                }
                n = n.getNextSibling();
            }
        }
        return nodeList;
    }

    /**
     * Return a NodeList containing the nodes that match the given XPath.
     *
     * @param context
     *        context node for XPath evaluation.
     * @param xPath
     *        xpath to evaluate.
     * @return
     */
    private NodeList findNode(Node context, String xPath)
    {
        Node base = context;
        Node appo = null;
        CNodeList nodeList = null;
        XPathTokenizer xpt = new XPathTokenizer(xPath);
        String tokenXPath = "";
        String attrName = "";
        String attrValue = "";
        String childName = "";
        int elemIndex = -1;
        boolean bBaseDefined = false;
        boolean bSelectAttribute = false;
        boolean bSelectChildren = true;
        boolean bInExpression = false;
        nodeList = new CNodeList();

        if (base == null) {
            return null;
        }

        while (xpt.hasMoreTokens()) {
            tokenXPath = xpt.nextToken();
            if (tokenXPath.equals("../")) {
                tokenXPath = "";
                base = base.getParentNode();
                if (base == null) {
                    return null;
                }
                nodeList.clear();
                nodeList.append(base);
                bBaseDefined = true;
                bSelectChildren = true;
            }
            else if (tokenXPath.equals("..")) {
                base = base.getParentNode();
                if (base == null) {
                    return null;
                }
                nodeList.append(base);
                return nodeList;
            }
            else if (tokenXPath.equals("./")) {
                tokenXPath = "";
                nodeList.clear();
                nodeList.append(base);
                bBaseDefined = true;
            }
            else if (tokenXPath.equals("/")) {
                tokenXPath = "";
                if (!bBaseDefined) {
                    if (base.getNodeType() != Node.DOCUMENT_NODE) {
                        base = ((Document) base).getOwnerDocument();
                    }
                    nodeList.append(base);
                    bBaseDefined = true;
                }
                bSelectChildren = true;
            }
            else if (tokenXPath.equals(".")) {
                nodeList.clear();
                nodeList.append(base);
                return nodeList;
            }
            else if (tokenXPath.equals("[")) {
                tokenXPath = "";
                bInExpression = true;
            }
            else if (tokenXPath.equals("]")) {
                tokenXPath = "";
                if (bInExpression) {
                    bInExpression = false;
                    if (elemIndex > -1) {
                        if (elemIndex >= nodeList.getLength()) {
                            return null;
                        }
                        appo = nodeList.item(elemIndex);
                        nodeList.clear();
                        nodeList.append(appo);
                    }
                }
            }
            else if (tokenXPath.equals("(")) {
                tokenXPath = "";
            }
            else if (tokenXPath.equals(")")) {
                tokenXPath = "";
            }
            else if (tokenXPath.equals("*")) {
                tokenXPath = "";
                if (bSelectAttribute) {
                    nodeList = (CNodeList) selectAttribute(nodeList, "", true);
                    bSelectAttribute = false;
                }
                else if (bSelectChildren) {
                    nodeList = (CNodeList) selectChildren(nodeList, "", true);
                    bSelectChildren = false;
                }
            }
            else if (tokenXPath.equals("@")) {
                tokenXPath = "";
                bSelectAttribute = true;
                bSelectChildren = false;
            }
            else {
                if (!bBaseDefined) {
                    nodeList.clear();
                    nodeList.append(base);
                    bBaseDefined = true;
                }
                if (bSelectAttribute) {
                    if (bInExpression) {
                        bInExpression = false;
                        attrName = tokenXPath;
                        String tmp = xpt.nextToken();
                        if (tmp.equals("=")) {
                            attrValue = xpt.nextToken();
                        }
                        else {
                            xpt.putBack();
                        }
                        nodeList = (CNodeList) selectNodeWithAttribute(nodeList, attrName, attrValue);
                        bSelectAttribute = false;
                    }
                    else {
                        attrName = tokenXPath;
                        nodeList = (CNodeList) selectAttribute(nodeList, attrName, false);
                        bSelectAttribute = false;
                    }
                    tokenXPath = "";
                }
                else if (bSelectChildren) {
                    childName = tokenXPath;
                    nodeList = (CNodeList) selectChildren(nodeList, childName, false);
                    bSelectChildren = false;
                    tokenXPath = "";
                }
                else if (bInExpression) {
                    elemIndex = Integer.parseInt(tokenXPath.trim()) - 1;
                    tokenXPath = "";
                }
                tokenXPath = "";
            }
        }

        if ((nodeList == null) || (nodeList.getLength() == 0)) {
            return null;
        }
        return nodeList;
    }

    /**
     * TODO - Write Javadocs
     */
    private Node addElement(Document document, String xPath, String elementName, int instance, int childType,
            String elementValue) throws XPathUtilsException
    {
        CNodeList nodeList = null;
        Node context = null;
        Node newNode = null;
        XPathTokenizer xpt = new XPathTokenizer(xPath);
        boolean baseDef = false;
        boolean bSelectChildren = true;
        boolean bInExpression = false;
        int elemIndex = -1;
        String tokenXPath = "";
        String xPath2 = "";
        String childName = "";

        try {
            while (xpt.hasMoreTokens()) {
                tokenXPath = xpt.nextToken();
                if (tokenXPath.equals("/")) {
                    tokenXPath = "";
                    if (!baseDef) {
                        context = document;
                        baseDef = true;
                        xPath2 = "/";
                    }
                    else {
                        xPath2 = "./";
                    }

                }
                else if (tokenXPath.equals("[")) {
                    tokenXPath = "";
                    bInExpression = true;
                }
                else if (tokenXPath.equals("]")) {
                    tokenXPath = "";
                    if (bInExpression) {
                        bInExpression = false;
                        int i = 0;
                        nodeList = (CNodeList) findNode(context, xPath2);
                        if ((nodeList != null) && (nodeList.getLength() > 0)) {
                            i = nodeList.getLength();
                        }
                        while (i < elemIndex) {
                            newNode = document.createElement(childName);
                            context.appendChild(newNode);
                            i++;
                        }
                        if ((nodeList != null) && (nodeList.getLength() >= elemIndex)) {
                            context = nodeList.item(elemIndex - 1);
                        }
                        else {
                            context = newNode;
                        }
                    }
                }
                else if (bSelectChildren) {
                    if (bInExpression) {
                        elemIndex = Integer.parseInt(tokenXPath.trim());
                    }
                    else {
                        childName = tokenXPath;
                        xPath2 += childName;
                    }
                    tokenXPath = "";
                }
            }

            if (context == null) {
                context = document;
            }

            int i = 0;
            nodeList = (CNodeList) findNode(context, "./" + elementName);
            if ((nodeList != null) && (nodeList.getLength() > 0)) {
                i = nodeList.getLength();
            }
            while (i < instance) {
                newNode = document.createElement(elementName);
                context.appendChild(newNode);
                i++;
            }
            if ((nodeList != null) && (nodeList.getLength() >= instance)) {
                context = nodeList.item(instance - 1);
            }
            else {
                context = newNode;
            }

            switch (childType) {
                case CHILD_TYPE_ELEMENT :
                    break;
                case CHILD_TYPE_TEXT :
                    Text textNode = document.createTextNode(elementValue);
                    context.appendChild(textNode);
                    break;
                case CHILD_TYPE_CDATA :
                    CDATASection cdataNode = document.createCDATASection(elementValue);
                    context.appendChild(cdataNode);
                    break;
            }

            return context;
        }
        catch (Exception ex) {
            throw new XPathUtilsException("Error while adding an element", ex);
        }
    }

    /**
     * @param document
     * @return the formatted document
     * @throws XPathUtilsException
     */
    public static String printDoc(Document document) throws XPathUtilsException
    {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            StringWriter out = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(out));
            return out.toString();
        }
        catch (TransformerConfigurationException ex) {
            throw new XPathUtilsException("Error while creating Transformer Object", ex);

        }
        catch (TransformerException ex) {
            throw new XPathUtilsException("Error while using Transformer Object", ex);
        }
    }

}
