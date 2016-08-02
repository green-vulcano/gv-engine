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
package it.greenvulcano.gvesb.statistics.plugin;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.gvesb.statistics.GVStatisticsException;
import it.greenvulcano.gvesb.statistics.IStatisticsWriter;
import it.greenvulcano.gvesb.statistics.StatisticsData;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JMSStatisticsWriter implements IStatisticsWriter
{
    private static final Logger   logger                = LoggerFactory.getLogger(JMSStatisticsWriter.class);

    private JNDIHelper          jndiHelper          = null;
    private String              connectionFactory   = null;
    private String              queueName           = null;
    private QueueConnection     queueConnection;
    private QueueSession        qsession;
    private Queue               queue;
    private QueueSender         qsender;

    /**
     * Initialization method for the JMS configuration/connection
     *
     * @param node
     * @throws GVStatisticsException
     */
    @Override
    public void init(Node node) throws GVStatisticsException
    {
        logger.debug("JMSStatisticsWriter init");
        try {
            jndiHelper = new JNDIHelper(XMLConfig.getNode(node, "JNDIHelper"));
            connectionFactory = XMLConfig.get(node, "@connectionFactory");
            queueName = XMLConfig.get(node, "@queue");
            logger.info("JMSStatisticsWriter - connectionFactory: " + connectionFactory + " - queueName: " + queueName);
            startJMS();
        }
        catch (Exception exc) {
            logger.error("Error initializing JMSStatisticsWriter", exc);
            throw new GVStatisticsException("Error initializing JMSStatisticsWriter", exc);
        }
    }

    /**
     * This method get the statistics data information and write them on JMS
     * queue support
     *
     * @param statisticsData
     * @return
     * @throws GVStatisticsException
     */
    @Override
    public boolean writeStatisticsData(StatisticsData statisticsData) throws GVStatisticsException
    {
        boolean storeFalg = false;
        logger.debug("JMS writeStatisticsData START");
        ObjectMessage statisticsMsg = null;
        try {
            statisticsMsg = qsession.createObjectMessage(statisticsData);
            qsender.send(statisticsMsg);
            storeFalg = true;
        }
        catch (JMSException exc) {
            try {
                closeJMS();
                startJMS();
                statisticsMsg = qsession.createObjectMessage(statisticsData);
                qsender.send(statisticsMsg);
                storeFalg = true;
            }
            catch (JMSException exc2) {
                logger.error("An error occurred storing JMS Message.", exc2);
                throw new GVStatisticsException("GVSTATISTICS_JMSSTORE_ERROR", new String[][]{{"exception",
                        exc2.getMessage()}});
            }
        }
        logger.debug("JMS writeStatisticsData STOP");
        return storeFalg;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.statistics.IStatisticsWriter#destroy()
     */
    @Override
    public void destroy()
    {
        // do nothing
    }

    /**
     *
     * @throws GVStatisticsException
     */
    private void startJMS() throws GVStatisticsException
    {
        try {
            queueConnection = ((QueueConnectionFactory) jndiHelper.lookup(connectionFactory)).createQueueConnection();
            queue = (Queue) jndiHelper.lookup(queueName);
            qsession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            qsender = qsession.createSender(queue);
            queueConnection.start();
        }
        catch (Exception exc) {
            throw new GVStatisticsException("Error initializing JMSStatisticsWriter", exc);
        }
        finally {
            try {
                jndiHelper.close();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
    }

    /**
     *
     */
    private void closeJMS()
    {
        try{
            if (qsender != null)
               qsender.close();
        }
        catch (Exception exc) {
            // do nothing
        }
        try{
            if (qsession != null)
                qsession.close();
        }
        catch (Exception exc) {
            // do nothing
        }
        try{
            if (queueConnection != null)
                queueConnection.close();
        }
        catch (Exception exc) {
            // do nothing
        }
        qsender = null;
        qsession = null;
        queueConnection = null;
    }
}
