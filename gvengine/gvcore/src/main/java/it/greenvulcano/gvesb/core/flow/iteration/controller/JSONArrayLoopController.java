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

import java.util.Optional;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.flow.iteration.LoopController;

/**
 * A {@link LoopController} implementation to iteratw over a json array.
 *
 * The results are returned in a {@link JSONArray} using the same order in the input object
 * 
 * @version 4.0.0 20160606
 * @author GreenVulcano Developer Team
 * 
 */
public class JSONArrayLoopController extends JSONObjectLoopController {
	
	private GVBuffer inputBuffer;
	private JSONArray jsonArray;
	
	@Override
	protected GVBuffer doLoop(GVBuffer inputCollection) throws GVException {
		inputBuffer = inputCollection;
		jsonArray = parseGVBuffer(inputCollection).orElseThrow(() -> {
			return new GVException("GVCORE_UNPARSABLE_JSON_DATA", new String[][]{{"name", "'collection-type'"},
                {"object", "" + inputCollection.getObject()}});
		});
		
		JSONObject result = IntStream.range(0, jsonArray.length())
					.mapToObj(this::buildLoopGVBuffer)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.map(this::performAction)
					.reduce(new JSONObject(), this::bindResults,  this::mergeResults);
		
		JSONArray resultArray = new JSONArray();
		
		result.keySet().stream().sorted().forEach(key->resultArray.put( Integer.valueOf(key), result.get(key)));
					
		GVBuffer outputData = new GVBuffer(inputCollection, false);
		outputData.setObject(resultArray);	
		return outputData;
	}
	
	
	private Optional<JSONArray> parseGVBuffer(GVBuffer gvBuffer) {
		JSONArray jsonArray =  null;
		try {
			if (gvBuffer.getObject() instanceof JSONArray) {
				jsonArray = (JSONArray) gvBuffer.getObject();			
			} else if (gvBuffer.getObject() instanceof String)  {
				jsonArray = new JSONArray(gvBuffer.getObject().toString());
			} else {
				jsonArray = (JSONArray) JSONObject.wrap(gvBuffer.getObject());
			}
			
		} catch (Exception e) {
			LOG.error("Invalid json array "+gvBuffer.getObject(), e);
		}
		
		return Optional.ofNullable(jsonArray);
	}
	
	private Optional<GVBuffer> buildLoopGVBuffer(int index) {
		
		GVBuffer itemData = null;
		try {
			itemData = new GVBuffer(inputBuffer, false);			
			itemData.setProperty(GV_LOOP_KEY, Integer.toString(index));
			itemData.setObject(jsonArray.get(index));
		} catch (Exception e) {
			LOG.error("Exception on GVBuffer creation ", e);
		}
		return Optional.ofNullable(itemData);		
	}	

}
