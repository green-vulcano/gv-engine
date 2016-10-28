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
package it.greenvulcano.gvesb.virtual.ws;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * This class is the virtual communication layer plug-in working with a Web
 * Service executing a call operation. The invocation detail can be specified in
 * the configuration file.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class WSCallOperation implements CallOperation
{
    private static final Logger logger    = org.slf4j.LoggerFactory.getLogger(WSCallOperation.class);

    private OperationKey key = null;

    /**
     * The WebServiceInvoker object
     */
    private GVWebServiceInvoker wsiObject = null;

    /**
     * Empty constructor
     *
     * @throws WSCallException
     *         if an error occurred
     */
    public WSCallOperation()
    {
        // do nothing
    }

    /**
     * The initialization method creates the WebServiceInvoker object requested.
     *
     * @param node
     *        The configuration ws-call node
     * @throws InitializationException
     *         if an error occurred
     */
    public void init(Node node) throws InitializationException
    {
        logger.debug("BEGIN INIT WSCallOperation");

        try {
            String nameWSCall = XMLConfig.get(node, "@name");
            logger.debug("Initialize the WSCall '" + nameWSCall + "'");

            Node invokerConfigNode = XMLConfig.getNode(node, "*[@type='invoker']");
            String className = XMLConfig.get(invokerConfigNode, "@class");
            Class<?> wsiObjectClass = Class.forName(className);

            wsiObject = (GVWebServiceInvoker) wsiObjectClass.newInstance();
            wsiObject.init(invokerConfigNode);
        }
        catch (Exception exc) {
            logger.error("An error occurred initializing the WS Call Operation", exc);
            throw new InitializationException("GVVCL_XML_CONFIG_ERROR", new String[][]{{"exc", "" + exc}}, exc);
        }

        logger.debug("END INIT WSCallOperation");
    }

    /**
     * This method execute the Web Service call operation
     *
     * @param gvBuffer
     *        The GVBuffer object input
     * @return The GVBuffer object output
     * @throws WSCallException
     *         If an error occurred in the ws-call execution
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws WSCallException
    {
        try {
            return wsiObject.perform(gvBuffer);
        }
        catch (Exception exc) {
            throw new WSCallException("GVVCL_WS_ERROR", new String[][]{{"exception", "" + exc}}, exc);
        }
    }


    /**
     * Do Nothing
     *
     * @param gvBuffer
     *        The GVBuffer object
     * @return null
     */
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        wsiObject.cleanUp();
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
        wsiObject.destroy();
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    @Override
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    @Override
    public OperationKey getKey()
    {
        return key;
    }
}