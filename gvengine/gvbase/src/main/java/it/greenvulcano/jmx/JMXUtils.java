/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.jmx;

import java.util.Iterator;
import java.util.Set;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import org.slf4j.Logger;

/**
 * JMXUtils interface
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 **/
public final class JMXUtils
{
	/**
	 * Constructor
	 */
	private JMXUtils()
	{
		// do nothing
	}

	/**
	 * Call 'invoke' setting 'mustBePresent' to false
	 *
	 * @param jmxFilter
	 *        the ObjectName to find MBeans
	 * @param operationName
	 *        the operation to invoke
	 * @param params
	 *        the operation actual parameters
	 * @param signature
	 *        the operation signature
	 * @param logger
	 *        the logger on which to write error messages, can be null
	 * @return the invocation result
	 * @throws Exception
	 *         if errors occurs
	 */
	public static Object invoke(String jmxFilter, String operationName, Object[] params, String[] signature,
			Logger logger) throws Exception
	{
		return invoke(jmxFilter, operationName, params, signature, false, logger);
	}

	/**
	 * Invoke the 'operationName' on MBeans found by 'jmxFilter'
	 *
	 * @param jmxFilter
	 *        the ObjectName to find MBeans
	 * @param operationName
	 *        the operation to invoke
	 * @param params
	 *        the operation actual parameters
	 * @param signature
	 *        the operation signature
	 * @param mustBePresent
	 *        if true, the operation must be invoked at least once
	 * @param logger
	 *        the logger on which to write error messages, can be null
	 * @return the invocation result
	 * @throws Exception
	 *         if errors occurs
	 */
	public static Object invoke(String jmxFilter, String operationName, Object[] params, String[] signature,
			boolean mustBePresent, Logger logger) throws Exception
	{
		if (logger != null) {
			logger.debug("Calling operation '" + operationName + "' for ObjectName [" + jmxFilter + "]");
		}
		else {
			System.out.println("Calling operation '" + operationName + "' for ObjectName [" + jmxFilter + "]");
		}
		JMXEntryPoint jmx = JMXEntryPoint.getInstance();
		MBeanServer server = jmx.getServer();
		Object output = null;
		boolean executed = false;

		Set<ObjectName> names = server.queryNames(new ObjectName(jmxFilter), null);

		JMException exception = null;

		Iterator<ObjectName> i = names.iterator();
		while (i.hasNext()) {
			executed = true;
			ObjectName name = i.next();
			try {
				output = server.invoke(name, operationName, params, signature);
			}
			catch (JMException exc) {
				if (logger != null) {
					logger.warn("Error calling operation '" + operationName + "' for ObjectName [" + jmxFilter + "] on server '" + name + "'", exc);
				}
				else {
					System.out.println("Error calling operation '" + operationName + "' for ObjectName [" + jmxFilter + "] on server '" + name + "'");
					exc.printStackTrace();
				}
				exception = exc;
			}
		}

		if (exception != null) {
			throw exception;
		}

		if (!executed && mustBePresent) {
			throw new InstanceNotFoundException("No instances found for ObjectName '" + jmxFilter + "'");
		}

		return output;
	}

	/**
	 * Call 'get' setting 'mustBePresent' to false
	 *
	 * @param jmxFilter
	 *        the ObjectName to find MBeans
	 * @param attributeName
	 *        the attribute to read
	 * @param logger
	 *        the logger on which to write error messages, can be null
	 * @return the invocation result
	 * @throws Exception
	 *         if errors occurs
	 */
	public static Object[] get(String jmxFilter, String attributeName, Logger logger) throws Exception
	{
		return get(jmxFilter, attributeName, false, logger);
	}

	/**
	 * Reads the attribute 'attributeName' on MBeans found by 'jmxFilter'
	 *
	 * @param jmxFilter
	 *        the ObjectName to find MBeans
	 * @param attributeName
	 *        the attribute to read
	 * @param mustBePresent
	 *        if true, the attribute must be read at least once
	 * @param logger
	 *        the logger on which to write error messages, can be null
	 * @return the invocation result
	 * @throws Exception
	 *         if errors occurs
	 */
	public static Object[] get(String jmxFilter, String attributeName, boolean mustBePresent, Logger logger)
			throws Exception
	{
		if (logger != null) {
			logger.debug("Reading attribute '" + attributeName + "' for ObjectName [" + jmxFilter + "]");
		}
		else {
			System.out.println("Reading attribute '" + attributeName + "' for ObjectName [" + jmxFilter + "]");
		}

		JMXEntryPoint jmx = JMXEntryPoint.getInstance();
		MBeanServer server = jmx.getServer();
		Object output[] = null;
		boolean executed = false;

		Set<ObjectName> names = server.queryNames(new ObjectName(jmxFilter), null);
		JMException exception = null;

		output = new Object[names.size()];
		int idx = 0;
		Iterator<ObjectName> i = names.iterator();
		while (i.hasNext()) {
			executed = true;
			ObjectName name = i.next();
			try {
				output[idx] = server.getAttribute(name, attributeName);
			}
			catch (JMException exc) {
				if (logger != null) {
					logger.warn("Error getting attribute '" + attributeName + "' for ObjectName [" + jmxFilter + "] on server '" + name + "'", exc);
				}
				else {
					System.out.println("Error getting attribute '" + attributeName + "' for ObjectName [" + jmxFilter + "] on server '" + name + "'");
					exc.printStackTrace();
				}
				exception = exc;
			}
			idx++;
		}

		if (exception != null) {
			throw exception;
		}

		if (!executed && mustBePresent) {
			throw new InstanceNotFoundException("No instances found for ObjectName '" + jmxFilter + "'");
		}

		return output;
	}

