package it.greenvulcano.gvesb.console.api.repository;

import org.mongodb.morphia.mapping.DefaultCreator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleObjectFactory extends DefaultCreator {
	private BundleContext bundleContext;
	private static final Logger LOG = LoggerFactory.getLogger(BundleObjectFactory.class);
	
	
	@Override
	protected ClassLoader getClassLoaderForClass() {
		LOG.debug("BundleObjectFactory.getClassLoaderForClass - START");
		ClassLoader cl = ((BundleWiring)bundleContext.getBundle().adapt(BundleWiring.class)).getClassLoader();
		LOG.debug("BundleObjectFactory.getClassLoaderForClass - END: ClassLoader: " + cl);
		return cl;
	}

	public BundleObjectFactory(BundleContext bundleContext) {
		super();
		LOG.debug("BundleObjectFactory.construct - START");
		this.bundleContext = bundleContext;
		LOG.debug("BundleObjectFactory.construct - END");
	}
}
