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
package it.greenvulcano.expression.ognl;

import it.greenvulcano.expression.ExpressionEvaluator;
import it.greenvulcano.expression.ExpressionEvaluatorException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

/**
 * Uses OGNL_EXPRESSION_LANGUAGE to evaluate expressions on POJO.
 * 
 * @version 3.0.0 Feb 26, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class OGNLExpressionEvaluator implements ExpressionEvaluator
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(OGNLExpressionEvaluator.class);
    private static Map<String, Object> expressions = new ConcurrentHashMap<String, Object>();
    private OgnlContext                context     = new OgnlContext();

    /**
     * @see it.greenvulcano.expression.ExpressionEvaluator#getValue(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public Object getValue(String expression, Object object) throws ExpressionEvaluatorException
    {
        Object tree = expressions.get(expression);
        if (tree == null) {
            try {
                tree = Ognl.parseExpression(expression);
                expressions.put(expression, tree);
            }
            catch (OgnlException e) {
                logger.error("Error parsing the expression " + expression, e);
                throw new ExpressionEvaluatorException("Error parsing the expression " + expression, e);
            }
        }
        try {
            /*if (logger.isDebugEnabled()) {
                logger.debug("Current context: ");
                logger.debug(context.getValues());
            }*/
            return Ognl.getValue(tree, context, object);
        }
        catch (OgnlException e) {
            logger.error("Error evaluating the expression [\n" + expression + "\n].", e);
            throw new ExpressionEvaluatorException("Error evaluating the expression " + expression, e);
        }
    }

    /**
     * @see it.greenvulcano.expression.ExpressionEvaluator#setValue(java.lang.String,
     *      java.lang.Object, java.lang.Object)
     */
    @Override
    public void setValue(String expression, Object value, Object object) throws ExpressionEvaluatorException
    {
        /*Object tree = expressions.get(expression);
        if (tree == null) {
            try {
                tree = Ognl.parseExpression(expression);
                expressions.put(expression, tree);
            }
            catch (OgnlException e) {
                logger.error("Error parsing the expression " + expression, e);
                throw new ExpressionEvaluatorException("Error parsing the expression " + expression, e);
            }
        }
        try {
            Ognl.setValue(tree, context, object, value);
        }
        catch (OgnlException e) {
            logger.error("Error evaluating the expression " + expression, e);
            throw new ExpressionEvaluatorException("Error evaluating the expression " + expression, e);
        }*/
    }

    /**
     * @see it.greenvulcano.expression.ExpressionEvaluator#addAllToContext(java.util.Map)
     */
    @Override
    public void addAllToContext(Map<String, Object> context)
    {
        this.context.putAll(context);
    }

    /**
     * @see it.greenvulcano.expression.ExpressionEvaluator#addToContext(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public void addToContext(String key, Object value)
    {
        context.put(key, value);
    }

    /**
     * @see it.greenvulcano.expression.ExpressionEvaluator#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // do nothing
    }
}
