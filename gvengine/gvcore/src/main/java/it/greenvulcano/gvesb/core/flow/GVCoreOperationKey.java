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
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.DequeueOperation;
import it.greenvulcano.gvesb.virtual.EnqueueOperation;
import it.greenvulcano.gvesb.virtual.OperationKey;
import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * <code>GVCoreOperationKey</code> represents the OperationKey to use for
 * retrieving and create operations available at Virtual Communication Layer
 * level. The key is build concatenating the fileName and the absolute XPath of
 * the node from which the configuration item will be initialized.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public class GVCoreOperationKey extends OperationKey
{
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(GVCoreOperationKey.class);

    /**
     * The configuration fileName name where the configuration for the Virtual
     * Communication Layer operations are stored.
     */
    private String              fileName   = null;

    /**
     * The XPath used to build up the key to use against the OperationManager to
     * return the node of the Operation to initialize.
     */
    private String              xpath  = null;

    private Node                operationNode = null;
    /**
     * The key of the operation: in our case it is the "fileName:XPath" string of
     * the node representing the Virtual Communication Layer Operation.
     */
    private String              key    = null;
    /**
     * the VCLOperation type
     */
    private String              type   = "";

    public GVCoreOperationKey()
    {
        // do nothing
    }

    /**
     * This constructor initializes the key.
     *
     * @param fileNameN
     *        The file name containing the configuration for the Virtual
     *        Communication Layer operation
     * @param xPath
     *        The XPath to point to the configuration node representing the
     *        operation
     */
    public GVCoreOperationKey(String fileNameN, String xPath)
    {
        init(fileNameN, xPath);
    }

    /**
     * Initialize the operation key building a unique key to use to identify the
     * Virtual Communication Layer operations.
     *
     * @param fileNameN
     *        The file name containing the configuration for the Virtual
     *        Communication Layer operation
     * @param xPath
     *        The XPath to point to the configuration node representing the
     *        operation
     */
    public void init(String fileNameN, String xPath)
    {
        fileName = fileNameN;
        logger.debug("fileName: (" + fileName + ")");

        xpath = xPath;
        logger.debug("xpath: (" + xpath + ")");
        key = fileName + ":" + xpath;
        logger.debug("key: (" + key + ")");

        try {
            String opType = XMLConfig.get(getNode(), "@type");
            if (opType.equals("call")) {
                type = CallOperation.TYPE;
            }
            else if (opType.equals("enqueue")) {
                type = EnqueueOperation.TYPE;
            }
            else if (opType.equals("dequeue")) {
                type = DequeueOperation.TYPE;
            }
        }
        catch (Exception exc) {
            type = "invalid-operation-type";
        }
    }

    /**
     * Get the key of the operation. <br/>
     *
     * @return The key of the operation
     */
    @Override
    public String getKey()
    {
        return key;
    }

    /**
     * Get the name of the file name from which the configuration for the operations
     * has been loaded.
     *
     * @return The key of the operation
     */
    @Override
    public String getFile()
    {
        return fileName;
    }

    /**
     * @return the VCLOperation type
     */
    public String getType()
    {
        return type;
    }

    /**
     * Get the node containing the configuration of the Virtual Communication Layer operation.
     *
     * @return The node containing the configuration for the operation
     * @throws GVCoreException
     *         if an error occurs
     */
    @Override
    public Node getNode() throws GVCoreException
    {
        if (operationNode == null) {
            try {
                operationNode = XMLConfig.getNode(fileName, xpath);
            }
            catch (XMLConfigException exc) {
                logger.error("Error finding VCLOperation node : ", exc);
                throw new GVCoreException("GVCORE_VCL_OPERATION_SEARCH_ERROR", new String[][] { { "id", "(-)" },
                        { "node", "(-)" }, { "xpath", xpath } }, exc);
            }
            if (operationNode == null) {
                logger.error("Error finding VCLOperation node : " + xpath + " invalid.");
                throw new GVCoreException("GVCORE_VCL_OPERATION_SEARCH_ERROR", new String[][] { { "id", "(-)" },
                        { "node", "(-)" }, { "xpath", xpath } });
            }
        }
        return operationNode;
    }

    /**
     * Provide a message to identify the Core Operation Key.
     *
     * @return The key of the operation in string form
     */
    @Override
    public String toString()
    {
        return this.getClass() + "[key = '" + getKey() + "']";
    }
}