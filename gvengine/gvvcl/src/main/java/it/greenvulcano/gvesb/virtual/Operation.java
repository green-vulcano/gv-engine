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
package it.greenvulcano.gvesb.virtual;

import it.greenvulcano.gvesb.buffer.GVBuffer;

import org.w3c.dom.Node;

/**
 * <code>Operation</code> interface defines methods of all GVVCL objects that
 * <code>OperationFactory</code> and <code>OperationManager</code> manages.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface Operation
{
    /**
     * Invoked from <code>OperationFactory</code> when an <code>Operation</code>
     * is initialized.<br>
     *
     * @param node
     *        configuration node. The operation should use this node with
     *        <code>XMLConfig</code> in order to read its configuration
     *        parameters.
     *
     * @exception InitializationException
     *            if an error occurs during initialization
     *
     */
    void init(Node node) throws InitializationException;

    /**
     * Execute the operation using an <code>GVBuffer</code>. Usually this method
     * is used in order to call external systems.
     *
     * @param gvBuffer
     *        input data for the operation.
     *
     * @return an <code>GVBuffer</code> containing the operation result.
     *
     * @exception ConnectionException
     *            if an error occurs making the connection to the external
     *            system.
     *
     * @exception VCLException
     *            if an error occurs performing the operation.
     *
     * @exception InvalidDataException
     *            if the call fail because the data are invalid. GreenVulcano
     *            ESB should not retry to perform the operation with the same
     *            data.
     *
     * @see it.greenvulcano.gvesb.buffer.GVBuffer
     */
    GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, VCLException, InvalidDataException, 
                                               InterruptedException;

    /**
     * Must be always called after 'perform' for execute
     * plug-in specific clean up operation
     */
    void cleanUp();

    /**
     * Called when an operation is discarded from cache. Here the implementation
     * should release allocated resources.
     */
    void destroy();

    /**
     * Set the Operation key
     *
     * @param key
     *        the key to set
     */
    void setKey(OperationKey key);

    /**
     *
     * @return the operation key
     */
    OperationKey getKey();

    /**
     * Return the alias for the given service
     *
     * @param gvBuffer
     *        the input service GVBuffer
     * @return the configured alias
     */
    public String getServiceAlias(GVBuffer gvBuffer);
}
