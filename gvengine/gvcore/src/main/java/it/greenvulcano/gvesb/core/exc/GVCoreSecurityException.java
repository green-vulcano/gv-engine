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
package it.greenvulcano.gvesb.core.exc;

/**
 * <code>GVCoreSecurityException</code> is the exception raised by the
 * GreenVulcano core components in case of authorization errors.
 * 
 * @version 3.2.0 Feb 04, 2012
 * @author GreenVulcano Developer Team
 */

public class GVCoreSecurityException extends GVCoreException
{
    private static final long serialVersionUID = 2675771334455093890L;

    /**
     * Creates a new GVCoreSecurityException with error code identified by
     * <code>errorId</code> and no cause.
     * 
     * @param id
     *        message associated to an error in the error catalog
     */
    public GVCoreSecurityException(String id)
    {
        super(id);
    }

    /**
     * Creates a new GVCoreSecurityException with error code identified by
     * <code>errorId</code> and no cause. <code>params</code> is used to
     * complete the error message.
     * 
     * @param id
     *        message associated to an error in the error catalog
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     */
    public GVCoreSecurityException(String id, String[][] params)
    {
        super(id, params);
    }

    /**
     * Creates a new GVCoreSecurityException with error code identified by
     * <code>errorId</code> and a cause.
     * 
     * @param id
     *        message associated to the exception
     * @param cause
     *        Throwable that caused this exception to get thrown
     */
    public GVCoreSecurityException(String id, Throwable cause)
    {
        super(id, cause);
    }

    /**
     * Creates a new GVCoreSecurityException with a cause.
     * 
     * @param id
     *        message associated to the exception
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     * @param cause
     *        Throwable that caused this exception to get thrown
     */
    public GVCoreSecurityException(String id, String[][] params, Throwable cause)
    {
        super(id, params, cause);
    }
}
