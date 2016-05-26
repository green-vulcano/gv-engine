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
package it.greenvulcano.util.xpath.search.jaxen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

/**
 * This class implements the current() XPath function. This function is not
 * provided by Jaxen itself. The starting context is put into a Map and
 * associate with the Thread executing the XPath.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author gianluca
 *
 *
 *
 */
public class CurrentFunction implements Function
{
    private static Map<Thread, Stack<Object>> currentMap = new HashMap<Thread, Stack<Object>>();

    /**
     * Associate to the current Thread the context object.
     *
     * @param object
     */
    public static synchronized void putCurrent(Object object)
    {
        Thread thread = Thread.currentThread();
        Stack<Object> stack = currentMap.get(thread);
        if (stack == null) {
            stack = new Stack<Object>();
            currentMap.put(thread, stack);
        }
        stack.push(object);
    }

    /**
     * @return the context object for the current Thread.
     */
    public static synchronized Object getCurrent()
    {
        Thread thread = Thread.currentThread();
        Stack<Object> stack = currentMap.get(thread);
        if (stack == null) {
            return null;
        }
        return stack.peek();
    }

    /**
     * Dissociates the current Thread from the context object.
     */
    public static synchronized void removeCurrent()
    {
        Thread thread = Thread.currentThread();
        Stack<Object> stack = currentMap.get(thread);
        if (stack != null) {
            stack.pop();
            if (stack.empty()) {
                currentMap.remove(thread);
            }
        }
    }

    /**
     * @see org.jaxen.Function#call(org.jaxen.Context, java.util.List)
     */
    @SuppressWarnings("rawtypes")
	public Object call(Context context, List args) throws FunctionCallException
    {
        return getCurrent();
    }
}
