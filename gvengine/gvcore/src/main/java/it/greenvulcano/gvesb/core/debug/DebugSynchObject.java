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
package it.greenvulcano.gvesb.core.debug;

import it.greenvulcano.gvesb.core.debug.model.DebuggerEventObject;
import it.greenvulcano.gvesb.core.debug.model.DebuggerEventObject.Event;
import it.greenvulcano.gvesb.core.debug.model.DebuggerObject;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

/**
 * @version 3.3.0 Jan 23, 2013
 * @author GreenVulcano Developer Team
 */
public class DebugSynchObject
{
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(DebugSynchObject.class);

    private enum BreakpointType {
        PERSISTENT, TEMPORARY, REMOVED
    }

    private static Map<String, DebugSynchObject> synchObjects   = new ConcurrentHashMap<String, DebugSynchObject>();
    private static Map<String, DebugSynchObject> waitingObjects = new ConcurrentHashMap<String, DebugSynchObject>();

    public static boolean checkWaitingDebug(ExecutionInfo execInfo)
    {
        String key = execInfo.getUniqueKey();
        return waitingObjects.containsKey(key);
    }

    public static DebugSynchObject waitNew(ExecutionInfo execInfo)
    {
        DebugSynchObject dObj = null;
        String key = execInfo.getUniqueKey();
        synchronized (waitingObjects) {
            if (!waitingObjects.containsKey(key)) {
                waitingObjects.put(key, new DebugSynchObject());
            }
            dObj = waitingObjects.get(key);
        }
        synchronized (dObj) {
            try {
                dObj.wait(60000);
            }
            catch (InterruptedException e) {
                // do nothing
            }
        }
        return dObj;
    }

    public static DebugSynchObject createNew(String threadName, String flowId, ExecutionInfo execInfo)
            throws DebuggerException
    {
        DebugSynchObject dObj = null;
        String execKey = execInfo.getUniqueKey();
        if (waitingObjects.containsKey(execKey)) {
            dObj = waitingObjects.get(execKey);

            synchronized (waitingObjects) {
                waitingObjects.remove(execKey);
            }
        }

        String key = getUniqueKey(flowId);
        if (synchObjects.containsKey(key)) {
            throw new DebuggerException("Already created synchronization object for debugging");
        }
        if (dObj == null) {
            dObj = new DebugSynchObject(threadName, flowId);
        }
        else {
            dObj.setThreadName(threadName);
            dObj.setFlowId(flowId);
            dObj.sendDebugEvent(Event.STARTED);
        }
        dObj.setExecutionInfo(execInfo);
        synchObjects.put(key, dObj);

        synchronized (dObj) {
            dObj.notifyAll();
        }
        return dObj;
    }

    private static String getUniqueKey(String flowId)
    {
        return new StringBuilder().append(flowId).toString();
    }

    public synchronized static DebugSynchObject getSynchObject(String flowId, ExecutionInfo execInfo)
    {
        DebugSynchObject obj = synchObjects.get(getUniqueKey(flowId));
        if (obj != null && execInfo != null) {
            obj.setExecutionInfo(execInfo);
        }
        return obj;
    }

    private String                      threadName;

    private String                      flowId;

    private Deque<ExecutionInfo>        execInfoStack;

    private boolean                     mustStop;

    /**
     * The next flow node before which suspend execution of the currently
     * debugging associated flows.
     */
    private Map<String, BreakpointType> debugFlowsBreakpoints = new ConcurrentHashMap<String, BreakpointType>();
    /**
     * The list of ID/Thread in debug mode.
     */
    private Set<String>                 onDebugThreads        = new LinkedHashSet<String>();

    private boolean                     debugInto;

    private boolean                     stepReturn;

    private Deque<DebuggerObject>       events;
    private boolean                     skipAllBreakpoints;

    private DebugSynchObject()
    {
        this.mustStop = true;
        execInfoStack = new ArrayDeque<ExecutionInfo>();
        events = new LinkedList<DebuggerObject>();
        skipAllBreakpoints = false;
    }

    private DebugSynchObject(String threadName, String flowId)
    {
        this();
        setFlowId(flowId);
        setThreadName(threadName);
        sendDebugEvent(Event.STARTED);
    }

    private void setFlowId(String flowId)
    {
        this.flowId = flowId;
    }

    private void setThreadName(String threadName)
    {
        this.threadName = threadName;
        onDebugThreads.add(threadName);
    }

    /**
     * Return true if the flow flowId must suspend execution on node nodeId.
     * 
     * @param threadName
     * @param flowId
     * @param subflow
     *        the subflow name or null if main flow
     * @param nodeId
     * @return
     */
    public boolean mustStop()
    {
        ExecutionInfo execInfo = execInfoStack.peek();
        boolean res = mustStop;
        if (!res && execInfo != null) {
            synchronized (debugFlowsBreakpoints) {
                BreakpointType bt = debugFlowsBreakpoints.get(execInfo.toString());
                if (bt != null) {
                    switch (bt) {
                        case PERSISTENT :
                            if (!skipAllBreakpoints) {
                                res = true;
                                sendDebugEvent(Event.SUSPENDED, "breakpoint", execInfo.getNodeId());
                            }
                            break;
                        case TEMPORARY :
                            debugFlowsBreakpoints.put(execInfo.toString(), BreakpointType.REMOVED);
                            res = true;
                            break;
                        default :
                            res = false;
                    }
                    mustStop = res;
                }
            }
        }
        logger.debug("MUST STOP: " + threadName + "-" + flowId + "-" + execInfo + ": " + res);
        return res;
    }

