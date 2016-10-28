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
package it.greenvulcano.gvesb.ws.axis2.message;

import it.greenvulcano.util.bin.Dump;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.attachments.ConfigurableDataHandler;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.saaj.SOAPMessageImpl;
import org.apache.axis2.saaj.util.IDGenerator;
import org.apache.axis2.saaj.util.SAAJUtil;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.2.0 Nov 11, 2012
 * @author GreenVulcano Developer Team
 */
@SuppressWarnings({"deprecation","unchecked", "rawtypes"})
public class MessageConverter {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(MessageConverter.class);

    /**
     * Returns a OMElement tree parsed from the given input object.
     *
     * @param envelope
     *        input envelope to populate
     * @param object
     *        input object to use as body
     * @param debug
     *        if true, the various conversion step outputs are dumped on log
     * @return a OMElement tree parsed from the given input object.
     * @throws Exception
     */
    public static void setBody(org.apache.axiom.soap.SOAPEnvelope envelope, Object input) throws Exception {
        envelope.getBody().addChild(toOM(input));
    }

    /**
     * Returns a OMElement tree parsed from the given input object.
     *
     * @param object
     *        input object to convert
     * @param debug
     *        if true, the various conversion step outputs are dumped on log
     * @return a OMElement tree parsed from the given input object.
     * @throws Exception
     */
    public static OMElement toOM(Object input) throws Exception {
        Node n = XMLUtils.parseObject_S(input, false, true);
        if (n instanceof Document) {
            n = ((Document) n).getDocumentElement();
        }
        OMElement elem = org.apache.axis2.util.XMLUtils.toOM((Element) n);
        return elem;
    }

    /**
     * Returns a OMElement tree parsed from the given input object.
     *
     * @param envelope
     *        input envelope to populate
     * @param object
     *        input object to use as body
     * @param debug
     *        if true, the various conversion step outputs are dumped on log
     * @return a OMElement tree parsed from the given input object.
     * @throws Exception
     */
    public static void setBody_gv(org.apache.axiom.soap.SOAPEnvelope envelope, Object input, boolean debug) throws Exception {
        envelope.getBody().addChild(toOM_gv(input, debug));
    }

    /**
     * Returns a OMElement tree parsed from the given input object.
     *
     * @param object
     *        input object to convert
     * @param debug
     *        if true, the various conversion step outputs are dumped on log
     * @return a OMElement tree parsed from the given input object.
     * @throws Exception
     */
    
