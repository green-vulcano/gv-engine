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
 *
 *
 *
 */
public class IntRangeDef implements RangeDef<Integer>
{
    /**
     * Lower bound for check
     */
    private int min;
    /**
     * Upper bound for check
     */
    private int max;

    /**
     * Constructor
     */
    public IntRangeDef()
    {
        min = Integer.MIN_VALUE;
        max = Integer.MIN_VALUE;
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
    public IntRangeDef(int min, int max)
    {
        if (min > max) {
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
        min = XMLConfig.getInteger(node, "@min", Integer.MIN_VALUE);
        max = XMLConfig.getInteger(node, "@max", Integer.MAX_VALUE);

        if (min > max) {
            throw new XMLConfigException("@min must be < @max for node: " + XPathFinder.buildXPath(node));
        }
    }

    /**
     * @see it.greenvulcano.gvesb.internal.condition.RangeDef#contains(java.lang.Object)
     */
    public boolean contains(Integer value)
    {
        return ((min <= value) && (value <= max));
    }

    /**
     * @return Returns the min.
     */
    public Integer getMin()
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
