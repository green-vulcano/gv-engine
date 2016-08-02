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
package it.greenvulcano.gvesb.osgi.commands;


import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import it.greenvulcano.gvesb.osgi.bus.BusLink;

@Command(scope = "gvesb", name = "vulcon-link", description = "Connect this instance of GreenVulcano ESB to VulCon.io")
@Service
public class GVConnector implements Action {
	
	@Argument(name = "apikey", description = "A VulCon.io API key", required = false, multiValued = false)
	private String uuid;

	@Reference
	private BusLink busLink;
	
	public void setBusConnector(BusLink busLink) {
		this.busLink = busLink;
	}   
	
	@Override
	public Object execute() throws Exception {
		String message = "Connecting to Vulcon.io";
		if (uuid==null || uuid.trim().equals("")) {
			message = "An API key is required";
		} else {
			message = busLink.connect(uuid);
		}
		return message;
	}

}
