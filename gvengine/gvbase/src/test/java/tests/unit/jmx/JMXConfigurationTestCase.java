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
package tests.unit.jmx;

import it.greenvulcano.configuration.jmx.RegisterXMLConfig;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.MBeanServerInitializerFactory;

import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class JMXConfigurationTestCase extends TestCase
{
    /**
     *
     */
    public void testJMXConfiguration()
    {
        try {
        	MBeanServerInitializerFactory.registerSupplier("it.greenvulcano.configuration.jmx.RegisterXMLConfig", RegisterXMLConfig::new);
        	
            JMXEntryPoint jmx = JMXEntryPoint.instance();
            MBeanServer server = jmx.getServer();

            Set<ObjectName> set = server.queryNames(new ObjectName("GreenVulcano:*"), null);
            assertTrue("No JMX object returned in GreenVulcano domain", set != null && !set.isEmpty());
        }
        catch (Exception exc) {
            fail("Exception occurred while testing JMX configuration: " + exc.getMessage());
            exc.printStackTrace();
        }
    }
}
