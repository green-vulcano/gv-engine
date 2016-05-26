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
package it.greenvulcano.gvesb.internal.cache;

import it.greenvulcano.gvesb.internal.GVInternalException;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public class CacheException extends GVInternalException
{
    private static final long serialVersionUID = 5151484650528446980L;

    /**
     * @param idMessage
     */
    public CacheException(String idMessage)
    {
        super(idMessage);
    }

    /**
     * @param idMessage
     * @param params
     */
    public CacheException(String idMessage, String[][] params)
    {
        super(idMessage, params);
    }

    /**
     * @param idMessage
     * @param cause
     */
    public CacheException(String idMessage, Throwable cause)
    {
        super(idMessage, cause);
    }

    /**
     * @param idMessage
     * @param params
     * @param cause
     */
    public CacheException(String idMessage, String[][] params, Throwable cause)
    {
        super(idMessage, params, cause);
    }

}
