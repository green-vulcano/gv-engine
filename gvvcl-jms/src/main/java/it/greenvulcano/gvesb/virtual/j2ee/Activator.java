package it.greenvulcano.gvesb.virtual.j2ee;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.impl.JMSBytesMessageDataProvider;
import it.greenvulcano.gvesb.gvdp.impl.JMSMapMessageDataProvider;
import it.greenvulcano.gvesb.gvdp.impl.JMSObjectMessageDataProvider;
import it.greenvulcano.gvesb.gvdp.impl.JMSStreamMessageDataProvider;
import it.greenvulcano.gvesb.gvdp.impl.JMSTextMessageDataProvider;
import it.greenvulcano.gvesb.virtual.OperationFactory;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		
		LoggerFactory.getLogger(getClass()).debug("*********** VCL JMS Up&Running ");
		
		DataProviderManager.registerSupplier("JMSBytesMessageDataProvider", JMSBytesMessageDataProvider::new);
		DataProviderManager.registerSupplier("JMSMapMessageDataProvider", JMSMapMessageDataProvider::new);
		DataProviderManager.registerSupplier("JMSObjectMessageDataProvider", JMSObjectMessageDataProvider::new);
		DataProviderManager.registerSupplier("JMSStreamMessageDataProvider",JMSStreamMessageDataProvider::new);
		DataProviderManager.registerSupplier("JMSTextMessageDataProvider", JMSTextMessageDataProvider::new);
		
		OperationFactory.registerSupplier("jms-enqueue", JMSEnqueueOperation::new);
		OperationFactory.registerSupplier("jms-dequeue", JMSDequeueOperation::new);

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		DataProviderManager.unregisterSupplier("JMSBytesMessageDataProvider");
		DataProviderManager.unregisterSupplier("JMSMapMessageDataProvider");
		DataProviderManager.unregisterSupplier("JMSObjectMessageDataProvider");
		DataProviderManager.unregisterSupplier("JMSStreamMessageDataProvider");
		DataProviderManager.unregisterSupplier("JMSTextMessageDataProvider");
		
		OperationFactory.unregisterSupplier("jms-enqueue");
		OperationFactory.unregisterSupplier("jms-dequeue");
		
		LoggerFactory.getLogger(getClass()).debug("*********** VCL JMS Stopped ");

	}

}
