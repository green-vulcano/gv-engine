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
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;

@Command(scope = "gvesb", name = "services", description = "It allows to retrieve services")
@Service
public class GVServices implements Action {
	
	@Argument(index=0, name = "service", description = "It allows to retrieve a single service",
			required = false, multiValued = false)
	String service = null;
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Override
	public Object execute() throws Exception {
		String message = null;		
		
		try {
			
			if (service == null) {
				
				NodeList serviceNodes = XMLConfig.getNodeList("GVServices.xml", "//Service");
				
				for (int i=0; i < serviceNodes.getLength(); i++) {
					
					Node node = (Node)serviceNodes.item(i);
					NamedNodeMap attributes = node.getAttributes();
					
					String idService = attributes.getNamedItem("id-service").getNodeValue();
					String groupName = attributes.getNamedItem("group-name").getNodeValue();
					
					System.out.println("--------------------------------------");
					System.out.println("Service: ");
					System.out.println('\t'+"id-service: " + idService);
					System.out.println('\t'+"group-name: " + groupName);
					System.out.println(" ");
					System.out.println("Operations: ");
					
					Node serviceNode = XMLConfig.getNode("GVServices.xml", "//Service[@id-service='" + idService + "']");
					NodeList operationNodes = XMLConfig.getNodeList(serviceNode, "./Operation");
					
					for (int j=0; j < operationNodes.getLength(); j++) {
						Node oper = operationNodes.item(j);
						NamedNodeMap OperAttrs = oper.getAttributes();
						String idOper = OperAttrs.getNamedItem("name").getNodeValue();
						
						System.out.println('\t'+"name: " + idOper);
						
					}
				}
				
				LOG.debug("Services found "+serviceNodes.getLength());
				message = serviceNodes.getLength() + " services found";
				
			} else {
					
					System.out.println("--------------------------------------");
					System.out.println("Service: " + service);
					System.out.println(" ");
					System.out.println("Operations: ");
					System.out.println(" ");
					
					Node serviceNode = XMLConfig.getNode("GVServices.xml", "//Service[@id-service='" + service + "']");
					NodeList operationNodes = XMLConfig.getNodeList(serviceNode, "./Operation");
					
					for (int j=0; j < operationNodes.getLength(); j++) {
						Node oper = operationNodes.item(j);
						NamedNodeMap operAttrs = oper.getAttributes();
						
						for (int i = 0; i<operAttrs.getLength(); i++) {
							Node attribute = operAttrs.item(i);
							System.out.println('\t'+attribute.getNodeName()+": " + attribute.getNodeValue());
						}
		
						
					}
			}
			
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
			LOG.error("GVServices - Retrieve services failed", exception);
			message = "Fail to retireve services";
		}		
		
		return message;
	}

}
