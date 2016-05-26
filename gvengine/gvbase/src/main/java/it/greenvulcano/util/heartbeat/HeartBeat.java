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
package it.greenvulcano.util.heartbeat;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.w3c.dom.Node;

/**
 * @version 3.2.0 09/11/2011
 * @author GreenVulcano Developer Team
 */
public abstract class HeartBeat implements ConfigurationListener, ShutdownEventListener
{
    protected boolean              confChanged        = true;
    protected boolean              shutdownInProgress = false;

    private Map<Integer, BeatData> beatMap            = new HashMap<Integer, BeatData>();
    private AtomicInteger          idGen              = new AtomicInteger(0);

    public class BeatData
    {
        public String  subsystem;
        public long    timestamp;
        public boolean success;

        public BeatData(String subsystem)
        {
            this.subsystem = subsystem;
            this.timestamp = System.currentTimeMillis();
            this.success   = false;
        }

        public BeatData(String subsystem, long timestamp, boolean success)
        {
            this.subsystem = subsystem;
            this.timestamp = timestamp;
            this.success   = success;
        }
    }

    public final void init(Node node) throws HeartBeatException
    {
        internalInit(node);
        confChanged = false;
    }

    protected abstract void internalInit(Node node) throws HeartBeatException;

    protected abstract void beat(String subsystem, long timestamp, boolean success) throws HeartBeatException;

    public abstract long lastBeat(String subsystems, long fromTime) throws HeartBeatException;

    public void beat(String subsystem) throws HeartBeatException
    {
        beat(subsystem, System.currentTimeMillis(), true);
    }

    public int prepareBeat(String subsystem) throws HeartBeatException
    {
        int id = idGen.incrementAndGet();
        beatMap.put(id, new BeatData(subsystem));
        return id;
    }

    public void confirmBeat(int id) throws HeartBeatException
    {
        confirmBeat(id, true);
    }
    
    public void confirmBeat(int id, boolean success) throws HeartBeatException
    {
        BeatData bd = beatMap.remove(id);
        if (bd != null) {
            beat(bd.subsystem, bd.timestamp, success);
        }
    }

    public void cancelBeat(int id) throws HeartBeatException
    {
        beatMap.remove(id);
    }

    @Override
    public void shutdownStarted(ShutdownEvent event)
    {
        shutdownInProgress = true;
    }

    @Override
    public void configurationChanged(ConfigurationEvent evt)
    {
        confChanged = true;
    }
}
