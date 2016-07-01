package it.greenvulcano.gvesb.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.core.flow.hub.Event;
import it.greenvulcano.gvesb.core.flow.hub.Observer;
import it.greenvulcano.gvesb.core.flow.hub.State;
import it.greenvulcano.gvesb.core.flow.hub.Subject;

public class GVConsoleObserver extends Observer {

	private static final Logger LOG = LoggerFactory.getLogger(GVConsoleObserver.class);
	
	public GVConsoleObserver(Subject subject){
		this.subject = subject;
		this.subject.attach(this);
	}

	@Override
	public void update(Event event, State state) {
		LOG.info("STATE: " + subject.getState() ); 
		
		if(state == State.START) {
			LOG.info("CALLING GVConsoleAspect.logGVFlowPerformBefore");
			GVConsoleAspect.logGVFlowPerformBefore(event);
		} else if(state == State.END)
		{
			LOG.info("CALLING GVConsoleAspect.logGVFlowPerformAfterReturning");
			GVConsoleAspect.logGVFlowPerformAfterReturning(event);
		} else {
			LOG.error("STATE NOT AVAILABLE: "+ state);
		}
	}

}
