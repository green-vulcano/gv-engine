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
package it.greenvulcano.jmx;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.thread.BaseThread;
import it.greenvulcano.util.xml.DOMWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
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
public class JMXEntryPoint implements ConfigurationListener {
	
	private final static Logger LOG = LoggerFactory.getLogger(JMXEntryPoint.class);
	
    /**
     *
     */
    public static final String        MODELER_DTD         = "it/greenvulcano/jmx/modeler.dtd";

    /**
     * XMLConfig file for the <code>JMXEntryPoint</code>.
     */
    private static final String       CONFIGURATION_FILE  = "gv-jmx.xml";

    /**
     * Unique instance of the <code>JMXEntryPoint</code>.
     */
    private static JMXEntryPoint      instance            = null;
    
    private static boolean            initialized         = false;
    private static boolean            initializing        = false;

    /**
     * Error occurred during initialization.
     */
    private static Exception          initializationError = null;

    private static String             serverName          = "";
    private static Registry           registry            = null;

    private Vector<ObjectNameBuilder> objectNameBuilders  = null;
    private MBeanServer               mbeanServer;

    private JMXEntryPoint() {
        // do nothing
    }
    
    /**
     * This method returns the unique instance of the <code>JMXEntryPoint</code>
     * available in the JVM.
     * 
     * @return the unique instance of <code>JMXEntryPoint</code>
     * @throws Exception
     */
    public static synchronized JMXEntryPoint instance() throws Exception
    {
        if (instance == null) {
            initialized = false;
            initializationError = null;
            instance = new JMXEntryPoint();
            try {
                XMLConfig.addConfigurationListener(instance, CONFIGURATION_FILE);

                instance.init();
            }
            catch (Exception exc) {
                initializationError = exc;
            }

            if (initializationError != null) {
                XMLConfig.removeConfigurationListener(instance, CONFIGURATION_FILE);
                instance = null;
                throw initializationError;
            }
        }
        return instance;
    }

    /**
     * @return the initialization error.
     */
    public static synchronized Exception getInitializationError()
    {
        return initializationError;
    }

    /**
     * Initializes the <code>JMXEntryPoint</code> according to the
     * configuration.
     */
    private void init() throws Exception
    {
        if (initialized) {
            return;
        }

        synchronized (JMXEntryPoint.class) {
            if (initialized) {
                return;
            }
            if (initializing) {
                return;
            }

            initializing = true;
            try {
                // JMXEntryPoint specific configuration
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
        
                Node serverFinderConf = XMLConfig.getNode(CONFIGURATION_FILE, "/jmx/entry-point/*[@type='server-finder']");
                initMBeanServer(serverFinderConf);
        
                // Modeler configuration
                Node modelerConf = XMLConfig.getNode(CONFIGURATION_FILE, "/jmx/mbeans-descriptors");
                initModeler(modelerConf);

                // in order to re-enter init() during initializers run...
                initialized = true;

                // Modeler configuration
                invokeInitializers();
            }
            finally {
                initializing = false;
            }
        }
    }

