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
package it.greenvulcano.gvesb.gvdte.controller;

import it.greenvulcano.gvesb.gvdte.DTEException;
import it.greenvulcano.gvesb.gvdte.config.DataSourceFactory;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransformer;
import it.greenvulcano.util.thread.ThreadUtils;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

/**
 * Mantain a cache of Transformer's instances.
 * Use the DTETransformerFactory to initialize Transformers.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DTETransformerManager
{
    private static final Logger         logger            = org.slf4j.LoggerFactory.getLogger(DTETransformerManager.class);

    private DataSourceFactory           dataSourceFactory = null;

    /**
     * Cache for DTETransformer objects.
     */
    private Map<String, DTETransformer> transformerCache;

    /**
     * Initialize the instance.
     *
     * @param dsf
     * @throws DTEException
     */
    public DTETransformerManager(DataSourceFactory dsf) throws DTEException
    {
        if (dsf == null) {
            throw new DTEException("GVDTE_CONFIGURATION_ERROR", new String[][]{{"cause",
                    "The DataSourceFactory instance can't be null"}});
        }
        dataSourceFactory = dsf;
        transformerCache = new HashMap<String, DTETransformer>();
    }

    /**
     * Return the requested transformer.
     *
     * @param name
     * @return the requested transformer
     * @throws DTEException
     * @throws InterruptedException 
     */
    public DTETransformer getTransformer(String name) throws DTEException, InterruptedException
    {
        logger.debug("Requested transformer '" + name + "'");
        ThreadUtils.checkInterrupted("DTETransformerManager", name, logger);
        DTETransformer transformer = transformerCache.get(name);
        if (transformer == null) {
            logger.debug("Transformer '" + name + "' not found in cache, creating ...");
            try {
                transformer = DTETransformerFactory.instance().newTransformer(name, dataSourceFactory);
                logger.debug("Caching transformer '" + name + "'");
                if (transformer instanceof SequenceTransformer) {
                    ((SequenceTransformer) transformer).setTransformerManager(this);
                }
                transformerCache.put(name, transformer);
                manageTransformerAlias(name, transformer);
            }
            catch (DTEException exc) {
                logger.error("Error while requesting new transformer" + exc);
                throw new DTEException("GVDTE_REQUEST_TRASFORMER_ERROR", new String[][]{{"name", name}}, exc);
            }
        }
        return transformer;
    }

    private void manageTransformerAlias(String name, DTETransformer transformer) throws DTEException
    {
        String[] aliases = DTETransformerFactory.instance().getTransformerAlias(name, dataSourceFactory);
        for (int i = 0; i < aliases.length; i++) {
            DTETransformer tmp = transformerCache.get(aliases[i]);
            if (tmp == null) {
                transformerCache.put(aliases[i], transformer);
            }
            else {
                delTransformer(name);
                throw new DTEException("GVDTE_TRASFORMER_ALIAS_ERROR", new String[][]{{"name", name},
                        {"alias", aliases[i]}});
            }
        }
    }

    private void delTransformer(String name) throws DTEException
    {
        DTETransformer tmp = transformerCache.get(name);
        if (tmp == null) {
            return;
        }
        transformerCache.remove(name);

        String[] aliases = DTETransformerFactory.instance().getTransformerAlias(name, dataSourceFactory);
        for (int i = 0; i < aliases.length; i++) {
            tmp = transformerCache.get(aliases[i]);
            if (tmp == null) {
                return;
            }
            transformerCache.remove(aliases[i]);
        }
    }

    /**
     * Clean transformers cache.
     */
    public void clear()
    {
        // Clean Transformers cache
        for (DTETransformer transformer : transformerCache.values()) {
            try {
                transformer.destroy();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        transformerCache.clear();
    }

    /**
     *
     */
    public void destroy()
    {
        clear();
        dataSourceFactory = null;
    }

}
