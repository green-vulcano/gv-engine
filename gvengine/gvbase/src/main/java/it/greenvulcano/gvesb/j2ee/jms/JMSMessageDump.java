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
package it.greenvulcano.gvesb.j2ee.jms;

import it.greenvulcano.util.bin.Dump;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * Utility class to dump JMS messages.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JMSMessageDump
{
    private static final String DATE_TOSTRING_FORMAT = "dd.MM.yyyy HH:mm:ss.SSS";

    private Message             message              = null;
    private String              description          = null;
    private boolean             escapeBody           = false;

    /**
     * @param message
     * @param description
     */
    public JMSMessageDump(Message message, String description)
    {
        this.message = message;
        this.description = description;
    }

    /**
     * @return if escape body is set
     */
    public boolean isEscapeBody()
    {
        return escapeBody;
    }

    /**
     * @param escapeBody
     */
    public void setEscapeBody(boolean escapeBody)
    {
        this.escapeBody = escapeBody;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @SuppressWarnings("unchecked")
    public String toString()
    {
        StringBuilder sbuffer = new StringBuilder();
        Date date = null;

        if ((description != null) && !description.equals("")) {
            sbuffer.append(description).append("\n");
        }
        try {
            sbuffer.append(format("Message class", ": ")).append("'").append(message.getClass().getName()).append("'").append(
                    "\n");
            sbuffer.append(format("JMSCorrelationID", ": ")).append("'").append(message.getJMSCorrelationID()).append(
                    "'").append("\n");
            int delivery = message.getJMSDeliveryMode();
            String deliveryMode = "";
            if (delivery == 2) {
                deliveryMode = "Persistent";
            }
            else {
                if (delivery == 1) {
                    deliveryMode = "NotPersistent";
                }
            }
            sbuffer.append(format("JMSDeliveryMode", ": ")).append("'").append(delivery).append("'").append("(").append(
                    deliveryMode).append(")").append("\n");
            date = new Date(message.getJMSExpiration());
            String sdate = "Never";
            if (message.getJMSExpiration() != 0) {
                sdate = DateUtils.dateToString(date, DateUtils.FORMAT_ISO_TIMESTAMP_L);
            }
            sbuffer.append(format("JMSExpiration", ": ")).append("'").append(sdate).append("'").append(" (Millisecond: ").append(
                    "'").append(message.getJMSExpiration()).append("')").append("\n");
            sbuffer.append(format("JMSMessageID", ": ")).append("'").append(message.getJMSMessageID()).append("'").append(
                    "\n");
            sbuffer.append(format("JMSPriority", ": ")).append("'").append(message.getJMSPriority()).append("'").append(
                    "\n");
            date = new Date(message.getJMSTimestamp());
            sbuffer.append(format("JMSTimestamp", ": ")).append("'").append(DateUtils.dateToString(date, DateUtils.FORMAT_ISO_TIMESTAMP_L)).append("'").append(
                    " (Millisecond: ").append("'").append(message.getJMSTimestamp()).append("')").append("\n");
            sbuffer.append(format("JMSType", ": ")).append("'").append(message.getJMSType()).append("'").append("\n");
            sbuffer.append(format("JMSDestination", ": ")).append("'").append(message.getJMSDestination()).append("'").append(
                    "\n");
            sbuffer.append(format("JMSRedelivered", ": ")).append("'").append(message.getJMSRedelivered()).append("'").append(
                    "\n");
            sbuffer.append(format("JMSReplyTo", ": ")).append("'").append(message.getJMSReplyTo()).append("'").append(
                    "\n");
            Enumeration<String> en = message.getPropertyNames();
            while (en.hasMoreElements()) {
                String name = en.nextElement();
                Object value = message.getObjectProperty(name);
                sbuffer.append(format(name, ": ")).append("'").append(value).append("'").append(
                        value == null ? "" : " (" + value.getClass().getName() + ")").append("\n");
            }
        }
        catch (Exception exc) {
            sbuffer.append("Error reading message properties: " + exc).append("\n");
        }
        sbuffer.append(format("Payload", ": ")).append("\n").append(dumpPayload());

        return sbuffer.toString();
    }

    private String dumpPayload()
    {
        byte[] payload = null;

        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String body = textMessage.getText();
                if (body == null) {
                    payload = new byte[0];
                }
                else {
                    payload = textMessage.getText().getBytes();
                }
            }
            else if (message instanceof BytesMessage) {
                BytesMessage bytesMessage = (BytesMessage) message;
                try {
                    int size;
                    byte[] buffer = new byte[1024];
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                    while ((size = bytesMessage.readBytes(buffer)) != -1) {
                        baos.write(buffer, 0, size);
                    }
                    payload = baos.toByteArray();
                }
                finally {
                    bytesMessage.reset();
                }
            }
            else if (message instanceof MapMessage) {
            	MapMessage mapMessage = (MapMessage) message;
            	StringBuilder sbuffer = new StringBuilder();
                try {
                	@SuppressWarnings("unchecked")
                    Enumeration<String> names = mapMessage.getMapNames();
                	while(names.hasMoreElements()) {
                		String name = names.nextElement();
                        Object value = mapMessage.getObject(name);
                        sbuffer.append(format(name, ": ")).append("'").append(value).append("'").append(
                                value == null ? "" : " (" + value.getClass().getName() + ")").append("\n");
					}
                	return sbuffer.toString();
                }
                finally {
                	//
                }
            }
            else {
                return "Payload dump not available";
            }
            String sPayload = new Dump(payload, -1).toString();
            if (escapeBody) {
                sPayload = XMLUtils.replaceXMLInvalidChars(sPayload);
            }
            return sPayload;
        }
        catch (Exception exc) {
            return ("Error dumping payload: " + exc);
        }
    }

    private static String format(String str, Object suffix)
    {
        return format(20, str, suffix);
    }

    private static String format(int size, String str, Object suffix)
    {
        str = str + ".............................................................................";
        return str.substring(0, size) + suffix;
    }
}