	public static OMElement toOM_gv(Object input, boolean debug) throws Exception {
        boolean debugEnabled = logger.isDebugEnabled();

        if (debugEnabled && debug) {
            logger.debug("BEGIN conversion");
        }
        try {
            if (debugEnabled && debug) {
                if (input == null) {
                    logger.debug("Input: [NULL]");
                }
                else {
                    logger.debug("Input(" + input.getClass() + "):");
                    if (input instanceof byte[]) {
                        Dump dump = new Dump((byte[]) input, -1);
                        logger.debug("\n" + dump.toString());
                    }
                    else if (input instanceof Node) {
                        try {
                            logger.debug(XMLUtils.serializeDOM_S((Node) input));
                        }
                        catch (Exception exc) {
                            logger.debug("[DUMP ERROR!!!!!].");
                        }
                    }
                    else {
                        try {
                            logger.debug(input.toString());
                        }
                        catch (Exception exc) {
                            logger.debug("[DUMP ERROR!!!!!].");
                        }
                    }
                }
            }
            Node n = XMLUtils.parseObject_S(input, false, true);
            byte[] data = XMLUtils.serializeDOMToByteArray_S(n, "UTF-8", true, false);

            if (debugEnabled && debug) {
                Dump dump = new Dump((byte[]) data, -1);
                logger.debug("Middle step 1:\n" + dump.toString());
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader(bais, "UTF-8");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            OMElement elem = null;
            StAXOMBuilder builder = null;
            try {
                builder = new StAXOMBuilder(xmlreader);
                builder.releaseParserOnClose(true);

                elem = builder.getDocumentElement();
                elem.build();
                elem.serializeAndConsume(baos);
                if (debugEnabled && debug) {
                    logger.debug("Middle step 2:\n" + new String(baos.toByteArray()));
                }
                builder.close();
            }
            catch (Exception e) {
                if (builder != null) {
                    builder.close();
                }
            }

            data = TextUtils.replaceSubstring(new String(baos.toByteArray()), "&;", "").getBytes("UTF-8");

            bais = new ByteArrayInputStream(data);
            xmlreader = StAXUtils.createXMLStreamReader(bais, "UTF-8");

            try {
                builder = new StAXOMBuilder(xmlreader);
                builder.releaseParserOnClose(true);

                elem = builder.getDocumentElement();
                elem.build();
                builder.close();
            }
            catch (Exception e) {
                if (builder != null) {
                    builder.close();
                }
            }

            if (debugEnabled && debug) {
                logger.debug("Output:" + elem);
            }
            return elem;
        }
        finally {
            if (debugEnabled && debug) {
                logger.debug("END conversion");
            }
        }
    }

	/**
	 * Parse a wire representation of a SOAP message (also multipart-related) 
	 * and return a SAAJ SOAPMessage instance.
	 *
	 * @param payload
	 * @return a SAAJ SOAPMessage instance
	 * @throws Exception
	 */
	public static SOAPMessage createSOAPMessage(byte[] payload) throws Exception {
		SOAPMessage message = null;

		String input = new String(payload, 0, (payload.length < 1000) ? payload.length : 1000);
		int idxB = input.indexOf("--");
		if (idxB != -1) {
			int idxCR = input.indexOf("\r", idxB + 2);
			String boundary = input.substring(idxB + 2, idxCR);
			int idxF = input.indexOf("Content-Id: ");
			int idxFe = input.indexOf("\r", idxF + 12);
			String first = input.substring(idxF + 12, idxFe);

			MimeHeaders hdrs = new MimeHeaders();
			hdrs.addHeader("MIME-Version", "1.0");
			hdrs.addHeader("Content-Type", "multipart/related; type=\"text/xml\"; start=\"" + first + "\"; boundary=\"" + boundary + "\"");
			message = new SOAPMessageImpl(new ByteArrayInputStream(payload), hdrs, false);
		} 
		else {
			message = new SOAPMessageImpl(new ByteArrayInputStream(payload), new MimeHeaders(), false);
		}

		//System.out.println("1 envelope: " + XMLUtils.serializeDOM_S(message.getSOAPPart().getEnvelope()));
		//System.out.println("1 countAttachments: " + message.countAttachments());

		return message;
	}

	/**
	 * Convert a MessageContext to a SAAJ SOAPMessage.
	 * 
	 * @param msgCtx
	 * @return a SOAPMessage instance
	 * @throws Exception
	 */
	public static SOAPMessage createSOAPMessage(MessageContext msgCtx) throws Exception {
		SOAPMessage response = getSOAPMessage(msgCtx.getEnvelope());

	    Attachments attachments = msgCtx.getAttachmentMap();
	    for (String contentId : attachments.getAllContentIDs()) {
	        if (!contentId.equals(attachments.getSOAPPartContentID())) {
	            AttachmentPart ap = response.createAttachmentPart(attachments.getDataHandler(contentId));
	            ap.setContentId(contentId);
	            response.addAttachmentPart(ap);
	        }
	    }
    
	    return response;
	}

	/**
	 * Parse a wire representation of a SOAP message (also multipart-related) 
	 * and return a MessageContext instance.
	 * 
	 * @param payload
	 * @return a MessageContext instance
	 * @throws Exception
	 */
	public static MessageContext createMessageContext(byte[] payload) throws Exception {
		MessageContext msgCtx = new MessageContext();
		updateMessageContext(payload, msgCtx);
		return msgCtx;
	}

	/**
	 * Parse a wire representation of a SOAP message (also multipart-related) 
	 * and update a MessageContext instance.
	 * 
	 * @param payload
	 * @throws Exception
	 */
	public static void updateMessageContext(byte[] payload, MessageContext msgCtx) throws Exception {
		SOAPMessage message = createSOAPMessage(payload);

		Options options = msgCtx.getOptions();

		org.apache.axiom.soap.SOAPEnvelope envelope;
		if (isMTOM(message)) {
			envelope = SAAJUtil.toOMSOAPEnvelope(message);
			options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
		} 
		else {
			envelope = SAAJUtil.toOMSOAPEnvelope(message.getSOAPPart().getDocumentElement());
			if (message.countAttachments() != 0) { // SOAPMessage with attachments
				Attachments attachments = msgCtx.getAttachmentMap();
				
				Iterator<AttachmentPart> it = message.getAttachments();
				while (it.hasNext()) {
					AttachmentPart attachment = it.next();
					String contentId = attachment.getContentId();
					// Axiom currently doesn't support attachments without Content-ID
					// (see WSCOMMONS-418); generate one if necessary.
					if (contentId == null) {
						contentId = IDGenerator.generateID();
					}
					else {
						if (contentId.startsWith("<")) {
							contentId = contentId.substring(1);
						}
						if (contentId.endsWith(">")) {
							contentId = contentId.substring(0, contentId.length()-1);
						}
					}
					DataHandler handler = attachment.getDataHandler();
					// make sure that AttachmentPart content-type overrides DataHandler content-type
					if (!SAAJUtil.compareContentTypes(attachment.getContentType(), handler.getContentType())) {
						ConfigurableDataHandler configuredHandler = new ConfigurableDataHandler(handler.getDataSource());
						configuredHandler.setContentType(attachment.getContentType());
						handler = configuredHandler;
					}
					attachments.addDataHandler(contentId, handler);
				}
				options.setProperty(Constants.Configuration.ENABLE_SWA,	Constants.VALUE_TRUE);
			}
		}
		msgCtx.setEnvelope(envelope);
		//System.out.println("2 envelope: " + msgCtx.getEnvelope());
		//System.out.println("2 countAttachments: " + msgCtx.getAttachmentMap().getAllContentIDs().length);
	}

    
	public static byte[] serializeSOAPMessage(SOAPMessage message) throws Exception {
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		message.writeTo(byteBuffer);
		
		return byteBuffer.toByteArray();
	}

	public static byte[] serializeMessageContext(MessageContext msgCtx) throws Exception {
		SOAPMessage message = createSOAPMessage(msgCtx);
		return serializeSOAPMessage(message);
	}

	private static boolean isMTOM(SOAPMessage soapMessage) {
		SOAPPart soapPart = soapMessage.getSOAPPart();
		String[] contentTypes = soapPart.getMimeHeader("Content-Type");
		if ((contentTypes != null) && (contentTypes.length > 0)) {
			return SAAJUtil.normalizeContentType(contentTypes[0]).equals("application/xop+xml");
		} 
		else {
			return false;
		}
	}
	
	/**
     * This method handles the conversion of an OM SOAP Envelope to a SAAJ SOAPMessage
     *
     * @param respOMSoapEnv
     * @return the SAAJ SOAPMessage
     * @throws Exception If an exception occurs during this conversion
     */
    private static SOAPMessage getSOAPMessage(org.apache.axiom.soap.SOAPEnvelope respOMSoapEnv) throws Exception {
        // Create the basic SOAP Message
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage response = mf.createMessage();
        SOAPPart sPart = response.getSOAPPart();
        javax.xml.soap.SOAPEnvelope env = sPart.getEnvelope();
        SOAPBody body = env.getBody();
        SOAPHeader header = env.getHeader();

        // Convert all header blocks
        org.apache.axiom.soap.SOAPHeader header2 = respOMSoapEnv.getHeader();
        if (header2 != null) {
        	Iterator hbIter = header2.examineAllHeaderBlocks();
            while (hbIter.hasNext()) {
                // Converting a single OM SOAP HeaderBlock to a SAAJ SOAP
                // HeaderBlock
                org.apache.axiom.soap.SOAPHeaderBlock hb = (org.apache.axiom.soap.SOAPHeaderBlock) hbIter.next();
                QName hbQName = hb.getQName();
                SOAPHeaderElement headerEle = header.addHeaderElement(env.createName(hbQName.getLocalPart(), hbQName.getPrefix(), hbQName.getNamespaceURI()));
                Iterator attribIter = hb.getAllAttributes();
                while (attribIter.hasNext()) {
                    OMAttribute attr = (OMAttribute) attribIter.next();
                    QName attrQName = attr.getQName();
                    headerEle.addAttribute(env.createName(attrQName.getLocalPart(), attrQName.getPrefix(), attrQName.getNamespaceURI()), attr.getAttributeValue());
                }
                String role = hb.getRole();
                if (role != null) {
                    headerEle.setActor(role);
                }
                headerEle.setMustUnderstand(hb.getMustUnderstand());

                toSAAJElement(headerEle, hb, response);
            }
        }
        // Convert the body
        toSAAJElement(body, respOMSoapEnv.getBody(), response);

        return response;
    }

    /**
     * Converts an OMNode into a SAAJ SOAPElement
     *
     * @param saajEle
     * @param omNode
     * @param saajSOAPMsg
     * @throws SOAPException If conversion fails
     */
    private static void toSAAJElement(SOAPElement saajEle,
                               OMNode omNode,
                               javax.xml.soap.SOAPMessage saajSOAPMsg) throws SOAPException {

        if (omNode instanceof OMText) {
            return; // simply return since the text has already been added to saajEle
        }

        if (omNode instanceof OMElement) {
            OMElement omEle = (OMElement) omNode;
            Iterator childIter = omEle.getChildren();
            while (childIter.hasNext()) {
                OMNode omChildNode = (OMNode) childIter.next();
                SOAPElement saajChildEle = null;
                if (omChildNode instanceof OMText) {
                    // check whether the omtext refers to an attachment
                    final OMText omText = (OMText) omChildNode;
                    if (omText.isOptimized()) { // is this an attachment?
                        DataHandler datahandler = (DataHandler) omText.getDataHandler();
                        AttachmentPart attachment = saajSOAPMsg.createAttachmentPart(datahandler);
                        String id = IDGenerator.generateID();
                        attachment.setContentId("<" + id + ">");
                        attachment.setContentType(datahandler.getContentType());
                        saajSOAPMsg.addAttachmentPart(attachment);

                        SOAPElement xopInclude = saajEle.addChildElement(MTOMConstants.XOP_INCLUDE,
                                "xop", MTOMConstants.XOP_NAMESPACE_URI);
                        xopInclude.addAttribute(saajSOAPMsg.getSOAPPart().getEnvelope().createName("href"), "cid:" + id);
                    } 
                    else {
                        saajChildEle = saajEle.addTextNode(omText.getText());
                    }
                } 
                else if (omChildNode instanceof OMElement) {
                    OMElement omChildEle = (OMElement) omChildNode;
                    QName omChildQName = omChildEle.getQName();
                    saajChildEle = saajEle.addChildElement(omChildQName.getLocalPart(), omChildQName.getPrefix(), omChildQName.getNamespaceURI());
                    Iterator attribIter = omChildEle.getAllAttributes();
                    while (attribIter.hasNext()) {
                        OMAttribute attr = (OMAttribute) attribIter.next();
                        QName attrQName = attr.getQName();
                        saajChildEle.addAttribute(saajSOAPMsg.getSOAPPart().getEnvelope().createName(attrQName.getLocalPart(), 
                        		   attrQName.getPrefix(), attrQName.getNamespaceURI()), attr.getAttributeValue());
                    }
                }
                // go down the tree adding child elements, till u reach a leaf(i.e. text element)
                toSAAJElement(saajChildEle, omChildNode, saajSOAPMsg);
            }
        }
    }
}
