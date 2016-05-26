/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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

import it.greenvulcano.gvesb.core.flow.GVFlowNodeIF;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * DebuggingInvocationHandler class.
 * 
 * @version 3.3.0 Jan 24, 2013
 * @author GreenVulcano Developer Team
 * 
 */
public class DebuggingInvocationHandler<T extends GVFlowNodeIF> implements InvocationHandler
{
    @SuppressWarnings("unchecked")
    public static <T> T getProxy(Class<T> intf, final GVFlowNodeIF obj, DebugSynchObject synchObj)
    {
        return (T) Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[]{intf},
                new DebuggingInvocationHandler<GVFlowNodeIF>(obj, synchObj));
    }

    private final GVFlowNodeIF flowNode;
    private DebugSynchObject   synchObj;

    public DebuggingInvocationHandler(T obj, DebugSynchObject synchObj)
    {
        this.flowNode = obj;
        this.synchObj = synchObj;
    }

    /**
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
     *      java.lang.reflect.Method, java.lang.Object[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        String methodName = method.getName();
        if (methodName.equals("execute")) {

            ExecutionInfo execInfo = synchObj.getExecutionInfo();
            if (execInfo != null) {
                execInfo.setEnvironment((Map<String, Object>) args[0]);
                execInfo.setNodeId(flowNode.getId());
            }
            if (synchObj.mustStop()) {
                synchronized (synchObj) {
                    synchObj.notifyAll();
                    synchObj.wait();
                    if (args.length > 1 && args[1] == Boolean.TRUE) {
                        args[1] = synchObj.isDebugInto();
                    }
                }
            }
        }
        return method.invoke(flowNode, args);
    }

}
