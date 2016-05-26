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

/**
 * This class encapsulates a low level XPath. In order to enhance the
 * performances, the low level XPath is build only once. <br/>
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XPath
{
    /**
     * Low-level XPath. The actual type depends on the implementation.
     */
    private Object xpath       = null;

    /**
     * The string representation of the XPath
     */
    private String xpathString = null;

    /**
     * @param xpathStr
     *        a correct XPath
     * @throws TransformerException
     *         if an invalid XPath is given
     */
    public XPath(String xpathStr) throws TransformerException
    {
        xpathString = xpathStr;

        xpath = XPathAPIFactory.instance().newXPath(xpathStr);
    }

    /**
     * @return the low level XPath
     */
    public Object getXPath()
    {
        return xpath;
    }

    /**
     * @return the string representation of this XPath
     */
    public String getXPathString()
    {
        return xpathString;
    }

    /**
     * @return the string representation of this XPath
     */
    @Override
    public String toString()
    {
        return xpathString;
    }
}
