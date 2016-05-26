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
package it.greenvulcano.event.interfaces;

/**
*
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
*/
public interface EventSelector {
    /**
     * Return true if the given Event match the selection criteria.
     *
     * @param event
     *            the Event to check
     * @return true if the event is valid
     */
    boolean select(Event event);

    /**
     * Return true if the objects are equals.
     *
     * @param obj
     *            the Object to compare
     * @return true if are equals
     */
    boolean equals(Object obj);

    /**
     * Return HashCode for the instance.
     *
     * @return the HashCode
     */
    int hashCode();
}
