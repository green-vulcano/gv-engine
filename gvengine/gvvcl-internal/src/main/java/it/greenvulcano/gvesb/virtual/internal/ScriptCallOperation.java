/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.virtual.internal;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.script.ScriptExecutor;
import it.greenvulcano.script.ScriptExecutorFactory;
import it.greenvulcano.util.thread.ThreadUtils;

import org.w3c.dom.Node;


/**
 * 
 * ScriptCallOperation class
 * 
 * @version 3.5.0 08/ago/2014
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class ScriptCallOperation implements CallOperation
{
    /**
     * The operation key.
     */
    protected OperationKey key    = null;

    private ScriptExecutor script = null;


    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws InitializationException {
        try {
            script = ScriptExecutorFactory.createSE(XMLConfig.getNode(node, "Script"));
        }
        catch (Exception exc) {
            throw new InitializationException("GVVCL_SCRIPT_INIT_ERROR", new String[][]{{"node",
                    node.getLocalName()}}, exc);
        }
    }

    /**
     * Executes the operation.
     * 
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException,
            InterruptedException {
        try {
            script.putProperty("data", gvBuffer);
            script.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true), gvBuffer);
            return gvBuffer;
        }
        catch (Exception exc) {
            ThreadUtils.checkInterrupted(exc);
            throw new CallException("GVVCL_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    public void cleanUp() {
        if (script != null) {
            script.cleanUp();
        }
    }

    /**
     * Called when an operation is discarded from cache.
     */
    public void destroy() {
        if (script != null) {
            script.destroy();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    public void setKey(OperationKey key) {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    public OperationKey getKey() {
        return key;
    }

    /**
     * Return the alias for the given service
     * 
     * @param gvBuffer
     *        the input service data
     * @return the configured alias
     */
    public String getServiceAlias(GVBuffer gvBuffer) {
        return gvBuffer.getService();
    }
}
