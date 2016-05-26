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
package it.greenvulcano.gvesb.core.flow;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.statistics.StatisticsDataManager;

import java.util.Map;

import org.apache.log4j.Level;
import org.w3c.dom.Node;

/**
 * GVFlow.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public interface GVFlow
{
    /**
     * don't perform any check on output
     */
    public static final String OUT_CHECK_NONE       = "none";
    /**
     * perform system/service check on output
     */
    public static final String OUT_CHECK_SYS_SVC    = "sys-svc";
    /**
     * perform system/service/id check on output
     */
    public static final String OUT_CHECK_SYS_SVC_ID = "sys-svc-id";

    /**
     * Initialize the instance
     * 
     * @param gvopNode
     *        the node from which read configuration data
     * @throws GVCoreConfException
     *         if errors occurs
     */
    public void init(Node gvopNode) throws GVCoreConfException;


    /**
     * Execute the flow
     * 
     * @param gvBuffer
     *        the input data
     * @return the output data
     * @throws GVCoreException
     *         if errors occurs
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws GVCoreException, InterruptedException;

    /**
     * Execute the flow
     * 
     * @param gvBuffer
     *        the input data
     * @param onDebug
     * @return the output data
     * @throws GVCoreException
     *         if errors occurs
     */
    public GVBuffer perform(GVBuffer gvBuffer, boolean onDebug) throws GVCoreException, InterruptedException;


    /**
     * Execute the flow
     * 
     * @param gvBuffer
     *        the input data
     * @return the output data
     * @throws GVCoreException
     *         if errors occurs
     */
    public GVBuffer recover(String recoveryNode, Map<String, Object> environment) throws GVCoreException, InterruptedException;


    /**
     * @return the statistics data manager
     */
    public StatisticsDataManager getStatisticsDataManager();

    /**
     * @param manager
     *        the statistics data manager
     */
    public void setStatisticsDataManager(StatisticsDataManager manager);


    /**
     * @return the statistics activation flag value
     */
    public boolean isStatisticsEnabled();


    /**
     * @param b
     *        set the statistics activation flag
     */
    public void setStatisticsEnabled(boolean b);

    /**
     * @return the actual logger level
     */
    public Level getLoggerLevel();

    /**
     * 
     * @param loggerLevel
     *        the logger level to set
     */
    public void setLoggerLevel(Level loggerLevel);

    /**
     * @return the flow activation flag value
     */
    public boolean getActivation();


    /**
     * Execute destroy operations
     */
    public void destroy();

}