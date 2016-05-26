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
package it.greenvulcano.util.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Extension of ErrorHandler class for XML parsing errors
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ErrHandler implements ErrorHandler
{

    /**
     * Default constructor.
     */
    public ErrHandler()
    {
        // do nothing
    }

    /**
     * Warning.
     *
     * @param ex
     * @throws SAXException
     */
    public void warning(SAXParseException ex) throws SAXException
    {
        printError("Warning", ex);
    }

    /**
     * Error.
     *
     * @param ex
     * @throws SAXException
     */
    public void error(SAXParseException ex) throws SAXException
    {
        printError("Error", ex);
        throw ex;
    }

    /**
     * Fatal error.
     *
     * @param ex
     * @throws SAXException
     */
    public void fatalError(SAXParseException ex) throws SAXException
    {
        printError("Fatal Error", ex);
        throw ex;
    }

    /**
     * Prints the error message.
     *
     * @param type
     * @param ex
     */
    protected void printError(String type, SAXParseException ex)
    {
        StringBuffer sb = new StringBuffer("[" + type + "] ");
        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1) {
                systemId = systemId.substring(index + 1);
            }
            sb.append(systemId);
        }
        sb.append(':').append(ex.getLineNumber()).append(':').append(ex.getColumnNumber()).append(": ").append(ex.getMessage()).append("\n");
        System.err.println(sb);
        System.err.flush();

    }
}
