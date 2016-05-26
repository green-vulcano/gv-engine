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

import it.greenvulcano.log.NMDC;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class write, on the configured log file, the notification message.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class Log4JNotification implements GVNotification
{
    private static Logger       logger         = org.slf4j.LoggerFactory.getLogger(Log4JNotification.class);
    private String              input          = "";
    private String              baseInput      = "";
    private boolean             traceException = true;
    private String              id             = "";
    private String              msgLogger      = "";
    private String              excMsg         = "";
    private String              gvBufferMsg    = "";
    private String              staticMsg      = "";
    private boolean             critical       = false;
    private Map<String, String> nmdcProperties = new HashMap<String, String>();

    /**
     * Log4j fields
     */
    private org.apache.log4j.Logger              notifyLogger   = null;
    private Level               msgLevel       = Level.INFO;

    /**
     * @param notifyNode
     *        The Notify node in the configuration file
     * @see it.greenvulcano.gvesb.notification.GVNotification#init(org.w3c.dom.Node)
     * @throws XMLConfigException
     * @throws GVNotificationException
     */
    @Override
    public void init(Node notifyNode) throws XMLConfigException, GVNotificationException
    {
        input = XMLConfig.get(notifyNode, "@input", "");
        baseInput = XMLConfig.get(notifyNode, "@baseInput", "");
        traceException = XMLConfig.getBoolean(notifyNode, "@traceException", true);
        id = XMLConfig.get(notifyNode, "@id", "");
        critical = XMLConfig.get(notifyNode, "@critical", "no").equals("yes");
        excMsg = XMLConfig.get(notifyNode, "ExceptionMessage");
        gvBufferMsg = XMLConfig.get(notifyNode, "GVBufferMessage");
        staticMsg = XMLConfig.get(notifyNode, "StaticMessage");
        NodeList nmdc = XMLConfig.getNodeList(notifyNode, "NMDC/NMDCEntry");
        if ((nmdc != null) && (nmdc.getLength() > 0)) {
            for (int i = 0; i < nmdc.getLength(); i++) {
                Node n = nmdc.item(i);
                nmdcProperties.put(XMLConfig.get(n, "@name"), XMLConfig.get(n, "@value", ""));
            }
        }
        msgLevel = Level.toLevel(XMLConfig.get(notifyNode, "@level", "INFO"));
        msgLogger = XMLConfig.get(notifyNode, "@logger");
        if (msgLogger != null) {
            notifyLogger = org.apache.log4j.Logger.getLogger(msgLogger);
        }
        else {
            throw new GVNotificationException("CONFIGURATION_ERROR", new String[][]{{"XPath", "@logger"}});
        }
    }

    /**
     * This method creates the Notify message for the input object
     * 
     * @param envInput
     *        the Input object in the environment. It can be :<br>
     *        <li>Map</li> <li>GVBuffer</li> <li>Exception</li>
     * @throws GVNotificationException
     */
    @Override
    public void execute(Object envInput) throws GVNotificationException
    {
        String message = "";
        Object baseInp = null;
        try {
            if (envInput instanceof Map<?, ?>) {
                baseInp = ((Map<?, ?>) envInput).get(baseInput);
                envInput = ((Map<?, ?>) envInput).get(input);
            }
            if (envInput instanceof Throwable) {
                message = manageMessage(excMsg, staticMsg, "Exception");
            }
            else if (GVBuffer.class.isInstance(envInput)) {
                message = manageMessage(gvBufferMsg, staticMsg, "GVBuffer");
            }
            else {
                if (staticMsg == null) {
                    throw new GVNotificationException("NO_NOTIFICATION_MESSAGE", new String[][]{
                            {"inputObject", envInput.getClass().toString()}, {"id", id}});
                }
                message = staticMsg;
            }

            MessageFormatter messageFormatter = new MessageFormatter(message, envInput, baseInp);
            try {
                NMDC.push();
                setNMDC(envInput, baseInp);
                if (traceException && (envInput instanceof Throwable)) {
                    notifyLogger.log(msgLevel, messageFormatter.toString(), (Throwable) envInput);
                }
                else {
                    notifyLogger.log(msgLevel, messageFormatter);
                }
            }
            finally {
                NMDC.pop();
            }
        }
        catch (GVNotificationException exc) {
            logger.error("Error executing Log4J notification [" + id + "]", exc);
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error executing Log4J notification [" + id + "]", exc);
            throw new GVNotificationException("GV_NOTIFICATION_EXECUTE_ERROR", new String[][]{{"id", id},
                    {"class", this.getClass().getName()}}, exc);
        }
    }

    /**
     * This method is to set a notify as critical or not
     * 
     * @return Returns <i>true</i> if the notify object is critical in case of
     *         error.
     */
    @Override
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
     * @return message The message
     * @throws GVNotificationException
     */
    private String manageMessage(String msg, String stMsg, String inputType) throws GVNotificationException
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

        return message;
    }

    private void setNMDC(Object envInput, Object baseInp)
    {
        Iterator<String> it = nmdcProperties.keySet().iterator();
        while (it.hasNext()) {
            String name = it.next();
            String value = nmdcProperties.get(name);
            MessageFormatter messageFormatter = new MessageFormatter(value, envInput, baseInp);
            NMDC.put(name, messageFormatter.toString());
        }
    }
}
