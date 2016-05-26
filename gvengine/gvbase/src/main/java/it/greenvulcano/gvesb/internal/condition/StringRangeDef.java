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
package it.greenvulcano.gvesb.internal.condition;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.util.xpath.XPathFinder;

import org.w3c.dom.Node;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class StringRangeDef implements RangeDef<String>
{
    /**
     * Lower bound for check
     */
    private String min;
    /**
     * Upper bound for check
     */
    private String max;

    /**
     * Constructor
     */
    public StringRangeDef()
    {
        min = "";
        max = "";
    }

    /**
     * Constructor
     *
     * @param min
     *        lower bound
     * @param max
     *        upper bound
     * @throws IllegalArgumentException
     *         if min > max
     */
    public StringRangeDef(String min, String max)
    {
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("'min' must be < 'max'");
        }
        this.min = min;
        this.max = max;
    }

    /**
     * Initialize the instance
     *
     * @param node
     *        the XML node containing configuration data
     * @throws XMLConfigException
     *         if @min > @max
     */
    public void init(Node node) throws XMLConfigException
    {
        min = XMLConfig.get(node, "@min", "");
        max = XMLConfig.get(node, "@max", "");

        if (min.compareTo(max) > 0) {
            throw new XMLConfigException("@min must be < @max for node: " + XPathFinder.buildXPath(node));
        }
    }

    /**
     * @see it.greenvulcano.gvesb.internal.condition.RangeDef#contains(java.lang.Object)
     */
    @Override
    public boolean contains(String value)
    {
        if (min.equals(max)) {
            return min.equals(value);
        }
        if (max.equals("")) {
            return (value.compareTo(min) >= 0);
        }
        else if (min.equals("")) {
            return (value.compareTo(max) <= 0);
        }
        else {
            return ((min.compareTo(value) <= 0) && (value.compareTo(max) <= 0));
        }
    }

    /**
     * @return Returns the min.
     */
    @Override
    public String getMin()
    {
        return min;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return -1;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "[" + min + "," + max + "]";
    }
}
