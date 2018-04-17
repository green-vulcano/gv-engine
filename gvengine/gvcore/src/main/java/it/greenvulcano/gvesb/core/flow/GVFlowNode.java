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
import it.greenvulcano.gvesb.log.GVBufferDump;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * GVFlowNode base class.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public abstract class GVFlowNode implements GVFlowNodeIF
{
    /**
     * the flow node id
     */
    private String  id                     = "";
    /**
     * the input object name
     */
    private String  input                  = "";
    /**
     * the output object name
     */
    private String  output                 = "";
    /**
     * if true the flow node terminate a business flow
     */
    private boolean businessFlowTerminated = false;
    /**
     * dump input/output
     */
    private boolean dumpInOut              = false;
    /**
     * dump environment input/output
     */
    private boolean dumpEnvInOut           = false;

    /**
     * Initialize the instance
     *
     * @param defNode
     *        the flow node definition
     * @throws GVCoreConfException
     *         if errors occurs
     */
    public void init(Node defNode) throws GVCoreConfException
    {
        try {
            id = XMLConfig.get(defNode, "@id");
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'id'"},
                    {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }
        input = XMLConfig.get(defNode, "@input", "");
        output = XMLConfig.get(defNode, "@output", input);

        if (input.equals("") && output.equals("")) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{
                    {"name", "'input' or 'output'"}, {"node", XPathFinder.buildXPath(defNode)}});
        }

        dumpInOut = XMLConfig.getBoolean(defNode, "@dump-in-out", false);
        dumpEnvInOut = XMLConfig.getBoolean(defNode, "@dump-env-in-out", false);
    }

    /**
     * @return the flow node id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @return the input object name
     */
    public String getInput()
    {
        return input;
    }

    /**
     * @return the output object name
     */
    public String getOutput()
    {
        return output;
    }

    /**
     * @return the node default nextNodeID
     */
    public abstract String getDefaultNextNodeId();

    /**
     * @return the flag value
     */
    public boolean isBusinessFlowTerminated()
    {
        return businessFlowTerminated;
    }

    /**
     * @param b
     *        the flag value
     */
    public void setBusinessFlowTerminated(boolean b)
    {
        businessFlowTerminated = b;
    }

    /**
     * Perform the flow node work
     *
     * @param environment
     *        the flow execution environment
     * @return the next flow node id
     * @throws GVCoreException
     *         if errors occurs
     * @throws InterruptedException
     *         if the current Thread is interrupted
     */
    public String execute(Map<String, Object> environment) throws GVCoreException, InterruptedException {
        return execute(environment, false);
    }

    /**
     * Perform the flow node work
     *
     * @param environment
     *        the flow execution environment
     * @return the next flow node id
     * @throws GVCoreException
     *         if errors occurs
     * @throws InterruptedException
     *         if the current Thread is interrupted
     */
    public abstract String execute(Map<String, Object> environment, boolean onDebug) throws GVCoreException, 
        InterruptedException;

    /**
     * @return if GVBuffer should be dumped for input and output logging
     */
    public boolean isDumpInOut()
    {
        return this.dumpInOut;
    }

    /**
     * @return if input and output Execution Environment should be dumped for
     *         logging
     */
    public boolean isDumpEnvInOut()
    {
        return this.dumpEnvInOut;
    }

    /**
     * Perform the flow node cleanup operation
     *
     * @throws GVCoreException
     *         if errors occurs
     */
    public abstract void cleanUp() throws GVCoreException;

    /**
     * Perform the flow node destroy operation
     *
     * @throws GVCoreException
     *         if errors occurs
     */
    public abstract void destroy() throws GVCoreException;

    /**
     * 
     * @return
     *        the current Thread interrupted state
     */
    public boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    public void checkInterrupted(String type, Logger logger) throws InterruptedException {
        ThreadUtils.checkInterrupted(type, getId(), logger);
    }
    
    /**
     * @param logger
     * @param isInput
     * @param environment
     */
    protected void dumpEnvironment(Logger logger, boolean isInput, Map<String, Object> environment)
    {
        if (isDumpEnvInOut()) {
            StringBuffer msg = new StringBuffer(10000);
            if (isInput) {
                msg.append("\nBEGIN INPUT - Node[" + getId() + "] Execution Environment dump\n");
            }
            else {
                msg.append("\nBEGIN OUTPUT - Node[" + getId() + "] Execution Environment dump\n");
            }
            for (Map.Entry<String, Object> element : environment.entrySet()) {
                Object value = element.getValue();
                if (value instanceof GVBuffer) {
                    msg.append("***** Entry [").append(element.getKey()).append("] of class: ").append(
                            value.getClass().getName()).append("\n").append(new GVBufferDump((GVBuffer) value)).append(
                            "\n\n");
                }
                else if (value instanceof Throwable) {
                    msg.append("***** Entry [").append(element.getKey()).append("] of class: ").append(
                            value.getClass().getName()).append("\n").append(value).append("\n").append(
                            ThreadUtils.getStackTrace((Throwable) value)).append("\n\n");
                }
                else if (value == null) {
                    msg.append("***** Entry [").append(element.getKey()).append("] IS NULL\n\n");
                }
                else {
                    msg.append("***** Entry [").append(element.getKey()).append("] of class: ").append(
                            value.getClass().getName()).append("\n").append(value).append("\n\n");
                }
            }
            if (isInput) {
                msg.append("END INPUT - Node[" + getId() + "] Execution Environment dump\n");
            }
            else {
                msg.append("END OUTPUT - Node[" + getId() + "] Execution Environment dump\n");
            }
            logger.info(msg.toString());
        }
    }

}