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
package it.greenvulcano.gvesb.core.jmx;

import it.greenvulcano.jmx.JMXUtils;

import org.apache.log4j.Level;
import org.slf4j.Logger;

/**
 * ServiceInfo class.
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 */
public class ServiceInfo {
    private static Logger      logger          = org.slf4j.LoggerFactory.getLogger(ServiceInfo.class);

    /**
     * the object JMX descriptor
     */
    public static final String DESCRIPTOR_NAME = "ServiceInfo";
    /**
     * the service name
     */
    private String             name            = "";
    /**
     * the jmx filter for inter-instances communication
     */
    private String             jmxFilter       = "";
    /**
     * The status of the service activation.
     */
    private boolean            activation      = true;
  
    /**
     * Constructor
     *
     * @param sName
     *            the service name
     * @param act
     *            the activation flag
     */
    public ServiceInfo(String sName, boolean act, String loggerLevel) {
        this.name = sName;
        this.activation = act;
        jmxFilter = "GreenVulcano:*,Component=" + ServiceOperationInfo.DESCRIPTOR_NAME
        + ",Group=management,Internal=Yes,IDService=" + name;
    }

    /**
     * Get the status of the service activation. <br/>
     *
     * @return The Service activation flag
     */
    public boolean getActivation() {
        return activation;
    }

    /**
     * @param act
     *            the activation flag
     * @exception Exception
     *                if errors occurs
     */
    public void setActivation(boolean act) throws Exception {
        activation = act;
        JMXUtils.set(jmxFilter, "serviceActivation", new Boolean(activation), false, logger);
    }
    
    /**
     * @param loggerLevel
     *        the master logger level value
     */
    public void setLoggerLevel(String loggerLevel) throws Exception
    {
        JMXUtils.set(jmxFilter, "loggerLevelj", Level.toLevel(loggerLevel), false, logger);
    }

    /**
     * @return the service name
     */
    public String getName() {
        return name;
    }
}