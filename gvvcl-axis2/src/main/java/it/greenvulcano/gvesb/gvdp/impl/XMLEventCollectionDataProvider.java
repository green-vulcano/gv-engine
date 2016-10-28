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
import it.greenvulcano.gvesb.gvdp.DataProviderException;
import it.greenvulcano.gvesb.gvdp.FieldExpressionKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;

/**
 * This data provider provides a collection of objects constructed using XML
 * events.
 *
 * @version 3.0.0 Mar 22, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public class XMLEventCollectionDataProvider extends AbstractDataProvider
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(XMLEventCollectionDataProvider.class);
    /**
     *
     */
    protected static final String ENVIRONMENT_KEY    = "environment";
    /**
     *
     */
    protected static final String PARSER_KEY         = "parser";

    private List<Object>          internalCollection = null;

    /**
     * @see it.greenvulcano.gvesb.gvdp.AbstractDataProvider#getInternalObject()
     */
    @Override
    protected Object getInternalObject() throws Exception
    {
        return internalCollection;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getResult()
     */
    @Override
    public Object getResult() throws Exception
    {
        return internalCollection;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getValue(it.greenvulcano.gvesb.gvdp.FieldExpressionKey)
     */
    @Override
    public Object getValue(FieldExpressionKey fieldExpressionKey) throws Exception
    {
        return internalCollection;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#reset()
     */
    @Override
    public void reset()
    {
        internalCollection = null;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#setObject(java.lang.Object)
     */
    @Override
    public final void setObject(Object object) throws Exception
    {
        ExpressionEvaluatorHelper.startEvaluation();
        if (!resetCalled) {
            reset();
        }
        else {
            resetCalled = false;
        }
        addToContext(INPUT_OBJECT_KEY, object);
        if (internalCollection == null) {
            internalCollection = new ArrayList<Object>();
        }
        addToContext(OUTPUT_OBJECT_KEY, internalCollection);
        Map<String, Object> environment = new HashMap<String, Object>();
        addToContext(ENVIRONMENT_KEY, environment);
        Object sourceObject = object;
        if (!DEFAULT_SOURCE.equals(sourceSelector)) {
            sourceObject = getExpressionEvaluator(ExpressionEvaluatorHelper.OGNL_EXPRESSION_LANGUAGE).getValue(
                    sourceSelector, object);
        }
        try {
            if (sourceObject instanceof OMElement) {
                XMLStreamReader parser = ((OMElement) sourceObject).getXMLStreamReaderWithoutCaching();
                addToContext(PARSER_KEY, parser);
                while (parser.hasNext()) {
                    String field = null;
                    switch (parser.next()) {
                        case XMLStreamReader.START_DOCUMENT :{
                            field = "START_DOCUMENT";
                        }
                            break;
                        case XMLStreamReader.END_DOCUMENT :{
                            field = "END_DOCUMENT";
                        }
                            break;
                        case XMLStreamReader.NAMESPACE :{
                            field = "NAMESPACE";
                        }
                            break;
                        case XMLStreamReader.PROCESSING_INSTRUCTION :{
                            field = "PROCESSING_INSTRUCTION";
                        }
                            break;
                        case XMLStreamReader.ENTITY_REFERENCE :{
                            field = "ENTITY_REFERENCE";
                        }
                            break;
                        case XMLStreamReader.DTD :{
                            field = "DTD";
                        }
                            break;
                        case XMLStreamReader.ATTRIBUTE :{
                            field = "ATTRIBUTE";
                        }
                            break;
                        case XMLStreamReader.START_ELEMENT :{
                            field = "START_ELEMENT";
                        }
                            break;
                        case XMLStreamReader.END_ELEMENT :{
                            field = "END_ELEMENT";
                        }
                            break;
                        case XMLStreamReader.CHARACTERS :{
                            field = "CHARACTERS";
                        }
                    }
                    processField(sourceObject, getFieldKey(field));
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

    private void processField(Object sourceObject, FieldExpressionKey key) throws Exception
    {
        if (key != null) {
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

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#setValue(it.greenvulcano.gvesb.gvdp.FieldExpressionKey,
     *      java.lang.Object)
     */
    @SuppressWarnings("unchecked")
	@Override
    public void setValue(FieldExpressionKey fieldExpressionKey, Object value) throws Exception
    {
        if (value instanceof List<?>) {
            internalCollection = (List<Object>) value;
        }
        else if (value instanceof Object[]) {
            internalCollection = Arrays.asList((Object[]) value);
        }
        else {
            throw new DataProviderException("DP_COLLECTION_BAD_VALUE", new String[][]{
                    {"className", getClass().getName()},
                    {"method", "setValue(FieldExpressionKey fieldExpressionKey, Object value)"},
                    {"cause", "Resulting expression value is not a collection."},
                    {"failingClass", value == null ? "NULL" : value.getClass().getName()}});
        }
    }
}
