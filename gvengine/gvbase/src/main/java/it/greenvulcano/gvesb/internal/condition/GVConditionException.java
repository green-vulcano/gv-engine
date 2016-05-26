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
package it.greenvulcano.gvesb.internal.condition;

import it.greenvulcano.gvesb.internal.GVInternalException;

/**
 * <code>GVConditionException</code> is the exception raised by the GreenVulcano
 * condition check mechanisms in case of errors.
 * It is possible for <code>GVConditionException</code> to encapsulate other
 * exceptions.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVConditionException extends GVInternalException
{
    private static final long serialVersionUID = -789608768612633293L;

    /**
     * Creates a new GVConditionException with error code identified by
     * <code>errorId</code> and no cause.
     *
     * @param idMessage
     *        message associated to an error in the error catalog
     */
    public GVConditionException(String idMessage)
    {
        super(idMessage);
    }

    /**
     * Creates a new GVConditionException with error code identified by
     * <code>errorId</code> and no cause. <code>params</code> is used to
     * complete the error message.
     *
     * @param idMessage
     *        message associated to an error in the error catalog
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     */
    public GVConditionException(String idMessage, String[][] params)
    {
        super(idMessage, params);
    }

    /**
     * Creates a new GVConditionException with error code identified by
     * <code>errorId</code> and a cause.
     *
     * @param idMessage
     *        message associated to the exception
     * @param cause
     *        Throwable that caused this exception to get thrown
     */
    public GVConditionException(String idMessage, Throwable cause)
    {
        super(idMessage, cause);
    }

    /**
     * Creates a new GVConditionException with a cause.
     *
     * @param idMessage
     *        message associated to the exception
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     * @param cause
     *        Throwable that caused this exception to get thrown
     */
    public GVConditionException(String idMessage, String[][] params, Throwable cause)
    {
        super(idMessage, params, cause);
    }
}
