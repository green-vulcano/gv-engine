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
package it.greenvulcano.gvesb.scheduler.api;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.CronTrigger;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.scheduler.ScheduleManager;

public class ScheduleControllerRest {
		
	private final static Logger LOG = LoggerFactory.getLogger(ScheduleControllerRest.class);
	
	private ScheduleManager gvScheduleManager;
	
	public void setGvScheduleManager(ScheduleManager gvScheduleManager) {
		this.gvScheduleManager = gvScheduleManager;
	}

	private Function<Trigger, JSONObject> triggerMapper = t -> {
		JSONObject obj = new JSONObject();
		obj.put("id", t.getKey().getName());
		obj.put("description", t.getDescription());
		
		if (t instanceof CronTrigger) {
			obj.put("cronExpression", ((CronTrigger) t).getCronExpression());
		}
		
		try {
			String status = gvScheduleManager.getTriggerStatus(t.getKey().getName());
			obj.put("status", status);
		} catch (SchedulerException e) {
			obj.put("status", "UNKNOW");
		}
		
		return obj;
	};
	
	@RolesAllowed("gvadmin")
	@Path("/schedules")
	@GET @Produces(MediaType.APPLICATION_JSON)
	public Response getSchedules() {
		Response response;
		
		try {
			List<Trigger> triggers = gvScheduleManager.getTriggersList();
			
			JSONObject responseData = triggers.stream()
					.map(triggerMapper)
					.reduce(new JSONObject(), (obj, t)-> obj.put(t.getString("id"), t));
			
			response = Response.ok(responseData.toString()).build();
		} catch (SchedulerException e) {
			LOG.error("Fail to read from quartz scheduler",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build();
		}
		
		return response;
	}
	
	@RolesAllowed("gvadmin")
	@Path("/schedules/{id}")
	@GET @Produces(MediaType.APPLICATION_JSON)
	public Response getSchedule(@PathParam("id")String id) {
		Response response;
		try{
			JSONObject responseData = gvScheduleManager.getTrigger(id)
											  .map(triggerMapper)
											  .orElseThrow(NoSuchElementException::new);
						
			response = Response.ok(responseData.toString()).build();
		} catch (NoSuchElementException e) {
			response = Response.status(Status.NOT_FOUND).build();
		} catch (SchedulerException e) {
			LOG.error("Fail to read from quartz scheduler",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build();
		}
		return response;
	}
	
	@RolesAllowed("gvadmin")
	@Path("/schedules/{id}")
	@DELETE @Produces(MediaType.APPLICATION_JSON)
	public Response deleteSchedule(@PathParam("id")String id) {
		Response response;
		try{
		
			gvScheduleManager.deleteTrigger(id);
			
			response = Response.ok().build();
		} catch (NoSuchElementException e) {
			response = Response.status(Status.NOT_FOUND).build();
		} catch (SchedulerException e) {
			LOG.error("Fail to perform delete trigger on quartz scheduler",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build();
		}
		return response;
	}
	
	@RolesAllowed("gvadmin")
	@Path("/schedules/{id}/pause")
	@PUT @Produces(MediaType.APPLICATION_JSON)
	public Response pauseSchedule(@PathParam(value = "id")String id) {
		Response response;
		try{
		
			gvScheduleManager.suspendTrigger(id);
			
			response = Response.accepted().build();
		} catch (NoSuchElementException e) {
			response = Response.status(Status.NOT_FOUND).build();
		} catch (SchedulerException e) {
			LOG.error("Fail to pause trigger on quartz scheduler",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build();
		}
		return response;
	}
	
	@Path("/schedules/{id}/resume")
	@PUT @Produces(MediaType.APPLICATION_JSON)
	public Response resumeSchedule(@PathParam(value = "id")String id) {
		Response response;
		try{
			gvScheduleManager.resumeTrigger(id);
			
			response = Response.accepted().build();
		} catch (NoSuchElementException e) {
			response = Response.status(Status.NOT_FOUND).build();
		} catch (SchedulerException e) {
			LOG.error("Fail to resume trigger on quartz scheduler",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build();
		}
		return response;
	}
	
	@RolesAllowed("gvadmin")
	@Path("/schedule/{service}/{operation}")
	@POST @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
	public Response scheduleOperation(@PathParam("service")String serviceName, @PathParam("operation")String operationName, String scheduleData) {
		
		Response response;
		
		try {
			
			JSONObject scheduleInfo = new JSONObject(scheduleData);						
			
			final Map<String, String> props = new HashMap<>();
			
			Optional.ofNullable(scheduleInfo.optJSONObject("properties"))
					.ifPresent(p -> props.putAll(p.keySet().stream().collect(Collectors.toMap(Function.identity(), p::getString))));
									
			String triggerName = gvScheduleManager.scheduleOperation(scheduleInfo.getString("cronExpression"), serviceName, operationName, props, scheduleInfo.opt("object"));
			
			response = Response.created(URI.create("/schedules/"+triggerName)).build();
			
		} catch (JSONException jsonException) {
			response = Response.status(Status.BAD_REQUEST).entity("Required JSON body with keys : cronExpression, properties, object").build();
			
		} catch (SchedulerException e) {
			LOG.error("Fail to create trigger on quartz scheduler",e);
			response = Response.status(Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build();
		}
		
		return response;
	}

	
}
