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
package it.greenvulcano.gvesb.core.savepoint.jmx;

import it.greenvulcano.gvesb.core.savepoint.SavePointController;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.MBeanServerInitializer;
import it.greenvulcano.log.NMDC;

import javax.management.MBeanServer;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.1.0 Feb 17, 2011
 * @author GreenVulcano Developer Team
 * 
 */
public class ResetGVCoreSavePoint implements MBeanServerInitializer
{
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ResetGVCoreSavePoint.class);


    /**
     * Initialize the <code>MBeanServerInitializer</code>.
     * 
     * @param conf
     *        the node from which read configuration data
     * @exception Exception
     *            if errors occurs
     */
    @Override
    public void init(Node conf) throws Exception
    {
        // do nothing
    }

    /**
     * Initialize the given <code>MBeanServer</code>.
     * 
     * @param server
     *        the mbean server instance
     * @exception Exception
     *            if errors occurs
     */
    @Override
    public void initializeMBeanServer(MBeanServer server) throws Exception
    {
        NMDC.push();
        try {
            NMDC.setServer(JMXEntryPoint.getServerName());
            try {
                SavePointController.instance().resetSavePoints();
            }
            catch (Exception exc) {
                logger.error("Error resetting SavePoint state", exc);
            }
        }
        finally {
            NMDC.pop();
        }
    }
}