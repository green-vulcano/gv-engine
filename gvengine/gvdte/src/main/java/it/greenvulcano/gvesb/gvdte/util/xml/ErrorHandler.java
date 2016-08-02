/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.gvdte.util.xml;

import org.slf4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * ErrorHandler for intercept XML parsing errors
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ErrorHandler implements org.xml.sax.ErrorHandler
{
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ErrorHandler.class);

    public ErrorHandler()
    {
        // do nothing
    }

    /**
     * Warning.
     *
     * @param ex
     * @throws SAXException
     */
    public void warning(SAXParseException exc) throws SAXException
    {
        logger.warn("XML Validation warning", exc);
    }

    /**
     * Error.
     *
     * @param ex
     * @throws SAXException
     */
    public void error(SAXParseException exc) throws SAXException
    {
        logger.error("XML Validation error", exc);
        throw exc;
    }

    /**
     * Fatal error.
     *
     * @param ex
     * @throws SAXException
     */
    public void fatalError(SAXParseException exc) throws SAXException
    {
        logger.error("XML Validation fatal error", exc);
        throw exc;
    }
}
