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
package it.greenvulcano.gvesb.datahandling;

import it.greenvulcano.gvesb.datahandling.utils.DiscardCause;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DHResult class
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DHResult implements Serializable
{

    private static final long  serialVersionUID = 300L;
    private Object             data;
    private long               read;
    private long               total;
    private long               insert;
    private long               update;
    private long               discard;
    private List<DiscardCause> discardCauseList = new ArrayList<DiscardCause>();

    /**
     *
     */
    public void reset()
    {
        data = null;
        read = 0;
        total = 0;
        insert = 0;
        update = 0;
        discard = 0;
        discardCauseList = new ArrayList<DiscardCause>();
    }

    /**
     * @return the data
     */
    public Object getData()
    {
        return data;
    }

    /**
     * @param data
     *        the data to set
     */
    public void setData(Object data)
    {
        this.data = data;
    }

    /**
     * @return the read
     */
    public long getRead()
    {
        return read;
    }

    /**
     * @param read
     *        the read to set
     */
    public void setRead(long read)
    {
        this.read = read;
    }

    /**
     * @return the total
     */
    public long getTotal()
    {
        return total;
    }

    /**
     * @param total
     *        the total to set
     */
    public void setTotal(long total)
    {
        this.total = total;
    }

    /**
     * @return the insert
     */
    public long getInsert()
    {
        return insert;
    }

    /**
     * @param insert
     *        the insert to set
     */
    public void setInsert(long insert)
    {
        this.insert = insert;
    }

    /**
     * @return the update
     */
    public long getUpdate()
    {
        return update;
    }

    /**
     * @param update
     *        the update to set
     */
    public void setUpdate(long update)
    {
        this.update = update;
    }

    /**
     * @return the discard
     */
    public long getDiscard()
    {
        return discard;
    }

    /**
     * @param discard
     *        the discard to set
     */
    public void setDiscard(long discard)
    {
        this.discard = discard;
    }

    /**
     * @return the discardCauseList
     */
    public List<DiscardCause> getDiscardCauseList()
    {
        return discardCauseList;
    }

    /**
     * @return the discardCauseList as String
     */
    public String getDiscardCauseListAsString()
    {
        StringBuilder DSList = new StringBuilder();

        for (DiscardCause dc : discardCauseList) {
            DSList.append(dc.toString());
        }
        if (DSList.length() > 0) {
            DSList.replace(0, 0, "");
        }

        return DSList.toString();
    }

    /**
     * @param discardCauseList
     *        the discardCauseList to set
     */
    public void setDiscardCauseList(List<DiscardCause> discardCauseList)
    {
        this.discardCauseList = discardCauseList;
    }

    /**
     * @param discardCause
     *        the discardCause to add
     */
    public void addDiscardCause(DiscardCause discardCause)
    {
        discardCauseList.add(discardCause);
    }

    /**
     *
     */
    public void resetDiscardList()
    {
        discardCauseList = new ArrayList<DiscardCause>();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String output = null;
        output = "\n DHResult content: \n";
        output += "\nRead: " + getRead();
        output += "\nInsert: " + getInsert();
        output += "\nUpdate: " + getUpdate();
        output += "\nDiscard: " + getDiscard();
        output += "\nTotal: " + getTotal();
        output += "\nDiscard List: " + getDiscardCauseList();

        return output;
    }
}
