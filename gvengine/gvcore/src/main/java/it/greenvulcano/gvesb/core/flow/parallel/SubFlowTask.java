/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.flow.parallel;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.flow.GVSubFlow;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.thread.ThreadMap;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import org.slf4j.Logger;

/**
 * 
 * @version 3.4.0 Jun 17, 2013
 * @author GreenVulcano Developer Team
 * 
 */
public class SubFlowTask implements Callable<Result>
{
    private static final Logger logger            = org.slf4j.LoggerFactory.getLogger(SubFlowTask.class);

    private GVSubFlowPool       pool             = null;
    private GVSubFlow           subFlow          = null;
    private GVBuffer            input;
    private boolean             onDebug;
    private boolean             changeLogContext;
    private Map<String, String> logContext;
    private String              inputRefDP;
    private boolean             spawned           = false;
    private String              spawnedName       = null;

    public SubFlowTask(GVSubFlowPool pool, GVBuffer input, boolean onDebug, boolean changeLogContext, Map<String, String> logContext, String inputRefDP) {
        this.pool = pool;
        this.input = input;
        this.onDebug = onDebug;
        this.logContext = logContext;
        this.changeLogContext = changeLogContext;
        this.inputRefDP = inputRefDP;
    }

    public SubFlowTask(GVSubFlow subFlow, GVBuffer input, boolean onDebug, boolean changeLogContext, Map<String, String> logContext, String inputRefDP) {
        this.subFlow = subFlow;
        this.input = input;
        this.onDebug = onDebug;
        this.logContext = logContext;
        this.changeLogContext = changeLogContext;
        this.inputRefDP = inputRefDP;
    }

    public void setSpawned(boolean spawned) {
        this.spawned = spawned;
    }

    public void setSpawnedName(String spawnedName) {
        this.spawnedName = spawnedName;
    }

    @Override
    public Result call() throws Exception {
        try {
            NMDC.push();
            if (spawned && (spawnedName != null)) {
                Thread th = Thread.currentThread();
                String thn = th.getName();
                thn = thn.substring(thn.lastIndexOf("_"));
                th.setName(spawnedName + thn);
            }
            NMDC.setCurrentContext(logContext);

            Result result = null;
            GVSubFlow currSubFlow = null;
            try {
                GVBuffer internalData = input;

                if (pool != null) {
                    currSubFlow = pool.getSubFlow();
                }
                else {
                    currSubFlow = subFlow;
                }

                if (changeLogContext) {
                    NMDC.setOperation(currSubFlow.getFlowName());
                    GVBufferMDC.put(internalData);
                }
                
                DataProviderManager dataProviderManager = DataProviderManager.instance();
                if ((inputRefDP != null) && (inputRefDP.length() > 0)) {
                    IDataProvider dataProvider = dataProviderManager.getDataProvider(inputRefDP);
                    try {
                        logger.debug("Working on Input data provider: " + dataProvider);
                        internalData = new GVBuffer(input);
                        dataProvider.setObject(internalData);
                        Object inputCall = dataProvider.getResult();
                        internalData.setObject(inputCall);
                    }
                    finally {
                        dataProviderManager.releaseDataProvider(inputRefDP, dataProvider);
                    }
                }

                GVBuffer output = currSubFlow.perform(internalData, onDebug);
                result = new Result(Result.State.STATE_OK, input, output);
            }
            catch (InterruptedException exc) {
                if (spawned) {
                    logger.error("SubFlow execution interrupted", exc);
                }
                result = new Result(Result.State.STATE_INTERRUPTED, input, exc);
                Thread.currentThread().interrupt();
            }
            catch (Exception exc) {
                if (spawned) {
                    logger.error("SubFlow execution failed", exc);
                }
                result = new Result(Result.State.STATE_ERROR, input, exc);
            }
            finally {
                if (pool != null) {
                    pool.releaseSubFlow(currSubFlow);
                }
                if (subFlow != null) {
                    subFlow.destroy();
                }
            }
            return result;
        }
        finally {
            NMDC.pop();
            ThreadMap.clean();
            
            this.pool = null;
            this.subFlow = null;
            //this.input = null;
            this.logContext = null;
        }
    }

    public Result getFailureResult(Throwable cause) {
        return new Result(Result.State.STATE_ERROR, input, cause);
    }

    public Result getTimeoutResult(InterruptedException cause) {
        return new Result(Result.State.STATE_TIMEOUT, input, cause);
    }

    public Result getCancelledResult(CancellationException cause) {
        return new Result(Result.State.STATE_CANCELLED, input, cause);
    }
}
