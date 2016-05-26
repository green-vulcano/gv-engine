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

import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.debug.DebugSynchObject;
import it.greenvulcano.gvesb.core.debug.DebuggerException;
import it.greenvulcano.gvesb.core.debug.ExecutionInfo;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.3.0 Dic 14, 2012
 * @author GreenVulcano Developer Team
 */
public class FrameStack extends DebuggerObject
{

    /**
     * 
     */
    private static final long  serialVersionUID = 1L;
    private Map<String, Frame> frames;
    private Frame              currentFrame;

    public FrameStack()
    {
        frames = new LinkedHashMap<String, Frame>();
    }

    public Frame getCurrentFrame()
    {
        return currentFrame;
    }

    @Override
    protected Node getXML(XMLUtils xml, Document doc) throws XMLUtilsException
    {
        Element frameStack = xml.createElement(doc, "FrameStack");
        if (!frames.isEmpty()) {
            for (Frame f : frames.values()) {
                frameStack.appendChild(f.getXML(xml, doc));
            }
        }
        return frameStack;
    }

    public Variable getVar(String stackFrame, String varEnv, String varID) throws DebuggerException
    {
        Frame f = frames.get(stackFrame);
        if (f == null) {
            return currentFrame.getVar(varEnv, varID);
        }
        return f.getVar(varEnv, varID);
    }

    public void setVar(String stackFrame, String varEnv, String varID, String varValue) throws GVException
    {
        Frame f = frames.get(stackFrame);
        if (f == null) {
            currentFrame.setVar(varEnv, varID, varValue);
        }
        f.setVar(varEnv, varID, varValue);
    }

    public void loadInfo(DebugSynchObject synchObject)
    {
        frames.clear();
        Deque<ExecutionInfo> executionInfoStack = synchObject.getExecutionInfoStack();
        if (executionInfoStack != null && executionInfoStack.size() > 0) {
            Iterator<ExecutionInfo> iterator = executionInfoStack.descendingIterator();
            while (iterator.hasNext()) {
                currentFrame = new Frame(iterator.next());
                frames.put(currentFrame.getFrameName(), currentFrame);
            }
        }
    }
}
