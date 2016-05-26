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
package it.greenvulcano.event.internal;

import java.util.Vector;

/**
 * EventToFireQueue class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public final class EventToFireQueue
{
    /**
     * Wait timeout.
     */
    private static final long        WAIT   = 30000;
    /**
     * The Even queue.
     */
    private static Vector<EventData> events = new Vector<EventData>();

    /**
     * Constructor.
     */
    private EventToFireQueue()
    {
        // do nothing
    }

    /**
     * Enqueue the EventData.
     *
     * @param eventD
     *        the event to enqueue
     */
    public static void addEvent(EventData eventD)
    {
        synchronized (events) {
            events.add(eventD);
            events.notifyAll();
        }
    }

    /**
     * Return the first EventData in the list, waiting for 30 seconds if empty.
     *
     * @return the firs EventData
     */
    public static EventData getEvent()
    {
        if (events.isEmpty()) {
            synchronized (events) {
                try {
                    events.wait(WAIT);
                }
                catch (Exception exc) {
                    // do nothing
                    System.out.println("\ngetEvent failed - " + exc);
                    exc.printStackTrace();
                }
            }
        }
        EventData eventD = null;
        synchronized (events) {
            if (!events.isEmpty()) {
                eventD = events.remove(0);
            }
        }
        return eventD;
    }

    /**
     * Return true if the queue is empty.
     *
     * @return the queue status
     */
    public static boolean isEmpty()
    {
        return events.isEmpty();
    }
}
