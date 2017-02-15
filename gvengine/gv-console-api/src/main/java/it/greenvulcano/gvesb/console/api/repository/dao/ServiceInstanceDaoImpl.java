package it.greenvulcano.gvesb.console.api.repository.dao;

import it.greenvulcano.gvesb.console.api.dto.ServiceInstanceDTO;
import it.greenvulcano.gvesb.console.api.dto.ServiceStatsThroughput;
import it.greenvulcano.gvesb.console.api.dto.ServiceStatsTimeDTO;
import it.greenvulcano.gvesb.console.api.model.ServiceInstance;

import java.util.Date;
import java.util.List;

import javax.ws.rs.BadRequestException;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.WriteResult;

//extends BasicDAO<ServiceInstance, ObjectId> 
public class ServiceInstanceDaoImpl 
implements ServiceInstanceDao {
	private static final Logger LOG = LoggerFactory.getLogger(ServiceInstanceDaoImpl.class);

	private Datastore datastore;
	//
	//		
	//	public ServiceInstanceDaoImpl(Class<ServiceInstance> entityClass, Datastore ds) {
	//		super(entityClass, ds);
	//	}


	//	public ServiceInstanceDaoImpl(Class<ServiceInstance> entityClass, Datastore datastore) {
	//		super(entityClass, datastore);
	//		this.datastore = datastore;
	//	}

	public ServiceInstanceDaoImpl(Datastore datastore) {
		this.datastore = datastore;
	}

	/**
	 * get a ServiceInstance by ServiceInstanceId
	 * @return
	 */
	public List<ServiceInstance> getByServiceInstanceId(String serviceInstanceId){
		Query<ServiceInstance> query = datastore.createQuery(ServiceInstance.class).field("serviceInstanceId").equal(serviceInstanceId);
		return query.asList();
	}

	public ServiceStatsTimeDTO getStatsByServiceName(String serviceName, Date inStartDate, Date inEndDate){
		LOG.debug("getStatsByServiceName - START - serviceName: " + serviceName + " - inStartDate: + " + inStartDate + " - inEndDate: " + inEndDate);
		ServiceStatsTimeDTO dto = new ServiceStatsTimeDTO();

		Query<ServiceInstance> query = datastore.createQuery(ServiceInstance.class)
				.field("serviceName").equal(serviceName)
				.field("startDate").greaterThanOrEq(inStartDate)
			    .field("endDate").lessThanOrEq(inEndDate);

		Long tMin = 0L;
		Long tMax = 0L;
		Long tAvg = 0L;
		int i = 0;
		for(ServiceInstance instance : query.asList()){
			LOG.debug("serviceInstance i: " + i + " - serviceInstance: " + instance);
			if(instance.getEndDate() != null && instance.getStartDate() != null) {
				
				Date endDate = instance.getEndDate();
				Date startDate = instance.getStartDate();
				
				LOG.debug("endDate: " + endDate + " - startDate: " + startDate);

				long tDiff = endDate.getTime() - startDate.getTime();

				if( i == 0) {
					tMin = tDiff;
					tMax = tDiff;
				} else {
					if(tDiff < tMin) tMin = tDiff;
					if(tDiff > tMax) tMax = tDiff;
				}
				i++;
			}
		}

		if(i > 0) tAvg = (tMax - tMin) / i;

		dto.setServiceName(serviceName);
		dto.settMin(tMin);
		dto.settMax(tMax);
		dto.settAvg(tAvg);

		LOG.debug("getStatsByServiceName - END - serviceName: " + serviceName + " - dto: " + dto);

		return dto;
	}

	
	public ServiceStatsThroughput getThroughputRequestsByServiceName(String serviceName, Date inStartDate, Date inEndDate) {
		LOG.debug("getThroughputRequestsByServiceName - START - serviceName: " + serviceName + " - inStartDate: + " + inStartDate + " - inEndDate: " + inEndDate);
		
		ServiceStatsThroughput dto = new ServiceStatsThroughput();

		Date firstStartDate = null;
		Date lastEndDate 	= null;
		Integer throughput = 0;
		Query<ServiceInstance> query = datastore.createQuery(ServiceInstance.class)
				.field("serviceName").equal(serviceName)
				.field("startDate").greaterThanOrEq(inStartDate)
			    .field("endDate").lessThanOrEq(inEndDate);
		
		int n = 0;
		for(ServiceInstance instance : query.asList()){
			
			Date start = instance.getStartDate();
			Date end   = instance.getEndDate();
			
			if( n == 0) {
				firstStartDate = start;
				lastEndDate    = end;
			} else {
				if(start.getTime() < firstStartDate.getTime()) firstStartDate = start;
				if(end.getTime() > lastEndDate.getTime()) lastEndDate = end;
			}
			
			n++;
		}
		
		dto.setServiceName(serviceName);
		dto.setInEndDate(inEndDate);
		dto.setInStartDate(inStartDate);
		dto.setLastEnd(lastEndDate);
		dto.setFirstStart(firstStartDate);
		
		if(lastEndDate != null && firstStartDate != null) {
			long diffSeconds = (lastEndDate.getTime() - firstStartDate.getTime())/1000;
			dto.setInterval(diffSeconds);
			if(diffSeconds > 0) {
				throughput = n;
				dto.setThroughput(throughput);
			}
			
			LOG.debug("getThroughputRequestsByServiceName - END - serviceName: " + serviceName + " - diffSeconds: + " + diffSeconds + " - n: " + n + " - throughput: " + throughput);
		}
						
		LOG.debug("getThroughputRequestsByServiceName - END - serviceName: " + serviceName + " - inStartDate: + " + inStartDate + " - inEndDate: " + inEndDate + " - dto: " + dto);
		
		return dto;
	}
	
	public void delete(ServiceInstanceDTO serviceInstanceDTO){
		LOG.debug("delete - START - serviceInstanceDTO: " + serviceInstanceDTO);

		if(serviceInstanceDTO != null){
			ServiceInstance serviceInstance = new ServiceInstance();

			//DELETE THE DOC IF THE ID HAS NOT BEEN FOUND.
			serviceInstance.setId(serviceInstanceDTO.getId());
			serviceInstance.setServiceInstanceId(serviceInstanceDTO.getServiceInstanceId());
			serviceInstance.setServiceName(serviceInstanceDTO.getServiceName());
			
			//TODO If use also other fields.

			WriteResult result = datastore.delete(serviceInstance);
			
			LOG.debug("delete - result N. ELEMENTS DELETED: " + result.getN());
		} else {
			throw new BadRequestException("ERROR IN THE INPUT OBJECT PARAMETER.");
		}
		
		LOG.debug("delete - END - serviceInstanceDTO: " + serviceInstanceDTO);
	}
	
	public Datastore getDatastore() {
		return datastore;
	}

	public void setDatastore(Datastore datastore) {
		this.datastore = datastore;
	}

	
}
