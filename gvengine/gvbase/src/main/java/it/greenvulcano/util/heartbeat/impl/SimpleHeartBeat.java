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
package it.greenvulcano.util.heartbeat.impl;

import it.greenvulcano.util.heartbeat.HeartBeat;
import it.greenvulcano.util.heartbeat.HeartBeatException;
import it.greenvulcano.util.txt.DateUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * @version 3.2.0 09/11/2011
 * @author GreenVulcano Developer Team
 */
public class SimpleHeartBeat extends HeartBeat
{
    private static Logger         logger    = org.slf4j.LoggerFactory.getLogger(SimpleHeartBeat.class);
    private Map<String, BeatData> ssBeatMap = new HashMap<String, BeatData>();

    /**
     * 
     */
    public SimpleHeartBeat()
    {
        super();
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.util.heartbeat.HeartBeat#internalInit(org.w3c.Node)
     */
    @Override
    protected void internalInit(Node node) throws HeartBeatException
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.util.heartbeat.HeartBeat#beat(java.lang.String, long, boolean)
     */
    @Override
    protected void beat(String subsystem, long timestamp, boolean success) throws HeartBeatException
    {
        ssBeatMap.put(subsystem, new BeatData(subsystem, timestamp, success));
        logger.debug("HeartBeat: [" + DateUtils.dateToString(new Date(timestamp), "yyyyMMdd HH:mm:ss.SSS") + "] from ["
                + subsystem + "] [" + (success ? "S" : "F") + "][" + DateUtils.durationToString(System.currentTimeMillis() - timestamp) + "]");
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.util.heartbeat.HeartBeat#lastBeat(java.lang.String, long)
     */
    @Override
    public long lastBeat(String subsystems, long fromTime) throws HeartBeatException
    {
        BeatData bd = ssBeatMap.remove(subsystems);
        if (bd != null) {
            if (bd.timestamp > fromTime) {
                return bd.timestamp;
            }
        }
        return -1;
    }
}
