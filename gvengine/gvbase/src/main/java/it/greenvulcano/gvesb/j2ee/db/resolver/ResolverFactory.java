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
package it.greenvulcano.gvesb.j2ee.db.resolver;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * Creates ResolveFormatter objects.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public final class ResolverFactory
{
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ResolverFactory.class);

    /**
     * Private constructor.
     */
    private ResolverFactory()
    {
        // do nothing
    }

    /**
     * Creates a new ParamResolver subclass.
     *
     * @param confNode
     *        the configuration node
     * @return a new instance of the <tt>ParamResolver</tt> class
     * @throws GVDBException
     *         if errors occur
     */
    public static ParamResolver create(Node confNode) throws GVDBException
    {
        ParamResolver paramResolver = null;
        String className = null;

        logger.debug("create: Start");

        try {
            className = XMLConfig.get(confNode, "@class");
            logger.debug("create param resolver: " + className);
            paramResolver = (ParamResolver) Class.forName(className).newInstance();
            logger.debug("init param resolver...");
            paramResolver.init(confNode);
        }
        catch (XMLConfigException ex) {
            logger.error("create - Error while accessing configuration informations via XMLConfig: ", ex);
            throw new GVDBException("EB_GENERIC_ERROR", new String[][]{{"msg",
                    "Error while accessing configuration informations via XMLConfig" + ex}}, ex);
        }
        catch (Throwable exc) {
            logger.error("create - Error while creating class", exc);
            throw new GVDBException("EB_GENERIC_ERROR", new String[][]{{"msg", exc.toString()}}, exc);
        }
        logger.debug("create: End");

        return paramResolver;
    }
}
