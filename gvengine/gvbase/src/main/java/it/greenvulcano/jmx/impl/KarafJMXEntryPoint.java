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
package it.greenvulcano.jmx.impl;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.ModelMBeanUser;
import it.greenvulcano.jmx.ObjectNameBuilder;
import it.greenvulcano.util.xml.DOMWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBean;

import org.apache.commons.modeler.ManagedBean;
import org.apache.commons.modeler.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
public class KarafJMXEntryPoint extends JMXEntryPoint {
	
	private final static Logger LOG = LoggerFactory.getLogger(KarafJMXEntryPoint.class);
	
	private Registry registry;
	private Vector<ObjectNameBuilder> objectNameBuilders  = null;
	private final MBeanServer mBeanServer;
  
	private KarafJMXEntryPoint(MBeanServer mBeanServer) {
		this.mBeanServer = Objects.requireNonNull(mBeanServer);
	}
	
    public static void setup(MBeanServer mBeanServer) {
    	KarafJMXEntryPoint jmxEntryPoint = new KarafJMXEntryPoint(mBeanServer); 
    	jmxEntryPoint.init();
    	setInstance(jmxEntryPoint);	      
    } 
    
    private void init() {
    	try {
   		 LOG.debug("Initialiting JMX entry point");
   		 
            registry = Registry.getRegistry(null, null);
            objectNameBuilders = null;
            NodeList nl = XMLConfig.getNodeList(CONFIGURATION_FILE, "/jmx/entry-point/ObjectNameBuilders/ObjectNameBuilder");
            if (nl != null) {
           	 objectNameBuilders = new Vector<ObjectNameBuilder>();
                for (int i = 0; i < nl.getLength(); i++) {
                    ObjectNameBuilder onb = new ObjectNameBuilder();
                    onb.init(nl.item(i));
                    objectNameBuilders.add(onb);
                }
            }              
    
            // Modeler configuration
            Node modelerConf = XMLConfig.getNode(CONFIGURATION_FILE, "/jmx/mbeans-descriptors");
            initModeler(modelerConf);         
           
        } catch (Exception exc) {
       	 LOG.error("Failed to configure JMXEntryPoint for Karaf", exc);
            initializationError = exc;
        } 
    }
    
