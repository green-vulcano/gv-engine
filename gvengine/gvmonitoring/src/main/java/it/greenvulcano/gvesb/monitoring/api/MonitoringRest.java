/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.monitoring.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.greenvulcano.gvesb.monitoring.model.CPUStatus;
import it.greenvulcano.gvesb.monitoring.model.ClassesStatus;
import it.greenvulcano.gvesb.monitoring.model.MemoryStatus;
import it.greenvulcano.gvesb.monitoring.model.ThreadsStatus;
import it.greenvulcano.gvesb.monitoring.service.SystemMonitor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;


@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true)
public class MonitoringRest {
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	
	private SystemMonitor systemMonitor;
	
	public void setSystemMonitor(SystemMonitor systemMonitor) {
		this.systemMonitor = systemMonitor;
	}
	
	@Path("/memory")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response readMemoryStatus() throws JsonProcessingException{
		
		MemoryStatus memory = systemMonitor.getMemoryStatus();
		
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(memory)).build();
		
		return response; 
	}	
	
	@Path("/cpu")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response readCPUStatus() throws Exception,JsonProcessingException{
		
		CPUStatus cpuStatus = systemMonitor.getCPUStatus();
        
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(cpuStatus)).build();
		
		return response;
	}
	
	
	@Path("/classes")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getClasses() throws JsonProcessingException{
		
		ClassesStatus classes = systemMonitor.getClassesStatus();		
		
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(classes)).build();
		
		return response;
	}
	
	
	@Path("/threads")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getThreads() throws JsonProcessingException{
		
		ThreadsStatus threads = systemMonitor.getThreadsStatus();
		
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(threads)).build();
		
		return response;
	}
	
	
}
