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

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.utils.GVESBPropertyHandler;
import it.greenvulcano.util.metadata.properties.BeanPropertiesHandler;
import it.greenvulcano.util.metadata.properties.ClassPropertiesHandler;
import it.greenvulcano.util.metadata.properties.CodedPropertiesHandler;
import it.greenvulcano.util.metadata.properties.InternalPropertiesHandler;
import it.greenvulcano.util.metadata.properties.JSONPropertiesHandler;
import it.greenvulcano.util.metadata.properties.ScriptPropertiesHandler;
import it.greenvulcano.util.metadata.properties.SystemPropertiesHandler;
import it.greenvulcano.util.metadata.properties.XPathPropertiesHandler;
import it.greenvulcano.util.thread.ThreadMap;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper class for metadata substitution in strings.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 **/
public final class PropertiesHandler {
	private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PropertiesHandler.class);	
    private final static Map<String, PropertyHandler> propHandlers = new ConcurrentHashMap<String, PropertyHandler>();
        
    static {
    	registerHandler(new BasicPropertyHandler());
    	registerHandler(new BeanPropertiesHandler());
    	registerHandler(new ClassPropertiesHandler());
    	registerHandler(new CodedPropertiesHandler());
    	registerHandler(new InternalPropertiesHandler());
    	registerHandler(new JSONPropertiesHandler());
    	registerHandler(new ScriptPropertiesHandler());
    	registerHandler(new SystemPropertiesHandler());
    	registerHandler(new XPathPropertiesHandler());
    	registerHandler(new GVESBPropertyHandler());       
    }

    /**
     * A private empty constructor. Is not possible to instantiate this class.
     */
    private PropertiesHandler()
    {
        // do nothing
    }

    /**
     * Registers a new Property Handler
     * @param handler the property handler to register
     */
    public static void registerHandler(PropertyHandler handler){
    	handler.getManagedTypes().forEach(type->{        	
    		LOG.debug("PropertiesHandler.registerHandler: registering "+type+" -> "+handler.getClass().getName());
        	propHandlers.put(type, handler);	      
        });
    }

    /**
     * Unregisters a property handler
     * @param handler the property handler to unregister
     */
    public static void unregisterHandler(PropertyHandler handler) {
        handler.getManagedTypes().forEach(type->{          
            propHandlers.remove(type);
            LOG.debug("PropertiesHandler.unregisterHandler: unregistered "+type+" -> "+handler.getClass().getName());
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
    public static String expand(String str, Map<String, Object> inProperties, Object obj, Object extra) throws PropertiesHandlerException {
    	
        if (!isExpanded(str)) {        
	        PropertyToken token = null;
	        try {
	            token = PropertiesHandler.parse(str);
	            return token.getValue(Optional.ofNullable(inProperties).orElseGet(LinkedHashMap<String, Object>::new), obj, extra);
	        } catch (PropertiesHandlerException exc) {
	            if (isExceptionOnErrors()) {
	                throw exc;
	            }
	        } catch (Exception exc) {
	            if (isExceptionOnErrors()) {
	                throw new PropertiesHandlerException(exc);
	            }
	        }
        }
        return str;
    }
    
    public static String expand(String str, GVBuffer gvbuffer) throws PropertiesHandlerException{
    	return PropertiesHandler.expand(str, GVBufferPropertiesHelper.getPropertiesMapSO(gvbuffer, true), gvbuffer.getObject());
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
                String type = extractType(pToken).orElse("");
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
    private static Optional<String> extractType(String str) {
    	
    	return propHandlers.keySet().stream().filter(t->endsWith(str, t)).findFirst();    	
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
    public static boolean isExpanded(String str) {
                
        return  Objects.isNull(str) || 
        		str.isEmpty() ||
        		propHandlers.keySet().stream()
        							 .map(t->t.concat(PropertyHandler.PROP_START))
        							 .noneMatch(p->str.contains(p));      
    }
    
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
}
