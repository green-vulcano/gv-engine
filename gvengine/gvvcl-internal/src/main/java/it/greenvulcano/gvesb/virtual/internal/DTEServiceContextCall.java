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
package it.greenvulcano.gvesb.virtual.internal;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvdte.controller.DTEController;
import it.greenvulcano.gvesb.internal.InvocationContext;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

/**
 *
 * @version 3.0.0 Apr 26, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DTEServiceContextCall implements CallOperation
{
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DTEServiceContextCall.class);

    /**
     *
     */
    protected OperationKey      key    = null;

    /**
     *
     * @param node
     * @throws InitializationException
     */
    public void init(Node node) throws InitializationException
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws CallException, InterruptedException
    {
        if (gvBuffer == null) {
            return null;
        }

        Object output = null;

        logger.debug("transform - start");
        try {
            String name = gvBuffer.getProperty("map-name");
            logger.debug("transformation name: " + name);
            logger.debug("request ID: " + gvBuffer.getId().toString());

            Object input = gvBuffer.getObject();
            output = ((DTEController) InvocationContext.getInstance().getExtraField("DTE_CONTROLLER")).transform(name,
                    input, buildMapParam(gvBuffer));
            gvBuffer.setObject(output);
        }
        catch (InterruptedException exc) {
            logger.error("DTE interrupted", exc);
            throw exc;
        }
        catch (Exception exc) {
            logger.error("DTE Error: ", exc);
            throw new CallException("GVVCL_DTE_PERFORM_ERROR", new String[][]{{"exc", exc.toString()}}, exc);
        }
        logger.debug("transform - stop");
        return gvBuffer;
    }


    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    public OperationKey getKey()
    {
        return key;
    }

    /**
     * Called when an operation is discarded from cache.
     */
    public void destroy()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    public void cleanUp()
    {
        // do nothing
    }

    /**
     * Return the alias for the given service.
     *
     * @param gvBuffer
     *        the input service GVBuffer
     * @return the configured alias
     */
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

    /**
     * Extract from data the transformation parameters
     *
     * @param gvBuffer
     *        the input data
     * @return an HashMap containing the parameters name:value
     */
    private Map<String, Object> buildMapParam(GVBuffer gvBuffer)
    {
        String parameters = "Transformation parameters:";
        Map<String, Object> hashMapParam = new HashMap<String, Object>();
        hashMapParam.put("SYSTEM", gvBuffer.getSystem());
        hashMapParam.put("SERVICE", gvBuffer.getService());
        hashMapParam.put("ID", gvBuffer.getId().toString());
        hashMapParam.put("RET_CODE", "" + gvBuffer.getRetCode());
        for (String name : gvBuffer.getPropertyNames()) {
            String value = gvBuffer.getProperty(name);
            parameters += "\n" + name + "=" + value;
            hashMapParam.put(name, value);
        }
        logger.debug(parameters);
        return hashMapParam;
    }
}
