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
package it.greenvulcano.gvesb.ws.axis2;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.impl.Axis2MessageContextDataProvider;
import it.greenvulcano.gvesb.gvhttp.impl.HttpMethodDataProvider;
import it.greenvulcano.gvesb.virtual.OperationFactory;
import it.greenvulcano.gvesb.virtual.http.HTTPCallOperation;
import it.greenvulcano.gvesb.virtual.ws.WSCallOperation;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		LoggerFactory.getLogger(getClass()).debug("*********** VCL Axis2 Up&Running ");
		
		DataProviderManager.registerSupplier("Axis2MessageContextDataProvider", Axis2MessageContextDataProvider::new);
		DataProviderManager.registerSupplier("HttpMethodDataProvider", HttpMethodDataProvider::new);
		
		
		OperationFactory.registerSupplier("ws-call", WSCallOperation::new);
		OperationFactory.registerSupplier("http-call", HTTPCallOperation::new);
			

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LoggerFactory.getLogger(getClass()).debug("*********** VCL Axis2 stopped ");
		
		DataProviderManager.unregisterSupplier("Axis2MessageContextDataProvider");
		DataProviderManager.unregisterSupplier("HttpMethodDataProvider");
		
		OperationFactory.unregisterSupplier("ws-call");
		OperationFactory.unregisterSupplier("http-call");
		
	}

}
