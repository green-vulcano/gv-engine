package it.greenvulcano.gvesb.core.flow.hub;

public abstract class Observer
{
	protected Subject subject;
	public abstract void update(Event event, State state);
}
