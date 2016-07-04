package it.greenvulcano.gvesb.core.flow.hub;

import it.greenvulcano.gvesb.core.config.InvocationContext;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;


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

	public static void startStatistics(Event event) throws Exception {    	
		InvocationContext context = event.getInvocationContext();
		logger.info("startStatistics - START -> serviceName: " + context.getService() + " - operationName: " + context.getOperation() + " - serviceInstanceId: " + context.getId().toString());
		instance.setEvent(event);
		instance.setState(State.START);
		logger.info("startStatistics - END -> serviceName: " + context.getService() + " - operationName: " + context.getOperation() + " - serviceInstanceId: " + context.getId().toString());
	}

	public static void stopStatistics(Event event) throws Exception {    	
		InvocationContext context = event.getInvocationContext();
		logger.info("stopStatistics - START -> serviceName: " + context.getService() + " - operationName: " + context.getOperation() + " - serviceInstanceId: " + context.getId().toString());
		instance.setEvent(event);
		instance.setState(State.END);
		logger.info("stopStatistics - END -> serviceName: " + context.getService() + " - operationName: " + context.getOperation() + " - serviceInstanceId: " + context.getId().toString());
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
