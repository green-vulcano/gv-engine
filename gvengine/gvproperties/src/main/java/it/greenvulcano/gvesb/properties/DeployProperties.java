package it.greenvulcano.gvesb.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeployProperties {

	public static void main(String[] args) {
		
		InputStream is = null;
		
		
		File gvCore = new File("/home/alessio/Downloads/GVCore.xml");
		String content = "";
		
		try {
			is = new FileInputStream(gvCore);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = "";
		try {
			while((line = br.readLine()) != null) {
				
				content = content.concat(line).concat("\n");
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String pattern = "xmlp\\{\\{([-a-zA-Z0-9._]+)\\}\\}";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(content);
		while(m.find()) {
			System.out.println(m.group(1) + "\n");
		}
		
		

	}

}
