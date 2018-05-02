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
package it.greenvulcano.gvesb.j2ee;

import it.greenvulcano.configuration.XMLConfig;

import javax.naming.NameNotFoundException;
import javax.naming.NoInitialContextException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * XAHelper class
 * 
 * @version 4.0.0 Mar 19, 2018
 * @author GreenVulcano Developer Team
 */
public class XAHelper {
	
	public static final String DEFAULT_JDNI_NAME = "osgi:service/javax.transaction.TransactionManager";
    /**
     * The Logger instance must be provided by the caller.
     */
    private Logger             logger                  = null;

    /**
     * Helper class for JNDI operation.
     */
    private JNDIHelper         initialContext          = null;
    /**
     * Reference to Application Server TransactionManager instance.
     */
    private TransactionManager tManager                = null;
    
    /**
     * Transaction status to use on resource delist.
     */
    private int                tStatus                 = XAResource.TMSUCCESS;
    /**
     * Last resource enlisted.
     */
    private XAResource         lastXAResource          = null;
    /**
     * If true the Application server auto-enlist the resources.
     */
    private boolean            autoEnlist              = true;
    /**
     * If true the last enlist operation have succeeded.
     */
    private boolean            lastEnlisted            = false;

    private boolean            noXA                    = false;

    private String             transactionManagerJNDI;
   
    public XAHelper() {
        initialContext = new JNDIHelper();        
    }
    
    public XAHelper(String transactionManagerJNDIName) {
        initialContext = new JNDIHelper();
        transactionManagerJNDI = transactionManagerJNDIName;
    }
    
    /**
     * Constructor.
     * 
     * @param node
     *        the node from which read configuration data
     * @throws XAHelperException
     *         if initialization error occurs
     */
    public XAHelper(Node node) throws XAHelperException
    {
        if (node != null) {
            try {
                initialContext = new JNDIHelper(XMLConfig.getNode(node, "JNDIHelper"));
            }
            catch (Exception exc) {
                throw new XAHelperException("J2EE_XAHELPER_INIT_ERROR", new String[][]{{"cause", exc.getMessage()}},
                        exc);
            }
            autoEnlist = XMLConfig.getBoolean(node, "@auto-enlist", true);
            String sTStatus = XMLConfig.get(node, "@transaction-status", "TMSUCCESS");
            transactionManagerJNDI = XMLConfig.get(node, "@transaction-manager-jndi", DEFAULT_JDNI_NAME);
           
            if (!sTStatus.equals("TMSUCCESS")) {
                tStatus = XAResource.TMFAIL;
            }
        }
        else {
            initialContext = new JNDIHelper();
        }
    }
    
    public void setTransactionManagerJNDI(String transactionManagerJNDI) {
		this.transactionManagerJNDI = transactionManagerJNDI;
	}

    /**
     * Initialize the instance.
     * 
     * @throws XAHelperException
     *         if initialization error occurs
     */
    private void init() throws XAHelperException
    {
        if (tManager != null) {
        	return;
        }
        
        if (transactionManagerJNDI!=null && initialContext!=null) {
	        try {        	
	        	tManager = (TransactionManager) initialContext.lookup(transactionManagerJNDI);
	        } catch (NameNotFoundException|NoInitialContextException exc) {
	        	logger.error("TransactionManager lookup failed: "+transactionManagerJNDI);
	            noXA = true;
	        } catch (Exception exc) {
	            throw new XAHelperException("J2EE_XAHELPER_INIT_ERROR", new String[][]{{"cause", exc.getMessage()}}, exc);
	        } 
        } else {
        	 noXA = true;
        }
    }

    /**
     * @return the TransactionManager
     * @throws XAHelperException
     */
    public TransactionManager getTransactionManager() throws XAHelperException
    {
        init();
        return this.tManager;
    }

