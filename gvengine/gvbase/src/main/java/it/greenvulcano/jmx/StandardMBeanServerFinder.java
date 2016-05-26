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
package it.greenvulcano.jmx;

import it.greenvulcano.configuration.XMLConfig;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.w3c.dom.Node;

/**
 * Find the MBeanServer
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class StandardMBeanServerFinder implements MBeanServerFinder
{
    private String mode;
    private String domain;
    private String agentId;

    /**
     * Initialize the <code>MBeanServerFinder</code>.
     *
     * @param conf
     * @throws Exception
     */
    public void init(Node conf) throws Exception
    {

        mode = XMLConfig.get(conf, "@mode", "create");
        domain = XMLConfig.get(conf, "@domain");
        agentId = XMLConfig.get(conf, "@agent-id");
    }

    /**
     * Find the <code>MBeanServer</code>. For the 'find' mode, the first server
     * will be returned.
     *
     * @return the <code>MBeanServer</code>
     * @throws Exception
     */
    public MBeanServer findMBeanServer() throws Exception
    {

        if (mode.equals("create")) {
            if (domain == null) {
                return MBeanServerFactory.createMBeanServer();
            }
            return MBeanServerFactory.createMBeanServer(domain);
        }
        else if (mode.equals("find")) {
            return MBeanServerFactory.findMBeanServer(agentId).get(0);
        }
        else if (mode.equals("new")) {
            if (domain == null) {
                return MBeanServerFactory.newMBeanServer();
            }
            return MBeanServerFactory.newMBeanServer(domain);
        }
        else {
            throw new Exception("Invalid mode: '" + mode + "'");
        }
    }

    /**
     * @see it.greenvulcano.jmx.MBeanServerFinder#getServerName()
     */
    public String getServerName()
    {
        return "StandardServerName";
    }

}
