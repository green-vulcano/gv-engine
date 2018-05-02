package it.greenvulcano.jmx.impl;

import java.util.Map;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.jmx.JMXEntryPoint;

public class DummyJMXEntryPoint extends JMXEntryPoint {

	private final String DUMMY = "it.greenvulcano.gvesb:type=dummy,name=";
	
	@Override
	public void configurationChanged(ConfigurationEvent evt) {
		// do nothing

	}

	@Override
	public void registerObject(Object object, String descriptorName, ObjectName oname) throws Exception {
		// do nothing

	}

	@Override
	public ObjectName registerObject(Object object, String descriptorName) throws Exception {
		// do nothing
		return new ObjectName(DUMMY + descriptorName);
	}

	@Override
	public ObjectName registerObject(Object object, String descriptorName, String key, String value) throws Exception {
		// do nothing
		return new ObjectName(DUMMY + descriptorName);
	}

	@Override
	public ObjectName registerObject(Object object, String descriptorName, Map<String, String> keyProperties)
			throws Exception {
		// do nothing
		return new ObjectName(DUMMY + descriptorName);
	}

	@Override
	public void registerMBean(Object mbean, ObjectName oname) throws Exception {
		// do nothing

	}

	@Override
	public ObjectName registerMBean(Object mbean, String descriptorName, Map<String, String> keyProperties)
			throws Exception {
		// do nothing
		return new ObjectName(DUMMY + descriptorName);
	}

	@Override
	public void unregisterObject(ObjectName oname) {
		// do nothing

	}

	@Override
	public ObjectName unregisterObject(String descriptorName) throws Exception {
		// do nothing
		return new ObjectName(DUMMY + descriptorName);
	}

	@Override
	public ObjectName unregisterObject(String descriptorName, String key, String value) throws Exception {
		// do nothing
		return new ObjectName(DUMMY + descriptorName);
	}

	@Override
	public ObjectName unregisterObject(String descriptorName, Map<String, String> keyProperties) throws Exception {
		// do nothing
		return new ObjectName(DUMMY + descriptorName);
	}

	@Override
	public ObjectName unregisterObject(Object object, String descriptorName, Map<String, String> keyProperties)
			throws Exception {
		// do nothing
		return new ObjectName(DUMMY + descriptorName);
	}

	@Override
	public ObjectName unregisterMBean(String descriptorName, Map<String, String> keyProperties) throws Exception {
		// do nothing
		return new ObjectName(DUMMY + descriptorName);
	}

	@Override
	public ObjectName unregisterMBean(Object mbean, String descriptorName, Map<String, String> keyProperties)
			throws Exception {
		
		return new ObjectName(DUMMY + descriptorName);
	}


	@Override
	public String getServerName() {		
		return "DUMMY";
	}

	@Override
	public MBeanServer getServer() {		
		return null;
	}

}
