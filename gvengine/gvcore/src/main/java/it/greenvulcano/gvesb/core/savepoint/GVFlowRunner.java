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
package it.greenvulcano.gvesb.core.savepoint;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.core.GreenVulcano;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.gvesb.log.GVBufferDump;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.thread.ThreadMap;

import java.util.Map;

import org.slf4j.Logger;

/**
 *
 * @version 3.1.0 15/feb/2011
 * @author GreenVulcano Developer Team
 */
class GVFlowRunner implements Runnable
{
    private static final Logger logger       = org.slf4j.LoggerFactory.getLogger(GVFlowRunner.class);

    private String              id           = null;
    private String              flowSystem   = null;
    private String              flowService  = null;
    private String              gvsOperation = null;
    private String              recoveryNode = null;
    private Map<String, Object> environment  = null;

    /**
     *
     */
    GVFlowRunner(String id, String flowSystem, String flowService, String gvsOperation, String recoveryNode,
            Map<String, Object> environment)
    {
        this.id = id;
        this.flowSystem = flowSystem;
        this.flowService = flowService;
        this.gvsOperation = gvsOperation;
        this.recoveryNode = recoveryNode;
        this.environment = environment;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
        NMDC.push();
        try {
            GVBuffer gvBuffer = new GVBuffer(flowSystem, flowService, new Id(id));

            GreenVulcano gv = GreenVulcanoPoolManager.instance().getGreenVulcanoPool("J2EEGreenVulcano")
            													.orElseGet(GreenVulcanoPoolManager::getDefaultGreenVulcanoPool)
            													.getGreenVulcano(gvBuffer);

            GVBufferDump dump = new GVBufferDump(gvBuffer, false);
            if (logger.isInfoEnabled()) {
                logger.info(GVFormatLog.formatBEGINOperation(gvBuffer)+" INPUT GVBuffer: \n"+dump);
            }
            long startTime = System.currentTimeMillis();
            gv.recover(id, flowSystem, flowService, gvsOperation, recoveryNode, environment);
            if (logger.isInfoEnabled()) {
                long endTime = System.currentTimeMillis();
                logger.info(GVFormatLog.formatENDOperation(gvBuffer, endTime - startTime)+" OUTPUT GVBuffer: "+dump);
            }
        }
        catch (Throwable exc) {
            logger.error("ERROR - Recovering Flow[" + id + "#" + flowSystem + "#" + flowService + "#"
                        + gvsOperation + "]", exc);
        }
        finally {
            NMDC.pop();
            ThreadMap.clean();
        }
    }

}
