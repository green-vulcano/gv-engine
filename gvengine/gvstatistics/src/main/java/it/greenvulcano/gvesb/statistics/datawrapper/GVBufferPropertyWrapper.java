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
package it.greenvulcano.gvesb.statistics.datawrapper;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * DataMappingWrapper extracting the data from GVBuffer property.
 *
 * @version 3.0.0 13/giu/2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVBufferPropertyWrapper implements DataMappingWrapper
{
    private static final Logger logger = LoggerFactory.getLogger(GVBufferPropertyWrapper.class);

    /**
     * The name of GVBuffer property.
     */
    private String              property;


    /**
     *
     */
    public GVBufferPropertyWrapper()
    {
        // do nothing
    }

    /**
     *
     * @param node
     *        the configuration node.
     *
     * @throws XMLConfigException
     *         if an error occurs.
     */
    public void init(Node node) throws XMLConfigException
    {
        property = XMLConfig.get(node, "@property");
        logger.debug("GVBuffer Property: " + property);
    }

    /**
     * Resolves the parameter. If the extended property having the given
     * property name does not exist returns null.
     */
    @Override
    public String resolveData(GVBuffer gvBuffer)
    {
        String value = gvBuffer.getProperty(property);
        logger.debug("GVBuffer Property[" + property + "] resolved: " + value);
        return value;
    }
}
