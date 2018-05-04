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
package it.greenvulcano.gvesb.core.config;

import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.statistics.StatisticsDataManager;
import it.greenvulcano.gvesb.virtual.VCLException;
import it.greenvulcano.gvesb.virtual.pool.OperationManagerPool;

/**
 * The InvocationContext GreenVulcano class.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public class InvocationContext extends it.greenvulcano.gvesb.internal.InvocationContext {
   
    /**
     * Used to get an GVServiceConf of a specific service (SYSTEM + SERVICE).
     */
    private ServiceConfigManager  gvServiceConfigManager = null;
    /**
     * The VCL OperationManagerPool to be used.
     */
    private OperationManagerPool  opMgr                  = null;
    /**
     * The Statistics StatisticsDataManager to be used.
     */
    private StatisticsDataManager statisticsDataManager  = null;

    /**
     * Default constructor.
     */
    public InvocationContext()
    {
        super();
        init();
    }

    /**
     * The initialize method.
     */
    private void init()
    {
        subSystem = "GVCore";
    }

    /**
     * @return Returns the gvServiceConfigManager.
     */
    public ServiceConfigManager getGVServiceConfigManager()
    {
        return gvServiceConfigManager;
    }

    /**
     * @param gvServiceConfigManager
     *        The gvServiceConfigManager to set.
     */
    public void setGVServiceConfigManager(ServiceConfigManager gvServiceConfigManager)
    {
        this.gvServiceConfigManager = gvServiceConfigManager;
    }

    /**
     * @return Returns the operationManager.
     * @throws GVException
     * @throws VCLException
     */
    public OperationManagerPool getOperationManager() throws GVCoreException
    {
        if (opMgr == null) {
            try {
                opMgr = OperationManagerPool.instance();
            }
            catch (Exception exc) {
                throw new GVCoreException("Error extracting OperationManager from OperationManagerPool", exc);
            }
        }
        return opMgr;
    }

    /**
     * @return Returns the statisticsDataManager.
     */
    public StatisticsDataManager getStatisticsDataManager()
    {
        return statisticsDataManager;
    }

    /**
     * @param statisticsDataManager
     *        The statisticsDataManager to set.
     */
    public void setStatisticsDataManager(StatisticsDataManager statisticsDataManager)
    {
        this.statisticsDataManager = statisticsDataManager;
    }

    /**
     * @see it.greenvulcano.gvesb.internal.InvocationContext#cleanup()
     */
    @Override
    public void cleanup()
    {
        super.cleanup();        
        opMgr = null;
    }

    /**
     * @see it.greenvulcano.gvesb.internal.InvocationContext#destroy()
     */
    @Override
    public void destroy()
    {
        super.destroy();
        gvServiceConfigManager = null;
        statisticsDataManager = null;
    }

}