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
package it.greenvulcano.gvesb.virtual.datahandler;

import it.greenvulcano.gvesb.virtual.CallException;

/**
 * @version 3.0.0 Mar 31, 2010
 * @author nunzio
 *
 *
 */
public class DataHandlerCallException extends CallException
{

    private static final long serialVersionUID = 300L;

    /**
     * @param errorId
     * @param params
     * @param exc
     */
    public DataHandlerCallException(String errorId, String[][] params, Throwable exc)
    {
        super(errorId, params, exc);
    }

    /**
     * @param errorId
     * @param params
     */
    public DataHandlerCallException(String errorId, String[][] params)
    {
        super(errorId, params);
    }

    /**
     * @param errorId
     * @param exc
     */
    public DataHandlerCallException(String errorId, Throwable exc)
    {
        super(errorId, exc);
    }

    /**
     * @param errorId
     */
    public DataHandlerCallException(String errorId)
    {
        super(errorId);
    }

}
