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
package it.greenvulcano.gvesb.virtual;

import it.greenvulcano.gvesb.buffer.GVException;

import org.w3c.dom.Node;

/**
 * <code>OperationKey</code> class is used from <code>OperationManager</code> in
 * order to maintains references to cached <code>Operation</code>s.
 * <p>
 * The <code>OperationKey</code> is an abstract class.<br>
 * The concrete implementation must provide methods that calculates a Node where
 * the operation configuration is stored. Therefore the
 * <code>OperationKey</code> must provide a String as key that prevent the
 * calculus of the node if not needed.
 * <p>
 * Because we need to use the encapsulated components as key for the map, and
 * not just the <code>OperationKey</code> itself, we overwrite
 * <code>hashCode()</code> and <code>equals()</code> methods.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public abstract class OperationKey
{
    /**
     * Return the key for the cache.
     *
     * @return the internal key
     */
    public abstract String getKey();

    /**
     * Return the configuration file. This information is used on configuration
     * reloading.
     *
     * @return the file name
     */
    public abstract String getFile();

    /**
     * @return the configuration node.
     * @exception GVException
     *            if errors occurs
     */
    public abstract Node getNode() throws GVException;

    /**
     * Calculates the hash code.
     *
     * @return the instance hashcode
     */
    @Override
    public int hashCode()
    {

        return getKey().hashCode();
    }

    /**
     * Equality.
     *
     * @param obj
     *        the object to compare
     * @return the compare result
     */
    @Override
    public boolean equals(Object obj)
    {

        // Fast check for simple cases
        //
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof OperationKey)) {
            return false;
        }

        // Equality with another OperationKey

        OperationKey other = (OperationKey) obj;

        return getKey().equals(other.getKey());
    }

    /**
     * Printable string for OperationKey.
     *
     * @return a string representation of the instance
     */
    @Override
    public String toString()
    {
        return "OperationKey[key=" + getKey() + "]";
    }
}
