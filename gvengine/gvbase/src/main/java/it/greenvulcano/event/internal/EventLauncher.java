/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project.
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
package it.greenvulcano.event.internal;

import it.greenvulcano.event.interfaces.EventListener;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.thread.BaseThread;
import it.greenvulcano.util.thread.ThreadMap;

import java.util.Map;
import java.util.Map.Entry;

/**
 * EventLauncher class
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class EventLauncher extends BaseThread
{
    /**
     * If true the thread exit.
     */
    private boolean terminate = false;

    public EventLauncher()
    {
        super("EventLauncher");
    }

    /**
     * Stop the thread.
     * 
     */
    public final void stopThread()
    {
        terminate = true;
    }

    /**
     * Launch the queued Events.
     */
    @Override
    public final void run()
    {
        while (!terminate) {
            EventData eventD = EventToFireQueue.getEvent();
            while (eventD != null) {
                processEvent(eventD);
                eventD = EventToFireQueue.getEvent();
            }
        }
    }

    /**
     * Send the given event to listeners.
     * 
     * @param eventD
     *        the event to send
     */
    private void processEvent(EventData eventD)
    {
        NMDC.push();
        NMDC.clear();
        NMDC.setServer(JMXEntryPoint.getServerName());
        try {
            Map<EventListener, EventListenerData> elListenersData = EventListenerHandler.getEventListeners();
            EventListenerData elData = null;

            for (Entry<EventListener, EventListenerData> listener : elListenersData.entrySet()) {
                NMDC.push();
                try {
                    listener.getValue().fireEventToListener(eventD.methodName, eventD.event);
                }
                catch (Exception exc) {
                    System.out.println("EventLauncher.fireEvents() - Event handling error");
                    System.out.println("EventLauncher.fireEvents() - destination: " + elData);
                    System.out.println("EventLauncher.fireEvents() - event: " + eventD.event);
                    exc.printStackTrace();
                }
                finally {
                    NMDC.pop();
                }
            }
        }
        finally {
            NMDC.pop();
            ThreadMap.clean();
        }
    }
}
