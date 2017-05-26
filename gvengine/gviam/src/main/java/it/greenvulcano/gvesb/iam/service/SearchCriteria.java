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
package it.greenvulcano.gvesb.iam.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SearchCriteria {
	
	private final Map<String, Object> parameters = new HashMap<>(); 
	private final LinkedHashMap<String, String> order = new LinkedHashMap<>() ;
	private int offset, limit;
	
	public SearchCriteria() {
		this.offset = 0;
		this.limit = 1024; 
	}
	
	public SearchCriteria(int offset, int limit) {
		this.offset = offset;
		this.limit = limit; 
	}
	
	public Map<String, Object> getParameters() {
		return parameters;
	}
	public LinkedHashMap<String, String> getOrder() {
		return order;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		final SearchCriteria c;
		
		Builder() {
			this.c = new SearchCriteria();
		}		
		
		public Builder offsetOf(int offset) {
			c.setOffset(offset);
			return this;
		}
		
		public Builder limitedTo(int limit) {
			c.setLimit(limit);
			return this;
		}
		
		public Builder byUsername(String username){
			c.getParameters().put("username", username);
			return this;
		}
		
		public Builder byEmail(String email){
			c.getParameters().put("email", email);
			return this;
		}
		
		public Builder byFullname(String fullname){
			c.getParameters().put("fullname", fullname);
			return this;			
		}
		
		public Builder havingRole(String role){
			c.getParameters().put("role", role);
			return this;
		}
		
		public Builder isEnabled(){ 
			c.getParameters().put("enabled", Boolean.TRUE);
			return this;
		}
		
		public Builder isNotEnabled(){ 
			c.getParameters().put("enabled", Boolean.FALSE);
			return this;
		}
		
		public Builder isExpired() {
			c.getParameters().put("expired", Boolean.TRUE);
			return this;
		}
		
		public Builder isNotExpired() {
			c.getParameters().put("expired", Boolean.FALSE);
			return this;
		}
		
		public Builder orderByEmail() {
			c.getOrder().put("email", "asc");
			return this;
		}
		
		public Builder orderByFullname() {
			c.getOrder().put("fullname", "asc");
			return this;
		}
		
		public Builder orderByExpiration() {
			c.getOrder().put("expired", "asc");
			return this;
		}
		
		public Builder orderByEnablement() {
			c.getOrder().put("enabled", "asc");
			return this;
		}
		
		public SearchCriteria build() {
			return c;
		}
		
	}	
	
	
}