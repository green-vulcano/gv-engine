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
package it.greenvulcano.gvesb.statistics;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.statistics.datawrapper.ExtendedDataWrapper;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class holds statistics information.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */

public class StatisticsData implements Serializable
{

    /**
     *
     */
    private static final long   serialVersionUID         = 2356888573710L;
    /**
     *
     */
    public static final int     SERVICE_STATUS_SUCCESS   = 1;
    /**
     *
     */
    public static final int     SERVICE_STATUS_FAILED    = 0;
    /**
     *
     */
    public static final int     SERVICE_STATUS_UNDEFINED = -1;

    private String              system                   = null;
    private String              service                  = null;
    private String              processName              = null;
    private String              packageName              = null;

    private Id                  id                       = null;

    private int                 status                   = SERVICE_STATUS_UNDEFINED;
    private int                 errorCode                = SERVICE_STATUS_UNDEFINED;

    private long                startTime                = 0;
    private long                stopTime                 = 0;

    private Map<String, String> propertiesMap            = null;

    /**
     * The constructor initialize the statisticsData object with: <li>system</li>
     * <li>service</li> <li>Id</li>
     *
     * @param gvBuffer
     *        GVBuffer object
     */
    public StatisticsData(GVBuffer gvBuffer)
    {
        system = gvBuffer.getSystem();
        service = gvBuffer.getService();
        id = gvBuffer.getId();

        ExtendedDataWrapper dataWrapper = StatisticsDataManager.getWrapper(system + "::" + service);

        if (dataWrapper != null) {
            propertiesMap = dataWrapper.resolveData(gvBuffer);
        }
        else {
            propertiesMap = new LinkedHashMap<String, String>();
        }
    }

    /**
     * The constructor initialize the statisticsData object with: <li>system</li>
     * <li>service</li> <li>Id</li> <li>package</li> <li>process</li>
     *
     * @param gvBuffer
     *        GVBuffer object
     *@param packageName
     *        The package
     *@param processName
     *        The process name into the package
     */
    public StatisticsData(GVBuffer gvBuffer, String packageName, String processName)
    {
        this(gvBuffer);
        this.packageName = packageName;
        this.processName = processName;
    }

    /**
     * The constructor initialize the statisticsData object with: <li>system</li>
     * <li>service</li> <li>Id</li> <li>process</li>
     *
     * @param gvBuffer
     *        GVBuffer object
     *@param processName
     *        The process name into the package
     */
    public StatisticsData(GVBuffer gvBuffer, String processName) throws Exception
    {
        this(gvBuffer);
        this.processName = processName;
    }

    /**
     * To string method for noString fields vale. ES: Id
     *
     * @return string String object formatted
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer("[StatisticsData]\n");
        buf.append("\tsystem ....... = ").append(system).append("\n");
        buf.append("\tservice ...... = ").append(service).append("\n");
        buf.append("\tid ........... = ").append(id).append("\n");
        buf.append("\tpackage ...... = ").append(packageName).append("\n");
        buf.append("\tprocess ...... = ").append(processName).append("\n");
        buf.append("\tstartTime .... = ").append(startTime).append("\n");
        buf.append("\tstopTime ..... = ").append(stopTime).append("\n");
        buf.append("\tstatus ....... = ").append(status).append("\n");
        buf.append("\terror code ... = ").append(errorCode).append("\n");
        if (propertiesMap != null) {
            buf.append("\t[Properties]\n");
            for (String objectName : propertiesMap.keySet()) {
                String value = propertiesMap.get(objectName);
                buf.append("\t\t").append(objectName).append(" = ").append(value).append("\n");
            }
            buf.append("\t[Properties]\n");
        }
        buf.append("[StatisticsData]\n");
        return buf.toString();
    }

    // PUBLIC SET METHODS
    //
    /**
     * Set the statistics stop time <li>StopTime</li>
     */
    public void setStopValues()
    {
        long currentTime = System.currentTimeMillis();
        setStopTime(currentTime);
    }

