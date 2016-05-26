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
package it.greenvulcano.util.thread;

import java.util.concurrent.atomic.AtomicLong;

/**
 * BaseThread class
 *
 * @version 3.2.0 Aug 28, 2011
 * @author GreenVulcano Developer Team
 */
public class BaseThread extends Thread
{
    private static AtomicLong instCount       = new AtomicLong();
    private static AtomicLong instIdCount     = new AtomicLong();
    private long              instId          = 0;
    private static boolean    dumpInstCount   = false;
    private static boolean    dumpCreateStack = false;

    static {
        try {
            dumpInstCount = Boolean.getBoolean("it.greenvulcano.util.thread.BaseThread.dumpInstCount");
            dumpCreateStack = Boolean.getBoolean("it.greenvulcano.util.thread.BaseThread.dumpCreateStack");
        }
        catch (Exception exc) {
            dumpInstCount = false;
            dumpCreateStack = false;
        }
    }

    /**
     *
     */
    public BaseThread()
    {
        incrCounter();
        dumpCreate();
    }

    /**
     * @param target
     */
    public BaseThread(Runnable target)
    {
        super(target);
        incrCounter();
        dumpCreate();
    }

    /**
     * @param name
     */
    public BaseThread(String name)
    {
        super(name);
        incrCounter();
        dumpCreate();
    }

    /**
     * @param group
     * @param target
     */
    public BaseThread(ThreadGroup group, Runnable target)
    {
        super(group, target);
        incrCounter();
        dumpCreate();
    }

    /**
     * @param target
     * @param name
     */
    public BaseThread(Runnable target, String name)
    {
        super(target, name);
        incrCounter();
        dumpCreate();
    }

    /**
     * @param group
     * @param name
     */
    public BaseThread(ThreadGroup group, String name)
    {
        super(group, name);
        incrCounter();
        dumpCreate();
    }

    /**
     * @param group
     * @param target
     * @param name
     */
    public BaseThread(ThreadGroup group, Runnable target, String name)
    {
        super(group, target, name);
        incrCounter();
        dumpCreate();
    }

    /**
     * @param group
     * @param target
     * @param name
     * @param stackSize
     */
    public BaseThread(ThreadGroup group, Runnable target, String name, long stackSize)
    {
        super(group, target, name, stackSize);
        incrCounter();
        dumpCreate();
    }

    /**
     *
     * @return
     */
    public static long getInstCount()
    {
        return instCount.get();
    }

    /**
     *
     * @return
     */
    public long getInstId()
    {
        return instId;
    }

    public static String getStackTrace(Thread thr)
    {
        StringBuffer str = new StringBuffer();
        StackTraceElement[] elems = thr.getStackTrace();
        int size = elems.length;
        for (int i = 0; i < size; i++) {
            StackTraceElement stk = elems[i];
            str.append("\t").append(stk.toString()).append("\n");
        }
        return str.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable
    {
        instCount.decrementAndGet();
        if (dumpInstCount) {
            System.out.println(getClass() + " - Finalized[" + getName() + "] - instanceId=" + instId + "/"
                    + instIdCount + " - instanceCount=" + instCount);
        }
        super.finalize();
    }

    private void incrCounter()
    {
        instCount.incrementAndGet();
        instId = instIdCount.incrementAndGet();
    }

    private void dumpCreate()
    {
        if (dumpInstCount) {
            System.out.println(getClass() + " - Created[" + getName() + "] - instanceId=" + instId + "/" + instIdCount + " - instanceCount="
                    + instCount);
        }
        if (dumpCreateStack) {
            System.out.println("Create Stack:\n" + getStackTrace(Thread.currentThread()));
        }
    }
}
