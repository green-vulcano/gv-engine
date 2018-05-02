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
package it.greenvulcano;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.SecretKey;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.jmx.XMLConfigProxy;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.impl.KarafJMXEntryPoint;
import it.greenvulcano.util.crypto.CryptoHelper;
import it.greenvulcano.util.crypto.CryptoUtils;
import it.greenvulcano.util.crypto.CryptoUtilsException;
import it.greenvulcano.util.crypto.KeyID;
import it.greenvulcano.util.crypto.KeyStoreID;
import it.greenvulcano.util.crypto.KeyStoreUtils;
import it.greenvulcano.util.crypto.KeyStoreUtilsException;

public class Activator implements BundleActivator {

	
	private final static Logger LOG = LoggerFactory.getLogger(Activator.class);

	
	private static Optional<ObjectName> registeredObjectName = Optional.empty(); 
	
	@Override
	public void start(BundleContext context) throws Exception {
		LOG.debug("****** GVBase started");
		
		ServiceReference<?> configurationAdminReference = context.getServiceReference(ConfigurationAdmin.class.getName());
        ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) context.getService(configurationAdminReference);
		
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory", 
									getConfigEntry(configurationAdmin, "javax.xml.parsers.DocumentBuilderFactory")
													.orElse("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl"));
		
		System.setProperty("javax.xml.transform.TransformerFactory",
									getConfigEntry(configurationAdmin, "javax.xml.transform.TransformerFactory")
													.orElse("org.apache.xalan.processor.TransformerFactoryImpl"));							
		        
              
        String home = getConfigEntry(configurationAdmin, XMLConfig.CONFIG_KEY_HOME).orElse(XMLConfig.DEFAULT_FOLDER);
                		
		try {
			
			Path configPath = Paths.get(home);
						
			if(Files.notExists(configPath)) {
				LOG.debug("Deploying default config in " + configPath.getParent());
				
				ZipInputStream defaultConfig = new ZipInputStream(getClass().getClassLoader().getResourceAsStream("config.zip"));
				ZipEntry zipEntry = null;
				
				while ((zipEntry=defaultConfig.getNextEntry())!=null) {
					
					Path entryPath = configPath.getParent().resolve(zipEntry.getName());
					
					if (zipEntry.isDirectory()) {
						Files.createDirectories(entryPath);
					} else {
						
						Files.copy(defaultConfig, entryPath, StandardCopyOption.REPLACE_EXISTING);					
					}
				}
				
				
				createRootKeystore(configPath);				
			} 
			
			String config = configPath.toAbsolutePath().toString();						
			XMLConfig.setBaseConfigPath(config);
			
			LOG.debug("Configuration path set to " + config);
					
		} catch (Exception exception) {
			LOG.error("Fail to set configuration path ",exception);
		}	
		
		try {
			if (System.getProperties().containsKey("com.sun.management.jmxremote")) {		
				ServiceReference<?> mBeanServerReference = context.getServiceReference(MBeanServer.class.getName());
				KarafJMXEntryPoint.setup((MBeanServer) context.getService(mBeanServerReference));
			}
		} catch (Exception exception) {
			LOG.error("Fail to retrieve MBeanServer ",exception);
		}
		registeredObjectName = registerXMLConfigMBean();
		
		try {
			CryptoHelper.init();
		} catch(Throwable wtf) {
			LOG.error("Failed to initalize CryptoHelper configuration ", wtf);
		}	
	}

	private void createRootKeystore(Path configPath) throws CryptoUtilsException, KeyStoreUtilsException {
		Path keystorePath = configPath.resolve("keystores");
		
		KeyStoreID defaultKeyStoreID = new KeyStoreID(CryptoHelper.DEFAULT_KEYSTORE_ID, 
				   KeyStoreUtils.DEFAULT_KEYSTORE_TYPE, 
				   CryptoHelper.DEFAULT_KEY_STORE_NAME, 
				   CryptoHelper.SECRET_KEY_STORE_PWD,										   
				   KeyStoreUtils.DEFAULT_KEYSTORE_PROVIDER);

		KeyID defaultKeyid = new KeyID(CryptoHelper.DEFAULT_KEY_ID, CryptoUtils.TRIPLE_DES_TYPE, defaultKeyStoreID, CryptoHelper.SECRET_KEY_NAME, CryptoHelper.SECRET_KEY_PWD);

		SecretKey secretKey = CryptoUtils.generateSecretKey(CryptoUtils.TRIPLE_DES_TYPE, CryptoHelper.SECRET_KEY_PWD.getBytes());

		KeyStoreUtils.writeKey(keystorePath.toAbsolutePath().toString(), defaultKeyid, secretKey, null);
		
	}

	@Override
	public void stop(BundleContext context) throws Exception {	
		try {
			registeredObjectName.ifPresent(JMXEntryPoint.getInstance()::unregisterObject);
			LOG.debug("Successfully unregistered MBean "+XMLConfigProxy.JMX_KEY_VALUE);
		} catch (Exception e) {
			LOG.error("Failed to unregister MBean "+XMLConfigProxy.JMX_KEY_VALUE,e);
		}
		registeredObjectName = null;
		LOG.debug("****** GVBase stopped");
	}
	
	private Optional<String> getConfigEntry(ConfigurationAdmin configurationAdmin, String configKey) throws IOException {
				     
		 Configuration gvcfg = configurationAdmin.getConfiguration(XMLConfig.CONFIG_PID);
		 	        
	     return  Optional.ofNullable(gvcfg.getProperties()).map(p -> p.get(configKey))
					        							   .filter(Objects::nonNull)
					        							   .map(c->c.toString().trim())
					        							   .filter(c-> c.length()>0)
					        							   .filter(c-> !c.equalsIgnoreCase("undefined"));
					        							
		 
	}
	
	private Optional<ObjectName> registerXMLConfigMBean() {
		
		Optional<ObjectName> o;
		try {
			Map<String, String> properties = new HashMap<String, String>();
			properties.put(XMLConfigProxy.JMX_KEY_NAME, XMLConfigProxy.JMX_KEY_VALUE);
			XMLConfigProxy proxy = new XMLConfigProxy();
		
			ObjectName  objectName = JMXEntryPoint.getInstance().registerObject(proxy, XMLConfigProxy.JMX_KEY_VALUE, properties);
			o = Optional.ofNullable(objectName);
		} catch (Exception e) {
			LOG.error("Failed to register XMLConfig MBean",e);
			 o = Optional.empty();
		}
		
		return o;
		
	}
	
	

}
