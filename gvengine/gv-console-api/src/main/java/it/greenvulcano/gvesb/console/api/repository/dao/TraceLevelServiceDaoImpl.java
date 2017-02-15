package it.greenvulcano.gvesb.console.api.repository.dao;

import it.greenvulcano.gvesb.console.api.dto.TraceLevelServiceDTO;
import it.greenvulcano.gvesb.console.api.model.TraceLevelService;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.WriteResult;

public class TraceLevelServiceDaoImpl implements TraceLevelServiceDao {

	private static final Logger LOG = LoggerFactory.getLogger(TraceLevelServiceDaoImpl.class);

	private Datastore datastore;


	public TraceLevelServiceDaoImpl() {
		super();
	}

	public TraceLevelServiceDaoImpl(Datastore datastore) {
		super();
		this.datastore = datastore;
	}

	public Datastore getDatastore() {
		return datastore;
	}

	public void setDatastore(Datastore datastore) {
		this.datastore = datastore;
	}

	@Override
	public List<TraceLevelServiceDTO> getByServiceName(String serviceName) {
		LOG.debug("getByServiceName - START - serviceName: " + serviceName);

		List<TraceLevelServiceDTO> listTraceLevel = new ArrayList<TraceLevelServiceDTO>();

		Query<TraceLevelService> query = datastore.createQuery(TraceLevelService.class);

		if(serviceName != null) query.field("serviceName").equal(serviceName);

		List<TraceLevelService> traceLevelServiceList = query.asList();

		for(TraceLevelService item : traceLevelServiceList){
			TraceLevelServiceDTO dto = new TraceLevelServiceDTO();
			dto.setEnabled(item.getEnabled());
			dto.setId(item.getId().toString());
			dto.setServiceName(item.getServiceName());
			dto.setTraceLevel(item.getTraceLevel());

			listTraceLevel.add(dto);
		}

		LOG.debug("getByServiceName - END - serviceName: " + serviceName);

		return listTraceLevel;
	}

	public void save(TraceLevelServiceDTO traceLevelServiceDTO){
		LOG.debug("save - START - traceLevelServiceDTO: " + traceLevelServiceDTO);

		if(traceLevelServiceDTO != null){
			TraceLevelService traceLevelService = new TraceLevelService();

			//CREATE a NEW DOCUMENT IF THE ID HAS NOT BEEN FOUND.
			traceLevelService.setEnabled(traceLevelServiceDTO.getEnabled());
			
			if(traceLevelServiceDTO.getId() != null) {
				traceLevelService.setId(new ObjectId(traceLevelServiceDTO.getId()));
			}
			
			traceLevelService.setServiceName(traceLevelServiceDTO.getServiceName());
			traceLevelService.setTraceLevel(traceLevelServiceDTO.getTraceLevel());

			datastore.save(traceLevelService);
		} else {
			throw new BadRequestException("ERROR IN THE INPUT OBJECT PARAMETER.");
		}
		
		LOG.debug("save - END - traceLevelServiceDTO: " + traceLevelServiceDTO);
	}
	
	public void delete(TraceLevelServiceDTO traceLevelServiceDTO){
		LOG.debug("delete - START - traceLevelServiceDTO: " + traceLevelServiceDTO);

		if(traceLevelServiceDTO != null && traceLevelServiceDTO.getId() != null){
			TraceLevelService traceLevelService = new TraceLevelService();

			//DELETE THE DOCUMENT
			traceLevelService.setEnabled(traceLevelServiceDTO.getEnabled());
			traceLevelService.setIdString(traceLevelServiceDTO.getId());
			traceLevelService.setServiceName(traceLevelServiceDTO.getServiceName());
			traceLevelService.setTraceLevel(traceLevelServiceDTO.getTraceLevel());

			WriteResult result = datastore.delete(traceLevelService);
			
			LOG.debug("delete - result N. ELEMENTS DELETED: " + result.getN());
		} else {
			throw new BadRequestException("ERROR IN THE INPUT OBJECT PARAMETER.");
		}
		
		LOG.debug("delete - END - traceLevelServiceDTO: " + traceLevelServiceDTO);
	}

	@Override
	public Response update(TraceLevelServiceDTO traceLevelServiceDTO) {
		LOG.debug("update - START - traceLevelServiceDTO: " + traceLevelServiceDTO);
		
		//String id = traceLevelServiceDTO.getId();
		String serviceName = traceLevelServiceDTO.getServiceName();
		Query<TraceLevelService> query = datastore.createQuery(TraceLevelService.class).field("serviceName").equal(serviceName);
		UpdateOperations<TraceLevelService> updateOperation = datastore.createUpdateOperations(TraceLevelService.class).set("enabled", traceLevelServiceDTO.getEnabled()).set("traceLevel", traceLevelServiceDTO.getTraceLevel());
		LOG.debug("update - query: " + query);
		
		UpdateResults result = datastore.updateFirst(query, updateOperation, true);
		
		LOG.debug("update - END - traceLevelServiceDTO: " + traceLevelServiceDTO + " - result: " 
		+ result + " - newId: " + result.getNewId() + " result.getUpdatedCount(): " + result.getUpdatedCount() + " result.getInsertedCount(): " + result.getInsertedCount());
		
		return Response.ok().build();
	}

}
