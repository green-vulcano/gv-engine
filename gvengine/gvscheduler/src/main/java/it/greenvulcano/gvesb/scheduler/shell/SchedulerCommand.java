package it.greenvulcano.gvesb.scheduler.shell;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.CronTrigger;
import org.quartz.SchedulerException;

import it.greenvulcano.gvesb.scheduler.ScheduleManager;

@Command(scope="gvesb", name="scheduler", description="Manage operations scheduling on GV ESB")
@Service
public class SchedulerCommand implements Action {

	private enum Action {SUSPEND, RESUME, DELETE}
	
	@Argument(index=0, name="urn", description="The identifier (expressed as service/operation) of the operation to schedule", required=false, multiValued=false)
	private String service;
	
	@Argument(index=1, name="cronexp", description="The schedule settings expressed as cron expression ", required=false, multiValued=false)
	private String cronExpression;
	
	@Option(name="-c", aliases="--create", description="Create a schedule entry specified by a desriptor file")
	private String descriptorURI;
	
	@Option(name="-s", aliases="--suspend", description="Suspend a schedule entry specified by id")
	private String suspendId;
	
	@Option(name="-r", aliases="--resume", description="Resume a schedule entry specified by id")
	private String resumeId;
	
	@Option(name="-d", aliases="--delete", description="Delete a schedule entry specified by id")
	private String deleteId;
	
	@Reference
	private ScheduleManager scheduleManager;
	
	public void setScheduleManager(ScheduleManager scheduleManager) {
		this.scheduleManager = scheduleManager;
	}
	
	@Override
	public Object execute() throws Exception {
				
		if (Objects.nonNull(deleteId)){
			return performAction(Action.DELETE, deleteId);
		}
		
		if (Objects.nonNull(suspendId)){
			return performAction(Action.SUSPEND, suspendId);
		}
		
		if (Objects.nonNull(resumeId)){
			return performAction(Action.RESUME, resumeId);
		}
		
		if (Objects.nonNull(descriptorURI)){
			return createByDescriptorFile(descriptorURI);
		}
		
		if (Objects.nonNull(cronExpression)){
			return create();
		}
		

        ShellTable table = new ShellTable();
        
        table.emptyTableText("There are no scheduled execution");
        
        //***** Table header
        table.column("ID");
        table.column("Operation");
        table.column("Schedule");
        table.column("Status");
		
        scheduleManager.getTriggersList().stream()
            	       .map(t -> {
            	    	   		/*
            	    	   		 * Building a row for trigger
            	    	   		 * 
            	    	   		 */
					        	Optional<String> cronExpression = Optional.empty(); 
								if (t instanceof CronTrigger) {
									cronExpression = Optional.ofNullable(((CronTrigger) t).getCronExpression());
								}
								
								String status; 
								try {
									status = scheduleManager.getTriggerStatus(t.getKey().getName());				
								} catch (SchedulerException e) {
									status = "UNKNOW";
								}
					        	
					        	return new Object[]{ t.getKey().getName(), t.getDescription(), cronExpression.orElse("ND"),  status };
	            	       })
            	       .forEach(row -> {
            	    	   			table.addRow().addContent(row);
            	       			});
        
        table.print(System.out, true);
        
		return null;
	}
	
	private String performAction(Action action, String id) {
		String response = null;
		try {
			switch (action) {
				case DELETE:
					scheduleManager.deleteTrigger(id);
					response = id + " - DELETED";
					break;
				case RESUME:
					scheduleManager.resumeTrigger(id);
					response = id + " - RESUMED";
					break;
				case SUSPEND:
					scheduleManager.suspendTrigger(id);
					response = id + " - PAUSED";
					break;			
			}
		} catch (NoSuchElementException e) {
			response = "No matching entry found for ID "+id;
		} catch (SchedulerException e) {
			response = "Sevice unavailable";
		}
		
		return response;
	}
		
	/**
	 * Read a file containing a JSON representation of schedule
	 * 
	 * <pre>
	 * 
	 *{
	 *	cronExpression: " * 5 * * * ?",
	 * 	properties: {
	 *		PROP_A: 1,
	 *  	PROP_B: "value"
	 *  	...
	 *  	},
	 *	object: "something"
	 * 
	 * } 
	 * </pre>
	 * 
	 * @param uri Descriptor file URI
	 * 
	 * @return Creation response
	 * 	
	 */
	private String createByDescriptorFile(String uri){
		String response = null;	
		
		try {		
			
			String[] serviceURI = service.split("/");
			String service = serviceURI[0];
			String operation = serviceURI[1];
			
			byte[] descriptor = Files.readAllBytes(Paths.get(uri));			
			
			JSONObject scheduleInfo = new JSONObject(new String(descriptor));						
			
			final Map<String, String> props = new HashMap<>();
			
			Optional.ofNullable(scheduleInfo.optJSONObject("properties"))
					.ifPresent(p -> props.putAll(p.keySet().stream().collect(Collectors.toMap(Function.identity(), p::getString))));
									
			response = scheduleManager.scheduleOperation(scheduleInfo.getString("cronExpression"), service, operation, props, scheduleInfo.opt("object")) + " - CREATED";
			
		} catch (NullPointerException|IndexOutOfBoundsException e) {	
			throw new IllegalArgumentException("Required param operation identifier expressed as service/operation");
		} catch (SchedulerException e) {
			response = "Sevice unavailable";
		} catch (JSONException e) {
			throw new IllegalArgumentException("Invalid descriptor file "+uri, e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Fail to read descriptor file "+uri);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid cron expression in descriptor file "+uri, e);
		}
		
		return response;
	}
	
	/**
	 * Create a simple schedule entry without option and payload
	 * 
	 * @return Creation response
	 *  
	 */
	private String create() {
		String response = null;	
		
		try {		
			
			String[] serviceURI = service.split("/");
			String service = serviceURI[0];
			String operation = serviceURI[1];
												
			response = scheduleManager.scheduleOperation(cronExpression, service, operation, null, null) + " - CREATED";
			
		} catch (NullPointerException|IndexOutOfBoundsException e) {	
			throw new IllegalArgumentException("Required param operation identifier expressed as service/operation");
		} catch (SchedulerException e) {
			response = "Sevice unavailable";		
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid cron expression "+cronExpression, e);
		}
		
		return response;
	}

}
