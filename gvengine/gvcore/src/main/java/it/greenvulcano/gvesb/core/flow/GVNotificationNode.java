/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.core.flow;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.gvesb.notification.GVNotification;
import it.greenvulcano.gvesb.notification.GVNotificationException;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 * 
 */
public class GVNotificationNode extends GVFlowNode
{
    private static final Logger    logger               = org.slf4j.LoggerFactory.getLogger(GVNotificationNode.class);

    /**
     * Notification vector
     */
    private Vector<GVNotification> notificationVector   = new Vector<GVNotification>();

    /**
     * The Id of the following node in workflow
     */
    private String                 nextNodeId           = "";

    /**
     * What to do when a critical notification raise an exception
     */
    private boolean                onCriticalErrorBreak = false;

    /**
     * The inititalization method creates the vector list of notifications.
     * 
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node defNode) throws GVCoreConfException
    {
        super.init(defNode);

        try {
            nextNodeId = XMLConfig.get(defNode, "@next-node-id");
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'next-node-id'"},
                    {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }

        try {
            checkOnCriticalErrorBehavior(defNode);
            notificationVector = buildNotificationVector(defNode);
        }
        catch (GVCoreConfException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new GVCoreConfException("GVCORE_NOTIFICATION_NODE_INIT_ERROR", new String[][]{{"node",
                    XPathFinder.buildXPath(defNode)}}, exc);
        }
    }

    /**
     * The notification execute method calls the execute method of each element
     * in the notificationVector. If a critical exception occurs, it's put in
     * the environment, and the execution can be terminated, dependently on the
     * value of the "onCriticalErrorBreak" parameter.
     * 
     * @param environment
     *        the flow environment
     * @return the next flow node id
     * @throws GVCoreException
     *         is errors occurs
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#execute(java.util.Map,
     *      boolean)
     */
    @Override
    public String execute(Map<String, Object> environment, boolean onDebug) throws GVCoreException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        if ((notificationVector.size() == 0)) {
            logger.info("Skipping execution of GVNotificationNode '" + getId()
                    + "': No GVNotification configured for this GVNotificationNode");
            return nextNodeId;
        }
        logger.info("Executing GVNotificationNode '" + getId() + "'");
        checkInterrupted("GVNotificationNode", logger);
        dumpEnvironment(logger, true, environment);

        try {
            Object inputObj = environment.get(getInput());
            if (GVBuffer.class.isInstance(inputObj) && (logger.isDebugEnabled() || isDumpInOut())) {
                logger.info(GVFormatLog.formatINPUT((GVBuffer) inputObj, false, false).toString());
            }
            GVNotificationException criticalExc = null;
            for (int ind = 0; (ind < notificationVector.size()) && !isInterrupted(); ind++) {
                GVNotification notification = notificationVector.elementAt(ind);
                try {
                    notification.execute(environment);
                }
                catch (GVNotificationException exc) {
                    logger.debug("Notification Node " + getId() + "; An exception "
                            + "occurred performing notification: " + notification.getClass().getName()
                            + ". The exception message is: " + exc.getMessage());
                    if (notification.isCritical()) {
                        criticalExc = exc;
                        if (onCriticalErrorBreak) {
                            break;
                        }
                    }
                }
            }
            checkInterrupted("GVNotificationNode", logger);
            if (criticalExc != null) {
                environment.put(getOutput(), criticalExc);
            }
            else if (getInput() != null && environment.get(getInput()) != null && environment.get(getOutput()) == null) {
                environment.put(getOutput(), environment.get(getInput()));
            }
            Object outputObj = environment.get(getInput());
            if (GVBuffer.class.isInstance(inputObj) && (logger.isDebugEnabled() || isDumpInOut())) {
                logger.info(GVFormatLog.formatOUTPUT((GVBuffer) outputObj, false, false).toString());
            }
        }
        catch (InterruptedException exc) {
            throw exc;
        }
        catch (Exception exc) {
            environment.put(getOutput(), exc);
        }

        dumpEnvironment(logger, false, environment);
        long endTime = System.currentTimeMillis();
        logger.info("END - Execute GVNotificationNode '" + getId() + "' - ExecutionTime (" + (endTime - startTime) + ")");

        return nextNodeId;
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#getDefaultNextNodeId()
     */
    @Override
    public String getDefaultNextNodeId()
    {
        return nextNodeId;
    }

    /**
     * Do nothing.
     * 
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#cleanUp()
     */
    @Override
    public void cleanUp() throws GVCoreException
    {
        // do nothing
    }

    /**
     * 
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#destroy()
     */
    @Override
    public void destroy() throws GVCoreException
    {
        // do nothing
    }

    /**
     * This method returns an Object that implements the Notification interface,
     * obtained from its Class name.
     * 
     * @param notificationNode
     *        the node from which read configuration data
     * @return the configured notification
     * @throws GVCoreConfException
     *         if errors occurs
     */
    private GVNotification getNotification(Node notificationNode) throws GVCoreConfException
    {
        String className = null;
        GVNotification notification = null;

        try {
            className = XMLConfig.get(notificationNode, "@class");
            notification = (GVNotification) Class.forName(className).newInstance();
            notification.init(notificationNode);
            logger.info("Prepared GVNotification: " + notification);
        }
        catch (ClassNotFoundException exc) {
            throw new GVCoreConfException("GVCORE_CLASS_NOT_FOUND_ERROR", new String[][]{{"className", className}}, exc);
        }
        catch (Exception exc) {
            throw new GVCoreConfException("GVCORE_NEW_INSTANCE_ERROR_ERROR", new String[][]{{"className", className},
                    {"exception", exc.getMessage()}}, exc);
        }

        return notification;
    }

    /**
     * This method returns the Vector with the Notification to be performed,
     * obtained from its Class name.
     * 
     * @param definitionNode
     *        the node definition
     * @return the notifications vector
     * @throws GVCoreConfException
     *         if error occurs
     * @throws XMLConfigException
     *         if error occurs
     */
    private Vector<GVNotification> buildNotificationVector(Node definitionNode) throws GVCoreConfException,
            XMLConfigException
    {
        NodeList nodeListDef = null;
        Vector<GVNotification> notiVect = new Vector<GVNotification>();
        nodeListDef = XMLConfig.getNodeList(definitionNode, "*[@type='notification-node']");
        if ((nodeListDef != null) && (nodeListDef.getLength() > 0)) {
            for (int i = 0; i < nodeListDef.getLength(); i++) {
                notiVect.add(getNotification(nodeListDef.item(i)));
            }
        }
        return notiVect;
    }

    /**
     * This is the setter method for the "onCriticalErrorBreak" parameter
     * 
     * @param definitionNode
     *        the node from which read the flag value
     * @throws GVCoreConfException
     *         if errors occurs
     */
    private void checkOnCriticalErrorBehavior(Node definitionNode) throws GVCoreConfException
    {
        String onCriticalErrorValue = "";

        try {
            onCriticalErrorValue = XMLConfig.get(definitionNode, "@on-critical-error");
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{
                    {"name", "'on-critical-error'"}, {"node", XPathFinder.buildXPath(definitionNode)}}, exc);
        }
        if (onCriticalErrorValue.equalsIgnoreCase("break")) {
            onCriticalErrorBreak = true;
        }
        else if (onCriticalErrorValue.equalsIgnoreCase("continue")) {
            onCriticalErrorBreak = false;
        }
        else {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{
                    {"name", "'on-critical-error'"}, {"node", XPathFinder.buildXPath(definitionNode)}});
        }
    }
}