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
package it.greenvulcano.gvesb.internal;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.util.thread.ThreadMap;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * It allows to go isolate to the context of execution of a given subsystem, the
 * managed information depends on the particular subsystem.
 *
 * @version 3.0.0 Mar 4, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class InvocationContext
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(InvocationContext.class);

    /**
     * The KEY in thread map.
     */
    private static final String     THREAD_CTX_MAP_KEY = "InvocationContext_Key";

    /**
     * The subsystem
     */
    protected String                subSystem          = "";

    /**
     * The system
     */
    private String                  system;

    /**
     * The service
     */
    private String                  service;

    /**
     * The operation
     */
    private String                  operation;

    /**
     * The Id
     */
    private Id                      id;

    /**
     * The subOperation
     */
    private String                  subOperation       = "";

    /**
     * User-defined extra Context fields.
     */
    private HashMap<String, Object> extraFields        = new HashMap<String, Object>();

    /**
     * Default constructor.
     */
    public InvocationContext()
    {
        // do nothing
    }

    /**
     * It inserts the request of InvocationContext in associated top to the
     * stack of the current thread.
     */
    public final void push()
    {
        LinkedList<InvocationContext> icStack = getInvocationContextStack();
        icStack.addFirst(this);
        //System.out.println("InvocationContext TH[" + Thread.currentThread().getId() + "] push() size: " + icStack.size());
    }

    /**
     * It remove the InvocationContext from the top to the stack associated to
     * the current thread.
     */
    public final void pop()
    {
        LinkedList<InvocationContext> icStack = getInvocationContextStack();
        if (!icStack.isEmpty()) {
            @SuppressWarnings("unused")
			InvocationContext ctx = icStack.removeFirst();
            /*
             * try { ctx.destroy(); } catch (Exception exc) {
             * exc.printStackTrace(); }
             */
        }
        //System.out.println("InvocationContext TH[" + Thread.currentThread().getId() + "] pop() size: " + icStack.size());
        if (icStack.isEmpty()) {
            ThreadMap.remove(THREAD_CTX_MAP_KEY);
        }
    }

    /**
     * It returns the InvocationContext instance from the top to the stack,
     * leaving the stack unchanged.
     *
     * @return InvocationContext
     * @throws GVInternalException
     *         if the invocation context not found in the ThreadMap
     */
    public static final InvocationContext getInstance() throws GVInternalException
    {
        InvocationContext invocationContext = null;

        LinkedList<InvocationContext> icStack = getInvocationContextStack();
        if (!icStack.isEmpty()) {
            invocationContext = icStack.getFirst();
        }

        if (invocationContext != null) {
            return invocationContext;
        }
        logger.error("The invocationContext with key '" + THREAD_CTX_MAP_KEY
                + "' not found in the ThreadMap. Verify the InvocationContext correct use. Throw Exception");
        throw new GVInternalException("The invocationContext with key '" + THREAD_CTX_MAP_KEY
                + "' not found in the ThreadMap. Verify the InvocationContext correct use.");
    }

    /**
     * It returns the InvocationContext instance from the top to the stack,
     * leaving the stack unchanged. It verification that the instance is
     * assignment-compatible to clazz (otherwise returns null), in order to
     * avoid ClassCastException.
     *
     * @param clazz
     *        The class
     * @return InvocationContext
     */
    public static final InvocationContext getInstance(Class<?> clazz)
    {
        InvocationContext invocationContext = null;

        LinkedList<InvocationContext> icStack = getInvocationContextStack();
        if (!icStack.isEmpty()) {
            Object obj = icStack.getFirst();
            if (clazz.isInstance(obj)) {
                invocationContext = (InvocationContext) obj;
            }
        }

        return invocationContext;
    }

    /**
     * Set the invocation context.
     *
     * @param operation
     * @param gvBuffer
     */
    public void setContext(String operation, GVBuffer gvBuffer)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Set Context:\n\toperation: '" + operation + "'\n\tGVBuffer: " + gvBuffer.toString());
        }
        this.operation = operation;
        service = gvBuffer.getService();
        system = gvBuffer.getSystem();
        id = gvBuffer.getId();
    }

    /**
     * Set the invocation context.
     *
     * @param operation
     *
     * @param subOperation
     * @param gvBuffer
     */
    public void setContext(String operation, String subOperation, GVBuffer gvBuffer)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Set Context:'\n\tsubOperation: '" + subOperation + "'");
        }
        setContext(operation, gvBuffer);
        this.subOperation = subOperation;
    }

    /**
     * @return Returns the subSystem.
     */
    public String getSubSystem()
    {
        return subSystem;
    }

    /**
     * @return Returns the operation.
     */
    public String getOperation()
    {
        return operation;
    }

    /**
     * @return Returns the service.
     */
    public String getService()
    {
        return service;
    }

    /**
     * @return Returns the subOperation.
     */
    public String getSubOperation()
    {
        return subOperation;
    }

    /**
     * @return Returns the system.
     */
    public String getSystem()
    {
        return system;
    }

    /**
     * @return Returns the id.
     */
    public Id getId()
    {
        return id;
    }

    /**
     * @param subSystem
     *        The subSystem to set.
     */
    public void setSubSystem(String subSystem)
    {
        this.subSystem = subSystem;
    }

    /**
     * @param operation
     *        The operation to set.
     */
    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    /**
     * @param service
     *        The service to set.
     */
    public void setService(String service)
    {
        this.service = service;
    }

    /**
     * @param subOperation
     *        The subOperation to set.
     */
    public void setSubOperation(String subOperation)
    {
        this.subOperation = subOperation;
    }

    /**
     * @param system
     *        The system to set.
     */
    public void setSystem(String system)
    {
        this.system = system;
    }

    /**
     * @param id
     *        The Id to set.
     */
    public void setId(Id id)
    {
        this.id = id;
    }

    /**
     *
     */
    public void cleanup()
    {
        // do nothing
    }

    /**
     *
     */
    public void destroy()
    {
        cleanup();
        operation = "";
        service = "";
        system = "";
        id = null;
        subOperation = "";
        subSystem = "";
    }

    /**
     * @return string that represent the object.
     */
    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("InvocationContext[");
        buffer.append("SubSystem: '").append(subSystem).append("'");
        buffer.append(" - Service: '").append(service).append("'");
        buffer.append(" - System: '").append(system).append("'");
        buffer.append(" - Operation: '").append(operation).append("'");
        buffer.append(" - SubOperation: '").append(subOperation).append("'");
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * @param fieldName
     * @return the field requested
     */
    public String getFieldAsString(String fieldName)
    {
        Object oValue = getFieldAsObject(fieldName);
        String retValue = "" + oValue;

        return retValue;
    }

    /**
     * @param fieldName
     * @return the field requested
     */
    public Object getFieldAsObject(String fieldName)
    {
        Object retValue = null;

        if (isInvocationContextField(fieldName)) {
            if (fieldName.equals("IContext.subSystem")) {
                retValue = system;
            }
            if (fieldName.equals("IContext.system")) {
                retValue = system;
            }
            else if (fieldName.equals("IContext.service")) {
                retValue = service;
            }
            else if (fieldName.equals("IContext.operation")) {
                retValue = operation;
            }
            else if (fieldName.equals("IContext.id")) {
                retValue = id;
            }
            else if (fieldName.equals("IContext.subOperation")) {
                retValue = subOperation;
            }
        }
        else {
            retValue = "NoValid";
        }

        return retValue;
    }

    /**
     * @param fieldName
     * @return the extra field value
     */
    public Object getExtraField(String fieldName)
    {
        return extraFields.get(fieldName);
    }

    /**
     * @param fieldName
     * @param object
     */
    public void setExtraField(String fieldName, Object object)
    {
        extraFields.put(fieldName, object);
    }


    /**
     * Check if the field is a Invocation Context field.
     *
     * @param fieldName
     *        field name to check
     * @return the check result
     */
    public static boolean isInvocationContextField(String fieldName)
    {
        if (fieldName == null) {
            return false;
        }
        return fieldName.startsWith("IContext.");
    }

    /**
     * @return invocation context stack LinkedList
     */
    @SuppressWarnings("unchecked")
    private static LinkedList<InvocationContext> getInvocationContextStack()
    {
        LinkedList<InvocationContext> icStack = (LinkedList<InvocationContext>) ThreadMap.get(THREAD_CTX_MAP_KEY);
        if (icStack == null) {
            icStack = new LinkedList<InvocationContext>();
            ThreadMap.put(THREAD_CTX_MAP_KEY, icStack);
        }
        return icStack;
    }
}
