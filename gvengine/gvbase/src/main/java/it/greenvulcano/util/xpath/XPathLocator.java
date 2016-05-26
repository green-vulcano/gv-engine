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

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class allows to locate a Node within a DOM that matches EXACTLY with a
 * given XPath.
 *
 * It implements a backtracking algorithm so that it doesn't always have to
 * traverse the DOM from the root node to find the matching node.
 *
 * NOTE: Currently, only COMPLETELY specified XPaths are supported (single
 * entities selected)
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XPathLocator
{

    private final static char                      XPATH_LEVEL_SEPARATOR      = '/';
    private final static char                      XPATH_ATTRIBUTE_SEPARATOR  = '@';
    private final static char                      XPATH_LEVEL_INSTANCE_BEGIN = '[';
    private final static char                      XPATH_LEVEL_INSTANCE_END   = ']';
    private final static byte                      ELEMENT_XPATH              = 1;
    private final static byte                      ATTRIBUTE_XPATH            = 2;

    /**
     * An HashMap of HashMaps: The HashMap mapped to the key 'i' (i > 0)
     * contains mappings between XPath strings with 'i' levels and the
     * corresponding deepest Node within the DOM
     */
    private HashMap<String, HashMap<String, Node>> multilevelCache;

    /**
     * Public constructor
     *
     * @param doc
     *        The document to search within
     */
    public XPathLocator(Document doc)
    {
        multilevelCache = new HashMap<String, HashMap<String, Node>>(10);

        // Create the cache entry for the root node
        // in a cache of level 1
        Element root = doc.getDocumentElement();
        String rootXPath = XPATH_LEVEL_SEPARATOR + root.getTagName() + "[1]";

        // there should be only 1 root node !
        HashMap<String, Node> rootCache = new HashMap<String, Node>(1);
        rootCache.put(rootXPath, root);

        multilevelCache.put("" + 1, rootCache);
    }

    /**
     * Returns the single Element node matching the given XPath
     *
     * @param xpath
     *        Of the Element
     * @return Element found
     * @throws XPathUtilsException
     *         when errors occur during search
     */
    public Element getElement(String xpath) throws XPathUtilsException
    {
        Node theNode = null;
        XPathObject xpObj = new XPathObject(xpath);

        if (xpObj.getXpathNodeType() != ELEMENT_XPATH) {
            throw new XPathUtilsException("Given xpath can't match to an Element node");
        }
        theNode = searchXPath(xpObj);
        return (Element) theNode;
    }

    /**
     * Returns the value of the attribute matching the given XPath
     *
     * @param xpath
     *        XPath of the attribute
     * @return The attribute value
     * @throws XPathUtilsException
     *         on errors while searching
     */
    public String getAttributeValue(String xpath) throws XPathUtilsException
    {
        XPathObject xpObj = new XPathObject(xpath);
        String attrName = xpObj.getAttributePath().substring(1);

        if (xpObj.getXpathNodeType() != ATTRIBUTE_XPATH) {
            throw new XPathUtilsException("Given XPath can't match to an Attr node");
        }

        String deeperNodeXpath = xpObj.getNodeXPath();
        XPathObject xpNodeObj = new XPathObject(deeperNodeXpath);
        Node theNode = searchXPath(xpNodeObj);

        if ((theNode != null) && (theNode.getNodeType() == Node.ELEMENT_NODE)) {
            Element elem = (Element) theNode;
            String attrValue = elem.getAttribute(attrName);
            return attrValue;
        }
        return null;
    }

    /**
     * Recursive method implementing the backtracking search algorithm
     *
     * @param xpObj
     *        The xPath to search for
     * @return Node The node found given xpObj
     * @throws XPathUtilsException
     *         when errors occur
     */
    private Node searchXPath(XPathObject xpObj) throws XPathUtilsException
    {
        int currLevel = xpObj.getLevelsNum();
        try {
            // Search given XPath in its cache level
            String xpath = xpObj.getNodeXPath();
            String nodeName = xpObj.getLevelNodeName(currLevel);
            int nodeInstance = xpObj.getLevelNodeInstance(currLevel);
            HashMap<String, Node> currCacheLevel = multilevelCache.get("" + currLevel);

            if (currCacheLevel == null) {
                // Current cache level doesn't exist: create it
                currCacheLevel = new HashMap<String, Node>();
                multilevelCache.put("" + currLevel, currCacheLevel);
            }

            Node theNode = currCacheLevel.get(xpath);
            if (theNode == null) {
                // Node not found in the current cache level:
                // lookup for its father in the upper level
                if (currLevel > 1) {
                    String fatherXPath = xpObj.getXPath(currLevel - 1);
                    XPathObject fatherXpObj = new XPathObject(fatherXPath);
                    Node nodeFather = searchXPath(fatherXpObj);
                    // Find the node corresponding to the given XPath
                    // starting from its father node
                    NodeList children = nodeFather.getChildNodes();

                    int instancesFound = 0;
                    for (int i = 0; i < children.getLength(); i++) {
                        Node currChild = children.item(i);
                        if (currChild.getNodeType() == Node.ELEMENT_NODE) {
                            if (currChild.getNodeName().equals(nodeName)) {
                                instancesFound++;
                                if (instancesFound == nodeInstance) {
                                    // Found the right instance
                                    theNode = currChild;
                                    break;
                                }
                            }
                        }
                    }
                    // Cache the node
                    currCacheLevel.put(xpath, theNode);
                }
            }
            // Return the node
            return theNode;
        }
        catch (XPathUtilsException ex) {
            throw new XPathUtilsException("Error while looking up node at level " + currLevel, ex);
        }
        catch (Throwable ex) {
            throw new XPathUtilsException("Generic error while looking up node at level " + currLevel, ex);
        }
    }

    /**
     * This class encapsulates an XPath string.
     *
     * MC: Per ora sono gestiti solo Xpath COMPLETAMENTE specificati: istanze di
     * Nodi corrispondenti a elementi o attributi
     */
    private class XPathObject
    {
        /**
         * The XPath string (from the root node to the deepest Element node):
         * e.g if the XPath string is "/TESTINPUT[1]/AGE[1]/@Value" nodeXpath
         * contains "/TESTINPUT[1]/AGE[1]"
         */
        private String nodeXpath;

        /**
         * If the XPath matches to an attribute value, this string contains the
         * substring of XPath containing the attribute name: e.g if the XPath
         * string is "/TESTINPUT[1]/AGE[1]/@Value" attributePath contains
         * "@Value"
         */
        private String attributePath;

        /**
         * The number of node levels within an XPath.
         */
        private int    nodeLevelNum;

        /**
         * Small integer indicating if this XPath maps to an Element node or to
         * an Attribute node
         */
        private byte   xpathNodeType;

        /**
         * Public constructor
         */
        private XPathObject(String theXpath)
        {

            // Detect node type (Element, Attribute)
            if (theXpath.indexOf(XPATH_ATTRIBUTE_SEPARATOR) != -1) {
                // The XPath specifies an attribute
                xpathNodeType = ATTRIBUTE_XPATH;
                nodeXpath = theXpath.substring(0, theXpath.lastIndexOf(XPATH_LEVEL_SEPARATOR));
                attributePath = theXpath.substring(theXpath.lastIndexOf(XPATH_ATTRIBUTE_SEPARATOR));
            }
            else {
                // The XPath specifies an element
                xpathNodeType = ELEMENT_XPATH;
                nodeXpath = theXpath;
            }

            // Detect node depth into XPath
            for (int i = 0; i < nodeXpath.length(); i++) {
                if (nodeXpath.charAt(i) == XPATH_LEVEL_SEPARATOR) {
                    nodeLevelNum++;
                }
            }
        }

        /**
         * Returns the full XPath
         *
         * @return the full XPath
         */
        @SuppressWarnings("unused")
        public String getFullXPath()
        {
            if (xpathNodeType == ATTRIBUTE_XPATH) {
                return nodeXpath + XPATH_LEVEL_SEPARATOR + attributePath;
            }
            return nodeXpath;
        }

        /**
         * Returns the XPath of the deepest node within the given XPath
         *
         * @return the XPath of the deepest node
         */
        public String getNodeXPath()
        {
            return nodeXpath;
        }

        /**
         * Returns the attributePath value
         *
         * @return the attributePath
         */
        public String getAttributePath()
        {
            return attributePath;
        }

        /**
         * Returns the XPath of the node at the specified level within the given
         * XPath
         *
         * @param level
         *        Of the node
         * @return the XPath of the node
         * @throws XPathUtilsException
         *         on errors
         */
        public String getXPath(int level) throws XPathUtilsException
        {
            if ((level < 1) || (level > nodeLevelNum)) {
                throw new XPathUtilsException("No such level within given xpath");
            }
            if (level == nodeLevelNum) {
                return nodeXpath;
            }
            int currLevel = 0;
            int idx1 = 0;
            for (int i = 0; i < nodeXpath.length(); i++) {
                if (nodeXpath.charAt(i) == XPATH_LEVEL_SEPARATOR) {
                    currLevel++;
                    if (currLevel == level) {
                        idx1 = i;
                        break;
                    }
                }
            }
            int idx2 = nodeXpath.indexOf(XPATH_LEVEL_SEPARATOR, idx1 + 1);
            return nodeXpath.substring(0, idx2);
        }

        /**
         * Returns the name of the node at the specified level within the given
         * XPath
         *
         * @param level
         *        Of the node
         * @return the name of the node
         * @throws XPathUtilsException
         *         on errors
         */
        public String getLevelNodeName(int level) throws XPathUtilsException
        {
            String levelXpath = getXPath(level);
            int levelIdx = levelXpath.lastIndexOf(XPATH_LEVEL_SEPARATOR);
            int instIdx = levelXpath.indexOf(XPATH_LEVEL_INSTANCE_BEGIN, levelIdx);
            if (instIdx != -1) {
                return levelXpath.substring(levelIdx + 1, instIdx);
            }
            return levelXpath.substring(levelIdx + 1);
        }

        /**
         * Returns the instance number of the node at the specified level within
         * the given XPath
         *
         * @param level
         *        Of the node
         * @return the instance number of the node
         * @throws XPathUtilsException
         *         on errors
         */
        public int getLevelNodeInstance(int level) throws XPathUtilsException
        {
            String levelXpath = getXPath(level);
            int levelIdx = levelXpath.lastIndexOf(XPATH_LEVEL_SEPARATOR);
            int instIdx = levelXpath.indexOf(XPATH_LEVEL_INSTANCE_BEGIN, levelIdx);
            if (instIdx != -1) {
                int instIdxEnd = levelXpath.indexOf(XPATH_LEVEL_INSTANCE_END, instIdx);
                String instance = levelXpath.substring(instIdx + 1, instIdxEnd);
                return Integer.parseInt(instance);
            }
            return 1;
        }

        /**
         * Returns the number of levels within this XPath
         *
         * @return the number of levels within this XPath
         */
        public int getLevelsNum()
        {
            return nodeLevelNum;
        }

        /**
         * Returns the number of levels within this XPath
         *
         * @return the number of levels within this XPath
         */
        public byte getXpathNodeType()
        {
            return xpathNodeType;
        }

    }
}
