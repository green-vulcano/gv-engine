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
package it.greenvulcano.jmx;

import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.jmx.impl.DummyJMXEntryPoint;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * The <code>JMXEntryPoint</code> is an entry point for the used JMX
 * functionalities. <br/>
 * The JMX functionalities are based on the support provided by the Jakarta
 * modeler. <br/>
 * The <code>JMXEntryPoint</code> functionalities are configurable. In order to
 * avoid multiple configuration files, the configuration file contains both
 * configuration for the <code>JMXEntryPoint</code> and for the modeler. <br/>
 * The <code>JMXEntryPoint</code> retrieves the MBean server using the
 * configured <code>MBeanServerFinder</code> and initializes the modeler
 * registry with the retrieved MBean server.
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public abstract class JMXEntryPoint implements ConfigurationListener {
		
	private final static Logger LOG = LoggerFactory.getLogger(JMXEntryPoint.class);
	
    public static final String MODELER_DTD = "it/greenvulcano/jmx/modeler.dtd";

    /**
     * XMLConfig file for the <code>JMXEntryPoint</code>.
     */
    protected static final String CONFIGURATION_FILE  = "gv-jmx.xml";
   
    /**
     * Error occurred during initialization.
     */
    protected static Exception initializationError = null;
    
    /**
     * Unique instance of the <code>JMXEntryPoint</code>.
     */
    private static AtomicReference<JMXEntryPoint> instance;
    
    static {
    	JMXEntryPoint defaultImpl = new DummyJMXEntryPoint();
    	instance = new AtomicReference<JMXEntryPoint>(defaultImpl);
    }
        
    /**
     * This method returns the unique instance of the <code>JMXEntryPoint</code>
     * available in the JVM.
     * 
     * @return the unique instance of <code>JMXEntryPoint</code>
     * @throws Exception
     */
    public static JMXEntryPoint getInstance() throws Exception  {
    	JMXEntryPoint jmx = instance.get();
    	LOG.debug("Provided JMX entry point of class " + jmx.getClass());
        return jmx;
    }
    
    protected static void setInstance(JMXEntryPoint instance) {	
    	
    	JMXEntryPoint jmx = Optional.ofNullable(instance).orElseGet(DummyJMXEntryPoint::new);
    	
    	LOG.debug("Setting JMX entry point instance of class " + jmx.getClass());
    	XMLConfig.addConfigurationListener(jmx, JMXEntryPoint.CONFIGURATION_FILE);
    	XMLConfig.removeConfigurationListener(JMXEntryPoint.instance.getAndSet(jmx), JMXEntryPoint.CONFIGURATION_FILE);
    }

    /**
     * @return the initialization error.
     */
    public static  Exception getInitializationError()
    {
        return initializationError;
    }
      
    /**
     * Register an object to the MBeanServer.
     * 
     * @param object
     *        Object to register to the server
     * @param descriptorName
     *        attribute 'name' of the configured mbean.
     * @param oname
     *        name of the MBean
     * @throws Exception
     */
    public abstract void registerObject(Object object, String descriptorName, ObjectName oname) throws Exception;
    
    /**
     * Register an object to the MBeanServer. The object name is automatically
     * calculated with values contained in the configuration.
     * 
     * @param object
     * @param descriptorName
     * 
     * @return the ObjectName of the registered MBean.
     * @throws Exception
     */
    public abstract  ObjectName registerObject(Object object, String descriptorName) throws Exception;

    /**
     * Register an object to the MBeanServer. The object name is automatically
     * calculated with values contained in the configuration and the key
     * provided.
     * 
     * @param object
     * @param descriptorName
     * @param key
     * @param value
     * 
     * @return the ObjectName of the registered MBean.
     * @throws Exception
     */
    public abstract  ObjectName registerObject(Object object, String descriptorName, String key, String value) throws Exception;

    /**
     * Register an object to the MBeanServer. The object name is automatically
     * calculated with the given properties.
     * 
     * @param object
     * @param descriptorName
     * @param keyProperties
     * 
     * @return the ObjectName of the registered MBean.
     * @throws Exception
     */
    public abstract  ObjectName registerObject(Object object, String descriptorName, Map<String, String> keyProperties) throws Exception;
    /**
     * Register an MBean to the MBeanServer.
     * 
     * @param mbean
     *        ModelMBean to register to the server
     * @param oname
     *        name of the MBean
     * @throws Exception
     */
    public abstract  void registerMBean(Object mbean, ObjectName oname) throws Exception;

    /**
     * Register an MBean to the MBeanServer. The object name is automatically
     * calculated with the given properties.
     * 
     * @param mbean
     * @param descriptorName
     * @param keyProperties
     * 
     * @return the ObjectName of the registered MBean.
     * @throws Exception
     */
    public abstract  ObjectName registerMBean(Object mbean, String descriptorName, Map<String, String> keyProperties) throws Exception;
    
    /**
     * Unregisters an object from the MBeanServer.
     * 
     * @param oname
     *        name of the MBean. Can be a filter: in this case all objects
     *        matching the filter will be unregistered.
     * @throws Exception
     */
    public abstract  void unregisterObject(ObjectName oname) throws Exception;
    
    /**
     * Unregisters an object from the MBeanServer. The object name is
     * automatically calculated with values contained in the configuration.
     * 
     * @param descriptorName
     * 
     * @return the ObjectName of the unregistered MBean.
     * @throws Exception
     */
    public abstract  ObjectName unregisterObject(String descriptorName) throws Exception;

    /**
     * Unregisters an object from the MBeanServer. The object name is
     * automatically calculated with values contained in the configuration and
     * the key provided.
     * 
     * @param descriptorName
     * @param key
     * @param value
     * 
     * @return the ObjectName of the unregistered MBean.
     * @throws Exception
     */
    public abstract  ObjectName unregisterObject(String descriptorName, String key, String value) throws Exception;

    /**
     * Unregisters an object from the MBeanServer. The object name is
     * automatically calculated with the given properties.
     * 
     * @param descriptorName
     * @param keyProperties
     * 
     * @return the ObjectName of the unregistered MBean.
     * @throws Exception
     */
    public abstract  ObjectName unregisterObject(String descriptorName, Map<String, String> keyProperties)
            throws Exception;

    /**
     * Unregisters an object from the MBeanServer. The object name is
     * automatically calculated with the given properties and object.
     * 
     * @param object
     * @param descriptorName
     * @param keyProperties
     * 
     * @return the ObjectName of the unregistered MBean.
     * @throws Exception
     */
    public abstract  ObjectName unregisterObject(Object object, String descriptorName,
            Map<String, String> keyProperties) throws Exception;

    /**
     * Unregisters an MBean from the MBeanServer. The object name is
     * automatically calculated with the given properties.
     * 
     * @param descriptorName
     * @param keyProperties
     * 
     * @return the ObjectName of the unregistered MBean.
     * @throws Exception
     */
    public abstract  ObjectName unregisterMBean(String descriptorName, Map<String, String> keyProperties)
            throws Exception;

    /**
     * Unregisters an MBean from the MBeanServer. The object name is
     * automatically calculated with the given properties and object.
     * 
     * @param mbean
     * @param descriptorName
     * @param keyProperties
     * 
     * @return the ObjectName of the unregistered MBean.
     * @throws Exception
     */
    public abstract  ObjectName unregisterMBean(Object mbean, String descriptorName,
            Map<String, String> keyProperties) throws Exception;

    
    /**
     * @return the serverName
     */
    public abstract String getServerName();
    
    public abstract MBeanServer getServer(); 

}
