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

import it.greenvulcano.util.metadata.PropertiesHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.HashMap;

/**
 *
 * KeyStoreUtils class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public final class KeyStoreUtils
{
    /**
     * default keystore type.
     */
    public static final String               DEFAULT_KEYSTORE_TYPE     = "JCEKS";

    /**
     * default keystore provider.
     */
    public static final String               DEFAULT_KEYSTORE_PROVIDER = "SunJCE";

    /**
     * mapping from keystore name -> instance.
     */
    private static HashMap<String, KeyStore> keyStoreMap               = new HashMap<String, KeyStore>();

    /**
     * Constructor.
     */
    private KeyStoreUtils()
    {
        // do nothing
    }

    /**
     * @param keySid
     */
    public static KeyStore getKeyStore(KeyStoreID keySid) throws KeyStoreUtilsException
    {
        return KeyStoreUtils.getKeyStore(keySid.getKeyStoreName(), keySid.getKeyStorePwd(), keySid.getKeyStoreType(),
                keySid.getKeyStorePrv());
    }

    /**
     * Initialize a keystore from a file in the ClassPath.
     *
     * @param keyStoreName
     *        the key store name
     * @param keyStorePwd
     *        the key store password
     * @param keyStorePrv
     *        the key store provider
     * @return the requested key store
     * @throws KeyStoreUtilsException
     *         if error occurs
     */
    public static KeyStore getKeyStore(String keyStoreName, String keyStorePwd, String keyStoreType, String keyStorePrv)
            throws KeyStoreUtilsException
    {
        KeyStore keyStore = keyStoreMap.get(keyStoreName);
        if (keyStore == null) {
            InputStream is = null;
            try {
                String fn = PropertiesHandler.expand("sp{{gv.app.home}}" + File.separatorChar + "keystores"
                        + File.separatorChar + keyStoreName);

                URL url = KeyStoreUtils.class.getClassLoader().getResource(keyStoreName);
                if (url != null) {
                    fn = url.getFile();
                }
                System.out.println("getKeyStore: keyStoreName[" + keyStoreName + "] keyStorePwd[HIDDEN" // +
                        // keyStorePwd
                        + "] keyStoreType[" + keyStoreType + "] keyStorePrv[" + keyStorePrv + "] + fileName[" + fn + "]");

                File fks = new File(fn);
                if (fks.canRead()) {
                    is = new FileInputStream(fks);
                }
                if (is == null) {
                    throw new IOException("File " + keyStoreName + " not found in ClassPath or in Path [" + fn + "]");
                }

                keyStore = KeyStore.getInstance(keyStoreType, keyStorePrv);
                keyStore.load(is, keyStorePwd.toCharArray());
                keyStoreMap.put(keyStoreName, keyStore);
            }
            catch (Exception exc) {
                throw new KeyStoreUtilsException("Error occurred initializing keystore '" + keyStoreName + "'", exc);
            }
            finally {
                if (is != null) {
                    try {
                        is.close();
                    }
                    catch (Exception exc2) {
                        // do nothing
                    }
                }
            }
        }
        return keyStore;
    }

    /**
     * Insert the key in a initialized keystore, or create a new keystore.
     *
     * @param keyid
     *        the key identification data
     * @param key
     *        the key to insert
     * @param certs
     *        the key certificate chain
     * @throws KeyStoreUtilsException
     *         if errors occurs
     */
    public static void writeKey(KeyID keyid, Key key, Certificate[] certs) throws KeyStoreUtilsException
    {
        KeyStoreID ksID = keyid.getKeyStoreID();
        KeyStore keyStore = null;
        try {
            keyStore = getKeyStore(ksID);
        }
        catch (Exception exc) {
            keyStore = null;
        }
        try {
            if (keyStore == null) {
                keyStore = KeyStore.getInstance(KeyStoreUtils.DEFAULT_KEYSTORE_TYPE,
                        KeyStoreUtils.DEFAULT_KEYSTORE_PROVIDER);
                keyStore.load(null, null);
            }
            keyStore.setKeyEntry(keyid.getKeyAlias(), key, keyid.getKeyPwd().toCharArray(), certs);

            String fn = PropertiesHandler.expand("sp{{gv.app.home}}" + File.separatorChar + "keystores"
                    + File.separatorChar + ksID.getKeyStoreName());

            // System.out.println("------writeKey: keyStoreName[" +
            // keyid.getKeyStoreName() + "] keyStorePwd[" +
            // keyid.getKeyStorePwd() + "] keyStoreType[" +
            // keyid.getKeyStoreType() + "] keyStorePrv[" +
            // keyid.getKeyStorePrv() + "] + fn[" + fn + "]");

            FileOutputStream fos = null;
            URL url = KeyStoreUtils.class.getClassLoader().getResource(ksID.getKeyStoreName());
            if (url != null) {
                fn = url.getFile();
            }
            File fks = new File(fn);
            fks.getParentFile().mkdirs();
            if (!fks.exists() || fks.canWrite()) {
                fos = new FileOutputStream(fks);
            }
            if (fos == null) {
                throw new IOException("File " + ksID.getKeyStoreName() + " not writable on Path [" + fn + "]");
            }

            keyStore.store(fos, ksID.getKeyStorePwd().toCharArray());
            fos.close();
        }
        catch (Exception exc) {
            throw new KeyStoreUtilsException("Error occurred inserting key '" + keyid.getKeyAlias()
                    + "' into keystore '" + ksID.getKeyStoreName() + "'", exc);
        }
    }

    /**
     * Read a key from a keystore.
     *
     * @param keyid
     *        the key identification data
     * @return the key
     * @throws KeyStoreUtilsException
     *         if errors occurs
     */
    public static Key readKey(KeyID keyid) throws KeyStoreUtilsException
    {
        KeyStoreID ksID = keyid.getKeyStoreID();
        Key key = null;
        KeyStore keyStore = getKeyStore(ksID);
        try {
            key = keyStore.getKey(keyid.getKeyAlias(), keyid.getKeyPwd().toCharArray());
            if (key == null) {
            	Certificate cert = keyStore.getCertificate(keyid.getKeyAlias());
            	if (cert != null) {
            		key = cert.getPublicKey();
            	}
            }
            keyid.setKey(key);
        }
        catch (Exception exc) {
            throw new KeyStoreUtilsException("Error occurred reading key '" + keyid.getKeyAlias()
                    + "' from the keystore '" + ksID.getKeyStoreName() + "'", exc);
        }
        return key;
    }


    /**
     *
     */
    public static void resetCache()
    {
        keyStoreMap.clear();
    }

}
