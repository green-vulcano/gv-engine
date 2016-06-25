package it.greenvulcano;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.configuration.XMLConfig;

public class Activator implements BundleActivator {

	private final static Logger LOG = LoggerFactory.getLogger(Activator.class);
		
	@Override
	public void start(BundleContext context) throws Exception {
		LOG.debug("****** GVBase started");
		
		ServiceReference<?> configurationAdminReference = context.getServiceReference(ConfigurationAdmin.class.getName());
        ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) context.getService(configurationAdminReference);        
        
        Configuration gvcfg = configurationAdmin.getConfiguration("it.greenvulcano.gvesb.bus");
        	
        String userConfiguration = (String) Optional.ofNullable(gvcfg.getProperties().get("gvbus.apikey"))
        											 .filter(Objects::nonNull)
        											 .map(c->c.toString().trim())
        											 .filter(c-> c.length()>0)
        											 .filter(c-> !c.equalsIgnoreCase("undefined"))
        											 .orElse(XMLConfig.DEFAULT_FOLDER);
   		
		String configurationPath = System.getProperty("gv.app.home") + File.separator + userConfiguration;
		try {
			
			File configDir = new File(configurationPath);
			
			if(configDir.exists() && configDir.isDirectory()){
				XMLConfig.setBaseConfigPath(configurationPath);
				LOG.debug("Configuration path set to " + configurationPath);
			}			
		
		} catch (Exception exception) {
			LOG.error("Fail to set configuration path " + configurationPath);
		}
		
	}

	@Override
	public void stop(BundleContext context) throws Exception {	
		LOG.debug("****** GVBase stopped");
	}

}
