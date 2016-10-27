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

import java.util.ArrayList;
import java.util.List;


/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ArrayParameter implements IParameter, IParameterHolder
{
    private List<IParameter> list = new ArrayList<IParameter>();

    /**
     * Creates a new instance of ArrayParameter
     */
    public ArrayParameter()
    {
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameter#write(it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IParameterWriter)
     */
    public void write(IParameterWriter writer)
    {
        writer.writeArrayStart(getParameterDesc(), list.size());

        for (IParameter parameter : list) {
            parameter.write(writer);
        }

        writer.writeArrayEnd();
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameterHolder#addParameter(it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameter)
     */
    public void addParameter(IParameter parameter)
    {
        list.add(parameter);
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
        return true;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameter#getParameterDesc()
     */
    public ParamDescription getParameterDesc()
    {
        ParamDescription p = null;

        if (list.size() > 0) {
            p = list.get(0).getParameterDesc();
        }

        return p;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameterHolder#getParameters()
     */
    public Object getParameters()
    {
        return list;
    }
}