	/**
	 * Call 'set' setting 'mustBePresent' to false
	 *
	 * @param jmxFilter
	 *        the ObjectName to find MBeans
	 * @param attributeName
	 *        the attribute name
	 * @param value
	 *        the value to set
	 * @param logger
	 *        the logger on which to write error messages, can be null
	 * @throws Exception
	 *         if errors occurs
	 */
	public static void set(String jmxFilter, String attributeName, Object value, Logger logger) throws Exception
	{
		set(jmxFilter, attributeName, value, false, logger);
	}

	/**
	 * Sets the attribute 'attributeName' to value 'value' on MBeans found by
	 * 'jmxFilter'
	 *
	 * @param jmxFilter
	 *        the ObjectName to find MBeans
	 * @param attributeName
	 *        the attribute name
	 * @param value
	 *        the value to set
	 * @param mustBePresent
	 *        if true, the attribute must be set at least once
	 * @param logger
	 *        the logger on which to write error messages, can be null
	 * @throws Exception
	 *         if errors occurs
	 */
	public static void set(String jmxFilter, String attributeName, Object value, boolean mustBePresent, Logger logger)
			throws Exception
	{
		if (logger != null) {
			logger.debug("Setting attribute '" + attributeName + "' for ObjectName [" + jmxFilter + "]");
		}
		else {
			System.out.println("Setting attribute '" + attributeName + "' for ObjectName [" + jmxFilter + "]");
		}

		JMXEntryPoint jmx = JMXEntryPoint.getInstance();
		MBeanServer server = jmx.getServer();
		boolean executed = false;

		Set<ObjectName> names = server.queryNames(new ObjectName(jmxFilter), null);

		JMException exception = null;
		Attribute attr = new Attribute(attributeName, value);

		Iterator<ObjectName> i = names.iterator();
		while (i.hasNext()) {
			executed = true;
			ObjectName name = i.next();
			try {
				server.setAttribute(name, attr);
			}
			catch (JMException exc) {
				if (logger != null) {
					logger.warn("Error setting attribute '" + attributeName + "' for ObjectName [" + jmxFilter + "] on server '" + name + "'", exc);
				}
				else {
					System.out.println("Error setting attribute '" + attributeName + "' for ObjectName [" + jmxFilter + "] on server '" + name + "'");
					exc.printStackTrace();
				}
				exception = exc;
			}
		}

		if (exception != null) {
			throw exception;
		}

		if (!executed && mustBePresent) {
			throw new InstanceNotFoundException("No instances found for ObjectName '" + jmxFilter + "'");
		}
	}

	/**
	 * Call 'addNotificationListener' setting 'mustBePresent' to false
	 *
	 * @param jmxFilter
	 *        the ObjectName to find MBeans
	 * @param listener
	 *        the listener to register
	 * @param filter
	 *        the notification filter
	 * @param handback
	 *        the listener handback
	 * @param logger
	 *        the logger on which to write error messages, can be null
	 * @throws Exception
	 *         if errors occurs
	 */
	public static void addNotificationListener(String jmxFilter, NotificationListener listener,
			NotificationFilter filter, Object handback, Logger logger) throws Exception
	{
		addNotificationListener(jmxFilter, listener, filter, handback, false, logger);
	}

	/**
	 * Register a NotificationListener on MBeans found by 'jmxFilter'
	 *
	 * @param jmxFilter
	 *        the ObjectName to find MBeans
	 * @param listener
	 *        the listener to register
	 * @param filter
	 *        the notification filter
	 * @param handback
	 *        the listener handback
	 * @param mustBePresent
	 *        if true, the listener must be registered at least once
	 * @param logger
	 *        the logger on which to write error messages, can be null
	 * @throws Exception
	 *         if errors occurs
	 */
	public static void addNotificationListener(String jmxFilter, NotificationListener listener,
			NotificationFilter filter, Object handback, boolean mustBePresent, Logger logger) throws Exception
	{
		JMXEntryPoint jmx = JMXEntryPoint.getInstance();
		MBeanServer server = jmx.getServer();
		boolean executed = false;

		Set<ObjectName> names = server.queryNames(new ObjectName(jmxFilter), null);

		JMException exception = null;

		Iterator<ObjectName> i = names.iterator();
		while (i.hasNext()) {
			executed = true;
			ObjectName name = i.next();
			try {
				server.addNotificationListener(name, listener, filter, handback);
			}
			catch (JMException exc) {
				if (logger != null) {
					logger.warn("Error setting NotificationListener on server '" + name + "'", exc);
				}
				else {
					System.out.println("Error setting NotificationListener on server '" + name + "'");
					exc.printStackTrace();
				}
				exception = exc;
			}
		}

		if (exception != null) {
			throw exception;
		}

		if (!executed && mustBePresent) {
			throw new InstanceNotFoundException("No instances found for ObjectName '" + jmxFilter + "'");
		}
	}