    /**
     * Set the statistics stop time and state of service
     *
     * @param gvBuffer
     *        GVBuffer object
     * @param status
     *        Service status
     */
    public void setStopValues(GVBuffer gvBuffer, int status)
    {
        setStopValues();
        if (status != SERVICE_STATUS_UNDEFINED) {
            setServiceStatus(status);
            setErrorCode(gvBuffer.getRetCode());
        }
    }

    /**
     * Set the statistics stop time and state of service
     *
     * @param gvException
     *        GVException object
     *@param status
     *        Service status
     */
    public void setStopValues(GVException gvException, int status)
    {
        setStopValues();
        if (status != SERVICE_STATUS_UNDEFINED) {
            setServiceStatus(status);
            setErrorCode(gvException.getErrorCode());
        }
    }

    /**
     * Set the system name
     *
     * @param system
     *        The system name
     */
    public void setSystem(String system)
    {
        this.system = system;
    }

    /**
     * Set the system name
     *
     * @param service
     *        The service name
     */
    public void setService(String service)
    {
        this.service = service;
    }

    /**
     * Set the Id
     *
     * @param id
     *        The id object
     */
    public void setID(Id id)
    {
        this.id = id;
    }

    /**
     * Set the statistics StartTime
     *
     * @param startTime
     *        The long value of startTime
     */
    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }

    /**
     * Set the statistics StopTime
     *
     * @param stopTime
     *        The long value of StopTime
     */
    public void setStopTime(long stopTime)
    {
        this.stopTime = stopTime;
    }

    /**
     * Set the service status
     *
     * @param status
     *        The value of service status
     */
    public void setServiceStatus(int status)
    {
        this.status = status;
    }

    /**
     * Set the statistics errorCode
     *
     * @param errorCode
     *        The value of errorCode
     */
    public void setErrorCode(int errorCode)
    {
        this.errorCode = errorCode;
    }

    /**
     * Set the statistics processName
     *
     * @param processName
     *        The name of process
     */
    public void setProcessName(String processName)
    {
        this.processName = processName;
    }

    /**
     * Set the statistics package name
     *
     * @param packageName
     *        The name of package
     */
    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    /**
     * Gets the statistics <code>StartTime</code>
     *
     * @return the statistics <code>StartTime</code>
     */
    public long getStartTime()
    {
        return startTime;
    }

    /**
     * Gets the statistics <code>StopTime</code>
     *
     * @return the statistics <code>StopTime</code>
     */
    public long getStopTime()
    {
        return stopTime;
    }

    /**
     * Gets the statistics service <code>Status</code>
     *
     * @return the statistics service <code>Status</code>
     */
    public int getServiceStatus()
    {
        return status;
    }

    /**
     * Gets the statistics <code>System Name</code>
     *
     * @return the statistics <code>System Name</code>
     */
    public String getSystem()
    {
        return system;
    }

    /**
     * Gets the statistics <code>Service Name</code>
     *
     * @return the statistics <code>Service Name</code>
     */
    public String getService()
    {
        return service;
    }

    /**
     * Gets the statistics <code>Error Code</code>
     *
     * @return the statistics <code>Error Code</code>
     */
    public int getErrorCode()
    {
        return errorCode;
    }

    /**
     * Gets the statistics <code>Process Time</code>
     *
     * @return the statistics <code>Process Time</code>
     */
    public long getProcessTime()
    {
        long processTime = stopTime - startTime;
        return processTime;
    }

    /**
     * Gets the statistics <code>Process Name</code>
     *
     * @return the statistics <code>Process Name</code>
     */
    public String getProcessName()
    {
        return processName;
    }

    /**
     * Gets the statistics <code>Package Name</code>
     *
     * @return the statistics <code>Package Name</code>
     */
    public String getPackageName()
    {
        return packageName;
    }

    /**
     * Gets the statistics <code>Id value</code>
     *
     * @return the statistics <code>Id value</code>
     */
    public Id getID()
    {
        return id;
    }

    /**
     * Gets the properties map.
     *
     * @return the properties map.
     */
    public Map<String, String> getPropertiesMap()
    {
        return propertiesMap;
    }
}
