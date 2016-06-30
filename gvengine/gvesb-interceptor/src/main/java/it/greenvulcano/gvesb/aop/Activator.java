package it.greenvulcano.gvesb.aop;

import it.greenvulcano.gvesb.core.flow.hub.Subject;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
	
	private static GVConsoleObserver gvConsoleObserver;
	
	@Override
	public void start(BundleContext context) throws Exception {
		LOG.info("************ GVinterceptor start - START");
		
		gvConsoleObserver = new GVConsoleObserver(Subject.getInstance());
		
		LOG.info("************ GVinterceptor start - END");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LOG.info("************ GVinterceptor stop - START");
		Subject.getInstance().detach(gvConsoleObserver);
		gvConsoleObserver = null;
		LOG.info("************ GVinterceptor stop - END");
	}

}
