/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
