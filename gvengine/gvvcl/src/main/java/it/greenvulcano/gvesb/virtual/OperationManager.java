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

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>OperationManager</code> class is the entry point of GVVCL mechanisms.
 * GreenVulcanoESB algorithm ask <code>OperationManager</code> in order to
 * obtains concrete objects that implement GVVCL mechanisms.
 * <p>
 * When GreenVulcanoESB ask the <code>OperationManager</code> for an object,
 * <code>OperationManager</code> ask the <code>OperationFactory</code> to build
 * the requested object.<br>
 * <code>OperationManager</code> caches GVVCL's objects in order to maximize
 * performances.<br>
 * Configuration changes on used files cause cache cleanup.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * @deprecated
 */
@Deprecated
public class OperationManager implements ConfigurationListener
{

	
	
	
	
    /**
     * The GVLogger instance
     */
    private static final org.slf4j.Logger              logger         = org.slf4j.LoggerFactory.getLogger(OperationManager.class);

    /**
     * Cache for the operations.
     */
    private Map<OperationKey, Operation>     operationCache = new HashMap<OperationKey, Operation>();

    /**
     * This map holds associations between files and operation key using them,
     * so in case of configuration changes the operations built from older
     * configuration can be discarded. <br>
     * This field associates file names with a Set of OperatonKey.
     */
    protected Map<String, Set<OperationKey>> filesToKeys    = new HashMap<String, Set<OperationKey>>();

    /**
     * This Set contains all configuration files to discard. At each invocation
     * of the getOperation() method, operations to discard will be discarded.
     * This set is filled by the configurationChanged() method.
     */
    private Set<String>                      filesToDiscard = new HashSet<String>();

    /**
     * Construct an OperationManager. NOTES: THE CONSTRUCTOR MECHANISM IS NOT
     * YET DEFINED. WE NEED TO CHANGE THIS CONSTRUCTOR.
     */
    public OperationManager()
    {

        // We are interested to configuration changes
        //
        XMLConfig.addConfigurationListener(this);

        logger.debug("Operation Manager created");
    }

    /**
     * Remove from the cache a given operation.
     *
     * @param key
     *        the key of the operation to remove
     */
    public void discardOperation(OperationKey key)
    {

        logger.debug("BEGIN discarding " + key);

        try {
            Operation operation = operationCache.get(key);

            // The operation is not into the cache
            //
            if (operation == null) {
                return;
            }

            // Remove the operation from cache and destroys it.
            //
            operationCache.remove(key);
            logger.debug("Destroy: " + operation);
            operation.destroy();

            // Remove the key from the file->key map
            //
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
     * Remove from the cache all operations from a given file.
     *
     * @param file
     *        the file name
     */
    public void discardOperations(String file)
    {
        logger.debug("BEGIN discarding operations from file " + file);

        try {
            Set<OperationKey> set = filesToKeys.get(file);
            if (set != null) {
                discardOperations(set);
            }
        }
        finally {
            logger.debug("END discarding operations from file " + file);
        }
    }

    /**
     * Remove from the cache all operations.
     */
    public synchronized void discardAllOperations()
    {
        Iterator<String> i = filesToKeys.keySet().iterator();
        while (i.hasNext()) {
            discardOperations(i.next());
            i.remove();
        }
    }

    /**
     * Obtains a cached operation. If the operation is not available then uses
     * OperationFactory to create it.
     *
     * @param key
     *        the key of the operation
     * @return the requested operation
     * @throws GVException
     *         if an error occurs.
     */
    public CallOperation getCall(OperationKey key) throws GVException
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
     * @throws GVException
     *         if an error occurs.
     */
    public EnqueueOperation getEnqueue(OperationKey key) throws GVException
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
     * @throws GVException
     *         if an error occurs.
     */
    public DequeueOperation getDequeue(OperationKey key) throws GVException
    {
        return (DequeueOperation) getOperation(key, DequeueOperation.TYPE);
    }

    /**
     * Obtains a cached operation. If the operation is not available then uses
     * OperationFactory to create it.
     *
     * @param key
     *        the key of the operation
     * @param type
     *        the operation type
     * @return the requested operation
     * @throws GVException
     *         if an error occurs.
     */
    public Operation getOperation(OperationKey key, String type) throws GVException
    {

        discardOperations();

        // First check if the requested operation is already in cache
        //
        Operation operation = operationCache.get(key);

        // OK. The operation is in cache, so we return it.
        //
        if (operation != null) {
            return operation;
        }

        // No, the operation is not in cache, so we ask the OperationFactory
        // in order to create it.
        //
        operation = OperationFactory.createOperation(key, type);

        // Store the new operation in cache for future uses
        //
        cacheOperation(key, operation);

        // Return the operation
        //
        return operation;
    }

    /**
     * At finalization time, we discard all cached operations.
     */
    @Override
    protected void finalize()
    {
        discardAllOperations();
    }

    /**
     * Stores the operation in cache and updates the filesToKeys Map.
     *
     * @param key
     *        the key to use for mapping
     * @param operation
     *        the operation to cache
     */
    private void cacheOperation(OperationKey key, Operation operation)
    {
        // Associates the key with the operation and put them in the cache.
        //
        operationCache.put(key, operation);

        String file = key.getFile();

        // Obtains the set of OperationKey for the given file.
        // Create a set if it is not already created.
        //
        Set<OperationKey> set = filesToKeys.get(file);
        if (set == null) {
            set = new HashSet<OperationKey>();
            filesToKeys.put(file, set);
        }

        // Add the key to the set
        //
        set.add(key);
    }

    /**
     * Removes from the cache the given operation keys
     *
     * @param keys
     *        the list of keys to remove
     */
    private void discardOperations(Set<OperationKey> keys)
    {
        for (OperationKey key : keys) {
            Operation operation = operationCache.remove(key);
            try {
                if (operation != null) {
                    logger.debug("Destroy: " + operation);
                    operation.destroy();
                }
            }
            catch (Throwable exc) {
                logger.error(exc.getLocalizedMessage(), exc);
            }
        }
    }

    /**
     * Discard all operations related to files in the Set filesToDiscard.
     */
    private void discardOperations()
    {
        while (filesToDiscard.size() > 0) {
            String fileToDiscard = null;
            synchronized (filesToDiscard) {
                Iterator<String> i = filesToDiscard.iterator();
                if (i.hasNext()) {
                    fileToDiscard = i.next();
                    i.remove();
                }
            }
            if (fileToDiscard != null) {
                discardOperations(fileToDiscard);
            }
        }
    }

    /**
     * Configuration changed. Discard all operations built from the discarder
     * files.
     *
     * @param evt
     *        the event received
     */
   
    public void configurationChanged(ConfigurationEvent evt)
    {
        if (evt.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) {
            String file = evt.getFile();
            synchronized (filesToDiscard) {
                filesToDiscard.add(file);
            }
        }
    }

    /**
     * Destroy the cached operations
     */
    public void destroy()
    {
        XMLConfig.removeConfigurationListener(this);
        discardAllOperations();
    }
}
