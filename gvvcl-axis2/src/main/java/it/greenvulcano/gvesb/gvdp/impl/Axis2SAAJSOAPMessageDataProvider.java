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
package it.greenvulcano.gvesb.gvdp.impl;

import it.greenvulcano.expression.ExpressionEvaluatorHelper;
import it.greenvulcano.gvesb.gvdp.AbstractDataProvider;
import it.greenvulcano.gvesb.gvdp.FieldExpressionKey;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axis2.client.Options;
import org.slf4j.Logger;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author nunzio
 *
 */
public class Axis2SAAJSOAPMessageDataProvider extends AbstractDataProvider
{

    private static final Logger logger         = org.slf4j.LoggerFactory.getLogger(Axis2SAAJSOAPMessageDataProvider.class);

    private String              soapVersionURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
    private SOAPMessage         soapMessage;

    /**
     * <p>
     * This implementation of data provider can create a different
     * {@link SOAPMessage} based SOAP Specifications.
     * </p>
     * <p>
     * You can pass a String containing SOAP version:
     *
     * @see javax.xml.soap.SOAPConstants#SOAP_1_1_PROTOCOL
     * @see javax.xml.soap.SOAPConstants#SOAP_1_2_PROTOCOL </p>
     *
     * @see it.greenvulcano.gvesb.gvdp.AbstractDataProvider#setContext(java.lang.Object)
     */
    @Override
    public void setContext(Object object) throws Exception
    {
        super.setContext(object);
        if (object instanceof String) {
            soapVersionURI = (String) object;
        }
        else if (object instanceof SOAPMessage) {
            soapMessage = (SOAPMessage) object;
        }
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.AbstractDataProvider#getInternalObject()
     */
    @Override
    protected Object getInternalObject() throws Exception
    {
        return getSOAPMessage();
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getResult()
     */
    @Override
    public Object getResult() throws Exception
    {
        return getSOAPMessage();
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getValue(it.greenvulcano.gvesb.gvdp.FieldExpressionKey)
     */
    @Override
    public Object getValue(FieldExpressionKey fieldExpressionKey) throws Exception
    {
        SOAPMessage context = getSOAPMessage();
        logger.debug("Get value called for expression {" + fieldExpressionKey.getExpression() + "} on object "
                + context);
        return ExpressionEvaluatorHelper.getExpressionEvaluator(fieldExpressionKey.getExpressionType()).getValue(
                fieldExpressionKey.getExpression(), context);
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#reset()
     */
    @Override
    public void reset()
    {
        soapMessage = null;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#setValue(it.greenvulcano.gvesb.gvdp.FieldExpressionKey,
     *      java.lang.Object)
     */
    @Override
    public void setValue(FieldExpressionKey fieldExpressionKey, Object value) throws Exception
    {
        SOAPMessage context = getSOAPMessage();
        logger.debug("Set value called for expression " + fieldExpressionKey.getExpression() + " on object " + context);
        ExpressionEvaluatorHelper.getExpressionEvaluator(fieldExpressionKey.getExpressionType()).setValue(
                fieldExpressionKey.getExpression(), context, value);
    }

    /**
     * @return the {@link SOAPMessage}
     * @throws Exception
     */
    private SOAPMessage getSOAPMessage() throws Exception
    {
        if (soapMessage == null) {
            MessageFactory messageFactory = getSOAPFactory();
            soapMessage = messageFactory.createMessage();
        }
        return soapMessage;
    }

    /**
     * Return the SOAP factory to use depending on what options have been set.
     * If the SOAP version can not be seen in the options, version 1.1 is the
     * default.
     *
     * @return the SOAP factory
     * @throws SOAPException
     * @see Options#setSoapVersionURI(String)
     */
    private MessageFactory getSOAPFactory() throws SOAPException
    {
        MessageFactory factory = null;
        if (SOAPConstants.SOAP_1_2_PROTOCOL.equals(soapVersionURI)) {
            factory = MessageFactory.newInstance(soapVersionURI);
        }
        else {
            factory = MessageFactory.newInstance();
        }
        logger.debug("Message Factory created " + factory + " for SOAP version: " + soapVersionURI);
        return factory;
    }
}
