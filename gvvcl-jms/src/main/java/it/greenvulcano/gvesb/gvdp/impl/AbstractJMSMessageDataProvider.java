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

import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.expression.ExpressionEvaluatorHelper;
import it.greenvulcano.gvesb.gvdp.AbstractDataProvider;
import it.greenvulcano.gvesb.gvdp.FieldExpressionKey;

/**
 * @version 3.0.0 Apr 20, 2010
 * @author nunzio
 *
 */
public abstract class AbstractJMSMessageDataProvider extends AbstractDataProvider
{
    /**
     * The shared {@link Logger} between JMS Message Data Provider classes.
     */
    protected static final Logger logger = LoggerFactory.getLogger(JMSBytesMessageDataProvider.class);

    /**
     * The shared {@link Message} between JMS Message Data Provider classes.
     */
    protected Message             currentMessage;

    /**
     * @see it.greenvulcano.gvesb.gvdp.AbstractDataProvider#getInternalObject()
     */
    @Override
    protected Object getInternalObject() throws Exception
    {
        return currentMessage;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#reset()
     */
    @Override
    public void reset()
    {
        currentMessage = null;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getResult()
     */
    @Override
    public Object getResult() throws Exception
    {
        return currentMessage;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getValue(it.greenvulcano.gvesb.gvdp.FieldExpressionKey)
     */
    @Override
    public Object getValue(FieldExpressionKey fieldExpressionKey) throws Exception
    {
        logger.debug("Get value called for expression " + fieldExpressionKey.getExpression() + " on object "
                + currentMessage);
        return ExpressionEvaluatorHelper.getExpressionEvaluator(fieldExpressionKey.getExpressionType()).getValue(
                fieldExpressionKey.getExpression(), currentMessage);
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#setValue(it.greenvulcano.gvesb.gvdp.FieldExpressionKey,
     *      java.lang.Object)
     */
    @Override
    public void setValue(FieldExpressionKey fieldExpressionKey, Object value) throws Exception
    {
        logger.debug("Set value called for expression " + fieldExpressionKey.getExpression() + " on object "
                + currentMessage);
        ExpressionEvaluatorHelper.getExpressionEvaluator(fieldExpressionKey.getExpressionType()).setValue(
                fieldExpressionKey.getExpression(), currentMessage, value);
    }

}
