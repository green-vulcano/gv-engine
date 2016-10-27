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
package it.greenvulcano.gvesb.adapter.http.formatters.handlers;

import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpException;

/**
 * <code>ParameterHandlerFactoryException</code> is thrown by inbound
 * <i>AdapterHttp</i> module methods on errors occurred while using
 * <code>InterfaceParametersHandlerFactory</code> class.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ParameterHandlerFactoryException extends AdapterHttpException
{
    private static final long serialVersionUID = -5774150083567349963L;

    /**
     * Creates a new <code>ParameterHandlerFactoryException</code> with error
     * code identified by <code>errorId</code> and no cause.
     *
     * @param id
     *        ErrorId associated to the exception
     */
    public ParameterHandlerFactoryException(String id)
    {
        super(id);
    }

    /**
     * Creates a new <code>ParameterHandlerFactoryException</code> with error
     * code identified by <code>id</code> and a cause.
     *
     * @param id
     *        ErrorId associated to the exception
     * @param cause
     *        Throwable that caused this exception to get thrown
     */
    public ParameterHandlerFactoryException(String id, Throwable cause)
    {
        super(id, cause);
    }

    /**
     * Creates a new <code>ParameterHandlerFactoryException</code> with error
     * code identified by <code>id</code> and no cause. <code>params</code> is
     * used to complete the error message.
     *
     * @param id
     *        ErrorId associated to the exception
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     */
    public ParameterHandlerFactoryException(String id, String[][] params)
    {
        super(id, params);
    }

    /**
     * Creates a new <code>ParameterHandlerFactoryException</code> with a cause.
     *
     * @param id
     *        ErrorId associated to the exception
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     * @param cause
     *        Throwable that caused this exception to get thrown
     */
    public ParameterHandlerFactoryException(String id, String[][] params, Throwable cause)
    {
        super(id, params, cause);
    }
}
