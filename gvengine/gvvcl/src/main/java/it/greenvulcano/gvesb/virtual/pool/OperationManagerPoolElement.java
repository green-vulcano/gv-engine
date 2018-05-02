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
package it.greenvulcano.gvesb.virtual.pool;

import it.greenvulcano.gvesb.virtual.Operation;
import it.greenvulcano.gvesb.virtual.OperationFactory;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.gvesb.virtual.VCLException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

/**
 * OperationManagerPoolElement class.
 * 
 * @version 3.2.0 Feb 28, 2011
 * @author GreenVulcano Developer Team
 * 
 */
public class OperationManagerPoolElement
{
    /**
     * The GVLogger instance
     */
    private static final Logger              logger        = org.slf4j.LoggerFactory.getLogger(OperationManagerPoolElement.class);

    private ConcurrentLinkedQueue<Operation> vclOps        = new ConcurrentLinkedQueue<Operation>();
    private Set<Operation>                   vclOpsManaged = Collections.synchronizedSet(new HashSet<Operation>());
    private Set<Operation>                   vclOpsInUse   = Collections.synchronizedSet(new HashSet<Operation>());
    private OperationKey                     key           = null;
    private String                           type          = null;
    private AtomicBoolean                    valid         = new AtomicBoolean(true);

    /**
     *
     */
    public OperationManagerPoolElement(OperationKey key, String type)
    {
        this.key = key;
        this.type = type;
        logger.debug("OperationManagerPoolElement created for key: " + key);
    }


    /**
     *
     */
    public boolean checkOperation() throws VCLException
    {
        if (!valid.get()) {
            throw new VCLException("GVVCL_INVALID_POOL_STATE", new String[][]{{"key", key.toString()}});
        }

        // First check if the requested operation is already in cache
        Operation operation = vclOps.peek();

        // OK. The operation is in cache, so we return true.
        if (operation != null) {
            logger.debug("OperationManagerPoolElement - CHECK - found Operation instance in pool for key: " + key);
            return true;
        }

        logger.debug("OperationManagerPoolElement - CHECK - not found Operation instance in pool for key: " + key);

        // No, the operation is not in cache, so we ask the OperationFactory
        // in order to create it.
        operation = OperationFactory.createOperation(key, type);

        vclOpsManaged.add(operation);
        vclOps.add(operation);

        return true;
    }

    /**
     *
     */
    public Operation getOperation() throws VCLException
    {
        try {
            if (!valid.get()) {
                throw new VCLException("GVVCL_INVALID_POOL_STATE", new String[][]{{"key", key.toString()}});
            }

            // First check if the requested operation is already in cache
            Operation operation = vclOps.poll();

            // OK. The operation is in cache, so we return it.
            if (operation != null) {
                logger.debug("OperationManagerPoolElement - GET - found Operation instance in pool for key: " + key);
                vclOpsInUse.add(operation);
                return operation;
            }

            logger.debug("OperationManagerPoolElement - GET - not found Operation instance in pool for key: " + key);

            // No, the operation is not in cache, so we ask the OperationFactory
            // in order to create it.
            operation = OperationFactory.createOperation(key, type);

            vclOpsManaged.add(operation);
            vclOpsInUse.add(operation);

            // Return the operation
            return operation;
        }
        catch (VCLException exc) {
            logger.debug("OperationManagerPoolElement - GET - Error obtaining Operation for key: " + key, exc);
            throw exc;
        }
    }

    /**
     * 
     * @param operation
     */
    public void releaseOperation(Operation operation)
    {
        if (operation != null) {
            if (valid.get()) {
                if (vclOpsManaged.contains(operation)) {
                    if (vclOpsInUse.contains(operation)) {
                        logger.debug("OperationManagerPoolElement - releasing instance in pool for key: " + key);
                        vclOps.add(operation);
                        vclOpsInUse.remove(operation);
                    }
                    else {
                        logger.warn("OperationManagerPoolElement - releasing already released instance in pool for key: " + key);
                    }
                    return;
                }
            }
            else {
                vclOpsManaged.remove(operation);
                vclOpsInUse.remove(operation);
            }

            try {
                logger.debug("Destroy: " + operation);
                operation.destroy();
            }
            catch (Throwable exc) {
                logger.warn("Error destroying Operation " + operation, exc);
            }
        }
    }

    /**
     *
     */
    public void destroy()
    {
        logger.debug("BEGIN discarding " + key);
        try {
            valid.set(false);
            synchronized (vclOpsManaged) {
                vclOps.clear();
                vclOpsInUse.clear();
                for (Iterator<Operation> iterator = vclOpsManaged.iterator(); iterator.hasNext();) {
                    Operation operation = iterator.next();
                    iterator.remove();
                    try {
                        logger.debug("Destroy: " + operation);
                        operation.destroy();
                    }
                    catch (Throwable exc) {
                        logger.warn("Error destroying Operation " + operation, exc);
                    }
                }
            }
        }
        finally {
            logger.debug("END discarding " + key);
        }
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer("OperationManagerPoolElement Key[").append(key).append("]");
        int man = vclOpsManaged.size();
        int pol = vclOps.size();
        int use = vclOpsInUse.size();
        sb.append("\nManaged: ").append(man);
        sb.append("\nPooled : ").append(pol);
        sb.append("\nInUse  : ").append(use);
        return sb.toString();
    }
}
