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
package it.greenvulcano.event.util;

import it.greenvulcano.event.interfaces.Event;
import it.greenvulcano.event.interfaces.EventSelector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 *
 * DefaultEventSelector class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 **/
public class DefaultEventSelector implements EventSelector
{
    /**
     * Event tpes on which filter events.
     */
    HashSet<String> typesSet = new HashSet<String>();

    /**
     * Constructor. Create an empty filter set.
     */
    public DefaultEventSelector()
    {
        // do nothing
    }

    /**
     * Constructor.
     *
     * @param type
     *        insert type in the filter set.
     */
    public DefaultEventSelector(String type)
    {
        typesSet.add(type);
    }

    /**
     * Add the given type to the filter set.
     *
     * @param type
     *        the type to add.
     */
    public final void addType(String type)
    {
        typesSet.add(type);
    }

    /**
     * Remove the given type from the filter set.
     *
     * @param type
     *        the type to remove.
     */
    public final void removeType(String type)
    {
        typesSet.remove(type);
    }

    /**
     * Add the given list of types to the filter set.
     *
     * @param types
     *        the types to add.
     */
    public final void addTypes(List<String> types)
    {
        Iterator<String> i = types.iterator();
        while (i.hasNext()) {
            typesSet.add(i.next());
        }
    }

    /**
     * Remove the given list of types from the filter set.
     *
     * @param types
     *        the types to remove.
     */
    public final void removeTypes(List<String> types)
    {
        Iterator<String> i = types.iterator();
        while (i.hasNext()) {
            typesSet.remove(i.next());
        }
    }

    /**
     * @see it.greenvulcano.event.interfaces.EventSelector#select(Event)
     */
    public final boolean select(Event event)
    {
        boolean selected = true;

        if (!typesSet.isEmpty()) {
            selected = false;
            String type = event.getType();

            Iterator<String> i = typesSet.iterator();
            while (i.hasNext() && !selected) {
                selected = type.equals(i.next());
            }
        }

        return selected;
    }

    /**
     * Implements equality check.
     *
     * @param obj
     *        the object to compare
     * @return true if the instances are equals
     */
    @Override
    public final boolean equals(Object obj)
    {
        if (obj instanceof DefaultEventSelector) {
            return typesSet.equals(((DefaultEventSelector) obj).typesSet);
        }
        return false;
    }

    /**
     * Implements hash code generation algorithm.
     *
     * @return the hash code
     */
    @Override
    public final int hashCode()
    {
        return typesSet.hashCode();
    }
}
