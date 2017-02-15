package it.greenvulcano.gvesb.console.api;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
	
	@Override
	public void start(BundleContext context) throws Exception {
		LOG.info("************ GVConsoleApi start - START");
		
//		ApplicationContext springContext = new ClassPathXmlApplicationContext("application-context.xml");
//
//		repository = (ServiceInstanceRepository)springContext.getBean("serviceInstanceRepository");
//		List<ServiceInstance> allServiceInstanes = repository.findAll();
//		LOG.info("************ allServiceInstanes: " + allServiceInstanes);
//		
		LOG.info("************ GVConsoleApi start - END");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LOG.info("************ GVConsoleApi stop - START");
		
		LOG.info("************ GVConsoleApi stop - END");
	}

}
