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
package it.greenvulcano.gvesb.osgi.repository.vulcon;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.http.HTTPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import it.greenvulcano.gvesb.osgi.bus.connectors.GVBusLink;
import it.greenvulcano.gvesb.osgi.repository.GVConfigurationRepository;
import it.greenvulcano.gvesb.osgi.repository.exception.GVResourceException;


public class GVConfigurationRestRepository implements GVConfigurationRepository {

	private final Logger LOG = LoggerFactory.getLogger(getClass());	
	
	private String endpoint;
			
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
		
	@Override
	public Document retrieveConfiguration(String uuid) throws GVResourceException {
		Document document = null;
		
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			String authenticationToken = "Bearer " + GVBusLink.getBusId();
			String projectURL = endpoint.concat("/project/").concat(uuid); 
						
			LOG.debug("GVConfigurationRestRepository - Starting REST call to " + projectURL);
			URL restURL = new URL(projectURL);			
						
			HttpURLConnection apiUrlConnection = (HttpURLConnection) restURL.openConnection();			
			apiUrlConnection.setRequestProperty("Authorization", authenticationToken);	
			apiUrlConnection.setRequestMethod("GET");		
			apiUrlConnection.setRequestProperty("Accept", "application/xml");
			apiUrlConnection.setDoInput(true);		
			
			apiUrlConnection.connect();	
		    
			int responseCode = apiUrlConnection.getResponseCode();
			LOG.debug("GVConfigurationRestRepository - Server response HTTP " + responseCode);
			switch (responseCode){
				case HttpURLConnection.HTTP_OK:					
					document = documentBuilder.parse(apiUrlConnection.getInputStream());
					apiUrlConnection.disconnect();
					break;
				
				case HttpURLConnection.HTTP_UNAUTHORIZED:
				case HttpURLConnection.HTTP_FORBIDDEN:
					throw new GVResourceException("Response "+ responseCode+": Authentication failure for user "+authenticationToken+ " on "+projectURL, new HTTPException(responseCode));
				
				case HttpURLConnection.HTTP_NOT_FOUND:
					throw new GVResourceException("Response "+ responseCode+": Resource not found "+projectURL, new HTTPException(responseCode));
				
				default:
					throw new GVResourceException("Response "+ responseCode+": Server response error on "+projectURL, new HTTPException(responseCode));
			};					
			
		} catch (MalformedURLException malformedURLException) {
			throw new GVResourceException("Invalid API endopoint url: "+endpoint, malformedURLException);
		}   catch (ParserConfigurationException parserConfigurationException) {
			throw new GVResourceException("XML API failure ", parserConfigurationException);
		} catch (SAXException saxException) {
			throw new GVResourceException("Invalid XML response ", saxException);
		} catch (IOException ioException) {
			throw new GVResourceException("Server response error on "+endpoint, ioException);
		}
			
		return document ;
	}

}
