package it.greenvulcano.gvesb.core.flow.hub;

import it.greenvulcano.gvesb.core.config.InvocationContext;
import it.greenvulcano.gvesb.internal.GVInternalException;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;


//dovrebbe essere lo statistics hub 
public class Subject {
	
	private static final Logger logger                = org.slf4j.LoggerFactory.getLogger(Subject.class);

	private List<Observer> observers = new ArrayList<Observer>();
	private State state;
	private Event event;

	private static final Subject instance = new Subject();
    
    private Subject(){}

    public static Subject getInstance(){
        return instance;
    }
    
    public static void startStatistics(Event event) throws GVInternalException {    	
    	InvocationContext context = event.getInvocationContext();
		logger.info("SERVICE_GVFLOW_XXX:" +context.getService());
		logger.info("SERVICE_OPERATION_XXX:" +context.getOperation());
		String serviceInstanceId = context.getId().toString();		
		logger.info("startStatistics serviceInstanceId:" +serviceInstanceId);
		
    	instance.setEvent(event);
    	instance.setState(State.START);
    }
    
    public static void stopStatistics(Event event) throws GVInternalException {    	
    	InvocationContext context = event.getInvocationContext();
		logger.info("SERVICE_GVFLOW_XXX:" +context.getService());
		logger.info("SERVICE_OPERATION_XXX:" +context.getOperation());
		String serviceInstanceId = context.getId().toString();
		logger.info("stopStatistics serviceInstanceId:" +serviceInstanceId);
		
    	instance.setEvent(event);
    	instance.setState(State.END);
    }
    
    public State getState(){
		return state;
	}

	public void setState(State state) {
		this.state = state;
		notifyAllObservers();
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public void attach(Observer observer){
		observers.add(observer);		
	}
	
	public void detach(Observer observer){
		observers.remove(observer);
	}

	public void notifyAllObservers(){
		for (Observer observer : observers) {
			observer.update(event, state);
		}
	}
}
