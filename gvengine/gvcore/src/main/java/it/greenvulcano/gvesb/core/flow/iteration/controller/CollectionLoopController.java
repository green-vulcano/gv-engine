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
package it.greenvulcano.gvesb.core.flow.iteration.controller;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.flow.iteration.LoopController;

/**
 * A {@link LoopController} implementation that handle well-formed {@link Collection} iterating over its elements.
 *
 * The results are returned in a {@link Collection}
 * 
 * @version 4.0.0 20160603
 * @author GreenVulcano Developer Team
 * 
 */
public class CollectionLoopController extends BaseLoopController {
	
	private GVBuffer inputBuffer;

	@Override
	protected GVBuffer doLoop(GVBuffer inputCollection) throws GVException {
		inputBuffer = inputCollection;
		if ( inputCollection.getObject() instanceof Collection<?>) {
			Collection<?> inputCollectionData = (Collection<?>) inputCollection.getObject();
			
			Collection<Object> outputCollection = inputCollectionData.stream()
																	  .map(this::buildLoopGVBuffer)
																	  .filter(Optional::isPresent)
																	  .map(Optional::get)
																	  .map(this::performAction)
																	  .map(GVBuffer::getObject)
																	  .collect(Collectors.toList());
			
			GVBuffer outputData = new GVBuffer(inputCollection, false);
			outputData.setObject(outputCollection);
			return outputData;
			
		} else {
			throw new GVException("GVCORE_UNPARSABLE_COLLECTION", new String[][]{{"name", "'collection-type'"},
                {"object", "" + inputCollection.getObject()}});
		}
	}	
		
	private Optional<GVBuffer> buildLoopGVBuffer(Object o) {
			
		GVBuffer itemData = null;
		try {
			itemData = new GVBuffer(inputBuffer, false);
			itemData.setObject(o);
		} catch (Exception e) {
			LOG.error("Exception on GVBuffer creation ", e);
		}
		return Optional.ofNullable(itemData);		
	}		

}
