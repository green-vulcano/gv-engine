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
import it.greenvulcano.util.crypto.CryptoHelper;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.metadata.PropertyHandler;

public class CryptoPropertiesHandler implements PropertyHandler {

    private final List<String> types = Collections.unmodifiableList(Arrays.asList("encrypt", "decrypt"));

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

        String value = str;

        if (!PropertiesHandler.isExpanded(value)) {
            value = PropertiesHandler.expand(value, inProperties, object, extra);
        }
        if (!PropertiesHandler.isExpanded(value)) {
            return "type" + PROP_START + value + PROP_END;
        }

        try {
            switch (type) {
                case "decrypt":
        
                    return CryptoHelper.decrypt(CryptoHelper.DEFAULT_KEY_ID, value, true);
                default:
        
                    return CryptoHelper.encrypt(CryptoHelper.DEFAULT_KEY_ID, value, true);
            }
        }  catch (Exception exc) {

                if (PropertiesHandler.isExceptionOnErrors()) {
                        if (exc instanceof PropertiesHandlerException) {
                                throw (PropertiesHandlerException) exc;
                        }
                        throw new PropertiesHandlerException("Error handling '"
                                + type
                                + "' metadata '" + str + "'", exc);
                }
                return "type" + PROP_START + str + PROP_END;
        }

    }

}