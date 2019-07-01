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
package it.greenvulcano.gvesb.gviamx.service.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.gviamx.service.CallBackService;

public class HttpCallBackManager implements CallBackService {
	private final static Logger LOG = LoggerFactory.getLogger(HttpCallBackManager.class);
	
	
	private final String destination, authorization;
	
	public HttpCallBackManager(String destination, String authorization) {		
		this.destination = destination;
		this.authorization = authorization;
	}

	@Override
	public void performCallBack(byte[] payload){
		if (destination!=null && destination.startsWith("http")){
			try {
				
				URL callBackURL = new URL(destination);
								
				LOG.debug("Performing callback to "+destination);
				HttpURLConnection urlConnection = (HttpURLConnection) callBackURL.openConnection();
	
				urlConnection.setRequestMethod("POST");
	
				urlConnection.setRequestProperty("content-type", "application/json; charset=utf-8");
				Optional.ofNullable(authorization).filter(String::isEmpty).ifPresent(token->urlConnection.setRequestProperty("authorization", token));
	
				String payloadData = new String(Objects.requireNonNull(payload, "A valid JSON payload is required"), Charset.forName("UTF-8"));
				urlConnection.setDoOutput(true);
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream(), Charset.forName("UTF-8"));
				outputStreamWriter.write(payloadData);
				outputStreamWriter.flush();
				outputStreamWriter.close();
	
				urlConnection.connect();	          
	
				InputStream responseStream = null;
	
				int responseCode = -1;
				try {
					responseCode = urlConnection.getResponseCode();
					responseStream = urlConnection.getInputStream();
				} catch (IOException connectionFail) {
					responseStream = urlConnection.getErrorStream();
				}           
	
				InputStreamReader contentReader = new InputStreamReader(responseStream, "UTF-8");	       	   
				BufferedReader bufferedReader = new BufferedReader(contentReader);
	
				String response = bufferedReader.lines().collect(Collectors.joining("\n"));	  
				if (responseCode>399) {
					LOG.error("Got error response on callback: "+responseCode+" - "+response);	
				} else {
					LOG.debug("Got response on callback: "+responseCode+" - "+response);	
				}			     		       	   
				
			} catch (Exception e) {
				LOG.error("Fail to perform callback on "+destination, e);
			}
		} else {
			LOG.debug("No callback url configured");
		}
	
	}

}
