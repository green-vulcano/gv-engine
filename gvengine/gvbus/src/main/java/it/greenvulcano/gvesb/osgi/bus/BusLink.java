package it.greenvulcano.gvesb.osgi.bus;

import java.io.IOException;

public interface BusLink {

	String connect(String busId) throws IOException;	

}
