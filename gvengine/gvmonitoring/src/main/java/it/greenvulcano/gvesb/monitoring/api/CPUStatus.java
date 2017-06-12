package it.greenvulcano.gvesb.monitoring.api;

import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;

import java.lang.management.RuntimeMXBean;

import com.sun.management.OperatingSystemMXBean;

@SuppressWarnings({ "restriction"})
public class CPUStatus {
	
	private long upTime;
	private long processCpuTime;
	private long prevUpTime;
	private long prevProcessCpuTime;
	private double cpuUsage;
	private RuntimeMXBean bean;
	
	
	private OperatingSystemMXBean sunOSMBean;
	
	public long getUpTime() {
		return upTime;
	}
	public void setUpTime(long upTime) {
		this.upTime = upTime;
	}
	public long getProcessCpuTime() {
		return processCpuTime;
	}
	public void setProcessCpuTime(long processCpuTime) {
		this.processCpuTime = processCpuTime;
	}
	public long getPrevUpTime() {
		return prevUpTime;
	}
	public void setPrevUpTime(long prevUpTime) {
		this.prevUpTime = prevUpTime;
	}
	public long getPrevProcessCpuTime() {
		return prevProcessCpuTime;
	}
	public void setPrevProcessCpuTime(long prevProcessCpuTime) {
		this.prevProcessCpuTime = prevProcessCpuTime;
	}
	public double getCpuUsage(){
		return cpuUsage;
	}
	public void setCpuUsage(){
		
		setBean(getRuntimeMXBean());
		setSunOSMBean((OperatingSystemMXBean) getOperatingSystemMXBean());
		setPrevUpTime(getBean().getUptime());
		setPrevProcessCpuTime(getSunOSMBean().getProcessCpuTime());
		
		//Messo solo per far lavorare la cpu e farmi dare un valore visibile
		for(int i=0;i<300000000;i++){
        	
        }
		
		setUpTime(getBean().getUptime());
		setProcessCpuTime(getSunOSMBean().getProcessCpuTime());
		long prevUpTime = getPrevUpTime();
		long prevProcessCpuTime = getPrevProcessCpuTime();
		long upTime = getUpTime();
		long processCpuTime = getProcessCpuTime();
		
        
        if (prevUpTime > 0L && upTime > prevUpTime) {
            // elapsedCpu is in ns and elapsedTime is in ms.
            long elapsedCpu = processCpuTime - prevProcessCpuTime;
            long elapsedTime = upTime - prevUpTime;
            // cpuUsage could go higher than 100% because elapsedTime and
            // elapsedCpu are not fetched simultaneously. Limit to 99% to avoid
            // Plotter showing a scale from 0% to 200%.
            cpuUsage = Math.min(99F, elapsedCpu / (elapsedTime * 10000F * Runtime.getRuntime().availableProcessors()));
            cpuUsage = (Math.round(getCpuUsage() * Math.pow(10.0, 1)))/10;
        }
        else {
            cpuUsage = 0;
        }
        prevUpTime = upTime;
        prevProcessCpuTime = processCpuTime;
        
	}	
	public OperatingSystemMXBean getSunOSMBean() {
		return sunOSMBean;
	}	
	public void setSunOSMBean(OperatingSystemMXBean sunOSMBean) {
		this.sunOSMBean = sunOSMBean;
	}
	public RuntimeMXBean getBean() {
		return bean;
	}
	public void setBean(RuntimeMXBean bean) {
		this.bean = bean;
	}
	
}
