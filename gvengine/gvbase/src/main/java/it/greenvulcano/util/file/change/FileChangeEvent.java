/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.util.file.change;

import it.greenvulcano.event.interfaces.Event;

/**
 * Signal a file change.
 * 
 * @version 3.3.0 05/lug/2012
 * @author GreenVulcano Developer Team
 */
public class FileChangeEvent implements Event
{
    /**
     * The configuration event type.
     */
    public static final String EVT_TYPE = "it.greenvulcano.event.FileChangedEvent";

    /**
     * The file affected.
     */
    private String             file;

    /**
     * Constructor.
     * 
     * @param file
     *        the event file
     */
    public FileChangeEvent(String file)
    {
        this.file = file;
    }

    @Override
    public long getCode()
    {
        return 0;
    }

    /**
     * @return the event file name
     */
    public final String getFile()
    {
        return file;
    }

    /**
     * @return a string representation of the event
     */
    @Override
    public final String toString()
    {
        return "FileChangeEvent[file=" + file + "]";
    }

    /**
     * @return the event description
     */
    @Override
    public final String getDescription()
    {
        return toString();
    }

    /**
     * @return the event name
     */
    @Override
    public final String getName()
    {
        return "FileChangeEvent";
    }

    /**
     * @return the event source
     */
    @Override
    public final Object getSource()
    {
        return FileChangeMonitor.EVENT_SOURCE;
    }

    /**
     * @return the event type
     */
    @Override
    public final String getType()
    {
        return EVT_TYPE;
    }
}
