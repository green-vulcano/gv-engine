package it.greenvulcano.gvesb.api;

public interface GvServicesController<T> {
	
	T getServices();	

	T getOperations(String service);
	
	T query(String service, String operation, String data);
	
	T execute(String service, String operation, String data);
	
	T modify(String service, String operation, String data);
	
	T drop(String service, String operation, String data);
}
