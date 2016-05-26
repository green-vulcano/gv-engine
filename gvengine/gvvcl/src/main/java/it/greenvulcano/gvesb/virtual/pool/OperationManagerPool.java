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
package it.greenvulcano.gvesb.virtual.pool;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.j2ee.XAHelper;
import it.greenvulcano.gvesb.j2ee.XAHelperException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.DequeueOperation;
import it.greenvulcano.gvesb.virtual.EnqueueOperation;
import it.greenvulcano.gvesb.virtual.Operation;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.gvesb.virtual.VCLException;
import it.greenvulcano.script.util.BaseContextManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.transaction.Transaction;

import org.slf4j.Logger;

/**
 * OperationManager Pool.
 *
 * @version 3.2.0 Feb 28, 2011
 * @author GreenVulcano Developer Team
 *
 */
public class OperationManagerPool implements ConfigurationListener, ShutdownEventListener
{
    private static final Logger                            logger                = org.slf4j.LoggerFactory.getLogger(OperationManagerPool.class);

    public static final String                             OPMGR_CONFIG_FILENAME = "GVSystems.xml";

    private static OperationManagerPool                    instance              = null;
    /**
     * Pool of OperationManager instances.
     */
    private Map<OperationKey, OperationManagerPoolElement> pool                  = new ConcurrentHashMap<OperationKey, OperationManagerPoolElement>();

    private Map<Transaction, VCLXASynchronization>         xaInUseOperations     = new ConcurrentHashMap<Transaction, VCLXASynchronization>();

    /**
     * This map holds associations between files and operation key using them,
     * so in case of configuration changes the operations built from older
     * configuration can be discarded. <br>
     * This field associates file names with a Set of OperatonKey.
     */
    protected Map<String, Set<OperationKey>>               filesToKeys           = new ConcurrentHashMap<String, Set<OperationKey>>();

    /**
     * This Set contains all configuration files to discard. At each invocation
     * of the getOperation() method, operations to discard will be discarded.
     * This set is filled by the configurationChanged() method.
     */
    private Set<String>                                    filesToDiscard        = Collections.synchronizedSet(new HashSet<String>());

    /**
     * If true shutdown in progress.
     */
    private AtomicBoolean                                  shutdownFlag          = new AtomicBoolean(false);

    private XAHelper                                       xaHelper              = new XAHelper();
    
    private static boolean                                 debugXA               = Boolean.getBoolean("it.greenvulcano.gvesb.virtual.pool.OperationManagerPool.debugXA");


    /**
     * @throws GVException
     */
    private OperationManagerPool()
    {
        init();
    }

    public static synchronized OperationManagerPool instance()
    {
        if (instance == null) {
            instance = new OperationManagerPool();
        }
        return instance;
    }

    /**
     * @throws GVException
     */
    private void init()
    {
        logger.info("Initializing the Operation Manager Pool.");
        if (debugXA) {
            xaHelper.setLogger(logger);
        }
        ShutdownEventLauncher.addEventListener(this);
        XMLConfig.addConfigurationListener(this);
    }

    /**
     *
     */
    public void resetCounter()
    {
        // TODO:
    }

    /**
     * Obtains a cached operation. If the operation is not available then uses
     * OperationFactory to create it.
     *
     * @param key
     *        the key of the operation
     * @return the requested operation
     * @throws VCLException
     *         if an error occurs.
     */
    public CallOperation getCall(OperationKey key) throws VCLException
    {
        return (CallOperation) getOperation(key, CallOperation.TYPE);
    }

    /**
     * Obtains a cached operation. If the operation is not available then uses
     * OperationFactory to create it.
     *
     * @param key
     *        the key of the operation
     * @return the requested operation
     * @throws VCLException
     *         if an error occurs.
     */
    public EnqueueOperation getEnqueue(OperationKey key) throws VCLException
    {
        return (EnqueueOperation) getOperation(key, EnqueueOperation.TYPE);
    }

