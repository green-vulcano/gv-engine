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
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvdp.AbstractDataProvider;
import it.greenvulcano.gvesb.gvdp.FieldExpressionKey;

/**
 * @version 3.2.0 Gen 31, 2012
 * @author GreenVulcano Developer Team
 * 
 */
public class GVBufferChangeDataProvider extends AbstractDataProvider
{

	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GVBufferChangeDataProvider.class);

    private GVBuffer            gvBuffer = null;

    /**
     * <p>
     * This implementation of data provider can change a {@link GVBuffer}
     * instance set as context.
     * </p>
     * 
     * @see it.greenvulcano.gvesb.gvdp.AbstractDataProvider#setContext(java.lang.Object)
     */
    @Override
    public void setContext(Object object) throws Exception
    {
        super.setContext(object);
        gvBuffer = (GVBuffer) object;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.AbstractDataProvider#getInternalObject()
     */
    @Override
    protected Object getInternalObject() throws Exception
    {
        return getGVBuffer();
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getResult()
     */
    @Override
    public Object getResult() throws Exception
    {
        return getGVBuffer();
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getValue(it.greenvulcano.gvesb.gvdp.FieldExpressionKey)
     */
    @Override
    public Object getValue(FieldExpressionKey fieldExpressionKey) throws Exception
    {
        GVBuffer gvBuffer = getGVBuffer();
        logger.debug("Get value called for expression {" + fieldExpressionKey.getExpression() + "} on object "
                + gvBuffer);
        ExpressionEvaluator expressionEvaluator = ExpressionEvaluatorHelper.getExpressionEvaluator(fieldExpressionKey.getExpressionType());
        return expressionEvaluator.getValue(fieldExpressionKey.getExpression(), gvBuffer);
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#reset()
     */
    @Override
    public void reset()
    {
        gvBuffer = null;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#setValue(it.greenvulcano.gvesb.gvdp.FieldExpressionKey,
     *      java.lang.Object)
     */
    @Override
    public void setValue(FieldExpressionKey fieldExpressionKey, Object value) throws Exception
    {
        GVBuffer gvBuffer = getGVBuffer();
        logger.debug("Set value called for expression {" + fieldExpressionKey.getExpression() + "} on object "
                + gvBuffer);
        ExpressionEvaluator expressionEvaluator = ExpressionEvaluatorHelper.getExpressionEvaluator(fieldExpressionKey.getExpressionType());
        expressionEvaluator.setValue(fieldExpressionKey.getExpression(), gvBuffer, value);
    }

    /**
     * @return the gvbuffer
     * @throws Exception
     */
    private GVBuffer getGVBuffer() throws Exception
    {
        if (gvBuffer == null) {
            gvBuffer = new GVBuffer();
        }
        return gvBuffer;
    }

}
