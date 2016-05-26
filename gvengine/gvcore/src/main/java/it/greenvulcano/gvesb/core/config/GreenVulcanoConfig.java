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
package it.greenvulcano.gvesb.core.config;

/**
 * The main Config GreenVulcano class.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public final class GreenVulcanoConfig
{
    /**
     * Constructor
     */
    private GreenVulcanoConfig()
    {
        // do nothing
    }

    /**
     * The name of the Services configuration file.
     */
    private static String svcConfigFileName = "GVServices.xml";

    /**
     * The name of the Systems configuration file.
     */
    private static String sysConfigFileName = "GVSystems.xml";

    /**
     * Get the services configuration file name.
     *
     * @return The services configuration file name
     */
    public static String getServicesConfigFileName()
    {
        return GreenVulcanoConfig.svcConfigFileName;
    }

    /**
     * Get the systems configuration file name.
     *
     * @return The systems configuration file name
     */
    public static String getSystemsConfigFileName()
    {
        return GreenVulcanoConfig.sysConfigFileName;
    }

}