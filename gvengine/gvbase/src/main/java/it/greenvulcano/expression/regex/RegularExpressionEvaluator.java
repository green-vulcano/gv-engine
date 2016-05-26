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
package it.greenvulcano.expression.regex;

import it.greenvulcano.expression.ExpressionEvaluator;
import it.greenvulcano.expression.ExpressionEvaluatorException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version 3.0.0 Mar 22, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 * 
 */
public class RegularExpressionEvaluator implements ExpressionEvaluator
{
    private static final Map<String, Pattern> patterns = new ConcurrentHashMap<String, Pattern>();

    /**
     * @see it.greenvulcano.expression.ExpressionEvaluator#addAllToContext(java.util.Map)
     */
    @Override
    public void addAllToContext(Map<String, Object> context)
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.expression.ExpressionEvaluator#addToContext(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public void addToContext(String key, Object value)
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.expression.ExpressionEvaluator#getValue(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public Object getValue(String expression, Object object) throws ExpressionEvaluatorException
    {
        Pattern pattern = patterns.get(expression);
        if (pattern == null) {
            pattern = Pattern.compile(expression);
            patterns.put(expression, pattern);
        }
        Collection<String> result = new ArrayList<String>();
        Matcher matcher = pattern.matcher(object.toString());
        if (matcher.matches()) {
            for (int i = 1; i < (matcher.groupCount() + 1); i++) {
                result.add(matcher.group(i));
            }
        }
        return result;
    }

    /**
     * @see it.greenvulcano.expression.ExpressionEvaluator#setValue(java.lang.String,
     *      java.lang.Object, java.lang.Object)
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
        // do nothing
    }
}
