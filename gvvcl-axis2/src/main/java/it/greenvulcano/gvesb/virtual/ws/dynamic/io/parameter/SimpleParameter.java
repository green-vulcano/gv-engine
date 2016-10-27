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


/**
 * SimpleParameter class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class SimpleParameter implements IParameter
{
    private ParamDescription desc;
    private String           value;

    /**
     * @param desc
     */
    public SimpleParameter(ParamDescription desc)
    {
        this.desc = desc;
    }

    /**
     * Creates a new instance of SimpleParameter
     *
     * @param desc
     * @param value
     */
    public SimpleParameter(ParamDescription desc, String value)
    {
        this.desc = desc;
        this.value = value;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameter#write(it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IParameterWriter)
     */
    public void write(IParameterWriter writer)
    {
        writer.write(desc, value);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameter#getParameterDesc()
     */
    public ParamDescription getParameterDesc()
    {
        return desc;
    }

    /**
     * @return the value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameter#isArray()
     */
    public boolean isArray()
    {
        return false;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameter#isComplex()
     */
    public boolean isComplex()
    {
        return false;
    }

    /**
     * @param value
     */
    public void setValue(String value)
    {
        this.value = value;
    }
}
