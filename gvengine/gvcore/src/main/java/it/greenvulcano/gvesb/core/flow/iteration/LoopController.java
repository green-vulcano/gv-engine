/*
 * Copyright (c) 2009-2016 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.core.flow.iteration;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;

/**
 * A contract to hadle the iteration logic over a {@link GVBuffer}.
 * 
 * 
 * @version 4.0.0 20160603
 * @author GreenVulcano Developer Team
 * 
 */
public interface LoopController {
	
	/**
	 * Inventory of all {@link LoopController} implementation
	 * 
	 *
	 */
	enum Type { JAVA_COLLECTION("javaCollection"), 
		JSON_OBJECT("jsonObject"), 
		JSON_ARRAY("jsonArray"), 
		XML_NODE("xml");

		private static final Map<String, Type> typesMap;

		static {
			typesMap = Arrays.stream(values()).collect(Collectors.toMap(Type::getId, Function.identity()));			 
		}

		static Optional<Type> getById(String id) {		    
			if (Objects.nonNull(id)) {
				return Optional.ofNullable(typesMap.get(id));
			} else {
				return Optional.empty();
			}

		}

		private final String id;		

		private Type(String id) {
			this.id = id;
		}

		String getId() {
			return id;
		}

	}
	
	GVBuffer executeLoop(GVBuffer inputData, boolean onDebug) throws GVException;

}
