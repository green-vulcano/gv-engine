package it.greenvulcano.gvesb.datahandling;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.virtual.OperationFactory;
import it.greenvulcano.gvesb.virtual.datahandler.DBOBuilderSelectorCallOperation;
import it.greenvulcano.gvesb.virtual.datahandler.DataHandlerCallOperation;

public class Activator implements BundleActivator {

	private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
	
	@Override
	public void start(BundleContext context) throws Exception {		
		OperationFactory.registerSupplier("dh-call", DataHandlerCallOperation::new);
		OperationFactory.registerSupplier("dh-selector-call", DBOBuilderSelectorCallOperation::new);
		LOG.debug("*********** GV DataHandler Up&Runnig");
	}

	@Override
	public void stop(BundleContext context) throws Exception {		
		OperationFactory.unregisterSupplier("dh-call");
		OperationFactory.unregisterSupplier("dh-selector-call");
		LOG.debug("*********** GV DataHandler Stopped");
	}

}
