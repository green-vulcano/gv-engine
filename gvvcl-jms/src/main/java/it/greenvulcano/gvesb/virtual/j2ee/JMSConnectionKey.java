/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
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
 *******************************************************************************/
package it.greenvulcano.gvesb.virtual.j2ee;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This class is a key used to retrieve a JMS connection from the pool managed
 * by the JMSConnectionManager.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public class JMSConnectionKey
{
    private Hashtable<?, ?> properties = null;
    private String          connectionFactory;
    private boolean         isUsePooling;
    private boolean         invalidateOnReinsert;

    /**
     * @param properties
     * @param connectionFactory
     * @param isUsePooling
     * @param invalidateOnReinsert
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public JMSConnectionKey(Hashtable<?, ?> properties, String connectionFactory, boolean isUsePooling,
            boolean invalidateOnReinsert)
    {
        this.isUsePooling = isUsePooling;
        this.invalidateOnReinsert = invalidateOnReinsert;
        if (properties != null) {
            this.properties = new Hashtable(properties);
        }
        this.connectionFactory = connectionFactory;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        // Trivial cases
        //
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof JMSConnectionKey)) {
            return false;
        }

        JMSConnectionKey other = (JMSConnectionKey) obj;

        if (!other.connectionFactory.equals(connectionFactory)) {
            return false;
        }

        if (other.isUsePooling != isUsePooling) {
            return false;
        }

        // Complex case
        //
        Map<?, ?> otherProperties = other.properties;
        Map<?, ?> thisProperties = properties;

        if ((otherProperties == null) && (thisProperties == null)) {
            return true;
        }
        if ((otherProperties == null) && (thisProperties != null)) {
            return false;
        }
        if ((otherProperties != null) && (thisProperties == null)) {
            return false;
        }

        Set<?> otherKeys = otherProperties.keySet();
        Set<?> thisKeys = thisProperties.keySet();
        if (!otherKeys.equals(thisKeys)) {
            return false;
        }

        Iterator<?> i = thisKeys.iterator();
        while (i.hasNext()) {
            Object key = i.next();
            Object otherValue = otherProperties.get(key);
            Object thisValue = thisProperties.get(key);
            if (!otherValue.equals(thisValue)) {
                return false;
            }
        }

        return true;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        // For brevity we consider only the connectionFactory member
        // This is sufficient because usually different Map will refer
        // different connection factories
        return connectionFactory.hashCode();
    }

    /**
     * @return Returns the connectionFactory.
     */
    public String getConnectionFactory()
    {
        return connectionFactory;
    }

    /**
     * @return if using pooling
     */
    public boolean isUsingVCLPooling()
    {
        return isUsePooling;
    }

    /**
     * @return if must invalidate object after re-insert
     */
    public boolean isInvalidateOnReinsert()
    {
        return invalidateOnReinsert;
    }
}