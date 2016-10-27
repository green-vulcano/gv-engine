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
package it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter;

import it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ParamDescription;
import it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IParameterWriter;

import javax.xml.stream.XMLStreamConstants;

import org.apache.axiom.om.OMText;

/**
 * TextParameter class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class TextParameter implements IParameter, XMLStreamConstants
{
    private String value;
    private int    type;

    /**
     * @param text
     */
    public TextParameter(String text)
    {
        this.value = text;
        this.type = OMText.TEXT_NODE;
    }

    /**
     * Creates a new instance of TextParameter Type must be specified so that
     * the IParameterWriter class knows how the text should be written. If you
     * use the OMParameterWriter the type should be one of the specified types
     * of the OMNode rather the XMLStreamConstants interface.
     *
     * @param text
     * @param type
     */
    public TextParameter(String text, int type)
    {
        this.value = text;
        this.type = type;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameter#write(it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IParameterWriter)
     */
    public void write(IParameterWriter writer)
    {
        writer.writeText(value, type);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameter#isComplex()
     */
    public boolean isComplex()
    {
        return false;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameter#isArray()
     */
    public boolean isArray()
    {
        return false;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameter#getParameterDesc()
     */
    public ParamDescription getParameterDesc()
    {
        return null;
    }
}
