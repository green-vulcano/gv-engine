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
package it.greenvulcano.gvesb.datahandling.factory.pool;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.datahandling.DataHandlerException;
import it.greenvulcano.gvesb.datahandling.factory.DHFactory;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.Stats;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Object Pool  <code>DHFactory</code>.
 *
 * @version 3.1.0 Feb 17, 2011
 * @author GreenVulcano Developer Team
 *
 */
public class DHFactoryPool implements ShutdownEventListener
{

    public static final long         DEFAULT_SHRINK_DELAY_TIME = 1000 * 60 * 15;

    public static final long         DEFAULT_TIMEOUT           = 1000 * 20;

    public static final int          DEFAULT_INITIAL_SIZE      = 1;

    public static final int          DEFAULT_MAXIMUM_SIZE      = 10;

    public static final int          DEFAULT_MAXIMUM_CREATION  = 50;

   private static final Logger      logger                    = org.slf4j.LoggerFactory.getLogger(DHFactoryPool.class);

    private static DHFactoryPool instance = null;
    /**
     * Pool of GreenVulcano istances.
     */
    private LinkedList<DHFactory> pool                      = new LinkedList<DHFactory>();
    /**
     * Set istance pool assigned.
     */
    private Set<DHFactory>        assignedDH                = new HashSet<DHFactory>();
    /**
     *  Initial size  pool.
     */
    private int                      initialSize               = DEFAULT_INITIAL_SIZE;
    /**
     *  Max size  pool, -1 = unlimited.
     */
    private int                      maximumSize               = DEFAULT_MAXIMUM_SIZE;
    /**
     * Max istannce created, se -1 = unlimited.
     */
    private int                      maximumCreation           = DEFAULT_MAXIMUM_CREATION;
    /**
     * Timeout.
     */
    private long                     defaultTimeout            = DEFAULT_TIMEOUT;
    /**
     * Shrink delay time.
     */
    private long                     shrinkDelayTime           = DEFAULT_SHRINK_DELAY_TIME;
    /**
     * Next shrink time
     */
    private long                     nextShrinkTime            = System.currentTimeMillis() + shrinkDelayTime;
    /**
     * Number of created istances.
     */
    private int                      created                   = 0;
    /**
     * If true shutdown in progress.
     */
    private boolean                  shutdownFlag              = false;

    private long                     poolMiss                  = 0;
    private long                     serviceMiss               = 0;
    private int                      maxCreated                = 0;
    private Stats                    statPoolMiss              = new Stats(1000, 1000, 30);
    private Stats                    statServiceMiss           = new Stats(1000, 1000, 30);

    /**
     * @param config
     * @throws DataHandlerException
     */
    private DHFactoryPool() throws DataHandlerException
    {
        init();
    }

    public static synchronized DHFactoryPool instance() throws DataHandlerException
    {
        if (instance == null) {
            instance = new DHFactoryPool();
        }
        return instance;
    }

    /**
     * @throws DataHandlerException
     */
    private void init() throws DataHandlerException
    {
        logger.debug("Initializing the Data Handler Factory Pool.");
        try {
            Document dhfCfg = XMLConfig.getDocument(DHFactory.DH_CONFIG_FILENAME);
            if (dhfCfg != null) {
                Node node = XMLConfig.getNode(dhfCfg, "//GVDataHandlerConfiguration/DHFactoryPool");
                if (node != null) {
                    initialSize = XMLConfig.getInteger(node, "@initial-size", DEFAULT_INITIAL_SIZE);
                    maximumSize = XMLConfig.getInteger(node, "@maximum-size", DEFAULT_MAXIMUM_SIZE);
                    maximumCreation = XMLConfig.getInteger(node, "@maximum-creation", DEFAULT_MAXIMUM_CREATION);
                    setDefaultTimeout(XMLConfig.getLong(node, "@default-timeout", DEFAULT_TIMEOUT));
                    setShrinkDelayTime(XMLConfig.getLong(node, "@shrink-timeout", DEFAULT_SHRINK_DELAY_TIME));
                    nextShrinkTime = System.currentTimeMillis() + shrinkDelayTime;
                }
            }
        }
        catch (XMLConfigException exc) {
            logger.warn("DHFactoryPool initialization error", exc);
            throw new DataHandlerException("DHFactoryPool initialization error", exc);
        }

        if (initialSize < 0) {
            throw new DataHandlerException("DHFactoryPool initialSize < 0");
        }
        if ((maximumSize > 0) && (initialSize > maximumSize)) {
            throw new DataHandlerException("DHFactoryPool initialSize(" + initialSize + ") > maximumSize(" + maximumSize
                    + ")");
        }
        if ((maximumCreation > 0) && (maximumSize > maximumCreation)) {
            throw new DataHandlerException("DHFactoryPool maximumSize(" + maximumSize + ") > maximumCreation(" + maximumCreation
                    + ")");
        }

        NMDC.push();
        try {
            for (int i = 0; i < initialSize; ++i) {
                pool.add(createDHFactory());
            }
        }
        finally {
            NMDC.pop();
        }

        logger.debug("Initialized DHFactoryPool instance: initialSize=" + initialSize + ", maximumSize="
                + maximumSize + ", maximumCreation=" + maximumCreation + ", defaultTimeout="
                + defaultTimeout + ", shrinkDelayTime=" + shrinkDelayTime);
        ShutdownEventLauncher.addEventListener(this);
    }

