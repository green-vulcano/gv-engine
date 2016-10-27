package it.greenvulcano.gvesb.ws.axis2;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.impl.Axis2MessageContextDataProvider;
import it.greenvulcano.gvesb.virtual.OperationFactory;
import it.greenvulcano.gvesb.virtual.http.HTTPCallOperation;
import it.greenvulcano.gvesb.virtual.ws.WSCallOperation;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		LoggerFactory.getLogger(getClass()).debug("*********** VCL Axis2 Up&Running ");
		
		DataProviderManager.registerSupplier("Axis2MessageContextDataProvider", Axis2MessageContextDataProvider::new);
		
		OperationFactory.registerSupplier("ws-call", WSCallOperation::new);
		OperationFactory.registerSupplier("http-call", HTTPCallOperation::new);
			

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LoggerFactory.getLogger(getClass()).debug("*********** VCL Axis2 stopped ");
		
		DataProviderManager.unregisterSupplier("Axis2MessageContextDataProvider");
		
		OperationFactory.unregisterSupplier("ws-call");
		OperationFactory.unregisterSupplier("http-call");
		
	}

}
