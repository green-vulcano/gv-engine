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
package it.greenvulcano.gvesb.throughput;

import java.io.Serializable;

/**
 * This is the ThroughputData object having the Throughput data value.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class ThroughputData implements Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = 300L;
    private float             throughputNod;
    private float             throughputSvc;
    private float             historyThroughputSvc;
    private float             historyThroughputNod;
    private String            location;

    /**
     *
     *
     */
    public ThroughputData()
    {
        throughputNod = 0.0F;
        throughputSvc = 0.0F;
        historyThroughputSvc = 0.0F;
        historyThroughputNod = 0.0F;
        location = "";
    }

    /**
     *
     * @param throughput
     */
    public void setThroughputNod(float throughput)
    {
        if (Float.isNaN(throughput)) {
            throughputNod = 0;
        }
        else if (Float.isInfinite(throughput)) {
            throughputNod = Float.MAX_VALUE;
        }
        else {
            throughputNod = throughput;
        }
    }

    /**
     *
     * @param throughput
     */
    public void setThroughputSvc(float throughput)
    {
        if (Float.isNaN(throughput)) {
            throughputSvc = 0;
        }
        else if (Float.isInfinite(throughput)) {
            throughputSvc = Float.MAX_VALUE;
        }
        else {
            throughputSvc = throughput;
        }
    }

    /**
     *
     * @param historyThroughput
     */
    public void setHistoryThroughputSvc(float historyThroughput)
    {
        if (Float.isNaN(historyThroughput)) {
            historyThroughputSvc = 0;
        }
        else if (Float.isInfinite(historyThroughput)) {
            historyThroughputSvc = Float.MAX_VALUE;
        }
        else {
            historyThroughputSvc = historyThroughput;
        }
    }

    /**
     *
     * @param historyThroughput
     */
    public void setHistoryThroughputNod(float historyThroughput)
    {
        if (Float.isNaN(historyThroughput)) {
            historyThroughputNod = 0;
        }
        else if (Float.isInfinite(historyThroughput)) {
            historyThroughputNod = Float.MAX_VALUE;
        }
        else {
            historyThroughputNod = historyThroughput;
        }
    }

    /**
     *
     * @param location
     */
    public void setLocation(String location)
    {
        this.location = location;
    }

    /**
     *
     * @return the field <code>throughputNod</code>
     */
    public float getThroughputNod()
    {
        return throughputNod;
    }

    /**
     *
     * @return the field <code>throughputSvc</code>
     */
    public float getThroughputSvc()
    {
        return throughputSvc;
    }

    /**
     *
     * @return the field <code>historyThroughputNod</code>
     */
    public float getHistoryThroughputNod()
    {
        return historyThroughputNod;
    }

    /**
     *
     * @return the field <code>historyThroughputSvc</code>
     */
    public float getHistoryThroughputSvc()
    {
        return historyThroughputSvc;
    }

    /**
     *
     * @return the field <code>location</code>
     */
    public String getLocation()
    {
        return location;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "location = " + location + " - throughputNod = " + throughputNod + " - throughputSvc = " + throughputSvc
                + " - historyThroughputSvc = " + historyThroughputSvc + " - historyThroughputNod = "
                + historyThroughputNod;
    }
}
