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

import org.w3c.dom.Node;

/**
 * Interface for a XPath extension function. Implementing class must have a default constructor.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface XPathFunction
{
    /**
     * Calculate the function return value.
     *
     * @param contextNode
     *        context node for XPath evaluation.
     * @param params
     *        function's arguments. The array's elements can be of the following types:
     *        <ul>
     *        <li> Double
     *        <li> String
     *        <li> Boolean
     *        <li> NodeList
     *        <li> DocumentFragment
     *        <li> List
     *        <ul>
     * @return possible return types:
     *         <ul>
     *         <li>Node
     *         <li>String
     *         <li>Number
     *         <li>Boolean
     *         <li>null
     *         <li>Node[]
     *         <li>NodeList
     *         <li>NodeSet
     *         </ul>
     * @throws TransformerException
     */
    public Object evaluate(Node contextNode, Object[] params) throws TransformerException;
}
