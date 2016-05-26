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
package it.greenvulcano.gvesb.core.debug.model;

import it.greenvulcano.gvesb.core.debug.DebuggerException;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.3.0 Dic 14, 2012
 * @author GreenVulcano Developer Team
 */
public class Service extends DebuggerObject
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String     serviceName;
    private String     operationName;
    private String     id;
    private ThreadList threads;

    public Service(String serviceName, String operationName)
    {
        this.serviceName = serviceName;
        this.operationName = operationName;
        threads = new ThreadList(serviceName);
    }

    @Override
    protected Node getXML(XMLUtils xml, Document doc) throws XMLUtilsException
    {
        Element service = xml.createElement(doc, "Service");
        service.setAttribute("service", serviceName);
        service.setAttribute("operation", operationName);
        service.setAttribute("id", id);
        service.appendChild(threads.getXML(xml, doc));
        return service;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }

    public String getOperationName()
    {
        return operationName;
    }

    public void setOperationName(String operationName)
    {
        this.operationName = operationName;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void loadInfo() throws DebuggerException
    {
        try {
            threads = new ThreadList(serviceName);
            threads.loadInfo(getId());
        }
        catch (Exception e) {
            throw new DebuggerException(e);
        }
    }

    public ThreadInfo getThread(String threadName)
    {
        return threads.getThread(threadName);
    }

}
