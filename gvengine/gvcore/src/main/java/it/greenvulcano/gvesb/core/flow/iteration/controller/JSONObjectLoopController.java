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

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import org.json.JSONObject;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.flow.iteration.LoopController;

/**
 * A {@link LoopController} implementation that handle a JSON iterating over key.
 *
 * The results are returned in a {@link JSONObject} using the same keys found in the input object
 * 
 * @version 4.0.0 20160603
 * @author GreenVulcano Developer Team
 * 
 */
public class JSONObjectLoopController extends BaseLoopController {
	protected static final String GV_LOOP_KEY = "GV_LOOP_KEY";
	
	private GVBuffer inputBuffer;
	private JSONObject jsonObject = null; 
		
	@Override
	protected GVBuffer doLoop(GVBuffer inputCollection) throws GVException{
		inputBuffer = inputCollection;
		jsonObject = parseGVBuffer(inputCollection).orElseThrow(() -> {
			return new GVException("GVCORE_UNPARSABLE_JSON_DATA", new String[][]{{"name", "'collection-type'"},
                {"object", "" + inputCollection.getObject()}});
		});
		
		GVBuffer outputData = new GVBuffer(inputCollection, false);
		
		JSONObject result = jsonObject.keySet()
					.stream()
					.map(this::buildLoopGVBuffer)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.map(this::performAction)
					.reduce(new JSONObject(), this::bindResults,  this::mergeResults);
		
		outputData.setObject(result);			
					
		return outputData;
	}
	
	private Optional<GVBuffer> buildLoopGVBuffer(String key) {
		
			GVBuffer itemData = null;
			try {
				
				itemData = new GVBuffer(inputBuffer, false);
				
				itemData.setProperty(GV_LOOP_KEY, key);
				itemData.setObject(jsonObject.get(key));
			} catch (Exception e) {
				LOG.error("Exception on GVBuffer creation ", e);
			}
			return Optional.ofNullable(itemData);		
	}
	
	private Optional<JSONObject> parseGVBuffer(GVBuffer data){
		JSONObject json = null;
		try {
			if (Objects.isNull(data.getObject())) {
				return Optional.empty();
			} else if (data.getObject() instanceof byte[]) {
			        json = new JSONObject(new String((byte[])data.getObject(), StandardCharsets.UTF_8));
			} else if (data.getObject() instanceof JSONObject) {
				json = JSONObject.class.cast(data.getObject());
			} else if (data.getObject() instanceof String) {
				json = new JSONObject(data.getObject().toString());
			} else {
				json = new JSONObject(data.getObject());
			}
		} catch (Exception e) {
			LOG.error("Invalid json data "+data.getObject(), e);
		}
		
		return Optional.ofNullable(json);
	}
	
	protected JSONObject bindResults(JSONObject jsonResult, GVBuffer data){
		try {
			Optional<JSONObject> responseData = parseGVBuffer(data);
			if (responseData.isPresent()) {				
				jsonResult.put(data.getProperty(GV_LOOP_KEY), responseData.get());
			} else {
				jsonResult.put(data.getProperty(GV_LOOP_KEY), data.getObject());
			}
		} catch (Exception e) {
			LOG.error("Invalid json data ", e);
		}
		
		return jsonResult;
	}
	
	protected JSONObject mergeResults(JSONObject jsonResult, JSONObject otherJsonResult) {
		JSONObject merged = new JSONObject(jsonResult, JSONObject.getNames(jsonResult));
		otherJsonResult.keySet().stream().forEach(k-> merged.put(k, otherJsonResult.get(k)));
		return merged;
	}

}
