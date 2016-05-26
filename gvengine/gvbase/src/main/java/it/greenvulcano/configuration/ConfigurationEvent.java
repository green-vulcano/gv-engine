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
package it.greenvulcano.configuration;

import it.greenvulcano.event.interfaces.Event;

import java.net.URL;

/**
 * Signal a configuration change.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ConfigurationEvent implements Event
{
    /**
     * Indicate a file removing operation.
     */
    public static final long   EVT_FILE_REMOVED = 1;
    /**
     * Indicate a file loading operation.
     */
    public static final long   EVT_FILE_LOADED  = 2;
    /**
     * The configuration event type.
     */
    public static final String EVT_TYPE         = "it.greenvulcano.event.ConfigurationEvent";

    /**
     * The event code.
     */
    private long               code;
    /**
     * The file affected.
     */
    private String             file;
    /**
     * The file url.
     */
    private URL                url;

    /**
     * Constructor.
     *
     * @param code
     *        the event code
     * @param file
     *        the event file
     * @param url
     *        the file url
     */
    public ConfigurationEvent(long code, String file, URL url)
    {
        this.code = code;
        this.file = file;
        this.url = url;
    }

    /**
     * @return the event code
     */
    public final long getCode()
    {
        return code;
    }

    /**
     * @return the event file name
     */
    public final String getFile()
    {
        return file;
    }

    /**
     * @return the event file url
     */
    public final URL getURL()
    {
        return url;
    }

    /**
     * @return a string representation of the event
     */
    @Override
    public final String toString()
    {
        return "ConfigurationEvent[code=" + code + ", file=" + file + ", url=" + url + "]";
    }

    /**
     * @return the event description
     */
    public final String getDescription()
    {
        return toString();
    }

    /**
     * @return the event name
     */
    public final String getName()
    {
        return "ConfigurationEvent";
    }

    /**
     * @return the event source
     */
    public final Object getSource()
    {
        return XMLConfig.EVENT_SOURCE;
    }

    /**
     * @return the event type
     */
    public final String getType()
    {
        return EVT_TYPE;
    }
}