    /**
     * Initializes the modeler.
     */
    private void initModeler(Node conf) throws Exception
    {
        // The modeler requires an InputStream in order to configure itself,
        // so we need to serialize the configuration.

        DOMWriter writer = new DOMWriter();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream print = new PrintStream(out);

        // Read the DTD and put it in the out stream, in order to avoid the
        // exceptions
        // raised by the XML parser.
        //
        InputStream istream = getClass().getClassLoader().getResourceAsStream(MODELER_DTD);
        if (istream != null) {
            byte[] buf = new byte[2048];
            int l = 0;
            while ((l = istream.read(buf)) != -1) {
                print.write(buf, 0, l);
            }
            print.flush();
            istream.close();
        }
        else {
            LOG.warn(MODELER_DTD + " NOT FOUND. PLEASE, IGNORE FOLLOWING EXCEPTIONS.");
        }

        writer.write(conf, print);
        print.flush();
        print.close();

        // Prepares the input stream and initializes the modeler's Registry
        //
        ByteArrayInputStream stream = new ByteArrayInputStream(out.toByteArray());
        registry.loadDescriptors(stream);
    }

   
    /**
     * XMLConfig changed. Reconfigure <code>JMXEntryPoint</code> if the
     * CONFIGURATION_FILE is reloaded.
     * 
     * @param event
     */
    @Override
    public void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_LOADED) && event.getFile().equals(CONFIGURATION_FILE)) {
           init();
        }
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
    public synchronized void registerObject(Object object, String descriptorName, ObjectName oname) throws Exception
    {
        

        ManagedBean managed = registry.findManagedBean(descriptorName);
        if (managed!=null) {
	        ModelMBean mbean = managed.createMBean(object);
	        mBeanServer.registerMBean(mbean, oname);
	        if (object instanceof ModelMBeanUser) {
	            ((ModelMBeanUser) object).setMBean(mbean, oname);
	        }
        }
    }

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
    public synchronized ObjectName registerObject(Object object, String descriptorName) throws Exception
    {
        return registerObject(object, descriptorName, (Map<String, String>) null);
    }

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
    public synchronized ObjectName registerObject(Object object, String descriptorName, String key, String value)
            throws Exception
    {
        

        Map<String, String> keyProperties = null;
        if ((key != null) && (value != null)) {
            keyProperties = new HashMap<String, String>();
            keyProperties.put(key, value);
        }
        return registerObject(object, descriptorName, keyProperties);
    }

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
    public synchronized ObjectName registerObject(Object object, String descriptorName,
            Map<String, String> keyProperties) throws Exception
    {
        

        ManagedBean managed = registry.findManagedBean(descriptorName);
        if (managed!= null) {
	        ModelMBean mbean = managed.createMBean(object);       
	        ObjectName oname = calculateObjectName(object, mBeanServer, managed, keyProperties, descriptorName);
	        mBeanServer.registerMBean(mbean, oname);
	        if (object instanceof ModelMBeanUser) {
	            ((ModelMBeanUser) object).setMBean(mbean, oname);
	        }
	        return oname;
        }
        
        return null;
        
    }

    /**
     * Register an MBean to the MBeanServer.
     * 
     * @param mbean
     *        ModelMBean to register to the server
     * @param oname
     *        name of the MBean
     * @throws Exception
     */
    public synchronized void registerMBean(Object mbean, ObjectName oname) throws Exception
    {
        

        mBeanServer.registerMBean(mbean, oname);
    }

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
    public synchronized ObjectName registerMBean(Object mbean, String descriptorName, Map<String, String> keyProperties)
            throws Exception
    {
        

        ObjectName oname = calculateMBeanName(mbean, mBeanServer, keyProperties, descriptorName);
        mBeanServer.registerMBean(mbean, oname);
        return oname;
    }

    /**
     * Unregisters an object from the MBeanServer.
     * 
     * @param oname
     *        name of the MBean. Can be a filter: in this case all objects
     *        matching the filter will be unregistered.
     * @throws Exception
     */
    public synchronized void unregisterObject(ObjectName oname) {
        try {
	        if (oname.isPattern()) {            
	            Set<ObjectName> names = mBeanServer.queryNames(oname, null);
	            Iterator<ObjectName> i = names.iterator();
	            while (i.hasNext()) {
	                ObjectName name = i.next();
	                mBeanServer.unregisterMBean(name);
	            }          
	        }  else {
	            mBeanServer.unregisterMBean(oname);
	        }
        } catch (Exception e) {
        	LOG.error("Unable to unregister ObjectName "+oname,e);
		}
    }

    /**
     * Unregisters an object from the MBeanServer. The object name is
     * automatically calculated with values contained in the configuration.
     * 
     * @param descriptorName
     * 
     * @return the ObjectName of the unregistered MBean.
     * @throws Exception
     */
    public synchronized ObjectName unregisterObject(String descriptorName) throws Exception
    {
        return unregisterObject(descriptorName, null);
    }

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
    public synchronized ObjectName unregisterObject(String descriptorName, String key, String value) throws Exception
    {
        

        Map<String, String> keyProperties = null;
        if ((key != null) && (value != null)) {
            keyProperties = new HashMap<String, String>();
            keyProperties.put(key, value);
        }
        return unregisterObject(descriptorName, keyProperties);
    }

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
    public synchronized ObjectName unregisterObject(String descriptorName, Map<String, String> keyProperties)
            throws Exception
    {
        

        ManagedBean managed = registry.findManagedBean(descriptorName);
        if (managed!=null){
        	ObjectName oname = calculateObjectName(null, mBeanServer, managed, keyProperties, descriptorName);
        	mBeanServer.unregisterMBean(oname);
        	
        	return oname;
        }
        
        
        return null;
    }

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
    public synchronized ObjectName unregisterObject(Object object, String descriptorName,
            Map<String, String> keyProperties) throws Exception
    {
        

        ManagedBean managed = registry.findManagedBean(descriptorName);
        if (managed!=null){
	        ObjectName oname = calculateObjectName(object, mBeanServer, managed, keyProperties, descriptorName);
	        mBeanServer.unregisterMBean(oname);
	        return oname;
        }
        
        return null;
    }

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
    public synchronized ObjectName unregisterMBean(String descriptorName, Map<String, String> keyProperties)
            throws Exception
    {
        

        ObjectName oname = calculateMBeanName(null, mBeanServer, keyProperties, descriptorName);
        mBeanServer.unregisterMBean(oname);
        return oname;
    }

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
    public synchronized ObjectName unregisterMBean(Object mbean, String descriptorName,
            Map<String, String> keyProperties) throws Exception
    {
        

        ObjectName oname = calculateMBeanName(mbean, mBeanServer, keyProperties, descriptorName);
        mBeanServer.unregisterMBean(oname);
        return oname;
    }

    /**
     * Returns the MBeanServer
     * 
     * @return the MBeanServer
     */
    public synchronized MBeanServer getServer()
    {
        return mBeanServer;
    }

    /**
     * @return the serverName
     */
    @Override
    public String getServerName()
    {
        return "karaf";
    }

    private ObjectName calculateObjectName(Object object, MBeanServer mserver, ManagedBean managed,
            Map<String, String> properties, String descriptorName) throws Exception
    {
        String domain = managed.getDomain();
        if (domain == null) {
            domain = mserver.getDefaultDomain();
        }

        String name = managed.getName();
        String className = managed.getClassName();
        String group = managed.getGroup();
        String type = managed.getType();

        Hashtable<String, String> keyProperties = new Hashtable<String, String>();
        keyProperties.put("Name", name);
        keyProperties.put("Class", className);
        keyProperties.put("Group", group);
        keyProperties.put("Type", type);
        if (properties != null) {
            keyProperties.putAll(properties);
        }

        if (objectNameBuilders != null) {
            for (int i = 0; i < objectNameBuilders.size(); i++) {
                ObjectNameBuilder onb = objectNameBuilders.get(i);
                keyProperties = onb.resolve(descriptorName, keyProperties, object);
            }
        }

        ObjectName oname = new ObjectName(domain, keyProperties);

        return oname;
    }

    private ObjectName calculateMBeanName(Object mbean, MBeanServer mserver, Map<String, String> properties,
            String descriptorName) throws Exception
    {
        String domain = mserver.getDefaultDomain();

        Hashtable<String, String> keyProperties = new Hashtable<String, String>();
        if (properties != null) {
            keyProperties.putAll(properties);
        }

        if (objectNameBuilders != null) {
            for (int i = 0; i < objectNameBuilders.size(); i++) {
                ObjectNameBuilder onb = objectNameBuilders.get(i);
                keyProperties = onb.resolve(descriptorName, keyProperties, mbean);
            }
        }

        ObjectName oname = new ObjectName(domain, keyProperties);

        return oname;
    }
}
