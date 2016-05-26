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
package it.greenvulcano.gvesb.core.task;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVPublicException;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.core.GreenVulcano;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.gvesb.core.task.GVTaskAction.ActionType;
import it.greenvulcano.gvesb.internal.data.ChangeGVBuffer;
import it.greenvulcano.gvesb.j2ee.XAHelper;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.gvesb.utils.MessageFormatter;
import it.greenvulcano.jmx.JMXEntryPoint;

import it.greenvulcano.log.NMDC;
import it.greenvulcano.scheduler.Task;
import it.greenvulcano.scheduler.TaskException;
import it.greenvulcano.util.txt.DateUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.transaction.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Specialization of the GreenVulcano {@link it.greenvulcano.scheduler.Task
 * <code>Task</code>} class, which performs a call to an GreenVulcano workflow
 * upon scheduling.
 * 
 * 
 * @version 3.2.0 09/11/2011
 * @author GreenVulcano Developer Team
 */
public class GVServiceCallerTask extends Task
{

    private static final String  SUBSYSTEM         = "GreenVulcano-TimerTask";

    private static final Logger  logger            = LoggerFactory.getLogger(GVServiceCallerTask.class);

    private String               location          = null;

    /**
     *
     */
    protected GVBuffer           GVBufferIn        = null;
    private String               operation         = "";
    private boolean              transacted        = false;

    /**
     * The GreenVulcanoPool instance.
     */
    private GreenVulcanoPool     greenVulcanoPool  = null;

    
    private XAHelper             xaHelper          = null;
    private int                  txTimeout         = 30;

    private Vector<GVTaskAction> actionHandlers    = null;

    
    private int                  maxCalls          = 100;

    private ChangeGVBuffer       cGVBuffer         = null;

    private File                 outputDirectory   = null;

    private static final String  OUTPUT_CREATE     = "create";
    private static final String  OUTPUT_APPEND     = "append";
    private static final String  OUTPUT_CREATE_NEW = "create-new";

    private String               outputPolicy      = OUTPUT_CREATE;

    private static final String  OUTPUT_BODY       = "body";
    private static final String  OUTPUT_DUMP       = "dump";
    private static final String  OUTPUT_BOTH       = "both";

    private String               outputType        = OUTPUT_BODY;

    private static final String  DATE_TIME_FORMAT  = "yyyyMMddHHmmssSSS";

    /**
     * Initialization method.
     * 
     * @param node
     *        configuration node. The operation should use this node with
     *        <code>XMLConfig</code> in order to read its configuration
     *        parameters.
     * 
     * @exception TaskException
     *            if an error occurs during initialization
     * 
     * 
     * @see it.greenvulcano.scheduler.Task#initTask(org.w3c.dom.Node)
     */
    @Override
    protected void initTask(Node node) throws TaskException
    {
        NMDC.push();
        NMDC.clear();
        NMDC.setSubSystem(SUBSYSTEM);
        try {
            location = JMXEntryPoint.getServerName();
            NMDC.setServer(location);

            // Create a template GVBuffer instance
            GVBufferIn = new GVBuffer();
            GVBufferIn.setSystem(XMLConfig.get(node, "@id-system", GVBuffer.DEFAULT_SYS));
            GVBufferIn.setService(XMLConfig.get(node, "@id-service"));

            operation = XMLConfig.get(node, "@operation");
            transacted = XMLConfig.getBoolean(node, "@transacted", false);
            txTimeout = XMLConfig.getInteger(node, "@timeout", 30);

            Node xaHNode = null;
            try {
                xaHNode = XMLConfig.getNode(node, "XAHelper");
            }
            catch (Exception exc) {
                xaHNode = null;
            }
            try {
                xaHelper = new XAHelper(xaHNode);
            }
            catch (Exception exc) {
                throw new TaskException("Error initializing XAHelper", exc);
            }
            
            maxCalls = XMLConfig.getInteger(node, "@max-calls-sequence", 1);

            // Configure post-call actions
            setActionHandlers(node);

            // Configure ChangeGVBuffer instance
            Node cGVBufferNode = XMLConfig.getNode(node, "ChangeGVBuffer");
            if (cGVBufferNode != null) {
                cGVBuffer = new ChangeGVBuffer();
                cGVBuffer.setLogger(logger);
                try {
                    cGVBuffer.init(cGVBufferNode);
                }
                catch (XMLConfigException exc) {
                    logger.error("Error initializing ChangeGVBuffer", exc);
                    throw new TaskException("Error initializing ChangeGVBuffer", exc);
                }
            }

            // Configure output formatter
            Node outputNode = XMLConfig.getNode(node, "OutputData");
            if (outputNode != null) {
                configureOutput(outputNode);
            }

            // Get an handle to the GreenVulcano pool
            greenVulcanoPool = GreenVulcanoPoolManager.instance().getGreenVulcanoPool(SUBSYSTEM);

            logger.debug("Configured GVServiceCallerTask(" + getFullName() + ")");
        }
        catch (TaskException exc) {
            logger.error("Error initializing Task(" + getFullName() + "): " + exc, exc);
            throw exc;

        }
        catch (Exception exc) {
            logger.error("Error initializing Task(" + getFullName() + "): " + exc, exc);
            throw new TaskException("Error initializing Task(" + getFullName() + "): " + exc, exc);

        }
        finally {
            NMDC.pop();
        }
    }