    /**
     * Retrieves the MBeanServer and set it to the modeler's registry.
     */
    private void initMBeanServer(Node conf) throws Exception
    {
        // Build and initialize the MBeanServerFinder
        //
        String serverFinderClassName = XMLConfig.get(conf, "@class");
        Class<?> serverFinderClass = Class.forName(serverFinderClassName);
        MBeanServerFinder serverFinder = (MBeanServerFinder) serverFinderClass.newInstance();
        serverFinder.init(conf);

        // Find the MBeanServer
        //
        mbeanServer = serverFinder.findMBeanServer();

        // Gets the modele's Registry
        //
        registry = Registry.getRegistry(null, null);

        serverName = serverFinder.getServerName();
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
     * Invokes the initializers.
     */
    private void invokeInitializers() throws Exception
    {
        NodeList list = XMLConfig.getNodeList(CONFIGURATION_FILE,
                "/jmx/entry-point/Initializers/*[@type='initializer']");

        for (int i = 0; i < list.getLength(); ++i) {
            final Node node = list.item(i);
            final int delayedInit = XMLConfig.getInteger(node, "@delayed-init", -1);
            if (delayedInit > 0) {
            	final String className = XMLConfig.get(node, "@class");

            	Runnable rr = new Runnable() {
            		private Node lnode = node;
            		private long ldelay = delayedInit * 1000;
                    @Override
                    public void run()
                    {
                        try {
                            Thread.sleep(ldelay);
                        }
                        catch (InterruptedException exc) {
                            // do nothing
                        }
                        try {
                               createInitializer(lnode);
                        } catch (Exception exc) {
                               LOG.error("Error initializing class " + className,exc);
                   
                        }
                    }
                };

                BaseThread bt = new BaseThread(rr, "Initializer for: " + className);
                bt.setDaemon(true);
                bt.start();
            }
            else {
	            createInitializer(node);
            }
        }
    }

    /**
     * @param node
     * @throws XMLConfigException
     * @throws PropertiesHandlerException
     */
    private void createInitializer(Node node) throws XMLConfigException,
                       PropertiesHandlerException {
        String className = XMLConfig.get(node, "@class");
        if (className != null) {
            String sName = PropertiesHandler.expand(XMLConfig.get(node, "@target", ""));
            if ((sName.length() == 0) || (sName.indexOf(serverName) != -1)) {
                try {
                    MBeanServerInitializer initializer = MBeanServerInitializerFactory.create(className);
                    initializer.init(node);
                    initializer.initializeMBeanServer(mbeanServer);
                } catch (NoSuchElementException noSuchElementException){
                	LOG.warn("Supplier not found for "+className);
                } catch (Exception exc) {
                    LOG.error("Error initializing class " + className, exc);
                   
                }
            }
        }
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
            initialized = false;
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
        init();

        ManagedBean managed = registry.findManagedBean(descriptorName);
        ModelMBean mbean = managed.createMBean(object);
        mbeanServer.registerMBean(mbean, oname);
        if (object instanceof ModelMBeanUser) {
            ((ModelMBeanUser) object).setMBean(mbean, oname);
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
        init();

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
        init();

        ManagedBean managed = registry.findManagedBean(descriptorName);
        ModelMBean mbean = managed.createMBean(object);
        ObjectName oname = calculateObjectName(object, mbeanServer, managed, keyProperties, descriptorName);
        mbeanServer.registerMBean(mbean, oname);
        if (object instanceof ModelMBeanUser) {
            ((ModelMBeanUser) object).setMBean(mbean, oname);
        }
        return oname;
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
        init();

        mbeanServer.registerMBean(mbean, oname);
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
        init();

        ObjectName oname = calculateMBeanName(mbean, mbeanServer, keyProperties, descriptorName);
        mbeanServer.registerMBean(mbean, oname);
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
    public synchronized void unregisterObject(ObjectName oname) throws Exception
    {
        init();

        if (oname.isPattern()) {
            Exception lastException = null;

            Set<ObjectName> names = mbeanServer.queryNames(oname, null);
            Iterator<ObjectName> i = names.iterator();
            while (i.hasNext()) {
                ObjectName name = i.next();
                try {
                    mbeanServer.unregisterMBean(name);
                }
                catch (Exception exc) {
                    lastException = exc;
                }
            }

            if (lastException != null) {
                throw lastException;
            }
        }
        else {
            mbeanServer.unregisterMBean(oname);
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
        init();

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
        init();

        ManagedBean managed = registry.findManagedBean(descriptorName);
        ObjectName oname = calculateObjectName(null, mbeanServer, managed, keyProperties, descriptorName);
        mbeanServer.unregisterMBean(oname);
        return oname;
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
        init();

        ManagedBean managed = registry.findManagedBean(descriptorName);
        ObjectName oname = calculateObjectName(object, mbeanServer, managed, keyProperties, descriptorName);
        mbeanServer.unregisterMBean(oname);
        return oname;
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
        init();

        ObjectName oname = calculateMBeanName(null, mbeanServer, keyProperties, descriptorName);
        mbeanServer.unregisterMBean(oname);
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
        init();

        ObjectName oname = calculateMBeanName(mbean, mbeanServer, keyProperties, descriptorName);
        mbeanServer.unregisterMBean(oname);
        return oname;
    }

    /**
     * Returns the MBeanServer
     * 
     * @return the MBeanServer
     */
    public synchronized MBeanServer getServer()
    {
        return mbeanServer;
    }

    /**
     * @return the serverName
     */
    public static String getServerName()
    {
        return serverName;
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
