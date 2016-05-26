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

import it.greenvulcano.gvesb.osgi.repository.GVConfigurationRepository;
import it.greenvulcano.gvesb.osgi.repository.exception.GVResourceException;


public class GVConfigurationRestRepository implements GVConfigurationRepository {

	private final Logger LOG = LoggerFactory.getLogger(getClass());	
	
	private String endpoint, authtoken;
			
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
	public void setAuthtoken(String authtoken) {
		this.authtoken = authtoken;
	}

	@Override
	public Document retrieveConfiguration(String uuid) throws GVResourceException {
		Document document = null;
		
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			String authenticationToken = "Bearer " +authtoken;
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
