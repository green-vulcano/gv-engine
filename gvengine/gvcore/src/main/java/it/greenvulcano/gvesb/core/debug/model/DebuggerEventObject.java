/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project.
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
 */
package it.greenvulcano.gvesb.core.debug.model;

import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.3.0 Dic 14, 2012
 * @author GreenVulcano Developer Team
 */
public class DebuggerEventObject extends DebuggerObject
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public enum Event {
        STARTED, SUSPENDED, RESUMED, TERMINATED, NONE
    }

    public static final DebuggerEventObject NO_EVENT = new DebuggerEventObject(Event.NONE);

    private Event                     event;
    private String[]                  props;

    public DebuggerEventObject(Event event, String... props)
    {
        this.event = event;
        this.props = props;
    }

    protected Node getXML(XMLUtils xml, Document doc) throws XMLUtilsException
    {
        Element eventEl = doc.createElement("Event");
        eventEl.setAttribute(NAME_ATTR, event.toString());
        if (props != null) {
            for (String prop : props) {
                xml.insertElement(eventEl, "Property").setTextContent(prop);
            }
        }
        return eventEl;
    }
}