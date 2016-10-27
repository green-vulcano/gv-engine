/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.ws.wsdl;

import it.greenvulcano.configuration.XMLConfig;

import org.w3c.dom.Node;

/**
 * WebService Module class.
 * 
 * @version 3.2.0 Feb 10, 2012
 * @author GreenVulcano Developer Team
 */
public class Module
{
    private String name          = null;
    private String specification = null;
    private String policyData    = null;

    public Module(Node node) throws Exception
    {
        name = XMLConfig.get(node, "@name");
        specification = XMLConfig.get(node, "@specification");
        policyData = XMLConfig.get(node, "PolicyData", null);
    }

    public String getName()
    {
        return this.name;
    }

    public String getSpecification()
    {
        return this.specification;
    }

    public String getPolicyData()
    {
        return this.policyData;
    }
}
