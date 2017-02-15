package it.greenvulcano.gvesb.console.api;

import it.greenvulcano.gvesb.console.api.dto.TraceLevelServiceDTO;
import it.greenvulcano.gvesb.console.api.utility.DateParam;


public interface GvConsoleServicesController<T> {
	
//	T getServices();	
//
//	T getOperations(String service);
//	
//	T query(String service, String operation, String data);
//	
//	T execute(String service, String operation, String data);
//	
//	T modify(String service, String operation, String data);
//	
//	T drop(String service, String operation, String data);
	
	T getThroughputByServiceName(String servicename, DateParam startdate, DateParam enddate);
	
	T getStatsByServiceName(String servicename, DateParam startdate, DateParam enddate);
	
	T findByServiceInstanceId(String serviceinstanceid);
	
	T findAllTraceLevelByServiceName();
	
	T findTraceLevelByServiceName(String servicename);
	
	T updateTraceLevel(TraceLevelServiceDTO traceLevelServiceDTO);
	
	T deleteTraceLevel(String tracelevelid);
	
	T createTraceLevel(TraceLevelServiceDTO traceLevelServiceDTO);
}
