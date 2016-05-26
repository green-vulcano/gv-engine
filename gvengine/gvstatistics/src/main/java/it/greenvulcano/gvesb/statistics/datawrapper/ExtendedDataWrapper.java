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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class resolves extra parameters to be stored for statistics purposes.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ExtendedDataWrapper
{
    private static final Logger             logger       = LoggerFactory.getLogger(ExtendedDataWrapper.class);

    /**
     * The data mappings.
     */
    private Map<String, DataMappingWrapper> dataMappings = null;

    /**
     * Default constructor.
     *
     * @param node
     *        the configuration node.
     *
     * @throws XMLConfigException
     *         if an error occurs.
     */
    public ExtendedDataWrapper(Node node) throws Exception
    {
        dataMappings = Collections.synchronizedMap(new LinkedHashMap<String, DataMappingWrapper>());
        NodeList mappings = XMLConfig.getNodeList(node, "*[@type='data-mapping']");
        for (int i = 0; i < mappings.getLength(); i++) {
            Node mapping = mappings.item(i);
            String storeField = XMLConfig.get(mapping, "@storeField");
            logger.debug("Initializing Wrapper for store field: " + storeField);
            DataMappingWrapper wrapper = (DataMappingWrapper) Class.forName(XMLConfig.get(mapping, "@class")).newInstance();
            wrapper.init(mapping);
            dataMappings.put(storeField, wrapper);
        }
    }

    /**
     * Resolves the data.
     *
     * @param gvBuffer
     *        the GVBuffer.
     *
     * @return a list containing the name and the value of the objects resolved.
     * @throws Exception
     *         if an error occurs.
     */
    public Map<String, String> resolveData(GVBuffer gvBuffer)
    {
        LinkedHashMap<String, String> dataResolved = new LinkedHashMap<String, String>();
        for (Entry<String, DataMappingWrapper> entry: dataMappings.entrySet()) {
            dataResolved.put(entry.getKey(), entry.getValue().resolveData(gvBuffer));
        }
        return dataResolved;
    }

}
