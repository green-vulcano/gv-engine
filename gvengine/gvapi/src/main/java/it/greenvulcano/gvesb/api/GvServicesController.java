package it.greenvulcano.gvesb.api;

import java.util.Map;

import javax.ws.rs.core.Response;

public interface GvServicesController {
	
	Response getServices();

	Response execute(String service, String operation, Map<String, String> properties, String data);

	Response getOperations(String service);
}
