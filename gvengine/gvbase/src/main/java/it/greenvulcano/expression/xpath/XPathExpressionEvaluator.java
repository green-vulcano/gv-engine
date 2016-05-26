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
package it.greenvulcano.expression.xpath;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.expression.ExpressionEvaluator;
import it.greenvulcano.expression.ExpressionEvaluatorException;
import it.greenvulcano.util.xml.XMLUtils;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * @version 4.0.0 Mar 16, 2016
 * @author GreenVulcano Developer Team
 * 
 * 
 * 
 */
public class XPathExpressionEvaluator implements ExpressionEvaluator {
  
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
        try {
            Object obj = XMLUtils.parseObject_S(object, false, true);
            return XMLConfig.getNodeListCollection((Node) obj, expression);
        }
        catch (Exception e) {
            throw new ExpressionEvaluatorException("Error evaluating XPath expression: " + expression, e);
        }
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
