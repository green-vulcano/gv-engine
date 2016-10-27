/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.ws.axis2.context;

import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.TransportOutDescription;

/**
 * @version 3.2.0 06/mag/2012
 * @author GreenVulcano Developer Team
 */
public class Axis2ConfigurationContextHelper
{
    private static ConfigurationContext configurationContext = null;

    public static void setConfigurationContext(ConfigurationContext confCtx) {
        configurationContext = confCtx;
    }

    public static ConfigurationContext getConfigurationContext() throws AxisFault, PropertiesHandlerException {
        if (configurationContext == null) {
            configurationContext = createConfigurationContext();
        }
        return configurationContext;
    }

    public static TransportOutDescription getTransportOut(String name) {
        return configurationContext.getAxisConfiguration().getTransportOut(name);
    }

    private static ConfigurationContext createConfigurationContext() throws AxisFault, PropertiesHandlerException {
        return ConfigurationContextFactory.createConfigurationContextFromFileSystem(PropertiesHandler.expand("${{gv.app.home}}/webservices"), 
                PropertiesHandler.expand("${{gv.app.home}}/xmlconfig/axis2.xml"));
    }

}
