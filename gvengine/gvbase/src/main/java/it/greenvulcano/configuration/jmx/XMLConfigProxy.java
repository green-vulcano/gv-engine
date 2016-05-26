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
package it.greenvulcano.configuration.jmx;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.JMXUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * XMLConfigProxy class.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XMLConfigProxy
{
    /**
     *
     */
    public static final String JMX_KEY_NAME  = "Component";
    /**
     *
     */
    public static final String JMX_KEY_VALUE = "XMLConfig";
    /**
     *
     */
    public static final String JMX_KEY       = JMX_KEY_NAME + "=" + JMX_KEY_VALUE;
    /**
     *
     */
    public static final String JMX_FILTER    = "*:*," + JMX_KEY;

    /**
     * @return a String array containing all loaded files
     * @throws Exception
     */
    public String[] getLoadedFiles() throws Exception
    {
        JMXEntryPoint jmx = JMXEntryPoint.instance();
        MBeanServer server = jmx.getServer();

        Set<ObjectName> names = server.queryNames(new ObjectName(JMX_FILTER), null);

        Set<String> ret = new HashSet<String>();

        Iterator<ObjectName> i = names.iterator();
        while (i.hasNext()) {
            ObjectName name = i.next();
            Object val = server.getAttribute(name, "loadedFilesLocal");
            String files[] = (String[]) val;
            for (int j = 0; j < files.length; ++j) {
                ret.add(files[j]);
            }
        }

        String loadedFiles[] = new String[ret.size()];
        ret.toArray(loadedFiles);
        return loadedFiles;
    }

    /**
     * @param file
     * @throws Exception
     */
    public void discard(String file) throws Exception
    {
        Object params[] = new Object[]{file};
        String signature[] = new String[]{"java.lang.String"};
        JMXUtils.invoke(JMX_FILTER, "discardLocal", params, signature, null);
    }

    /**
     * @throws Exception
     */
    public void discardAll() throws Exception
    {
        Object params[] = new Object[0];
        String signature[] = new String[0];
        JMXUtils.invoke(JMX_FILTER, "discardAllLocal", params, signature, null);
    }

    /**
     * @param file
     * @throws Exception
     */
    public void load(String file) throws Exception
    {
        Object params[] = new Object[]{file};
        String signature[] = new String[]{"java.lang.String"};
        JMXUtils.invoke(JMX_FILTER, "loadLocal", params, signature, null);
    }

    /**
     * @param file
     * @throws Exception
     */
    public void reload(String file) throws Exception
    {
        Object params[] = new Object[]{file};
        String signature[] = new String[]{"java.lang.String"};
        JMXUtils.invoke(JMX_FILTER, "reloadLocal", params, signature, null);
    }

    /**
     * @throws Exception
     */
    public void reloadAll() throws Exception
    {
        Object params[] = new Object[0];
        String signature[] = new String[0];
        JMXUtils.invoke(JMX_FILTER, "reloadAllLocal", params, signature, null);
    }

    /**
     * @return a String array containing all loaded files by local instance
     */
    public String[] getLoadedFilesLocal()
    {
        return XMLConfig.getLoadedFiles();
    }

    /**
     * @param file
     */
    public void discardLocal(String file)
    {
        XMLConfig.discard(file);
    }

    /**
     *
     */
    public void discardAllLocal()
    {
        XMLConfig.discardAll();
    }

    /**
     * @param file
     * @throws XMLConfigException
     */
    public void loadLocal(String file) throws XMLConfigException
    {
        XMLConfig.load(file);
    }

    /**
     * @param file
     * @throws XMLConfigException
     */
    public void reloadLocal(String file) throws XMLConfigException
    {
        XMLConfig.reload(file);
    }

    /**
     * @throws XMLConfigException
     */
    public void reloadAllLocal() throws XMLConfigException
    {
        XMLConfig.reloadAll();
    }
}
