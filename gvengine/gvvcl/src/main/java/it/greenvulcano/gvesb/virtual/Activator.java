package it.greenvulcano.gvesb.virtual;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.virtual.pool.OperationManagerPool;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		try {
			OperationManagerPool.instance().resetCounter();
			LoggerFactory.getLogger(getClass())
				.debug("*********** OperationManagerPool Up&Runnig ");
		} catch (Exception exception) {
			LoggerFactory.getLogger(getClass())	
				.error("------!!! WTF !!! OperationManagerPool ", exception);
		}		
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			OperationManagerPool.instance().destroy();
		} catch (Exception exception) {
			LoggerFactory.getLogger(getClass())
				.error("------!!! WTF !!! Fail to destroy OperationManagerPool ", exception);;
		}		
	}
}