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
package it.greenvulcano.gvesb.virtual;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ServiceAlias
{
    private static final org.slf4j.Logger logger          = org.slf4j.LoggerFactory.getLogger(ServiceAlias.class);

    /**
     * Contains the mapping for Service->Alias
     */
    private Map<String, String> serviceAliasMap = new HashMap<String, String>();

    /**
     * The real service name
     */
    private String              serviceName     = "";

    /**
     *
     */
    public ServiceAlias()
    {
        // do nothing
    }

    /**
     * Initialize the object
     *
     * @param node
     *        Node from which read configuration data
     * @throws InitializationException
     *         If configuration error occurs
     */
    public void init(Node node) throws InitializationException
    {
        logger.debug("BEGIN - Build Alias Map");
        serviceAliasMap.clear();

        NodeList aliasNodeList = null;
        try {
            aliasNodeList = XMLConfig.getNodeList(node, "ServiceAlias/AliasMapping");
        }
        catch (XMLConfigException exc) {
            return;
        }
        if ((aliasNodeList != null) && (aliasNodeList.getLength() != 0)) {
            for (int i = 0; i < aliasNodeList.getLength(); i++) {
                Node aliasNode = aliasNodeList.item(i);
                String realName = "";
                String alias = "";
                try {
                    realName = XMLConfig.get(aliasNode, "@real-name");
                }
                catch (XMLConfigException exc) {
                    throw new InitializationException("GVVCL_MISSED_CFG_PARAM_ERROR", new String[][]{
                            {"name", "'real-name'"}, {"node", XPathFinder.buildXPath(node)}}, exc);
                }
                try {
                    alias = XMLConfig.get(aliasNode, "@alias");
                }
                catch (XMLConfigException exc) {
                    throw new InitializationException("GVVCL_MISSED_CFG_PARAM_ERROR", new String[][]{
                            {"name", "'alias'"}, {"node", XPathFinder.buildXPath(node)}}, exc);
                }
                logger.debug("Caching Alias Mapping: " + realName + " to " + alias);
                serviceAliasMap.put(realName, alias);
            }
        }
        logger.debug("END - Build Alias Map");
    }

    /**
     * Set the GVBuffer service field to serviceName
     *
     * @param gvBuffer
     *        The input GVBuffer
     */
    public void manageAliasInput(GVBuffer gvBuffer)
    {
        serviceName = gvBuffer.getService();
        try {
            String aliasName = serviceAliasMap.get(serviceName);
            if (aliasName != null) {
                gvBuffer.setService(aliasName);
            }
        }
        catch (Exception exc) {
            // do nothing
        }
    }

    /**
     * Set the GVBuffer service field to aliasName
     *
     * @param gvBuffer
     *        The input GVBuffer
     */
    public void manageAliasOutput(GVBuffer gvBuffer)
    {
        try {
            gvBuffer.setService(serviceName);
        }
        catch (Exception exc) {
            // do nothing
        }
        finally {
            serviceName = "";
        }
    }

    /**
     * Returns the configured alias of the given service
     *
     * @param service
     *        the service to use as key
     * @return the configured alias
     */
    public String getAlias(String service)
    {
        String alias = serviceAliasMap.get(service);
        return (alias == null) ? service : alias;
    }
}