    /**
     * @param logger
     *        the Logger instance to use
     */
    public final void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    /**
     * @return the transaction status
     * @throws XAHelperException
     *         if error occurs
     */
    public final boolean isTransactionActive() throws XAHelperException {
        init();
        if (noXA) {
            return false;
        }
        try {
            Transaction transaction = tManager.getTransaction();
            if (transaction == null) {
                debug("Transaction is null");
                return false;
            }
            int status = transaction.getStatus();
            debug("Transaction status == " + status + " - is active == " + (status != Status.STATUS_NO_TRANSACTION)
                    + " - Transaction: [" + transaction.hashCode() + "] " + transaction);
            return (status != Status.STATUS_NO_TRANSACTION);
        }
        catch (Exception exc) {
            error("Error testing transaction status ", exc);
            throw new XAHelperException("J2EE_XAHELPER_ERROR", new String[][]{{"cause", exc.getMessage()}}, exc);
        }
    }

    /**
     * @return the transaction status
     * @throws XAHelperException
     *         if error occurs
     */
    public final boolean isTransactionRunning() throws XAHelperException {
        init();
        if (noXA) {
            return false;
        }
        try {
            Transaction transaction = tManager.getTransaction();
            if (transaction == null) {
                debug("Transaction is null");
                return false;
            }
            int status = transaction.getStatus();
            debug("Transaction status == " + status + " - is running == " + (status != Status.STATUS_ACTIVE)
                    + " - Transaction: [" + transaction.hashCode() + "] " + transaction);
            return (status == Status.STATUS_ACTIVE);
        }
        catch (Exception exc) {
            error("Error testing transaction status ", exc);
            throw new XAHelperException("J2EE_XAHELPER_ERROR", new String[][]{{"cause", exc.getMessage()}}, exc);
        }
    }

    /**
     * @return the active transaction
     * @throws XAHelperException
     *         if error occurs
     */
    public final Transaction getTransaction() throws XAHelperException {
        init();
        if (noXA) {
            return null;
        }
        try {
            Transaction transaction = tManager.getTransaction();
            if (transaction == null) {
                debug("Transaction not active");
                return null;
            }
            debug("Transaction active: [" + transaction.hashCode() + "] " + transaction);
            return transaction;
        }
        catch (Exception exc) {
            error("Error extracting transaction ", exc);
            throw new XAHelperException("J2EE_XAHELPER_ERROR", new String[][]{{"cause", exc.getMessage()}}, exc);
        }
    }

    /**
     * Enlist the given resource in the current transaction.
     * 
     * @param xaRes
     *        the resource to enlist
     * @return the operation status
     * @throws XAHelperException
     *         if error occurs
     */
    public final boolean enlistResource(XAResource xaRes) throws XAHelperException {
        debug("Enlisting resource: " + xaRes);

        if (xaRes == null) {
            throw new XAHelperException("J2EE_BAD_XARESOURCE_ERROR");
        }
        if (!isTransactionActive()) {
            throw new XAHelperException("J2EE_BAD_XASTATUS_ERROR");
        }
        try {
            Transaction transaction = tManager.getTransaction();
            lastXAResource = xaRes;
            lastEnlisted = transaction.enlistResource(xaRes);
        }
        catch (Exception exc) {
            error("Error enlisting resource", exc);
            throw new XAHelperException("J2EE_XAHELPER_ERROR", new String[][]{{"cause", exc.getMessage()}}, exc);
        }

        return lastEnlisted;
    }

    /**
     * Delist the given resource from the current transaction.
     * 
     * @param xaRes
     *        the resource to delist
     * @param flag
     *        the delist flag value
     * @return the operation status
     * @throws XAHelperException
     *         if error occurs
     */
    public final boolean delistResource(XAResource xaRes, int flag) throws XAHelperException
    {
        debug("Delisting resource: " + xaRes + ", flag=" + flag);

        boolean result = false;

        if (xaRes == null) {
            throw new XAHelperException("J2EE_BAD_XARESOURCE_ERROR");
        }
        if (!isTransactionActive()) {
            throw new XAHelperException("J2EE_BAD_XASTATUS_ERROR");
        }
        try {
            Transaction transaction = tManager.getTransaction();
            result = transaction.delistResource(xaRes, flag);
        }
        catch (Exception exc) {
            error("Error delisting resource", exc);
            throw new XAHelperException("J2EE_XAHELPER_ERROR", new String[][]{{"cause", exc.getMessage()}}, exc);
        }
        finally {
            lastXAResource = null;
            lastEnlisted = false;
        }

        return result;
    }

