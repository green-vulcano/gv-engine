/*
 * Copyright (c) 2009-2011 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.virtual.pool;

import it.greenvulcano.gvesb.virtual.Operation;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Synchronization;
import javax.transaction.Transaction;

/**
 *
 * VCL XA Synchronization class
 *
 * @version 3.2.0 Feb 28, 2011
 * @author GreenVulcano Developer Team
 *
 */
public class VCLXASynchronization implements Synchronization
{
    private static final org.slf4j.Logger logger        = org.slf4j.LoggerFactory.getLogger(OperationManagerPool.class);

    private List<Operation>     vclOperations = new ArrayList<Operation>();
    private Transaction         transaction   = null;

    /**
     * Constructor
     *
     * @param transaction
     */
    public VCLXASynchronization(Transaction transaction)
    {
        this.transaction = transaction;
    }

    /**
     *
     * @param operation
     */
    public void add(Operation operation)
    {
        logger.debug("Added Operation [" + operation.getKey() + "] to Transaction [" + transaction + "]");
        vclOperations.add(operation);
    }

    /**
     * @see javax.transaction.Synchronization#beforeCompletion()
     */
    @Override
    public void beforeCompletion()
    {
        // TODO Auto-generated method stub
    }

    /**
     * @see javax.transaction.Synchronization#afterCompletion(int)
     */
    @Override
    public void afterCompletion(int status)
    {
        Transaction localTx = transaction;
        logger.debug("Closing Transaction of VCLXASynchronization [" + localTx + "]");
        OperationManagerPool.instance().xaReleaseOperation(this);
        logger.debug("Closed Transaction of VCLXASynchronization [" + localTx + "]");
    }

    /**
     * @return Returns the VCL Operations.
     */
    public List<Operation> getVCLOperations()
    {
        return vclOperations;
    }

    /**
     * @return Returns the transaction.
     */
    public Transaction getTransaction()
    {
        return transaction;
    }

    /**
     *
     */
    public void destroy()
    {
        transaction = null;
        vclOperations.clear();
        vclOperations = null;
    }

}
