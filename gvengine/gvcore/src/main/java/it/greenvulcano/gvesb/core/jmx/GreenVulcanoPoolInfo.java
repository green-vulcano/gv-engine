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
package it.greenvulcano.gvesb.core.jmx;

import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;

/**
 * GreenVulcanoPoolInfo class.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public class GreenVulcanoPoolInfo
{
    /**
     * the object JMX descriptor.
     */
    public static final String DESCRIPTOR_NAME  = "GreenVulcanoPoolInfo";

    private GreenVulcanoPool   GreenVulcanoPool = null;

    /**
     * @param greenVulcanoPool
     */
    public GreenVulcanoPoolInfo(GreenVulcanoPool greenVulcanoPool)
    {
        GreenVulcanoPool = greenVulcanoPool;
    }

    /**
     * @return the pool's default timeout
     * @see it.greenvulcano.gvesb.core.pool.GreenVulcanoPool#getDefaultTimeout()
     */
    public long getDefaultTimeout()
    {
        return GreenVulcanoPool.getDefaultTimeout();
    }

    /**
     * @return the number of GreenVulcanoPool object in use
     * @see it.greenvulcano.gvesb.core.pool.GreenVulcanoPool#getInUseCount()
     */
    public int getInUseCount()
    {
        return GreenVulcanoPool.getInUseCount();
    }

    /**
     * @return the initial size of GreenVulcanoPool
     * @see it.greenvulcano.gvesb.core.pool.GreenVulcanoPool#getInitialSize()
     */
    public int getInitialSize()
    {
        return GreenVulcanoPool.getInitialSize();
    }

    /**
     * @return the maximum number of GreenVulcanoPool object created
     * @see it.greenvulcano.gvesb.core.pool.GreenVulcanoPool#getMaxCreated()
     */
    public int getMaxCreated()
    {
        return GreenVulcanoPool.getMaxCreated();
    }

    /**
     * @return the maximum creation parameter
     * @see it.greenvulcano.gvesb.core.pool.GreenVulcanoPool#getMaximumCreation()
     */
    public int getMaximumCreation()
    {
        return GreenVulcanoPool.getMaximumCreation();
    }

    /**
     * @return the maximum size of GreenVulcanoPool
     * @see it.greenvulcano.gvesb.core.pool.GreenVulcanoPool#getMaximumSize()
     */
    public int getMaximumSize()
    {
        return GreenVulcanoPool.getMaximumSize();
    }

    /**
     * @return the missed GreenVulcanoPool object
     * @see it.greenvulcano.gvesb.core.pool.GreenVulcanoPool#getPoolMiss()
     */
    public long getPoolMiss()
    {
        return GreenVulcanoPool.getPoolMiss();
    }

    /**
     * @return the missed ratio GreenVulcanoPool object
     * @see it.greenvulcano.gvesb.core.pool.GreenVulcanoPool#getPoolMissRatio()
     */
    public float getPoolMissRatio()
    {
        return GreenVulcanoPool.getPoolMissRatio();
    }

    /**
     * @return the current object number in the pool
     * @see it.greenvulcano.gvesb.core.pool.GreenVulcanoPool#getPooledCount()
     */
    public int getPooledCount()
    {
        return GreenVulcanoPool.getPooledCount();
    }

    /**
     * @return the shrink delay time
     * @see it.greenvulcano.gvesb.core.pool.GreenVulcanoPool#getShrinkDelayTime()
     */
    public long getShrinkDelayTime()
    {
        return GreenVulcanoPool.getShrinkDelayTime();
    }

    /**
     * @return the subsystem configured for the pool
     * @see it.greenvulcano.gvesb.core.pool.GreenVulcanoPool#getSubsystem()
     */
    public String getSubsystem()
    {
        return GreenVulcanoPool.getSubsystem();
    }

}
