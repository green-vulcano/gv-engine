package it.greenvulcano.gvesb.console.api.repository.dao;

import it.greenvulcano.gvesb.console.api.dto.ServiceInstanceDTO;
import it.greenvulcano.gvesb.console.api.dto.ServiceStatsThroughput;
import it.greenvulcano.gvesb.console.api.dto.ServiceStatsTimeDTO;
import it.greenvulcano.gvesb.console.api.model.ServiceInstance;

import java.util.Date;
import java.util.List;

public interface ServiceInstanceDao {

	public List<ServiceInstance> getByServiceInstanceId(String serviceInstanceId);

	public ServiceStatsTimeDTO getStatsByServiceName(String serviceName, Date inStartDate, Date inEndDate);
	
	public ServiceStatsThroughput getThroughputRequestsByServiceName(String serviceName, Date inStartDate, Date inEndDate);

	public void delete(ServiceInstanceDTO serviceInstanceDTO);

}
