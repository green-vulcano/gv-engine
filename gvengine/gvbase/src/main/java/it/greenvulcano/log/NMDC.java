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
package it.greenvulcano.log;

import it.greenvulcano.util.thread.ThreadMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.MDC;



/**
 * That class is a merge of the functionalities of MDC (Mapped Diagnostic Context)
 * and NDC (Nested Diagnostic Context) of Log4J. <br/>
 * For every Thread the MDC context are saved and restored in a stack through the
 * methods {@link #push()} and{@link #pop()}.
 * Except for the methods {@link #push()} and {@link #pop()}, the <code>NMDC</code>
 * can be used as a normal MDC with the methods {@link #put(String, Object)},
 * {@link #get(String)} and {@link #remove(String)}. <br/>
 * At the first invokation of {@link #push()}, at the current Thread is associated
 * a unique identifier by key <code>THREADID</code>. Even the Thread's name and group
 * are insertd in the NMDC with the keys <code>THREADNAME</code> and <code>THREADGROUP</code>.
 * <p/>
 * Follows the usage pattern:
 *
 * <pre>
 * ...
 * NMDC.push();
 * NMDC.put(key1, value1);
 * NMDC.put(key2, value2);
 * ...
 * NMDC.put(keyJ, valueJ);
 * try {
 *     ...
 *     NMDC.put(keyN, valueN);
 *     ...
 * }
 * finally {
 *     NMDC.pop();
 * }
 * ...
 * </pre>
 *
 * The couple {@link #push()}/{@link #pop()} isn't mandatory on every method,
 * but only in the main components entry poin.
 * <p/>
 * After a call to {@link #push()} the context isn't empty but the properties set are
 * inherited from the previous status:
 * {@link #push()} mark a restore point.
 *
 * <p/>
 *
 * MDCContext class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 **/
public final class NMDC
{
    /**
     * ThreadMap's key referencing a <code>LinkedList[Map[String key, Object value]]</code>.
     * The list, used as a stack, hold the previous state of the NMDC.
     */
    public static final String       CONTEXT_KEY      = NMDC.class.getName();

    /**
     * Thread ID field name. <br/>
     * Value: <code>"THREADID"</code>
     */
    public static final String       THREAD_ID_KEY    = "THREADID";

    /**
     * Thread name field name. <br/>
     * Value: <code>"THREADNAME"</code>
     */
    public static final String       THREAD_NAME_KEY  = "THREADNAME";

    /**
     * Thread group field name. <br/>
     * Value: <code>"THREADGROUP"</code>
     */
    public static final String       THREAD_GROUP_KEY = "THREADGROUP";

    /**
     * Operation field name. <br/>
     * Value: <code>"OPERATION"</code>
     */
    public static final String       OPERATION_KEY    = "OPERATION";

    /**
     * SubSystem field name. <br/>
     * Value: <code>"SUBSYSTEM"</code>
     */
    public static final String       SUBSYSTEM_KEY    = "SUBSYSTEM";

    /**
     * Server field name. <br/>
     * Value: <code>"SERVER"</code>
     */
    public static final String       SERVER_KEY       = "SERVER";

    /**
     * Module field name. <br/>
     * Value: <code>"MODULE"</code>
     */
    public static final String       MODULE_KEY       = "MODULE";

    /**
     * Hold the general keys.
     */
    private static final Set<String> GENERAL_KEYS     = Collections.synchronizedSet(new HashSet<String>());

    static {
        GENERAL_KEYS.add(THREAD_ID_KEY);
        GENERAL_KEYS.add(THREAD_GROUP_KEY);
        GENERAL_KEYS.add(THREAD_NAME_KEY);
        GENERAL_KEYS.add(OPERATION_KEY);
        GENERAL_KEYS.add(SUBSYSTEM_KEY);
        GENERAL_KEYS.add(SERVER_KEY);
        GENERAL_KEYS.add(MODULE_KEY);
    }

    /**
     * Counter to set Thread ID.
     */
    private static int               threadCounter    = 0;

    private NMDC()
    {
        // do nothing
    }

    /**
     * Initialize a NMDC context.
     * The NMDC context must be closed through the method {@link #pop()}.
     * After a call to {@link #push()} the context isn't empty but the properties set are
     * inherited from the previous status:
     * {@link #push()} mark a restore point.
     */
    @SuppressWarnings("unchecked")
    public static void push()
    {
        LinkedList<Map<Object, Object>> stack = getStackContext();
        Map<Object, Object> previousContext = new HashMap<Object, Object>(MDC.getContext());
        stack.addLast(previousContext);
    }

