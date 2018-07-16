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
package it.greenvulcano.gvesb.api.security;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.cxf.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.identity.impl.BaseIdentityInfo;

public class JaxWsIdentityInfo extends BaseIdentityInfo {

	private static final Logger logger = LoggerFactory.getLogger(JaxWsIdentityInfo.class);
	
	private final SecurityContext securityContext;
	private final String remoteAddress;	

	public JaxWsIdentityInfo(WebServiceContext securityContext) {
		super();
		
		HttpServletRequest request = (HttpServletRequest) securityContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
						
		this.securityContext = (SecurityContext)securityContext.getMessageContext().get(SecurityContext.class.getName());
		this.remoteAddress =  request!=null?request.getRemoteAddr():null;
	}

	@Override
	public String getName() {		
		Principal p = securityContext.getUserPrincipal();
		return (p != null ? p.getName() : "NONE");
	}

	@Override
	protected boolean subIsInRole(String role) {
		if (role == null) {
			return false;
		}
		boolean res = securityContext.isUserInRole(role);
		if (debug) {
			logger.debug("JaxWsIdentityInfo[" + getName() + "]: Role[" + role + "] -> " + res);
		}
		return res;
	}

	@Override
	protected boolean subMatchAddress(String address) {
		if (address == null) {
			return false;
		}
		boolean res = address.equals(remoteAddress);
		if (debug) {
			logger.debug("JaxWsIdentityInfo[" + getName() + "]: Address[" + address + ": " + remoteAddress + "] -> " + res);
		}
		return res;
	}

	@Override
	protected boolean subMatchAddressMask(String addressMask) {
		boolean matches = false;
		
		if (addressMask != null) {
		
			SubnetUtils subnet = new SubnetUtils(addressMask);
	        subnet.setInclusiveHostCount(true);  
			
	        matches = subnet.getInfo().isInRange(remoteAddress);
			if (debug) {
				logger.debug("JaxRsIdentityInfo[" + getName() + "]: AddressMask[" + subnet.getInfo().getCidrSignature() + ": "
						+ remoteAddress + "] -> " + matches);
			}
		
		}
		return matches;
	}

	@Override
	public String toString() {
		return "JaxWsIdentityInfo[" + getName() + "]";
	}

}