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
package it.greenvulcano.gvesb.core.pool;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVPublicException;
import it.greenvulcano.gvesb.core.GreenVulcano;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.jmx.JMXEntryPoint;

import it.greenvulcano.util.Stats;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * Object Pool <code>GreenVulcano</code>.
 * 
 * @version 3.1.0 Aug 23, 2011
 * @author GreenVulcano Developer Team
 */
public class GreenVulcanoPool implements ShutdownEventListener
{
    
	public static final String       DEFAULT_SUBSYSTEM = "default-pool";
	
    public static final long         DEFAULT_SHRINK_DELAY_TIME = 1000 * 60 * 15;

    public static final long         DEFAULT_TIMEOUT           = 1000 * 20;

    public static final int          DEFAULT_INITIAL_SIZE      = 1;

    public static final int          DEFAULT_MAXIMUM_SIZE      = 10;

    public static final int          DEFAULT_MAXIMUM_CREATION  = 50;

    private static final Logger      logger                    = org.slf4j.LoggerFactory.getLogger(GreenVulcanoPool.class);
    /**
     * Pool of GreenVulcano instances.
     */
    private LinkedList<GreenVulcano> pool                      = new LinkedList<GreenVulcano>();
    /**
     * Set instance pool assigned.
     */
    private Set<GreenVulcano>        assignedGV                = new HashSet<GreenVulcano>();
    /**
     * Initial size pool.
     */
    private int                      initialSize               = DEFAULT_INITIAL_SIZE;
    /**
     * Max size pool, -1 = unlimited.
     */
    private int                      maximumSize               = DEFAULT_MAXIMUM_SIZE;
    /**
     * Max instance created, -1 = unlimited.
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
     * Subsystem pool owner.
     */
    private String                   subsystem;
    /**
     * Server instance.
     */
    private String                   serverName;
    /**
     * Number of created instances.
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
     * @param initialSize
     * @param maximumSize
     * @param maximumCreation
     * @param subsystem
     * @throws GVCoreException
     * @throws GVPublicException
     */
    GreenVulcanoPool(int initialSize, int maximumSize, int maximumCreation, String subsystem) throws GVCoreException,
            GVPublicException
    {
        init(initialSize, maximumSize, maximumCreation, subsystem);
        logger.debug("Initialized GreenVulcanoPool instance: subsystem= " + subsystem + ", initialSize= " + initialSize 
                + ", maximumSize= " + maximumSize + ", maximumCreation= " + maximumCreation + ", defaultTimeout= " 
                + defaultTimeout + ", shrinkDelayTime= " + shrinkDelayTime);
        ShutdownEventLauncher.addEventListener(this);
    }

    /**
     * @param config
     * @throws GVCoreException
     * @throws GVPublicException
     */
    GreenVulcanoPool(Node config) throws GVCoreException, GVPublicException
    {
        init(config);
    }

