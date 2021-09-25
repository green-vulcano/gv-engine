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
package it.greenvulcano.util.metadata.properties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.metadata.PropertyHandler;

public class JSONPropertiesHandler implements PropertyHandler {

    private final static Logger LOG = LoggerFactory.getLogger(JSONPropertiesHandler.class);
    private final List<String> types = Collections.unmodifiableList(Arrays.asList("json"));

    @Override
    public List<String> getManagedTypes() {

        return types;
    }

    @Override
    public void cleanupResources() {
    	// do nothing
    }

    @Override
    public String expand(String type, String str, Map<String, Object> inProperties, Object object, Object extra) throws PropertiesHandlerException {

  
        String propName, value;
        if (str.matches("^.+::.+$")) {
            String[] values = str.split("::");
            propName = values[0];
            value = values[1];
            
            if (!PropertiesHandler.isExpanded(value)) {
                value = PropertiesHandler.expand(value, inProperties, object, extra);
            }
            
        } else {
            propName = str;
            value = "json" + PROP_START + str + PROP_END;
        }
        
        if (!PropertiesHandler.isExpanded(propName)) {
            propName = PropertiesHandler.expand(propName, inProperties, object, extra);
        }
                
        Function<Object, Optional<JSONObject>> objProvider = (Object o) -> {
            JSONObject jsonObject;
            try {

                if (o instanceof String) {
                    if (o.toString().trim().startsWith("{")) {
                        jsonObject = new JSONObject(o.toString().trim());
                    } else if (o.toString().trim().startsWith("[")) {
                        jsonObject = new JSONObject("{ \"array\":" + o.toString().trim() + "}");
                    } else {
                        jsonObject = new JSONObject("{ \"value\":\"" + o.toString().trim() + "\"}");
                    }
                } else if (o instanceof JSONArray) {
                    jsonObject = new JSONObject("{ \"array\":" + o.toString().trim() + "}");

                } else if (o instanceof JSONObject) {
                    jsonObject = JSONObject.class.cast(o);
                } else {
                    jsonObject = new JSONObject(o);
                }
            } catch (Exception e) {
                return Optional.empty();
            }

            return Optional.of(jsonObject);
        };

        try {
            JSONObject jsonObject;

            if (object instanceof GVBuffer) {
                return expand(type, str, inProperties, GVBuffer.class.cast(object).getObject(), extra);
            } else if (object instanceof byte[]) {
                String rawdata = new String((byte[]) object, "UTF-8");
                jsonObject = objProvider.apply(rawdata).orElseThrow(IllegalArgumentException::new);
            } else {
                jsonObject = objProvider.apply(object).orElseThrow(IllegalArgumentException::new);
            }           

            if ("this".equals(propName)) {
                value = jsonObject.toString();
            } else if (propName.contains(".")) {
                List<String> hieararchy = Arrays.asList(propName.split("\\."));
                for (String prop : hieararchy) {
                    Object child = null;
                    if (prop.matches("^.*\\[[0-9]+\\]")) {
                        String[] arrayRef = prop.split("[\\[,\\]]");
                        child = jsonObject.getJSONArray(arrayRef[0]).get(Integer.valueOf(arrayRef[1]));
                    } else {
                        child = jsonObject.get(prop);
                    }

                    if (child instanceof JSONObject) {
                        jsonObject = JSONObject.class.cast(child);
                    } else {
                        value = child.toString();
                        break;
                    }
                }
            } else {
                if (propName.matches("^.*\\[[0-9]+\\]")) {
                    String[] arrayRef = propName.split("[\\[,\\]]");
                    value = jsonObject.getJSONArray(arrayRef[0]).get(Integer.valueOf(arrayRef[1])).toString();
                } else {
                    value = jsonObject.get(propName).toString();
                }
            }

        } catch (Exception e) {
            LOG.debug("Failed to parse '%s' json path, returned value is %s", str, value);
        }

        return value;
    }

}