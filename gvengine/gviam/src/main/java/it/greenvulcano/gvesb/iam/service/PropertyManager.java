/*******************************************************************************
 * Copyright (c) 2009, 2020 GreenVulcano ESB Open Source Project.
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

import java.util.Optional;
import java.util.Set;

import it.greenvulcano.gvesb.iam.domain.Property;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;

/**
 * Manage a simple key/value store 
 * 
 */
public interface PropertyManager {
    
    public void store(String key, String value, String user, String address) throws UserNotFoundException;
    
    public Optional<Property> retrieve(String key);
    
    public Set<Property> retrieveAll();
    
    public void delete(String key);
}
