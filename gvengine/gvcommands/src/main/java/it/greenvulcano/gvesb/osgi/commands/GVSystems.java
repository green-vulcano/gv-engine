/* Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
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
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;

@Command(scope = "gvesb", name = "system-list", description = "It provides the list of systems")
@Service
public class GVSystems implements Action {
	
	@Argument(index=0, name = "system", description = "It allows to retrieve a single system",
			required = false, multiValued = false)
	String system = null;
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Override
	public Object execute() throws Exception {
		String message = null;		
		
		try {
			
			if (system == null) {
				
				NodeList systemNodes = XMLConfig.getNodeList("GVSystems.xml", "//System");
				
				for (int i=0; i < systemNodes.getLength(); i++) {
					
					Node node = (Node)systemNodes.item(i);
					NamedNodeMap attributes = node.getAttributes();
					
					String idSystem = attributes.getNamedItem("id-system").getNodeValue();
					
					System.out.println("--------------------------------------");
					System.out.println("System: ");
					System.out.println("id-system: " + idSystem);
					System.out.println(" ");
					System.out.println("Channels: ");
					
					Node systemNode = XMLConfig.getNode("GVSystems.xml", "//System[@id-system='" + idSystem + "']");
					NodeList channelNodes = XMLConfig.getNodeList(systemNode, "./Channel");
					
					for (int j=0; j < channelNodes.getLength(); j++) {
						Node channelNode = channelNodes.item(j);
						NamedNodeMap channelAttrs = channelNode.getAttributes();
						String idChannel = channelAttrs.getNamedItem("id-channel").getNodeValue();
						
						System.out.println("Channel: " + idChannel);			
					}
				}
				
				LOG.debug("Systems found "+systemNodes.getLength());
				message = systemNodes.getLength() + " systems found";
				
			} else {
				
				System.out.println("--------------------------------------");
				System.out.println("System: " + system);
				System.out.println(" ");
				System.out.println("Channels: ");
				
				Node systemNode = XMLConfig.getNode("GVSystems.xml", "//System[@id-system='" + system + "']");
				NodeList channelNodes = XMLConfig.getNodeList(systemNode, "./Channel");
				
				for (int j=0; j < channelNodes.getLength(); j++) {
					Node channelNode = channelNodes.item(j);
					NamedNodeMap channelAttrs = channelNode.getAttributes();
					
					String idChannel = channelAttrs.getNamedItem("id-channel").getNodeValue();
					String type = channelAttrs.getNamedItem("type").getNodeValue();
					String endPoint = channelAttrs.getNamedItem("endpoint").getNodeValue();
					
					System.out.println(" ");
					System.out.println("id-channel: " + idChannel);
					System.out.println("type: " + type);
					System.out.println("endpoint: " + endPoint);
					System.out.println(" ");
					System.out.println("Operations: ");
					System.out.println(" ");
					
					NodeList operationNode = XMLConfig.getNodeList(channelNode, "./*");
					
					for (int i=0; i < operationNode.getLength(); i++) {
						Node node = operationNode.item(i);
						NamedNodeMap attributes = node.getAttributes();
						 
						for (j=0; j < attributes.getLength(); j++) {
							String nameAttr = attributes.item(j).getNodeName();
							String valueAttr = attributes.getNamedItem(nameAttr).getNodeValue();
							System.out.println(nameAttr + ": " + valueAttr);
						}
						
						System.out.println(" ");
					}
				}
			}
			
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
			LOG.error("GVSystemList - System List failed", exception);
			message = "Fail to show system list";
		}		
		
		return message;
	}

}