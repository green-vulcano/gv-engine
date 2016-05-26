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
package it.greenvulcano.event.util.shutdown;

import it.greenvulcano.event.interfaces.Event;

/**
 * ShutdownEvent class
 *
 * @version 3.0.0 Feb 17, 2010
 *
 *
 */
public class ShutdownEvent implements Event
{
    /**
     * The event type.
     */
    static final String TYPE        = "it.greenvulcano.event.Shutdown";
    /**
     * The event description.
     */
    static final String DESCRIPTION = "JVM shutdown event";
    /**
     * The event source.
     */
    private Object      source      = null;

    /**
     * Constructor.
     */
    public ShutdownEvent()
    {
        source = ShutdownEventListener.SOURCE;
    }

    /**
     * Constuctor.
     *
     * @param source
     *        the event source
     */
    public ShutdownEvent(Object source)
    {
        this.source = source;
    }

    /**
     * @return the event name
     */
    public final String getName()
    {
        return "Shutdown";
    }

    /**
     * @return the event type
     */
    public final String getType()
    {
        return TYPE;
    }

    /**
     * @return the event code
     */
    public final long getCode()
    {
        return 0;
    }

    /**
     * @return the event source
     */
    public final Object getSource()
    {
        return source;
    }

    /**
     * @return the event description
     */
    public final String getDescription()
    {
        return DESCRIPTION;
    }

    /**
     * @return a string representation of the event
     */
    @Override
    public final String toString()
    {
        return TYPE + " - " + DESCRIPTION;
    }
}
