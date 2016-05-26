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
package it.greenvulcano.gvesb.security;

import java.util.LinkedList;

import it.greenvulcano.gvesb.security.callers.AnonymousCaller;
import it.greenvulcano.gvesb.security.callers.Caller;
import it.greenvulcano.util.thread.ThreadMap;


/**
 * SecurityContext class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 **/
public class SecurityContext
{
    /**
     * The KEY in thread map.
     */
    private static final String    THREAD_SEC_MAP_KEY = "SecurityContext_Key";

    /**
     * Unique instance.
     */
    private static SecurityContext instance           = null;

    /**
     * The anonymous caller.
     */
    public static final Caller     ANONYMOUS_CALLER   = new AnonymousCaller();

    /**
     * Default constructor.
     */
    private SecurityContext()
    {
        // do nothing
    }

    /**
     * Thread safe.
     *
     * @return the unique instance of the class.
     */
    public static synchronized SecurityContext instance()
    {
        if (instance == null) {
            instance = new SecurityContext();
        }
        return instance;
    }

    /**
     * It inserts the Caller on the top of the stack of the current thread.
     *
     * @param caller
     */
    public final synchronized void push(Caller caller)
    {
        getSecurityContextStack().addFirst(caller);
    }

    /**
     * It remove the Caller from the top of the stack of the current thread.
     */
    public final synchronized void pop()
    {
        LinkedList<Caller> icStack = getSecurityContextStack();
        if (!icStack.isEmpty()) {
            icStack.removeFirst();
        }
    }

    /**
     * @return the actual caller
     */
    public Caller peek()
    {
        return getActualCaller();
    }

    /**
     * It returns the name of the first caller in the stack. If the stack is
     * empty it returns "anonymous".
     *
     * @return the caller name
     */
    public String getCallerName()
    {
        return getActualCaller().getCallerName();
    }

    /**
     * It returns true if the caller is in role otherwise false. If the stack is
     * empty it returns false.
     *
     * @param role
     *        the role to match
     * @return boolean
     */
    public boolean isCallerInRole(String role)
    {
        return getActualCaller().isCallerInRole(role);
    }

    /**
     * It returns true if the caller is secure otherwise false. If the stack is
     * empty it returns false.
     *
     * @return boolean
     */
    public boolean isSecure()
    {
        return getActualCaller().isSecure();
    }

    /**
     * @param acl
     * @return if actual caller is allowed to access the resource
     */
    public boolean isAllowed(ACL acl)
    {
        return acl.isAllowed(getActualCaller());
    }

    /**
     * It release the resources.
     */
    public void destroy()
    {
        instance = null;
    }

    /**
     * @return string that represent the object.
     */
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        return buffer.toString();
    }

    /**
     * @return security context stack LinkedList
     */
    @SuppressWarnings("unchecked")
    private static LinkedList<Caller> getSecurityContextStack()
    {
        LinkedList<Caller> icStack = (LinkedList<Caller>) ThreadMap.get(THREAD_SEC_MAP_KEY);
        if (icStack == null) {
            icStack = new LinkedList<Caller>();
            ThreadMap.put(THREAD_SEC_MAP_KEY, icStack);
        }
        return icStack;
    }

    /**
     * @return the first caller in the stack.
     */
    private Caller getActualCaller()
    {
        Caller caller = null;
        LinkedList<Caller> securityContextList = getSecurityContextStack();
        if (!securityContextList.isEmpty()) {
            caller = getSecurityContextStack().getFirst();
        }
        else {
            return ANONYMOUS_CALLER;
        }
        return caller;
    }
}