    /**
     * Remove the NMDC context started with {@link #push()} and restore the NMDC
     * present at the call of the last {@link #push()}.
     */
    @SuppressWarnings("unchecked")
    public static void pop()
    {
        LinkedList<Map<Object, Object>> stack = getStackContext();
        if (stack.size() == 0) {
            ThreadMap.remove(CONTEXT_KEY);
            return;
        }
        Map<Object, Object> previousContext = stack.removeLast();
       
        MDC.getContext().clear();
        MDC.getContext().putAll(previousContext);
    }

    /**
     * Insert into the NMDC the value for the key.
     *
     * @param key NMDC key
     * @param obj value to bind
     */
    public static void put(String key, Object obj)
    {
        MDC.put(key, obj);
    }

    /**
     * Return the last value inserted for the given key.
     *
     * @param key
     * @return key associated value, or null if not bind.
     */
    public static Object get(String key)
    {
        return MDC.get(key);
    }

    /**
     * Remove from the NMDC the given key.
     *
     * @param key key to remove, or set at previous value.
     */
    public static void remove(String key)
    {
        if (GENERAL_KEYS.contains(key)) {
            return;
        }
        MDC.remove(key);
    }

    /**
     * Clear the current level NMDC keys. Don't remove the key related to Thread, server, operation nad subsystem.
     */
    @SuppressWarnings("unchecked")
    public static void clear()
    {
        MDC.getContext().keySet().retainAll(GENERAL_KEYS);
    }

    /**
     * Save the operation in to the NMDC.
     *
     * @param operation
     *        the operation name
     */
    public static void setOperation(String operation)
    {
        MDC.put(OPERATION_KEY, operation);
    }

    /**
     * Remove the operation from the NMDC.
     */
    public static void removeOperation()
    {
        MDC.remove(OPERATION_KEY);
    }

    /**
     * Save the subsystem in to the NMDC.
     *
     * @param subSystem
     *        the subsystem name
     */
    public static void setSubSystem(String subSystem)
    {
        MDC.put(SUBSYSTEM_KEY, subSystem);
    }

    /**
     * Remove the subsystem from the NMDC.
     */
    public static void removeSubSystem()
    {
        MDC.remove(SUBSYSTEM_KEY);
    }

    /**
     * Save the server name in to the NMDC.
     *
     * @param server
     *        the server name
     */
    public static void setServer(String server)
    {
        MDC.put(SERVER_KEY, server);
    }

    /**
     * Remove the server from the NMDC.
     */
    public static void removeServer()
    {
        MDC.remove(SERVER_KEY);
    }

    /**
     * Save the module name in to the NMDC.
     *
     * @param module
     *        the module name
     */
    public static void setModule(String module)
    {
        MDC.put(MODULE_KEY, module);
    }

    /**
     * Remove the module from the NMDC.
     */
    public static void removeModule()
    {
        MDC.remove(MODULE_KEY);
    }

    @SuppressWarnings("unchecked")
	public static Map<String, String> getCurrentContext()
    {
        return Collections.unmodifiableMap(MDC.getContext());
    }
    
    public static void setCurrentContext(Map<String, String> context)
    {
        Iterator<String> i = context.keySet().iterator();
        while (i.hasNext()) {
            String k = i.next();
            MDC.put(k, context.get(k));
        }
    }

    /**
     * The first invocation for the current Thread build the NMDC's context stack,
     * Next invocations use the created stack.
     *
     * @return the Thread's context stack.
     */
    @SuppressWarnings("unchecked")
    private static LinkedList<Map<Object, Object>> getStackContext()
    {
        LinkedList<Map<Object, Object>> stack = (LinkedList<Map<Object, Object>>) ThreadMap.get(CONTEXT_KEY);
        if (stack == null) {
            stack = new LinkedList<Map<Object, Object>>();
            ThreadMap.put(CONTEXT_KEY, stack);
            MDC.put(THREAD_ID_KEY, "" + (++threadCounter));
            MDC.put(THREAD_NAME_KEY, Thread.currentThread().getName());
            MDC.put(THREAD_GROUP_KEY, Thread.currentThread().getThreadGroup().getName());
        }
        return stack;
    }
}
