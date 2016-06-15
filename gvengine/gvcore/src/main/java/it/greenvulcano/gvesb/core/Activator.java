package it.greenvulcano.gvesb.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator  {

	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Override
	public void start(BundleContext context) throws Exception {
		LOG.debug("********* GVCore Up&Running");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LOG.debug("********* GVCore stopped");
	}

}
