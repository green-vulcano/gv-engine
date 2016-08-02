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
package it.greenvulcano.expression;

import it.greenvulcano.expression.js.JavaScriptExpressionEvaluator;
import it.greenvulcano.expression.ognl.OGNLExpressionEvaluator;
import it.greenvulcano.expression.regex.RegularExpressionEvaluator;
import it.greenvulcano.expression.xpath.XPathExpressionEvaluator;
import it.greenvulcano.util.thread.ThreadMap;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * GreenVulcanoESB Expression Evaluator Helper.
 * </p>
 * 
 * <p>
 * <b>Usage: </b> To use multiple times the <code>ExpressionEvaluator</code>
 * using the context per-thread, follow this usage pattern:
 * 
 * <pre>
 * 
 *   ...
 *   String expressionType = ExpressionEvaluatorHelper.OGNL_EXPRESSION_LANGUAGE;
 *   ExpressionEvaluatorHelper.startEvaluation();
 *   try {
 *       ExpressionEvaluator evaluator = ExpressionEvaluatorHelper.getExpressionEvaluator(expressionType);
 *       ...
 *   }
 *   finally {
 *       ExpressionEvaluatorHelper.endEvaluation();
 *   }
 *   ...
 * 
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * If you don't need a per-thread context, use directly #getValue and #setValue
 * helper methods.
 * </p>
 * 
 * @version 3.0.0 Feb 26, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class ExpressionEvaluatorHelper
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(ExpressionEvaluatorHelper.class);

    private static final String EXPRESSION_EVALUATOR_KEY         = "__EXPRESSION_EVALUATOR_KEY__";
    private static final String EXPRESSION_EVALUATOR_CONTEXT_KEY = "__EXPRESSION_EVALUATOR_CONTEXT_KEY__";
    /**
     * OGNL Expression Evaluator constant
     */
    public static final String  OGNL_EXPRESSION_LANGUAGE         = "ognl";
    /**
     * XPath Expression Evaluator constant
     */
    public static final String  XPATH_EXPRESSION_LANGUAGE        = "xpath";

    /**
     * Regular Expression Evaluator constant
     */
    public static final String  REGEX_EXPRESSION_LANGUAGE        = "regex";

    /**
     * JavaScript Expression Evaluator constant
     */
    public static final String  JSCRIPT_EXPRESSION_LANGUAGE      = "js";

    private ExpressionEvaluatorHelper()
    {
        // do nothing
    }

    /**
     *
     */
    public static void startEvaluation()
    {
        ThreadMap.put(EXPRESSION_EVALUATOR_KEY, new HashMap<String, ExpressionEvaluator>());
        ThreadMap.put(EXPRESSION_EVALUATOR_CONTEXT_KEY, new HashMap<String, Object>());
    }

    /**
     * @param expressionType
     * @return the expression evaluator configured
     * @throws ExpressionEvaluatorException
     */
    @SuppressWarnings("unchecked")
    public static ExpressionEvaluator getExpressionEvaluator(String expressionType) throws ExpressionEvaluatorException
    {
        Map<String, ExpressionEvaluator> exprEvalMap = (Map<String, ExpressionEvaluator>) ThreadMap.get(EXPRESSION_EVALUATOR_KEY);
        if (exprEvalMap == null) {
            exprEvalMap = new HashMap<String, ExpressionEvaluator>();
            ThreadMap.put(EXPRESSION_EVALUATOR_KEY, exprEvalMap);
        }
        ExpressionEvaluator exprEval = exprEvalMap.get(expressionType);
        if (exprEval == null) {
            exprEval = retrieveExpressionEvaluator(expressionType);
            exprEvalMap.put(expressionType, exprEval);
            Map<String, Object> context = (Map<String, Object>) ThreadMap.get(EXPRESSION_EVALUATOR_CONTEXT_KEY);
            if (context != null) {
                exprEval.addAllToContext(context);
            }
        }
        return exprEval;
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
	public static void endEvaluation()
    {
        Map<String, ExpressionEvaluator> exprEvalMap = (Map<String, ExpressionEvaluator>) ThreadMap.get(EXPRESSION_EVALUATOR_KEY);
        if (exprEvalMap != null) {
            for (ExpressionEvaluator exprEval : exprEvalMap.values()) {
                exprEval.cleanUp();
            }
        }
        ThreadMap.remove(EXPRESSION_EVALUATOR_KEY);
    }

    /**
     * @param key
     * @param value
     * 
     * @see it.greenvulcano.expression.ExpressionEvaluator#addToContext(String,
     *      Object)
     */
    @SuppressWarnings("unchecked")
    public static void addToContext(String key, Object value)
    {
        Map<String, Object> context = (Map<String, Object>) ThreadMap.get(EXPRESSION_EVALUATOR_CONTEXT_KEY);
        if (context == null) {
            context = new HashMap<String, Object>();
            ThreadMap.put(EXPRESSION_EVALUATOR_CONTEXT_KEY, context);
        }
        context.put(key, value);
        Map<String, ExpressionEvaluator> exprEvalMap = (Map<String, ExpressionEvaluator>) ThreadMap.get(EXPRESSION_EVALUATOR_KEY);
        for (ExpressionEvaluator exprEval : exprEvalMap.values()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding to context of " + exprEval + ": " + key + " - " + value);
            }
            exprEval.addToContext(key, value);
        }
    }

    /**
     * Helper method that evaluates the 'get' expression to the object passed
     * 
     * @param expressionType
     * @param expression
     *        the expression to evaluate
     * @param object
     *        the object to get the value to
     * @return the object resulting from expression evaluation
     * @throws ExpressionEvaluatorException
     * 
     * @see ExpressionEvaluator#getValue(String, Object)
     */
    public static Object getValue(String expressionType, String expression, Object object)
            throws ExpressionEvaluatorException
    {
        ExpressionEvaluator expressionEvaluator = retrieveExpressionEvaluator(expressionType);
        return expressionEvaluator.getValue(expression, object);
    }

    /**
     * Helper method that evaluates the 'set' expression to the object passed
     * 
     * @param expressionType
     * @param expression
     *        the expression to evaluate
     * @param value
     *        the value to set to the object
     * @param object
     *        the object to set the value to
     * @throws ExpressionEvaluatorException
     * 
     * @see ExpressionEvaluator#setValue(String, Object, Object)
     */
    public void setValue(String expressionType, String expression, Object value, Object object)
            throws ExpressionEvaluatorException
    {
        ExpressionEvaluator expressionEvaluator = retrieveExpressionEvaluator(expressionType);
        expressionEvaluator.setValue(expression, value, object);
    }

    private static ExpressionEvaluator retrieveExpressionEvaluator(String expressionType)
            throws ExpressionEvaluatorException
    {
        if (OGNL_EXPRESSION_LANGUAGE.equalsIgnoreCase(expressionType)) {
            return new OGNLExpressionEvaluator();
        }
        else if (XPATH_EXPRESSION_LANGUAGE.equalsIgnoreCase(expressionType)) {
            return new XPathExpressionEvaluator();
        }
        else if (REGEX_EXPRESSION_LANGUAGE.equalsIgnoreCase(expressionType)) {
            return new RegularExpressionEvaluator();
        }
        else if (JSCRIPT_EXPRESSION_LANGUAGE.equalsIgnoreCase(expressionType)) {
            return new JavaScriptExpressionEvaluator();
        }
        throw new ExpressionEvaluatorException("INVALID_EXPRESSION_TYPE: " + expressionType);
    }

    /**
     * @param expressionType
     * @return if the expression type is a well-known expression
     */
    public static boolean isValidExpressionType(String expressionType)
    {
        return expressionType.startsWith(OGNL_EXPRESSION_LANGUAGE)
                || expressionType.startsWith(XPATH_EXPRESSION_LANGUAGE)
                || expressionType.startsWith(REGEX_EXPRESSION_LANGUAGE)
                || expressionType.startsWith(JSCRIPT_EXPRESSION_LANGUAGE);
    }

}
