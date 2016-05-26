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
package it.greenvulcano.gvesb.virtual.internal;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.internal.data.ChangeGVBuffer;
import it.greenvulcano.gvesb.internal.log.GVDump;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.util.thread.ThreadUtils;

import java.io.OutputStreamWriter;

import org.w3c.dom.Node;

/**
 * This class is used only for test.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class TestServiceCall implements CallOperation
{
    private static final org.slf4j.Logger logger           = org.slf4j.LoggerFactory.getLogger(TestServiceCall.class);

    /**
     *
     */
    protected OperationKey      key              = null;

    private String              service          = null;
    private String              exceptionMessage = "";
    private boolean             removeProperties = false;
    private ChangeGVBuffer      cGVBuffer        = null;
    private long                sleepOnPerform;
    private String              refDP            = "";

    /**
     *
     * @param node
     * @throws InitializationException
     */
    public void init(Node node) throws InitializationException
    {
        try {
            service = XMLConfig.get(node, "@service", "toupper");
            exceptionMessage = XMLConfig.get(node, "@exception-message", "");
            removeProperties = XMLConfig.getBoolean(node, "@remove-properties", false);
            long sleepOnInit = XMLConfig.getLong(node, "@sleep-on-init", -1);
            sleepOnPerform = XMLConfig.getLong(node, "@sleep-on-perform", -1);
            refDP = XMLConfig.get(node, "@ref-dp", "");
            Node cGVBufferNode = XMLConfig.getNode(node, "ChangeGVBuffer");
            if (cGVBufferNode != null) {
                cGVBuffer = new ChangeGVBuffer();
                cGVBuffer.setLogger(logger);
                cGVBuffer.init(cGVBufferNode);
            }
            if (sleepOnInit > 0) {
                try {
                    Thread.sleep(sleepOnInit);
                }
                catch (InterruptedException exc) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        catch (XMLConfigException exc) {
            logger.error("An error occurred reading configuration data.", exc);
            throw new InitializationException("GVVCL_XML_CONFIG_ERROR", new String[][]{{"exc", exc.toString()},
                    {"key", key.toString()}}, exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws CallException, InterruptedException
    {
        GVBuffer localData = gvBuffer;

        if (localData == null) {
            return null;
        }

        String encoding = localData.getProperty("encoding");
        if (encoding == null) {
            OutputStreamWriter osw = new OutputStreamWriter(System.out);
            encoding = osw.getEncoding();
        }

        logger.debug("---------------------------------------------------------------");
        logger.debug(" TEST SERVICE.....: " + service);
        logger.debug(" encoding.........: " + encoding);
        logger.debug(" removeProperties.: " + removeProperties);
        logger.debug("---------------------------------------------------------------");
        logger.debug(new GVDump(localData).toString());

        if (sleepOnPerform > 0) {
            try {
                Thread.sleep(sleepOnPerform);
            }
            catch (Exception exc) {
                logger.error("TEST SERVICE TIMEOUT FAILED", exc);
                ThreadUtils.checkInterrupted(exc);
            }
        }

        if (!exceptionMessage.equals("")) {
            logger.debug(" TEST SERVICE: thrown exception");
            throw new CallException(exceptionMessage);
        }

        try {
            if (removeProperties) {
                localData = new GVBuffer(gvBuffer.getSystem(), gvBuffer.getService(), gvBuffer.getId());
                localData.setObject(gvBuffer.getObject());
                localData.setRetCode(gvBuffer.getRetCode());
            }

            if (cGVBuffer != null) {
                return cGVBuffer.execute(localData, null);
            }

            if (service.equals("toupper")) {
                return toupper(localData, encoding);
            }
            else if (service.equals("tolower")) {
                return tolower(localData, encoding);
            }
            else {
                return localData;
            }
        }
        catch (Exception exc) {
            logger.error("TEST SERVICE FAILED", exc);
            ThreadUtils.checkInterrupted(exc);
            throw new CallException("TEST SERVICE FAILED", exc);
        }
        finally {
            logger.debug("---------------------------------------------------------------");
        }
    }


    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    public OperationKey getKey()
    {
        return key;
    }

    private GVBuffer tolower(GVBuffer gvBuffer, String encoding) throws Exception
    {
        try {
            String str = getWorkingString(gvBuffer, encoding);
            str = str.toLowerCase();
            gvBuffer.setObject(str);
        }
        catch (GVException exc) {
            gvBuffer.setRetCode(exc.getErrorCode());
        }
        return gvBuffer;
    }

    private GVBuffer toupper(GVBuffer gvBuffer, String encoding) throws Exception
    {
        try {
            String str = getWorkingString(gvBuffer, encoding);
            String ucStr = str.toUpperCase();
            gvBuffer.setObject(ucStr);
        }
        catch (GVException exc) {
            gvBuffer.setRetCode(exc.getErrorCode());
        }
        return gvBuffer;
    }

    private String getWorkingString(GVBuffer gvBuffer, String encoding) throws Exception
    {
        String str = null;
        if (!refDP.equals("")) {
            DataProviderManager dataProviderManager = DataProviderManager.instance();
            IDataProvider dataProvider = dataProviderManager.getDataProvider(refDP);
            try {
                dataProvider.setObject(gvBuffer);
                str = (String) dataProvider.getResult();
            }
            finally {
                dataProviderManager.releaseDataProvider(refDP, dataProvider);
            }
        }
        else {
            Object currentObject = gvBuffer.getObject();
            if (currentObject instanceof String) {
                str = (String) currentObject;
            }
            else if (currentObject instanceof byte[]) {
                str = new String((byte[]) currentObject, encoding);
            }
            else {
                str = currentObject.toString();
            }
        }
        return str;
    }

    /**
     * Called when an operation is discarded from cache.
     */
    public void destroy()
    {
        if (cGVBuffer != null) {
            cGVBuffer.destroy();
        }
        cGVBuffer = null;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    public void cleanUp()
    {
        if (cGVBuffer != null) {
            cGVBuffer.cleanUp();
        }
    }

    /**
     * Return the alias for the given service.
     *
     * @param gvBuffer
     *        the input service GVBuffer
     * @return the configured alias
     */
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }
}
