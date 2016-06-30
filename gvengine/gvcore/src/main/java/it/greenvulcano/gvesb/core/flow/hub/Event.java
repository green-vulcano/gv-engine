package it.greenvulcano.gvesb.core.flow.hub;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.config.InvocationContext;

public class Event
{
	private InvocationContext invocationContext;
	private GVBuffer gvBuffer;
	private EventResult eventResult;
	

	public Event(InvocationContext invocationContext,
			GVBuffer gvBuffer) {
		super();
		this.invocationContext = invocationContext;
		this.gvBuffer = gvBuffer;
	}

	public InvocationContext getInvocationContext() {
		return invocationContext;
	}

	public void setInvocationContext(InvocationContext invocationContext) {
		this.invocationContext = invocationContext;
	}

	public GVBuffer getGvBuffer() {
		return gvBuffer;
	}

	public void setGvBuffer(GVBuffer gvBuffer) {
		this.gvBuffer = gvBuffer;
	}

	public EventResult getEventResult() {
		return eventResult;
	}

	public void setEventResult(EventResult eventResult) {
		this.eventResult = eventResult;
	}

}
