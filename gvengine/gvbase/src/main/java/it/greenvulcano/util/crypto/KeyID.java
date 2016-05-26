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

import java.security.AlgorithmParameters;
import java.security.Key;

import org.w3c.dom.Node;

/**
 * 
 * KeyID class
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class KeyID
{
    /**
     * the KeyID id
     */
    private String              keyID          = "";
    /**
     * the KeyStoreID id
     */
    private KeyStoreID          keyStoreID     = null;
    /**
     * the key algorithm
     */
    private String              keyType        = "";
    /**
     * the key algorithm provider
     */
    private String              keyProvider    = null;
    /**
     * the key algorithm mode
     */
    private String              keyTypeMode    = "";
    /**
     * the key algorithm padding
     */
    private String              keyTypePadding = "NoPadding";
    /**
     * the key name alias
     */
    private String              keyAlias       = "";
    /**
     * the key alias password
     */
    private String              keyPwd         = "";
    /**
     * the key
     */
    private Key                 key            = null;

    private AlgorithmParameters params         = null;

    /**
     * Key identification data for reading from /writing in a keystore
     * 
     * @param kID
     *        the KeyID id
     * @param kType
     *        the key algorithm
     * @param kStoreID
     *        the key store id
     * @param kAlias
     *        the key name alias
     * @param kPwd
     *        the key alias password
     */
    public KeyID(String kID, String kType, KeyStoreID kStoreID, String kAlias, String kPwd)
    {
        keyID = kID;
        keyType = kType;
        keyStoreID = kStoreID;
        keyAlias = kAlias;
        keyPwd = kPwd;
    }

    /**
     * Key identification data for reading from /writing in a keystore
     * 
     * @param kID
     *        the KeyID id
     * @param kType
     *        the key algorithm
     * @param kStoreID
     *        the key store id
     * @param kAlias
     *        the key name alias
     * @param kPwd
     *        the key alias password
     * @param kKey
     *        the key
     */
    public KeyID(String kID, String kType, KeyStoreID kStoreID, String kAlias, String kPwd, Key kKey)
    {
        keyID = kID;
        keyType = kType;
        keyStoreID = kStoreID;
        keyAlias = kAlias;
        keyPwd = kPwd;
        key = kKey;
    }

    /**
     * Key identification data for reading from /writing in a keystore
     * 
     * @param kID
     *        the KeyID id
     * @param kType
     *        the key algorithm
     * @param kTypeMode
     *        the key algorithm mode
     * @param kTypePadding
     *        the key algorithm padding (default to NoPadding)
     * @param kStoreID
     *        the key store id
     * @param kAlias
     *        the key name alias
     * @param kPwd
     *        the key alias password
     * @param kKey
     *        the key
     */
    public KeyID(String kID, String kType, String kProvider, String kTypeMode, String kTypePadding,
            KeyStoreID kStoreID, String kAlias, String kPwd, Key kKey)
    {
        keyID = kID;
        keyType = kType;
        keyProvider = kProvider;
        keyTypeMode = kTypeMode;
        if ((kTypePadding != null) && !"".equals(kTypePadding)) {
            keyTypePadding = kTypePadding;
        }
        keyStoreID = kStoreID;
        keyAlias = kAlias;
        keyPwd = kPwd;
        key = kKey;
    }

    /**
     * Key identification data for reading from /writing in a keystore
     * 
     * @param kID
     *        the KeyID id
     * @param kStoreID
     *        the key store id
     * @param kAlias
     *        the key name alias
     */
    public KeyID(String kID, KeyStoreID kStoreID, String kAlias)
    {
        keyID = kID;
        keyStoreID = kStoreID;
        keyAlias = kAlias;
    }

    /**
     * Key identification data for reading from /writing in a keystore
     * 
     * @param node
     *        XML node
     * @throws XMLConfigException
     *         if error occurs
     * @throws CryptoHelperException
     */
    public KeyID(Node node) throws XMLConfigException, CryptoHelperException, CryptoUtilsException
    {
        keyID = XMLConfig.get(node, "@id");
        keyType = XMLConfig.get(node, "@key-type", "");
        keyProvider = XMLConfig.get(node, "@key-provider", null);
        keyTypeMode = XMLConfig.get(node, "@key-type-mode", "");
        keyTypePadding = XMLConfig.get(node, "@key-type-padding", "NoPadding");
        String kSID = XMLConfig.get(node, "@key-store-id");
        keyStoreID = CryptoHelper.getKeyStoreID(kSID);
        keyAlias = XMLConfig.get(node, "@key-alias");
        keyPwd = XMLConfig.getDecrypted(node, "@key-pwd", "");
        if (XMLConfig.exists(node, "AlgorithmParameters")) {
            String algP = XMLConfig.get(node, "AlgorithmParameters");
            params = AlgorithmParametersHolder.createAlgorithmParameters(CryptoUtils.getTypeI(keyType), keyProvider,
                    algP);
        }
    }

    /**
     * @return Returns the key.
     */
    public Key getKey()
    {
        return key;
    }

    /**
     * @param kKey
     *        The key to set.
     */
    public void setKey(Key kKey)
    {
        key = kKey;
    }

    /**
     * @return Returns the keyType.
     */
    public String getKeyType()
    {
        return keyType;
    }

    /**
     * @return Returns the keyType/keyTypeMode/keyTypePadding.
     */
    public String getFullKeyType()
    {
        String type = keyType;
        if ((keyTypeMode != null) && !"".equals(keyTypeMode)) {
            type += "/" + keyTypeMode + "/" + keyTypePadding;
        }
        return type;
    }


    /**
     * @param kType
     *        The keyType to set.
     */
    public void setKeyType(String kType)
    {
        keyType = kType;
    }

    /**
     * @return Returns the keyProvider.
     */
    public String getKeyProvider()
    {
        return keyProvider;
    }


    /**
     * @param kProvider
     *        The keyProvider to set.
     */
    public void setKeyProvider(String kProvider)
    {
        keyProvider = kProvider;
    }

    /**
     * @return the keyTypeMode
     */
    public String getKeyTypeMode()
    {
        return this.keyTypeMode;
    }

    /**
     * @param keyTypeMode
     *        the keyTypeMode to set
     */
    public void setKeyTypeMode(String keyTypeMode)
    {
        this.keyTypeMode = keyTypeMode;
    }

    /**
     * @return the keyTypePadding
     */
    public String getKeyTypePadding()
    {
        return this.keyTypePadding;
    }

    /**
     * @param keyTypePadding
     *        the keyTypePadding to set
     */
    public void setKeyTypePadding(String keyTypePadding)
    {
        this.keyTypePadding = keyTypePadding;
    }

    /**
     * @return Returns the keyAlias.
     */
    public String getKeyAlias()
    {
        return keyAlias;
    }

    /**
     * @param kAlias
     *        The keyAlias to set.
     */
    public void setKeyAlias(String kAlias)
    {
        keyAlias = kAlias;
    }

    /**
     * @return Returns the keyPwd.
     */
    public String getKeyPwd()
    {
        return keyPwd;
    }

    /**
     * @param kPwd
     *        The keyPwd to set.
     */
    public void setKeyPwd(String kPwd)
    {
        keyPwd = kPwd;
    }

    /**
     * @return Returns the keyStoreID.
     */
    public KeyStoreID getKeyStoreID()
    {
        return keyStoreID;
    }

    /**
     * @param kStoreID
     *        The keyStoreID to set.
     */
    public void setKeyStoreID(KeyStoreID kStoreID)
    {
        keyStoreID = kStoreID;
    }

    /**
     * @return Returns the keyID.
     */
    public String getKeyID()
    {
        return keyID;
    }

    public AlgorithmParameters getAlgorithmParameters()
    {
        return this.params;
    }

    public void setAlgorithmParameters(AlgorithmParameters params)
    {
        this.params = params;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String descr = "KeyID (" + keyID + "): keyStoreID = " + keyStoreID.getKeyStoreID() + " - keyAlias = "
                + keyAlias + " - keyPwd = HIDDEN";// + keyPwd;
        return descr;
    }

}
