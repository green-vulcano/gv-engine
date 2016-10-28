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
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.gvdp.DataProviderException;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.j2ee.XAHelper;
import it.greenvulcano.gvesb.j2ee.jms.JMSMessageDecorator;
import it.greenvulcano.gvesb.j2ee.jms.JMSMessageDump;
import it.greenvulcano.gvesb.virtual.DequeueOperation;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.VCLException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
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
 * This class realizes an dequeue mechanism for a JMS queue or topic.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JMSDequeueOperation extends J2EEOperation implements DequeueOperation
{

	private static final Logger logger = LoggerFactory.getLogger(JMSDequeueOperation.class);

    /**
     * The message selector will be:
     *
     * <pre>
     * (DYNAMIC) AND(CONFIGURED)
     * </pre>
     */
    private static final int     MSC_DYNAMIC_AND_CONFIGURED   = 1;

    /**
     * The message selector will be:
     *
     * <pre>
     * (DYNAMIC) OR(CONFIGURED)
     * </pre>
     */
    private static final int     MSC_DYNAMIC_OR_CONFIGURED    = 2;

    /**
     * The message selector will be:
     *
     * <pre>
     * DYNAMIC
     * </pre>
     */
    private static final int     MSC_DYNAMIC_ONLY             = 3;

    /**
     * The message selector will be:
     *
     * <pre>
     * CONFIGURED
     * </pre>
     */
    private static final int     MSC_CONFIGURED_ONLY          = 4;

    /**
     * The message selector will be:
     *
     * <pre>
     * DYNAMIC != null ? DYNAMIC : CONFIGURED
     * </pre>
     */
    private static final int     MSC_CONFIGURED_IF_NO_DYNAMIC = 5;

    /**
     * If true the operation will use queues, otherwise will use topics.
     */
    private boolean              isQueue                      = true;

    private String               connectionFactoryName;
    private String               destinationName;
    private boolean              transacted;
    private int                  acknowledge;
    private boolean              useVCLPooling;
    private boolean              invalidateOnReinsert;
    private boolean              keepInputExtraProperties;

    /**
     * Receive timeout. If 0 then the receive blocks indefinitely. If <nobr>&gt;
     * 0</nobr> then this parameter specify, in milliseconds, the time that the
     * receive must wait for a message. If <nobr>&lt; 0</nobr> then a
     * receiveNoWait() will be used.
     */
    private long                 receiveTimeout               = 0;

    /**
     * Timeout configured. If the <code>setTimeout()</code> method is non
     * invoked or the configuration says that the timeout is not overridable,
     * then this value is is used for time-out.
     */
    private long                 configuredReceiveTimeout     = 0;

    private boolean              timeoutIsOverridable         = false;

    /**
     * Message selector used for the receiving operation, set by the
     * <code>setFilter()</code> method. This is combined with the configured
     * message selector according to the <code>messageSelectorCombining</code>.
     */
    private String               dynamicMessageSelector       = null;

    /**
     * Configured message selector. This is combined with the dynamic message
     * selector according to the <code>messageSelectorCombining</code>.
     */
    private String               configuredMessageSelector    = null;

    /**
     * Says how the dynamic message selector (set by the
     * <code>setFilter()</code> method) and the configured message selector must
     * be combined. Available values are those expressed by the
     * <code>MSC_xxx</code> constants.
     */
    private int                  messageSelectorCombining     = MSC_DYNAMIC_AND_CONFIGURED;

    private Session              session;

    /**
     * Messages to acknowledge.
     */
    private Map<String, Message> messagesToAcknowledge        = new HashMap<String, Message>();

    private JMSConnectionKey     jmsKey                       = null;
    private JMSData              jmsData                      = null;

    // Fields for queue
    // private Queue                queue                        = null;

    // Fields for topic
    // private Topic                topic                        = null;
    private String               durableSubscriber            = null;
    private boolean              noLocal                      = true;

    private XAHelper             xaHelper                     = null;

    /**
     * If true the incoming message is dumped on log.
     */
    private boolean              dumpMessage                  = false;
    /**
     * If true the GVBuffer is enriched with message properties.
     */
    private boolean             decorateGVBuffer              = true;
    
    /**
     * Completes operation specific initialization.
     */
    @Override
    protected void j2eeInit(Node node) throws InitializationException, XMLConfigException
    {
        connectionFactoryName = XMLConfig.get(node, "@connection-factory");
        checkAttribute("connection-factory", connectionFactoryName);
        logger.debug("Connection factory.....: " + connectionFactoryName);

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

        String receiveType = XMLConfig.get(node, "@receive-type", "non-blocking");
        logger.debug("Receive type...........: " + receiveType);
        if (receiveType.equals("non-blocking")) {
            configuredReceiveTimeout = TO_NON_BLOCKING;
        }
        else if (receiveType.equals("blocking")) {
            configuredReceiveTimeout = TO_INDEFINITELY;
        }
        else {
            configuredReceiveTimeout = XMLConfig.getLong(node, "@receive-timeout", 1000);
            logger.debug("Receive timeout....: " + configuredReceiveTimeout);
        }
        String overridable = XMLConfig.get(node, "@receive-timeout-overridable", "false");
        timeoutIsOverridable = overridable.equals("true");
        logger.debug("Receive timeout overr..: " + timeoutIsOverridable);

        configuredMessageSelector = XMLConfig.get(node, "message-selector");
        logger.debug("Message selector.......: " + configuredMessageSelector);
        String combining = XMLConfig.get(node, "message-selector/@combining", "dynamic-and-configured");
        if (combining.equals("dynamic-and-configured")) {
            messageSelectorCombining = MSC_DYNAMIC_AND_CONFIGURED;
        }
        else if (combining.equals("dynamic-or-configured")) {
            messageSelectorCombining = MSC_DYNAMIC_OR_CONFIGURED;
        }
        else if (combining.equals("dynamic-only")) {
            messageSelectorCombining = MSC_DYNAMIC_ONLY;
        }
        else if (combining.equals("configured-only")) {
            messageSelectorCombining = MSC_CONFIGURED_ONLY;
        }
        else if (combining.equals("configured-if-no-dynamic")) {
            messageSelectorCombining = MSC_CONFIGURED_IF_NO_DYNAMIC;
        }
        logger.debug("Message selector comb..: " + combining);
        
        if (!isQueue) {
            durableSubscriber = XMLConfig.get(node, "@durable-subscriber");
            checkAttribute("durable-subscriber", durableSubscriber);
            logger.debug("Durable subscriber.: " + durableSubscriber);
            String noLocalStr = XMLConfig.get(node, "@no-local", "true");
            checkAttribute("no-local", noLocalStr);
            noLocal = noLocalStr.equals("true");
            logger.debug("No-local...........: " + noLocal);
        }
        
        keepInputExtraProperties = XMLConfig.getBoolean(node, "@keep-input-extra-properties", false);
        logger.debug("Keep input properties..: " + keepInputExtraProperties);

        resetDequeueOperation();

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
        decorateGVBuffer = XMLConfig.getBoolean(node, "@decorate-gvbuffer", true);
        logger.debug("Decorate GVBuffer......: " + decorateGVBuffer);
    }

    /**
     * Called when the JNDI connection is established.
     */
    @Override
    protected void j2eeConnectionEstablished(Context context) throws Exception
    {
        jmsKey = new JMSConnectionKey(context.getEnvironment(), connectionFactoryName, useVCLPooling, invalidateOnReinsert);
        /*
        if (isQueue) {
            queue = (Queue) context.lookup(destinationName);
        }
        else {
            topic = (Topic) context.lookup(destinationName);
        }
        */
    }

    /**
     * Reset the object.
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
    public GVBuffer perform(GVBuffer data) throws J2EEConnectionException, J2EEDequeueException, InvalidDataException
    {
        try {
            return startPerform(data);
        }
        catch (J2EEDequeueException exc) {
            throw exc;
        }
        catch (J2EEConnectionException exc) {
            throw exc;
        }
        catch (InvalidDataException exc) {
            throw exc;
        }
        catch (VCLException exc) {
            throw new J2EEDequeueException("GVVCL_J2EE_INTERNAL_ERROR", exc);
        }
    }


    /**
     * Executes the actual receive of the message creating a new GVBuffer.
     */
    @Override
    protected GVBuffer j2eePerform(GVBuffer inputGVBuffer) throws J2EEConnectionException, J2EEDequeueException,
            InvalidDataException, JMSException
    {
        try {
            Message message = receiveMessage();

            if (message == null) {
                return null;
            }

            GVBuffer outBuffer = null;
            try {
                if (dumpMessage && logger.isDebugEnabled()) {
                    logger.debug("Dequeue " + name + " Received message :\n"  + new JMSMessageDump(message, null));
                }
                if (keepInputExtraProperties) {
                    outBuffer = new GVBuffer(inputGVBuffer, false);
                }
                else {
                    outBuffer = new GVBuffer();
                }
                if (decorateGVBuffer) {
                	JMSMessageDecorator.decorateGVBuffer(message, outBuffer);
                }

                outBuffer.setObject(message);
                if (refDP != null && refDP.length() > 0) {
                    DataProviderManager dataProviderManager = DataProviderManager.instance();
                    IDataProvider dataProvider = dataProviderManager.getDataProvider(refDP);
                    try {
                        logger.debug("Working on data provider: " + dataProvider.getClass());
                        dataProvider.setObject(outBuffer);
                        Object result = dataProvider.getResult();
                        if (result != null) {
                            outBuffer.setObject(result);
                        }
                    }
                    finally {
                        dataProviderManager.releaseDataProvider(refDP, dataProvider);
                    }
                }
            }
            catch (DataProviderException exc) {
                throw new InvalidDataException("GVVCL_J2EE_DATA_PROVIDER_ERROR", new String[][]{{"exc",
                        exc.getMessage()}}, exc);
            }
            catch (GVException exc) {
                throw new InvalidDataException("GVVCL_J2EE_UNHANDLED_ERROR", new String[][]{{"exc", exc.getMessage()}},
                        exc);
            }
            catch (Exception exc) {
                throw new InvalidDataException("GVVCL_J2EE_UNHANDLED_ERROR", new String[][]{{"exc", exc.getMessage()}},
                        exc);
            }

            messagesToAcknowledge.put(outBuffer.getId().toString(), message);

            return outBuffer;
        }
        catch (JMSException exc) {
            if (jmsData != null) {
                jmsData.invalidate();
            }
            throw exc;
        }
    }

    /**
     * Called if the perform fails.
     */
    @Override
    protected GVBuffer j2eePerformFailed(GVBuffer data, Exception exc) throws J2EEDequeueException
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
        throw new J2EEDequeueException("GVVCL_J2EE_DEQUEUE_ERROR", new String[][]{{"exc", exc.toString()}}, exc);
    }


    /**
     * Receive a message from the destination. The received message must be an
     * <code>ObjectMessage</code> containing an object of a given type (
     * <code>GVBuffer</code>).
     *
     * @see #receiveTimeout
     * @return The received <code>ObjectMessage</code>. <code>null</code> if no
     *         message are available or the timeout occurs.
     * @exception JMSException
     *            if some internal error on the JMS server occur.
     * @exception J2EEDequeueException
     *            if the received message is not an <code>ObjectMessage</code>
     *            or the message does not contains an object of the required
     *            type.
     */
    protected Message receiveMessage() throws JMSException, J2EEDequeueException
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
            throw new J2EEDequeueException("GVVCL_J2EE_DEQUEUE_ERROR", new String[][]{{"exc", exc.toString()}}, exc);
        }
        catch (Exception exc) {
            throw new J2EEDequeueException("GVVCL_J2EE_DEQUEUE_ERROR", new String[][]{{"exc", exc.toString()}}, exc);
        }

        MessageConsumer messageConsumer = createMessageConsumer();
        Message message = null;

        boolean mustEnlist = false;

        try {
            try {
                mustEnlist = !xaHelper.isAutoEnlist() && xaHelper.isTransactionActive();
                if (mustEnlist) {
                    xaHelper.enlistResource(((XASession) session).getXAResource());
                }
            }
            catch (Exception exc) {
                throw new J2EEDequeueException("GVVCL_J2EE_DEQUEUE_ERROR", new String[][]{{"exc", exc.toString()}}, exc);
            }
            if (receiveTimeout == 0) {
                message = messageConsumer.receive();
            }
            else if (receiveTimeout > 0) {
                message = messageConsumer.receive(receiveTimeout);
            }
            else {
                message = messageConsumer.receiveNoWait();
            }
        }
        catch (JMSException exc) {
            if (jmsData != null) {
                jmsData.invalidate();
            }
            throw exc;
        }
        finally {
            resetDequeueOperation();
            messageConsumer.close();
            try {
                if (!xaHelper.isAutoEnlist() && xaHelper.isTransactionActive()) {
                    xaHelper.delistResource();
                }
            }
            catch (Exception exc) {
                throw new J2EEDequeueException("GVVCL_J2EE_DEQUEUE_ERROR", new String[][]{{"exc", exc.toString()}}, exc);
            }
        }

        if (message == null) {
            logger.debug("Received null JMS message");
            return null;
        }

        return message;
    }

    /**
     * Creates the correct MessageConsumer according to the configuration.
     *
     * @return the correct MessageConsumer.
     * @throws JMSException
     * @throws J2EEDequeueException
     */
    protected MessageConsumer createMessageConsumer() throws JMSException, J2EEDequeueException
    {
        MessageConsumer messageConsumer = null;
        String messageSelector = combineSelectors();

        logger.debug("Using message selector: " + messageSelector);

        if (isQueue) {
            Queue lQueue = session.createQueue(destinationName);// (Queue) getDestination();
            if (session instanceof XAQueueSession) {
                messageConsumer = ((XAQueueSession) session).createConsumer(lQueue, messageSelector);
            }
            else {
                messageConsumer = ((QueueSession) session).createReceiver(lQueue, messageSelector);
            }
        }
        else {
            TopicSession tsession = null;
            Topic lTopic = session.createTopic(destinationName);// (Topic) getDestination();
            if (session instanceof XATopicSession) {
                tsession = ((XATopicSession) session).getTopicSession();
            }
            else {
                tsession = ((TopicSession) session);
            }
            if (durableSubscriber != null) {
                messageConsumer = tsession.createDurableSubscriber(lTopic, durableSubscriber, messageSelector, noLocal);
            }
            else {
                messageConsumer = tsession.createSubscriber(lTopic, messageSelector, noLocal);
            }
        }

        return messageConsumer;
    }

    /**
     * Combines dynamic and configured message selectors according to the
     * <code>messageSelectorCombining</code> setting.
     *
     * @return the combined message selector according to the
     *         <code>messageSelectorCombining</code> setting.
     */
    protected String combineSelectors()
    {
        String messageSelector = null;

        switch (messageSelectorCombining) {

            case MSC_DYNAMIC_AND_CONFIGURED :{
                if (dynamicMessageSelector != null) {
                    if (configuredMessageSelector != null) {
                        messageSelector = "(" + dynamicMessageSelector + ") AND (" + configuredMessageSelector + ")";
                    }
                    else {
                        messageSelector = dynamicMessageSelector;
                    }
                }
                else {
                    messageSelector = configuredMessageSelector;
                }
            }
                break;

            case MSC_DYNAMIC_OR_CONFIGURED :{
                if (dynamicMessageSelector != null) {
                    if (configuredMessageSelector != null) {
                        messageSelector = "(" + dynamicMessageSelector + ") OR (" + configuredMessageSelector + ")";
                    }
                    else {
                        messageSelector = dynamicMessageSelector;
                    }
                }
                else {
                    messageSelector = configuredMessageSelector;
                }
            }
                break;

            case MSC_DYNAMIC_ONLY :{
                messageSelector = dynamicMessageSelector;
            }
                break;

            case MSC_CONFIGURED_ONLY :{
                messageSelector = configuredMessageSelector;
            }
                break;

            case MSC_CONFIGURED_IF_NO_DYNAMIC :{
                if (dynamicMessageSelector != null) {
                    messageSelector = dynamicMessageSelector;
                }
                else {
                    messageSelector = configuredMessageSelector;
                }
            }
                break;
        }

        return messageSelector;
    }

    /**
     * Resets the timeout and the dynamic message selector.
     */
    protected void resetDequeueOperation()
    {
        receiveTimeout = configuredReceiveTimeout;
        dynamicMessageSelector = null;
    }

    /**
     * This method acknowledge the message identified by the given Id.
     *
     * @param id
     * @throws J2EEConnectionException
     * @throws J2EEAcknowledgeException
     */
    public void acknowledge(Id id) throws J2EEConnectionException, J2EEAcknowledgeException
    {
        Message message = messagesToAcknowledge.get(id.toString());
        if (message == null) {
            throw new J2EEAcknowledgeException("GVVCL_J2EE_ACKNOWLEDGE_ERROR", new String[][]{{"id", id.toString()}});
        }
        try {
            message.acknowledge();
            if (transacted && !(session instanceof XAQueueSession)) {
                session.commit();
            }
        }
        catch (JMSException exc) {
            try {
                if (transacted && !(session instanceof XAQueueSession)) {
                    if (session != null) {
                        session.rollback();
                    }
                }
            }
            catch (Exception exc1) {
                // do nothing
            }
            throw new J2EEConnectionException("GVVCL_J2EE_JMS_ACKNOWLEDGE_ERROR", new String[][]{{"id", id.toString()},
                    {"exc", exc.toString()}}, exc);
        }
        finally {
            messagesToAcknowledge.remove(id.toString());
        }
    }

    /**
     * This method acknowledge all the messages.
     *
     * @throws J2EEConnectionException
     * @throws J2EEAcknowledgeException
     */
    public void acknowledgeAll() throws J2EEConnectionException, J2EEAcknowledgeException
    {
        Set<String> keySet = messagesToAcknowledge.keySet();
        Iterator<String> iterator = keySet.iterator();

        while (iterator.hasNext()) {
            String id = iterator.next();
            Message message = messagesToAcknowledge.get(id);
            if (message != null) {
                try {
                    message.acknowledge();
                }
                catch (JMSException exc) {
                    // nothing
                }
                finally {
                    messagesToAcknowledge.remove(id);
                }
            }
        }
    }

    /**
     * This method roll-back the dequeue of the message identified by the given
     * Id.
     *
     * @param id
     * @throws J2EEConnectionException
     * @throws J2EEAcknowledgeException
     */
    public void rollback(Id id) throws J2EEConnectionException, J2EEAcknowledgeException
    {
        Message message = messagesToAcknowledge.get(id.toString());
        if (message != null) {
            try {
                if (transacted) {
                    session.rollback();
                }
            }
            catch (JMSException exc) {
                throw new J2EEConnectionException("GVVCL_J2EE_JMS_ROLLBACK_ERROR", new String[][]{
                        {"id", id.toString()}, {"exc", exc.toString()}});
            }
            finally {
                messagesToAcknowledge.remove(id.toString());
            }
        }
    }

    /**
     * This method roll-back the dequeue of all the messages.
     *
     * @throws J2EEConnectionException
     * @throws J2EEAcknowledgeException
     */
    public void rollbackAll() throws J2EEConnectionException, J2EEAcknowledgeException
    {
        messagesToAcknowledge.clear();
        try {
            if (transacted) {
                session.rollback();
            }
        }
        catch (JMSException exc) {
            throw new J2EEConnectionException("GVVCL_J2EE_JMS_ROLLBACK_ERROR", new String[][]{{"id", ""},
                    {"exc", exc.toString()}});
        }
    }

    /**
     * This method set the filter for messages to receive.
     *
     * @see it.greenvulcano.gvesb.virtual.DequeueOperation#setFilter(java.lang.String)
     */
    public void setFilter(String filter)
    {
        dynamicMessageSelector = filter;
    }

    /**
     * This method set the timeout for the receive operation.
     *
     * @see it.greenvulcano.gvesb.virtual.DequeueOperation#setTimeout(long)
     */
    public void setTimeout(long timeout)
    {
        if (timeoutIsOverridable) {
            receiveTimeout = timeout;
        }
    }

    /**
     * Perform clean up operation.
     */
    @Override
    protected void j2eeCleanUp()
    {
        /*if (session != null) {
            try {
                session.close();
            }
            catch (JMSException exc) {
                // do nothing
            }
            session = null;
        }*/
        if (jmsData != null) {
            JMSConnectionManager.instance().releaseConnection(jmsData);
            jmsData = null;
        }
    }

    /**
     * Does nothing for this operation.
     */
    @Override
    protected void j2eeDestroy()
    {
        j2eeCleanUp();
    }

    /**
     * String to use with logs.
     */
    @Override
    public String getDescription()
    {
        return "dequeue from " + destinationName + " to " + initialContext.getProviderURL();
    }

    
    /*
     * @return
     * @throws J2EEDequeueException
     
    protected Destination getDestination() throws J2EEDequeueException
    {
        try {
        if (isQueue) {
            return queue;
        }
        return topic;
        }
        catch (Exception exc) {
            throw new J2EEDequeueException("GVVCL_J2EE_DEQUEUE_ERROR", new String[][]{{"exc", exc.toString()}}, exc);
        }
    }
    */
}