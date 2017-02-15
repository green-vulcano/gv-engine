package it.greenvulcano.gvesb.console.api.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.Mapper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class MorphiaFactory
{
	private BundleContext bContext;	
	private List<String> classes;
	
	private static final Logger LOG = LoggerFactory.getLogger(MorphiaFactory.class);
	
	
	private Class<?> loadClass(String className) throws ClassNotFoundException {
		LOG.debug("MorphiaFactory.loadClass - START");
		ClassLoader cl = ((BundleWiring)bContext.getBundle().adapt(BundleWiring.class)).getClassLoader();
		LOG.debug("MorphiaFactory.loadClass - ENDING");
		return cl.loadClass(className);
	}
	
	private Mapper getMapper() {
		LOG.debug("MorphiaFactory.getMapper - START");
		Mapper mapper = new Mapper();
		mapper.getOptions().objectFactory = new BundleObjectFactory(bContext);	
		LOG.debug("MorphiaFactory.getMapper - END - mapper: " + mapper);
		return mapper;
	}
	
	private List<Class<?>> getClassObjects(List<String> classNames) 
		throws ClassNotFoundException {
		LOG.debug("MorphiaFactory.getClassObjects - START - classNames: " + classNames);
		if (classNames == null || classNames.size() == 0)
			return null;
		LOG.debug("MorphiaFactory.getClassObjects - START2 - classNames: " + classNames);
		
		List<Class<?>> classObjs = new ArrayList<>();
		for (String className:classNames) {
			classObjs.add(loadClass(className));
		}
		
		LOG.debug("MorphiaFactory.getClassObjects - END - classObjs: " + classObjs);
		
		return classObjs;
	}
	
	@SuppressWarnings("rawtypes")
	public Morphia get() throws ClassNotFoundException {
		LOG.debug("MorphiaFactory.get - START");
		Mapper mapper = getMapper();	
		List<Class<?>> classObjs = getClassObjects(classes);
		LOG.debug("MorphiaFactory.get - classObjs: " + classObjs);
		
		Morphia morphia = new Morphia(mapper, new HashSet<Class>(classObjs));
		
		LOG.debug("MorphiaFactory.get - morphia: " + morphia);
		
		return morphia;
	}

	public void setbContext(BundleContext bContext) {
		LOG.debug("xxxxxxxxxxxxxxSETTING BUNDLE CONTEXT");
		this.bContext = bContext;
	}
	
	public BundleContext getbContext() {
		LOG.debug("xxxxxxxxxxxxxxGETTING BUNDLE CONTEXT");
		return bContext;
	}

	public void setClasses(List<String> classes) {
		LOG.debug("MorphiaFactory.setClasses - classes: " + classes);
		this.classes = classes;
	}
}