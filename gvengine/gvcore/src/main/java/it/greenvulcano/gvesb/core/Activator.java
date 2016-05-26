package it.greenvulcano.gvesb.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.core.jmx.RegisterJMXServiceManager;
import it.greenvulcano.gvesb.core.jmx.RegisterServiceOperationInfoManager;
import it.greenvulcano.jmx.MBeanServerInitializerFactory;

public class Activator implements BundleActivator  {

	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Override
	public void start(BundleContext context) throws Exception {
		LOG.debug("********* GVCore Up&Running");
		MBeanServerInitializerFactory.registerSupplier("it.greenvulcano.gvesb.core.jmx.RegisterServiceOperationInfoManager", RegisterServiceOperationInfoManager::new);
		MBeanServerInitializerFactory.registerSupplier("it.greenvulcano.gvesb.core.jmx.RegisterJMXServiceManager", RegisterJMXServiceManager::new);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LOG.debug("********* GVCore stopped");
		MBeanServerInitializerFactory.deregisterSupplier("it.greenvulcano.gvesb.core.jmx.RegisterServiceOperationInfoManager");
		MBeanServerInitializerFactory.deregisterSupplier("it.greenvulcano.gvesb.core.jmx.RegisterJMXServiceManager");
	}

}
