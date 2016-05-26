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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import java.util.LinkedHashSet;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class RoutedEnqueue implements EnqueueOperation
{
    private static final org.slf4j.Logger      logger             = org.slf4j.LoggerFactory.getLogger(RoutedEnqueue.class);

    /**
     *
     */
    protected OperationKey           key                = null;

    private Vector<Routing>          routingVector      = new Vector<Routing>();
    private LinkedHashSet<Operation> performedOperation = new LinkedHashSet<Operation>();
    private String                   name               = "";

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, EnqueueException, InvalidDataException, InterruptedException
    {
        EnqueueOperation operation = getOperation(gvBuffer);

        GVBuffer outData = operation.perform(gvBuffer);

        return outData;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws InitializationException
    {
        name = XMLConfig.get(node, "@name", "undefined");

        NodeList nl = null;
        try {
            nl = XMLConfig.getNodeList(node, "VCLRouting");
        }
        catch (XMLConfigException exc) {
            // do nothing
        }

        if ((nl == null) || (nl.getLength() == 0)) {
            throw new InitializationException("GVVCL_BAD_ROUTING_CFG_ERROR", new String[][]{{"name", name}});
        }

        try {
            for (int i = 0; i < nl.getLength(); i++) {
                Routing routing = new Routing();
                routing.init(nl.item(i));
                routingVector.add(routing);
            }
        }
        catch (VCLException exc) {
            logger.error("Error initializing Routing : ", exc);
            throw new InitializationException("GVVCL_ROUTING_INIT_ERROR", new String[][]{{"name", name}});
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    public void cleanUp()
    {
        for (Operation op : performedOperation) {
            op.cleanUp();
        }
        performedOperation.clear();
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    public void destroy()
    {
        for (Routing routing : routingVector) {
            routing.destroy();
        }
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

    private EnqueueOperation getOperation(GVBuffer gvBuffer) throws EnqueueException, ConnectionException
    {
        EnqueueOperation operation = null;
        int i = 0;

        while ((operation == null) && (i < routingVector.size())) {
            operation = (EnqueueOperation) (routingVector.elementAt(i)).getOperation(gvBuffer);
            i++;
        }
        if (operation == null) {
            throw new EnqueueException("GVVCL_BAD_ROUTING_CFG_ERROR", new String[][]{{"name", name}});
        }
        performedOperation.add(operation);
        return operation;
    }

    /**
     * Return the alias for the given service
     *
     * @param gvBuffer
     *        the input service data
     * @return the configured alias
     */
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        try {
            EnqueueOperation op = getOperation(gvBuffer);
            return op.getServiceAlias(gvBuffer);
        }
        catch (Exception exc) {
            return gvBuffer.getService();
        }
    }
}
