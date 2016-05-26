/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 */
package it.greenvulcano.util.heartbeat;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;

import org.w3c.dom.Node;

/**
 * @version 3.2.0 09/11/2011
 * @author GreenVulcano Developer Team
 */
public class HeartBeatManager
{
    private static HeartBeat   instance  = null;
    private static boolean     instError = false;

    public static final String CFG_FILE  = "GVHeartBeatConfig.xml";

    /**
     * 
     */
    private HeartBeatManager()
    {
        // do nothing
    }

    public static void beat(String subsystem) throws HeartBeatException
    {
        HeartBeat hb = getHandler();
        hb.beat(subsystem);
    }

    public static long lastBeat(String subsystems, long fromTime) throws HeartBeatException
    {
        HeartBeat hb = getHandler();
        return hb.lastBeat(subsystems, fromTime);
    }

    public static int prepareBeat(String subsystem) throws HeartBeatException
    {
        HeartBeat hb = getHandler();
        return hb.prepareBeat(subsystem);
    }

    public static void confirmBeat(int id) throws HeartBeatException
    {
        confirmBeat(id, true);
    }

    public static void confirmBeat(int id, boolean success) throws HeartBeatException
    {
        HeartBeat hb = getHandler();
        hb.confirmBeat(id, success);
    }

    public static void cancelBeat(int id) throws HeartBeatException
    {
        HeartBeat hb = getHandler();
        hb.cancelBeat(id);
    }

    private static synchronized HeartBeat getHandler() throws HeartBeatException
    {
        if (instance == null) {
            String clazz = "";
            try {
                Node node = XMLConfig.getNode(CFG_FILE, "/GVHeartBeatConfig/*[@type='heartbeat']");
                if (node != null) {
                    clazz = XMLConfig.get(node, "@class", "it.greenvulcano.util.heartbeat.impl.SimpleHeartBeat");
                }
                instance = (HeartBeat) Class.forName(clazz).newInstance();
                instance.init(node);
                XMLConfig.addConfigurationListener(instance, CFG_FILE);
                ShutdownEventLauncher.addEventListener(instance);
                instError = false;
            }
            catch (Exception exc) {
                if (!instError) {
                    System.out.println("Error instantiating HeartBeat of class [" + clazz + "]: " + exc);
                    exc.printStackTrace();
                }
                instError = true;
                instance = null;
                throw new HeartBeatException("Error instantiating HeartBeat of class [" + clazz + "]", exc);
            }
        }
        return instance;
    }
}