    /**
     * Obtains a cached operation. If the operation is not available then uses
     * OperationFactory to create it.
     *
     * @param key
     *        the key of the operation
     * @return the requested operation
     * @throws VCLException
     *         if an error occurs.
     */
    public DequeueOperation getDequeue(OperationKey key) throws VCLException
    {
        return (DequeueOperation) getOperation(key, DequeueOperation.TYPE);
    }

    /**
     *
     * @throws VCLException
     */
    public Operation getOperation(OperationKey key, String type) throws VCLException
    {
        if (shutdownFlag.get()) {
            throw new VCLException("ShutdownEvent received, pool disabled");
        }
        if (pool == null) {
            return null;
        }

        discardOperations();

        OperationManagerPoolElement vclPoolElement = pool.get(key);

        if (vclPoolElement == null) {
            synchronized (pool) {
                vclPoolElement = pool.get(key);
                if (vclPoolElement == null) {
                    logger.debug("OperationManagerPool - not found instance in pool for key: " + key);
                    vclPoolElement = new OperationManagerPoolElement(key, type);
                    pool.put(key, vclPoolElement);

                    String file = key.getFile();
                    // Obtains the set of OperationKey for the given file.
                    // Create a set if it is not already created.
                    Set<OperationKey> set = filesToKeys.get(file);
                    if (set == null) {
                        set = new HashSet<OperationKey>();
                        filesToKeys.put(file, set);
                    }
                    // Add the key to the set
                    set.add(key);
                }
                else {
                    logger.debug("OperationManagerPool - found instance in pool for key: " + key);
                }
            }
        }
        else {
            logger.debug("OperationManagerPool - found instance in pool for key: " + key);
        }

        Operation operation = vclPoolElement.getOperation();

        try {
            if (xaHelper.isTransactionRunning()) {
                Transaction tx = xaHelper.getTransaction();
                VCLXASynchronization vclXA = xaInUseOperations.get(tx);

                if (vclXA == null) {
                    synchronized (xaInUseOperations) {
                        vclXA = xaInUseOperations.get(tx);
                        if (vclXA == null) {
                            vclXA = new VCLXASynchronization(tx);
                            xaInUseOperations.put(tx, vclXA);
                            xaHelper.registerSynchronization(vclXA);
                        }
                    }
                }
                vclXA.add(operation);
            }
        }
        catch (XAHelperException exc) {
            throw new VCLException("GVVCL_XA_ERROR", exc);
        }

        return operation;
    }

    /**
     *
     * @throws VCLException
     */
    public boolean checkOperation(OperationKey key, String type) throws VCLException
    {
        if (shutdownFlag.get()) {
            throw new VCLException("ShutdownEvent received, pool disabled");
        }
        if (pool == null) {
            return false;
        }

        discardOperations();

        OperationManagerPoolElement vclPoolElement = pool.get(key);

        if (vclPoolElement == null) {
            synchronized (pool) {
                vclPoolElement = pool.get(key);
                if (vclPoolElement == null) {
                    logger.debug("OperationManagerPool - not found instance in pool for key: " + key);
                    vclPoolElement = new OperationManagerPoolElement(key, type);
                    pool.put(key, vclPoolElement);

                    String file = key.getFile();
                    // Obtains the set of OperationKey for the given file.
                    // Create a set if it is not already created.
                    Set<OperationKey> set = filesToKeys.get(file);
                    if (set == null) {
                        set = new HashSet<OperationKey>();
                        filesToKeys.put(file, set);
                    }
                    // Add the key to the set
                    set.add(key);
                }
                else {
                    logger.debug("OperationManagerPool - found instance in pool for key: " + key);
                }
            }
        }
        else {
            logger.debug("OperationManagerPool - found instance in pool for key: " + key);
        }

        return vclPoolElement.checkOperation();
    }

    /**
     *
     * @param operation
     */
    public void releaseOperation(Operation operation) throws VCLException
    {
        if (operation == null) {
            return;
        }

        try {
            if (xaHelper.isTransactionRunning()) {
                return;
            }
        }
        catch (XAHelperException exc) {
            throw new VCLException("GVVCL_XA_ERROR", exc);
        }

        intReleaseOperation(operation);
    }

