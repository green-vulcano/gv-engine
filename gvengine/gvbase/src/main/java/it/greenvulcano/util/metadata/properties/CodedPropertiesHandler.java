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
 ********************************************************************************/
package it.greenvulcano.util.metadata.properties;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.metadata.PropertyHandler;

public class CodedPropertiesHandler implements PropertyHandler {
	
	private final List<String> types = Collections.unmodifiableList(Arrays.asList("enc","dec"));
	
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
		try {
			String string = str;

			if (!PropertiesHandler.isExpanded(string)) {
				string = PropertiesHandler.expand(string, inProperties, object, extra);
			}
			if (!PropertiesHandler.isExpanded(string)) {
				return "enc" + PROP_START + str + PROP_END;
			}

			String encoder = "base64";
			if (string.matches("^.+::.+$")) {
				String[] parts = string.split("::");
				encoder = parts[0];
				string = parts[1];
			}

			return type.startsWith("enc") ? encode(encoder, string) : decode(encoder, string);

		} catch (Exception exc) {

			if (PropertiesHandler.isExceptionOnErrors()) {
				if (exc instanceof PropertiesHandlerException) {
					throw (PropertiesHandlerException) exc;
				}
				throw new PropertiesHandlerException("Error handling 'urlDec' metadata '" + str + "'", exc);
			}
			return "enc" + PROP_START + str + PROP_END;
		}
	}
	
	private String encode(String encoder, String string) throws UnsupportedEncodingException {
		switch (encoder) {

		case "base64":
			return Base64.getEncoder().encodeToString(string.getBytes());

		case "url":
			return URLEncoder.encode(string, "UTF-8");

		case "hex":
			return string.chars().mapToObj(Integer::toHexString).collect(Collectors.joining());

		default:
			return "enc" + PROP_START + string + PROP_END;
		}
	}

	private String decode(String encoder, String string) throws UnsupportedEncodingException {
		switch (encoder) {

		case "base64":
			return new String(Base64.getDecoder().decode(string.getBytes()));

		case "url":
			return URLDecoder.decode(string, "UTF-8");

		case "hex":
			return Stream.of(string.split("(?<=\\G.{2})")).map(h -> (char) Integer.parseInt(h, 16))
					.reduce(new StringBuffer(), (b, h) -> b.append(h), (b, b1) -> b.append(b1.toString())).toString();

		default:
			return "dec" + PROP_START + string + PROP_END;
		}
	}

}