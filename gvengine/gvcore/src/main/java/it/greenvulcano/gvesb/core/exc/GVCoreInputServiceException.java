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
package it.greenvulcano.gvesb.core.exc;

/**
 * <code>GVCoreDisabledServiceException</code> is the exception raised by the
 * GreenVulcano core components in case of errors.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public class GVCoreInputServiceException extends GVCoreException
{
    private static final long serialVersionUID = -8351141651849381405L;

    /**
     * Creates a new GVCoreInputServiceException with error code identified by
     * <code>errorId</code> and no cause.
     *
     * @param id
     *        message associated to an error in the error catalog
     */
    public GVCoreInputServiceException(String id)
    {
        super(id);
    }

    /**
     * Creates a new GVCoreInputServiceException with error code identified by
     * <code>errorId</code> and no cause. <code>params</code> is used to
     * complete the error message.
     *
     * @param id
     *        message associated to an error in the error catalog
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     */
    public GVCoreInputServiceException(String id, String[][] params)
    {
        super(id, params);
    }

    /**
     * Creates a new GVCoreCallSvcException with error code identified by
     * <code>errorId</code> and a cause.
     *
     * @param idMessage
     *        message associated to the exception
     * @param cause
     *        Throwable that caused this exception to get thrown
     */
    public GVCoreInputServiceException(String id, Throwable cause)
    {
        super(id, cause);
    }

    /**
     * Creates a new GVCoreInputServiceException with a cause.
     *
     * @param id
     *        message associated to the exception
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     * @param cause
     *        Throwable that caused this exception to get thrown
     */
    public GVCoreInputServiceException(String id, String[][] params, Throwable cause)
    {
        super(id, params, cause);
    }
}
