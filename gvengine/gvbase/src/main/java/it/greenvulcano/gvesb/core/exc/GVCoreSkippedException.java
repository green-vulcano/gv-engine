/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
 * <code>GVCoreSkippedException</code> is the exception raised by the
 * GreenVulcano core components in case of errors.
 *
 * @version 3.4.0 Jun 17, 2013
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public class GVCoreSkippedException extends GVCoreException
{
    private static final long serialVersionUID = 8573236356527643609L;

    /**
     * Creates a new GVCoreSkippedException with error code identified by
     * <code>errorId</code> and no cause.
     *
     * @param id
     *        message associated to an error in the error catalog
     */
    public GVCoreSkippedException(String id)
    {
        super(id);
    }

    /**
     * Creates a new GVCoreSkippedException with error code identified by
     * <code>errorId</code> and no cause. <code>parameters</code> is used to
     * complete the error message.
     *
     * @param id
     *        message associated to an error in the error catalog
     * @param parameters
     *        key/value array of parameters to be substituted in the error
     *        message.
     */
    public GVCoreSkippedException(String id, String[][] parameters)
    {
        super(id, parameters);
    }

    /**
     * Creates a new GVCoreSkippedException with error code identified by
     * <code>errorId</code> and a cause.
     *
     * @param id
     *        message associated to the exception
     * @param cause
     *        Throwable that caused this exception to get thrown
     */
    public GVCoreSkippedException(String id, Throwable cause)
    {
        super(id, cause);
    }

    /**
     * Creates a new GVCoreSkippedException with a cause.
     *
     * @param id
     *        message associated to the exception
     * @param parameters
     *        key/value array of parameters to be substituted in the error
     *        message.
     * @param cause
     *        Throwable that caused this exception to get thrown
     */
    public GVCoreSkippedException(String id, String[][] parameters, Throwable cause)
    {
        super(id, parameters, cause);
    }
}
