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
package it.greenvulcano.gvesb.utils.concurrency;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVPublicException;

/**
 * ServiceConcurrencyInfo class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class ServiceConcurrencyInfo
{
    /**
     * The maximum concurrency for the current service
     */
    private int    maxConcurrency;
    /**
     * The current concurrency for the current service
     */
    private int    currentConcurrency;

    /**
     * The subsystem name
     */
    private String subSystemName = "";

    /**
     * Constructor
     *
     * @param subSystemName
     *        The subsystem name
     * @param maxConcurrency
     *        The maximum concurrency for the given service
     */
    public ServiceConcurrencyInfo(String subSystemName, int maxConcurrency)
    {
        this.subSystemName = subSystemName;
        this.maxConcurrency = maxConcurrency;
        currentConcurrency = 0;
    }

    /**
     * Increase the current concurrency for the service
     *
     * @param gvBuffer
     *        the input GVBuffer instance
     * @throws GVPublicException
     *         if max concurrency reached
     */
    public synchronized void add(GVBuffer gvBuffer) throws GVPublicException
    {
        if (currentConcurrency >= maxConcurrency) {
            throw new GVPublicException("GV_CONCURRENCY_ERROR",
                    new String[][]{{"service", gvBuffer.getService()}, {"system", gvBuffer.getSystem()},
                            {"id", gvBuffer.getId().toString()}, {"subsystem", subSystemName}});
        }
        currentConcurrency++;
    }

    /**
     * Decrease the current concurrency for the service
     */
    public synchronized void remove()
    {
        if (currentConcurrency > 0) {
            currentConcurrency--;
        }
    }

    /**
     * @return the current concurrency for the service
     */
    public int getCurrentConcurrency()
    {
        return currentConcurrency;
    }
}
