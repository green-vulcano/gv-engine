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

import it.greenvulcano.gvesb.gviamx.service.CallBackManager;

public class HttpCallBackManager implements CallBackManager {
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
