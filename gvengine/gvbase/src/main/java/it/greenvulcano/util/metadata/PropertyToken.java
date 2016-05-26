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
package it.greenvulcano.util.metadata;

import java.util.Map;
import java.util.Vector;

/**
 * PropertyToken class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class PropertyToken
{
    int                           begin     = -1;
    int                           end       = -1;
    String                        value     = "";
    String                        type      = "";
    private Vector<PropertyToken> subTokens = null;

    /**
     * @param begin
     * @param end
     * @param value
     * @param type
     */
    public PropertyToken(int begin, int end, String value, String type)
    {
        this.begin = begin;
        this.end = end;
        this.value = value;
        this.type = type;
    }

    /**
     * @return Returns the begin.
     */
    public int getBegin()
    {
        return begin;
    }

    /**
     * @return Returns the end.
     */
    public int getEnd()
    {
        if (subTokens != null) {
            PropertyToken subToken = (PropertyToken) subTokens.lastElement();
            return subToken.getEnd();
        }
        return end;
    }

    /**
     * @return Returns the type.
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param inProperties
     * @param obj
     * @param extra
     * @return Returns the value.
     * @throws PropertiesHandlerException
     */
    public String getValue(Map<String, Object> inProperties, Object obj, Object extra)
            throws PropertiesHandlerException
    {
        String retVal = value;
        if (subTokens != null) {
            for (int i = 0; i < subTokens.size(); i++) {
                PropertyToken subToken = subTokens.elementAt(i);
                retVal += subToken.getValue(inProperties, obj, extra);
            }
        }
        if (!type.equals("")) {
            retVal = PropertiesHandler.expandInternal(type, retVal, inProperties, obj, extra);
        }
        return retVal;
    }

    /**
     *
     * @param subToken
     */
    public void addSubToken(PropertyToken subToken)
    {
        if (subTokens == null) {
            subTokens = new Vector<PropertyToken>();
        }
        subTokens.add(subToken);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String result = "PropertyToken: type='" + type + "' - value= '" + value + "' - begin=" + begin + " - end="
                + end;
        if (subTokens != null) {
            result += "\nBEGIN SUB\n";
            for (int i = 0; i < subTokens.size(); i++) {
                PropertyToken subToken = (PropertyToken) subTokens.elementAt(i);
                result += subToken + "\n";
            }
            result += "END SUB";
        }
        return result;
    }
}
