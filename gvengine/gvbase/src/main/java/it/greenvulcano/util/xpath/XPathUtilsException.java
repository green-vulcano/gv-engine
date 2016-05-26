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

/**
 * Exception raised by XPath helper classes.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XPathUtilsException extends Exception
{
    private static final long serialVersionUID = 5342025340197794100L;
    protected Throwable       nestedException;

    /**
     * Builds a XPathUtilsException with a simple message
     *
     * @param sErrorMessage
     */
    public XPathUtilsException(String sErrorMessage)
    {
        super(sErrorMessage);
        nestedException = null;
    }

    /**
     * Builds a XPathUtilsException with an error description, an error code,
     * and a nested exception
     *
     * @param sErrorMessage
     * @param ne
     *
     */
    public XPathUtilsException(String sErrorMessage, Throwable ne)
    {
        super(sErrorMessage);
        nestedException = ne;
    }

    /**
     * Return first nested Exception for this XPathUtilsException
     *
     * @return the nested exception
     */
    public Throwable getNestedException()
    {
        return nestedException;
    }

    /**
     * Return a String representation for this XPathUtilsException
     */
    @Override
    public String toString()
    {

        // print app error code and error message
        String result = super.toString();
        // Chain nested exceptions messages
        if (nestedException != null) {
            result += " -> " + nestedException.toString();
        }
        return result;
    }
}
