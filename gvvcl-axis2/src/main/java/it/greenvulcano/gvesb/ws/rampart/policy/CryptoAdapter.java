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
package it.greenvulcano.gvesb.ws.rampart.policy;

import it.greenvulcano.util.crypto.CryptoHelper;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.components.crypto.Merlin;
import org.apache.ws.security.util.Loader;

/**
 * 
 * @version 3.3.0 23/oct/2012
 * @author GreenVulcano Developer Team
 */
public class CryptoAdapter extends Merlin
{
    private static Logger      logger         = org.slf4j.LoggerFactory.getLogger(CryptoAdapter.class);
    /*
     * KeyStore configuration types
     */
    public static final String KEYSTORE_ID    = "it.greenvulcano.wss.crypto.keystore.id";
    public static final String KEYSTORE_ALIAS = "it.greenvulcano.wss.crypto.keystore.alias";

    /*
     * TrustStore configuration types
     */
    public static final String TRUSTSTORE_ID  = "it.greenvulcano.wss.crypto.truststore.id";


    protected String           keyStoreID     = null;
    protected String           trustStoreID   = null;

    /**
     * Constructor
     * 
     * @param properties
     */
    public CryptoAdapter(Properties properties) throws CredentialException, IOException
    {
        this(properties, Loader.getClassLoader(Merlin.class));
    }

    public CryptoAdapter(Properties properties, ClassLoader loader) throws CredentialException, IOException
    {
        logger.debug("Initializing Rampart CryptoAdapter: " + properties);

        this.keyStoreID = properties.getProperty(KEYSTORE_ID);
        this.defaultAlias = properties.getProperty(KEYSTORE_ALIAS);
        this.trustStoreID = properties.getProperty(TRUSTSTORE_ID);
        try {
            logger.debug("Retrieving keystore: " + this.keyStoreID);
            this.keystore = CryptoHelper.getKeyStore(this.keyStoreID);
        }
        catch (Exception exc) {
            logger.error("CryptoAdapter - Error accessing KeyStore[" + this.keyStoreID + "]", exc);
            throw new CredentialException(CredentialException.IO_ERROR, "proxyNotFound", new Object[]{this.keyStoreID},
                    exc);
        }
        if (this.trustStoreID != null) {
            try {
                logger.debug("Retrieving truststore: " + this.trustStoreID);
                this.truststore = CryptoHelper.getKeyStore(this.trustStoreID);
            }
            catch (Exception exc) {
                logger.error("CryptoAdapter - Error accessing TrustStore[" + this.trustStoreID + "]", exc);
                throw new CredentialException(CredentialException.IO_ERROR, "proxyNotFound",
                        new Object[]{this.trustStoreID}, exc);
            }
        }
        super.loadProperties(properties, loader);
    }

}
