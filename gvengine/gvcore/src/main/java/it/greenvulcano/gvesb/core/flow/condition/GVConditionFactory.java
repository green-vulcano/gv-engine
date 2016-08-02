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
package it.greenvulcano.gvesb.core.flow.condition;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import it.greenvulcano.gvesb.identity.condition.IdentityCondition;
import it.greenvulcano.gvesb.internal.condition.ExceptionCondition;
import it.greenvulcano.gvesb.internal.condition.GVBufferCondition;
import it.greenvulcano.gvesb.internal.condition.GVCondition;
import it.greenvulcano.gvesb.internal.condition.ScriptCondition;

/**
 * A static factory of know GVCondition implementations 
 * mapped by class name
 *
 *
 */
public final class GVConditionFactory {
	
	private final static Map<String, Supplier<GVCondition>> suppliers;
	
	static {
		suppliers = new HashMap<>();
		suppliers.put("ScriptCondition", ScriptCondition::new);
		suppliers.put("GVBufferCondition", GVBufferCondition::new);
		suppliers.put("ExceptionCondition", ExceptionCondition::new);
		suppliers.put("IdentityCondition", IdentityCondition::new);
	}
	
	private GVConditionFactory(){}
	
	/**
	 * Create a GVCondition using the implementation identified by 
	 * the specified {@code coditionName}
	 * 
	 * @param conditionName The implementation identifier
	 * @return An GVCondition instance
	 * 
	 * @throws NoSuchElementException Specified implementation not found  
	 *  
	 */	
	public static GVCondition make(String conditionName) {
		return Optional.ofNullable(suppliers.get(conditionName))
					   .orElseThrow(NoSuchElementException::new)
					   .get();
	}
	

}
