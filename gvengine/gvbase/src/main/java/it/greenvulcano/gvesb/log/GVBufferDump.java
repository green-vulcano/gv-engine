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
package it.greenvulcano.gvesb.log;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.util.bin.Dump;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class GVBufferDump implements ConfigurationListener
{
    /**
     * Configuration file name.
     */
    private static final String             CFG_FILE_NAME   = "GVBufferDumpConfig.xml";
    /**
     * Dump size configuration.
     */
    private static HashMap<String, Integer> serviceCfg      = null;
    /**
     * Configuration reload flag.
     */
    private static boolean                  confChangedFlag = true;
    /**
     * Singleton reference.
     */
    private static GVBufferDump             instance        = null;
    /**
     * Default dump size.
     */
    private static int                      defaultSize     = 0;

    /**
     * GVBuffer to dump.
     */
    private GVBuffer                        gvBuffer        = null;
    /**
     * Dump size.
     */
    private int                             maxBufferLength = -1;
    /**
     * If true dumps only the body.
     */
    private boolean                         onlyData        = false;
    /**
     * If true force the dump.
     */
    private boolean                         forceDump       = false;

    static {
        init();
    }

    /**
     * Constructor.
     * 
     */
    private GVBufferDump()
    {
        // do nothing
    }

    /**
     * Constructor.
     * 
     * @param gvBuffer
     *        the GVBuffer instance to dump
     */
    public GVBufferDump(GVBuffer gvBuffer)
    {
        this.gvBuffer = gvBuffer;
    }

    /**
     * Constructor.
     * 
     * @param gvBuffer
     *        the GVBuffer instance to dump
     * @param forceDump
     *        if true force the dump
     */
    public GVBufferDump(GVBuffer gvBuffer, boolean forceDump)
    {
        this.gvBuffer = gvBuffer;
        this.forceDump = forceDump;
    }

    /**
     * Constructor.
     * 
     * @param gvBuffer
     *        the GVBuffer instance to dump
     * @param forceDump
     *        if true force the dump
     * @param onlyData
     *        if true dump only the body
     */
    public GVBufferDump(GVBuffer gvBuffer, boolean forceDump, boolean onlyData)
    {
        this.gvBuffer = gvBuffer;
        this.forceDump = forceDump;
        this.onlyData = onlyData;
    }

    /**
     * Constructor.
     * 
     * @param gvBuffer
     *        the GVBuffer instance to dump
     * @param maxBufferLength
     *        the max dump size
     * @param forceDump
     *        if true force the dump
     */
    public GVBufferDump(GVBuffer gvBuffer, int maxBufferLength, boolean forceDump)
    {
        this.gvBuffer = gvBuffer;
        this.maxBufferLength = maxBufferLength;
        this.forceDump = forceDump;
    }

    /**
     * Constructor.
     * 
     * @param gvBuffer
     *        the GVBuffer instance to dump
     * @param maxBufferLength
     *        the max dump size
     * @param forceDump
     *        if true force the dump
     * @param onlyData
     *        if true dump only the body
     */
    public GVBufferDump(GVBuffer gvBuffer, int maxBufferLength, boolean forceDump, boolean onlyData)
    {
        this.gvBuffer = gvBuffer;
        this.maxBufferLength = maxBufferLength;
        this.forceDump = forceDump;
        this.onlyData = onlyData;
    }

    /**
     * Perform the dump.
     * 
     * @return the dumped GVBuffer.
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString()
    {
        if (gvBuffer == null) {
            return "null GVBuffer";
        }

        StringBuilder buf = new StringBuilder("\n");
        if (!onlyData) {
            buf.append("Standard Field:\n");
            buf.append("\tsystem   = ").append(gvBuffer.getSystem()).append("\n");
            buf.append("\tservice  = ").append(gvBuffer.getService()).append("\n");
            buf.append("\tid       = ").append(gvBuffer.getId()).append("\n");
            buf.append("\tretCode  = ").append(gvBuffer.getRetCode()).append("\n");
        }

        int dumpSize = (forceDump ? -1 : maxDumpSize());
        if (dumpSize == 0) {
            buf.append("GVBuffer: dump disabled\n");
        }
        else {
            Object buffer = gvBuffer.getObject();
            buf.append("GVBuffer: ");
            if (buffer == null) {
                buf.append("NULL").append('\n');
            }
            else {
                buf.append(buffer.getClass()).append('\n');
                if (buffer instanceof byte[]) {
                    Dump dump = new Dump(((byte[]) gvBuffer.getObject()), dumpSize);
                    buf.append(dump.toString());
                }
                else if (buffer instanceof Node) {
                    try {
                        StringBuilder val = new StringBuilder(XMLUtils.serializeDOM_S((Node) buffer));
                        int len = Math.min(((dumpSize == -1) ? Integer.MAX_VALUE : dumpSize), val.length());
                        buf.append(val.substring(0, len));
                    }
                    catch (Exception exc) {
                        buf.append("[\nDUMP ERROR!!!!!\n].");
                    }
                }
                else {
                    try {
                        StringBuilder val = new StringBuilder("" + buffer);
                        int len = Math.min(((dumpSize == -1) ? Integer.MAX_VALUE : dumpSize), val.length());
                        buf.append(val.substring(0, len));
                    }
                    catch (Exception exc) {
                        buf.append("[\nDUMP ERROR!!!!!\n].");
                    }
                }
            }
        }

        Iterator<String> iter = gvBuffer.getPropertyNamesIterator();
        if (iter.hasNext()) {
            buf.append("\nProperties:\n");
            while (iter.hasNext()) {
                String currFieldName = iter.next();
                String currFieldValue = gvBuffer.getProperty(currFieldName);
                buf.append("\t").append(currFieldName).append(" = ").append(currFieldValue);
                if (iter.hasNext()) {
                    buf.append("\n");
                }
            }
        }
        return buf.toString();
    }

    /**
     * Handle configuration events.
     * 
     * @param event
     *        the configuration event
     */
    @Override
    public final void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED)
                && (event.getFile().equals(GVBufferDump.CFG_FILE_NAME))) {
            confChangedFlag = true;
        }
    }

    /**
     * Set the dump size for a given service.
     * 
     * @param service
     *        the service name or 'DEFAULT_SIZE' for setting the default dump
     *        size
     * @param system
     *        the system name or "" for all systems
     * @param size
     *        the dump size
     */
    public static synchronized void setDumpSize(String service, String system, int size)
    {
        if (service.equals("DEFAULT_SIZE")) {
            defaultSize = size;
        }
        else {
            if (system.equals("")) {
                system = "ALL";
            }
            System.out.println("GVBufferDump: " + service + "::" + system + " = " + size);
            serviceCfg.put(service + "::" + system, Integer.valueOf(size));
        }
    }

    /**
     * Get the registered dump size for the called services.
     * 
     * @return the dump size list
     */
    public static synchronized String[] getDumpSizeList()
    {
        if (serviceCfg == null) {
            return null;
        }
        Set<String> keys = serviceCfg.keySet();
        Iterator<String> i = keys.iterator();
        String[] list = new String[keys.size()];
        int c = 0;
        while (i.hasNext()) {
            String key = i.next();
            list[c] = key + " -> " + serviceCfg.get(key);
            c++;
        }
        return list;
    }

    /**
     * Calculate the max dump size.
     * 
     * @return the max dump size
     */
    private int maxDumpSize()
    {
        int max = 0;
        try {
            max = getDumpSize(gvBuffer);

            if (max < 0) {
                max = maxBufferLength;
            }
            else if (maxBufferLength > 0) {
                if (max > maxBufferLength) {
                    max = maxBufferLength;
                }
            }
        }
        catch (Exception exc) {
            max = 0;
        }

        return max;
    }

    /**
     * Return the dump size for the given GVBuffer instance.
     * 
     * @param data
     *        the GVBuffer instance to dump
     * @return the dump size
     */
    private int getDumpSize(GVBuffer data)
    {
        if (data == null) {
            return 0;
        }

        loadConfiguration();

        Integer iSize = serviceCfg.get(data.getService() + "::" + data.getSystem());
        if (iSize == null) {
            iSize = serviceCfg.get(data.getService() + "::ALL");
            if (iSize == null) {
                setDumpSize(data.getService(), data.getSystem(), defaultSize);
                return defaultSize;
            }
            setDumpSize(data.getService(), data.getSystem(), iSize.intValue());
        }

        return iSize.intValue();
    }

    /**
     * Initialize the instance.
     * 
     */
    private static void init()
    {
        if (instance == null) {
            instance = new GVBufferDump();
            XMLConfig.addConfigurationListener(instance, CFG_FILE_NAME);
            serviceCfg = new HashMap<String, Integer>();
            loadConfiguration();
        }
    }

    /**
     * Load the configuration.
     * 
     */
    private static void loadConfiguration()
    {
        if (!confChangedFlag) {
            return;
        }

        synchronized (instance) {
            if (!confChangedFlag) {
                return;
            }

            serviceCfg.clear();
            try {
                try {
                    String xPath = "/GVBufferDump/@log-dump-size";
                    defaultSize = XMLConfig.getInteger(CFG_FILE_NAME, xPath, 0);
                }
                catch (Exception exc) {
                    defaultSize = 0;
                }
                loadServicesConfig();
            }
            catch (Exception exc) {
                serviceCfg.clear();
                defaultSize = 0;
            }
            confChangedFlag = false;
        }
    }

    /**
     * @throws XMLConfigException
     *         if error occurs
     */
    private static void loadServicesConfig() throws XMLConfigException
    {
        NodeList svcList = null;
        svcList = XMLConfig.getNodeList(CFG_FILE_NAME, "/GVBufferDump/ServiceDump");
        if ((svcList != null) && (svcList.getLength() > 0)) {
            for (int i = 0; i < svcList.getLength(); i++) {
                Node svcNode = svcList.item(i);
                String service = XMLConfig.get(svcNode, "@id-service");
                int size = XMLConfig.getInteger(svcNode, "@log-dump-size", defaultSize);
                setDumpSize(service, "", size);
                loadClientsConfig(svcNode, service);
            }
        }
    }

    /**
     * @param svcNode
     *        the service node
     * @param service
     *        the service name
     * @throws XMLConfigException
     *         if error occurs
     */
    private static void loadClientsConfig(Node svcNode, String service) throws XMLConfigException
    {
        int size;
        NodeList cliList = XMLConfig.getNodeList(svcNode, "ClientDump");
        if ((cliList != null) && (cliList.getLength() > 0)) {
            for (int j = 0; j < cliList.getLength(); j++) {
                Node cliNode = cliList.item(j);
                String system = XMLConfig.get(cliNode, "@id-system");
                size = XMLConfig.getInteger(cliNode, "@log-dump-size", defaultSize);
                setDumpSize(service, system, size);
            }
        }
    }
}
