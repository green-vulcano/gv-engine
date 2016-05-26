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
package it.greenvulcano.expression;

import java.util.Map;

/**
 * This interface evaluates expression over an object.
 * 
 * @version 3.0.0 Feb 27, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public interface ExpressionEvaluator
{
    /**
     * Evaluates the 'get' expression to the object passed
     * 
     * @param expression
     * @param object
     * @return the object resulting from expression evaluation
     * @throws ExpressionEvaluatorException
     */
    public Object getValue(String expression, Object object) throws ExpressionEvaluatorException;

    /**
     * Evaluates the 'set' expression to the object passed
     * 
     * @param expression
     * @param value
     *        the value to set to the object
     * @param object
     *        the object to set the value
     * @throws ExpressionEvaluatorException
     */
    public void setValue(String expression, Object value, Object object) throws ExpressionEvaluatorException;

    /**
     * @param key
     * @param value
     */
    public void addToContext(String key, Object value);

    /**
     * @param context
     */
    public void addAllToContext(Map<String, Object> context);

    /**
     * Clean up internal evaluation structures.
     */
    public void cleanUp();
}