    /**
     *
     * @param om
     */
    public void xaReleaseOperation(VCLXASynchronization xaSync)
    {
        Transaction tx = xaSync.getTransaction();
        xaInUseOperations.remove(tx);

        for (Operation operation : xaSync.getVCLOperations()) {
            intReleaseOperation(operation);
        }
        xaSync.destroy();
    }

    /**
     * Remove from the cache a given operations.
     *
     * @param key
     *        the key of the operations to remove
     */
    public void discardOperations(OperationKey key)
    {
        logger.debug("BEGIN discarding " + key);
        try {
            OperationManagerPoolElement vclPoolElement = pool.remove(key);
            if (vclPoolElement != null) {
                vclPoolElement.destroy();
            }

            // Remove the key from the file->key map
            String file = key.getFile();
            Set<OperationKey> set = filesToKeys.get(file);
            set.remove(key);
            if (set.size() == 0) {
                filesToKeys.remove(file);
            }
        }
        finally {
            logger.debug("END discarding " + key);
        }
    }

    /**
     *
     */
    public void destroy()
    {
        if (pool == null) {
            return;
        }
        logger.debug("OperationManagerPool - Begin destroying instances");
        synchronized (pool) {
            for (Map.Entry<OperationKey, OperationManagerPoolElement> element : pool.entrySet()) {
                element.getValue().destroy();
            }
            pool.clear();
            filesToDiscard.clear();
            filesToKeys.clear();
        }
        logger.debug("OperationManagerPool - End destroying instances");
        // assignedOM.clear();
        pool = null;
    }

    /**
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        destroy();
    }

    public XAHelper getXAHelper() {
        return this.xaHelper;
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent event)
    {
        if (event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) {
            String file = event.getFile();
            if (BaseContextManager.CFG_FILE.equals(file)) {
                filesToDiscard.addAll(filesToKeys.keySet());
            }
            else {
                filesToDiscard.add(file);
            }
        }
    }

    /**
     * @see it.greenvulcano.event.util.shutdown.ShutdownEventListener#shutdownStarted(it.greenvulcano.event.util.shutdown.ShutdownEvent)
     */
    @Override
    public void shutdownStarted(ShutdownEvent event)
    {
        shutdownFlag.set(true);
        destroy();
    }

    @Override
    public String toString()
    {
        if (pool == null) {
            return "EMPTY OperationManagerPool";
        }
        StringBuffer sb = new StringBuffer("OperationManagerPool\n");
        for (Map.Entry<OperationKey, OperationManagerPoolElement> element : pool.entrySet()) {
            sb.append("\n").append(element.getValue());
        }
        return sb.toString();
    }

    /**
     *
     * @param operation
     */
    private void intReleaseOperation(Operation operation)
    {
        if (operation == null) {
            return;
        }

        if (shutdownFlag.get()) {
            logger.debug("OperationManagerPool - ShutdownEvent received, destroying Operation instance");
            try {
                logger.debug("Destroy: " + operation);
                operation.destroy();
            }
            catch (Throwable exc) {
                logger.warn("Error destroying Operation " + operation, exc);
            }
            return;
        }

        OperationKey key = operation.getKey();

        logger.debug("OperationManagerPool - releasing instance in pool for key: " + key);

        OperationManagerPoolElement vclPoolElement = pool.get(key);
        if (vclPoolElement != null) {
            vclPoolElement.releaseOperation(operation);
        }
        else {
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
     * Discard all operations related to files in the Set filesToDiscard.
     */
    private void discardOperations()
    {
        synchronized (pool) {
            synchronized (filesToDiscard) {
                for (String file : filesToDiscard) {
                    Set<OperationKey> set = filesToKeys.remove(file);
                    if (set != null) {
                        for (OperationKey key : set) {
                            OperationManagerPoolElement element = pool.remove(key);
                            if (element != null) {
                                element.destroy();
                            }
                        }
                    }
                }
                filesToDiscard.clear();
            }
        }
    }
}