    /**
     * Executes the task, preparing an <code>GVBuffer</code> object and
     * performing the required GreenVulcano workflow call.<br>
     * Post-call configured actions (if any) are performed too.
     * 
     * @see it.greenvulcano.scheduler.Task#executeTask(java.lang.String, Date,
     *      java.util.Map<java.lang.String, java.lang.String>, booolean)
     */
    @Override
    protected boolean executeTask(String name, Date fireTime, Map<String, String> locProperties, boolean isLast)
    {
        boolean cont = true;
        boolean success = false;
        int numCalls = maxCalls;
        try {
            do {
                try {
                    numCalls--;
                    Object output = null;
                    GreenVulcano greenVulcano = null;

                    // Call GreenVulcano workflow
                    try {
                        NMDC.push();
                        NMDC.setSubSystem(SUBSYSTEM);
                        NMDC.setServer(location);
                        logger.debug("BEGIN - GVServiceCallerTask(" + getFullName() + ")");

                        GVBuffer input = prepareInput(locProperties);

                        GVBufferMDC.put(input);

                        start();

                        greenVulcano = getGreenVulcano(input);
                        output = greenVulcano.forward(input, operation);

                        if (outputDirectory != null) {
                            saveOutput((GVBuffer) output);
                        }
                    }
                    catch (Exception exc) {
                        output = exc;
                    }
                    finally {
                        try {
                            greenVulcanoPool.releaseGreenVulcano(greenVulcano);
                        }
                        catch (Exception exc) {
                            // do nothing
                        }
                        if (cGVBuffer != null) {
                            cGVBuffer.cleanUp();
                        }

                        NMDC.pop();
                    }

                    // Perform post-call actions
                    ActionType action = ActionType.NO_ACTION;
                    Iterator<GVTaskAction> i = actionHandlers.iterator();
                    while (i.hasNext() && (action == ActionType.NO_ACTION)) {
                        GVTaskAction a = i.next();
                        action = a.check(output);
                        logger.debug("GVServiceCallerTask(" + getFullName()
                                + ") - Checking GVTaskAction[" + a + "]: " + output.getClass().getSimpleName() + " -> '" + action + "'");
                    }

                    if (action == ActionType.NO_ACTION) {
                        if (output instanceof Exception) {
                            logger.warn("GVServiceCallerTask(" + getFullName()
                                    + ") - Executing default GVTaskAction: Exception -> 'rollback-exit'",
                                    (Exception) output);
                            action = ActionType.RB_EXIT_ACTION;
                        }
                        else {
                            logger.debug("GVServiceCallerTask(" + getFullName()
                                    + ") - Executing default GVTaskAction: GVBuffer -> 'commit-continue'");
                            action = ActionType.CO_CONTINUE_ACTION;
                        }
                    }

                    logger.debug("GVServiceCallerTask(" + getFullName() + ") - Executing GVTaskAction: "
                            + action.toString());

                    switch (action) {
                        case CO_CONTINUE_ACTION :
                            commit();
                            cont = true;
                            success = true;
                            break;
                        case RB_EXIT_ACTION :
                            rollback();
                            cont = false;
                            success = false;
                            break;
                        case CO_EXIT_ACTION :
                            commit();
                            cont = false;
                            success = true;
                            break;
                        case RB_CONTINUE_ACTION :
                            rollback();
                            cont = true;
                            success = false;
                            break;
                        default :
                            logger.error("GVServiceCallerTask(" + getFullName() + ") - Invalid GVTaskAction type: "
                                    + action.toString());
                            cont = false;
                            rollback();
                            success = false;
                    }
                }
                finally {
                    logger.debug("END - GVServiceCallerTask(" + getFullName() + ")");
                }
            }
            while (cont && (numCalls > 0));

            if (numCalls <= 0) {
                logger.debug("GVServiceCallerTask(" + getFullName() + ") - Reached max number of calls in sequence ("
                        + maxCalls + "). Exiting.");
            }

        }
        catch (Exception exc) {
            logger.error("Error executing Task (" + getFullName() + "): " + exc, exc);
            success = false;
        }
        
        return success;
    }


