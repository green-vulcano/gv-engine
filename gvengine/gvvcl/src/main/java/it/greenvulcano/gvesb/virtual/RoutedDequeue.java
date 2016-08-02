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
package it.greenvulcano.gvesb.virtual;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.Id;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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
public class RoutedDequeue implements DequeueOperation
{
    private static final org.slf4j.Logger      logger             = org.slf4j.LoggerFactory.getLogger(RoutedDequeue.class);

    /**
     *
     */
    protected OperationKey           key                = null;

    private Vector<Routing>          routingVector      = new Vector<Routing>();
    private LinkedHashSet<Operation> performedOperation = new LinkedHashSet<Operation>();
    private Map<String, Operation>   idToOperationMap  = new HashMap<String, Operation>();
    private String                   name               = "";
    private String                   filter             = "";
    private long                     timeout            = 0;

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, DequeueException, InvalidDataException, InterruptedException
    {
        DequeueOperation operation = getOperation(gvBuffer);
        try {
            if (!filter.equals("")) {
                operation.setFilter(filter);
            }
        }
        catch (FilterException exc) {
            logger.error("Error setting filter : ", exc);
            throw new DequeueException("GVVCL_FILTER_ERROR", new String[][]{{"name", name}});
        }
        if (timeout >= -1) {
            operation.setTimeout(timeout);
        }

        GVBuffer outData = operation.perform(gvBuffer);

        filter = "";
        timeout = -2;

        idToOperationMap.put(outData.getId().toString(), operation);

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
     * @see it.greenvulcano.gvesb.virtual.DequeueOperation#acknowledge(it.greenvulcano.gvesb.buffer.Id)
     */
    public void acknowledge(Id id) throws ConnectionException, AcknowledgeException
    {
        DequeueOperation operation = (DequeueOperation) idToOperationMap.get(id.toString());
        if (operation == null) {
            throw new AcknowledgeException("GVVCL_ACKNOWLEDGE_ERROR", new String[][]{{"tid", id.toString()}});
        }
        operation.acknowledge(id);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.DequeueOperation#acknowledgeAll()
     */
    public void acknowledgeAll() throws ConnectionException, AcknowledgeException
    {
        for (String id : idToOperationMap.keySet()) {
            DequeueOperation operation = (DequeueOperation) idToOperationMap.get(id);
            if (operation != null) {
                try {
                    operation.acknowledgeAll();
                }
                catch (VCLException exc) {
                    // nothing
                }
                finally {
                    idToOperationMap.remove(id);
                }
            }
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.DequeueOperation#rollback(it.greenvulcano.gvesb.buffer.Id)
     */
    public void rollback(Id id) throws ConnectionException, AcknowledgeException
    {
        DequeueOperation operation = (DequeueOperation) idToOperationMap.get(id.toString());
        if (operation != null) {
            operation.acknowledge(id);
            idToOperationMap.remove(id.toString());
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.DequeueOperation#rollbackAll()
     */
    public void rollbackAll() throws ConnectionException, AcknowledgeException
    {
        for (String id : idToOperationMap.keySet()) {
            DequeueOperation operation = (DequeueOperation) idToOperationMap.get(id);
            if (operation != null) {
                operation.rollbackAll();
            }
        }
        idToOperationMap.clear();
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.DequeueOperation#setFilter(java.lang.String)
     */
    public void setFilter(String filter) throws FilterException
    {
        this.filter = filter;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.DequeueOperation#setTimeout(long)
     */
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
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

    private DequeueOperation getOperation(GVBuffer data) throws DequeueException, ConnectionException
    {
        DequeueOperation operation = null;
        int i = 0;

        while ((operation == null) && (i < routingVector.size())) {
            operation = (DequeueOperation) (routingVector.elementAt(i)).getOperation(data);
            i++;
        }
        if (operation == null) {
            throw new DequeueException("GVVCL_BAD_ROUTING_CFG_ERROR", new String[][]{{"name", name}});
        }
        performedOperation.add(operation);
        return operation;
    }

    /**
     * Return the alias for the given service
     *
     * @param data
     *        the input service data
     * @return the configured alias
     */
    public String getServiceAlias(GVBuffer data)
    {
        try {
            DequeueOperation op = getOperation(data);
            return op.getServiceAlias(data);
        }
        catch (Exception exc) {
            return data.getService();
        }
    }
}