    /**
     * @return Returns the shrinkDelayTime.
     */
    public long getShrinkDelayTime()
    {
        return shrinkDelayTime;
    }

    /**
     * @param shrinkDelayTime
     *        The shrinkDelayTime to set.
     */
    public void setShrinkDelayTime(long shrinkDelayTime) throws DataHandlerException
    {
        if ((shrinkDelayTime != -1) && (shrinkDelayTime < 1)) {
            throw new DataHandlerException("DHFactoryPool shrinkDelayTime can be -1 or > 0");
        }
        this.shrinkDelayTime = shrinkDelayTime;
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
     * @return Returns the defaultTimeout.
     */
    public long getDefaultTimeout()
    {
        return defaultTimeout;
    }

    /**
     * @param defaultTimeout
     *        The defaultTimeout to set.
     */
    public void setDefaultTimeout(long defaultTimeout) throws DataHandlerException
    {
        if (defaultTimeout < 1) {
            throw new DataHandlerException("DHFactoryPool defaultTimeout < 1");
        }
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * @return Returns the maxCreated.
     */
    public int getMaxCreated()
    {
        return maxCreated;
    }

    /**
     * @return Returns the poolMiss.
     */
    public long getPoolMiss()
    {
        return poolMiss;
    }

    /**
     *
     * @return PoolMiss ratio value
     */
    public float getPoolMissRatio()
    {
        return statPoolMiss.getThroughput();
    }

    /**
     * @return Returns the serviceMiss.
     */
    public long getServiceMiss()
    {
        return serviceMiss;
    }

    /**
     *
     * @return ServiceMiss ratio value
     */
    public float getServiceMissRatio()
    {
        return statServiceMiss.getThroughput();
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
        return assignedDH.size();
    }

    /**
     *
     */
    public void resetCounter()
    {
        poolMiss = 0;
        statPoolMiss.reset();
        serviceMiss = 0;
        statServiceMiss.reset();
    }

    /**
     * @param name
     * @return the DHFactory object or <code>null</code> if no object is
     *         available after the default timeout is elapsed.
     *
     * @throws DataHandlerException
     *
     * @see #getDHFactory(String, long)
     */
    public DHFactory getDHFactory(String name) throws DataHandlerException, InterruptedException
    {
        return getDHFactory(name, defaultTimeout);
    }

    /**
     *
     * @param name
     * @param timeout
     *
     * @return <code>null</code> if not available a
     *         <code>DHFactory</code> prior to timeout
     * @throws DataHandlerException
     */
    public DHFactory getDHFactory(String name, long timeout) throws DataHandlerException,
            InterruptedException {
        if (shutdownFlag) {
            throw new DataHandlerException("ShutdownEvent received, pool disabled");
        }
        long endTime = System.currentTimeMillis() + timeout;
        String key = name;
        while (true) {
            synchronized (this) {
                if (pool == null) {
                    return null;
                }

                if (pool.size() > 0) {
                    DHFactoryPoolElement dhPoolElement = null;
                    boolean found = false;
                    Iterator<DHFactory> i = pool.iterator();
                    while (i.hasNext() && !found) {
                        dhPoolElement = (DHFactoryPoolElement) i.next();
                        found = dhPoolElement.isServiceExecuted(key);
                    }
                    if (!found) {
                        logger.debug("DHFactoryPool - not found instance in pool for key: " + key);
                        ++serviceMiss;
                        statServiceMiss.hint();
                        dhPoolElement = (DHFactoryPoolElement) pool.removeFirst();
                    }
                    else {
                        logger.debug("DHFactoryPool - found instance in pool for key: " + key);
                        pool.remove(dhPoolElement);
                    }
                    logger.debug("DHFactoryPool - extracting instance from pool(" + pool.size() + "/"
                            + created + "/" + maximumCreation + ")");
                    assignedDH.add(dhPoolElement);
                    return dhPoolElement;
                }

                if ((maximumCreation == -1) || (created < maximumCreation)) {
                    DHFactory dhf = createDHFactory();
                    ++poolMiss;
                    statPoolMiss.hint();
                    logger.debug("DHFactoryPool - not found instance in pool for key: " + key);
                    logger.debug("DHFactoryPool - creating new instance(" + pool.size() + "/" + created
                            + "/" + maximumCreation + ")");
                    assignedDH.add(dhf);
                    return dhf;
                }

                long waitTime = Math.min(timeout, endTime - System.currentTimeMillis());
                if (waitTime <= 0) {
                    logger.debug("DHFactoryPool - timeout occurred(" + pool.size() + "/" + created + "/"
                            + maximumCreation + ")");
                    throw new DataHandlerException("DHFactoryPool Timeout");
                }
                wait(waitTime);
            }
        }
    }

    /**
     *
     * @param dhf
     */
    public void releaseDHFactory(DHFactory dhf)
    {
        if (dhf == null) {
            return;
        }

        logger.debug("DHFactoryPool - releasing instance(" + pool.size() + "/" + created + "/"
                + maximumCreation + ")");

        if (shutdownFlag) {
            logger.debug("DHFactoryPool - ShutdownEvent received, destroying instance");
            dhf.destroy();
            return;
        }
        synchronized (this) {
            try {
                if (assignedDH.remove(dhf)) {
                    if ((maximumSize == -1) || ((pool != null) && (pool.size() < maximumSize))) {
                        pool.addFirst(dhf);

                        long now = System.currentTimeMillis();
                        if ((shrinkDelayTime == -1) || (now < nextShrinkTime) || (pool.size() <= initialSize)) {
                            return;
                        }
                        logger.debug("DHFactoryPool - shrink time elapsed");
                        dhf = pool.removeLast();
                    }
                    destroyDHFactory(dhf);
                    logger.debug("DHFactoryPool - destroying instance(" + pool.size() + "/" + created
                            + "/" + maximumCreation + ")");
                }
                else {
                    logger.debug("DHFactoryPool - instance not created by this pool, destroing it");
                    dhf.destroy();
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
        logger.debug("DHFactoryPool - Begin destroying instances");
        while (pool.size() > 0) {
            DHFactory dhf = pool.removeFirst();
            destroyDHFactory(dhf);
        }
        logger.debug("DHFactoryPool - End destroying instances");
        assignedDH.clear();
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

    /**
     * @see it.greenvulcano.event.util.shutdown.ShutdownEventListener#shutdownStarted(it.greenvulcano.event.util.shutdown.ShutdownEvent)
     */
    public void shutdownStarted(ShutdownEvent event)
    {
        shutdownFlag = true;
        destroy();
    }

    private DHFactory createDHFactory() throws DataHandlerException
    {
        DHFactory dhf = new DHFactoryPoolElement();
        nextShrinkTime = System.currentTimeMillis() + shrinkDelayTime;
        ++created;
        if (created > maxCreated) {
            maxCreated = created;
        }
        return dhf;
    }

    private void destroyDHFactory(DHFactory dhf)
    {
        dhf.destroy();
        nextShrinkTime = System.currentTimeMillis() + shrinkDelayTime;
        if (created > 0) {
            --created;
        }
    }
}
