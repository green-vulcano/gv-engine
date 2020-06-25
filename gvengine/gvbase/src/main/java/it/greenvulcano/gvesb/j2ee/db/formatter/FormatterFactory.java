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
package it.greenvulcano.gvesb.j2ee.db.formatter;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * Creates a ResponseFormatter class.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public final class FormatterFactory
{
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(FormatterFactory.class);

    /**
     * Define a private constructor
     */
    private FormatterFactory()
    {
        // do nothing
    }

    /**
     * Creates a new ResponseFormatter subclass.
     *
     * @param confNode
     *        the configuration node
     * @return a new instance of the <tt>ResponseFormatter</tt> class
     * @throws GVDBException
     *         if errors occur
     */
    public static ResponseFormatter create(Node confNode) throws GVDBException
    {
        ResponseFormatter responseFormatter = null;
        String className = null;

        logger.debug("create: Start");
        try {
            className = XMLConfig.get(confNode, "@class");
            logger.debug("create formatter: " + className);
            responseFormatter = (ResponseFormatter) Class.forName(XMLConfig.get(confNode, "@class")).getConstructor().newInstance();
            logger.debug("init formatter...");
            responseFormatter.init(confNode);
        }
        catch (XMLConfigException exc) {
            logger.error("create - Error while accessing configuration informations via XMLConfig: ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"msg",
                    "Error while accessing configuration informations via XMLConfig" + exc}}, exc);
        }
        catch (Throwable exc) {
            logger.error("create - Error while creating class" + className, exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"msg", exc.toString()}}, exc);
        }
        logger.debug("create: End");

        return responseFormatter;
    }
}
