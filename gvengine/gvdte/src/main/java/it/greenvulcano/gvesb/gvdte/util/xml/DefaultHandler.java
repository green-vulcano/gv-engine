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
package it.greenvulcano.gvesb.gvdte.util.xml;

import org.slf4j.Logger;
import org.xml.sax.SAXParseException;

/**
 * SAX handler that trap errors.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author Fabio Eusebi
 *
 *
 */
public class DefaultHandler extends org.xml.sax.helpers.DefaultHandler
{
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(DefaultHandler.class);

    /**
     * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
     */
    @Override
    public void error(SAXParseException exc) throws SAXParseException
    {
        logger.debug("Error: line " + exc.getLineNumber());
        logger.debug(exc.getMessage());
        throw exc;
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException)
     */
    @Override
    public void warning(SAXParseException exc) throws SAXParseException
    {
        logger.debug("Warning: line " + exc.getLineNumber());
        logger.debug(exc.getMessage());
        throw exc;
    }
}
