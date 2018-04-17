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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.script.ScriptExecutor;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.metadata.PropertyHandler;
import it.greenvulcano.util.txt.TextUtils;

public class ScriptPropertiesHandler implements PropertyHandler {

	private final static Logger LOGGER = LoggerFactory.getLogger(ScriptPropertiesHandler.class);
	private final static List<String> types = Collections.unmodifiableList(Arrays.asList("script","js","ognl"));

	@Override
	public List<String> getManagedTypes() {
		return types;
	}

	@Override
	public String expand(String type, String str, Map<String, Object> inProperties, Object object, Object extra)
			throws PropertiesHandlerException {
	    String lStr = str;
        String script = "";
       
        try {
            if (!PropertiesHandler.isExpanded(lStr)) {
                lStr = PropertiesHandler.expand(lStr, inProperties, object, extra);
            }
            
            String scope = null;
            String lang = null;
            
            switch (type) {
				case "script":
					List<String> parts = TextUtils.splitByStringSeparator(lStr, "::");
	                lang = parts.get(0);
	                if (parts.size() > 2) {
	                    scope = parts.get(1);
	                    script = parts.get(2);
	                } else {
	                    script = parts.get(1);
	                }
					break;
				
				case "js":
					lang = "js";
					int pIdx = lStr.indexOf("::");
		            if (pIdx != -1) {
		                scope = lStr.substring(0, pIdx);
		                script = lStr.substring(pIdx + 2);
		            } else {
		                script = lStr;
		            }
		            break;
	
				case "ognl":
					lang = type;
					script = lStr;
					break;
            }
                                 
            return execScript(lang, scope, script, inProperties, object, extra);
        }
        catch (Exception exc) {
        	LOGGER.error("Error handling 'script[" + type + "]' metadata '" + lStr + "' ", exc);
            
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'script' metadata '" + str + "'", exc);
            }
            return "script" + PROP_START + str + PROP_END;
        }
	}

	private String execScript(String lang, String bcName, String script, Map<String, Object> inProperties,
			Object object, Object extra) throws Exception {
		Map<String, Object> bindings = new HashMap<String, Object>();
		bindings.put("inProperties", inProperties);
		bindings.put("object", object);
		bindings.put("extra", extra);

		Object obj = ScriptExecutor.execute(lang, script, bindings, bcName);
		String paramValue = (obj == null) ? "" : obj.toString();

		return paramValue;
	}

}