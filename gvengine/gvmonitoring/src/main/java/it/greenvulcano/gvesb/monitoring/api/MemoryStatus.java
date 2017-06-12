package it.greenvulcano.gvesb.monitoring.api;


public class MemoryStatus {

	/*The maximum amount of memory available to
    the Java Virtual Machine*/
	private long maxMemory;
	
	/* Total memory allocated from the system
       (which can at most reach the maximum memory value
       returned by the previous function*/
	private long totalMemory;
	
	/*The free memory *within* the total memory
       returned by the previous function */
	private long freeMemory;
	
	//consumed memory (total memory - free memory)
	private long heapMemory;
	
	public long getMaxMemory(){
		return maxMemory;
	}
	
	public void setMaxMemory(long maxMemory){
		this.maxMemory = maxMemory;
	}
	public long getTotalMemory() {
		return totalMemory;
	}
	public void setTotalMemory(long totalMemory) {
		this.totalMemory = totalMemory;
	}
	public long getFreeMemory() {
		return freeMemory;
	}
	public void setFreeMemory(long freeMemory) {
		this.freeMemory = freeMemory;
	}
	public long getHeapMemory() {
		return heapMemory;
	}
	public void setHeapMemory(long heapMemory) {
		this.heapMemory = heapMemory;
	}
	
	
}
