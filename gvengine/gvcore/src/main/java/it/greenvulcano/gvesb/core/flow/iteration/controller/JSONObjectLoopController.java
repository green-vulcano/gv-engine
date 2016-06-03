package it.greenvulcano.gvesb.core.flow.iteration.controller;

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
	public static final String GV_LOOP_KEY = "GV_LOOP_KEY";
	private JSONObject jsonObject = null; 
	
	@Override
	protected GVBuffer doLoop(GVBuffer inputCollection) throws GVException{
		
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
				itemData = new GVBuffer();
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
			if (data.getObject() instanceof JSONObject) {
				json = JSONObject.class.cast(data.getObject());
			} else if (data.getObject() instanceof String) {
				json = new JSONObject(data.getObject().toString());
			} else {
				json = new JSONObject(data.getObject());
			}
		} catch (Exception e) {
			LOG.error("Invalid json data ", e);
		}
		
		return Optional.ofNullable(json);
	}
	
	private JSONObject bindResults(JSONObject jsonResult, GVBuffer data){
		try {
			Optional<JSONObject> jsonData = parseGVBuffer(data);
			if (jsonData.isPresent()) {
				jsonResult.put(data.getProperty(GV_LOOP_KEY), jsonData.get());
			}
		} catch (Exception e) {
			LOG.error("Invalid json data ", e);
		}
		
		return jsonResult;
	}
	
	private JSONObject mergeResults(JSONObject jsonResult, JSONObject otherJsonResult) {
		JSONObject merged = new JSONObject(jsonResult, JSONObject.getNames(jsonResult));
		otherJsonResult.keySet().stream().forEach(k-> merged.put(k, otherJsonResult.get(k)));
		return merged;
	}

}
