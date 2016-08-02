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
import java.util.Map;
import java.util.stream.Collectors;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.core.debug.GVDebugger;
import it.greenvulcano.gvesb.core.debug.GVDebugger.DebugCommand;
import it.greenvulcano.gvesb.core.debug.GVDebugger.DebugKey;
import it.greenvulcano.gvesb.core.debug.model.DebuggerObject;

public class GVBusDebugger implements GVBusConnector {
	private final static Logger LOG = LoggerFactory.getLogger(GVBusDebugger.class);
	
	private GVDebugger gvDebugger;
	private Session session;
	
	public void setGvDebugger(GVDebugger gvDebugger) {
		this.gvDebugger = gvDebugger;
	}
		
	@Override
	public void connect(Session session, String busId) throws JMSException {
		Queue debugQueue = session.createQueue(busId.concat("/debug"));
		MessageConsumer messageConsumer = session.createConsumer(debugQueue);
		messageConsumer.setMessageListener(this);
		
		this.session = session;
		
		LOG.debug("Debugger connected on queue "+busId+"/debug");
	}	
	
	@Override
	public void onMessage(Message message) {
		MessageProducer producer = null;
		TextMessage response = null;
		DebuggerObject commandResult = null;
		try {
			LOG.debug("Received message on debug queue: "+ message.getJMSCorrelationID());
			
			
			producer = session.createProducer(message.getJMSReplyTo());			
			response = session.createTextMessage();
			response.setJMSCorrelationID(message.getJMSCorrelationID());
						
			String debugMessage = null;
						
			if (message instanceof TextMessage) {
				debugMessage = TextMessage.class.cast(message).getText();
			} else if (message instanceof BytesMessage) {
				
				BytesMessage byteMessage = BytesMessage.class.cast(message);
				byte[] messageData = new byte[Long.valueOf(byteMessage.getBodyLength()).intValue()];
				byteMessage.readBytes(messageData);
				
				debugMessage = new String(messageData, Charset.forName("UTF-8"));
				
			} else {			
				throw new JMSException("Unmanaged message type: "+ message.getClass());
			}
			
			JSONObject jsonCommand = new JSONObject(debugMessage.toString());
			
			String command = jsonCommand.getString("command");
			Map<DebugKey, String> params =  jsonCommand.getJSONObject("params").keySet()
													 .stream()
										  			 .collect(Collectors.toMap(DebugKey::valueOf, k-> jsonCommand.getJSONObject("params").getString(k)));
	
			commandResult = gvDebugger.processCommand(DebugCommand.valueOf(command), params);
		
		} catch (Exception exception) {			
			commandResult = DebuggerObject.FAIL_DEBUGGER_OBJECT;			
			LOG.error("Error processing debug command", exception);
		} 	
		
		try {
			
			JSONObject jsonResponse = XML.toJSONObject(commandResult.toXML());
			response.setText(jsonResponse.toString());
			producer.send(response);
			producer.close();
		} catch (Exception exception) {
			LOG.error("Error publishing feedback", exception);
		}

	}
	
	
}
