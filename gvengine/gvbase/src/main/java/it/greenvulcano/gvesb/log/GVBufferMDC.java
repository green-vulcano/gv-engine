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

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.log.NMDC;

/**
 * This class contain the methods for insert and remove GVBuffer parameters for
 * Log4j Layout (%X{"ID|SYSTEM|SERVICE|RETCODE"}).
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVBufferMDC
{
    /**
     * This method insert GVBuffer context (parameters:
     * ID,SYSTEM,SERVICE,RETCODE) into MDC .
     *
     * @param gvBuffer
     *        the GVBuffer to put in MDC context
     */
    public static void put(GVBuffer gvBuffer)
    {
        NMDC.put(GVBuffer.Field.ID.toString(), gvBuffer.getId().toString());
        NMDC.put(GVBuffer.Field.SERVICE.toString(), gvBuffer.getService());
        if (NMDC.get("MASTER_SERVICE") == null) {
            NMDC.put("MASTER_SERVICE", gvBuffer.getService());
        }
        NMDC.put(GVBuffer.Field.SYSTEM.toString(), gvBuffer.getSystem());
        NMDC.put(GVBuffer.Field.RETCODE.toString(), "" + gvBuffer.getRetCode());
    }
    
    public static String changeMasterService(String newService)
    {
    	String service = (String) NMDC.get("MASTER_SERVICE");
    	if (newService != null) {
    		NMDC.put("MASTER_SERVICE", newService);
    	}
    	else {
    		NMDC.remove("MASTER_SERVICE");
    	}
        return service;
    }
}
