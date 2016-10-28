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

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.gvesb.j2ee.XAHelper;
import it.greenvulcano.gvesb.j2ee.XAHelperException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.util.thread.ThreadMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.transaction.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class manages JMS connections in order to avoid to allocate too many
 * connections.
 * <p>
 * The JMSConnectionManages allocates a single connection for each
 * application-server/connection-factory pair.
 * <p>
 * Each connection is associated to the operations that use them.
 * <p>
 * When a connection is released the operation is unassociated from the
 * connection and when the connection has no associated operation, it will be
 * closed.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public final class JMSConnectionManager implements ConfigurationListener
{
    /**
     * The Logger instance must be provided by the caller.
     */
    private static final Logger                              logger             = LoggerFactory.getLogger(JMSConnectionManager.class);

    /**
     * the currently valid id
     */
    private String                                           id                 = "";
    /**
     * Map(JMSConnectionKey, LinkedList)
     */
    private Map<JMSConnectionKey, LinkedList<JMSData>>       queueConnections   = null;
    /**
     * Map(JMSConnectionKey, LinkedList)
     */
    private Map<JMSConnectionKey, LinkedList<JMSData>>       topicConnections   = null;
    /**
     * the configuration files used to read data
     */
    private Set<String>                                      filesToCheck       = null;
    /**
     * the jms objects involved in transactions
     */
    private Set<JMSDataXASynchronization>                    xaSynchronizations = null;
    /**
     * the jms objects in use
     */
    private Set<JMSData>                                     inUseConnections   = null;
    /**
     * the jms objects in use
     */
    private Map<Transaction, Map<JMSConnectionKey, JMSData>> xaInUseConnections = null;

    /**
     * singleton instance
     */
    private static JMSConnectionManager                      _instance          = null;

    /**
     * Constructor
     */
    private JMSConnectionManager()
    {
        id = (new Id()).toString();
        queueConnections = Collections.synchronizedMap(new HashMap<JMSConnectionKey, LinkedList<JMSData>>());
        topicConnections = Collections.synchronizedMap(new HashMap<JMSConnectionKey, LinkedList<JMSData>>());
        xaSynchronizations = Collections.synchronizedSet(new HashSet<JMSDataXASynchronization>());
        inUseConnections = Collections.synchronizedSet(new HashSet<JMSData>());
        xaInUseConnections = Collections.synchronizedMap(new HashMap<Transaction, Map<JMSConnectionKey, JMSData>>());
        filesToCheck = Collections.synchronizedSet(new HashSet<String>());
    }

    /**
     * Singleton entry point
     *
     * @return the instance
     */
    public static JMSConnectionManager instance()
    {
        if (_instance == null) {
            _instance = new JMSConnectionManager();
            XMLConfig.addConfigurationListener(_instance);
        }
        return _instance;
    }

    /**
     * Retrieve a QueueConnection for a given connectionFactory looking in the
     * given context.
     *
     * @param initialContext
     *        the JNDIHelper instance to be used for lookups
     * @param key
     *        the jms key
     * @param opKey
     *        the VMOperation key
     * @param xaHelper
     *        the XAHelper instance
     * @return the JMSData instance
     * @exception Exception
     *            if error occurs
     */
    public JMSData getQueueConnection(JNDIHelper initialContext, JMSConnectionKey key, OperationKey opKey,
            XAHelper xaHelper) throws Exception
    {
        String connectionFactory = key.getConnectionFactory();
        logger.debug("BEGIN getQueueConnection: connection factory: " + connectionFactory);

        try {

            // Proviamo a cercare una connessione associata alla transazione
            // corrente
            //
            JMSData jmsData = getXAInUseConnection(xaHelper, key);
            if (jmsData != null) {
                return jmsData;
            }

            // Nessuna connessione associata alla transazione corrente:
            // proviamo a cercare nella ThreadMap
            //
            jmsData = (JMSData) ThreadMap.get(key);
            if (jmsData != null) {
                return jmsData;
            }

            // Nessuna connessione di chiave data � stata mai utilizzata.
            // Estraiamola dal pool
            //
            LinkedList<JMSData> qConnectionPool;
            synchronized (this) {
                qConnectionPool = queueConnections.get(key);
                if (qConnectionPool == null) {
                    qConnectionPool = new LinkedList<JMSData>();
                    queueConnections.put(key, qConnectionPool);
                }
                if (qConnectionPool.size() > 0) {
                    jmsData = getFromPool(qConnectionPool);
                }
            }

            if (jmsData == null) {
                // Nessuna connessione nel pool: ne creiamo una nuova (diamo
                // tempo 15 minuti)
                //
                jmsData = createQueueConnection(initialContext, key, opKey, xaHelper, connectionFactory);

            }

            assignConnection(jmsData);

            return jmsData;
        }
        catch (Exception exc) {
            logger.error("EXCEPTION on getQueueConnection: connection factory: " + connectionFactory, exc);
            throw exc;
        }
        finally {
            try {
                initialContext.close();
            }
            catch (Exception exc) {
                // do nothing
            }
            logger.debug("END getQueueConnection: connection factory: " + connectionFactory);
        }
    }

    /**
     * Retreive a TopicConnection for a given connectionFactory looking in the
     * given context.
     *
     * @param initialContext
     *        the JNDIHelper intance to be used for lookups
     * @param key
     *        the jms key
     * @param opKey
     *        the VMOperation key
     * @param xaHelper
     *        the XAHelper instance
     * @return the JMSData instance
     * @exception Exception
     *            if error occurs
     */
    public JMSData getTopicConnection(JNDIHelper initialContext, JMSConnectionKey key, OperationKey opKey,
            XAHelper xaHelper) throws Exception
    {
        String connectionFactory = key.getConnectionFactory();
        logger.debug("BEGIN getTopicConnection: connection factory: " + connectionFactory);

        try {

            // Proviamo a cercare una connessione associata alla transazione
            // corrente
            //
            JMSData jmsData = getXAInUseConnection(xaHelper, key);
            if (jmsData != null) {
                return jmsData;
            }

            // Nessuna connessione associata alla transazione corrente:
            // proviamo a cercare nella ThreadMap
            //
            jmsData = (JMSData) ThreadMap.get(key);
            if (jmsData != null) {
                return jmsData;
            }

            // Nessuna connessione di chiave data � stata mai utilizzata.
            // Estraiamola dal pool
            //
            LinkedList<JMSData> tConnectionPool;
            synchronized (this) {
                tConnectionPool = topicConnections.get(key);
                if (tConnectionPool == null) {
                    tConnectionPool = new LinkedList<JMSData>();
                    topicConnections.put(key, tConnectionPool);
                }
                if (tConnectionPool.size() > 0) {
                    jmsData = getFromPool(tConnectionPool);
                }
            }

            if (jmsData == null) {
                // Nessuna connessione nel pool: ne creiamo una nuova (diamo
                // tempo 15 minuti)
                //
                jmsData = createTopicConnection(initialContext, key, opKey, xaHelper, connectionFactory);
            }

            assignConnection(jmsData);

            return jmsData;
        }
        catch (Exception exc) {
            logger.error("EXCEPTION on getTopicConnection: connection factory: " + connectionFactory, exc);
            throw exc;
        }
        finally {
            try {
                initialContext.close();
            }
            catch (Exception exc) {
                // do nothing
            }
            logger.debug("END getTopicConnection: connection factory: " + connectionFactory);
        }
    }

    /**
     * @param initialContext
     * @param key
     * @param opKey
     * @param xaHelper
     * @param connectionFactory
     * @return
     * @throws NamingException
     * @throws JMSException
     */
    private JMSData createQueueConnection(JNDIHelper initialContext, JMSConnectionKey key, OperationKey opKey,
            XAHelper xaHelper, String connectionFactory) throws NamingException, JMSException
    {
        logger.debug("Creating QueueConnection: connection factory: " + connectionFactory);

        /*
        QueueConnectionFactory qconFactory = (QueueConnectionFactory) initialContext.lookup(connectionFactory);
        QueueConnection qconnection = null;

        if (qconFactory instanceof XAQueueConnectionFactory) {
            qconnection = ((XAQueueConnectionFactory) qconFactory).createXAQueueConnection();
        }
        else {
            qconnection = qconFactory.createQueueConnection();
        }
        */
        
        Connection qconnection = ConnectionFactory.class.cast(initialContext.lookup(connectionFactory)).createConnection();
        qconnection.start();
                
        JMSData jmsData = new JMSData(qconnection, key, id, xaHelper, true);
        filesToCheck.add(opKey.getFile());
        return jmsData;
    }

    /**
     * @param initialContext
     * @param key
     * @param opKey
     * @param xaHelper
     * @param connectionFactory
     * @return
     * @throws NamingException
     * @throws JMSException
     */
    private JMSData createTopicConnection(JNDIHelper initialContext, JMSConnectionKey key, OperationKey opKey,
            XAHelper xaHelper, String connectionFactory) throws NamingException, JMSException
    {
        logger.debug("Creating TopicConnection: connection factory: " + connectionFactory);
        /*
        TopicConnectionFactory tconFactory = (TopicConnectionFactory) initialContext.lookup(connectionFactory);
        TopicConnection tconnection = null;

        if (tconFactory instanceof XATopicConnectionFactory) {
            tconnection = ((XATopicConnectionFactory) tconFactory).createXATopicConnection();
        }
        else {
            tconnection = tconFactory.createTopicConnection();
        }*/
        
        Connection tconnection = ConnectionFactory.class.cast(initialContext.lookup(connectionFactory)).createConnection();
        tconnection.start();
        JMSData jmsData = new JMSData(tconnection, key, id, xaHelper, false);
        filesToCheck.add(opKey.getFile());
        return jmsData;
    }

    /**
     * Extract a correct JMSData instance from the pool
     *
     * @param tConnectionPool
     *        the pool to analize
     * @return the found instance, if any
     */
    private JMSData getFromPool(LinkedList<JMSData> tConnectionPool)
    {
        while (true) {
            if (tConnectionPool.size() == 0) {
                return null;
            }

            JMSData jmsData = tConnectionPool.removeFirst();
            if (jmsData != null) {
                if (id.equals(jmsData.getId())) {
                    return jmsData;
                }
                try {
                    jmsData.close();
                }
                catch (Exception exc) {
                    logger.debug("Error closing an unused connection", exc);
                }
            }
        }
    }

    /**
     * Release an unused connection
     *
     * @param jmsData
     *        the instance to release
     */
    public synchronized void releaseConnection(JMSData jmsData)
    {
        JMSConnectionKey key = jmsData.getKey();
        ThreadMap.remove(key);
        if (key.isUsingVCLPooling()) {
            // sono presenti solo le instanze non utilizzate in una transazione
            if (inUseConnections.contains(jmsData)) {
                reinsertConnection(jmsData);
            }
        }
        else {
            // non stiamo usando il pool: chiudiamo senza pieta' la connessione
            jmsData.close();
        }
    }

    /**
     * Release an unused connection involved in a transaction
     *
     * @param xaS
     *        the synchronization object
     */
    public synchronized void xaReleaseConnection(JMSDataXASynchronization xaS)
    {
        xaSynchronizations.remove(xaS);
        JMSData jmsData = xaS.getJmsData();
        Transaction transaction = xaS.getTransaction();
        if (transaction != null) {
            logger.debug("Removing from xaInUseConnections JMSData for transaction: " + transaction);
            xaInUseConnections.remove(transaction);
        }
        else {
            logger.error("Unable to remove from xaInUseConnections JMSData: " + jmsData);
        }
        reinsertConnection(jmsData);
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    public void configurationChanged(ConfigurationEvent event)
    {
        if (event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) {
            String file = event.getFile();
            if (filesToCheck.contains(file)) {
                logger.debug("Reloaded file " + file);
                id = (new Id()).toString();
            }
        }
    }

    /**
     * @param jmsData
     *        JMSData instance to process
     * @throws Exception
     *         if errors occurs
     */
    private synchronized void assignConnection(JMSData jmsData) throws Exception
    {
        if (jmsData.getKey().isUsingVCLPooling()) {
            try {
                XAHelper xaH = jmsData.getXAHelper();
                if (xaH.isTransactionActive()) {
                    JMSDataXASynchronization xaS = new JMSDataXASynchronization(jmsData);
                    xaH.registerSynchronization(xaS);
                    xaSynchronizations.add(xaS);
                    setXAInUseConnection(jmsData);
                }
                else {
                    logger.debug("Insert in InUseConnections map JMSData " + jmsData);
                    inUseConnections.add(jmsData);
                }

                ThreadMap.put(jmsData.getKey(), jmsData);
            }
            catch (Exception exc) {
                reinsertConnection(jmsData);
                throw exc;
            }
        }
        else {
            // Do nothing: non stiamo utilizzando il pool del VCL, quindi
            // non memorizziamo da nessuna parte la connessione.
            // Al momento del rilascio la chiuderemo e basta.
        }
    }

    /**
     * Insert the given object in the correct pool, or close the associated
     * connection if 'id' has changed
     *
     * @param jmsData
     *        JMSData instance to process
     */
    private void reinsertConnection(JMSData jmsData)
    {
        if (id.equals(jmsData.getId())) {
            if (jmsData.isQueue()) {
                logger.debug("Reinsert in QueueConnection pool JMSData " + jmsData);
                LinkedList<JMSData> qConnectionPool = queueConnections.get(jmsData.getKey());
                qConnectionPool.addLast(jmsData);
            }
            else {
                logger.debug("Reinsert in TopicConnection pool JMSData " + jmsData);
                LinkedList<JMSData> tConnectionPool = topicConnections.get(jmsData.getKey());
                tConnectionPool.addLast(jmsData);
            }
            inUseConnections.remove(jmsData);
        }
        else {
            logger.debug("Closing JMSData " + jmsData);
            jmsData.close();
        }
    }

    private void setXAInUseConnection(JMSData jmsData) throws XAHelperException
    {
        Transaction transaction = jmsData.getXAHelper().getTransaction();
        if (transaction != null) {
            synchronized (this) {
                Map<JMSConnectionKey, JMSData> connections = xaInUseConnections.get(transaction);
                if (connections == null) {
                    connections = new HashMap<JMSConnectionKey, JMSData>();
                    xaInUseConnections.put(transaction, connections);
                }
                logger.debug("Insert in XAInUseConnections map JMSData " + jmsData);
                connections.put(jmsData.getKey(), jmsData);
            }
        }
    }

    private JMSData getXAInUseConnection(XAHelper xaHelper, JMSConnectionKey key) throws XAHelperException
    {
        Transaction transaction = xaHelper.getTransaction();
        if (transaction == null) {
            // Non in transazione: nessuna connessione
            //
            return null;
        }
        synchronized (this) {
            Map<JMSConnectionKey, JMSData> connections = xaInUseConnections.get(transaction);
            if (connections != null) {
                // Connessione di chiave data associata a questa transazione
                //
                return connections.get(key);
            }
        }

        // Nessuna connessione di chiave data associata a questa transazione
        //
        return null;
    }
}
