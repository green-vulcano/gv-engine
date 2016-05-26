/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 */
package it.greenvulcano.expression.js;

import it.greenvulcano.expression.ExpressionEvaluator;
import it.greenvulcano.expression.ExpressionEvaluatorException;
import it.greenvulcano.script.ScriptExecutor;
import it.greenvulcano.script.ScriptExecutorFactory;

import java.util.Map;

/**
 * Uses JSCRIPT_EXPRESSION_LANGUAGE to evaluate expressions on POJO.
 * 
 * @version 3.2.0 17/dic/2012
 * @author GreenVulcano Developer Team
 */
public class JavaScriptExpressionEvaluator implements ExpressionEvaluator
{
	private static org.slf4j.Logger logger  = org.slf4j.LoggerFactory.getLogger(JavaScriptExpressionEvaluator.class);
    private ScriptExecutor      script   = null;

    /* (non-Javadoc)
     * @see it.greenvulcano.expression.ExpressionEvaluator#addToContext(java.lang.String, java.lang.Object)
     */
    @Override
    public void addToContext(String key, Object value)
    {
        try {
            initContext();
            script.putProperty(key, value);
        }
        catch (Exception exc) {
            logger.error("JavaScriptExpressionEvaluator - Error setting key[" + key + "] in context", exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.expression.ExpressionEvaluator#addAllToContext(java.util.Map)
     */
    @Override
    public void addAllToContext(Map<String, Object> context)
    {
        try {
            initContext();
            script.putAllProperties(context);
        }
        catch (Exception exc) {
            logger.error("JavaScriptExpressionEvaluator - Error initializing context", exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.expression.ExpressionEvaluator#getValue(java.lang.String, java.lang.Object)
     */
    @Override
    public Object getValue(String expression, Object object) throws ExpressionEvaluatorException
    {
        try {
        	initContext();
        	script.putProperty("root", object);
            return script.execute(expression, object);
        }
        catch (Exception exc) {
            logger.error("Error evaluating the expression [\n" + expression + "\n].", exc);
            throw new ExpressionEvaluatorException("Error evaluating the expression " + expression, exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.expression.ExpressionEvaluator#setValue(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void setValue(String expression, Object value, Object object) throws ExpressionEvaluatorException
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.expression.ExpressionEvaluator#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        if (script != null) {
            script.destroy();
            script = null;
        }
    }

    private void initContext() throws Exception
    {
        if (script == null) {
            script = ScriptExecutorFactory.createSE("js", null, null, null);
            script.putProperty("logger", logger);
        }
    }
}
