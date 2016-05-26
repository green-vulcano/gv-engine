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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.greenvulcano.event;

import it.greenvulcano.event.interfaces.Event;
import it.greenvulcano.event.interfaces.EventListener;
import it.greenvulcano.event.interfaces.EventSelector;
import it.greenvulcano.event.internal.EventData;
import it.greenvulcano.event.internal.EventLauncher;
import it.greenvulcano.event.internal.EventListenerData;
import it.greenvulcano.event.internal.EventListenerHandler;
import it.greenvulcano.event.internal.EventToFireQueue;
import it.greenvulcano.event.internal.shutdown.ShutdownHandler;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
/**
 * EventHandler class.
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
*/
public final class EventHandler {
    /**
     * The EventLauncher thread instance.
     */
    private static EventLauncher eLauncher = null;

    static {
        System.out.println("EventHandler - Registering EventLauncher");
        eLauncher = new EventLauncher();
        eLauncher.setDaemon(true);
        eLauncher.start();

        try {
            System.out.println("EventHandler - Registering ShutdownHook");
            Runtime.getRuntime().addShutdownHook(new ShutdownHandler());
        }
        catch (Exception exc) {
            System.out.println("EventHandler - Unable to register ShutdownHook: " + exc);
            exc.printStackTrace();
        }
    }

    /**
     * Constructor.
     */
    private EventHandler() {
        // do nothing
    }

    /**
     * Add a EventListener.
     *
     * @param listener
     *            the Event listener
     * @param elInterface
     *            the listener interface
     * @param source
     *            the event source
     * @throws NoSuchMethodException
     *             if the interface is invalid
     */
    public static void addEventListener(EventListener listener, Class<?> elInterface, Object source)
            throws NoSuchMethodException {
        EventListenerHandler.addEventListener(listener, elInterface, source);
    }

    /**
     * Add a EventListener listening for events filtered by a selector.
     *
     * @param listener
     *            a <tt>EventListener</tt> object
     * @param elInterface
     *            the listener interface
     * @param selector
     *            a <tt>EventSelector</tt> filtering the events that must be
     *            notified to the given listener
     * @param source
     *            the event source
     * @throws NoSuchMethodException
     *             if the interface is invalid
     */
    public static void addEventListener(EventListener listener, Class<?> elInterface, EventSelector selector,
            Object source) throws NoSuchMethodException {
        EventListenerHandler.addEventListener(listener, elInterface, selector, source);
    }

    /**
     * Add a EventListener listening for events filtered by a list of selectors.
     *
     * @param listener
     *            a <tt>XMLConfigListener</tt> object
     * @param elInterface
     *            the listener interface
     * @param selectorList
     *            a <tt>List</tt> of <tt>EventSelector</tt> that must be
     *            associated to the given listener
     * @param source
     *            the event source
     * @throws NoSuchMethodException
     *             if the interface is invalid
     */
    public static void addEventListener(EventListener listener, Class<?> elInterface, List<EventSelector> selectorList,
            Object source) throws NoSuchMethodException {
        EventListenerHandler.addEventListener(listener, elInterface, selectorList, source);
    }

    /**
     * Remove a EventListener.
     *
     * @param listener
     *            the Event listener to remove
     * @param source
     *            the event source
     */
    public static void removeEventListener(EventListener listener, Object source) {
        EventListenerHandler.removeEventListener(listener, source);
    }

    /**
     * Remove a EventListener.
     *
     * @param listener
     *            the Event listener
     * @param elInterface
     *            the listener interface to remove
     * @param source
     *            the event source
     */
    public static void removeEventListener(EventListener listener, Class<?> elInterface, Object source) {
        EventListenerHandler.removeEventListener(listener, elInterface, source);
    }

    /**
     * Remove a EventListener listening for changes on a single event type.
     *
     * @param listener
     *            a <tt>EventListener</tt> object
     * @param elInterface
     *            the listener interface
     * @param selector
     *            a <tt>EventSelector</tt> that must be removed for the given
     *            listener
     * @param source
     *            the event source
     */
    public static void removeEventListener(EventListener listener, Class<?> elInterface, EventSelector selector,
            Object source) {
        EventListenerHandler.removeEventListener(listener, elInterface, selector, source);
    }

    /**
     * Remove a EventListener listening for events on a List of selectors.
     *
     * @param listener
     *            a <tt>EventListener</tt> object
     * @param elInterface
     *            the listener interface
     * @param selectorList
     *            a <tt>List</tt> of <tt>EventSelector</tt> that must be removed
     *            for the given listener
     * @param source
     *            the event source
     */
    public static void removeEventListener(EventListener listener, Class<?> elInterface,
            List<EventSelector> selectorList, Object source) {
        EventListenerHandler.removeEventListener(listener, elInterface, selectorList, source);
    }
    
    /**
     * Remove all EventListener listening for a given event type and source.
     *
     * @param elInterface
     *            the listener interface
     * @param source
     *            the event source
     */
    public static void removeAllEventListener(Class<?> elInterface, Object source) {
        EventListenerHandler.removeAllEventListener(elInterface, source);
    }

    /**
     * Fires a Event on asynchronous manner to all registered EventListener.
     *
     * @param methodName
     *            the listener method to invoke
     * @param event
     *            event to fire
     */
    public static void fireEvent(String methodName, Event event) {
        if (event.getSource() == null) {
            throw new InvalidParameterException("Invalid null source for event: " + event);
        }
        // insert event on queue
        EventToFireQueue.addEvent(new EventData(methodName, event));
    }

    /**
     * Fires a Event on synchronous manner to all registered EventListener.
     *
     * @param methodName
     *            the listener method to invoke
     * @param event
     *            event to fire
     * @throws Exception
     *             if error occurs
     */
    public static synchronized void fireEventSync(String methodName, Event event) throws Exception {
        if (event.getSource() == null) {
            throw new InvalidParameterException("Invalid null source for event: " + event);
        }
        Map<EventListener, EventListenerData> elListenersData = EventListenerHandler.getEventListeners();

        for (Entry<EventListener, EventListenerData> listener : elListenersData.entrySet()) {
            listener.getValue().fireEventToListener(methodName, event);
        }
    }

    /**
     * @return Returns the EventLauncher instance.
     */
    public static EventLauncher getEventLauncher() {
        return eLauncher;
    }
}
