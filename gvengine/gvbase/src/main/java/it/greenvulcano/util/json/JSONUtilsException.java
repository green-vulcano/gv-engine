/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.util.json;

/**
 * Exception raised by JSONUtils.
 *
 * @version 3.5.0 29/ago/2014
 * @author GreenVulcano Developer Team
 */
public class JSONUtilsException extends Exception
{
    private static final long serialVersionUID = -3803211630950181854L;

    /**
     * Builds a JSONUtilsException with a simple message
     *
     * @param message
     */
    public JSONUtilsException(String message)
    {
        super(message);
    }

    /**
     * Builds a JSONUtilsException with an error description and a cause
     * exception
     *
     * @param message
     * @param cause
     */
    public JSONUtilsException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
