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
