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
package it.greenvulcano.gvesb.internal.condition;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.Iterator;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class GVBufferPropertyDef implements GVBufferProperty
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(GVBufferCondition.class);

    /**
     * The property name.
     */
    private String              name             = "";
    /**
     * If true the value is managed as string.
     */
    private boolean             valueIsString    = true;
    /**
     * A list of ranges.
     */
    private Vector<Object>      rangeVector      = new Vector<Object>();
    /**
     * The AND group at which the instance belong.
     */
    private String              group            = "";
    /**
     * The operator to be used.
     */
    private int                 operator         = GVB_PROP_PRESENT;

    private boolean             isUseRangeVector = false;

    /**
     * Initialize the instance.
     * 
     * @param node
     *        the node from which read configuration data
     * @throws XMLConfigException
     *         if errors occurs
     */
    public void init(Node node) throws XMLConfigException
    {
        name = XMLConfig.get(node, "@name", "");
        valueIsString = XMLConfig.get(node, "@value-type", "text").equals("text");
        group = XMLConfig.get(node, "@group", "");

        setRangeVector(node);

        if (XMLConfig.exists(node, "@value") || isUseRangeVector) {
            String sop = XMLConfig.get(node, "@operator", "");
            if (!sop.equals("")) {
                if (sop.equals("equal")) {
                    operator = GVB_PROP_EQUAL;
                }
                else if (sop.equals("lesser")) {
                    operator = GVB_PROP_LESSER;
                }
                else if (sop.equals("lesser-equal")) {
                    operator = GVB_PROP_LESSER_EQUAL;
                }
                else if (sop.equals("greater")) {
                    operator = GVB_PROP_GREATER;
                }
                else if (sop.equals("greater-equal")) {
                    operator = GVB_PROP_GREATER_EQUAL;
                }
                else if (sop.equals("different")) {
                    operator = GVB_PROP_DIFFERENT;
                }
                else {
                    throw new XMLConfigException("Invalid value '" + sop + "' for attribute 'operator' in element "
                            + XPathFinder.buildXPath(node));
                }
            }
        }
    }

    /**
     * Initialize the ranges vector.
     * 
     * @param node
     *        the node from which read configuration data
     * @throws XMLConfigException
     *         if errors occurs
     */
    private void setRangeVector(Node node) throws XMLConfigException
    {
        NodeList nl = XMLConfig.getNodeList(node, "RangeDef");

        if ((nl == null) || (nl.getLength() == 0)) {
            if (valueIsString) {
                String value = XMLConfig.get(node, "@value", "");
                StringRangeDef range = null;
                if (value.equals("")) {
                    range = new StringRangeDef();
                }
                else {
                    range = new StringRangeDef(value, value);
                }
                logger.debug("Adding StringRangeDef: " + range);
                rangeVector.add(range);
            }
            else {
                int value = XMLConfig.getInteger(node, "@value", Integer.MIN_VALUE);
                IntRangeDef range = null;
                if (value == Integer.MIN_VALUE) {
                    range = new IntRangeDef();
                }
                else {
                    range = new IntRangeDef(value, value);
                }
                logger.debug("Adding IntRangeDef: " + range);
                rangeVector.add(range);
            }
            return;
        }

        isUseRangeVector = true;
        for (int i = 0; i < nl.getLength(); i++) {
            if (valueIsString) {
                StringRangeDef range = new StringRangeDef();
                range.init(nl.item(i));
                logger.debug("Adding StringRangeDef: " + range);
                rangeVector.add(range);
            }
            else {
                IntRangeDef range = new IntRangeDef();
                range.init(nl.item(i));
                logger.debug("Adding IntRangeDef: " + range);
                rangeVector.add(range);
            }
        }
    }

    /**
     * @see it.greenvulcano.gvesb.internal.condition.GVBufferProperty#isGroup()
     */
    @Override
    public boolean isGroup()
    {
        return !group.equals("");
    }

    /**
     * @see it.greenvulcano.gvesb.internal.condition.GVBufferProperty#getGroup()
     */
    @Override
    public String getGroup()
    {
        return group;
    }

    /**
     * Perform the check.
     * 
     * @param gvBuffer
     *        the GVBuffer to check
     * @return the check result
     */
    @Override
    public boolean check(GVBuffer gvBuffer)
    {
        String value = gvBuffer.getProperty(name);
        logger.debug("Checking gvBuffer[" + name + "]=[" + value + "]");

        if (value != null) {
            if (operator == GVB_PROP_PRESENT) {
                return true;
            }
            if ((operator == GVB_PROP_EQUAL) || (operator == GVB_PROP_DIFFERENT)) {
                if (valueIsString) {
                    boolean stringMatch = false;
                    Iterator<Object> i = rangeVector.iterator();
                    while (i.hasNext() && !stringMatch) {
                        stringMatch = ((StringRangeDef) i.next()).contains(value);
                    }
                    return ((operator == GVB_PROP_EQUAL) == stringMatch);
                }
                try {
                    int iValue = Math.round(Float.parseFloat(value)); //Integer.parseInt(value);
                    boolean intMatch = false;
                    Iterator<Object> i = rangeVector.iterator();
                    while (i.hasNext() && !intMatch) {
                        intMatch = ((IntRangeDef) i.next()).contains(iValue);
                    }
                    return ((operator == GVB_PROP_EQUAL) == intMatch);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            else {
                if (valueIsString) {
                    String testValue = ((StringRangeDef) rangeVector.elementAt(0)).getMin();
                    switch (operator) {
                        case GVB_PROP_LESSER :
                            return (value.compareTo(testValue) < 0);
                        case GVB_PROP_LESSER_EQUAL :
                            return (value.compareTo(testValue) <= 0);
                        case GVB_PROP_GREATER :
                            return (value.compareTo(testValue) > 0);
                        case GVB_PROP_GREATER_EQUAL :
                            return (value.compareTo(testValue) >= 0);
                    }
                }
                else {
                    int iValue = 0;
                    try {
                        iValue = Math.round(Float.parseFloat(value)); //Integer.parseInt(value);
                        int testValue = ((IntRangeDef) rangeVector.elementAt(0)).getMin();
                        switch (operator) {
                            case GVB_PROP_LESSER :
                                return (iValue < testValue);
                            case GVB_PROP_LESSER_EQUAL :
                                return (iValue <= testValue);
                            case GVB_PROP_GREATER :
                                return (iValue > testValue);
                            case GVB_PROP_GREATER_EQUAL :
                                return (iValue >= testValue);
                        }
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                }
            }
        }

        return false;
    }

    @Override
    public String toString()
    {
        return "prop[" + name + "] - oper[" + operator + "] - val:" + rangeVector;
    }
}
