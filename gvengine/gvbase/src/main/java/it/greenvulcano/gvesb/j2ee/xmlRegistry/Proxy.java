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
package it.greenvulcano.gvesb.j2ee.xmlRegistry;

import it.greenvulcano.configuration.XMLConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 *
 * Proxy class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class Proxy
{
    /**
     * The logger utility.
     */
	private static final Logger logger = LoggerFactory.getLogger(Proxy.class);
    /**
     * XPath per raggiungere il Registry desiderato in configurazione
     */
    private Node                xpath  = null;
    /**
     * Proxy host
     */
    private String              host   = null;
    /**
     * Proxy port
     */
    private String              port   = null;

    /**
     * Constructor to initialize the fields of this object
     *
     * @param xpath
     *        The XPath node value
     */
    public Proxy(Node xpath)
    {
        logger.debug("BEGIN Proxy");
        this.xpath = xpath;
        loadConfiguration();
        logger.debug("END Proxy");
    }

    /**
     * This method reads the configuration and initializes the cache of objects.
     * it is invoked from the constructor and after the cleaning of the cache.
     */
    private void loadConfiguration()
    {
        logger.debug("BEGIN loadConfiguration");
        host = XMLConfig.get(xpath, "@host", "");
        port = XMLConfig.get(xpath, "@port", "");
        logger.debug("END loadConfiguration");
    }

    /**
     * @return the host to connect to
     */
    public String getHost()
    {
        return host;
    }

    /**
     * @return the host port to connect to
     */
    public String getPort()
    {
        return port;
    }
}
