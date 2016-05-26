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

import org.w3c.dom.Node;

/**
 * This class allows to build the XPath string representation of a given node
 * within a DOM
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XPathFinder
{

    /**
     * This methods returns an XPath of the given Node.
     *
     * @param node
     *        Node to find XPath of which
     * @return XPath
     */
    public static String buildXPath(Node node)
    {
        if(node == null) {
            throw new IllegalArgumentException("node is null");
        }
        // Local variables
        //
        String absoluteXPath = ""; // It is the XPath string to return to the
        // client
        String currentNodeName = ""; // The name of the current node (used to
        // make the XPath string).
        int currentIndex = 0; // Index of the current node to use to build the
        // XPath string .../Node[3]/...
        Node father = null; // The father of the current node.

        // If the given node is the root node of the document the Xpath is just
        // "/".
        //
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            // The XPath for the root node is by specifications equal to "/".
            //
            absoluteXPath = "/";
        }
        else {
            // Continue 'till there are parents to visit.
            //
            while ((father = node.getParentNode()) != null) {
                // Get the name and the index of the current node.
                //
                currentNodeName = node.getNodeName();
                currentIndex = getCurrentIndex(node);

                // Continue to build the absolute XPath by composing strings:
                // the current level is "/Name[index]".
                //
                absoluteXPath = "/" + currentNodeName + "[" + currentIndex + "]" + absoluteXPath;

                // The current node has been processed and it is time to move
                // up.
                //
                node = father;
            }
        }
        // Return the absolute XPath.
        //
        return absoluteXPath;
    }

    /**
     * Get the index of the given node respect to the other brothers. In other
     * words it can be said: If I have other brothers, which number am I in the
     * family? First son, second, ...? This function consider only brothers with
     * the same name (Ex.: <a> and <b> are not considered brothers).<br/>
     * <br/>
     *
     * @param node
     *        The node from which the absolute XPath has to be retrieved
     * @return The index of the given node respect to the other brothers
     */
    public static int getCurrentIndex(Node node)
    {
        // Local variables.
        //
        int index = 1; // The index to return: It is one if I am the only one or
        // if I am the patriarch.
        Node brother = null; // One of my bigger brothers (or cousins).
        String nodeName = null; // My name: Used to recognize brothers from
        // cousins.

        // Save my name to check if my bigger brothers have my same name (Ex.:
        // "<a>").
        //
        nodeName = node.getNodeName();

        // I am looking for myself (my index) in the family, visiting all bigger
        // brothers.
        //
        while ((brother = node.getPreviousSibling()) != null) {
            // I have to consider only ELEMENTS (no text, comments, ...)
            //
            if (brother.getNodeType() == Node.ELEMENT_NODE) {
                // If I have other bigger brothers (same name) it means I am at
                // position index++: first, second, third
                // brother, ...
                //
                if (brother.getNodeName().equals(nodeName)) {
                    // I found other bigger brothers, this means that I am
                    // younger than I thought.
                    //
                    index++;
                }
            }

            // The current node has been processed and it is time to move to my
            // bigger brothers.
            //
            node = brother;
        }

        // Return the index of the given node.
        //
        return index;
    }
}
