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
package it.greenvulcano.gvesb.osgi.bus.connectors;

import java.nio.charset.Charset;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.GVConfigurationManager;
import it.greenvulcano.gvesb.osgi.repository.GVConfigurationRepository;
import it.greenvulcano.gvesb.osgi.repository.exception.GVResourceException;

public class GVBusDeployer implements GVBusConnector {
	private final static Logger LOG = LoggerFactory.getLogger(GVBusDeployer.class);
	
	private GVConfigurationManager configurationManager;
		
	private GVConfigurationRepository configurationRepository;
	
	private Session session;
	
	public void setConfigurationManager(GVConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}

	public void setConfigurationRepository(GVConfigurationRepository configurationRepository) {
		this.configurationRepository = configurationRepository;
	}
	
	public void connect(Session session, String busId) throws JMSException {
		this.session = session;
	
		Queue queue = this.session.createQueue(busId);						
		MessageConsumer qReceiver = (MessageConsumer) session.createConsumer(queue);
		qReceiver.setMessageListener(this);
		LOG.debug("Deployer connected on queue "+busId);
		
	}
	
	@Override
	public void onMessage(Message message) {
		MessageProducer producer = null;
		TextMessage response = null;
		String deployResult = null;
		try {
			LOG.debug("Received message on deploy queue: "+ message.getJMSCorrelationID());
			
			
			producer = session.createProducer(message.getJMSReplyTo());			
			response = session.createTextMessage();
			response.setJMSCorrelationID(message.getJMSCorrelationID());
						
			String configurationId = null;
						
			if (message instanceof TextMessage) {
				configurationId = TextMessage.class.cast(message).getText();
			} else if (message instanceof BytesMessage) {
				
				BytesMessage byteMessage = BytesMessage.class.cast(message);
				byte[] messageData = new byte[Long.valueOf(byteMessage.getBodyLength()).intValue()];
				byteMessage.readBytes(messageData);
				
				configurationId = new String(messageData, Charset.forName("UTF-8"));
				
			} else {			
				throw new JMSException("Unmanaged message type: "+ message.getClass());
			}
	
			deployResult = deploy(configurationId);
		
		} catch (JMSException jmsException) {
			
			JSONObject result = new JSONObject();
			result.put("deployed", false);
			result.put("message", jmsException.getMessage());
			deployResult = result.toString();
			
			LOG.error("Error receiving message", jmsException);
		} 		
		
		try {
			response.setText(deployResult.toString());
			producer.send(response);
			producer.close();
		} catch (Exception exception) {
			LOG.error("Error publishing feedback", exception);
		}
		
		
	}
			
	private String deploy(String configurationId) {		
		JSONObject deployResult = new JSONObject();
		try {			
			Document config = configurationRepository.retrieveConfiguration(configurationId);			
			configurationManager.updateConfiguration(config);
			deployResult.put("deployed", true);
		} catch (GVResourceException e) {
			deployResult.put("deployed", false);
			deployResult.put("message", "Fail to retrieve configuration" );
			LOG.error("Error receiving configuration ", e);
		} catch (XMLConfigException e) {
			deployResult.put("deployed", false);
			deployResult.put("message","Invalid configuration" );
			LOG.error("Error installing configuration", e);
		}
		
		return deployResult.toString();
		
	}

}
