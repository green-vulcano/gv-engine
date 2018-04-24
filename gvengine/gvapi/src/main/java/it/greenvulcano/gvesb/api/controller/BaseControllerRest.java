package it.greenvulcano.gvesb.api.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.api.dto.ExceptionDTO;

public class BaseControllerRest {
	
	private final static ObjectMapper OBJECT_MAPPER;
	
	static {
		OBJECT_MAPPER = new ObjectMapper();		
	}
	
	protected String toJson(Object o) throws JsonProcessingException {
		return OBJECT_MAPPER.writeValueAsString(o);
	}
	
	protected String toJson(Exception exception) {
		ExceptionDTO dto = new ExceptionDTO(exception);
		return dto.toString();
	}
	
	protected <T> T parseJson(String json, Class<T> c) throws JsonParseException, JsonMappingException, IOException {
		return OBJECT_MAPPER.readValue(json, c);
	}

	public Properties getConfDescriptionProperties() throws FileNotFoundException, IOException {
		
		Path confDescPath = Paths.get(XMLConfig.getBaseConfigPath(), "../history/history.properties");
		
		if (Files.exists(confDescPath)) {
			Properties properties = new Properties();
			properties.load(Files.newInputStream(confDescPath, StandardOpenOption.READ));
			return properties;
		} else {
			throw new FileNotFoundException("history.properties");
		}	
    	
    }  
	
	public synchronized void saveConfDescriptionProperties(Properties confDescProperties) throws IOException {
			
		if (confDescProperties!=null) {
			
			try (OutputStream confDescPropertiesOutputStream = Files.newOutputStream(Paths.get(XMLConfig.getBaseConfigPath(), "../history/history.properties"), 
					                                                                  StandardOpenOption.WRITE, 
					                                                                  StandardOpenOption.CREATE,
					                                                                  StandardOpenOption.TRUNCATE_EXISTING)) {
				confDescProperties.store(confDescPropertiesOutputStream, null);
			}			
		}		
	}
}