    /**
     * Destroys the task, performing all the required cleanup.
     * 
     * @see it.greenvulcano.scheduler.Task#destroyTask()
     */
    @Override
    protected void destroyTask()
    {
        logger.debug("-----DESTROYING (" + getFullName() + ")");
        greenVulcanoPool = null;
        if (actionHandlers != null) {
            actionHandlers.clear();
        }
        if (cGVBuffer != null) {
            cGVBuffer.destroy();
        }
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }

    @Override
    protected boolean sendHeartBeat()
    {
        return true;
    }

    private GVBuffer prepareInput(Map<String, String> properties) throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer(GVBufferIn);
        gvBuffer.setId(new Id());
        if (cGVBuffer != null) {
            gvBuffer = cGVBuffer.execute(gvBuffer, new HashMap<String, Object>());
        }
        for (Entry<String, String> entry : properties.entrySet()) {
            gvBuffer.setProperty(entry.getKey(), entry.getValue());
        }

        return gvBuffer;
    }

    /**
     * @param GVBuffer
     * @return
     * @throws GVPublicException
     */
    private GreenVulcano getGreenVulcano(GVBuffer GVBuffer) throws GVPublicException
    {

        GreenVulcano greenVulcano = null;
        try {
            greenVulcano = greenVulcanoPool.getGreenVulcano(GVBuffer);

        }
        catch (Exception exc) {
            greenVulcano = null;
        }

        if (greenVulcano == null) {
            throw new GVPublicException("GV_GENERIC_ERROR", new String[][]{{"service", GVBuffer.getService()},
                    {"system", GVBuffer.getSystem()}, {"id", GVBuffer.getId().toString()},
                    {"message", "Timeout occurred in GreenVulcanoPool.getGreenVulcano()"}});
        }

        return greenVulcano;
    }

    /**
     * <i>Start</i> the user's transaction. Does nothing if the scheduled
     * service call is not required to be transactional.
     * 
     * @throws Exception
     *         if any error occurs
     */
    private void start() throws TaskException
    {
        if (!transacted) {
            return;
        }

        try {
            xaHelper.setTransactionTimeout(txTimeout);
            xaHelper.begin();
        }
        catch (Exception exc) {
        	logger.error("GVServiceCallerTask(" + getFullName() + ") - Error in startTx", exc);
            throw new TaskException("GVServiceCallerTask(" + getFullName() + ") - Error in startTx", exc);
        }
    }

    /**
     * <i>Commit</i> the user's transaction. Does nothing if the scheduled
     * service call is not required to be transactional.
     * 
     * @throws Exception
     *         if any error occurs
     */
    private void commit() throws TaskException
    {
        if (!transacted) {
            return;
        }

        Transaction tx = null;
        try {
            if (xaHelper.isTransactionActive()) {
                tx = xaHelper.getTransaction();
                xaHelper.commit();
            }
        }
        catch (Exception exc) {
            logger.error("GVServiceCallerTask(" + getFullName() + ") - Error in commitTx"
                    + ((tx != null) ? ": " + tx : ""), exc);
            throw new TaskException("GVServiceCallerTask(" + getFullName() + ") - Error in commitTx", exc);
        }
    }

    /**
     * <i>Rollback</i> the user's transaction. Does nothing if the scheduled
     * service call is not required to be transactional.
     * 
     * @throws Exception
     *         if any error occurs
     */
    private void rollback() throws TaskException
    {
        if (!transacted) {
            return;
        }
        Transaction tx = null;
        try {
            if (xaHelper.isTransactionActive()) {
                tx = xaHelper.getTransaction();
                xaHelper.rollback();
            }
        }
        catch (Exception exc) {
        	logger.error("GVServiceCallerTask(" + getFullName() + ") - Error in rollbackTx"
                    + ((tx != null) ? ": " + tx : ""), exc);
        }
    }

    /**
     * @param node
     * @throws TaskException
     */
    private void setActionHandlers(Node node) throws TaskException
    {
        actionHandlers = new Vector<GVTaskAction>();
        try {
            NodeList nl = XMLConfig.getNodeList(node, "NextAction");
            if ((nl != null) && (nl.getLength() > 0)) {
                for (int i = 0; i < nl.getLength(); i++) {
                    GVTaskAction a = new GVTaskAction();
                    a.init(nl.item(i));
                    logger.debug("GVServiceCallerTask(" + getFullName() + ") - Added GVTaskAction[" + a + "]");
                    actionHandlers.add(a);
                }
            }

        }
        /*catch (TaskException exc) {
            actionHandlers.clear();
            throw exc;
        }*/
        catch (Exception exc) {
            actionHandlers.clear();
            throw new TaskException("GVServiceCallerTask(" + getFullName()
                    + ") - Error occurred initializing GVTaskAction handlers", exc);
        }
    }

    /**
     * @param node
     * @throws XMLConfigException
     */
    private void configureOutput(Node node) throws XMLConfigException
    {

        String directory = XMLConfig.get(node, "@output-directory");
        outputDirectory = new File(directory);
        if (!outputDirectory.exists() || !outputDirectory.isDirectory() || !outputDirectory.canWrite()) {
            throw new XMLConfigException("GVServiceCallerTask(" + getFullName() + ") - The directory specified ("
                    + directory + ") is not valid");
        }

        outputPolicy = XMLConfig.get(node, "@output-policy");
        outputType = XMLConfig.get(node, "@output-type");
    }

    /**
     * @param gvBuffer
     */
    private void saveOutput(GVBuffer gvBuffer)
    {
        if (outputType.equals(OUTPUT_BODY) || outputType.equals(OUTPUT_BOTH)) {
            writeFile(gvBuffer.getObject(), OUTPUT_BODY);
        }

        if (outputType.equals(OUTPUT_DUMP) || outputType.equals(OUTPUT_BOTH)) {
            dumpData(gvBuffer);
        }
    }

    /**
     * @param data
     */
    private void dumpData(GVBuffer gvBuffer)
    {
        MessageFormatter formatterMessage = new MessageFormatter(gvBuffer, false);
        writeFile(formatterMessage.toString().getBytes(), OUTPUT_DUMP);
    }

    /**
     * @param buffer
     * @param type
     */
    private void writeFile(Object buffer, String type)
    {
        try {
            StringBuffer outputFileName = new StringBuffer(getFullName());
            if (outputPolicy.equals(OUTPUT_CREATE_NEW)) {
                outputFileName.append("-").append(DateUtils.nowToString(DATE_TIME_FORMAT));
            }

            if (type.equals(OUTPUT_BODY)) {
                outputFileName.append(".dat");

            }
            else {
                outputFileName.append(".dump");
            }

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(outputDirectory,
                    outputFileName.toString()), outputPolicy.equals(OUTPUT_APPEND)));

            bos.write(buffer.toString().getBytes());
            bos.flush();
            bos.close();

        }
        catch (Exception exc) {
            logger.error("Error executing Task (" + getFullName() + ")", exc);
        }
    }
}
