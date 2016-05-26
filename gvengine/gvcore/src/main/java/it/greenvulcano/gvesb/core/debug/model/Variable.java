/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.debug.model;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVBuffer.Field;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.debug.utils.ExceptionConverter;
import it.greenvulcano.gvesb.core.debug.utils.GVBufferConverter;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.3.0 Dic 14, 2012
 * @author GreenVulcano Developer Team
 */
public class Variable extends DebuggerObject
{
    private static final long     serialVersionUID = 4734478388039002009L;
    public static final String    ELEMENT_TAG      = "Variable";
    private String                id               = null;
    private String                name             = null;
    private String                value            = null;
    private Map<String, Variable> values           = new LinkedHashMap<String, Variable>();
    private boolean               isGVBuffer       = false;
    private boolean               isException      = false;
    private transient GVBuffer    origGVBuffer     = null;
    private Class<?>              type;
    private Field                 gvField;

    public static final String    GVFIELD_PFX      = "F$";
    public static final String    PROPERTY_PFX     = "P$";

    public Variable(String name, Class<?> type)
    {
        this.id = name;
        this.name = name;
        this.type = type;
    }

    public Variable(String name, Class<?> type, Object object)
    {
        this(name, type);
        isGVBuffer = object instanceof GVBuffer;
        isException = object instanceof Throwable;
        if (isGVBuffer) {
            origGVBuffer = (GVBuffer) object;
            GVBufferConverter.toDebugger(this, origGVBuffer);
        }
        else if (isException) {
            ExceptionConverter.toDebugger(this, (Throwable) object);
        }
        else if (object != null) {
            this.value = object.toString();
        }
    }

    public Variable(Field field, Class<?> type, Object object)
    {
        this(field.name(), type, object);
        this.gvField = field;
        this.id = GVFIELD_PFX + field.name();
    }

    public Variable(Field field, String name, Class<?> type, Object object)
    {
        this(name, type, object);
        this.gvField = field;
        if (field == Field.PROPERTY) {
            this.id = PROPERTY_PFX + name;
        }
        else {
            this.id = GVFIELD_PFX + name;
        }
    }

    /**
     * @see it.greenvulcano.gvesb.core.debug.model.DebuggerObject#getXML(it.greenvulcano.util.xml.XMLUtils,
     *      org.w3c.dom.Document)
     */
    @Override
    protected Node getXML(XMLUtils xml, Document doc) throws XMLUtilsException
    {
        Element var = xml.createElement(doc, ELEMENT_TAG);
        xml.setAttribute(var, ID_ATTR, id);
        xml.setAttribute(var, NAME_ATTR, name);
        xml.setAttribute(var, TYPE_ATTR, getTypeName());
        if (!values.isEmpty()) {
            for (Variable v : values.values()) {
                var.appendChild(v.getXML(xml, doc));
            }
        }
        else {
            if (value != null) {
                var.setTextContent(value);
            }
        }
        return var;
    }

    public String getTypeName()
    {
        if (type == null) {
            return "";
        }
        return type.getName();
    }

    public String getName()
    {
        return name;
    }

    public GVBuffer getGVBuffer()
    {
        return origGVBuffer;
    }

    public Field getGVBufferField()
    {
        return gvField;
    }

    public void addVar(Variable var)
    {
        values.put(var.id, var);
    }

    public Variable getVar(String key)
    {
        Variable v = values.get(key);
        if (id.toString().equals(key)) {
            v = this;
        }
        else if (isGVBuffer) {
            if (key.startsWith(PROPERTY_PFX)) {
                Variable props = values.get(GVFIELD_PFX + Field.PROPERTY);
                v = props.getVar(key);
            }
        }
        return v;
    }

    public void setVar(String varID, String varValue) throws GVException
    {
        if (isGVBuffer) {
            GVBufferConverter.toESB(this, varID, varValue);
        }
        else {
            value = varValue;
        }
    }

    public String getID()
    {
        return id.toString();
    }
}
