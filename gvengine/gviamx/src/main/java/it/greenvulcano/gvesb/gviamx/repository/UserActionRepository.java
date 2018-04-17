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
package it.greenvulcano.gvesb.gviamx.repository;

import java.util.Optional;
import java.util.Set;

import it.greenvulcano.gvesb.gviamx.domain.UserActionRequest;

public interface UserActionRepository {
	
	Optional<UserActionRequest> get(Long key);
	
	<T extends UserActionRequest> Optional<T> get(String email, Class<T> type);
	
	void add(UserActionRequest entity);
	
	Set<UserActionRequest> getAll();
	
	void remove(UserActionRequest entity);	

}
