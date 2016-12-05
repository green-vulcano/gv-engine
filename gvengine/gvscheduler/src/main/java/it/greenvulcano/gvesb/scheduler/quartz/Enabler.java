package it.greenvulcano.gvesb.scheduler.quartz;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Objects;
import java.util.Properties;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public class Enabler {
	private final static String QUARTZ_PID = "it.greenvulcano.gvesb.quartz";
		
	public static Scheduler build(ConfigurationAdmin configAdmin) throws IOException, SchedulerException{		

		Properties quartzProperties = new Properties();
		Configuration config = configAdmin.getConfiguration(QUARTZ_PID); 
		
		if (Objects.nonNull(config)) {
			final Dictionary<String, Object> props = config.getProperties();
			Collections.list(props.keys())
					   .stream()
					   .forEach(k -> quartzProperties.put(k, props.get(k)));
		}
		
		StdSchedulerFactory factory = new StdSchedulerFactory(quartzProperties);
		return factory.getScheduler();
	
	}
	
}
