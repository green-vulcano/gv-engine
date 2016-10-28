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

package it.greenvulcano.gvesb.gvhttp.impl;


import it.greenvulcano.expression.ExpressionEvaluator;
import it.greenvulcano.expression.ExpressionEvaluatorHelper;
import it.greenvulcano.gvesb.gvdp.AbstractDataProvider;
import it.greenvulcano.gvesb.gvdp.FieldExpressionKey;
import org.apache.commons.httpclient.HttpMethod;
import org.slf4j.Logger;

/**
 * @version 3.0.0 Jul 28, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class HttpMethodDataProvider extends AbstractDataProvider
{
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(HttpMethodDataProvider.class);

    private HttpMethod          httpMethod;

    /**
     * <p>
     * This method MUST be called, passing an {@link HttpMethod} to handle with
     * this DataProvider.
     * </p>
     *
     * @see it.greenvulcano.gvesb.gvdp.AbstractDataProvider#setContext(java.lang.Object)
     */
    @Override
    public void setContext(Object object) throws Exception
    {
        super.setContext(object);
        httpMethod = (HttpMethod) object;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getValue(it.greenvulcano.gvesb.gvdp.FieldExpressionKey)
     */
    @Override
    public Object getValue(FieldExpressionKey fieldExpressionKey) throws Exception
    {
        logger.debug("Get value called for expression {" + fieldExpressionKey.getExpression() + "} on object "
                + httpMethod);
        ExpressionEvaluator expressionEvaluator = ExpressionEvaluatorHelper.getExpressionEvaluator(fieldExpressionKey.getExpressionType());
        return expressionEvaluator.getValue(fieldExpressionKey.getExpression(), httpMethod);
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#setValue(it.greenvulcano.gvesb.gvdp.FieldExpressionKey,
     *      java.lang.Object)
     */
    @Override
    public void setValue(FieldExpressionKey fieldExpressionKey, Object value) throws Exception
    {
        logger.debug("Set value called for expression " + fieldExpressionKey.getExpression() + " on object "
                + httpMethod);
        ExpressionEvaluator expressionEvaluator = ExpressionEvaluatorHelper.getExpressionEvaluator(fieldExpressionKey.getExpressionType());
        expressionEvaluator.setValue(fieldExpressionKey.getExpression(), httpMethod, value);
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#reset()
     */
    @Override
    public void reset()
    {
        httpMethod = null;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getResult()
     */
    @Override
    public Object getResult() throws Exception
    {
        return httpMethod;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.AbstractDataProvider#getInternalObject()
     */
    @Override
    protected Object getInternalObject() throws Exception
    {
        return httpMethod;
    }

}
