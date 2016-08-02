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
package it.greenvulcano.gvesb.utils;

import it.greenvulcano.expression.ExpressionEvaluator;
import it.greenvulcano.expression.ExpressionEvaluatorException;
import it.greenvulcano.expression.ExpressionEvaluatorHelper;
import it.greenvulcano.util.txt.StringToHTML;

/**
 * 
 * This class creates a String getting dynamic parameters from an Object.
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class MessageFormatter
{
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MessageFormatter.class);

    /**
     * The string to formatter
     */
    private String              message    = "";

    /**
     * If true, escape HTML chars will be substituted.
     */
    private boolean             escapeHtml = false;

    /**
     * The object where the formatter get value for the dynamic parameters
     */
    private Object              object     = null;

    /**
     * The secondary object where the formatter get value for the dynamic
     * parameters
     */
    private Object              secObject  = null;

    /**
     * Constructor A default message will be used.
     * 
     * @param object
     *        the object where the formatter get value for the dynamic
     *        parameters
     * @param escapeHtml
     *        if true, HTML escape chars will be substituted.
     */
    public MessageFormatter(Object object, boolean escapeHtml)
    {
        StringBuilder buf = new StringBuilder();
        String lineSeparator = System.getProperty("line.separator");
        buf.append("System: ............ ${system}").append(lineSeparator).append("Service: ........... ${service}").append(
                lineSeparator).append("Id: ................ ${id}").append(lineSeparator).append(
                "Return Code: ....... ${retCode}").append(lineSeparator).append(lineSeparator).append(lineSeparator).append(
                "Properties:").append(lineSeparator).append("${properties}").append(lineSeparator).append(lineSeparator).append(
                "Data Buffer:").append(lineSeparator).append("${buffer}").append(lineSeparator);
        message = buf.toString();
        this.escapeHtml = escapeHtml;
        this.object = object;
    }

    /**
     * Constructor
     * 
     * @param message
     *        the string to formatter
     * @param object
     *        the object where the formatter get value for the dynamic
     *        parameters
     */
    public MessageFormatter(String message, Object object)
    {
        this(message, object, false);
    }

    /**
     * Constructor
     * 
     * @param message
     *        the string to formatter
     * @param object
     *        the object where the formatter get value for the dynamic
     *        parameters
     * @param secObject
     *        the secondary object where the formatter get value for
     *        the dynamic parameters
     */
    public MessageFormatter(String message, Object object, Object secObject)
    {
        this(message, object, secObject, false);
    }

    /**
     * Constructor
     * 
     * @param message
     *        the string to formatter
     * @param object
     *        the object where the formatter get value for the dynamic
     *        parameters
     * @param escapeHtml
     *        if true, HTML escape chars will be substituted.
     */
    public MessageFormatter(String message, Object object, boolean escapeHtml)
    {
        this.message = message;
        this.object = object;
        this.escapeHtml = escapeHtml;
    }

    /**
     * Constructor
     * 
     * @param message
     *        the string to formatter
     * @param object
     *        the object where the formatter get value for the dynamic
     *        parameters
     * @param secObject
     *        the secondary object where the formatter get value for the
     *        dynamic parameters
     * @param escapeHtml
     *        if true, HTML escape chars will be substituted.
     */
    public MessageFormatter(String message, Object object, Object secObject, boolean escapeHtml)
    {
        this.message = message;
        this.object = object;
        this.secObject = secObject;
        this.escapeHtml = escapeHtml;
    }

    /**
     * This method format a given string getting value for the dynamic parameter
     * from a given object.
     * 
     * @return string the formatted string
     */
    @Override
    public String toString()
    {
        String beginParam = "${";
        String endParam = "}";
        int indexOfBegin = 0;
        int indexOfEnd = 0;

        ExpressionEvaluator evaluator = null;
        try {
            evaluator = ExpressionEvaluatorHelper.getExpressionEvaluator(ExpressionEvaluatorHelper.OGNL_EXPRESSION_LANGUAGE);
        }
        catch (ExpressionEvaluatorException e1) {
            e1.printStackTrace();
        }

        StringBuilder buf = new StringBuilder();

        int startSubStr = 0;
        while (startSubStr < message.length()) {
            indexOfBegin = message.indexOf(beginParam, startSubStr);
            indexOfEnd = message.indexOf(endParam, startSubStr);

            if (indexOfBegin != -1) {
                buf.append(message.substring(startSubStr, indexOfBegin));

                String fieldName = message.substring(indexOfBegin + 2, indexOfEnd);
                if (evaluator != null) {
                    Object fieldValue = null;
                    try {
                        fieldValue = evaluator.getValue(fieldName, object);
                    }
                    catch (ExpressionEvaluatorException exc) {
                        if (exc.getCause() instanceof ognl.NoSuchPropertyException) {
                            fieldValue = "NoValid";
                        }
                        else {
                            logger.warn("Error while evaluating expression: " + beginParam + fieldName + endParam, exc);
                        }
                    }
                    // If no correct value inserted in the dynamic parameter
                    // return the no correct value ES : ${NoValidParam}
                    //
                    if (fieldValue != null) {
                        if (fieldValue.equals("NoValid")) {
                            if (secObject != null) {
                                try {
                                    fieldValue = evaluator.getValue(fieldName, secObject);
                                }
                                catch (ExpressionEvaluatorException exc) {
                                    if (exc.getCause() instanceof ognl.NoSuchPropertyException) {
                                        fieldValue = "NoValid";
                                    }
                                    else {
                                        logger.warn("Error while evaluating expression: " + beginParam + fieldName
                                                + endParam, exc);
                                    }
                                }
                            }
                            if (fieldValue != null) {
                                if (fieldValue.equals("NoValid")) {
                                    fieldValue = beginParam + fieldName + endParam;
                                }
                            }
                        }

                        if (escapeHtml) {
                            fieldValue = StringToHTML.quote(fieldValue.toString());
                        }
                    }

                    buf.append(fieldValue);
                }
                else {
                    buf.append(fieldName);
                }

                startSubStr = indexOfEnd + 1;
            }
            else {
                buf.append(message.substring(startSubStr));
                startSubStr = message.length();
            }
        }

        return buf.toString();
    }
}
