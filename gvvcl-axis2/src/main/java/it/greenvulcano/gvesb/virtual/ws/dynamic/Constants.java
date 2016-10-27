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
package it.greenvulcano.gvesb.virtual.ws.dynamic;

import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axis2.addressing.AddressingConstants;

/**
 *
 * Constants class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class Constants implements AddressingConstants, SOAP11Constants
{
    /**
     *
     */
    public static final String NS_URI_WS_ADDRESSING   = "http://schemas.xmlsoap.org/ws/2004/03/addressing";
    /**
     *
     */
    public static final String NS_URI_2001_SCHEMA_XSD = "http://www.w3.org/2001/XMLSchema";
    /**
     *
     */
    public static final String NS_URI_2000_SCHEMA_XSD = "http://www.w3.org/2000/10/XMLSchema";
    /**
     *
     */
    public static final String NS_URI_1999_SCHEMA_XSD = "http://www.w3.org/1999/XMLSchema";
    /**
     *
     */
    public static final String NS_URI_1999_SCHEMA_XSI = "http://www.w3.org/1999/XMLSchema-instance";
    /**
     *
     */
    public static final String LINE_SEPARATOR         = "==============================================================";
}