    /**
     * @param config
     * @throws GVCoreException
     * @throws GVPublicException
     */
    public void init(Node config) throws GVCoreException, GVPublicException
    {
        int initialSizeL = XMLConfig.getInteger(config, "@initial-size", DEFAULT_INITIAL_SIZE);
        int maximumSizeL = XMLConfig.getInteger(config, "@maximum-size", DEFAULT_MAXIMUM_SIZE);
        int maximumCreationL = XMLConfig.getInteger(config, "@maximum-creation", DEFAULT_MAXIMUM_CREATION);
        String subsystemL = XMLConfig.get(config, "@subsystem", "GreenVulcano");

        synchronized (this) {
            logger.debug("Subsystem= " + subsystem + " - Begin destroying instances");
            while (pool.size() > 0) {
                GreenVulcano GreenVulcano = pool.removeFirst();
                destroyGreenVulcano(GreenVulcano, false);
            }
            logger.debug("subsystem= " + subsystem + " - End destroying instances");
            assignedGV.clear();
        }
        
        init(initialSizeL, maximumSizeL, maximumCreationL, subsystemL);
        setDefaultTimeout(XMLConfig.getLong(config, "@default-timeout", DEFAULT_TIMEOUT));
        setShrinkDelayTime(XMLConfig.getLong(config, "@shrink-timeout", DEFAULT_SHRINK_DELAY_TIME));
        nextShrinkTime = System.currentTimeMillis() + shrinkDelayTime;
        logger.debug("Initialized GreenVulcanoPool instance: subsystem= " + subsystem + ", initialSize= " + initialSize 
                + ", maximumSize= " + maximumSize + ", maximumCreation= " + maximumCreation + ", defaultTimeout= " 
                + defaultTimeout + ", shrinkDelayTime= " + shrinkDelayTime);
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
    public void setShrinkDelayTime(long shrinkDelayTime)
    {
        if ((shrinkDelayTime != -1) && (shrinkDelayTime < 1)) {
            throw new IllegalArgumentException("shrinkDelayTime can be -1 or > 0, subsystem=" + subsystem);
        }
        this.shrinkDelayTime = shrinkDelayTime;
    }

    /**
     * @return Returns the serverName.
     */
    public String getServerName()
    {
        return serverName;
    }

    /**
     * @return Returns the subsystem.
     */
    public String getSubsystem()
    {
        return subsystem;
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
    public void setDefaultTimeout(long defaultTimeout)
    {
        if (defaultTimeout < 1) {
            throw new IllegalArgumentException("defaultTimeout < 1, subsystem=" + subsystem);
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
        return assignedGV.size();
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
     * @param gvBuffer
     * @return the GreenVulcano object or <code>null</code> if no object is
     *         available after the default timeout is elapsed.
     * 
     * @throws InterruptedException
     * @throws GVCoreException
     * @throws GVPublicException
     * @throws GreenVulcanoPoolException
     * 
     * @see #getGreenVulcano(GVBuffer, long)
     */
    public GreenVulcano getGreenVulcano(GVBuffer gvBuffer) throws InterruptedException, GVCoreException,
            GVPublicException, GreenVulcanoPoolException
    {
        return getGreenVulcano(gvBuffer, defaultTimeout);
    }

    /**
     * 
     * @param gvBuffer
     * @param timeout
     * 
     * @return <code>null</code> if not available a <code>GreenVulcano</code>
     *         prior to timeout
     * @exception InterruptedException
     * @throws GVCoreException
     * @throws GVPublicException
     * @throws GreenVulcanoPoolException
     */
    public GreenVulcano getGreenVulcano(GVBuffer gvBuffer, long timeout) throws InterruptedException, GVCoreException,
            GVPublicException, GreenVulcanoPoolException
    {
        if (shutdownFlag) {
            throw new GVPublicException("subSystem=" + getSubsystem() + " - ShutdownEvent received, pool disabled");
        }
        long endTime = System.currentTimeMillis() + timeout;
        String key = GreenVulcanoPoolElement.getKey(gvBuffer);
        while (true) {
            synchronized (this) {
                if (pool == null) {
                    return null;
                }

                while (pool.size() > 0) {
                    GreenVulcanoPoolElement gvPoolElement = null;
                    boolean found = false;
                    Iterator<GreenVulcano> i = pool.iterator();
                    while (i.hasNext() && !found) {
                        gvPoolElement = (GreenVulcanoPoolElement) i.next();
                        found = gvPoolElement.isServiceExecuted(key);
                    }
                    if (!found) {
                        gvPoolElement = (GreenVulcanoPoolElement) pool.removeFirst();
                    }
                    else {
                        pool.remove(gvPoolElement);
                    }
                    if (gvPoolElement.isValid()) {
                        if (!found) {
                            logger.debug("subsystem=" + subsystem + " - not found instance in pool for key: " + key);
                            ++serviceMiss;
                            statServiceMiss.hint();
                        }
                        else {
                            logger.debug("subsystem=" + subsystem + " - found instance in pool for key: " + key);
                        }
                        logger.debug("subsystem=" + subsystem + " - extracting instance from pool(" + pool.size() + "/"
                                + created + "/" + maximumCreation + ")");
                        assignedGV.add(gvPoolElement);
                        logger.debug("Using GreenVulcano instance " + gvPoolElement);
                        return gvPoolElement;
                    }
                    else {
                        releaseGreenVulcano(gvPoolElement);
                    }
                }

                if ((maximumCreation == -1) || (created < maximumCreation)) {
                    GreenVulcano gvPoolElement = createGreenVulcano();
                    ++poolMiss;
                    statPoolMiss.hint();
                    logger.debug("subsystem=" + subsystem + " - not found instance in pool for key: " + key);
                    logger.debug("subsystem=" + subsystem + " - creating new instance(" + pool.size() + "/" + created
                            + "/" + maximumCreation + ")");
                    assignedGV.add(gvPoolElement);
                    logger.debug("Using GreenVulcano instance " + gvPoolElement);
                    return gvPoolElement;
                }

                long waitTime = Math.min(timeout, endTime - System.currentTimeMillis());
                if (waitTime <= 0) {
                    logger.debug("subsystem=" + subsystem + " - timeout occurred(" + pool.size() + "/" + created + "/"
                            + maximumCreation + ")");
                    throw new GreenVulcanoPoolException("Timeout, subsystem=" + subsystem);
                }
                wait(waitTime);
            }
        }
    }

    /**
     * Invoca anche il metodo <code>MDCContext.removeSubSystem()</code>.
     * 
     * @param gvesb
     */
    public void releaseGreenVulcano(GreenVulcano gvesb)
    {
        if (gvesb == null) {
            return;
        }

        logger.debug("Releasing GreenVulcano[" + gvesb.isValid() + "] instance " + gvesb);
        logger.debug("subsystem=" + subsystem + " - releasing instance(" + pool.size() + "/" + created + "/"
                + maximumCreation + ")");

        if (shutdownFlag) {
            logger.debug("subsystem=" + subsystem + " - ShutdownEvent received, destroying instance");
            gvesb.destroy(true);
            return;
        }
        synchronized (this) {
            try {
                if (assignedGV.remove(gvesb)) {
                    if (gvesb.isValid() && ((maximumSize == -1) || ((pool != null) && (pool.size() < maximumSize)))) {
                        pool.addFirst(gvesb);

                        long now = System.currentTimeMillis();
                        if ((shrinkDelayTime == -1) || (now < nextShrinkTime) || (pool.size() <= initialSize)) {
                            return;
                        }
                        logger.debug("subsystem=" + subsystem + " - shrink time elapsed");
                        gvesb = pool.removeLast();
                    }
                    destroyGreenVulcano(gvesb, true);
                    logger.debug("subsystem=" + subsystem + " - destroying instance(" + pool.size() + "/" + created
                            + "/" + maximumCreation + ")");
                }
                else {
                    logger.debug("subsystem=" + subsystem + " - instance not created by this pool, destroing it");
                    gvesb.destroy(true);
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
    public synchronized void destroy(boolean force)
    {
        if (pool == null) {
            return;
        }
        logger.debug("subsystem=" + subsystem + " - Begin destroying instances");
        while (pool.size() > 0) {
            GreenVulcano GreenVulcano = pool.removeFirst();
            destroyGreenVulcano(GreenVulcano, force);
        }
        logger.debug("subsystem=" + subsystem + " - End destroying instances");
        assignedGV.clear();
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
        destroy(true);
    }

    /**
     * Convenience method that requests a <code>GreenVulcano</code> object from
     * the pool, calls <i>request</i> method on it, and releases it. Also
     * correctly handles the <code>MDC</code> for logging.
     * 
     * @param gvBuffer
     * @return the result of <i>request</i> method invocation.
     * @throws GVPublicException
     *         GreenVulcano exception
     * @throws GreenVulcanoPoolException
     *         GreenVulcano's pool exception if GreenVulcano is not available.
     */
    public GVBuffer request(GVBuffer gvBuffer) throws GVPublicException, GreenVulcanoPoolException
    {
       
        GreenVulcano gvesb = internalGetGreenVulcano(gvBuffer);
        try {
            return gvesb.request(gvBuffer);
        }
        finally {
            releaseGreenVulcano(gvesb);
           
        }
    }

    /**
     * Convenience method that requests a <code>GreenVulcano</code> object from
     * the pool, calls <i>requestReply</i> method on it, and releases it. Also
     * correctly handles the <code>MDC</code> for logging.
     * 
     * @param gvBuffer
     * @return the result of <i>requestReply</i> method invocation.
     * @throws GVPublicException
     *         GreenVulcano exception
     * @throws GreenVulcanoPoolException
     *         GreenVulcano's pool exception if GreenVulcano is not available.
     */
    public GVBuffer requestReply(GVBuffer gvBuffer) throws GVPublicException, GreenVulcanoPoolException
    {
      
        GVBufferMDC.put(gvBuffer);
    
        GreenVulcano gvesb = internalGetGreenVulcano(gvBuffer);
        try {
            return gvesb.requestReply(gvBuffer);
        }
        finally {
            releaseGreenVulcano(gvesb);
            
        }
    }

    /**
     * Convenience method that requests a <code>GreenVulcano</code> object from
     * the pool, calls <i>getRequest</i> method on it, and releases it. Also
     * correctly handles the <code>MDC</code> for logging.
     * 
     * @param gvBuffer
     * @return the result of <i>getRequest</i> method invocation.
     * @throws GVPublicException
     *         GreenVulcano exception
     * @throws GreenVulcanoPoolException
     *         GreenVulcano's pool exception if GreenVulcano is not available.
     */
    public GVBuffer getRequest(GVBuffer gvBuffer) throws GVPublicException, GreenVulcanoPoolException
    {
        
        GVBufferMDC.put(gvBuffer);
       
        GreenVulcano gvesb = internalGetGreenVulcano(gvBuffer);
        try {
            return gvesb.getRequest(gvBuffer);
        }
        finally {
            releaseGreenVulcano(gvesb);
           
        }
    }

    /**
     * Convenience method that requests a <code>GreenVulcano</code> object from
     * the pool, calls <i>getReply</i> method on it, and releases it. Also
     * correctly handles the <code>MDC</code> for logging.
     * 
     * @param gvBuffer
     * @return the result of <i>getReply</i> method invocation.
     * @throws GVPublicException
     *         GreenVulcano exception
     * @throws GreenVulcanoPoolException
     *         GreenVulcano's pool exception if GreenVulcano is not available.
     */
    public GVBuffer getReply(GVBuffer gvBuffer) throws GVPublicException, GreenVulcanoPoolException
    {

        GVBufferMDC.put(gvBuffer);
     
        GreenVulcano gvesb = internalGetGreenVulcano(gvBuffer);
        try {
            return gvesb.getReply(gvBuffer);
        }
        finally {
            releaseGreenVulcano(gvesb);
           
        }
    }

    /**
     * Convenience method that requests a <code>GreenVulcano</code> object from
     * the pool, calls <i>sendReply</i> method on it, and releases it. Also
     * correctly handles the <code>MDC</code> for logging.
     * 
     * @param gvBuffer
     * @return the result of <i>sendReply</i> method invocation.
     * @throws GVPublicException
     *         GreenVulcano exception
     * @throws GreenVulcanoPoolException
     *         GreenVulcano's pool exception if GreenVulcano is not available.
     */
    public GVBuffer sendReply(GVBuffer gvBuffer) throws GVPublicException, GreenVulcanoPoolException
    {
      
        GVBufferMDC.put(gvBuffer);
       
        GreenVulcano gvesb = internalGetGreenVulcano(gvBuffer);
        try {
            return gvesb.sendReply(gvBuffer);
        }
        finally {
            releaseGreenVulcano(gvesb);
           
        }
    }

    /**
     * Convenience method that requests a <code>GreenVulcano</code> object from
     * the pool, calls <i>forward</i> method on it, and releases it. Also
     * correctly handles the <code>MDC</code> for logging.
     * 
     * @param gvBuffer
     * @param forwardName
     *        the name of the forward
     * @return the result of <i>forward</i> method invocation.
     * @throws GVPublicException
     *         GreenVulcano exception
     * @throws GreenVulcanoPoolException
     *         GreenVulcano's pool exception if GreenVulcano is not available.
     */
    public GVBuffer forward(GVBuffer gvBuffer, String forwardName) throws GVPublicException, GreenVulcanoPoolException
    {
        
           
        GreenVulcano gvesb = internalGetGreenVulcano(gvBuffer);
        try {
            return gvesb.forward(gvBuffer, forwardName);
        }
        finally {
            releaseGreenVulcano(gvesb);
          
        }
    }

    /**
     * @see it.greenvulcano.event.util.shutdown.ShutdownEventListener#shutdownStarted(it.greenvulcano.event.util.shutdown.ShutdownEvent)
     */
    @Override
    public void shutdownStarted(ShutdownEvent event)
    {
        shutdownFlag = true;
        destroy(true);
    }

    private GreenVulcano createGreenVulcano() throws GVCoreException, GVPublicException
    {
        GreenVulcano greenVulcano = new GreenVulcanoPoolElement();
        nextShrinkTime = System.currentTimeMillis() + shrinkDelayTime;
        ++created;
        if (created > maxCreated) {
            maxCreated = created;
        }
        return greenVulcano;
    }

    private void destroyGreenVulcano(GreenVulcano gv, boolean force)
    {
        gv.destroy(force);
        nextShrinkTime = System.currentTimeMillis() + shrinkDelayTime;
        if (created > 0) {
            --created;
        }
    }

    private void init(int initialSize, int maximumSize, int maximumCreation, String subsystem) throws GVCoreException,
            GVPublicException
    {
        if (subsystem == null) {
            throw new IllegalArgumentException("null subsystem");
        }
        if (initialSize < 0) {
            throw new IllegalArgumentException("initialSize < 0, subsystem=" + subsystem);
        }
        if ((maximumSize > 0) && (initialSize > maximumSize)) {
            throw new IllegalArgumentException("initialSize(" + initialSize + ") > maximumSize(" + maximumSize
                    + "), subsystem=" + subsystem);
        }
        if ((maximumCreation > 0) && (maximumSize > maximumCreation)) {
            throw new IllegalArgumentException("maximumSize(" + maximumSize + ") > maximumCreation(" + maximumCreation
                    + "), subsystem=" + subsystem);
        }
        this.created = 0;
        this.initialSize = initialSize;
        this.maximumSize = maximumSize;
        this.maximumCreation = maximumCreation;
        this.subsystem = subsystem;
        try {
            serverName = JMXEntryPoint.getInstance().getServerName();
        }
        catch (Exception exc) {
            throw new GVCoreException("Error initializing GreenVulcanoPool", exc);
        }

       
        for (int i = 0; i < initialSize; ++i) {
            pool.add(createGreenVulcano());
        }
    }

    private GreenVulcano internalGetGreenVulcano(GVBuffer gvBuffer) throws GreenVulcanoPoolException
    {
        GreenVulcano gvesb = null;
        try {
            gvesb = getGreenVulcano(gvBuffer);
        }
        catch (GreenVulcanoPoolException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new GreenVulcanoPoolException(exc);
        }
        if (gvesb == null) {
            throw new GreenVulcanoPoolException("Timeout, subsystem=" + subsystem);
        }
        return gvesb;
    }
}
