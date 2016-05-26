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

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.management.Notification;

/**
 * This class is a JMX notification object with the Exception fields
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JMXExcNotification extends Notification implements JMXExcNotificationInterface
{
    private static final long serialVersionUID = 2160924652187873772L;

    public String             excMessage       = "";
    public String             cause            = "";
    public String             stackTrace       = "";
    public String             catalogMessage   = "";
    public String             errorCode        = "";
    public String             system           = "";
    public String             service          = "";
    public String             id               = "";

    /**
     * @param notificationType
     *        The dot separated String value indicating the type of notification
     *        object
     * @param source
     *        The source of the notification. Contains an ObjectName
     * @param sequenceNumber
     *        The number indicating the order in relation of events from the
     *        source. To ability the listeners to sort the notifications.
     * @param message
     *        A string object representing a message
     */
    public JMXExcNotification(String notificationType, Object source, long sequenceNumber, String message)
    {
        super(notificationType, source, sequenceNumber, message);
    }

    /**
     * @return the exception message
     */
    public String getExcMessage()
    {
        return excMessage;
    }

    /**
     * @return the cause
     */
    public String getCause()
    {
        return cause;
    }

    /**
     * @return the stack trace
     */
    public String getStackTrace()
    {
        return stackTrace;
    }

    /**
     * @return the catalog message
     */
    public String getCatalogMessage()
    {
        return catalogMessage;
    }

    /**
     * @return the error code
     */
    public String getErrorCode()
    {
        return errorCode;
    }

    /**
     * @return the system
     */
    public String getSystem()
    {
        return system;
    }

    /**
     * @return the service
     */
    public String getService()
    {
        return service;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * Initialization method to create the notification exception object
     *
     * @param object
     *        the object to get value to initialize the notification object
     * @param mainEnvInput
     *        the main input object of the flow
     * @throws GVNotificationException
     */
    public void init(Object object, Object mainEnvInput) throws GVNotificationException
    {
        if (object instanceof Throwable) {
            excMessage = ((Throwable) object).getMessage();
            Throwable causeObject = ((Throwable) object).getCause();
            if (causeObject != null) {
                cause = causeObject.toString();
            }
            else {
                cause = "No Cause Available.";
            }
            StringWriter buf = new StringWriter();
            PrintWriter out = new PrintWriter(buf);

            ((Throwable) object).printStackTrace(out);
            out.flush();
            stackTrace = buf.toString();

            if (object instanceof GVException) {
                catalogMessage = ((GVException) object).getMessage();
                errorCode = "" + ((GVException) object).getErrorCode();
            }

            if (object instanceof GVNotificationException) {
                catalogMessage = ((GVNotificationException) object).getMessage();
                errorCode = "" + ((GVNotificationException) object).getErrorCode();
            }
        }

        if (mainEnvInput instanceof GVBuffer) {
            system = ((GVBuffer) mainEnvInput).getSystem();
            service = ((GVBuffer) mainEnvInput).getService();
            id = ((GVBuffer) mainEnvInput).getId().toString();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.notification.JMXGVBufferNotificationInterface#set(java.lang.String,
     *      java.lang.String)
     */
    public void set(String fieldName, String fieldvalue) throws GVNotificationException
    {
        // do nothing
    }
}
