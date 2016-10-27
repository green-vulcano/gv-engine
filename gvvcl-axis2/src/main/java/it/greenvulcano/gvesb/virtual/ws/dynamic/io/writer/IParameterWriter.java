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
package it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer;

import it.greenvulcano.gvesb.virtual.ws.dynamic.descr.OperationDescription;
import it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ParamDescription;


/**
 * IParameterWriter interface
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface IParameterWriter
{

    /**
     * Initialize the <code>IParemterWriter</code> Implementation for the given
     * <code>OperationDescr</code>
     *
     * @param desc
     */
    void initialize(OperationDescription desc);

    /**
     * @param desc
     * @param value
     */
    void write(ParamDescription desc, String value);

    /**
     * @param text
     * @param type
     */
    void writeText(String text, int type);

    /**
     * @param desc
     * @param arraySize
     */
    void writeArrayStart(ParamDescription desc, int arraySize);

    /**
     *
     */
    void writeArrayEnd();

    /**
     * @param desc
     */
    void writeComplexStart(ParamDescription desc);

    /**
     *
     */
    void writeComplexEnd();
}
