package it.greenvulcano.gvesb.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class getProperty {

	public static void main(String[] args) {
		
		Properties properties = new Properties();
		
		String content = "";
		try {
			InputStream is = new FileInputStream(new File("/home/alessio/Scaricati/apache-karaf-4.1.2/GreenV/default/XMLConfig.properties"));
			properties.load(is);
			InputStreamReader reader = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(reader);
			String line = "";
			
			while((line = br.readLine()) != null) {
				
				content = content.concat(line).concat("\n");
				
			}
			
			System.out.println(properties.keySet().size());
			System.out.println(properties.getProperty("open_topic"));
			reader.close();
			br.close();
		}catch(IOException e) {
			System.out.println("error: " + e);
		}catch(IllegalArgumentException e) {
			System.out.println("error2: " + e);
		}
		
		//System.out.println(content);
	}

}
