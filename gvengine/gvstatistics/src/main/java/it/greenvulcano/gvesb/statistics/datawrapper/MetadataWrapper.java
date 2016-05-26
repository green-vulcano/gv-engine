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
import it.greenvulcano.util.metadata.PropertiesHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * DataMappingWrapper extracting the metadata expression execution.
 *
 * @version 3.0.0 13/giu/2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class MetadataWrapper implements DataMappingWrapper
{
    private static final Logger logger = LoggerFactory.getLogger(MetadataWrapper.class);

    /**
     * Metadata expression encoding.
     */
    private String              expression;

    /**
     *
     */
    public MetadataWrapper()
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
    @Override
    public void init(Node node) throws XMLConfigException
    {
        expression = XMLConfig.get(node, "@expression");
        logger.debug("Expression: " + expression);
    }

    /**
     * Resolves the parameter. If no match has been found returns null.
     */
    @Override
    public String resolveData(GVBuffer gvBuffer)
    {
        String value = null;
        try {
            value = PropertiesHandler.expand(expression, null, gvBuffer);
        }
        catch (Exception exc) {
            logger.warn("Error executing expression", exc);
        }
        logger.debug("Expression resolved: " + value);
        return value;
    }
}
