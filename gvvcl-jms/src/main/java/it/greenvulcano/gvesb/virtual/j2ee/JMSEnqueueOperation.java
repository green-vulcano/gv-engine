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
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.gvdp.DataProviderException;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.j2ee.XAHelper;
import it.greenvulcano.gvesb.j2ee.jms.JMSMessageDecorator;
import it.greenvulcano.gvesb.j2ee.jms.JMSMessageDump;
import it.greenvulcano.gvesb.virtual.EnqueueOperation;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.VCLException;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.XAQueueSession;
import javax.jms.XASession;
import javax.jms.XATopicSession;
import javax.naming.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * This class realizes an enqueue mechanism for a JMS queue or topic.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JMSEnqueueOperation extends J2EEOperation implements EnqueueOperation
{

	private static final Logger logger = LoggerFactory.getLogger(JMSEnqueueOperation.class);

    /**
     * If true the the operation will use queues, otherwise will use topics.
     */
    private boolean             isQueue     = false;

    private String              connectionFactory;
    private String              destinationName;
    private boolean             transacted;
    private int                 acknowledge;
    private int                 deliveryMode;
    private int                 priority;
    private long                ttl;
    private boolean             useVCLPooling;
    private boolean             invalidateOnReinsert;
    
    private Session session = null;

  //private Queue               queue       = null;
  //private Topic               topic       = null;
    
    private JMSConnectionKey    jmsKey      = null;
    private JMSData            jmsData     = null;  
    
    private XAHelper            xaHelper    = null;

    /**
     * If true the produced message is dumped on log.
     */
    private boolean             dumpMessage = false;
    /**
     * If true the message is enriched with GVBuffer properties.
     */
    private boolean             decorateMessage = true; 

    /**
     * @see it.greenvulcano.gvesb.virtual.j2ee.J2EEOperation#j2eeInit(org.w3c.dom.Node)
     */
    @Override
    protected void j2eeInit(Node node) throws InitializationException, XMLConfigException
    {
        connectionFactory = XMLConfig.get(node, "@connection-factory");
        checkAttribute("connection-factory", connectionFactory);
        logger.debug("Connection factory.....: " + connectionFactory);

        String destinationType = XMLConfig.get(node, "@destination-type", "queue");
        checkAttribute("destination-type", destinationType);
        isQueue = destinationType.equals("queue");
        logger.debug("Destination type.......: " + destinationType);
        destinationName = XMLConfig.get(node, "@destination-name");
        checkAttribute("destination-name", destinationName);
        logger.debug("Destination name.......: " + destinationName);

        String transactedStr = XMLConfig.get(node, "@transacted", "true");
        checkAttribute("transacted", transactedStr);
        transacted = transactedStr.equals("true");
        logger.debug("Transacted.............: " + transacted);

        String poolStr = XMLConfig.get(node, "@use-vcl-pool", "true");
        checkAttribute("use-vcl-pool", poolStr);
        useVCLPooling = poolStr.equals("true");
        logger.debug("Use VCL pooling........: " + useVCLPooling);
        String invConnStr = XMLConfig.get(node, "@invalidate-conn-on-pool-insertion", "false");
        checkAttribute("invalidate-conn-on-pool-insertion", invConnStr);
        invalidateOnReinsert = invConnStr.equals("true");
        logger.debug("Invalidate Connection on VCL pool insertion: " + invalidateOnReinsert);

        String acknowledgeType = XMLConfig.get(node, "@acknowledge-type", "auto-acknowledge");
        checkAttribute("acknowledge-type", acknowledgeType);
        if (acknowledgeType.equals("auto-acknowledge")) {
            acknowledge = Session.AUTO_ACKNOWLEDGE;
        }
        else if (acknowledgeType.equals("client-acknowledge")) {
            acknowledge = Session.CLIENT_ACKNOWLEDGE;
        }
        else if (acknowledgeType.equals("dups-ok-acknowledge")) {
            acknowledge = Session.DUPS_OK_ACKNOWLEDGE;
        }
        else {
            acknowledge = Session.AUTO_ACKNOWLEDGE;
        }
        logger.debug("Acknowledge type.......: " + acknowledgeType);

        String deliveryModeStr = XMLConfig.get(node, "@delivery-mode", "persistent");
        checkAttribute("delivery-mode", deliveryModeStr);
        if (deliveryModeStr.equals("persistent")) {
            deliveryMode = DeliveryMode.PERSISTENT;
        }
        else if (deliveryModeStr.equals("non-persistent")) {
            deliveryMode = DeliveryMode.NON_PERSISTENT;
        }
        else {
            deliveryMode = DeliveryMode.PERSISTENT;
        }
        logger.debug("Delivery mode..........: " + deliveryModeStr);
        priority = XMLConfig.getInteger(node, "@priority", 4);
        logger.debug("Priority...............: " + priority);
        ttl = XMLConfig.getLong(node, "@time-to-live", 1000 * 3600 * 24 * 30 * 12);
        logger.debug("Time to live (ms)......: " + ttl);

        Node xaHelperNode = null;
        try {
            xaHelperNode = XMLConfig.getNode(node, "XAHelper");
        }
        catch (Exception exc) {
            xaHelperNode = null;
        }
        try {
            xaHelper = new XAHelper(xaHelperNode);
            logger.debug("Using XAHelper in AutoEnlist: " + xaHelper.isAutoEnlist());
            xaHelper.setLogger(logger);
        }
        catch (Exception exc) {
            throw new InitializationException("GVVCL_J2EE_INIT_ERROR", new String[][]{{"exc", exc.getMessage()}}, exc);
        }

        dumpMessage = XMLConfig.getBoolean(node, "@dump-message", false);
        decorateMessage = XMLConfig.getBoolean(node, "@decorate-message", true);
        logger.debug("Decorate Message.......: " + decorateMessage);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.j2ee.J2EEOperation#j2eeConnectionEstablished(javax.naming.Context)
     */
    @Override
    protected void j2eeConnectionEstablished(Context context) throws Exception {
        jmsKey = new JMSConnectionKey(context.getEnvironment(), connectionFactory, useVCLPooling, invalidateOnReinsert);
	    
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.j2ee.J2EEOperation#j2eeConnectionClosing()
     */
    @Override
    protected void j2eeConnectionClosing()
    {
        // do nothing
    }

    /**
     * This method just delegates to the startPerform() method of the superclass
     * in order to perform the operation using the J2EEOperation facilities
     * (connection management)
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    public GVBuffer perform(GVBuffer data) throws J2EEConnectionException, J2EEEnqueueException, InvalidDataException
    {
        try {
            return startPerform(data);
        }
        catch (J2EEEnqueueException exc) {
            throw exc;
        }
        catch (J2EEConnectionException exc) {
            throw exc;
        }
        catch (InvalidDataException exc) {
            throw exc;
        }
        catch (VCLException exc) {
            throw new J2EEEnqueueException("GVVCL_J2EE_INTERNAL_ERROR", exc);
        }
    }


    /**
     * Execute the actual send/publish of the GVBuffer.
     *
     * @see it.greenvulcano.gvesb.virtual.j2ee.J2EEOperation#j2eePerform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    protected GVBuffer j2eePerform(GVBuffer gvBuffer) throws J2EEConnectionException, J2EEEnqueueException,
            InvalidDataException, JMSException
    {
        prepareSession();
        Message message = null;
        
            try {
                if (refDP != null && !"".equals(refDP.trim())) {
                    // Enqueue operation uses DataProvider to create message to
                    // send/publish.
                    DataProviderManager dataProviderManager = DataProviderManager.instance();
                    IDataProvider dataProvider = dataProviderManager.getDataProvider(refDP);
                    try {
                        logger.debug("Working on data provider: " + dataProvider.getClass());
                        dataProvider.setContext(session);
                        dataProvider.setObject(gvBuffer);
                        message = (Message) dataProvider.getResult();
                    }
                    finally {
                        dataProviderManager.releaseDataProvider(refDP, dataProvider);
                    }
                }
                if (message == null) {
                    if (gvBuffer.getObject() instanceof Message) {
                        message = (Message) gvBuffer.getObject();
                    }
                    else {
                        throw new InvalidDataException(
                                "GVVCL_J2EE_ENQUEUE_ERROR",
                                new String[][]{{"message",
                                        "Don't know how to create message to send/publish. Data provider is not set and gvBuffer#object is not a JMS message."}});
                    }
                }
                if (decorateMessage) {
                	JMSMessageDecorator.decorateMessage(message, gvBuffer);
                }
            }
            catch (InvalidDataException exc) {
                throw exc;
            }
            catch (DataProviderException exc) {
                throw new InvalidDataException("GVVCL_J2EE_DATA_PROVIDER_ERROR", new String[][]{{"exc",
                        exc.getMessage()}}, exc);
            }
            catch (Exception exc) {
                throw new InvalidDataException("GVVCL_J2EE_UNHANDLED_ERROR", new String[][]{{"exc", exc.getMessage()}},
                        exc);
            }

            sendOrPublish(message, gvBuffer.getId());

            return gvBuffer;
        
        
    }

    /**
     * Called if the perform fails.
     *
     * @see it.greenvulcano.gvesb.virtual.j2ee.J2EEOperation#j2eePerformFailed(it.greenvulcano.gvesb.buffer.GVBuffer,
     *      java.lang.Exception)
     */
    @Override
    protected GVBuffer j2eePerformFailed(GVBuffer gvBuffer, Exception exc) throws J2EEEnqueueException
    {
        try {
            if (transacted) {
                if (session != null) {
                    session.rollback();
                }
            }
        }
        catch (Exception exc1) {
            // do nothing
        }
        throw new J2EEEnqueueException("GVVCL_J2EE_ENQUEUE_ERROR", new String[][]{{"exc", exc.toString()}}, exc);
    }


    private void sendOrPublish(Message message, Id id) throws J2EEConnectionException, J2EEEnqueueException,
            InvalidDataException, JMSException
    {
        message.setJMSCorrelationID(id.toString());

        MessageProducer messageProducer = null;

        boolean mustEnlist = false;
        try {
            try {
                mustEnlist = !xaHelper.isAutoEnlist() && xaHelper.isTransactionActive()
                        && (session instanceof XASession);
                if (mustEnlist) {
                    xaHelper.enlistResource(((XASession) session).getXAResource());
                }
            }
            catch (Exception exc) {
                throw new J2EEEnqueueException("GVVCL_J2EE_ENQUEUE_ERROR", new String[][]{{"exc", exc.toString()}}, exc);
            }
            if (isQueue) {
                Queue lQueue = session.createQueue(destinationName);
                if (session instanceof XAQueueSession) {
                    messageProducer = ((XAQueueSession) session).createProducer(lQueue);
                }
                else {
                    messageProducer = ((QueueSession) session).createSender(lQueue);
                }

                messageProducer.send(message, deliveryMode, priority, ttl);
                if (dumpMessage && logger.isDebugEnabled()) {
                    logger.debug("Enqueue " + name + " Produced message :\n" + new JMSMessageDump(message, null));
                }
            }
            else {
                Topic lTopic = session.createTopic(destinationName);
                if (session instanceof XATopicSession) {
                    messageProducer = ((XATopicSession) session).createProducer(lTopic);
                }
                else {
                    messageProducer = ((TopicSession) session).createPublisher(lTopic);
                }

                messageProducer.send(message, deliveryMode, priority, ttl);
            }

            if (transacted && !(session instanceof XAQueueSession)) {
                session.commit();
            }
        }
        finally {
            try {
                if (mustEnlist) {
                    xaHelper.delistResource();
                }
            }
            catch (Exception exc) {
                throw new J2EEEnqueueException("GVVCL_J2EE_ENQUEUE_ERROR", new String[][]{{"exc", exc.toString()}}, exc);
            }
            if (messageProducer != null) {
                messageProducer.close();
            }
        }
    }    
    
    private void prepareSession() throws J2EEEnqueueException
    {
        try {
            if (isQueue) {
                jmsData = JMSConnectionManager.instance().getQueueConnection(initialContext, jmsKey, key, xaHelper);

                if (xaHelper.isAutoEnlist() || !xaHelper.isTransactionActive()) {
                    session = jmsData.getQSession(transacted, acknowledge);
                }
                else if (xaHelper.isTransactionActive()) {
                    session = jmsData.getQSession(true, 0);
                }
            }
            else {
                jmsData = JMSConnectionManager.instance().getTopicConnection(initialContext, jmsKey, key, xaHelper);

                if (xaHelper.isAutoEnlist() || !xaHelper.isTransactionActive()) {
                    session = jmsData.getTSession(transacted, acknowledge);
                }
                else if (xaHelper.isTransactionActive()) {
                    session = jmsData.getTSession(true, 0);
                }
            }
        }
        catch (JMSException exc) {
            if (jmsData != null) {
                jmsData.invalidate();
            }
            throw new J2EEEnqueueException("GVVCL_J2EE_ENQUEUE_ERROR", new String[][]{{"exc", exc.toString()}}, exc);
        }
        catch (Exception exc) {
            throw new J2EEEnqueueException("GVVCL_J2EE_ENQUEUE_ERROR", new String[][]{{"exc", exc.toString()}}, exc);
        }
    }
        
    /**
     * Perform clean up operation.
     *
     * @see it.greenvulcano.gvesb.virtual.j2ee.J2EEOperation#j2eeCleanUp()
     */
    @Override
    protected void j2eeCleanUp()
    {        
        if (jmsData != null) {
            JMSConnectionManager.instance().releaseConnection(jmsData);
            jmsData = null;
        }
    }

    /**
     * Does nothing for this operation.
     *
     * @see it.greenvulcano.gvesb.virtual.j2ee.J2EEOperation#j2eeDestroy()
     */
    @Override
    protected void j2eeDestroy()
    {
        j2eeCleanUp();
    }

    /**
     * String to use with logs.
     *
     * @see it.greenvulcano.gvesb.virtual.j2ee.J2EEOperation#getDescription()
     */
    @Override
    public String getDescription()
    {
        return "enqueue to " + destinationName + " in " + initialContext.getProviderURL();
    }
   
}
