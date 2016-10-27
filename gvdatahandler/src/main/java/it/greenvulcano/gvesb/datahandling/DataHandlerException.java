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
package it.greenvulcano.gvesb.datahandling;

import it.greenvulcano.gvesb.internal.GVInternalException;

/**
 *
 * DataHandlerException class
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DataHandlerException extends GVInternalException
{
    /**
     *
     */
    private static final long serialVersionUID = -1426399871691189590L;

    /**
     * @param messageId
     */
    public DataHandlerException(String messageId)
    {
        super(messageId);
    }

    /**
     * @param messageId
     * @param cause
     */
    public DataHandlerException(String messageId, Throwable cause)
    {
        super(messageId, cause);
    }

}
