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
package it.greenvulcano.gvesb.ws.wsdl;

import it.greenvulcano.gvesb.internal.GVInternalException;

/**
 * The WSDL loader exception object.
 *
 * @version 3.0.0 Apr 1, 2010
 * @author nunzio
 *
 *
 */
public class WSDLLoaderException extends GVInternalException
{

    private static final long serialVersionUID = 300L;

    /**
     * Creates a new WSDLLoaderException with error code identified by
     * <code>errorId</code> and no cause.
     *
     * @param errorId
     *        ErrorId associated to the exception
     */
    public WSDLLoaderException(String errorId)
    {
        super(errorId);
    }

    /**
     * Creates a new WSDLLoaderException with error code identified by
     * <code>errorId</code> and no cause. <code>params</code> is used to
     * complete the error message.
     *
     * @param errorId
     *        ErrorId associated to the exception
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     */
    public WSDLLoaderException(String errorId, String[][] params)
    {
        super(errorId, params);
    }

    /**
     * Creates a new WSDLLoaderException with error code identified by
     * <code>errorId</code> and a cause.
     *
     * @param errorId
     *        WSCallException associated to the exception
     * @param exc
     *        Throwable that caused this exception to get thrown
     */
    public WSDLLoaderException(String errorId, Throwable exc)
    {
        super(errorId, exc);
    }

    /**
     * Creates a new WSDLLoaderException with a cause.
     *
     * @param errorId
     *        error code of the exception
     * @param params
     *        message associated to the exception
     * @param exc
     *        Throwable that caused this exception to get thrown
     */
    public WSDLLoaderException(String errorId, String[][] params, Throwable exc)
    {
        super(errorId, params, exc);
    }
}
