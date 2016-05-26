/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.j2ee.jms;

import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Utility class to dump JMS messages into an XML Document.
 *
 * @version 3.5.0 Sep 9, 2014
 * @author GreenVulcano Developer Team
 *
 */
public class JMSMessageToXML
{
	public static final String JMS_DOC_NS = "http://www.greenvulcano.it/jms";

    public static Node toXML(Message message) throws Exception {
        Date date = null;
        Document doc = null;

        XMLUtils parser = null;
        try {
        	parser = XMLUtils.getParserInstance();
        	
            doc = parser.newDocument("JMSMessage", JMS_DOC_NS);
        	Element root = doc.getDocumentElement();
        	
        	parser.setAttribute(root, "class", message.getClass().getName());
        	String type = "Undefined";
        	if (message instanceof TextMessage) {
        		type = "TextMessage";
            }
            else if (message instanceof BytesMessage) {
        		type = "BytesMessage";
            }
            else if (message instanceof MapMessage) {
        		type = "MapMessage";
            }
            else if (message instanceof ObjectMessage) {
        		type = "ObjectMessage";
            }
        	parser.setAttribute(root, "type", type);

        	Node node = parser.insertElement(root, "JMSCorrelationID");
        	parser.insertText((Element) node, message.getJMSCorrelationID());
        	
            int delivery = message.getJMSDeliveryMode();
            String deliveryMode = "";
            if (delivery == 2) {
                deliveryMode = "Persistent";
            }
            else if (delivery == 1) {
                deliveryMode = "NotPersistent";
            }
        	node = parser.insertElement(root, "JMSDeliveryMode");
        	parser.setAttribute((Element) node, "desc", deliveryMode);
        	parser.insertText((Element) node, String.valueOf(delivery));

            String sdate = "Never";
            if (message.getJMSExpiration() != 0) {
                date = new Date(message.getJMSExpiration());
                sdate = DateUtils.dateToString(date, DateUtils.FORMAT_ISO_TIMESTAMP_L);
            }
            node = parser.insertElement(root, "JMSExpiration");
        	parser.insertText((Element) node, sdate);
            node = parser.insertElement(root, "JMSMessageID");
        	parser.insertText((Element) node, message.getJMSMessageID());
            node = parser.insertElement(root, "JMSPriority");
        	parser.insertText((Element) node, String.valueOf(message.getJMSPriority()));

        	date = new Date(message.getJMSTimestamp());
            node = parser.insertElement(root, "JMSTimestamp");
        	parser.insertText((Element) node, DateUtils.dateToString(date, DateUtils.FORMAT_ISO_TIMESTAMP_L));

        	node = parser.insertElement(root, "JMSType");
        	parser.insertText((Element) node, message.getJMSType());
        	node = parser.insertElement(root, "JMSDestination");
        	parser.insertText((Element) node, message.getJMSDestination().toString());
        	node = parser.insertElement(root, "JMSRedelivered");
        	parser.insertText((Element) node, String.valueOf(message.getJMSRedelivered()));
        	node = parser.insertElement(root, "JMSReplyTo");
        	Destination d = message.getJMSReplyTo();
        	parser.insertText((Element) node, (d == null ? "" : d.toString()));

        	@SuppressWarnings("unchecked")
			Enumeration<String> en = message.getPropertyNames();
        	if (en.hasMoreElements()) {
        		Element props = parser.insertElement(root, "Properties");
	            while (en.hasMoreElements()) {
	            	Element p = parser.insertElement(props, "Property");
	                String name = en.nextElement();
	                Object value = message.getObjectProperty(name);
	                parser.setAttribute(p, "name", name);
	                if (value != null) {
	                	parser.setAttribute(p, "class", value.getClass().getName());
		                parser.insertText(p, value.toString());
	                }
	                else {
	                	parser.setAttribute(p, "class", "Undefined");
	                }
	            }
        	}
        	
            dumpPayload(root, message, parser);
        }
        catch (Exception exc) {
            throw exc;
        }

        return doc;
    }

    private static void dumpPayload(Node root, Message message, XMLUtils parser) throws Exception {
        try {
        	Element payload = parser.insertElement((Element) root, "Payload");

            if (message instanceof TextMessage) {
            	parser.setAttribute(payload, "type", "string");
                TextMessage textMessage = (TextMessage) message;
                String body = textMessage.getText();
                if (body != null) {
                    parser.insertCDATA(payload, body);
                }
            }
            else if (message instanceof BytesMessage) {
            	parser.setAttribute(payload, "type", "base64");
                BytesMessage bytesMessage = (BytesMessage) message;
                try {
                    int size;
                    byte[] buffer = new byte[1024];
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                    while ((size = bytesMessage.readBytes(buffer)) != -1) {
                        baos.write(buffer, 0, size);
                    }
                    String value = new String(Base64.getEncoder().encode(baos.toByteArray()));
                    parser.insertText(payload, value);
                }
                finally {
                    bytesMessage.reset();
                }
            }
            else if (message instanceof MapMessage) {
            	parser.setAttribute(payload, "type", "properties");
            	MapMessage mapMessage = (MapMessage) message;
                try {
                	@SuppressWarnings("unchecked")
                    Enumeration<String> en = mapMessage.getMapNames();
    	            while (en.hasMoreElements()) {
    	            	Element p = parser.insertElement(payload, "Property");
    	                String name = en.nextElement();
    	                Object value = mapMessage.getObject(name);
    	                parser.setAttribute(p, "name", name);
    	                if (value != null) {
    	                	parser.setAttribute(p, "class", value.getClass().getName());
    		                parser.insertText(p, value.toString());
    	                }
    	                else {
    	                	parser.setAttribute(p, "class", "Undefined");
    	                }
    	            }
                }
                finally {
                	//
                }
            }
            else {
                //return "Payload dump not available";
            }
            //root.appendChild(payload);
        }
        catch (Exception exc) {
            throw exc;
        }
    }

}