    /**
     * Delist the last enlisted resource.
     * 
     * @return the operation status
     * @throws XAHelperException
     *         if error occurs
     */
    public final boolean delistResource() throws XAHelperException
    {
        if ((lastXAResource == null) || !lastEnlisted) {
            return true;
        }
        return delistResource(lastXAResource, tStatus);
    }

    /**
     * Register a Synchronization instance in the current transaction.
     * 
     * @param sync
     *        the instance to register
     * @throws XAHelperException
     *         if error occurs
     */
    public final void registerSynchronization(Synchronization sync) throws XAHelperException
    {
        debug("register Synchronization");
        if (!isTransactionActive()) {
            throw new XAHelperException("J2EE_BAD_XASTATUS_ERROR");
        }
        try {
            Transaction transaction = tManager.getTransaction();
            transaction.registerSynchronization(sync);
        }
        catch (Exception exc) {
            error("Error registering Synchronization", exc);
            throw new XAHelperException("J2EE_XAHELPER_ERROR", new String[][]{{"cause", exc.getMessage()}}, exc);
        }
    }

    /**
     * @return Returns the autoEnlist.
     */
    public final boolean isAutoEnlist()
    {
        return autoEnlist;
    }

    /**
     * @param autoEnlist
     *        The autoEnlist to set.
     */
    public final void setAutoEnlist(boolean autoEnlist)
    {
        this.autoEnlist = autoEnlist;
    }


    public void begin() throws XAHelperException
    {
        // If the transaction is already active throw a TransactionException.
        if (isTransactionActive() == true) {
            throw new XAHelperException("J2EE_BAD_XASTATUS_ERROR");
        }

        // Perform the begin.
        try {
            getTransactionManager().begin();
        }
        catch (Exception exc) {
            throw new XAHelperException("J2EE_XAHELPER_ERROR", new String[][]{{"cause", exc.getMessage()}}, exc);
        }
    }

    public void commit() throws XAHelperException
    {
        try {
            getTransactionManager().commit();
        }
        catch (Exception exc) {
            throw new XAHelperException("J2EE_XAHELPER_ERROR", new String[][]{{"cause", exc.getMessage()}}, exc);
        }
    }

    public void rollback() throws XAHelperException
    {
        try {
            getTransactionManager().rollback();
        }
        catch (Exception exc) {
            throw new XAHelperException("J2EE_XAHELPER_ERROR", new String[][]{{"cause", exc.getMessage()}}, exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.connector.transaction.Transaction#setTransactionTimeout(int)
     */
    public void setTransactionTimeout(int second) throws XAHelperException
    {
        try {
            getTransactionManager().setTransactionTimeout(second);
        }
        catch (Exception exc) {
            throw new XAHelperException("J2EE_XAHELPER_ERROR", new String[][]{{"cause", exc.getMessage()}}, exc);
        }
    }

    /**
     * Helper method for logging debug messages.
     * 
     * @param obj
     *        the object to log
     */
    private void debug(Object obj)
    {
        if (logger != null) {
            logger.debug(obj.toString());
        }
    }

    /**
     * Helper method for logging error messages, with throwable.
     * 
     * @param obj
     *        the object to log
     * @param thr
     *        the throwable to log
     */
    private void error(Object obj, Throwable thr)
    {
        if (logger != null) {
            logger.error(obj.toString(), thr);
        }
    }

}
