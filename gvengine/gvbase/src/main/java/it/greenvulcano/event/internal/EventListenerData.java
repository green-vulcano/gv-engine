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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * EventListenerData class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class EventListenerData
{
    /**
     * Map of interfaces -> class to listener.
     */
    HashMap<Class<?>, EventListenerInterfaceData> interfaces = new HashMap<Class<?>, EventListenerInterfaceData>();
    /**
     * The listener reference.
     */
    EventListener                                 evListener = null;

    /**
     * Constructor.
     *
     * @param evListener
     *        the associated event listener
     */
    public EventListenerData(EventListener evListener)
    {
        this.evListener = evListener;
    }

    /**
     * Add a new listener interface.
     *
     * @param elInterface
     *        the interface to add
     * @throws NoSuchMethodException
     *         if the interface is invalid
     */
    public final void addEventListenerInterface(Class<?> elInterface) throws NoSuchMethodException
    {
        getInterfaceData(elInterface, true);
    }

    /**
     * Add a list of listener interface.
     *
     * @param elInterfaceList
     *        the interface list to add
     * @throws NoSuchMethodException
     *         if the interface is invalid
     */
    public final void addEventListenerInterface(List<Class<?>> elInterfaceList) throws NoSuchMethodException
    {
        for (Class<?> clazz : elInterfaceList) {
            getInterfaceData(clazz, true);
        }
    }

    /**
     * Remove a listener interface.
     *
     * @param elInterface
     *        the interface to remove
     */
    public final void removeEventListenerInterface(Class<?> elInterface)
    {
        try {
            EventListenerInterfaceData elInterfaceData = getInterfaceData(elInterface, false);
            if (elInterfaceData != null) {
                elInterfaceData.destroy();
                interfaces.remove(elInterface);
            }
        }
        catch (NoSuchMethodException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Remove a list of listener interface.
     *
     * @param elInterfaceList
     *        the interface list to remove
     */
    public final void removeEventListenerInterface(List<Class<?>> elInterfaceList)
    {
        try {
            for (Class<?> clazz : elInterfaceList) {
                EventListenerInterfaceData elInterfaceData = getInterfaceData(clazz, false);
                if (elInterfaceData != null) {
                    elInterfaceData.destroy();
                    interfaces.remove(clazz);
                }
            }
        }
        catch (NoSuchMethodException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Add a new selector to a listener interface.
     *
     * @param elInterface
     *        the interface on which add the selector
     * @param selector
     *        the selector to add
     * @throws NoSuchMethodException
     *         if the interface is invalid
     */
    public final void addSelector(Class<?> elInterface, EventSelector selector) throws NoSuchMethodException
    {
        EventListenerInterfaceData elInterfaceData = getInterfaceData(elInterface, true);
        elInterfaceData.addSelector(selector);
    }

    /**
     * Add a new selector list to a listener interface.
     *
     * @param elInterface
     *        the interface on which add the selector
     * @param selectorList
     *        the selector list to add
     * @throws NoSuchMethodException
     *         if the interface is invalid
     */
    public final void addSelectors(Class<?> elInterface, List<EventSelector> selectorList) throws NoSuchMethodException
    {
        EventListenerInterfaceData elInterfaceData = getInterfaceData(elInterface, true);
        elInterfaceData.addSelectors(selectorList);
    }

    /**
     * Remove a selector from a listener interface.
     *
     * @param elInterface
     *        the interface from which remove the selector
     * @param selector
     *        the selector to remove
     */
    public final void removeSelector(Class<?> elInterface, EventSelector selector)
    {
        try {
            EventListenerInterfaceData elInterfaceData = getInterfaceData(elInterface, false);
            if (elInterfaceData != null) {
                boolean present = elInterfaceData.removeSelector(selector);
                if (present && elInterfaceData.getSelectorSet().isEmpty()) {
                    elInterfaceData.destroy();
                    interfaces.remove(elInterface);
                }
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Remove a selector list from a listener interface.
     *
     * @param elInterface
     *        the interface from which remove the selector
     * @param selectorList
     *        the selector list to remove
     */
    public final void removeSelectors(Class<?> elInterface, List<EventSelector> selectorList)
    {
        try {
            EventListenerInterfaceData elInterfaceData = getInterfaceData(elInterface, false);
            if (elInterfaceData != null) {
                boolean present = elInterfaceData.removeSelectors(selectorList);
                if (present && elInterfaceData.getSelectorSet().isEmpty()) {
                    elInterfaceData.destroy();
                    interfaces.remove(elInterface);
                }
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Add a event source to the given interface.
     *
     * @param elInterface
     *        the interface on which add the source
     * @param source
     *        the events source
     * @throws NoSuchMethodException
     *         if the interface is invalid
     */
    public final void addSource(Class<?> elInterface, Object source) throws NoSuchMethodException
    {
        EventListenerInterfaceData elInterfaceData = getInterfaceData(elInterface, true);
        elInterfaceData.addSource(source);
    }

    /**
     * Remove a event source from the given interface.
     *
     * @param elInterface
     *        the interface from which remove the source
     * @param source
     *        the events source
     */
    public final void removeSource(Class<?> elInterface, Object source)
    {
        try {
            EventListenerInterfaceData elInterfaceData = getInterfaceData(elInterface, false);
            if (elInterfaceData != null) {
                boolean present = elInterfaceData.removeSource(source);
                if (present && elInterfaceData.getSourceSet().isEmpty()) {
                    elInterfaceData.destroy();
                    interfaces.remove(elInterface);
                }
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Remove a event source from all interfaces.
     *
     * @param source
     *        the events source
     */
    public final void removeSource(Object source)
    {
        try {
            Iterator<Class<?>> i = interfaces.keySet().iterator();
            while (i.hasNext()) {
                Object elInterface = i.next();
                EventListenerInterfaceData elInterfaceData = interfaces.get(elInterface);
                boolean present = elInterfaceData.removeSource(source);
                if (present && elInterfaceData.getSourceSet().isEmpty()) {
                    elInterfaceData.destroy();
                    i.remove();
                }
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Fires a Event to the EventListener.
     *
     * @param methodName
     *        the listener method to invoke
     * @param event
     *        event to fire
     * @throws InvocationTargetException
     *         if error occurs
     * @throws IllegalAccessException
     *         if error occurs
     * @throws IllegalArgumentException
     *         if error occurs
     */
    public final void fireEventToListener(String methodName, Event event) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException
    {
        // make a copy to avoid ConcurrentModificationException
        HashMap<Class<?>, EventListenerInterfaceData> interfacesL = new HashMap<Class<?>, EventListenerInterfaceData>(
                interfaces);

        for (Entry<Class<?>, EventListenerInterfaceData> elInterface : interfacesL.entrySet()) {
            elInterface.getValue().fireEventToListener(evListener, methodName, event);
        }
    }

    /**
     * Check if the listener is valid.
     *
     * @return if true the instance can be destroyed
     */
    public final boolean canBeDestroyed()
    {
        return ((evListener == null) || interfaces.isEmpty());
    }

    /**
     * Perform cleanup operation.
     */
    public final void destroy()
    {
        for (Class<?> clazz : interfaces.keySet()) {
            EventListenerInterfaceData elInterfaceData = interfaces.get(clazz);
            elInterfaceData.destroy();
        }
        interfaces.clear();
        evListener = null;
    }

    /**
     * Create interface metadata.
     *
     * @param elInterface
     *        the interface class
     * @param create
     *        if true the InterfaceData can be create
     * @return the interface data instance
     * @throws NoSuchMethodException
     *         if error occurs
     */
    private EventListenerInterfaceData getInterfaceData(Class<?> elInterface, boolean create)
            throws NoSuchMethodException
    {
        EventListenerInterfaceData elInterfaceData = interfaces.get(elInterface);
        if ((elInterfaceData == null) && create) {
            elInterfaceData = new EventListenerInterfaceData(elInterface);
            interfaces.put(elInterface, elInterfaceData);
        }
        return elInterfaceData;
    }

    /**
     * Return a string representation of the instance.
     *
     * @return the string representation
     */
    @Override
    public final String toString()
    {
        String elInterface = "\nEventInterfaces :";
        for (Class<?> clazz : interfaces.keySet()) {
            elInterface += "\n" + interfaces.get(clazz);
        }
        return "--------------------\nEventListener: " + evListener + elInterface + "--------------------\n";
    }
}
