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

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.metadata.PropertyHandler;
import it.greenvulcano.util.xml.XMLUtils;

public class XPathPropertiesHandler implements PropertyHandler {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(XPathPropertiesHandler.class);
	private final static List<String> types = Collections.unmodifiableList(Arrays.asList("xpath"));
	
	@Override
	public List<String> getManagedTypes() {		
		return types;
	}

	@Override
	public String expand(String type, String str, Map<String, Object> inProperties, Object object, Object extra) throws PropertiesHandlerException {
		XMLUtils parser = null;
		String paramName = null;
		String paramValue = null;
		try {
			if (!PropertiesHandler.isExpanded(str)) {
				str = PropertiesHandler.expand(str, inProperties, object, extra);
			}
			int pIdx = str.indexOf("::");
			paramName = str.substring(0, pIdx);

			String xpath = str.substring(pIdx + 2);
			if (paramName.startsWith("file://")) {
				paramValue = XMLConfig.get(paramName.substring(7), xpath);
			} else {
				parser = XMLUtils.getParserInstance();
				DocumentBuilder db = parser.getDocumentBuilder(false, true, true);
				String xmlDoc = (String) inProperties.get(paramName);
				if ((xmlDoc == null) || ("".equals(xmlDoc))) {
					xmlDoc = "<dummy/>";
				}
				Document doc = db.parse(new InputSource(new StringReader(xmlDoc)));
				paramValue = parser.get(doc, xpath);
			}

			return paramValue;
		} catch (Exception exc) {
			LOGGER.error("Error handling 'xpath' metadata '" + paramName + "' ",  exc);
			
			if (PropertiesHandler.isExceptionOnErrors()) {
				if (exc instanceof PropertiesHandlerException) {
					throw (PropertiesHandlerException) exc;
				}
				throw new PropertiesHandlerException("Error handling 'xpath' metadata '" + str + "'", exc);
			}
			return "xpath" + PROP_START + str + PROP_END;
		} finally {
			if (parser != null) {
				XMLUtils.releaseParserInstance(parser);
			}
		}
	}

}