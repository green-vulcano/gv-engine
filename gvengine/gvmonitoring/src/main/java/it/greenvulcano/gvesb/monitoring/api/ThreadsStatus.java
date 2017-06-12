package it.greenvulcano.gvesb.monitoring.api;

public class ThreadsStatus {
	
	//Current number of live threads including both daemon and non-daemon threads
	private long totalThreads;
	
	//The current number of live daemon threads
	private long daemonThreads;
	
	//The peak live thread count since the Java virtual machine started or peak was reset
	private long peakThreads;

	public long getTotalThreads() {
		return totalThreads;
	}

	public void setTotalThreads(long totalThreads) {
		this.totalThreads = totalThreads;
	}

	public long getDaemonThreads() {
		return daemonThreads;
	}

	public void setDaemonThreads(long daemonThreads) {
		this.daemonThreads = daemonThreads;
	}

	public long getPeakThreads() {
		return peakThreads;
	}

	public void setPeakThreads(long peakThreads) {
		this.peakThreads = peakThreads;
	}
	
	

}
