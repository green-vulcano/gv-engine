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

import it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ParamDescription;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPEnvelope;


/**
 * IOMParameterWriter class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface IOMParameterWriter extends IParameterWriter
{
    /**
     *
     */
    public static final String MY_ROOT_NODE_CONST = "MY_ROOT_NODE_CONST";
    /**
     *
     */
    public static final String MY_ROOT_NODE_START = "<" + MY_ROOT_NODE_CONST + ">";
    /**
     *
     */
    public static final String MY_ROOT_NODE_END   = "</" + MY_ROOT_NODE_CONST + ">";

    /**
     * @param desc
     * @param element
     */
    void writeOMNode(ParamDescription desc, OMNode element);

    /**
     * @return the element
     */
    OMElement getOMElement();

    /**
     * @return the SOAP envelope
     */
    SOAPEnvelope getOMEnvelope();
}
