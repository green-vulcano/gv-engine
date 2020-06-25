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
package it.greenvulcano.gvesb.virtual.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.virtual.OperationFactory;
import it.greenvulcano.gvesb.virtual.internal.json.JSONValidationCallOperation;
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
		OperationFactory.registerSupplier("json-validation-call", JSONValidationCallOperation::new);
	
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		OperationFactory.unregisterSupplier("proxy-call");
		OperationFactory.unregisterSupplier("script-call");
		OperationFactory.unregisterSupplier("xml-validation-call");
		OperationFactory.unregisterSupplier("gvdte-context-call");
		OperationFactory.unregisterSupplier("test-service-call");
		OperationFactory.unregisterSupplier("json-validation-call");
		
		
		LoggerFactory.getLogger(getClass()).debug("*********** VCL-Internal Stopped ");	
	}
}