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

import it.greenvulcano.expression.ExpressionEvaluator;
import it.greenvulcano.expression.ExpressionEvaluatorHelper;
import it.greenvulcano.gvesb.gvdp.AbstractDataProvider;
import it.greenvulcano.gvesb.gvdp.FieldExpressionKey;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.client.Options;

import org.slf4j.Logger;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author nunzio
 * 
 */
public class Axis2MessageContextDataProvider extends AbstractDataProvider
{

    private static final Logger logger  = org.slf4j.LoggerFactory.getLogger(Axis2MessageContextDataProvider.class);

    private Options             options = null;
    private DomMessageContext      messageContext = null;

    /**
     * <p>
     * This implementation of data provider can create a different
     * {@link MessageContext} based on options set as context.
     * </p>
     * <p>
     * You can pass an {@link Options} object from which to take the
     * <code>getSoapVersionURI</code> parameter.
     * </p>
     * 
     * @see it.greenvulcano.gvesb.gvdp.AbstractDataProvider#setContext(java.lang.Object)
     */
    @Override
    public void setContext(Object object) throws Exception
    {
        super.setContext(object);
        options = null;
        messageContext = null;
        if (object instanceof Options) {
            options = (Options) object;
        }
        else if (object instanceof DomMessageContext) {
            messageContext = (DomMessageContext) object;
        }
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.AbstractDataProvider#getInternalObject()
     */
    @Override
    protected Object getInternalObject() throws Exception
    {
        return getMessageContext();
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getResult()
     */
    @Override
    public Object getResult() throws Exception
    {
        return getMessageContext();
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getValue(it.greenvulcano.gvesb.gvdp.FieldExpressionKey)
     */
    @Override
    public Object getValue(FieldExpressionKey fieldExpressionKey) throws Exception
    {
    	DomMessageContext context = getMessageContext();
        logger.debug("Get value called for expression {" + fieldExpressionKey.getExpression() + "} on object "
                + context);
        ExpressionEvaluator expressionEvaluator = ExpressionEvaluatorHelper.getExpressionEvaluator(fieldExpressionKey.getExpressionType());
        expressionEvaluator.addToContext("options", options);
        return expressionEvaluator.getValue(fieldExpressionKey.getExpression(), context);
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#reset()
     */
    @Override
    public void reset()
    {
        messageContext = null;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#setValue(it.greenvulcano.gvesb.gvdp.FieldExpressionKey,
     *      java.lang.Object)
     */
    @Override
    public void setValue(FieldExpressionKey fieldExpressionKey, Object value) throws Exception
    {
    	DomMessageContext context = getMessageContext();
        logger.debug("Set value called for expression " + fieldExpressionKey.getExpression() + " on object " + context);
        ExpressionEvaluator expressionEvaluator = ExpressionEvaluatorHelper.getExpressionEvaluator(fieldExpressionKey.getExpressionType());
        expressionEvaluator.addToContext("options", options);
        expressionEvaluator.setValue(fieldExpressionKey.getExpression(), context, value);
    }

    /**
     * @return the message context
     * @throws Exception
     */
    private DomMessageContext getMessageContext() throws Exception
    {
        if (messageContext == null) {
            messageContext = new DomMessageContext();
        }
        SOAPEnvelope env = messageContext.getEnvelope();
        if (env == null) {
            SOAPFactory sf = getSOAPFactory();
            env = sf.getDefaultEnvelope();
            messageContext.setEnvelope(env);
        }
        return messageContext;
    }

    /**
     * Return the SOAP factory to use depending on what options have been set.
     * If the SOAP version can not be seen in the options, version 1.1 is the
     * default.
     * 
     * @return the SOAP factory
     * @see Options#setSoapVersionURI(String)
     */
    private SOAPFactory getSOAPFactory()
    {
        SOAPFactory factory = null;
        Options lOptions = options;
        if (lOptions == null) {
            lOptions = messageContext.getOptions();
        }
        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(lOptions.getSoapVersionURI())) {
            factory = OMAbstractFactory.getSOAP12Factory();
        }
        else {
            // make the SOAP 1.1 the default SOAP version
            factory = OMAbstractFactory.getSOAP11Factory();
        }
        return factory;
    }
}
