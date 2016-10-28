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

import java.util.Date;

import javax.jws.WebService;
import javax.xml.ws.Holder;

/**
 * @version 3.0.0 Mar 25, 2010
 * @author GreenVulcano Developer Team
 *
 */
@WebService(serviceName = "GVAxis2TestService", portName = "GVAxis2TestPort", endpointInterface = "tests.unit.vcl.axis2.ws.GVAxis2TestWS", targetNamespace = "http://www.greenvulcano.com/gvesb/webservices")
public class GVAxis2TestWSImpl implements GVAxis2TestWS
{

    /**
     * @param o
     * @return the same string
     */
    public String echo(String o){
        System.out.println(" >>>> echo method called <<<< ");
        return o;
    }

    /**
     * @param o
     * @param data
     * @return the same string
     */
    public String echoWithAttachment(String o, Holder<String> data) {
        System.out.println(" >>>> echo with attachment method called <<<< ");
        if (data == null || data.value == null) {
            System.out.println("NO ATTACHMENT");
        }
        else {
            System.out.println("Attachment processes succesfully {" + data.value + "} - " + new Date());
        }
        return o;
    }

}
