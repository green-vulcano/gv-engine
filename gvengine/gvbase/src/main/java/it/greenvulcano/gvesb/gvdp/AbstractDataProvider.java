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
package it.greenvulcano.gvesb.gvdp;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.expression.ExpressionEvaluator;
import it.greenvulcano.expression.ExpressionEvaluatorException;
import it.greenvulcano.expression.ExpressionEvaluatorHelper;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Mar 5, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public abstract class AbstractDataProvider implements IDataProvider
{

	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(AbstractDataProvider.class);
    /**
     *
     */
    protected static final String           INPUT_OBJECT_KEY  = "input";
    /**
     *
     */
    protected static final String           OUTPUT_OBJECT_KEY = "output";
    /**
     *
     */
    protected static final String           DEFAULT_SOURCE    = "GVBuffer";
    private String                          name;
    private Map<String, FieldExpressionKey> keys              = new LinkedHashMap<String, FieldExpressionKey>();
    /**
     *
     */
    protected String                        sourceSelector;
    /**
     *
     */
    protected boolean                       resetCalled       = false;

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getFieldKey(java.lang.String)
     */
    @Override
    public FieldExpressionKey getFieldKey(String fieldId)
    {
        return keys.get(fieldId);
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getFieldKeys()
     */
    @Override
    public Collection<FieldExpressionKey> getFieldKeys()
    {
        return keys.values();
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node dpConfigNode) throws XMLConfigException
    {
        name = XMLConfig.get(dpConfigNode, "@name");
        sourceSelector = XMLConfig.get(dpConfigNode, "@source-selector", DEFAULT_SOURCE);
        NodeList nodeList = XMLConfig.getNodeList(dpConfigNode, "*[@type='field']");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String key = XMLConfig.get(node, "@key");
            String direction = XMLConfig.get(node, "@direction", "INOUT");
            Node expressionNode = XMLConfig.getNode(node, "Expression");
            String expressionType = XMLConfig.get(expressionNode, "@type");
            String expression = XMLConfig.get(expressionNode, "text()").trim();
            FieldExpressionKey fKey = new FieldExpressionKey(key, expressionType, expression, direction);
            keys.put(key, fKey);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @param expressionType
     * @return the expression evaluator configured for the expression type
     *         passed.
     * @throws ExpressionEvaluatorException
     */
    protected ExpressionEvaluator getExpressionEvaluator(String expressionType) throws ExpressionEvaluatorException
    {
        return ExpressionEvaluatorHelper.getExpressionEvaluator(expressionType);
    }

    /**
     * @param key
     * @param value
     */
    protected void addToContext(String key, Object value)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding to context: " + key + " - {" + value + "}.");
        }
        ExpressionEvaluatorHelper.addToContext(key, value);
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#setObject(java.lang.Object)
     */
    @Override
    public void setObject(Object object) throws Exception
    {
        ExpressionEvaluatorHelper.startEvaluation();
        if (!resetCalled) {
            reset();
        }
        else {
            resetCalled = false;
        }
        addToContext(INPUT_OBJECT_KEY, object);
        addToContext(OUTPUT_OBJECT_KEY, getResult());
        Object sourceObject = object;
        if (!DEFAULT_SOURCE.equals(sourceSelector)) {
            sourceObject = getExpressionEvaluator(ExpressionEvaluatorHelper.OGNL_EXPRESSION_LANGUAGE).getValue(
                    sourceSelector, object);
        }
        try {
            for (FieldExpressionKey key : getFieldKeys()) {
                ExpressionEvaluator expressionEvaluator = getExpressionEvaluator(key.getExpressionType());
                final String expression = key.getExpression();
                final String fieldId = key.getFieldId();
                logger.debug("Working on field " + fieldId + " with expression " + expression);
                switch (key.getDirection()) {
                    case INOUT :{
                        Object inputValue = expressionEvaluator.getValue(expression, sourceObject);
                        addToContext("key_" + fieldId, inputValue);
                        setValue(key, inputValue);
                    }
                        break;
                    case IN :{
                        Object inputValue = expressionEvaluator.getValue(expression, sourceObject);
                        addToContext("key_" + fieldId, inputValue);
                    }
                        break;
                    case OUT :{
                        expressionEvaluator.getValue(expression, getResult());
                    }
                }
            }
        }
        catch (Exception exc) {
            logger.error("Error evaluating expressions", exc);
            throw new DataProviderException("DP_EXPRESSION_EVALUATION_ERROR", new String[][]{
                    {"className", getClass().getName()}, {"method", "setObject(Object object)"},
                    {"cause", exc.getMessage()}}, exc);
        }
        finally {
            ExpressionEvaluatorHelper.endEvaluation();
        }
    }

    /**
     * Returns the internal object that the DataProvider is constructing.
     *
     * @return the internal object that the DataProvider is constructing.
     * @throws Exception
     */
    protected abstract Object getInternalObject() throws Exception;

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getValue(java.lang.String)
     */
    @Override
    public Object getValue(String fieldExpressionKey) throws Exception
    {
        FieldExpressionKey fieldKey = getFieldKey(fieldExpressionKey);
        if (fieldKey != null) {
            return getValue(fieldKey);
        }
        return null;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#setContext(java.lang.Object)
     */
    @Override
    public void setContext(Object object) throws Exception
    {
        reset();
        resetCalled = true;
    }

    @Override
    public String toString() {
        return "[" + name + "/" + getClass().getName() + "@" + Integer.toHexString(hashCode()) + "]";
    }
}
