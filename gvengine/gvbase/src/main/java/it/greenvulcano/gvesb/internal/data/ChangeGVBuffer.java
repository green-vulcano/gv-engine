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
package it.greenvulcano.gvesb.internal.data;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.expression.ExpressionEvaluatorException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.internal.GVInternalException;

import it.greenvulcano.script.ScriptExecutor;
import it.greenvulcano.script.ScriptExecutorFactory;
import it.greenvulcano.util.crypto.CryptoHelper;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xpath.XPathFinder;
import it.greenvulcano.util.zip.ZipHelper;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class ChangeGVBuffer
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(ChangeGVBuffer.class);
    /**
     * define the crypto operation "none".
     */
    public static final String      CRYPTO_OP_NONE            = "none";
    /**
     * define the crypto operation "encrypt".
     */
    public static final String      CRYPTO_OP_ENCRYPT         = "encrypt";
    /**
     * define the crypto operation "decrypt".
     */
    public static final String      CRYPTO_OP_DECRYPT         = "decrypt";
    /**
     * define the compress operation "none".
     */
    public static final String      COMPRESS_OP_NONE          = "none";
    /**
     * define the compress operation "compress".
     */
    public static final String      COMPRESS_OP_COMPRESS      = "compress";
    /**
     * define the compress operation "uncompress".
     */
    public static final String      COMPRESS_OP_UNCOMPRESS    = "uncompress";
    /**
     * define the base64 operation "none".
     */
    public static final String      BASE64_OP_NONE            = "none";
    /**
     * define the base64 operation "encode".
     */
    public static final String      BASE64_OP_ENCODE          = "encode";
    /**
     * define the base64 operation "decode".
     */
    public static final String      BASE64_OP_DECODE          = "decode";

    /**
     * if true the GVBuffer body is canceled.
     */
    private boolean                 clearData                 = false;
    /**
     * the value to set as GVBuffer System field.
     */
    private String                  system                    = "";
    /**
     * the value to set as GVBuffer Service field.
     */
    private String                  service                   = "";
    /**
     * the value to set as GVBuffer Retcode field.
     */
    private int                     retCode                   = 0;
    /**
     * the names/values to set as GVBuffer properties.
     */
    private HashMap<String, String> properties                = new HashMap<String, String>();
    /**
     * defines the order of properties setting in GVBuffer.
     */
    private List<String>            propertiesList            = new ArrayList<String>();
    /**
     * if true is configured a crypto operation.
     */
    private boolean                 cryptoOn                  = false;
    /**
     * the value of the crypto operation.
     */
    private boolean                 cryptoOpEncrypt           = false;
    /**
     * the value to use as crypto key.
     */
    private String                  keyId                     = "";
    /**
     * if true is configured a compress operation.
     */
    private boolean                 compressOn                = false;
    /**
     * the value of the compress operation.
     */
    private boolean                 compressOpZip             = false;
    /**
     * the value to use as compression level level.
     */
    private String                  compressionLevel          = "";
    /**
     * ZipHelper instance.
     */
    private ZipHelper               zipHelper                 = null;
    /**
     * if true is configured a base64 operation.
     */
    private boolean                 base64On                  = false;
    /**
     * the value of the base64 operation.
     */
    private boolean                 base64OpEncode            = false;
    /**
     * if true is configured a Script operation.
     */
    private boolean                 scriptOn                  = false;
    /**
     * The script executor instance.
     */
    private ScriptExecutor          script                    = null;
    /**
     * The GVBuffer body builder.
     */
    private GVBufferBodyMaker       bodyBuilder               = null;

    /**
     * if true is configured a BodyBuild operation.
     */
    private boolean                 bodyBuilderOn             = false;

    /**
     * the name of the property to be used for body overwriting, if the
     * name is empty the feature is disabled, the feature is applied before
     * applying crypto, zip or base64 features, WARNING!!! if the clearData
     * field is true the body is emptied
     */
    private String                  overwriteBodyWithProperty = "";

    /**
     * guard for an undefined field value.
     */
    private static final String     UNDEFINED                 = "UNDEFINED";

    /**
     * Initialize the instance.
     * 
     * @param node
     *        the node from which to read the configuration
     * @throws XMLConfigException
     *         if errors occurs
     */
    public final void init(Node node) throws XMLConfigException
    {
        try {
            clearData = XMLConfig.getBoolean(node, "@clear-data", false);
            system = XMLConfig.get(node, "@system", "");
            service = XMLConfig.get(node, "@service", "");

            int operations = 0;

            String cryptoOp = XMLConfig.get(node, "@crypto-op", CRYPTO_OP_NONE);
            keyId = XMLConfig.get(node, "@key-id", CryptoHelper.DEFAULT_KEY_ID);

            if (cryptoOp.equals(CRYPTO_OP_NONE)) {
                cryptoOn = false;
            }
            else {
                cryptoOn = true;
                operations++;
                cryptoOpEncrypt = cryptoOp.equals(CRYPTO_OP_ENCRYPT);
            }

            String compressOp = XMLConfig.get(node, "@compress-op", COMPRESS_OP_NONE);
            compressionLevel = XMLConfig.get(node, "@compress-level", ZipHelper.DEFAULT_COMPRESSION_S);

            if (compressOp.equals(COMPRESS_OP_NONE)) {
                compressOn = false;
            }
            else {
                compressOn = true;
                operations++;
                compressOpZip = compressOp.equals(COMPRESS_OP_COMPRESS);
                zipHelper = new ZipHelper();
                zipHelper.setCompressionLevel(compressionLevel);
            }

            String base64Op = XMLConfig.get(node, "@base64-op", BASE64_OP_NONE);

            if (base64Op.equals(BASE64_OP_NONE)) {
                base64On = false;
            }
            else {
                base64On = true;
                operations++;
                base64OpEncode = base64Op.equals(BASE64_OP_ENCODE);
            }

            Node scriptNode = XMLConfig.getNode(node, "Script");
            if (scriptNode != null) {
                script = ScriptExecutorFactory.createSE(scriptNode);
                scriptOn = true;
                operations++;
            }

            Node bbNode = XMLConfig.getNode(node, "*[@type='body-builder']");
            if (bbNode == null) {
                bodyBuilderOn = false;
            }
            else {
                String className = XMLConfig.get(bbNode, "@class");
                bodyBuilder = (GVBufferBodyMaker) Class.forName(className).newInstance();
                bodyBuilder.init(bbNode);
                bodyBuilderOn = true;
            }

            if (operations > 1) {
                throw new XMLConfigException(
                        "A ChangeGVBuffer can't execute crypto, compress, base64 or javascript operation together. Node: "
                                + XPathFinder.buildXPath(node));
            }

            String sRetCode = XMLConfig.get(node, "./@ret-code", UNDEFINED);
            if (!sRetCode.equals(UNDEFINED)) {
                retCode = Integer.parseInt(sRetCode);
            }
            else {
                retCode = Integer.MIN_VALUE;
            }

            NodeList nl = XMLConfig.getNodeList(node, "./PropertyDef");
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    String name = XMLConfig.get(nl.item(i), "./@name");
                    String value = XMLConfig.get(nl.item(i), "./@value", "");
                    boolean overwriteBody = XMLConfig.getBoolean(nl.item(i), "./@overwrite-body", false);
                    if (overwriteBody) {
                        if (!overwriteBodyWithProperty.equals("")) {
                            throw new XMLConfigException(
                                    "The 'overwrite-body' body attribute can be set only once. Node: "
                                            + XPathFinder.buildXPath(nl.item(i)));
                        }
                        overwriteBodyWithProperty = name;
                    }
                    properties.put(name, value);
                    propertiesList.add(name);
                }
            }
        }
        catch (XMLConfigException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new XMLConfigException("Error occurred initializing ChangeGVBuffer: " + exc.getMessage(), exc);
        }
    }
    
    public void setLogger(Logger logger) {
        ChangeGVBuffer.logger = logger;
    }

    /**
     * Perform the GVBuffer modification.
     * 
     * @param gvBuffer
     *        the input data
     * @param environment
     *        the flow environment
     * @return the modified data
     * @throws GVException
     *         if errors occurs
     * @throws ExpressionEvaluatorException
     */
    public final GVBuffer execute(GVBuffer gvBuffer, Map<String, Object> environment) throws GVException,
            ExpressionEvaluatorException, InterruptedException
    {
        if (!system.equals("")) {
            gvBuffer.setSystem(system);
        }
        if (!service.equals("")) {
            gvBuffer.setService(service);
        }
        if (retCode != Integer.MIN_VALUE) {
            gvBuffer.setRetCode(retCode);
        }

        try {
            PropertiesHandler.enableExceptionOnErrors();
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            for (String fName : propertiesList) {
                if (overwriteBodyWithProperty.equals(fName)) {
                    String value = gvBuffer.getProperty(fName);
                    if (value == null) {
                        value = properties.get(fName);
                    }
                    value = PropertiesHandler.expand(value, params, gvBuffer);
                    gvBuffer.setObject(value.getBytes());
                }
                else {
                    String value = PropertiesHandler.expand(properties.get(fName), params, gvBuffer);
                    params.put(fName, value);
                    gvBuffer.setProperty(fName, value);
                }
            }
        }
        catch (Exception exc) {
            throw new GVInternalException("PROPERTIES_EXPANSION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
        }

        if (clearData) {
            gvBuffer.setObject(null);
        }
        else {
            if (bodyBuilderOn) {
                gvBuffer.setObject(bodyBuilder.getBuffer(gvBuffer));
            }
            if (cryptoOn) {
                handleEncryption(gvBuffer);
            }
            else if (compressOn) {
                handleCompression(gvBuffer);
            }
            else if (base64On) {
                handleBase64(gvBuffer);
            }
            else if (scriptOn) {
                handleScript(gvBuffer, environment);
            }
        }

        return gvBuffer;
    }

    /**
     * @param gvBuffer
     *        the GVBuffer to handle
     * @throws GVInternalException
     *         if error occurs
     */
    private void handleCompression(GVBuffer gvBuffer) throws GVInternalException
    {
        try {
            if (compressOpZip) {
                gvBuffer.setObject(zipHelper.zip((byte[]) gvBuffer.getObject()));
            }
            else {
                gvBuffer.setObject(zipHelper.unzip((byte[]) gvBuffer.getObject()));
            }
        }
        catch (Exception exc) {
            throw new GVInternalException("COMPRESS_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
    }

    /**
     * @param gvBuffer
     *        the GVBuffer to handle
     * @throws GVInternalException
     *         if error occurs
     */
    private void handleEncryption(GVBuffer gvBuffer) throws GVInternalException
    {
        try {
            if (cryptoOpEncrypt) {
                gvBuffer.setObject(CryptoHelper.encrypt(keyId, (byte[]) gvBuffer.getObject(), false));
            }
            else {
                gvBuffer.setObject(CryptoHelper.decrypt(keyId, (byte[]) gvBuffer.getObject(), false));
            }
        }
        catch (Exception exc) {
            throw new GVInternalException("CRYPTO_ERROR", new String[][]{{"keyId", keyId},
                    {"message", exc.getMessage()}}, exc);
        }
    }

    /**
     * @param gvBuffer
     *        the GVBuffer to handle
     * @throws GVInternalException
     *         if error occurs
     */
    private void handleBase64(GVBuffer gvBuffer) throws GVInternalException
    {
        try {
            if (base64OpEncode) {
                gvBuffer.setObject(Base64.getEncoder().encode((byte[]) gvBuffer.getObject()));
            }
            else {
                gvBuffer.setObject(Base64.getDecoder().decode((byte[]) gvBuffer.getObject()));
            }
        }
        catch (Exception exc) {
            throw new GVInternalException("BASE64_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
    }

    /**
     * @param gvBuffer
     *        the data to handle
     * @param environment
     *        the flow environment
     * @throws GVInternalException
     *         if error occurs
     */
    private void handleScript(GVBuffer gvBuffer, Map<String, Object> environment) throws GVInternalException, InterruptedException
    {
        try {
            script.putProperty("environment", environment);
            script.putProperty("data", gvBuffer);
            script.putProperty("logger", logger);
            script.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true), gvBuffer);
        }
        catch (Exception exc) {
            ThreadUtils.checkInterrupted(exc);
            throw new GVInternalException("SCRIPT_ERROR", new String[][]{{"engine", script.getEngineName()},
                    {"script", script.getScriptName()}, {"message", exc.toString()}}, exc);
        }
    }

    /**
     * Perform cleanup operations.
     * 
     */
    public void cleanUp()
    {
        if (bodyBuilder != null) {
            bodyBuilder.cleanUp();
        }
        if (script != null) {
            script.cleanUp();
        }
    }
    
    public void destroy()
    {
        if (script != null) {
            script.destroy();
        }
        script = null;
        bodyBuilder = null;
    }
    
}
