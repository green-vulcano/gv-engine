package it.greenvulcano.gvesb.monitoring.model;

import java.time.Instant;

public class GVPoolStatus {

	private final Instant time;
	
	private final String name;
	
	private final int maximumSize;
	
	private final int inUseCount;
	
	private final int pooledCount;

	public GVPoolStatus(Instant time, String name, int maximumSize, int inUseCount, int pooledCount) {
		super();
		this.time = time;
		this.name = name;
		this.maximumSize = maximumSize;
		this.inUseCount = inUseCount;
		this.pooledCount = pooledCount;
	}

	public int getPooledCount() {
		return pooledCount;
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
