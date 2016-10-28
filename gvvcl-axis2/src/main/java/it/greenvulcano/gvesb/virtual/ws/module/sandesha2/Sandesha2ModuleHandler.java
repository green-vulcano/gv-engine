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
package it.greenvulcano.gvesb.virtual.ws.module.sandesha2;

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.slf4j.Logger;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.client.SandeshaClient;
import org.apache.sandesha2.client.SandeshaClientConstants;

import it.greenvulcano.gvesb.virtual.ws.module.ModuleHandler;
/*
 *
 * Sandesha2ModuleHandler class
 *
 * @version     3.1.0 17 Feb 2011
 * @author     GreenVulcano Developer Team
*/
@SuppressWarnings("deprecation")
public class Sandesha2ModuleHandler extends ModuleHandler
{
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Sandesha2ModuleHandler.class);

    public boolean postSendFaultOperationSpecific(ServiceClient serviceClient, Options options)
    {
        return true;
    }

    public boolean postSendOperationSpecific(ServiceClient serviceClient, Options options)
    {
        return true;
    }

    public boolean preSendOperationSpecific(ServiceClient serviceClient, Options options)
    {
        options.setUseSeparateListener(true);
        try {
            SandeshaClient.createSequence(serviceClient, false, null);
        }
        catch (SandeshaException exc) {
            logger.warn("Cannot create a new sequence.", exc);
        }
        options.setProperty(SandeshaClientConstants.OFFERED_SEQUENCE_ID, UUIDGenerator.getUUID());
        options.setProperty(SandeshaClientConstants.LAST_MESSAGE, "true");
        return true;
    }

}
