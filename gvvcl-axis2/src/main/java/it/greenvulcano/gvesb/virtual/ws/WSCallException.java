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
package it.greenvulcano.gvesb.virtual.ws;

import it.greenvulcano.gvesb.virtual.CallException;

/**
 * The call exception object.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class WSCallException extends CallException
{

    private static final long serialVersionUID = 300L;

    /**
     * Creates a new WSCallException with error code identified by
     * <code>errorId</code> and no cause.
     *
     * @param errorId
     *        ErrorId associated to the exception
     */
    public WSCallException(String errorId)
    {
        super(errorId);
    }

    /**
     * Creates a new WSCallException with error code identified by
     * <code>errorId</code> and no cause. <code>params</code> is used to
     * complete the error message.
     *
     * @param errorId
     *        ErrorId associated to the exception
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     */
    public WSCallException(String errorId, String[][] params)
    {
        super(errorId, params);
    }

    /**
     * Creates a new WSCallException with error code identified by
     * <code>errorId</code> and a cause.
     *
     * @param errorId
     *        WSCallException associated to the exception
     * @param exc
     *        Throwable that caused this exception to get thrown
     */
    public WSCallException(String errorId, Throwable exc)
    {
        super(errorId, exc);
    }

    /**
     * Creates a new WSCallException with a cause.
     *
     * @param errorId
     *        error code of the exception
     * @param params
     *        message associated to the exception
     * @param exc
     *        Throwable that caused this exception to get thrown
     */
    public WSCallException(String errorId, String[][] params, Throwable exc)
    {
        super(errorId, params, exc);
    }
}