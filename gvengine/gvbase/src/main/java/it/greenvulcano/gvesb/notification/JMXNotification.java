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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.utils.MessageFormatter;
import it.greenvulcano.jmx.JMXUtils;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

import java.lang.reflect.Constructor;
import java.util.Map;

import javax.management.Notification;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * This class reads the configuration file to keep the information useful for
 * the notification object construction.
 * The notification object can be :
 * <li> Standard : "javax.management.Notification"</li>
 * <li>GVBuffer Notification: A notification object to notifies an GVBuffer object.</li>
 * <li>Exception Notification: A notification object to notifies an Exception object.</li>
 *
 * The choice between these different notification object is based on :
 * <li>The configuration file read.</li>
 * <li>The Object found in the environment.</li>
 *
 * Is also possible to insert in the configuration file a Default notification
 * object to be used in case of the object in the environment is not an
 * Exception/GVBuffer or in case of a wrong configuration file.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JMXNotification implements GVNotification
{
    private static final Logger logger                        = org.slf4j.LoggerFactory.getLogger(JMXNotification.class);

    private String              input                         = "";
    private String              mainInput                     = "";
    private String              id                            = "";
    private String              notificationType              = "";
    private String              objectName                    = "";
    private String              staticMsg                     = null;
    boolean                     critical                      = false;

    private String              excMsg                        = null;
    private String              notificationExcClassName      = "";

    private String              gvBufferMsg                   = null;
    private String              notificationGVBufferClassName = "";

    /**
     * This field is evaluated only for the Notification with the GVBuffer
     * object.
     */
    private String              dataValue                     = null;

    @SuppressWarnings("unchecked")
    private Class               notificationClass             = null;

    private Node                nodeGVBuffer                  = null;
    private Node                nodeExc                       = null;
    private Node                nodeDef                       = null;

    /**
     * @param notifyNode
     *        The Notify node in the configuration file
     * @see it.greenvulcano.gvesb.notification.GVNotification#init(org.w3c.dom.Node)
     * @throws XMLConfigException
     * @throws GVNotificationException
     */
    public void init(Node notifyNode) throws XMLConfigException, GVNotificationException
    {
        input = XMLConfig.get(notifyNode, "@input", "");
        mainInput = XMLConfig.get(notifyNode, "@mainInput", "");
        id = XMLConfig.get(notifyNode, "@id", "");
        critical = XMLConfig.get(notifyNode, "@critical", "no").equals("yes");
        staticMsg = XMLConfig.get(notifyNode, "StaticMessage");
        nodeGVBuffer = XMLConfig.getNode(notifyNode, "JMXGVBufferNotification");
        nodeExc = XMLConfig.getNode(notifyNode, "JMXExcNotification");
        nodeDef = XMLConfig.getNode(notifyNode, "JMXDefaultNotification");

        if ((nodeGVBuffer == null) && (nodeExc == null) && (nodeDef == null)) {
            throw new GVNotificationException("GV_NOTIFICATION_INIT_ERROR", new String[][]{{"id", id},
                    {"class", this.getClass().getName()}});
        }

        setNodeAttributes(nodeGVBuffer, "GVBuffer");
        setNodeAttributes(nodeExc, "Exc");

        objectName = XMLConfig.get(notifyNode, "@objectName", "");
        try {
            objectName = PropertiesHandler.expand(objectName);
        }
        catch (PropertiesHandlerException exc) {
            // do nothing
        }

        notificationType = XMLConfig.get(notifyNode, "@notificationType", "it.greenvulcano.gvesb.core.notification");
    }

    /**
     * This method creates the Notify message for the input object
     *
     * @param envInput
     *        the Input object in the environment. It can be :<br>
     *        <li>Map</li> <li>GVBuffer</li> <li>Exception</li>
     * @throws GVNotificationException
     */
    @SuppressWarnings("unchecked")
    public void execute(Object envInput) throws GVNotificationException
    {
        String message = "";
        Object mainEnvInput = null;
        String envType = "";
        Notification notification = null;

        try {
            if (envInput instanceof Map) {
                mainEnvInput = ((Map) envInput).get(mainInput);
                envInput = ((Map) envInput).get(input);
            }
            if (envInput instanceof Exception) {
                envType = "Exception";
                message = manageMessage(excMsg, staticMsg, envType, envInput);
                notification = createNotification(notificationExcClassName, message, envInput, mainEnvInput, envType);
            }
            else if (envInput instanceof GVBuffer) {
                envType = "GVBuffer";
                message = manageMessage(gvBufferMsg, staticMsg, envType, envInput);
                notification = createNotification(notificationGVBufferClassName, message, envInput, mainEnvInput,
                        envType);
            }
            else if (staticMsg == null) {
                throw new GVNotificationException("NO_NOTIFICATION_MESSAGE", new String[][]{
                        {"inputObject", envInput.toString()}, {"id", id}});
            }
            else {
                envType = "NoType";
                message = staticMsg;
                if (nodeDef == null) {
                    throw new GVNotificationException("NO_NOTIFICATION_MESSAGE", new String[][]{
                            {"inputObject", envInput.toString()}, {"id", id}});
                }
                notification = new Notification(notificationType, objectName, 1, message);
            }
            JMXUtils.sendNotification(objectName, notification, true, logger);
        }
        catch (GVNotificationException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new GVNotificationException("GV_NOTIFICATION_EXECUTE_ERROR", new String[][]{{"id", id},
                    {"class", this.getClass().getName()}, {"cause", exc.getMessage()}}, exc);
        }
    }

    /**
     * This method is to set a notify as critical or not
     *
     * @return critical Is <i>true</i> if the notify object is critical in case
     *         of error.
     */
    public boolean isCritical()
    {
        return critical;
    }

    /**
     * This method read the notification message for the given input object.
     *
     * @param msg
     *        The dynamic message for the input object (GVBuffer or GVException)
     * @param stMsg
     *        The static message for the input object (GVBuffer or GVException)
     * @param inputType
     *        A string to identify the input object (GVBuffer or GVException)
     * @param object
     *        the object where the formatter get value for the dynamic
     *        parameters
     * @return message The message
     * @throws GVNotificationException
     */
    private String manageMessage(String msg, String stMsg, String inputType, Object object)
            throws GVNotificationException
    {
        String message = "";

        if (msg == null) {
            if (stMsg == null) {
                throw new GVNotificationException("NO_NOTIFICATION_MESSAGE", new String[][]{{"inputObject", inputType},
                        {"id", id}});
            }
            message = stMsg;
        }
        else {
            message = msg;
        }

        MessageFormatter messageFormatter = new MessageFormatter(message, object);
        return messageFormatter.toString();
    }

    /**
     * Create the notification object
     *
     * @return notification The Notification Object created
     */
    @SuppressWarnings("unchecked")
    private Notification createNotification(String className, String message, Object object, Object mainEnvInput,
            String envType) throws GVNotificationException
    {
        Notification notification = null;
        Constructor notConstr = null;
        Object[] paramsValue = null;

        try {
            if ((className != null) && (className != "")) {
                notificationClass = Class.forName(className);
                Class[] paramDef = new Class[]{String.class, Object.class, Long.TYPE, String.class};
                notConstr = notificationClass.getConstructor(paramDef);
                paramsValue = new Object[]{notificationType, objectName, Long.valueOf(1l), message};
            }

            if (envType.equals("GVBuffer")) {
                if (nodeGVBuffer != null) {
                    JMXGVBufferNotificationInterface notificationGVBuffer = (JMXGVBufferNotificationInterface) notConstr.newInstance(paramsValue);
                    notificationGVBuffer.init(object, mainEnvInput);
                    notificationGVBuffer.set("buffer", dataValue);
                    notification = ((Notification) notificationGVBuffer);
                }
                else {
                    if (nodeDef == null) {
                        throw new GVNotificationException("NO_NOTIFICATION_MESSAGE", new String[][]{
                                {"inputObject", envType}, {"id", id}});
                    }
                    notification = new Notification(notificationType, objectName, 1, message);
                }
            }
            else {
                if (envType.equals("Exception")) {
                    if (nodeExc != null) {
                        JMXExcNotificationInterface notificationExc = (JMXExcNotificationInterface) notConstr.newInstance(paramsValue);
                        notificationExc.init(object, mainEnvInput);
                        notification = ((Notification) notificationExc);
                    }
                    else {
                        if (nodeDef == null) {
                            throw new GVNotificationException("NO_NOTIFICATION_MESSAGE", new String[][]{
                                    {"inputObject", envType}, {"id", id}});
                        }
                        notification = new Notification(notificationType, objectName, 1, message);
                    }
                }
            }
        }
        catch (GVNotificationException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("GV_NOTIFICATION_EXECUTE_ERROR", exc);
            throw new GVNotificationException("GV_NOTIFICATION_EXECUTE_ERROR", new String[][]{{"id", id},
                    {"class", this.getClass().getName()}, {"cause", exc.getMessage()}}, exc);
        }

        return notification;
    }

    /**
     * This method evaluate the attributes for the element node done.
     *
     * @param node
     *        The node object for the configured notification object
     * @param type
     *        the notification object type (GVBuffer and others ...)
     */
    private void setNodeAttributes(Node node, String type) throws XMLConfigException
    {
        if (type != null) {
            if (type.equals("GVBuffer") && (nodeGVBuffer != null)) {
                notificationGVBufferClassName = XMLConfig.get(node, "@class");
                dataValue = XMLConfig.get(node, "@dataValue");
                gvBufferMsg = XMLConfig.get(node, "GVBufferMessage");
            }
            else {
                if (type.equals("Exc") && (nodeExc != null)) {
                    notificationExcClassName = XMLConfig.get(node, "@class");
                    excMsg = XMLConfig.get(node, "ExceptionMessage");
                }
            }
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String result = "input = '" + input + "' - mainInput = '" + mainInput + "' - id = '" + id + "' - excMsg = '"
                + excMsg + "' - gvBufferMsg = '" + gvBufferMsg + "' - staticMsg = '" + staticMsg
                + "' - notificationsGVBufferName = '" + notificationGVBufferClassName + "' - notificationsExcName = '"
                + notificationExcClassName + "' - notificationType = '" + notificationType + "' - objectName = '"
                + objectName + "'";
        return result;
    }
}
