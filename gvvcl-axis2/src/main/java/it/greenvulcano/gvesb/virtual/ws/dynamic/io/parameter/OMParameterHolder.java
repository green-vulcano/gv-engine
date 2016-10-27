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

import it.greenvulcano.gvesb.virtual.ws.dynamic.descr.OperationDescription;
import it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IOMParameterWriter;
import it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.OMDocLiteralWriter;
import it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.OMRpcEncodedWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * OMParameterHolder class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class OMParameterHolder implements IParameterHolder
{
    private List<IParameter>     list = new ArrayList<IParameter>();
    private OperationDescription desc;

    /**
     * Creates a new instance of OMParameterHolder
     *
     * @param desc
     */
    public OMParameterHolder(OperationDescription desc)
    {
        this.desc = desc;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameterHolder#addParameter(it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameter)
     */
    public void addParameter(IParameter parameter)
    {
        list.add(parameter);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameterHolder#getParameters()
     */
    public Object getParameters()
    {
        IOMParameterWriter writer;

        if (desc.getStyle().startsWith("doc")) {
            writer = new OMDocLiteralWriter();
        }
        else {
            writer = new OMRpcEncodedWriter();
        }

        writer.initialize(desc);

        for (int i = 0; i < list.size(); i++) {
            IParameter parameter = (IParameter) list.get(i);
            parameter.write(writer);
        }

        return writer.getOMElement();
    }
}
