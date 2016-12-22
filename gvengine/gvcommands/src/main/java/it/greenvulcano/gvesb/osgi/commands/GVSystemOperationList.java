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
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;

@Command(scope = "gvesb", name = "system-operation-list", description = "It provides the list of operation for a specified system")
@Service
public class GVSystemOperationList implements Action {
	
	@Option(name = "-sys", aliases = "--system", description = "The option for choice the system",
			required = true, multiValued = false)
	String sys = null;
	
	@Option(name = "-ch", aliases = "--channel", description = "The option for choice the channel",
			required = true, multiValued = false)
	String channel = null;
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Override
	public Object execute() throws Exception {
		String message = null;		
		
		try {		
			Boolean check = false;
			
			Node systemNode = XMLConfig.getNode("GVSystems.xml", "//System[@id-system='" + sys + "']");
			
			Node channelNode = XMLConfig.getNode(systemNode, "./Channel[@id-channel='" + channel + "']");
			
			NodeList operationNode = XMLConfig.getNodeList(channelNode, "./*");
			
			if (operationNode.getLength() != 0) {
				check = true;
			}
			
			for (int i=0; i < operationNode.getLength(); i++) {
				Node node = operationNode.item(i);
				NamedNodeMap attributes = node.getAttributes();
				String idOperation = attributes.getNamedItem("name").getNodeValue();
				System.out.println("Operation: " + idOperation);
				
			}
			
			if (check == true) {
				LOG.debug("Operations found");
				message = "Operations found";
			} else {
				LOG.debug("Operations not found");
				message = "Operations not found";
			}
			
			
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
			LOG.error("GVSystemOperationList - Operation retrieve failed", exception);
			message = "Fail to retrieve operation";
		}		
		
		return message;
	}

}