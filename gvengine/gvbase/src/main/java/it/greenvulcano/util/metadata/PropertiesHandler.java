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
package it.greenvulcano.util.metadata;

import it.greenvulcano.util.thread.ThreadMap;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

/**
 * Helper class for metadata substitution in strings.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 **/
public final class PropertiesHandler {
	private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PropertiesHandler.class);
	
	/**
     * @version 3.0.0 Feb 27, 2010
     * @author nunzio
     *
     */
    public final class MetaDataTokenizer
    {
        private Vector<String> tokens  = new Vector<String>();
        private int            lastPos = 0;

        /**
         * @param string
         */
        public MetaDataTokenizer(String string)
        {
            parse(string, 0);
        }

        /**
         * @return it there is another token
         */
        public boolean hasNext()
        {
            return (lastPos < tokens.size());
        }

        /**
         * @return the next token
         */
        public String next()
        {
            return tokens.get(lastPos++);
        }

        /**
         *
         */
        public void pushBack()
        {
            lastPos--;
            if (lastPos < -1) {
                lastPos = -1;
            }
        }

        private void parse(String string, int index)
        {
            if (string == null) {
                return;
            }
            if (index == string.length()) {
                return;
            }
            int begin = string.indexOf(PropertyHandler.PROP_START, index);
            int end = string.indexOf(PropertyHandler.PROP_END, index);
            String terminator = "";
            int pos = -1;
            if (begin == -1) {
                if (end == -1) {
                    tokens.add(string.substring(index));
                    return;
                }
                terminator = PropertyHandler.PROP_END;
                pos = end;
            }
            else {
                if (end == -1) {
                    terminator = PropertyHandler.PROP_START;
                    pos = begin;
                }
                else {
                    pos = (begin < end) ? begin : end;
                    terminator = (begin < end) ? PropertyHandler.PROP_START : PropertyHandler.PROP_END;
                }
            }
            tokens.add(string.substring(index, pos));
            tokens.add(terminator);
            parse(string, pos + PropertyHandler.PROP_START.length());
        }
    }

    private static HashMap<String, PropertyHandler> propHandlers = new HashMap<String, PropertyHandler>();
    private static HashSet<String>                  propSet      = new HashSet<String>();
    
    static {
    	Path cfgFilePath = null;
        try {
        	String configurationDir = System.getProperty("gv.app.home") + File.separator + "xmlconfig" + File.separator;
        	File cfgFile = new File(configurationDir+"PropertiesHandler.properties");
        	
        	if (cfgFile.exists()) {
        		cfgFilePath = cfgFile.toPath();
        	} else {        		
        		cfgFilePath = Paths.get(ClassLoader.getSystemResource("PropertiesHandler.properties").toURI());
        	}        	
        	
        	
        	 if (cfgFilePath!=null) {
             	
             	Files.lines(cfgFilePath).map(clazz-> {
             		Optional<PropertyHandler> propertyHandler = Optional.empty();
             		try {
             			PropertyHandler p = (PropertyHandler)Class.forName(clazz).newInstance();
             			propertyHandler = Optional.of(p);
             		} catch (Exception exception) {
             			LOG.error("PropertiesHandler: unable to load "+clazz, exception);
             		}
             		return propertyHandler;
             	})
             	.filter(Optional::isPresent)
             	.map(Optional::get)
             	.forEach(PropertiesHandler::registerHandler);              
             
             }        	
        	
    	} catch (Exception exc) {
    		LOG.error("PropertiesHandler: unable to load file PropertiesHandler.properties", exc);
    		
    		registerHandler(new BasicPropertyHandler());
        } 
       
       
    }

    /**
     * A private empty constructor. Is not possible to instantiate this class.
     */
    private PropertiesHandler()
    {
        // do nothing
    }

    /**
     * @param type
     * @param handler
     */
    private static void registerHandler(PropertyHandler handler){
    	handler.getManagedTypes().forEach(type->{        	
    		LOG.debug("PropertiesHandler.registerHandler: registering "+type+" -> "+handler.getClass().getName());
        	propHandlers.put(type, handler);
	        propSet.add(type);
        });
    }

    /**
     * This method insert the correct values for the dynamic parameter found in
     * the input string.
     * The property value can be a combination of:
     * - fixed : a text string;
     * - ${{propname}} : a System property name.
     *
     * @param str
     *        the string to value
     * @return the expanded string
     *
     * @throws PropertiesHandlerException
     *         if error occurs and the flag THROWS_EXCEPTION is set
     */
    public static String expand(String str) throws PropertiesHandlerException
    {
        return expand(str, null, null, null);
    }

    /**
     * This method insert the correct values for the dynamic parameter found in
     * the input string.
     * The property value can be a combination of:
     * - fixed : a text string;
     * - ${{propname}} : a System property name;
     * - @{{propname}} : a inProperties property name;
     *
     * @param str
     *        the string to value
     * @param inProperties
     *        the hashTable containing the properties
     * @return the expanded string
     *
     * @throws PropertiesHandlerException
     *         if error occurs and the flag THROWS_EXCEPTION is set
     */
    public static String expand(String str, Map<String, Object> inProperties) throws PropertiesHandlerException
    {
        return expand(str, inProperties, null, null);
    }

    /**
     * This method insert the correct values for the dynamic parameter found in
     * the input string.
     * The property value can be a combination of:
     * - fixed : a text string;
     * - %{{class}} : the object class name;
     * - %{{fqclass}} : the object fully qualified class name;
     * - %{{package}} : the object package name;
     * - ${{propname}} : a System property name;
     * - @{{propname}} : a inProperties property name;
     *
     * @param str
     *        the string to value
     * @param inProperties
     *        the hashTable containing the properties
     * @param obj
     *        the object to work with
     * @return the expanded string
     *
     * @throws PropertiesHandlerException
     *         if error occurs and the flag THROWS_EXCEPTION is set
     */
    public static String expand(String str, Map<String, Object> inProperties, Object obj)
            throws PropertiesHandlerException
    {
        return expand(str, inProperties, obj, null);
    }


    /**
     * This method insert the correct values for the dynamic parameter found in
     * the input string.
     * The property value can be a combination of:
     * - fixed : a text string;
     * - %{{class}} : the object class name;
     * - %{{fqclass}} : the object fully qualified class name;
     * - %{{package}} : the object package name;
     * - ${{propname}} : a System property name;
     * - @{{propname}} : a inProperties property name;
     * - &{{script}} : a JavaScript script;
     *
     * @param str
     *        the string to value
     * @param inProperties
     *        the hashTable containing the properties
     * @param obj
     *        the object to work with
     * @param extra
     *        a extra object passed to property handlers
     * @return the expanded string
     *
     * @throws PropertiesHandlerException
     *         if error occurs and the flag THROWS_EXCEPTION is set
     */
    public static String expand(String str, Map<String, Object> inProperties, Object obj, Object extra)
            throws PropertiesHandlerException
    {
        if (str == null) {
            return null;
        }
        if (inProperties == null) {
            inProperties = new HashMap<String, Object>();
        }
        PropertyToken token = null;
        try {
            token = PropertiesHandler.parse(str);
            return token.getValue(inProperties, obj, extra);
        }
        catch (PropertiesHandlerException exc) {
            if (isExceptionOnErrors()) {
                throw exc;
            }
        }
        catch (Exception exc) {
            if (isExceptionOnErrors()) {
                throw new PropertiesHandlerException(exc);
            }
        }
        return null;
    }

    /**
     * @param type
     * @param value
     * @param inProperties
     * @param obj
     * @param extra
     *        a extra object passed to property handlers
     * @return the expanded string
     * @throws PropertiesHandlerException
     */
    public static String expandInternal(String type, String value, Map<String, Object> inProperties, Object obj,
            Object extra) throws PropertiesHandlerException
    {
        PropertyHandler handler = propHandlers.get(type);
        if (handler == null) {
            return value;
        }
        return handler.expand(type, value, inProperties, obj, extra);
    }

    private static PropertyToken parse(String str)
    {
        PropertyToken token = new PropertyToken(0, 0, "", "");
        MetaDataTokenizer mdt = new PropertiesHandler().new MetaDataTokenizer(str);
        parse(token, mdt);
        return token;
    }

    /**
     * @param token
     * @param mdt
     */
    private static void parse(PropertyToken token, MetaDataTokenizer mdt)
    {
        String pToken = null;
        while (mdt.hasNext()) {
            String sToken = mdt.next();
            if (sToken.equals(PropertyHandler.PROP_START)) {
                String type = extractType(pToken);
                String staticToken = pToken.substring(0, pToken.lastIndexOf(type));
                PropertyToken subToken = null;
                if (staticToken.length() > 0) {
                    subToken = new PropertyToken(0, 0, staticToken, "");
                    token.addSubToken(subToken);
                }
                subToken = new PropertyToken(0, 0, "", type);
                token.addSubToken(subToken);
                parse(subToken, mdt);
            }
            else if (sToken.equals(PropertyHandler.PROP_END)) {
                break;
            }
            else {
                if (mdt.hasNext()) {
                    String nToken = mdt.next();
                    if (nToken.equals(PropertyHandler.PROP_START)) {
                        pToken = sToken;
                    }
                    else {
                        PropertyToken subToken = new PropertyToken(0, 0, sToken, "");
                        token.addSubToken(subToken);
                    }
                    mdt.pushBack();
                }
                else {
                    PropertyToken subToken = new PropertyToken(0, 0, sToken, "");
                    token.addSubToken(subToken);
                }
            }
        }
    }

    /**
     * Enable the exception throwing on errors, for the current thread.
     *
     * Example:
     *
     * <pre>
     *
     *     ...
     *     PropertiesHandler.enableExceptionOnErrors();
     *     try {
     *        ...
     *        String value = PropertiesHandler.expand(...);
     *        ...
     *     }
     *     catch (PropertiesHandlerException exc) {
     *        ...
     *     }
     *     finally {
     *        PropertiesHandler.disableExceptionOnErrors();
     *     }
     *
     * </pre>
     */
    public static void enableExceptionOnErrors()
    {
        ThreadMap.put(PropertyHandler.THROWS_EXCEPTION, "true");
    }

    /**
     * Disable the exception throwing on errors, for the current thread.
     *
     */
    public static void disableExceptionOnErrors()
    {
        ThreadMap.remove(PropertyHandler.THROWS_EXCEPTION);
    }

    /**
     * Check if the exception throwing on errors is enabled for the current
     * thread.
     *
     * @return if the exception throwing on errors is enabled for the current
     *         thread.
     *
     */
    public static boolean isExceptionOnErrors()
    {
        return "true".equals(ThreadMap.get(PropertyHandler.THROWS_EXCEPTION));
    }

    /**
     * @param str
     * @return
     */
    private static String extractType(String str)
    {
        String type = "";
        Iterator<String> i = propSet.iterator();
        while (i.hasNext()) {
            String currType = (String) i.next();
            if (endsWith(str, currType)) {
                type = currType;
                break;
            }
        }
        return type;
    }

    private static boolean endsWith(String str, String value)
    {
        if (value.length() == 0) {
            return false;
        }
        int begin = str.length() - 1;
        int end = begin - value.length();
        int index = value.length() - 1;
        if (begin < index) {
            return false;
        }
        while (begin > end) {
            if (str.charAt(begin) != value.charAt(index)) {
                return false;
            }
            index--;
            begin--;
        }
        return true;
    }

    /**
     * Check if the input string need to be processed.
     *
     * @param str
     *        the string to check
     * @return true if the string is processed
     */
    public static boolean isExpanded(String str)
    {
        if ((str == null) || (str.length() == 0)) {
            return true;
        }
        Iterator<String> i = propSet.iterator();
        while (i.hasNext()) {
            if (str.indexOf(((String) i.next() + PropertyHandler.PROP_START)) != -1) {
                return false;
            }
        }
        return true;
    }
}
