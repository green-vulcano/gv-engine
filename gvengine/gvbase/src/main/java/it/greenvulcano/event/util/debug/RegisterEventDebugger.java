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
package it.greenvulcano.event.util.debug;

import it.greenvulcano.event.EventHandler;
import it.greenvulcano.event.util.DefaultEventListener;
import it.greenvulcano.jmx.MBeanServerInitializer;

import javax.management.MBeanServer;

import org.w3c.dom.Node;

/**
 * RegisterEventDebugger class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 **/
public class RegisterEventDebugger implements MBeanServerInitializer
{
    /**
     * Listener instance.
     */
    static DebuggerEventListener listener = null;

    /**
     * Initialize the <code>MBeanServerInitializer</code>.
     *
     * @param conf
     *        configuration node
     * @throws Exception
     *         if error occurs
     */
    public final void init(Node conf) throws Exception
    {
        // do nothing
    }

    /**
     * Initialize the given <code>MBeanServer</code>.
     *
     * @param server
     *        The Mbeanserver instance
     * @throws Exception
     *         if error occurs
     */
    public final void initializeMBeanServer(MBeanServer server) throws Exception
    {
        if (listener == null) {
            listener = new DebuggerEventListener();
            EventHandler.addEventListener(listener, DefaultEventListener.class, null);
        }
    }
}