    /**
     * Execute the flowId's flow next inner node.
     * 
     * @param threadName
     * @param subflow
     *        the subflow name or null if main flow
     * @param flowId
     */
    public void stepInto() throws DebuggerException
    {
        sendDebugEvent(Event.RESUMED, "step", "into");
        this.debugInto = true;
        synchronized (this) {
            this.notifyAll();
            try {
                this.wait();
            }
            catch (InterruptedException e) {
                throw new DebuggerException(e);
            }

        }
        this.debugInto = false;
        sendDebugEvent(Event.SUSPENDED, "step", "into");
    }

    /**
     * Execute the flowId's flow next inner node.
     * 
     * @param threadName
     * @param subflow
     *        the subflow name or null if main flow
     * @param flowId
     */
    public void stepReturn() throws DebuggerException
    {
        sendDebugEvent(Event.RESUMED, "step", "return");
        stepReturn = true;
        synchronized (this) {
            mustStop = false;
            this.notifyAll();
            try {
                this.wait();
            }
            catch (InterruptedException e) {
                throw new DebuggerException(e);
            }

        }
        stepReturn = false;
        sendDebugEvent(Event.SUSPENDED, "step", "return");
    }

    public boolean isDebugInto()
    {
        return debugInto;
    }

    /**
     * Execute the flowId's flow next node.
     * 
     * @param threadName
     * @param subflow
     *        the subflow name or null if main flow
     * @param flowId
     * @throws InterruptedException
     */
    public void stepOver() throws DebuggerException
    {
        sendDebugEvent(Event.RESUMED, "step", "over");
        synchronized (this) {
            mustStop = true;
            this.notifyAll();
            try {
                this.wait();
            }
            catch (InterruptedException e) {
                throw new DebuggerException(e);
            }
        }
        sendDebugEvent(Event.SUSPENDED, "step", "over");
    }

    /**
     * Resume normal execution of the flowId's flow.
     * 
     * @param threadName
     * @param flowId
     */
    public void resume() throws DebuggerException
    {
        synchronized (this) {
            mustStop = false;
            this.notifyAll();
            try {
                this.wait();
            }
            catch (InterruptedException e) {
                throw new DebuggerException(e);
            }
        }
    }

    /**
     * Sets a breakpoint on node nodeId.
     * 
     * @param threadName
     * @param flowId
     * @param subflow
     *        the subflow name or null if main flow
     * @param execInfo
     */
    public void setBreakpoint(ExecutionInfo execInfo)
    {
        debugFlowsBreakpoints.put(execInfo.toString(), BreakpointType.PERSISTENT);
    }

    /**
     * Clears a breakpoint on node nodeId.
     * 
     * @param threadName
     * @param flowId
     * @param nodeId
     */
    public void clearBreakpoint(ExecutionInfo execInfo)
    {
        debugFlowsBreakpoints.put(execInfo.toString(), BreakpointType.REMOVED);
    }

    /**
     * Resume normal execution of the flowId's flow, till node nodeId, then
     * suspend execution.
     * 
     * @param threadName
     * @param flowId
     * @param subflow
     *        the subflow name or null if main flow
     * @param nodeId
     */
    public void resumeTo(ExecutionInfo execInfo)
    {
        BreakpointType breakpointType = debugFlowsBreakpoints.get(execInfo.toString());
        if (breakpointType != BreakpointType.PERSISTENT) {
            synchronized (debugFlowsBreakpoints) {
                debugFlowsBreakpoints.put(execInfo.toString(), BreakpointType.TEMPORARY);
            }
        }
        synchronized (this) {
            this.notifyAll();
        }
    }

    /**
     * Get the list of on debug flow's ID and threads.
     */
    public Set<String> getOnDebugThreads()
    {
        return Collections.unmodifiableSet(onDebugThreads);
    }

    public void setExecutionInfo(ExecutionInfo execInfo)
    {
        ExecutionInfo current = execInfoStack.peek();
        if (current == null || (!current.getUniqueKey().equals(execInfo.getUniqueKey()))) {
            execInfoStack.push(execInfo);
        }
    }

    public ExecutionInfo getExecutionInfo()
    {
        return execInfoStack.peek();
    }

    public Deque<ExecutionInfo> getExecutionInfoStack()
    {
        return execInfoStack;
    }

    public void terminated()
    {
        if (!execInfoStack.isEmpty()) {
            execInfoStack.pop();
        }
        if (stepReturn) {
            mustStop = true;
        }
        if (execInfoStack.isEmpty()) {
            synchronized (synchObjects) {
                synchObjects.remove(flowId);
            }
            sendDebugEvent(Event.TERMINATED);
        }
        synchronized (this) {
            this.notifyAll();
        }
    }

    private void sendDebugEvent(Event event, String... props)
    {
        DebuggerObject dObj = new DebuggerEventObject(event, props);
        events.add(dObj);
    }

    public void stop() throws DebuggerException
    {
        synchronized (debugFlowsBreakpoints) {
            debugFlowsBreakpoints.clear();
        }
        resume();
        execInfoStack.clear();
    }

    public DebuggerObject event()
    {
        return events.isEmpty() ? DebuggerEventObject.NO_EVENT : events.poll();
    }

    public String getFlowId()
    {
        return flowId;
    }

    public void skipAllBreakpoints(boolean enabled)
    {
        skipAllBreakpoints = enabled;
    }
}
