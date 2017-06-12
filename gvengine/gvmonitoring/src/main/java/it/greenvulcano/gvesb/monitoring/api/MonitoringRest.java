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
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	
	@Path("/monitoring/memory")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getMemory() throws JsonProcessingException{
		
		MemoryStatus memory = new MemoryStatus();
		memory.setMaxMemory((Runtime.getRuntime().maxMemory())/1048576);
		memory.setTotalMemory((Runtime.getRuntime().totalMemory())/1048576);
		memory.setFreeMemory((Runtime.getRuntime().freeMemory())/1048576);
		memory.setHeapMemory(memory.getTotalMemory() - memory.getFreeMemory()); 
		
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(memory)).build();
		
		return response; 
	}	
	
	@Path("/monitoring/cpuUsage")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getCpuUsage() throws Exception,JsonProcessingException{
		
		CPUStatus cpuStatus = new CPUStatus();
		cpuStatus.setCpuUsage();
        
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(cpuStatus.getCpuUsage())).build();
		
		return response;
	}
	
	
	@Path("/monitoring/classes")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getClasses() throws JsonProcessingException{
		
		ClassesStatus classes = new ClassesStatus();
		
		ClassLoadingMXBean mbean = ManagementFactoryHelper.getClassLoadingMXBean();
		
		classes.setTotalLoadedClasses(mbean.getTotalLoadedClassCount());
		classes.setLoadedClasses(mbean.getLoadedClassCount());
		classes.setUnLoadedClasses(mbean.getUnloadedClassCount());
		
		
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(classes)).build();
		
		return response;
	}
	
	
	@Path("/monitoring/threads")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getThreads() throws JsonProcessingException{
		
		ThreadsStatus threads = new ThreadsStatus();
		
		ThreadMXBean threadMXBean = ManagementFactoryHelper.getThreadMXBean();
		
		threads.setTotalThreads(threadMXBean.getThreadCount());
		threads.setDaemonThreads(threadMXBean.getDaemonThreadCount());
		threads.setPeakThreads(threadMXBean.getPeakThreadCount());
		
		Response response = Response.ok(OBJECT_MAPPER.writeValueAsString(threads)).build();
		
		return response;
	}
	
	
}
