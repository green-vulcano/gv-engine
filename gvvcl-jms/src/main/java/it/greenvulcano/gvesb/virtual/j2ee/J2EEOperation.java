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
package it.greenvulcano.gvesb.virtual.j2ee;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.GVInternalException;
import it.greenvulcano.gvesb.internal.jaas.SubjectBuilder;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.Operation;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.gvesb.virtual.VCLException;

import java.rmi.RemoteException;

import javax.jms.JMSException;
import javax.naming.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * This class realizes mechanisms needed for J2EE (connection to the initial
 * context and its management: open, close, retry etc.).
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public abstract class J2EEOperation implements Operation
{
	private static final Logger logger = LoggerFactory.getLogger(J2EEOperation.class);

    /**
     * The associated key
     */
    protected OperationKey      key            = null;

    /**
     * The operation name
     */
    protected String            name           = null;

    /**
     * Helper class for managing InitialContext
     */
    protected JNDIHelper        initialContext = null;

    /**
     * Helper class for managing JAAS tasks
     */
    protected SubjectBuilder    subjectBuilder = null;

    /**
     * true if the connection was established with the application server.
     */
    protected boolean           connected      = false;

    /**
     * Keeps reference to <code>IDataProvider</code> implementation.
     */
    protected String            refDP;

    /**
     *
     * @param node
     *        the node from which read configuration data
     *
     * @throws InitializationException
     *         on initialization errors
     * @throws XMLConfigException
     *         on errors reading configuration data
     */
    protected abstract void j2eeInit(Node node) throws InitializationException, XMLConfigException;

    /**
     * Perform the operation. The context is valid and must not retry to
     * establish the connection in case of errors.
     *
     * @param data
     *        the data to use for operation
     * @return the operation result
     *
     * @throws J2EEConnectionException
     *         on connection errors
     * @throws InvalidDataException
     *         if invalid GVBuffer are used
     * @throws RemoteException
     *         if RMI errors occurs
     * @throws JMSException
     *         if JMS errors occurs
     * @throws VCLException
     *         on generic errors
     */
    protected abstract GVBuffer j2eePerform(GVBuffer data) throws J2EEConnectionException, VCLException,
            InvalidDataException, RemoteException, JMSException;

    /**
     * Called if the perform fails.
     *
     * @param data
     *        the data used for operation
     * @param exc
     *        the exception occurred
     * @return the operation result
     *
     * @throws J2EEConnectionException
     *         on connection errors
     * @throws VCLException
     *         on generic errors
     * @throws InvalidDataException
     *         if invalid GVBuffer are used
     */
    protected abstract GVBuffer j2eePerformFailed(GVBuffer data, Exception exc) throws J2EEConnectionException,
            VCLException, InvalidDataException;

    /**
     * Called after the perform.
     */
    protected abstract void j2eeCleanUp();

    /**
     * Completes operation specific destroy. The connection is still valid. This
     * method must not close the connection.
     */
    protected abstract void j2eeDestroy();

    /**
     * Called when the JNDI connection is established. Typically this method
     * performs the lookup of needed objects.
     *
     * @param context
     *        the active context
     *
     * @throws Exception
     *         on errors
     */
    protected abstract void j2eeConnectionEstablished(Context context) throws Exception;

    /**
     * Called when the connection is closing. The connection is still valid.
     * This method must not close the connection.
     */
    protected abstract void j2eeConnectionClosing();

    /**
     * Return a description to use with logs.
     *
     * @return the operation description
     */
    protected abstract String getDescription();

    /**
     *
     * @param node
     *        configuration node
     *
     * @throws InitializationException
     *         if initialization errors occurs
     */
    public final void init(Node node) throws InitializationException
    {
        try {
            name = XMLConfig.get(node, "@name");
            refDP = XMLConfig.get(node, "@ref-dp", "");
            initialContext = new JNDIHelper(XMLConfig.getNode(node, "JNDIHelper"));
            logger.debug("Initial Context properties : " + initialContext);
            Node sbNode = XMLConfig.getNode(node, "./*[@type='subject-builder']");
            if (sbNode != null) {
                subjectBuilder = SubjectBuilder.getSubjectBuilder(sbNode);
                logger.debug("Subject Builder properties : " + subjectBuilder);
            }
            j2eeInit(node);
        }
        catch (XMLConfigException exc) {
            logger.error("An error occurred reading configuration data.", exc);
            throw new InitializationException("GVVCL_XML_CONFIG_ERROR", new String[][]{{"exc", exc.toString()},
                    {"key", "N/A"}}, exc);
        }
        catch (GVInternalException exc) {
            subjectBuilder = null;
            logger.error("An error occurred initializing SubjectBuilder.", exc);
            throw new InitializationException("GVVCL_JAAS_INIT_ERROR", new String[][]{{"exc", exc.toString()},
                    {"key", "N/A"}}, exc);
        }
        try {
            if (!connected) {
                establishConnection();
            }
        }
        catch (J2EEConnectionException exc) {
            logger.warn("Cannot establish the connection. The system will retry later to establish the connection.",
                    exc);
        }
    }

    /**
     * Checks if the attribute has a valid value (not null and not empty).
     *
     * @param attributeName
     *        the attribute name to check
     * @param attributeValue
     *        the attribute value to check
     *
     * @exception InitializationException
     *            if the attribute has an invalid value.
     */
    protected final void checkAttribute(String attributeName, String attributeValue) throws InitializationException
    {
        if (attributeValue == null) {
            throw new InitializationException("GVVCL_NULL_PARAMETER_ERROR", new String[][]{{"param", attributeName}});
        }
        if (attributeValue.equals("")) {
            throw new InitializationException("GVVCL_NULL_PARAMETER_ERROR", new String[][]{{"param", attributeName}});
        }
    }

    /**
     * Establish the connection based on the configured parameters.
     *
     * @throws J2EEConnectionException
     *         on connection errors
     */
    protected final void establishConnection() throws J2EEConnectionException
    {
        logger.debug("BEGIN establishing connection to " + initialContext.getProviderURL());

        try {
            j2eeConnectionEstablished(initialContext.getInitialContext());

            if (subjectBuilder != null) {
                logger.debug("Executing SubjectBuilder login");
                subjectBuilder.login();
                logger.debug("Using Subject: " + subjectBuilder.getSubject());
            }
            connected = true;
        }
        catch (J2EEConnectionException exc) {
            logger.error("Failed to establish connection", exc);
            closeConnection();
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("Failed to establish connection with unhandled exception.", exc);
            closeConnection();
            throw new J2EEConnectionException("GVVCL_J2EE_CONNECTION_ERROR", new String[][]{
                    {"providerUrl", initialContext.getProviderURL()}, {"exc", "" + exc}}, exc);
        }
        finally {
            try {
                initialContext.close();
            }
            catch (Exception exc) {
                logger.warn("Exception closing the context. Forgive it.", exc);
            }
            logger.debug("END establishing the connection to " + initialContext.getProviderURL());
        }
    }

    /**
     * Reset the object and close the context.
     */
    protected final void closeConnection()
    {
        logger.debug("BEGIN closing connection to " + initialContext.getProviderURL());

        try {
            if (subjectBuilder != null) {
                try {
                    logger.debug("Executing SubjectBuilder logout");
                    subjectBuilder.logout();
                }
                catch (Exception exc) {
                    logger.warn("Error on logout: " + exc);
                }
            }

            j2eeConnectionClosing();

            connected = false;
        }
        finally {
            logger.debug("END closing the connection to " + initialContext.getProviderURL());
        }
    }

    /**
     * Executes the operation. Establish the connection if necessary. Subclasses
     * should call this method in their perform() methods.
     *
     * @param data
     *        the data to use for operation
     * @return the operation result
     *
     * @throws J2EEConnectionException
     *         on connection errors
     * @throws InvalidDataException
     *         if invalid GVBuffer are used
     * @throws VCLException
     *         on generic errors
     */
    protected final GVBuffer startPerform(GVBuffer data) throws J2EEConnectionException, VCLException,
            InvalidDataException
    {
        logger.debug("BEGIN: " + getDescription());
        try {
            if (!connected) {
                establishConnection();
            }

            return j2eePerform(data, true);
        }
        finally {
            logger.debug("END: " + getDescription());
        }
    }


    /**
     * Perform the operation. Retry to establish the connection if necessary.
     *
     * @param data
     *        the data to use for operation
     * @param retry
     *        if true, then in case of RemoteException or JMSException try to
     *        re-establish the connection, otherwise ends with an exception.
     * @return the operation result
     *
     * @throws J2EEConnectionException
     *         on connection errors
     * @throws InvalidDataException
     *         if invalid GVBuffer are used
     * @throws VCLException
     *         on generic errors
     */
    private GVBuffer j2eePerform(GVBuffer data, boolean retry) throws J2EEConnectionException, VCLException,
            InvalidDataException
    {
        try {
            return j2eePerform(data);
        }
        catch (RemoteException exc) {
            return j2eePerform2(data, retry, exc);
        }
        catch (JMSException exc) {
            return j2eePerform2(data, retry, exc);
        }
    }


    /**
     * Perform the operation. Retries to establish the connection if
     * <i>retry</i> parameter is set to <code>true</code>.
     *
     * @param data
     *        the data to use for operation
     * @param retry
     *        if true, then in case of RemoteException or JMSException try to
     *        re-establish the connection, otherwise ends with an exception.
     * @param exc
     *        the exception occurred
     * @return the operation result
     *
     * @throws J2EEConnectionException
     *         on connection errors
     * @throws InvalidDataException
     *         if invalid GVBuffer are used
     * @throws VCLException
     *         on generic errors
     */
    private GVBuffer j2eePerform2(GVBuffer data, boolean retry, Exception exc) throws J2EEConnectionException,
            VCLException, InvalidDataException
    {
        if (retry) {
            logger.warn("Retries to establish connection", exc);

            closeConnection();
            establishConnection();
            return j2eePerform(data, false);
        }

        logger.error("Platform error", exc);

        closeConnection();
        return j2eePerformFailed(data, exc);
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

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    public void cleanUp()
    {
        j2eeCleanUp();
    }

    /**
     * Called when an operation is discarded from cache.
     */
    public final void destroy()
    {
        j2eeDestroy();
        closeConnection();
    }

    /**
     * Finalization.
     */
    @Override
    protected void finalize()
    {
        destroy();
    }

    /**
     * Return the alias for the given service
     *
     * @param data
     *        the input service data
     * @return the configured alias
     */
    public String getServiceAlias(GVBuffer data)
    {
        return data.getService();
    }
}
