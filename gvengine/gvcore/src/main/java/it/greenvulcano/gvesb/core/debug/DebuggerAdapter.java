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
package it.greenvulcano.gvesb.core.debug;

import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.debug.model.DebuggerEventObject;
import it.greenvulcano.gvesb.core.debug.model.DebuggerObject;
import it.greenvulcano.gvesb.core.debug.model.Service;
import it.greenvulcano.gvesb.core.debug.model.ThreadInfo;

import java.util.Set;

/**
 * @version 3.3.0 Dic 14, 2012
 * @author GreenVulcano Developer Team
 */
public class DebuggerAdapter
{
    private Service             debugService;
    private DebugSynchObject    synchObject;

    // TODO: make configurable
    public static final Version DEBUGGER_VERSION = new Version("4.0.0");

    public DebuggerAdapter() throws DebuggerException
    {
    }

    public DebuggerObject start() throws DebuggerException
    {
        try {
            ExecutionInfo execInfo = new ExecutionInfo(debugService.getServiceName(), debugService.getOperationName(),
                    null, null, null);
            synchObject = DebugSynchObject.waitNew(execInfo);
            String id = synchObject.getFlowId();
            debugService.setId(id);
            Set<String> set = synchObject.getOnDebugThreads();
            if (set != null && set.size() > 0) {
                debugService.loadInfo();

                return debugService;
            }
        }
        catch (Exception exc) {
            throw new DebuggerException(exc);
        }
        return null;
    }

    public DebuggerObject stack(String threadName) throws DebuggerException
    {
        checkNotEmpty(threadName);
        ThreadInfo thread = debugService.getThread(threadName);
        thread.loadInfo(synchObject);
        return thread.getStackFrame();
    }

    public DebuggerObject var(String threadName, String stackFrame, String varEnv, String varID)
            throws DebuggerException
    {
        checkNotEmpty(threadName, stackFrame, varEnv, varID);
        ThreadInfo thread = debugService.getThread(threadName);
        thread.loadInfo(synchObject);
        return thread.getStackFrame().getVar(stackFrame, varEnv, varID);
    }

    public DebuggerObject set_var(String threadName, String stackFrame, String varEnv, String varID, String varValue)
            throws DebuggerException
    {
        checkNotEmpty(threadName, stackFrame, varEnv, varID);
        ThreadInfo thread = debugService.getThread(threadName);
        thread.loadInfo(synchObject);
        try {
            thread.getStackFrame().setVar(stackFrame, varEnv, varID, varValue);
        }
        catch (GVException e) {
            throw new DebuggerException(e);
        }
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject set(String threadName, String subflow, String nodeId) throws DebuggerException
    {
        checkNotEmpty(threadName, nodeId);
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        ExecutionInfo bp = new ExecutionInfo(synchObject.getExecutionInfo());
        bp.setNodeId(nodeId);
        bp.setSubflow(subflow);
        synchObject.setBreakpoint(bp);
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject data()
    {
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject clear(String threadName, String subflow, String nodeId) throws DebuggerException
    {
        checkNotEmpty(threadName, nodeId);
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        ExecutionInfo bp = new ExecutionInfo(synchObject.getExecutionInfo());
        bp.setNodeId(nodeId);
        bp.setSubflow(subflow);
        synchObject.clearBreakpoint(bp);
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject stepOver(String threadName) throws DebuggerException
    {
        checkNotEmpty(threadName);
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        synchObject.stepOver();
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject stepInto(String threadName) throws DebuggerException
    {
        checkNotEmpty(threadName);
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        synchObject.stepInto();
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject stepReturn(String threadName) throws DebuggerException
    {
        checkNotEmpty(threadName);
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        synchObject.stepReturn();
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject resume(String threadName) throws DebuggerException
    {
        checkNotEmpty(threadName);
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        synchObject.resume();
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject skipAllBreakpoints(boolean enabled)
    {
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        synchObject.skipAllBreakpoints(enabled);
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject exit() throws DebuggerException
    {
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        if (synchObject != null) {
            synchObject.stop();
        }
        return DebuggerObject.OK_DEBUGGER_OBJECT;
    }

    public DebuggerObject connect(String version, String service, String operation) throws DebuggerException
    {
        checkNotEmpty(version, service, operation);
        checkVersion(version);
        debugService = new Service(service, operation);
        return debugService;
    }

    public DebuggerObject event()
    {
        DebugSynchObject synchObject = DebugSynchObject.getSynchObject(debugService.getId(), null);
        return ((DebuggerObject) (synchObject == null
                ? new DebuggerEventObject(DebuggerEventObject.Event.TERMINATED)
                : synchObject.event()));
    }

    private void checkNotEmpty(String... args) throws DebuggerException
    {
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null || args[i].length() == 0) {
                throw new DebuggerException("Input parameter at position " + (i + 1) + " is empty");
            }
        }
    }

    private void checkVersion(String version) throws DebuggerException
    {
        Version v = new Version(version);
        if (DEBUGGER_VERSION.compareTo(v) < 0) {
            throw new DebuggerException("GreenVulcano ESB debugger version is less than client debugger version. GV: "
                    + DEBUGGER_VERSION + " CLIENT: " + version);
        }
    }
}
