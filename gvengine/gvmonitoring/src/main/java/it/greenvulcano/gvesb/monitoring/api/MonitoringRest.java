package it.greenvulcano.gvesb.monitoring.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;
import sun.management.ManagementFactoryHelper;

import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.ClassLoadingMXBean;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;


@SuppressWarnings("restriction")
@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true)
public class MonitoringRest {
	
	private long freeMemory;
	private long maxMemory;
	private long totalMemory;
	private long heapMemory;
	private ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private long prevUpTime;
	private long prevProcessCpuTime;
	
	
	@Path("/monitoring/maxMemory")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getMaxMemory() throws JsonProcessingException{
		
		maxMemory = (Runtime.getRuntime().maxMemory())/1048576;
		
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(maxMemory)).build();
		
		return response;
	}	
	
	
	@Path("/monitoring/totalMemory")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getTotalMemory() throws JsonProcessingException{
		
		totalMemory = (Runtime.getRuntime().totalMemory())/1048576;
		
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(totalMemory)).build();
		
		return response;
	}
	
	
	@Path("/monitoring/freeMemory")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getFreeMemory() throws JsonProcessingException{		
				
		freeMemory = (Runtime.getRuntime().freeMemory())/1048576;
		
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(freeMemory)).build();
				
		return response;
	}	
	
	
	@Path("/monitoring/heapMemory")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getHeapMemory() throws JsonProcessingException{
		
		//vedere perchè il primo risultato è quasi sempre falso(0 o valore negativo della differenza)
		heapMemory = totalMemory - freeMemory;
		
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(heapMemory)).build();
		
		return response;
	}
	
	
	@Path("/monitoring/cpuUsage")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getCpuUsage() throws Exception,JsonProcessingException{
		
		RuntimeMXBean bean = getRuntimeMXBean();
		long upTime;
		long processCpuTime;
		OperatingSystemMXBean sunOSMBean;
		prevUpTime = 0L;
		prevProcessCpuTime = 0L;
		float cpuUsage;
		sunOSMBean = (OperatingSystemMXBean) getOperatingSystemMXBean();
		
        prevUpTime = bean.getUptime();
        prevProcessCpuTime = sunOSMBean.getProcessCpuTime(); 
        
        //Messo solo per far lavorare la cpu e farmi dare un valore visibile
        for(int i=0;i<300000000;i++){
        	
        }
        
        upTime = bean.getUptime();
        processCpuTime = sunOSMBean.getProcessCpuTime();
        
        if (prevUpTime > 0L && upTime > prevUpTime) {
            // elapsedCpu is in ns and elapsedTime is in ms.
            long elapsedCpu = processCpuTime - prevProcessCpuTime;
            long elapsedTime = upTime - prevUpTime;
            // cpuUsage could go higher than 100% because elapsedTime and
            // elapsedCpu are not fetched simultaneously. Limit to 99% to avoid
            // Plotter showing a scale from 0% to 200%.
            cpuUsage = Math.min(99F, elapsedCpu / (elapsedTime * 10000F * Runtime.getRuntime().availableProcessors()));
            cpuUsage = (Math.round(cpuUsage * Math.pow(10.0, 1)))/10;
        }
        else {
            cpuUsage = 0;
        }
        prevUpTime = upTime;
        prevProcessCpuTime = processCpuTime;
        
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(cpuUsage)).build();
		
		return response;
	}
	
	
	@Path("/monitoring/totalLoadedClasses")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getTotalLoadedClasses() throws JsonProcessingException{
		
		ClassLoadingMXBean mbean = ManagementFactoryHelper.getClassLoadingMXBean();
		
		// Returns the total number of classes that have been loaded since the
        // Java virtual machine has started execution.
		long totalLoadedClassCount = mbean.getTotalLoadedClassCount();
		
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(totalLoadedClassCount)).build();
		
		return response;
	}
	
	@Path("/monitoring/loadedClasses")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getLoadedClasses() throws JsonProcessingException{
		
		ClassLoadingMXBean mbean = ManagementFactoryHelper.getClassLoadingMXBean();
		
		// Returns the number of classes that are currently loaded in the Java
        // virtual machine.
		long loadedClassCount = mbean.getLoadedClassCount();	
		
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(loadedClassCount)).build();
		
		return response;
	}
	
	@Path("/monitoring/unLoadedClasses")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getUnloadedClasses() throws JsonProcessingException{
		
		ClassLoadingMXBean mbean = ManagementFactoryHelper.getClassLoadingMXBean();
		
		// Returns the total number of classes unloaded since the Java virtual
        // machine has started execution.
		long unLoadedClassCount = mbean.getUnloadedClassCount();	
		
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(unLoadedClassCount)).build();
		
		return response;
	}
	
	
	@Path("/monitoring/threads")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getThreads() throws JsonProcessingException{
		
		ThreadMXBean threadMXBean = ManagementFactoryHelper.getThreadMXBean();
		
		// Returns the current number of live threads including both daemon and
        // non-daemon threads.
		int threadCount = threadMXBean.getThreadCount();
		
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(threadCount)).build();
		
		return response;
	}
	
	@Path("/monitoring/daemonThreads")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getDaemonThreads() throws JsonProcessingException{
		
		ThreadMXBean threadMXBean = ManagementFactoryHelper.getThreadMXBean();
		
		// Returns the current number of live daemon threads.
		int daemonThreadCount = threadMXBean.getDaemonThreadCount();
		
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(daemonThreadCount)).build();
		
		return response;
	}
	
	@Path("/monitoring/peakThreads")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getPeakThreads() throws JsonProcessingException{
		
		ThreadMXBean threadMXBean = ManagementFactoryHelper.getThreadMXBean();
		
		// Returns the peak live thread count since the Java virtual machine
        // started or peak was reset.
		int peakThreadCount = threadMXBean.getPeakThreadCount();
		
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(peakThreadCount)).build();
		
		return response;
	}
	
	
}
