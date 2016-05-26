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

import it.greenvulcano.gvesb.core.debug.DebugSynchObject;
import it.greenvulcano.gvesb.core.debug.DebuggerException;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.3.0 Dic 14, 2012
 * @author GreenVulcano Developer Team
 */
public class ThreadList extends DebuggerObject
{
    /**
     * 
     */
    private static final long       serialVersionUID = 1L;
    private Map<String, ThreadInfo> threads;
    private String                  serviceName;

    public ThreadList(String serviceName)
    {
        threads = new HashMap<String, ThreadInfo>();
        this.serviceName = serviceName;
    }

    public void loadInfo(String id) throws DebuggerException
    {
        try {
            DebugSynchObject synchObject = DebugSynchObject.getSynchObject(id, null);
            
            Set<String> threadSet = synchObject.getOnDebugThreads();
            if (threadSet != null) {
                for (String tName : threadSet) {
                    threads.put(tName, new ThreadInfo(tName, serviceName));
                }
            }
        }
        catch (Exception e) {
            throw new DebuggerException(e);
        }
    }


    /**
     * @see it.greenvulcano.gvesb.core.debug.model.DebuggerObject#getXML(it.greenvulcano.util.xml.XMLUtils,
     *      org.w3c.dom.Document)
     */
    @Override
    protected Node getXML(XMLUtils xml, Document doc) throws XMLUtilsException
    {
        Element thds = xml.createElement(doc, "Threads");
        for (Entry<String, ThreadInfo> stack : threads.entrySet()) {
            thds.appendChild(stack.getValue().getXML(xml, doc));
        }
        return thds;
    }

    public ThreadInfo getThread(String threadName)
    {
        return threads.get(threadName);
    }

}
