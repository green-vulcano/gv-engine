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
package it.greenvulcano.gvesb.virtual.internal;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.util.regex.Pattern;

import org.w3c.dom.Node;

/**
 *
 * ExtractData class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */

public class ExtractData
{

    private String  field;
    private String  expression;
    private int     matchingGroup;
    private Pattern pattern = null;

    /**
     * @param node
     * @throws XMLConfigException
     */
    public ExtractData(Node node) throws XMLConfigException
    {
        field = XMLConfig.get(node, "@field");
        expression = XMLConfig.get(node, "@expression");
        matchingGroup = XMLConfig.getInteger(node, "@matching-group", 0);
    }

    /**
     * @return Returns the expression.
     */
    public String getExpression()
    {
        return expression;
    }

    /**
     * @param expression
     *        The expression to set.
     */
    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    /**
     * @return Returns the field.
     */
    public String getField()
    {
        return field;
    }

    /**
     * @param field
     *        The field to set.
     */
    public void setField(String field)
    {
        this.field = field;
    }

    /**
     * @return Returns the matchingGroup.
     */
    public int getMatchingGroup()
    {
        return matchingGroup;
    }

    /**
     * @param matchingGroup
     *        The matchingGroup to set.
     */
    public void setMatchingGroup(int matchingGroup)
    {
        this.matchingGroup = matchingGroup;
    }

    /**
     * @return Returns the pattern.
     */
    public Pattern getPattern()
    {
        return pattern;
    }

    /**
     * @param pattern
     *        The pattern to set.
     */
    public void setPattern(Pattern pattern)
    {
        this.pattern = pattern;
    }
}