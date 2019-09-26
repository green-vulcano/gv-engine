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
package it.greenvulcano.gvesb.j2ee.db.formatter.base;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * Creates a BaseFormatter class.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public final class BaseFormatterFactory
{
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(BaseFormatterFactory.class);

    /**
     * Private constructor
     */
    private BaseFormatterFactory()
    {
        // do nothing
    }

    /**
     * Creates a new BaseFormatter subclass.
     *
     * @param confNode
     *        The configuration node
     * @return a new instance of the <tt>BaseFormatter</tt> class
     * @throws GVDBException
     *         if errors occur
     */
    public static BaseFormatter create(Node confNode) throws GVDBException
    {
        BaseFormatter baseFormatter = null;
        String className = null;

        logger.debug("create: Start");
        try {
            className = XMLConfig.get(confNode, "@class");
            logger.debug("create BaseFormatter: " + className);
            baseFormatter = (BaseFormatter) Class.forName(className).getConstructor().newInstance();
            logger.debug("init BaseFormatter...");
            baseFormatter.init(confNode);
        }
        catch (XMLConfigException exc) {
            logger.error("create - Error while accessing configuration informations via XMLConfig: ", exc);
            throw new GVDBException("EB_GENERIC_ERROR", new String[][]{{"msg",
                    "Error while accessing configuration informations via XMLConfig" + exc}}, exc);
        }
        catch (Throwable exc) {
            logger.error("create - Error while creating class" + className, exc);
            throw new GVDBException("EB_GENERIC_ERROR", new String[][]{{"msg", exc.toString()}}, exc);
        }
        logger.debug("create: End");

        return baseFormatter;
    }
}
