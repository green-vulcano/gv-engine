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
package it.greenvulcano.gvesb.iam.modules;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Identity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final String name;
	private final Set<String> roles;
	
	protected Identity(){
		this.name = "";
		this.roles = new LinkedHashSet<>();
	};
	
	public Identity(String name, Set<String> roles) {
		this.name = name;
		if (Objects.nonNull(roles)) {
			this.roles = Collections.unmodifiableSet(roles);
		} else {
			this.roles = Collections.unmodifiableSet(new LinkedHashSet<>());
		}
	}
	
	public Identity(String name, String ... roles){
		this.name = name;
		
		Set<String> r;
		if (Objects.nonNull(roles)) {
			r = Stream.of(roles).collect(Collectors.toSet());
		} else {
			r = new LinkedHashSet<>();
		}
	
		this.roles = Collections.unmodifiableSet(r);
		
	}
	
	
	public String getName(){
		return name;
	}
	
	public Set<String> getRoles(){
		return roles;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Identity other = (Identity) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (roles == null) {
			if (other.roles != null)
				return false;
		} else if (!roles.equals(other.roles))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "{\"name\":\"" + name + "\", roles\":" + roles.stream().map(r->String.format("\"%s\"")).collect(Collectors.joining(",", "[", "]")) + "}";
	}	

}
