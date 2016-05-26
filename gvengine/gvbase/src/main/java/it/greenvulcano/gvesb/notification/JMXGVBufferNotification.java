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

import javax.management.Notification;

/**
 * This class is a JMX notification object with the GVBuffer fields
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JMXGVBufferNotification extends Notification implements JMXGVBufferNotificationInterface
{
    private static final long serialVersionUID = -5403016600823687401L;

    public GVBuffer           gvBuffer         = null;

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
    public JMXGVBufferNotification(String notificationType, Object source, long sequenceNumber, String message)
    {
        super(notificationType, source, sequenceNumber, message);
    }

    /**
     * Create a copy of the GVBuffer Object to not change its value in the
     * environment.
     *
     * @param gvBuffer
     *        GVBuffer object found in the environment.
     */
    private void setGVBuffer(GVBuffer gvBuffer)
    {
        this.gvBuffer = new GVBuffer(gvBuffer);
    }

    /**
     * Set the fieldName with the requested value.
     *
     * @param fieldName
     *        The field Name to value
     * @param dataValue
     *        If the GVBuffer data field must be evaluated or not in the
     *        notification.
     * @throws GVNotificationException
     */
    public void set(String fieldName, String dataValue) throws GVNotificationException
    {
        if (fieldName.equals("buffer")) {
            byte emptyBuffer[] = new byte[0];
            try {
                if (dataValue.equals("no")) {
                    gvBuffer.setObject(emptyBuffer);
                }
            }
            catch (GVException exc) {
                throw new GVNotificationException("GV_NOTIFICATION_EXECUTE_ERROR", new String[][]{{"class",
                        this.getClass().getName()}}, exc);
            }
        }
    }

    /**
     * Get the GVBuffer object value
     *
     * @return gvBuffer the gvBuffer object value.
     */
    public GVBuffer getGVBuffer()
    {
        return gvBuffer;
    }

    /**
     * Initialization method to create the object
     *
     * @param object
     *        the object to get value to initialize the notification object
     * @param mainEnvInput
     *        The main input object in the flow
     * @throws GVNotificationException
     */
    public void init(Object object, Object mainEnvInput) throws GVNotificationException
    {
        if (object instanceof GVBuffer) {
            setGVBuffer((GVBuffer) object);
        }
    }
}
