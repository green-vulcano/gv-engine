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
package it.greenvulcano.gvesb.log.jmx;

import it.greenvulcano.gvesb.log.GVBufferDump;
import it.greenvulcano.jmx.JMXUtils;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVBufferDumpProxy
{
    public static final String JMX_KEY_NAME  = "Component";
    public static final String JMX_KEY_VALUE = "GVBufferDumpProxy";
    public static final String JMX_KEY       = JMX_KEY_NAME + "=" + JMX_KEY_VALUE;
    public static final String JMX_FILTER    = "*:*,Internal=Yes," + JMX_KEY;

    /**
     * Set the dump size for a given service, on the cluster.
     *
     * @param service
     *        the service name or 'DEFAULT_SIZE' for setting the default dump
     *        size
     * @param system
     *        the system name or "" for all systems
     * @param size
     *        the dump size
     * @throws Exception
     *         if error occurs
     */
    public final void setDumpSize(String service, String system, int size) throws Exception
    {
        Object[] params = new Object[]{service, system, size};
        String[] signature = new String[]{"java.lang.String", "java.lang.String", "int"};
        JMXUtils.invoke(JMX_FILTER, "setDumpSize_Internal", params, signature, null);
    }

    /**
     * Get the registered dump size for the called services.
     *
     * @return the dump size list
     * @throws Exception
     */
    public static synchronized String[] getDumpSizeList() throws Exception
    {
        return GVBufferDump.getDumpSizeList();
    }

    /**
     * Set the dump size for a given service, on the local instance.
     *
     * @param service
     *        the service name or 'DEFAULT_SIZE' for setting the default dump
     *        size
     * @param system
     *        the system name or "" for all systems
     * @param size
     *        the dump size
     */
    public final void setDumpSize_Internal(String service, String system, int size)
    {
        GVBufferDump.setDumpSize(service, system, size);
    }

    /**
     * Get the registered dump size for the called services.
     *
     * @return the dump size list
     */
    public static synchronized String[] getDumpSizeList_Internal()
    {
        return GVBufferDump.getDumpSizeList();
    }
}
