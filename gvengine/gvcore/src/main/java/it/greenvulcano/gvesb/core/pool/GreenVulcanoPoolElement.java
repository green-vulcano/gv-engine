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
package it.greenvulcano.gvesb.core.pool;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVPublicException;
import it.greenvulcano.gvesb.core.GreenVulcano;
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;
import it.greenvulcano.gvesb.core.exc.GVCoreException;

import java.util.HashSet;
import java.util.Set;

/**
 * GreenVulcanoPoolElement class.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class GreenVulcanoPoolElement extends GreenVulcano implements ConfigurationListener
{
    private Set<String> execServices = new HashSet<String>();

    /**
     * @throws GVCoreException
     */
    public GreenVulcanoPoolElement() throws GVCoreException
    {
        super();
        XMLConfig.addConfigurationListener(this, GreenVulcanoConfig.getSystemsConfigFileName());
        XMLConfig.addConfigurationListener(this, GreenVulcanoConfig.getServicesConfigFileName());
    }

    /**
     * @see it.greenvulcano.gvesb.core.GreenVulcano#destroy()
     */
    @Override
    public void destroy(boolean force)
    {
        XMLConfig.removeConfigurationListener(this);
        super.destroy(force);
        execServices.clear();
    }

    /**
     * @param key
     * @return if service has been executed
     */
    public boolean isServiceExecuted(String key)
    {
        return execServices.contains(key);
    }

    /**
     * @see it.greenvulcano.gvesb.core.GreenVulcano#request(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer request(GVBuffer gvBuffer) throws GVPublicException
    {
        registerService(getKey(gvBuffer));
        return super.request(gvBuffer);
    }

    /**
     * @see it.greenvulcano.gvesb.core.GreenVulcano#requestReply(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer requestReply(GVBuffer gvBuffer) throws GVPublicException
    {
        registerService(getKey(gvBuffer));
        return super.requestReply(gvBuffer);
    }

    /**
     * @see it.greenvulcano.gvesb.core.GreenVulcano#getReply(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer getReply(GVBuffer gvBuffer) throws GVPublicException
    {
        registerService(getKey(gvBuffer));
        return super.getReply(gvBuffer);
    }

    /**
     * @see it.greenvulcano.gvesb.core.GreenVulcano#getRequest(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer getRequest(GVBuffer gvBuffer) throws GVPublicException
    {
        registerService(getKey(gvBuffer));
        return super.getRequest(gvBuffer);
    }

    /**
     * @see it.greenvulcano.gvesb.core.GreenVulcano#sendReply(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer sendReply(GVBuffer gvBuffer) throws GVPublicException
    {
        registerService(getKey(gvBuffer));
        return super.sendReply(gvBuffer);
    }

    /**
     * @see it.greenvulcano.gvesb.core.GreenVulcano#forward(it.greenvulcano.gvesb.buffer.GVBuffer,
     *      java.lang.String)
     */
    @Override
    public GVBuffer forward(GVBuffer gvBuffer, String name) throws GVPublicException
    {
        registerService(getKey(gvBuffer));
        return super.forward(gvBuffer, name);
    }

    /**
     * @see it.greenvulcano.gvesb.core.GreenVulcano#forward(it.greenvulcano.gvesb.buffer.GVBuffer,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public GVBuffer forward(GVBuffer gvBuffer, String name, String flowSystem, String flowService)
    throws GVPublicException
    {
        registerService(getKey(gvBuffer));
        return super.forward(gvBuffer, name, flowSystem, flowService);
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED)
                && (event.getFile().equals(GreenVulcanoConfig.getSystemsConfigFileName())
                 || event.getFile().equals(GreenVulcanoConfig.getServicesConfigFileName()))) {
            execServices.clear();
            setValid(false);
        }
    }

    /**
     * @param gvBuffer
     * @return the GVBuffer key
     */
    public static String getKey(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

    private void registerService(String key)
    {
        execServices.add(key);
    }
}
