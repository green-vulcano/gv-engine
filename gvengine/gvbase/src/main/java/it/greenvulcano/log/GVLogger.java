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

import org.slf4j.Logger;

/**
 * This class is an central point for access log4j functionalities.
 * <p>
 * The GreenVulcano code MUST NOT call <code>Logger.getLogger()</code> directly,
 * but MUST call <code>org.slf4j.LoggerFactory.getLogger()</code> method. This ensure a correct
 * initialization mechanism. The returned Logger is a classic log4j logger.
 * <p>
 * This class is a <code>ConfigurationListener</code> that reconfigures log4j if
 * the configuration file is reloaded.<br>
 * The <code>reload()</code> mehod can be used in order to reconfigure log4j.<br>
 * <code>getConfigurationFile()</code> return the configuration file name used
 * by <code>GVLogger</code>.
 * <p>
 * 
 * Note that the reloading mechanism is a little bit complicated, but this
 * uniforms the configuration reloading mechanism for all GreenVulcano classes.
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 **/
@Deprecated
public final class GVLogger
{
   /**
     * Constructor.
     */
    private GVLogger() {
        // do nothing
    }    
    
    /**
     * Return the Logger for the given class.
     * 
     * @param cls
     *        the logger class
     * @return the requested logger
     */
    public static Logger getLogger(Class<?> cls) {
        
        return org.slf4j.LoggerFactory.getLogger(cls);
    }

    /**
     * Return the Logger for the given logger name.
     * 
     * @param logger
     *        the logger name
     * @return the requested logger
     */
    public static Logger getLogger(String logger) {
    	 return org.slf4j.LoggerFactory.getLogger(logger);
    }
  

}
