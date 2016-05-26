/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 */
package it.greenvulcano.util.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version 3.2.0 20/nov/2011
 * @author GreenVulcano Developer Team
 */
public class BaseThreadFactory implements ThreadFactory
{
    private boolean       asDeamon     = true;
    private String        thNamePrefix = null;
    private AtomicInteger count        = new AtomicInteger(-1);

    /**
     * @param thNamePrefix
     *        Thread name prefix
     * @param asDeamon
     *        default true
     */
    public BaseThreadFactory(String thNamePrefix, boolean asDeamon)
    {
        this.thNamePrefix = thNamePrefix;
        this.asDeamon = asDeamon;
    }

    /**
     * 
     * @param asDeamon
     *        default true
     */
    public BaseThreadFactory(boolean asDeamon)
    {
        this.asDeamon = asDeamon;
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    @Override
    public Thread newThread(Runnable r)
    {
        int idx = count.incrementAndGet();
        BaseThread th = (thNamePrefix != null) ? new BaseThread(r, thNamePrefix + "_" + idx) : new BaseThread(r);
        th.setDaemon(asDeamon);
        return th;
    }

    /**
     * 
     * @return
     *     The current name prefix for newly created Threads
     */
    public String getThNamePrefix() {
        return this.thNamePrefix;
    }

    /**
     * Set the name prefix for newly created Threads
     *
     * @param thNamePrefix
     */
    public void setThNamePrefix(String thNamePrefix) {
        this.thNamePrefix = thNamePrefix;
    }

    /**
     * Set the asDeamon flag for newly created Threads
     *
     * @param asDeamon
     */
    public void setAsDeamon(boolean asDeamon) {
        this.asDeamon = asDeamon;
    }

    /**
     * @return
     *    Number of created Threads
     */
    public int getCount() {
        return this.count.get();
    }
}
