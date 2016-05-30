package it.greenvulcano.gvesb.api;

import javax.ws.rs.core.Response;

public interface GvServicesController {
	
	Response getServices();

	Response execute(String service, String operation, String data);

	Response getOperations(String service);
}
