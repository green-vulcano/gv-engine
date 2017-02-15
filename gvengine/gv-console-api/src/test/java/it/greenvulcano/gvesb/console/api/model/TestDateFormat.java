package it.greenvulcano.gvesb.console.api.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestDateFormat {
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
	public static void main(String[] args) {
		Date date = new Date();
		
		DateFormat dt = new SimpleDateFormat(DATE_FORMAT);
		
		String dateString = dt.format(date);
		
		System.out.println("DATE_STRING ISO: " + dateString);
		
		
		
	}

}
