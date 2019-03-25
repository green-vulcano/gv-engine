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
package it.greenvulcano.gvesb.iam.domain;

import java.util.Date;

/**
 * Represent a credentials to be validated in a security context 
 *
 * @author GreenVulcano Technologies
 *
 */
public abstract class Credentials {
	
	public abstract String getAccessToken();		
	public abstract String getRefreshToken();		
	public abstract Date getIssueTime();		 
	public abstract Long getLifeTime();
	public abstract User getClient();	
	public abstract User getResourceOwner();	
	public abstract boolean isValid();

}
