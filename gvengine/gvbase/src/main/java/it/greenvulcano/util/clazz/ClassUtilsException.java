/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.util.clazz;

/**
 * Exception raised by ClassUtils.
 * 
 * 
 * @version 3.2.0 Gen 31, 2012
 * @author GreenVulcano Developer Team
 */
public class ClassUtilsException extends Exception
{
    private static final long serialVersionUID = -3803210533350144854L;

    /**
     * Builds a ClassUtilsException with a simple message
     * 
     * @param sErrorMessage
     */
    public ClassUtilsException(String sErrorMessage)
    {
        super(sErrorMessage);
    }

    /**
     * Builds a ClassUtilsException with an error description and a nested
     * exception
     * 
     * @param sErrorMessage
     * @param ne
     */
    public ClassUtilsException(String sErrorMessage, Throwable ne)
    {
        super(sErrorMessage, ne);
    }

}
