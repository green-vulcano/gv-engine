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
package it.greenvulcano.gvesb.datahandling.utils;

import java.io.Serializable;

/**
 * DiscardCause class
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DiscardCause implements Serializable
{
    private static final long serialVersionUID = 300L;
    private long              record;
    private String            cause;

    /**
     * @param record
     * @param cause
     */
    public DiscardCause(long record, String cause)
    {
        this.record = record;
        this.cause = cause;
    }

    /**
     * @return the record
     */
    public long getRecord()
    {
        return record;
    }

    /**
     * @param record
     *        the record to set
     */
    public void setRecord(long record)
    {
        this.record = record;
    }

    /**
     * @return the cause
     */
    public String getCause()
    {
        return cause;
    }

    /**
     * @param cause
     *        the cause to set
     */
    public void setCause(String cause)
    {
        this.cause = cause;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {

        StringBuilder output = new StringBuilder();

        output.append("\n\tRecord: " + getRecord());
        output.append(" - Cause: " + getCause());

        return output.toString();
    }
}
