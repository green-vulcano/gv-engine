package it.greenvulcano.gvesb.console.api.repository.dao;

import it.greenvulcano.gvesb.console.api.dto.TraceLevelServiceDTO;

import java.util.List;

import javax.ws.rs.core.Response;

public interface TraceLevelServiceDao {

	public List<TraceLevelServiceDTO> getByServiceName(String serviceName);
	
	public void save(TraceLevelServiceDTO traceLevelServiceDTO);
	
	public void delete(TraceLevelServiceDTO traceLevelServiceDTO);
	
	public Response update(TraceLevelServiceDTO traceLevelServiceDTO);
}
