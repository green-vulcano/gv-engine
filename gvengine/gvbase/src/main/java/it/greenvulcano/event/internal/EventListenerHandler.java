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
package it.greenvulcano.event.internal;

import it.greenvulcano.event.interfaces.EventListener;
import it.greenvulcano.event.interfaces.EventSelector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * EventListenerHandler class
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
*/
public final class EventListenerHandler {
    /**
     * An <tt>HashMap</tt> mapping a listener to a <tt>EventListenerData</tt>.
     */
    private static Map<EventListener, EventListenerData> listenersData = new HashMap<EventListener, EventListenerData>();

    /**
     * Constructor.
     */
    private EventListenerHandler() {
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
     *             if erro occurs
     */
    public static synchronized void addEventListener(EventListener listener, Class<?> elInterface, Object source)
            throws NoSuchMethodException {
        EventListenerData elData = getEventListenerData(listener, true);
        elData.addEventListenerInterface(elInterface);
        elData.addSource(elInterface, source);
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
     *             if erro occurs
     */
    public static synchronized void addEventListener(EventListener listener, Class<?> elInterface,
            EventSelector selector, Object source) throws NoSuchMethodException {
        EventListenerData elData = getEventListenerData(listener, true);
        elData.addSource(elInterface, source);
        elData.addSelector(elInterface, selector);
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
     *             if erro occurs
     */
    public static synchronized void addEventListener(EventListener listener, Class<?> elInterface,
            List<EventSelector> selectorList, Object source) throws NoSuchMethodException {
        EventListenerData elData = getEventListenerData(listener, true);
        elData.addSource(elInterface, source);
        elData.addSelectors(elInterface, selectorList);
    }

    /**
     * Remove a EventListener from a given source.
     *
     * @param listener
     *            the Event listener to remove
     * @param source
     *            the event source
     */
    public static synchronized void removeEventListener(EventListener listener, Object source) {
        EventListenerData elData = getEventListenerData(listener, false);
        if (elData != null) {
            if (source != null) {
                elData.removeSource(source);
                if (elData.canBeDestroyed()) {
                    elData.destroy();
                    listenersData.remove(listener);
                }
            }
            else {
                elData.destroy();
                listenersData.remove(listener);
            }
        }
    }

    /**
     * Remove a EventListener from the given source.
     *
     * @param listener
     *            the Event listener
     * @param elInterface
     *            the listener interface to remove
     * @param source
     *            the event source
     */
    public static synchronized void removeEventListener(EventListener listener, Class<?> elInterface, Object source) {
        EventListenerData elData = getEventListenerData(listener, false);
        if (elData != null) {
            if (source != null) {
                elData.removeSource(elInterface, source);
            }
            else {
                elData.removeEventListenerInterface(elInterface);
            }
            if (elData.canBeDestroyed()) {
                elData.destroy();
                listenersData.remove(listener);
            }
        }
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
    public static synchronized void removeEventListener(EventListener listener, Class<?> elInterface,
            EventSelector selector, Object source) {
        EventListenerData elData = getEventListenerData(listener, false);
        if (elData != null) {
            if (selector != null) {
                elData.removeSelector(elInterface, selector);
            }
            else {
                elData.removeEventListenerInterface(elInterface);
            }
            if (elData.canBeDestroyed()) {
                elData.destroy();
                listenersData.remove(listener);
            }
        }
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
    public static synchronized void removeEventListener(EventListener listener, Class<?> elInterface,
            List<EventSelector> selectorList, Object source) {
        EventListenerData elData = getEventListenerData(listener, false);
        if (elData != null) {
            if (selectorList != null) {
                elData.removeSelectors(elInterface, selectorList);
            }
            else {
                elData.removeEventListenerInterface(elInterface);
            }
            if (elData.canBeDestroyed()) {
                elData.destroy();
                listenersData.remove(listener);
            }
        }
    }

    /**
     * Remove all EventListener listening for changes on a single event type ad source.
     *
     * @param elInterface
     *            the listener interface
     * @param source
     *            the event source
     */
    public static synchronized void removeAllEventListener(Class<?> elInterface, Object source) {
        Iterator<Entry<EventListener, EventListenerData>> elEntryIt = listenersData.entrySet().iterator();
        while (elEntryIt.hasNext()) {
            Entry<EventListener, EventListenerData> elEntry = elEntryIt.next();
            EventListenerData elData = elEntry.getValue();
            if (elData != null) {
                elData.removeEventListenerInterface(elInterface);
                elData.removeSource(source);
                if (elData.canBeDestroyed()) {
                    elData.destroy();
                    elEntryIt.remove();
                }
            }
        }
    }

    /**
     * Make a copy of the map to avoid ConcurrentModificationException.
     *
     * @return a copy of the listener map
     */
    public static synchronized Map<EventListener, EventListenerData> getEventListeners() {
        return new HashMap<EventListener, EventListenerData>(listenersData);
    }

    /**
     * Return the EventListenerData configured for the given listener.
     *
     * @param listener
     *            to search
     * @param create
     *            if true the InterfaceData can be create
     * @return the configured EventListenerData
     */
    private static synchronized EventListenerData getEventListenerData(EventListener listener, boolean create) {
        EventListenerData elData = listenersData.get(listener);
        if ((elData == null) && create) {
            elData = new EventListenerData(listener);
            listenersData.put(listener, elData);
        }
        return elData;
    }

    /**
     * Make a dump of the registered listener.
     *
     * @return a dump of the listeners
     */
    public static String dump() {
        StringBuilder sbuffer = new StringBuilder();
        sbuffer.append("EventListenerHandler:\n");
        for (EventListener listener : listenersData.keySet()) {
            EventListenerData elData = listenersData.get(listener);
            sbuffer.append(elData + "\n");
        }
        return sbuffer.toString();
    }
}
