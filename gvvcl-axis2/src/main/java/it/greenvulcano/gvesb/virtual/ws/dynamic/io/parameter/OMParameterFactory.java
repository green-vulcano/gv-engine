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

import org.apache.axiom.om.OMNode;


/**
 * OMParameterFactory class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class OMParameterFactory implements IOMParameterFactory
{

    /**
     * Creates a new instance of OMParameterFactory
     */
    private OMParameterFactory()
    {
    }

    /**
     * @return a new OMParameterFactory instance
     */
    public static OMParameterFactory newInstance()
    {
        return new OMParameterFactory();
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameterFactory#createTextParameter(java.lang.String)
     */
    public TextParameter createTextParameter(String value)
    {
        return new TextParameter(value);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameterFactory#createSimpleParameter(it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ParamDescription,
     *      java.lang.String)
     */
    public SimpleParameter createSimpleParameter(ParamDescription desc, String value)
    {
        return new SimpleParameter(desc, value);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IOMParameterFactory#createOMParameter(org.apache.axiom.om.OMNode)
     */
    public OMParameter createOMParameter(OMNode omNode)
    {
        return new OMParameter(omNode);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameterFactory#createTextParameter(java.lang.String,
     *      int)
     */
    public TextParameter createTextParameter(String value, int type)
    {
        return new TextParameter(value, type);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameterFactory#createSimpleParameter(it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ParamDescription)
     */
    public SimpleParameter createSimpleParameter(ParamDescription desc)
    {
        return new SimpleParameter(desc);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameterFactory#createComplexParameter(it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ParamDescription)
     */
    public ComplexParameter createComplexParameter(ParamDescription desc)
    {
        return new ComplexParameter(desc);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameterFactory#createArrayParameter()
     */
    public ArrayParameter createArrayParameter()
    {
        return new ArrayParameter();
    }
}
