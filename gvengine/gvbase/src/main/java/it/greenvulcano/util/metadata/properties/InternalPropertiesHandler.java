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
import java.util.Objects;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.metadata.PropertyHandler;

public class InternalPropertiesHandler implements PropertyHandler {
	
	private final List<String> types = Collections.unmodifiableList(Arrays.asList("@"));
	
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
		 String propName;
	        String fallback;
	        if (str.matches("^.+::.+$")) {
	        	String[] values = str.split("::");
	        	propName = values[0];
	        	fallback = values[1];
	        } else {
	        	propName = str;
	        	fallback = null;
	        }
	        
	        String paramValue = null;
	        if (propName.equals(GVBuffer.OBJECT_REF)){
	        	if(object instanceof GVBuffer) {
	        		object = GVBuffer.class.cast(object).getObject();
	        	} 
	       		paramValue = Objects.nonNull(object)? object.toString() : "";
	        	
	        } else {
	        	if (!PropertiesHandler.isExpanded(propName)) {
	                propName = PropertiesHandler.expand(propName, inProperties, object, extra);
	            }
	            
	            if (inProperties == null) {
	                return "@" + PROP_START + str + PROP_END;
	            }
	            paramValue = (String) inProperties.get(propName);
	            if ((paramValue == null)) {// || (paramValue.equals(""))) {
	            	
	            	if (fallback!=null) {
	            	  return PropertiesHandler.isExpanded(fallback)	?
	            			  fallback : PropertiesHandler.expand(fallback, inProperties, object, extra);
	            	}
	            	
	                return "@" + PROP_START + str + PROP_END;
	            }
	            if (!PropertiesHandler.isExpanded(paramValue)) {
	                paramValue = PropertiesHandler.expand(paramValue, inProperties, object, extra);
	            }            
	        }
	        return paramValue;
	}

}