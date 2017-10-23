package it.greenvulcano.gvesb.properties.service.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.properties.model.GVProperties;
import it.greenvulcano.gvesb.properties.service.ConfProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GVPropertiesService implements ConfProperties {
	
	private final static Logger LOG = LoggerFactory.getLogger(GVPropertiesService.class);
	
	private final Properties properties = new Properties();
	
	private final File file;
	
	public GVPropertiesService(){
		
		file = new File(XMLConfig.getBaseConfigPath().concat(File.separator).concat("XMLConfig.properties"));
		
	}
	
	@Override
	public GVProperties getProperties() {
		
		String content = "";
		
		try {
			
			properties.load(new FileInputStream(file));
			
			InputStream fis = new FileInputStream(file);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			
			String line = "";
			while ((line = br.readLine()) != null) {
				content = content.concat(line).concat("\n");
			}
			
			//br.close();			
			//fis.close();
		}catch (FileNotFoundException e) {
			LOG.error("File doesn't found! " + e);
			return null;
		}catch(SecurityException e) {
			LOG.error("Permission denied: " + e);
			return null;
		}catch(IOException e) {
			LOG.error("Error: " + e);
			return null;
		}
		
		return new GVProperties(properties,content);
	
	}
	
	@Override
	public void saveProperties(String content) {
		
		try {
			FileWriter writer = new FileWriter(file);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			LOG.error("Error perform on file: " + e);
		}
		
	}
	
	@Override
	public String getProperty(String key) {
		
		return properties.getProperty(key);
	}

}
