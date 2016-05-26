/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
package it.greenvulcano.util.ldap;

/**
 * @version 3.2.0 25/set/2011
 * @author GreenVulcano Developer Team
 */
public class LDAPUtilsException extends Exception
{
    /**
     *
     */
    private static final long serialVersionUID = -573009539922226252L;

    /**
     * @param message
     */
    public LDAPUtilsException(String message)
    {
        super(message);
    }

    /**
     * @param cause
     */
    public LDAPUtilsException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public LDAPUtilsException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
