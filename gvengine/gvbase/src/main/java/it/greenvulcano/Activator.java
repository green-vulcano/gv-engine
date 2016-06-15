package it.greenvulcano;

import java.io.File;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.configuration.XMLConfig;

public class Activator implements BundleActivator {

	private final static Logger LOG = LoggerFactory.getLogger(Activator.class);
		
	@Override
	public void start(BundleContext context) throws Exception {
		LOG.debug("****** GVBase started");
		
		String configurationPath = System.getProperty("gv.app.home") + File.separator + "xmlconfig";
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
