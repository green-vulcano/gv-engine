/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.utils;

import it.greenvulcano.expression.ExpressionEvaluatorHelper;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.util.txt.StringPlaceholderExpander;

/**
 * Subclass of {@link it.greenvulcano.util.txt.StringPlaceholderExpander
 * <code>StringPlaceholderExpander</code>} which supports the following
 * placeholders:
 *
 * <ul>
 * <li><code>%{ognl:expression}</code> is replaced with the value returned by a
 * call to the OGNL expression evaluator.</li>
 * </ul>
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class ExpressionEvaluatorPlaceholderExpander extends StringPlaceholderExpander
{

    private GVBuffer currentGVBuffer;

    /**
     * This method takes a text {@link java.lang.String <code>String</code>} as
     * input, possibly containing placeholders in the supported formats, and
     * returns a copy of the string with the placeholders replaced by the
     * corresponding values, or by an empty string if the value is not defined.
     *
     * @param input
     *        a text {@link java.lang.String <code>String</code>}, possibly
     *        containing placeholders in the supported formats.
     * @param gvBuffer
     *        an instance of {@link it.greenvulcano.gvesb.buffer.GVBuffer
     *        <code>GVBuffer</code>} whose content can used for placeholder
     *        replacement.
     * @return a copy of the input {@link java.lang.String <code>String</code>}
     *         with the placeholders replaced by the corresponding values, or by
     *         an empty string if the value is not defined.
     * @throws Exception
     *         if any error occurs.
     */
    public String expand(String input, GVBuffer gvBuffer) throws Exception
    {
        currentGVBuffer = gvBuffer;
        return super.expand(input);
    }

    /**
     * @see it.greenvulcano.util.txt.StringPlaceholderExpander#getPlaceholderValue(java.lang.String)
     */
    @Override
    protected synchronized String getPlaceholderValue(String phName) throws Exception
    {
        String result = null;

        int separator = phName.lastIndexOf(':');
        boolean isValid = false;
        String expressionType = null;
        if (separator != -1) {
            expressionType = phName.substring(0, separator);
        }
        isValid = ExpressionEvaluatorHelper.isValidExpressionType(expressionType);
        if (isValid) {
            String expression = phName.substring(separator + 1);
            if (currentGVBuffer != null) {
                Object obj = ExpressionEvaluatorHelper.getValue(expressionType, expression, currentGVBuffer);
                result = obj == null ? "" : obj.toString();
            }
            else {
                result = "";
            }
        }
        else {
            result = super.getPlaceholderValue(phName);
        }

        return result;
    }
}
