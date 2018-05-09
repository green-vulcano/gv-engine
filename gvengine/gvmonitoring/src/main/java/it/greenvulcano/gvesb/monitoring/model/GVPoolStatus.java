package it.greenvulcano.gvesb.monitoring.model;

import java.time.Instant;

public class GVPoolStatus {

	private final Instant time;
	
	private final String name;
	
	private final int maximumSize;
	
	private final int inUseCount;

	public GVPoolStatus(Instant time, String name, int maximumSize, int inUseCount) {
		super();
		this.time = time;
		this.name = name;
		this.maximumSize = maximumSize;
		this.inUseCount = inUseCount;
	}

	public Instant getTime() {
		return time;
	}

	public String getName() {
		return name;
	}

	public int getMaximumSize() {
		return maximumSize;
	}

	public int getInUseCount() {
		return inUseCount;
	}
	
}
