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

import it.greenvulcano.event.interfaces.Event;
import it.greenvulcano.event.interfaces.EventListener;
import it.greenvulcano.event.interfaces.EventSelector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * EventListenerInterfaceData class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class EventListenerInterfaceData
{
    /**
     * Default event listener method name.
     */
    private static final String     DEFAULT_METHOD_NAME = "onEvent";
    /**
     * Listener interface class.
     */
    private Class<?>                elInterface         = null;
    /**
     * Set of event source on which this listener is registered.
     */
    private Set<Object>             sourceSet           = new HashSet<Object>();
    /**
     * Set of event selectors.
     */
    private Set<EventSelector>      selectorSet         = new HashSet<EventSelector>();
    /**
     * Map from method names -> Method instances.
     */
    private HashMap<String, Method> methodNameToMethod  = new HashMap<String, Method>();

    /**
     * Constructor.
     *
     * @param elInterface
     *        the listener interface
     * @throws NoSuchMethodException
     *         if the interface isn't valid
     */
    public EventListenerInterfaceData(Class<?> elInterface) throws NoSuchMethodException
    {
        this.elInterface = elInterface;
        extractMethods();
    }

    /**
     * Extract the appropriate methods from interface.
     *
     * @throws NoSuchMethodException
     *         if the interface is invalid
     */
    private void extractMethods() throws NoSuchMethodException
    {
        Method[] mts = elInterface.getDeclaredMethods();

        for (Method mt : mts) {
            Class<?>[] prs = mt.getParameterTypes();
            if ((prs.length != 1) || !Event.class.isAssignableFrom(prs[0])) {
                throw new NoSuchMethodException("Not found suitable methods in Interface " + elInterface.getName());
            }
            methodNameToMethod.put(mt.getName(), mt);
        }
    }

    /**
     * Add an event selector.
     *
     * @param selector
     *        the selector to add
     */
    public final void addSelector(EventSelector selector)
    {
        selectorSet.add(selector);
    }

    /**
     * Add an event selector list.
     *
     * @param selectorList
     *        the selector list to add
     */
    public final void addSelectors(List<EventSelector> selectorList)
    {
        for (EventSelector selector : selectorList) {
            selectorSet.add(selector);
        }
    }

    /**
     * Remove an event selector.
     *
     * @param selector
     *        the selector to remove
     * @return true if the selector have been canceled
     */
    public final boolean removeSelector(EventSelector selector)
    {
        if (selector != null) {
            return selectorSet.remove(selector);
        }
        return false;
    }

    /**
     * Remove an event selector list.
     *
     * @param selectorList
     *        the selector list to remove
     * @return true if the selector have been canceled
     */
    public final boolean removeSelectors(List<EventSelector> selectorList)
    {
        boolean present = false;
        for (EventSelector selector : selectorList) {
            if (selectorSet.remove(selector)) {
                present = true;
            }
        }
        return present;
    }

    /**
     * @return the selectors set
     */
    public final Set<EventSelector> getSelectorSet()
    {
        return selectorSet;
    }

    /**
     * Add an event source.
     *
     * @param source
     *        the events source
     */
    public final void addSource(Object source)
    {
        if (source != null) {
            sourceSet.add(source);
        }
    }

    /**
     * Remove an event source.
     *
     * @param source
     *        the events source
     * @return true if the source have been canceled
     */
    public final boolean removeSource(Object source)
    {
        if (source != null) {
            return sourceSet.remove(source);
        }
        return false;
    }

    /**
     * @return the list of source on which the listener is registered
     */
    public final Set<Object> getSourceSet()
    {
        return sourceSet;
    }

    /**
     * Check if a given event must be dispatched to the listener.
     *
     * @param event
     *        the event to check
     * @return true if the event must be dispatched
     */
    private boolean select(Event event)
    {
        boolean selected = sourceSet.isEmpty() || (!sourceSet.isEmpty() && sourceSet.contains(event.getSource()));

        if (!selectorSet.isEmpty()) {
            selected = false;

            Iterator<EventSelector> i = selectorSet.iterator();
            while ((i.hasNext()) && !selected) {
                EventSelector selector = i.next();
                selected = selector.select(event);
            }
        }

        return selected;
    }

    /**
     * Fires a Event to the given EventListener.
     *
     * @param listener
     *        the event listener
     * @param methodName
     *        the interface method to invoke
     * @param event
     *        event to fire
     * @throws InvocationTargetException
     *         if error occurs
     * @throws IllegalAccessException
     *         if error occurs
     * @throws IllegalArgumentException
     *         if error occurs
     */
    final void fireEventToListener(EventListener listener, String methodName, Event event)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        if (select(event)) {
            Method mt = methodNameToMethod.get(methodName);
            if (mt == null) {
                mt = methodNameToMethod.get(DEFAULT_METHOD_NAME);
                if (mt == null) {
                    return;
                }
            }
            mt.invoke(listener, new Object[]{event});
        }
    }

    /**
     * Check if the listener interface is valid.
     *
     * @return if true the instance can be destroyed
     */
    public final boolean canBeDestroyed()
    {
        return ((elInterface == null) || methodNameToMethod.isEmpty());
    }

    /**
     * Perform cleanup operation.
     */
    public final void destroy()
    {
        sourceSet.clear();
        selectorSet.clear();
        methodNameToMethod.clear();
    }

    /**
     * Return true if the objects are equals.
     *
     * @param obj
     *        the Object to compare
     * @return true if are equals
     */
    @Override
    public final boolean equals(Object obj)
    {
        if (obj instanceof EventListenerInterfaceData) {
            return elInterface.equals(((EventListenerInterfaceData) obj).elInterface);
        }
        return false;
    }

    /**
     * Return HashCode for the instance.
     *
     * @return the HashCode
     */
    @Override
    public final int hashCode()
    {
        return elInterface.hashCode();
    }

    /**
     * @return a string representation of the event
     */
    @Override
    public final String toString()
    {
        String eSource = "EventSources:";
        for (Object source : sourceSet) {
            eSource += " " + source;
        }
        String eSelector = "EventSelectors:";
        for (EventSelector selector : selectorSet) {
            eSelector += " " + selector;
        }
        return "----------\nEventListenerInterface: " + elInterface.getName() + "\n" + eSource + "\n" + eSelector
                + "\n----------\n";
    }
}
