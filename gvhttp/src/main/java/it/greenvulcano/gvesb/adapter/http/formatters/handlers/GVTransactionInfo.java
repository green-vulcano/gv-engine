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
package it.greenvulcano.gvesb.adapter.http.formatters.handlers;

import it.greenvulcano.gvesb.buffer.Id;

/**
 * This class is a bean encapsulating some informations about a GV transaction
 * initiated by an external system or by GreenVulcano. Those informations can be
 * extracted from this bean and sent back to the invoking system even if the
 * call to GreenVulcano fails.
 *
 *
 * @version 3.1.0 Feb 07, 2011
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVTransactionInfo
{

    /**
     * Name of the system initiating the communication.
     */
    private String  system;

    /**
     * Name of the requested service.
     */
    private String  service;

    /**
     * Request identifier.
     */
    private Id      id;

    /**
     * GV Operation.
     */
    private String  operation;

    /**
     * Error message encapsulated within an exception thrown or caught by the
     * adapter.
     */
    private String  errorMessage;

    /**
     * Error code encapsulated within an exception thrown or caught by the
     * adapter.
     */
    private int     errorCode;

    /**
     * Boolean indicating that this <tt>GVTransactionInfo</tt> object is used to
     * send back an error response.
     */
    private boolean isError;

    /**
     * Boolean indicating that this <tt>GVTransactionInfo</tt> object is used to
     * send back an ACK response.
     */
    private boolean isACK;

    /**
     * Empty constructor.
     */
    public GVTransactionInfo()
    {
        isError = false;
        isACK = true;
    }

    /**
     *
     * @param system
     */
    public void setSystem(String system)
    {
        this.system = system;
    }

    /**
     *
     * @param service
     */
    public void setService(String service)
    {
        this.service = service;
    }

    /**
     *
     * @param id
     */
    public void setId(Id id)
    {
        this.id = id;
    }

    /**
     *
     * @param operation
     */
    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    /**
     *
     * @param errorMessage
     */
    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
        isError = true;
        isACK = false;
    }

    /**
     *
     * @param errorCode
     */
    public void setErrorCode(int errorCode)
    {
        this.errorCode = errorCode;
        isError = true;
        isACK = false;
    }

    /**
     *
     * @return the system
     */
    public String getSystem()
    {
        return (system);
    }

    /**
     *
     * @return the service
     */
    public String getService()
    {
        return (service);
    }

    /**
     *
     * @return the request identifier
     */
    public Id getid()
    {
        return id;
    }

    /**
     *
     * @return the operation name
     */
    public String getOperation()
    {
        return operation;
    }


    /**
     *
     * @return the error message
     */
    public String getErrorMessage()
    {
        return errorMessage;
    }

    /**
     *
     * @return the error code
     */
    public int getErrorCode()
    {
        return errorCode;
    }

    /**
     *
     * @return if is an error.
     */
    public boolean isError()
    {
        return isError;
    }

    /**
     *
     * @return if is an acknowledge
     */
    public boolean isACK()
    {
        return isACK;
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuffer toString = new StringBuffer("GVTransactionInfo: \n");
        toString.append("\t\nsystem = ");
        toString.append(system);
        toString.append("\t\nservice = ");
        toString.append(service);
        toString.append("\t\nid = ");
        if (id != null) {
            toString.append(id.toString());
        }
        toString.append("\t\noperation = ");
        toString.append(operation);
        toString.append("\t\nerrorMessage = ");
        toString.append(errorMessage);
        toString.append("\t\nerrorCode = ");
        toString.append(errorCode);
        toString.append("\t\nisACK = ");
        toString.append(isACK);
        toString.append("\t\nisError = ");
        toString.append(isError);
        toString.append("\t\n");
        return new String(toString);
    }

    /**
     * @param fieldName
     * @return the field as string
     */
    public String getFieldAsString(String fieldName)
    {
        String retValue = "";

        if (fieldName != null) {
            if (fieldName.equals("GVTransInfo.system")) {
                retValue = system;
            }
            else if (fieldName.equals("GVTransInfo.service")) {
                retValue = service;
            }
            else if (fieldName.equals("GVTransInfo.id")) {
                retValue = (id != null) ? id.toString() : "";
            }
            else if (fieldName.equals("GVTransInfo.operation")) {
                retValue = operation;
            }
            else if (fieldName.equals("GVTransInfo.errorCode")) {
                retValue = "" + errorCode;
            }
            else if (fieldName.equals("GVTransInfo.errorMessage")) {
                retValue = errorMessage;
            }
        }
        else {
            retValue = null;
        }

        return retValue;
    }

    /**
     * @param fieldName
     * @return if passed argument corresponds to a
     *         <code>GVTransactionInfo</code> field
     */
    public static boolean isGVTransInfoField(String fieldName)
    {
        return fieldName.startsWith("GVTransInfo.");
    }
}
