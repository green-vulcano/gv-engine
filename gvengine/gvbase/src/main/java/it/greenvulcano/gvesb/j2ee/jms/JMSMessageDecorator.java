/**
 *
 */
package it.greenvulcano.gvesb.j2ee.jms;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.buffer.Id;

import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JMSMessageDecorator
{
    private static final String  PROPERTY_FIELD = "p$";
    private static final Pattern namesP         = Pattern.compile("^[_a-zA-Z\\$]([\\w\\$]*)$");

    /**
     * @param message
     * @param gvBuffer
     * @throws JMSException
     * @throws GVException
     */
    @SuppressWarnings("unchecked")
    public static void decorateGVBuffer(Message message, GVBuffer gvBuffer) throws JMSException, GVException
    {
        if (message.propertyExists(GVBuffer.Field.SYSTEM.toString())) {
            gvBuffer.setSystem(message.getStringProperty(GVBuffer.Field.SYSTEM.toString()));
        }
        if (message.propertyExists(GVBuffer.Field.SERVICE.toString())) {
            gvBuffer.setService(message.getStringProperty(GVBuffer.Field.SERVICE.toString()));
        }
        if (message.propertyExists(GVBuffer.Field.ID.toString())) {
            gvBuffer.setId(new Id(message.getStringProperty(GVBuffer.Field.ID.toString())));
        }
        if (message.propertyExists(GVBuffer.Field.RETCODE.toString())) {
            gvBuffer.setRetCode(message.getIntProperty(GVBuffer.Field.RETCODE.toString()));
        }

        Enumeration<String> propertiesName = message.getPropertyNames();
        while (propertiesName.hasMoreElements()) {
            String name = propertiesName.nextElement();
            if (name.startsWith(PROPERTY_FIELD)) {
                String value = message.getStringProperty(name);
                name = name.substring(PROPERTY_FIELD.length());
                gvBuffer.setProperty(name, value);
            }
        }
    }

    /**
     * @param message
     * @param gvBuffer
     * @throws JMSException
     * @throws GVException
     */
    public static void decorateMessage(Message message, GVBuffer gvBuffer) throws JMSException, GVException
    {
        if (!message.propertyExists(GVBuffer.Field.SYSTEM.toString())) {
            message.setStringProperty(GVBuffer.Field.SYSTEM.toString(), gvBuffer.getSystem());
        }
        if (!message.propertyExists(GVBuffer.Field.SERVICE.toString())) {
            message.setStringProperty(GVBuffer.Field.SERVICE.toString(), gvBuffer.getService());
        }
        if (!message.propertyExists(GVBuffer.Field.ID.toString())) {
            message.setStringProperty(GVBuffer.Field.ID.toString(), gvBuffer.getId().toString());
        }
        if (!message.propertyExists(GVBuffer.Field.RETCODE.toString())) {
            message.setIntProperty(GVBuffer.Field.RETCODE.toString(), gvBuffer.getRetCode());
        }
        if (message.getJMSCorrelationID() == null || message.getJMSCorrelationID().equals("")) {
            message.setJMSCorrelationID(gvBuffer.getId().toString());
        }
        for (String name : gvBuffer.getPropertyNames()) {
            Matcher m = namesP.matcher(name);
            if (m.matches()) {
                String fieldName = PROPERTY_FIELD + name;
                if (!message.propertyExists(fieldName)) {
                    message.setStringProperty(fieldName, gvBuffer.getProperty(name));
                }
            }
        }
    }

}
