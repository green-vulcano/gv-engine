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
package it.greenvulcano.gvesb.internal.log;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.log.GVBufferDump;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVDump
{
    private GVBuffer         gvBuffer        = null;
    private int              maxBufferLength = -1;
    private boolean          onlyData        = false;


    /**
     * @param gvBuffer
     */
    public GVDump(GVBuffer gvBuffer)
    {
        this.gvBuffer = gvBuffer;
    }

    /**
     * @param gvBuffer
     * @param onlyData
     */
    public GVDump(GVBuffer gvBuffer, boolean onlyData)
    {
        this.gvBuffer = gvBuffer;
        this.onlyData = onlyData;
    }

    /**
     * @param gvBuffer
     * @param maxBufferLength
     */
    public GVDump(GVBuffer gvBuffer, int maxBufferLength)
    {
        this.gvBuffer = gvBuffer;
        this.maxBufferLength = maxBufferLength;
    }

    /**
     * @param gvBuffer
     * @param maxBufferLength
     * @param onlyData
     */
    public GVDump(GVBuffer gvBuffer, int maxBufferLength, boolean onlyData)
    {
        this.gvBuffer = gvBuffer;
        this.maxBufferLength = maxBufferLength;
        this.onlyData = onlyData;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {

        if (gvBuffer == null) {
            return "null GVBuffer";
        }
        StringBuilder buf = new StringBuilder();
        GVBufferDump gvBufferDump = new GVBufferDump(gvBuffer, maxBufferLength, onlyData);
        buf.append(gvBufferDump.toString());

        return buf.toString();
    }
}
