package it.greenvulcano.gvesb.virtual.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.virtual.OperationFactory;
import it.greenvulcano.gvesb.virtual.internal.xml.XMLValidationCallOperation;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		LoggerFactory.getLogger(getClass()).debug("*********** VCL-Internal Up&Running ");

		OperationFactory.registerSupplier("proxy-call", ProxyCallOperation::new);
		OperationFactory.registerSupplier("script-call", ScriptCallOperation::new);
		OperationFactory.registerSupplier("xml-validation-call", XMLValidationCallOperation::new);
		OperationFactory.registerSupplier("gvdte-context-call", DTEServiceContextCall::new);
		OperationFactory.registerSupplier("test-service-call", TestServiceCall::new);
	
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		OperationFactory.unregisterSupplier("proxy-call");
		OperationFactory.unregisterSupplier("script-call");
		OperationFactory.unregisterSupplier("xml-validation-call");
		OperationFactory.unregisterSupplier("gvdte-context-call");
		OperationFactory.unregisterSupplier("test-service-call");
		
		
		LoggerFactory.getLogger(getClass()).debug("*********** VCL-Internal Stopped ");	
	}
}