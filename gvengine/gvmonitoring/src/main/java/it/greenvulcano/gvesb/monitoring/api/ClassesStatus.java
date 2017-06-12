package it.greenvulcano.gvesb.monitoring.api;

public class ClassesStatus {
	
	/*Total number of classes that have been loaded since the
      Java virtual machine has started execution. */
	private long totalLoadedClasses;
	
	/*The number of classes that are currently loaded in the Java
      virtual machine. */
	private long loadedClasses;
	
	/*The total number of classes unloaded since the Java virtual
      machine has started execution. */
	private long unLoadedClasses;

	public long getTotalLoadedClasses() {
		return totalLoadedClasses;
	}

	public void setTotalLoadedClasses(long totalLoadedClasses) {
		this.totalLoadedClasses = totalLoadedClasses;
	}

	public long getLoadedClasses() {
		return loadedClasses;
	}

	public void setLoadedClasses(long loadedClasses) {
		this.loadedClasses = loadedClasses;
	}

	public long getUnLoadedClasses() {
		return unLoadedClasses;
	}

	public void setUnLoadedClasses(long unLoadedClasses) {
		this.unLoadedClasses = unLoadedClasses;
	}
	
	
	

}
