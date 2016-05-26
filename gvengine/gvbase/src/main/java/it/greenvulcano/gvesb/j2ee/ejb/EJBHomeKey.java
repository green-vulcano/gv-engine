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
package it.greenvulcano.gvesb.j2ee.ejb;

import it.greenvulcano.gvesb.j2ee.JNDIHelper;

/**
 * EJBHomeKey class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class EJBHomeKey
{
    private String     jndiName;
    private JNDIHelper jndiHelper;

    /**
     * @param jndiName
     * @param jndiHelper
     */
    public EJBHomeKey(String jndiName, JNDIHelper jndiHelper)
    {
        this.jndiName = jndiName;
        this.jndiHelper = jndiHelper;
    }

    /**
     * @return the jndiHelper
     */
    public JNDIHelper getJndiHelper()
    {
        return jndiHelper;
    }

    /**
     * @return the jndiName
     */
    public String getJndiName()
    {
        return jndiName;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return hc(jndiName) ^ hc(jndiHelper);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof EJBHomeKey)) {
            return false;
        }

        EJBHomeKey other = (EJBHomeKey) obj;

        return eq(jndiName, other.jndiName) && eq(jndiHelper, other.jndiHelper);
    }

    private int hc(Object obj)
    {
        return obj == null ? 0 : obj.hashCode();
    }

    private boolean eq(Object a, Object b)
    {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}
