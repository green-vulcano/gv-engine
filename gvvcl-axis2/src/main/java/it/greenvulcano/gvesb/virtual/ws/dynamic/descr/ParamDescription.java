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
package it.greenvulcano.gvesb.virtual.ws.dynamic.descr;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;


/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ParamDescription implements Description<ParamDescription>
{
    private static final long     serialVersionUID = 1L;
    /**
     *
     */
    public static final QName     VOID             = new QName("VOID");
    /**
     *
     */
    public static final QName     EMBEDDING        = new QName("EMBEDDING");

    boolean                       array_;
    QName                         xmlName_;
    GroupType                     groupType_;

    /**
     * for nested complexType parameters_
     */
    Map<String, ParamDescription> parameters_;
    ParamDescription              parent_;
    QName                         typeXmlName_;
    boolean                       cyclic_;

    /**
     * @param parent
     */
    public ParamDescription(ParamDescription parent)
    {
        parameters_ = new LinkedHashMap<String, ParamDescription>();
        parent_ = parent;
    }

    private static void appendParameterPath(StringBuilder sb, ParamDescription parent)
    {
        if (parent != null) {
            appendParameterPath(sb, parent.parent_);
            sb.append("/").append(parent.xmlName_.getLocalPart());
        } // else parent is root node and null/nil
        else {
            sb.append("/");
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String separator = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();

        if (parent_ == null) {
            sb.append("$PARAMETER: ").append(separator);
        }
        else {
            sb.append("$NESTED PARAMETER OF: ");
            appendParameterPath(sb, parent_);
            sb.append(separator);
        }

        sb.append("ParameterName: ").append(xmlName_).append(separator);
        sb.append("ParameterType: ").append(typeXmlName_);

        if ((xmlName_ == null) && (typeXmlName_ == null)) {
            sb.append(separator).append("=> Any element");
        }

        if (groupType_ != null) {
            sb.append(separator).append("Group: ").append(groupType_.toString());
        }

        if (array_) {
            sb.append(separator).append("IsArray: ").append("YES");
        }

        if (cyclic_) {
            sb.append(separator).append("IsCyclic: ").append("YES");
        }

        if (parameters_ != null) {
            Iterator<?> itr = parameters_.values().iterator();
            while (itr.hasNext()) {
                sb.append(separator).append(itr.next());
            }
        }

        return sb.toString();
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.descr.Description#getDescriptions()
     */
    public Map<String, ParamDescription> getDescriptions()
    {
        return parameters_;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.descr.Description#getXmlName()
     */
    public QName getXmlName()
    {
        return xmlName_;
    }

    /**
     * @return the XML type
     */
    public QName getXmlType()
    {
        return typeXmlName_;
    }

    /**
     * @return if this parameter is an array.
     */
    public boolean isArray()
    {
        return array_;
    }

    /**
     * @param array
     */
    public void setArray(boolean array)
    {
        this.array_ = array;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.descr.Description#setDescriptions(java.util.Map)
     */
    public void setDescriptions(Map<String, ParamDescription> parameters)
    {
        parameters_ = parameters;

        if (parameters != null) {
            Iterator<?> itr = parameters_.values().iterator();
            while (itr.hasNext()) {
                ParamDescription p = (ParamDescription) itr.next();
                p.setParent(this);
            }
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.descr.Description#setXmlName(javax.xml.namespace.QName)
     */
    public void setXmlName(QName parameterName)
    {
        this.xmlName_ = parameterName;
    }

    /**
     * @param parameterType
     */
    public void setTypeXmlName(QName parameterType)
    {
        this.typeXmlName_ = parameterType;
    }

    /**
     * @return the group type
     */
    public GroupType getGroupType()
    {
        return groupType_;
    }

    /**
     * @param groupType
     */
    public void setGroupType(GroupType groupType)
    {
        this.groupType_ = groupType;
    }

    /**
     * @return the parent parameter
     */
    public ParamDescription getParent()
    {
        return parent_;
    }

    /**
     * @param parent_
     */
    public void setParent(ParamDescription parent_)
    {
        this.parent_ = parent_;
    }

    /**
     * @return if this parameter is cyclic
     */
    public boolean isCyclic()
    {
        return cyclic_;
    }

    /**
     * @param cyclic_
     */
    public void setCyclic(boolean cyclic_)
    {
        this.cyclic_ = cyclic_;
    }

    /**
     * @version 3.0.0 Mar 24, 2010
     * @author nunzio
     *
     */
    public enum GroupType {
        /**
         *
         */
        Sequence,
        /**
         *
         */
        All,
        /**
         *
         */
        Choice,
        /**
         *
         */
        Unknown
    }
}
