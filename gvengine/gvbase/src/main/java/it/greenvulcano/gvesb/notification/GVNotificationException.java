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
package it.greenvulcano.gvesb.notification;

import it.greenvulcano.gvesb.buffer.GVException;

/**
 * Exception for the Notification node
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVNotificationException extends GVException
{
    private static final long serialVersionUID = -4606073732540919425L;

    /**
     * If the GVNotificationicationException has a cause, this field contains
     * it. A cause can be another Throwable that caused this Exception to get
     * thrown.
     */
    protected Throwable       cause            = null;

    /**
     * Creates a new GVNotificationException with error code identified by
     * <code>errorId</code> and no cause.
     *
     * @param idMessage
     *        message associated to an error in the error catalog
     */
    public GVNotificationException(String idMessage)
    {
        super(idMessage);
    }

    /**
     * Creates a new GVNotificationException with error code identified by
     * <code>errorId</code> and no cause. <code>params</code> is used to
     * complete the error message.
     *
     * @param idMessage
     *        message associated to an error in the error catalog
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     */
    public GVNotificationException(String idMessage, String[][] params)
    {
        super(idMessage, params);
    }

    /**
     * Creates a new GVNotificationException with error code identified by
     * <code>errorId</code> and a cause.
     *
     * @param idMessage
     *        message associated to the exception
     * @param cause
     *        Throwable that caused this exception to get thrown
     */
    public GVNotificationException(String idMessage, Throwable cause)
    {
        super(idMessage);
        this.cause = cause;
    }

    /**
     * Creates a new GVNotificationException with a cause.
     *
     * @param idMessage
     *        message associated to the exception
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     * @param cause
     *        Throwable that caused this exception to get thrown
     */
    public GVNotificationException(String idMessage, String[][] params, Throwable cause)
    {
        super(idMessage, params);
        this.cause = cause;
    }
}
