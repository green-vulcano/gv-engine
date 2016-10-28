/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package tests.unit.vcl.axis2.ws;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Holder;

/**
 * @version 3.0.0 Mar 25, 2010
 * @author GreenVulcano Developer Team
 *
 */
@WebService(name = "GVAxis2TestWS", targetNamespace = "http://www.greenvulcano.com/gvesb/webservices")
public interface GVAxis2TestWS
{
    /**
     * @param o
     * @return the same string
     */
    @SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL)
    public String echo(@WebParam(name="o")String o);

    /**
     * @param o
     * @param data
     * @return the same string
     */
    @SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL)
    public String echoWithAttachment(@WebParam(name="o")String o, @WebParam(name="data", mode=WebParam.Mode.OUT)Holder<String> data);
}
