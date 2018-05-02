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
package it.greenvulcano.gvesb.core.flow.parallel;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.flow.GVSubFlow;
import it.greenvulcano.log.NMDC;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * Object Pool  <code>GVSubFlow</code>.
 *
 * @version 3.3.4 Jun 19, 2013
 * @author GreenVulcano Developer Team
 *
 */
public class GVSubFlowPool
{
   private static final Logger      logger    = org.slf4j.LoggerFactory.getLogger(GVSubFlowPool.class);

    /**
     * Pool of GVSubFlow instances.
     */
    private LinkedList<GVSubFlow> pool        = new LinkedList<GVSubFlow>();
    /**
     * Set assigned pool's instances.
     */
    private Set<GVSubFlow>        assignedSF  = new HashSet<GVSubFlow>();

    private String                sfName      = null;
    private Node                  sfNode      = null;
        
    private int initialSize = 1;
    private int maximumSize = 10;
    private int maximumCreation = 20;
    private int created = 0;
    private int maxCreated = 0;
    
    /**
     * 
     */
    public GVSubFlowPool()
    {
        // do nothing
    }

    /**
     * @throws DataHandlerException
     */
    public void init(Node node, Node sfNode) throws GVCoreException
    {
        logger.debug("Initializing the GVSubFlow Pool.");
        
        this.sfNode = sfNode;
        try {
            this.sfName = XMLConfig.get(sfNode, "@name");
        }
        catch (XMLConfigException exc) {
            logger.warn("GVSubFlowPool initialization error", exc);
            throw new GVCoreConfException("GVSubFlowPool initialization error", exc);
        }

        if (initialSize < 0) {
            throw new GVCoreConfException("GVSubFlowPool initialSize < 0");
        }
        if ((maximumSize > 0) && (initialSize > maximumSize)) {
            throw new GVCoreConfException("GVSubFlowPool initialSize(" + initialSize + ") > maximumSize(" + maximumSize
                    + ")");
        }
        if ((maximumCreation > 0) && (maximumSize > maximumCreation)) {
            throw new GVCoreConfException("GVSubFlowPool maximumSize(" + maximumSize + ") > maximumCreation(" + maximumCreation
                    + ")");
        }

        NMDC.push();
        try {
            for (int i = 0; i < initialSize; ++i) {
                pool.add(createSubFlow());
            }
        }
        finally {
            NMDC.pop();
        }

        logger.debug("Initialized GVSubFlowPool instance: initialSize=" + initialSize + ", maximumSize="
                + maximumSize + ", maximumCreation=" + maximumCreation);
    }

    public String getSubFlowName() {
        return this.sfName;
    }

    /**
     * @return Returns the initialSize.
     */
    public int getInitialSize()
    {
        return initialSize;
    }

    /**
     * @return Returns the maximumCreation.
     */
    public int getMaximumCreation()
    {
        return maximumCreation;
    }

    /**
     * @return Returns the maximumSize.
     */
    public int getMaximumSize()
    {
        return maximumSize;
    }


    /**
     * @return Returns the maxCreated.
     */
    public int getMaxCreated()
    {
        return maxCreated;
    }

    /**
     * @return Returns the pooled instance count.
     */
    public int getPooledCount()
    {
        return pool.size();
    }

    /**
     * @return Returns the used instance count.
     */
    public int getInUseCount()
    {
        return assignedSF.size();
    }

    /**
     *
     */
    public void resetCounter()
    {
        // do nothing
    }

    /**
     *
     * @return a pooled or newly created <code>DHFactory</code>
     * @throws GVCoreException
     */
    public GVSubFlow getSubFlow() throws GVCoreException
    {
        if (pool == null) {
            return null;
        }

        synchronized (this) {
            if (pool.size() > 0) {
                logger.debug("GVSubFlowPool - found instance in pool");
                GVSubFlow subFlow = pool.removeFirst();
    
                logger.debug("GVSubFlowPool - extracting instance from pool(" + pool.size() + "/"
                        + created + "/" + maximumCreation + ")");
                assignedSF.add(subFlow);
                return subFlow;
            }

            if ((maximumCreation == -1) || (created < maximumCreation)) {
                GVSubFlow subFlow = createSubFlow();
                logger.debug("GVSubFlowPool - not found instance in pool");
                logger.debug("GVSubFlowPool - creating new instance(" + pool.size() + "/" + created
                        + "/" + maximumCreation + ")");
                assignedSF.add(subFlow);
                return subFlow;
            }
        }
        return null;
    }

    /**
     *
     * @param subFlow
     */
    public void releaseSubFlow(GVSubFlow subFlow)
    {
        if (subFlow == null) {
            return;
        }

        logger.debug("GVSubFlowPool - releasing instance(" + pool.size() + "/" + created + "/"
                + maximumCreation + ")");

        synchronized (this) {
            try {
                if (assignedSF.remove(subFlow)) {
                    if ((maximumSize == -1) || ((pool != null) && (pool.size() < maximumSize))) {
                        pool.addFirst(subFlow);
                        return;

                        /*long now = System.currentTimeMillis();
                        if ((shrinkDelayTime == -1) || (now < nextShrinkTime) || (pool.size() <= initialSize)) {
                            return;
                        }
                        logger.debug("GVSubFlowPool - shrink time elapsed");
                        subFlow = pool.removeLast();*/
                    }
                    destroySubFlow(subFlow);
                    logger.debug("GVSubFlowPool - destroying instance(" + pool.size() + "/" + created
                            + "/" + maximumCreation + ")");
                }
                else {
                    logger.debug("GVSubFlowPool - instance not created by this pool, destroing it");
                    subFlow.destroy();
                }
            }
            finally {
                notify();
            }
        }
    }

    /**
     *
     */
    public synchronized void destroy()
    {
        if (pool == null) {
            return;
        }
        logger.debug("GVSubFlowPool - Begin destroying instances");
        while (pool.size() > 0) {
            GVSubFlow subFlow = pool.removeFirst();
            destroySubFlow(subFlow);
        }
        logger.debug("GVSubFlowPool - End destroying instances");
        assignedSF.clear();
        pool = null;
    }

    /**
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        destroy();
    }


    private GVSubFlow createSubFlow() throws GVCoreException
    {
        GVSubFlow subFlow = new GVSubFlow();
        subFlow.init(sfNode, false);
        subFlow.setLoggerLevel(Level.ALL);
        ++created;
        if (created > maxCreated) {
            maxCreated = created;
        }
        return subFlow;
    }

    private void destroySubFlow(GVSubFlow subFlow)
    {
        subFlow.destroy();
        if (created > 0) {
            --created;
        }
    }
}
