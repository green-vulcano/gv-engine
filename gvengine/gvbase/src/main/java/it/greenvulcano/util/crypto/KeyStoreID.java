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
package it.greenvulcano.util.crypto;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import org.w3c.dom.Node;

/**
 *
 * KeyStoreID class
 *
 * @version 3.1.0 Jun 21, 2011
 * @author GreenVulcano Developer Team
 *
 *
 */
public class KeyStoreID
{
    /**
     * the KeyStoreID id
     */
    private String keyStoreID   = "";
    /**
     * the key store type
     */
    private String keyStoreType = "";
    /**
     * the key store provider
     */
    private String keyStorePrv  = "";
    /**
     * the key store name
     */
    private String keyStoreName = "";
    /**
     * the key store password
     */
    private String keyStorePwd  = "";

    /**
     * KeyStore identification data
     *
     * @param kID
     *        the KeyID id
     * @param kStoreType
     *        the key store name
     * @param kStoreName
     *        the key store name
     * @param kStorePwd
     *        the key store password
     * @param kStorePrv
     *        the key store provider
     */
    public KeyStoreID(String kID, String kStoreType, String kStoreName, String kStorePwd, String kStorePrv)
    {
        keyStoreID = kID;
        keyStoreName = kStoreName;
        keyStoreType = kStoreType;
        keyStorePrv = kStorePrv;
        keyStorePwd = kStorePwd;
    }


    /**
     * KeyStore identification data
     *
     * @param node
     *        XML node
     * @throws XMLConfigException
     *         if error occurs
     */
    public KeyStoreID(Node node) throws XMLConfigException
    {
        keyStoreID = XMLConfig.get(node, "@id");
        keyStoreType = XMLConfig.get(node, "@key-store-type", KeyStoreUtils.DEFAULT_KEYSTORE_TYPE);
        keyStorePrv = XMLConfig.getDecrypted(node, "@key-store-prv", KeyStoreUtils.DEFAULT_KEYSTORE_PROVIDER);
        keyStoreName = XMLConfig.get(node, "@key-store-name");
        keyStorePwd = XMLConfig.getDecrypted(node, "@key-store-pwd", "");
    }

    /**
     * @return Returns the keyStoreType.
     */
    public String getKeyStoreType()
    {
        return keyStoreType;
    }

    /**
     * @param kStoreType
     *        The keyStoreType to set.
     */
    public void setKeyStoreType(String kStoreType)
    {
        keyStoreType = kStoreType;
    }

    /**
     * @return the keyStorePrv
     */
    public String getKeyStorePrv()
    {
        return this.keyStorePrv;
    }

    /**
     * @param kStorePrv
     *        the keyStorePrv to set
     */
    public void setKeyStorePrv(String kStorePrv)
    {
        this.keyStorePrv = kStorePrv;
    }

    /**
     * @return Returns the keyStoreName.
     */
    public String getKeyStoreName()
    {
        return keyStoreName;
    }

    /**
     * @param kStoreName
     *        The keyStoreName to set.
     */
    public void setKeyStoreName(String kStoreName)
    {
        keyStoreName = kStoreName;
    }

    /**
     * @return Returns the keyStorePwd.
     */
    public String getKeyStorePwd()
    {
        return keyStorePwd;
    }

    /**
     * @param kStorePwd
     *        The keyStorePwd to set.
     */
    public void setKeyStorePwd(String kStorePwd)
    {
        keyStorePwd = kStorePwd;
    }

    /**
     * @return Returns the keyStoreID.
     */
    public String getKeyStoreID()
    {
        return keyStoreID;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String descr = "KeyStoreID (" + keyStoreID + "): keyStoreName = " + keyStoreName + " - keyStorePwd = HIDDEN" // +
                                                                                                                     // keyStorePwd
                + " - keyStoreType = " + keyStoreType + " - keyStorePrv = " + keyStorePrv;
        return descr;
    }

}
