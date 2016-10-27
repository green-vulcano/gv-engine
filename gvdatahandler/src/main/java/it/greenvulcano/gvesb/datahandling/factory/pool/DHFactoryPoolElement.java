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
package it.greenvulcano.gvesb.datahandling.factory.pool;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.datahandling.DataHandlerException;
import it.greenvulcano.gvesb.datahandling.IDBOBuilder;
import it.greenvulcano.gvesb.datahandling.factory.DHFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * DHFactoryPoolElement class.
 *
 * @version 3.1.0 Feb 17, 2011
 * @author GreenVulcano Developer Team
 *
 */
public class DHFactoryPoolElement extends DHFactory implements ConfigurationListener
{
    private Set<String> execServices = new HashSet<String>();

    /**
     * @throws GVCoreException
     */
    public DHFactoryPoolElement() throws DataHandlerException
    {
        super();
        initialize(null);
        XMLConfig.addConfigurationListener(this, DHFactory.DH_CONFIG_FILENAME);
    }


    /**
     * @see it.greenvulcano.gvesb.datahandling.factory.DHFactory#destroy()
     */
    @Override
    public void destroy()
    {
        XMLConfig.removeConfigurationListener(this);
        super.destroy();
    }

    /**
     * @param key
     * @return if service has been executed
     */
    public boolean isServiceExecuted(String name)
    {
        return execServices.contains(name);
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.factory.DHFactory#getDBOBuilder(String)
     */
    @Override
    public IDBOBuilder getDBOBuilder(String name) throws DataHandlerException, InterruptedException
    {
        if (!isInitialized()) {
            initialize(null);
        }
        registerService(name);
        return super.getDBOBuilder(name);
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    public void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && event.getFile().equals(DHFactory.DH_CONFIG_FILENAME)) {
            execServices.clear();
            destroy();
        }
    }

    private void registerService(String name)
    {
        execServices.add(name);
    }
}
