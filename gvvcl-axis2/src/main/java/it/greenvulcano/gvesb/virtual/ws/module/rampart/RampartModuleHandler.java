/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
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
 *******************************************************************************/
package it.greenvulcano.gvesb.virtual.ws.module.rampart;

import it.greenvulcano.gvesb.virtual.ws.module.ModuleHandler;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.slf4j.Logger;
/*
 *
 * RampartModuleHandler class
 *
 * @version     3.1.0 17 Feb 2011
 * @author     GreenVulcano Developer Team
*/
public class RampartModuleHandler extends ModuleHandler
{
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(RampartModuleHandler.class);
    
    public boolean preSendOperationSpecific(ServiceClient serviceClient, Options options)
    {
        logger.debug("Setting module data [" + getPolicyKey() + "]: " + getPolicy());
        options.setProperty(getPolicyKey(), getPolicy());
        return true;
    }

    public boolean postSendFaultOperationSpecific(ServiceClient serviceClient, Options options)
    {
        return true;
    }

    public boolean postSendOperationSpecific(ServiceClient serviceClient, Options options)
    {
        return true;
    }
}
