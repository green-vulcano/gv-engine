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
package it.greenvulcano.gvesb.virtual.ws.dynamic.descr;

import it.greenvulcano.gvesb.virtual.ws.dynamic.Constants;

import java.util.Collection;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ServiceDescription implements Description<OperationDescription>
{
    private static final long                 serialVersionUID = 210L;
    private QName                             serviceQN;
    private String                            address;
    private Map<String, OperationDescription> operations;
    private QName                             portTypeQN;

    /**
     *
     */
    public ServiceDescription()
    {
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.descr.Description#getXmlName()
     */
    public QName getXmlName()
    {
        return serviceQN;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.descr.Description#setXmlName(javax.xml.namespace.QName)
     */
    public void setXmlName(QName serviceName)
    {
        this.serviceQN = serviceName;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.descr.Description#getDescriptions()
     */
    public Map<String, OperationDescription> getDescriptions()
    {
        return operations;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.descr.Description#setDescriptions(java.util.Map)
     */
    public void setDescriptions(Map<String, OperationDescription> operations)
    {
        this.operations = operations;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String separator = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();

        sb.append("$SERVICE: ").append(separator);
        sb.append("ServiceName: ").append(serviceQN).append(separator);
        sb.append("PortTypeName: ").append(portTypeQN).append(separator);
        sb.append("ServiceAddress: ").append(address);
        sb.append(separator).append(Constants.LINE_SEPARATOR);

        Map<String, OperationDescription> descriptions = getDescriptions();
        Collection<OperationDescription> values = descriptions.values();
        for (OperationDescription operationDescription : values) {
            if (operationDescription != null) {
                sb.append(separator).append(operationDescription.toString());
            }
        }

        return sb.toString();
    }

    /**
     * @return the address
     */
    public String getAddress()
    {
        return address;
    }

    /**
     * @param address
     */
    public void setAddress(String address)
    {
        this.address = address;
    }

    /**
     * @return the port type XML name
     */
    public QName getPortTypeXmlName()
    {
        return portTypeQN;
    }

    /**
     * @param portTypeQN
     */
    public void setPortTypeXmlName(QName portTypeQN)
    {
        this.portTypeQN = portTypeQN;
    }
}