	/**
	 * Call 'removeNotificationListener' setting 'mustBePresent' to false
	 *
	 * @param jmxFilter
	 *        the ObjectName to find MBeans
	 * @param listener
	 *        the listener to unregister
	 * @param logger
	 *        the logger on which to write error messages, can be null
	 * @throws Exception
	 *         if errors occurs
	 */
	public static void removeNotificationListener(String jmxFilter, NotificationListener listener, Logger logger)
			throws Exception
	{
		removeNotificationListener(jmxFilter, listener, false, logger);
	}

	/**
	 * Unregister a NotificationListener from MBeans found by 'jmxFilter'
	 *
	 * @param jmxFilter
	 *        the ObjectName to find MBeans
	 * @param listener
	 *        the listener to unregister
	 * @param mustBePresent
	 *        if true, the listener must be registered at least once
	 * @param logger
	 *        the logger on which to write error messages, can be null
	 * @throws Exception
	 *         if errors occurs
	 */
	public static void removeNotificationListener(String jmxFilter, NotificationListener listener,
			boolean mustBePresent, Logger logger) throws Exception
	{
		JMXEntryPoint jmx = JMXEntryPoint.getInstance();
		MBeanServer server = jmx.getServer();
		boolean executed = false;

		Set<ObjectName> names = server.queryNames(new ObjectName(jmxFilter), null);

		JMException exception = null;

		Iterator<ObjectName> i = names.iterator();
		while (i.hasNext()) {
			executed = true;
			ObjectName name = i.next();
			try {
				server.removeNotificationListener(name, listener);
			}
			catch (JMException exc) {
				if (logger != null) {
					logger.warn("Error removing NotificationListener from server '" + name + "'", exc);
				}
				else {
					System.out.println("Error removing NotificationListener from server '" + name + "'");
					exc.printStackTrace();
				}
				exception = exc;
			}
		}

		if (exception != null) {
			throw exception;
		}

		if (!executed && mustBePresent) {
			throw new InstanceNotFoundException("No instances found for ObjectName '" + jmxFilter + "'");
		}
	}

	/**
	 * Call 'sendNotification' setting 'mustBePresent' to false
	 *
	 * @param jmxFilter
	 *        the ObjectName to find MBeans
	 * @param notification
	 *        the notification to send
	 * @param logger
	 *        the logger on which to write error messages, can be null
	 * @throws Exception
	 *         if errors occurs
	 */
	public static void sendNotification(String jmxFilter, Notification notification, Logger logger) throws Exception
	{
		sendNotification(jmxFilter, notification, false, logger);
	}

	/**
	 * Invoke the 'sendJMXNotification' operation on MBeans found by 'jmxFilter'
	 *
	 * @param jmxFilter
	 *        the ObjectName to find MBeans
	 * @param notification
	 *        the notification to send
	 * @param mustBePresent
	 *        if true, the notification must be sent at least once
	 * @param logger
	 *        the logger on which to write error messages, can be null
	 * @throws Exception
	 *         if errors occurs
	 */
	public static void sendNotification(String jmxFilter, Notification notification, boolean mustBePresent,
			Logger logger) throws Exception
	{
		JMXEntryPoint jmx = JMXEntryPoint.getInstance();
		MBeanServer server = jmx.getServer();
	
		boolean executed = false;

		String[] signature = new String[]{"javax.management.Notification", "javax.management.ObjectName"};
		Set<ObjectName> names = server.queryNames(new ObjectName(jmxFilter), null);
		
		JMException exception = null;

		Iterator<ObjectName> i = names.iterator();
		while (i.hasNext()) {
			executed = true;
			ObjectName name = i.next();
			try {
				Object[] params = new Object[]{notification, name};
				server.invoke(name, "sendJMXNotification", params, signature);
			}
			catch (JMException exc) {
				if (logger != null) {
					logger.warn("Error calling operation 'sendJMXNotification' on server '" + name + "'", exc);
				}
				else {
					System.out.println("Error calling operation 'sendJMXNotification' on server '" + name + "'");
					exc.printStackTrace();
				}
				exception = exc;
			}
		}

		if (exception != null) {
			throw exception;
		}

		if (!executed && mustBePresent) {
			throw new InstanceNotFoundException("No instances found for ObjectName '" + jmxFilter + "'");
		}
	}
}
