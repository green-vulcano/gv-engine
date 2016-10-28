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

import it.greenvulcano.gvesb.j2ee.XAHelper;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.XAQueueConnection;
import javax.jms.XATopicConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * JMSData class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author Gianluca Di Maio
 *
 *
 *
 */
public class JMSData implements ExceptionListener
{
    /**
     * The Logger instance must be provided by the caller.
     */
	 private static final Logger logger = LoggerFactory.getLogger(JMSData.class);

    /**
     * the associated Connection
     */
    private Connection           connection    = null;
    /**
     * the created QueueSessions
     */
    private Map<String, Session> queueSessions = new HashMap<String, Session>();
    /**
     * the created TopicSessions
     */
    private Map<String, Session> topicSessions = new HashMap<String, Session>();
    /**
     * the instance id
     */
    private String               id            = "";
    /**
     * the Connection key
     */
    private JMSConnectionKey     key           = null;
    /**
     * the associated HAHelper instance
     */
    private XAHelper             xaHelper      = null;
    /**
     * flag indicating if the JMSData object actually handles a QueueConnection.
     */
    private boolean              queue         = false;

    /**
     * @param conn
     *        the connection to handle
     * @param cKey
     *        the connection key
     * @param currID
     *        the id used to identify valid instances
     * @param xaH
     *        the associated XAHelper instance
     * @param queue
     *        true if handling a QueueConnection
     * @throws JMSException
     *         if unable to register exception listener
     */
    public JMSData(Connection conn, JMSConnectionKey cKey, String currID, XAHelper xaH, boolean queue)
            throws JMSException
    {
        logger.debug("Created JMSData key: " + cKey + " - id: " + currID + " - connection: " + conn + " - xahelper: "
                + xaH);
        connection = conn;
        key = cKey;
        id = currID;
        xaHelper = xaH;
        if (key.isUsingVCLPooling()) {
            try {
                connection.setExceptionListener(this);
            }
            catch (Exception exc) {
                logger.warn("Cannot set this object as JMS ExceptionListener", exc);
            }
        }
        this.queue = queue;
    }

    /**
     * Close the Sessions and Connection
     */
    public void close()
    {
        logger.debug("Closing JMSData " + this);
        try {
            logger.debug("Closing Connection on JMSData " + this);
            connection.close();
            logger.debug("Closed Connection on JMSData " + this);
        }
        catch (Exception exc) {
            // do nothing
        }
        logger.debug("Closed JMSData " + this);
    }

    /**
     * @return Returns the connection.
     */
    public Connection getConnection()
    {
        return connection;
    }

    /**
     * @param transacted
     *        if true, the session is transacted.
     * @param acknowledge
     *        indicates whether the consumer or the client will acknowledge any
     *        messages it receives. This parameter will be ignored if the
     *        session is transacted.
     * @return Returns the qSession.
     * @throws JMSException
     *         if errors occurs during Session creation
     */
    public Session getQSession(boolean transacted, int acknowledge) throws JMSException
    {
        String sKey = "" + transacted + acknowledge;
        logger.debug("Requested QueueSession key: " + sKey + " on JMSData " + this);
        Session qSession = queueSessions.get(sKey);
        if (qSession == null) {
            if ((transacted && (acknowledge == 0)) && (connection instanceof XAQueueConnection)) {
                qSession = ((XAQueueConnection) connection).createXAQueueSession();
            }
            else if (connection instanceof QueueConnection) {
                qSession = ((QueueConnection) connection).createQueueSession(transacted, acknowledge);
            }
            else {
                throw new JMSException("The Connection isn't a QueueConnection");
            }
            queueSessions.put(sKey, qSession);
        }
        return qSession;
    }

    /**
     * @param transacted
     *        if true, the session is transacted.
     * @param acknowledge
     *        indicates whether the consumer or the client will acknowledge any
     *        messages it receives. This parameter will be ignored if the
     *        session is transacted.
     * @return Returns the tSession.
     * @throws JMSException
     *         if errors occurs during Session creation
     */
    public Session getTSession(boolean transacted, int acknowledge) throws JMSException
    {
        String sKey = "" + transacted + acknowledge;
        logger.debug("Requested TopicSession key: " + sKey + " on JMSData " + this);
        Session tSession = topicSessions.get(key);
        if (tSession == null) {
            if ((transacted && (acknowledge == 0)) && (connection instanceof XATopicConnection)) {
                tSession = ((XATopicConnection) connection).createXATopicSession();
            }
            else if (connection instanceof TopicConnection) {
                tSession = ((TopicConnection) connection).createTopicSession(transacted, acknowledge);
            }
            else {
                throw new JMSException("The Connection isn't a TopicConnection");
            }
            topicSessions.put(sKey, tSession);
        }
        return tSession;
    }

    /**
     * @return Returns the id.
     */
    public String getId()
    {
        return id;
    }

    /**
     * @return Returns the xaHelper.
     */
    public XAHelper getXAHelper()
    {
        return xaHelper;
    }

    /**
     * @return Returns the key.
     */
    public JMSConnectionKey getKey()
    {
        return key;
    }

    /**
     * @return Returns the queue.
     */
    public boolean isQueue()
    {
        return queue;
    }

    /**
     * Invalidate the instance
     */
    public void invalidate()
    {
        logger.debug("Invalidating JMSData " + this);
        id = "";
    }

    /**
     * @see javax.jms.ExceptionListener#onException(javax.jms.JMSException)
     */
    public void onException(JMSException exc)
    {
        logger.warn("Catched Exception from JMS Provider, this instance is invalidated - JMSData " + this + " - "+ exc);
        invalidate();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "key: " + key + " - id: " + id + " - connection: " + connection + " - xahelper: " + xaHelper;
    }

}
