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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.script.ScriptExecutor;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.beanutils.BeanUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Helper class for basic metadata substitution in strings.
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class BasicPropertyHandler implements PropertyHandler
{
	private final static Set<String> managedTypes = new HashSet<>();
	
    static {   
       managedTypes.add("%");
       managedTypes.add("$");
       managedTypes.add("sp");
       managedTypes.add("env");
       managedTypes.add("@");
       managedTypes.add("#");
       managedTypes.add("json");
       managedTypes.add("xpath");
       managedTypes.add("timestamp");
       managedTypes.add("dateformat");
       managedTypes.add("decode");
       managedTypes.add("decodeL");
       managedTypes.add("script");
       managedTypes.add("js");
       managedTypes.add("ognl");
       managedTypes.add("escJS");
       managedTypes.add("escSQL");
       managedTypes.add("escXML");
       managedTypes.add("replace");
       managedTypes.add("urlEnc");
       managedTypes.add("urlDec");
       managedTypes.add("xmlp");
       managedTypes.add("enc");
       managedTypes.add("dec");
       
       Collections.unmodifiableSet(managedTypes);
    } 
    
    @Override
	public Set<String> getManagedTypes() {		
		return managedTypes;
	}

	/**
     * This method insert the correct values for the dynamic parameter found in
     * the input string. The property value can be a combination of:
     * 
     * <pre>
     * - fixed : a text string;
     * - %{{class}}         : the obj class name;
     * - %{{fqclass}}       : the obj fully qualified class name;
     * - %{{package}}       : the obj package name;
     * - ${{propname}}      : a System property value;
     * - sp{{propname}}     : a System property value;
     * - env{{varname}}     : an Environment variable value;
     * - @{{propname[::fallback]}} : a inProperties property value and an optional default value;
     * - json{{expression}} : a json expression to parse against object;
     * 	 xmlp{{propname}}   : a inProperties property value, only used by
     *                        XMLConfig on xml files reading;
     * - xpath{{field::path}} : parse the inProperties 'field' value, then
     *                          apply the xpath and return the found value
     * - xpath{{file://name::path}}  : if 'field' begin with 'file://' the following string
     *                                 must be a file in the classpath on which apply the xpath.
     *                                 The metadata is handled by XMLConfig.
     * - timestamp{{pattern[::tZone]]}} : return the current timestamp, in optional tZone value, formatted as 'pattern'
     * - dateformat{{date::source-pattern::dest-pattern[::source-tZone::dest-tZone]}} : reformat 'date' from 'source-pattern' to 'dest-pattern',
     *                          and optionally from 'source-tZone' to 'dest-tZone'   
     * - decode{{field[::cond1::val1][::cond2::val2][cond...n::val...n]::default}} :
     *                          evaluate as if-then-else; if 'field' is equal to cond1...n,
     *                          return the value of val1...n, otherwise 'default'
     * - decodeL{{sep::field[::cond1::val1][::cond2::val2][cond...n::val...n]::default}} :
     *                          is equivalent to 'decode', with the difference that 'condX'
     *                          can be a list of values separated by 'sep'
     * - script{{lang::[scope::]script}} : evaluate a 'lang' script, using the base context 'scope',
     *                           the inProperties map is added to the context as 'inProperties',
     *                           the object is added to the context as 'object',
     *                           the extra is added to the context as 'extra'
     * - js{{[scope::]script}} : evaluate a JavaScript script, using the context 'scope',
     *                           the inProperties map is added to the context as 'inProperties',
     *                           the object is added to the context as 'object',
     *                           the extra is added to the context as 'extra'
     * - ognl{{script}} : evaluate a OGNL script,
     *                    the inProperties map is added to the context as 'inProperties',
     *                    the object is added to the context as 'object' (and is also the object on which execute the script !! NO MORE FROM 3.5 !!),
     *                    the extra is added to the context as 'extra'
     * - escJS{{string}}    : escapes invalid JavaScript characters from 'string' (ex. ' -> \')
     * - escSQL{{string}}   : escapes invalid SQL characters from 'string' (ex. ' -> '')
     * - escXML{{string}}   : escapes invalid XML characters from 'string' (ex. ' -> &apos;)
     * - replace{{string::search::subst}}   : replace in 'string' all occurrences of 'search' with 'replace'
     * - urlEnc{{string}}   : URL encode invalid characters from 'string'
     * - urlDec{{string}}   : decode URL encoded characters from 'string'
     * - enc{{format::string}}   : encode the 'string' in the specified format between base64 (default), hex, url
     * - dec{{format::string}}   : decode the 'string' from the specified format between base64 (default), hex, url
     * </pre>
     * 
     * @param type
     * 
     * @param str
     *        the string to value
     * @param inProperties
     *        the hashTable containing the properties
     * @param object
     *        the object to work with
     * @param extra
     * @return the expanded string
     * @throws PropertiesHandlerException
     */
    @Override
    public String expand(String type, String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        if (type.startsWith("%")) {
            return expandClassProperties(str, inProperties, object, extra);
        }
        else if (type.startsWith("$") || type.startsWith("sp")) {
            return expandSystemProperties(str, inProperties, object, extra);
        }
        else if (type.startsWith("@")) {
            return expandInProperties(str, inProperties, object, extra);
        }
        else if (type.startsWith("#")) {
            return expandObject(str, inProperties, object, extra);
        }
        else if (type.startsWith("json")) {
            return expandJSON(str, inProperties, object, extra);
        }
        else if (type.startsWith("env")) {
            return expandEnvVariable(str, inProperties, object, extra);
        }
        else if (type.startsWith("xpath")) {
            return expandXPathProperties(str, inProperties, object, extra);
        }
        else if (type.startsWith("timestamp")) {
            return expandTimestamp(str, inProperties, object, extra);
        }
        else if (type.startsWith("dateformat")) {
            return expandDateFormat(str, inProperties, object, extra);
        }
        else if (type.startsWith("decodeL")) {
            return expandDecodeL(str, inProperties, object, extra);
        }
        else if (type.startsWith("decode")) {
            return expandDecode(str, inProperties, object, extra);
        }
        else if (type.startsWith("script")) {
            return expandScript(str, inProperties, object, extra);
        }
        else if (type.startsWith("js")) {
            return expandJavaScript(str, inProperties, object, extra);
        }
        else if (type.startsWith("ognl")) {
            return expandOGNL(str, inProperties, object, extra);
        }
        else if (type.startsWith("escJS")) {
            return expandEscJS(str, inProperties, object, extra);
        }
        else if (type.startsWith("escSQL")) {
            return expandEscSQL(str, inProperties, object, extra);
        }
        else if (type.startsWith("escXML")) {
            return expandEscXML(str, inProperties, object, extra);
        }
        else if (type.startsWith("replace")) {
            return expandReplace(str, inProperties, object, extra);
        }
        else if (type.startsWith("urlEnc")) {
            return expandUrlEnc(str, inProperties, object, extra);
        }
        else if (type.startsWith("urlDec")) {
            return expandUrlDec(str, inProperties, object, extra);
        }
        else if (type.startsWith("enc") || type.startsWith("dec")) {
            return expandEncoding(type, str, inProperties, object, extra);
        }
        else if (type.startsWith("xmlp")) {
            // DUMMY replacement - Must be handled by XMLConfig
            return "xmlp" + PROP_START + str + PROP_END;
        }
        return str;
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandSystemProperties(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        String propName = str;
        if (!PropertiesHandler.isExpanded(propName)) {
            propName = PropertiesHandler.expand(propName, inProperties, object, extra);
        }
        String paramValue = System.getProperty(propName, "");
        if (!PropertiesHandler.isExpanded(paramValue)) {
            paramValue = PropertiesHandler.expand(paramValue, inProperties, object, extra);
        }
        str = paramValue;
        return str;
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandEnvVariable(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        String propName = str;
        if (!PropertiesHandler.isExpanded(propName)) {
            propName = PropertiesHandler.expand(propName, inProperties, object, extra);
        }
        String paramValue = System.getenv(propName);
        if (paramValue == null) {
            paramValue = "";
        }
        if (!PropertiesHandler.isExpanded(paramValue)) {
            paramValue = PropertiesHandler.expand(paramValue, inProperties, object, extra);
        }
        str = paramValue;
        return str;
    }

    /**
     * @param str
     *        the string to valorize
     * @param inProperties
     *        the hashTable containing the properties
     * @return the expanded string
     */
    private static String expandInProperties(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        String propName;
        String fallback;
        if (str.matches("^.+::.+$")) {
        	String[] values = str.split("::");
        	propName = values[0];
        	fallback = values[1];
        } else {
        	propName = str;
        	fallback = null;
        }
        
        String paramValue = null;
        if (propName.equals(GVBuffer.OBJECT_REF)){
        	if(object instanceof GVBuffer) {
        		object = GVBuffer.class.cast(object).getObject();
        	} 
       		paramValue = Objects.nonNull(object)? object.toString() : "";
        	
        } else {
        	if (!PropertiesHandler.isExpanded(propName)) {
                propName = PropertiesHandler.expand(propName, inProperties, object, extra);
            }
            
            if (inProperties == null) {
                return "@" + PROP_START + str + PROP_END;
            }
            paramValue = (String) inProperties.get(propName);
            if ((paramValue == null)) {// || (paramValue.equals(""))) {
            	
            	if (fallback!=null) {
            	  return PropertiesHandler.isExpanded(fallback)	?
            			  fallback : PropertiesHandler.expand(fallback, inProperties, object, extra);
            	}
            	
                return "@" + PROP_START + str + PROP_END;
            }
            if (!PropertiesHandler.isExpanded(paramValue)) {
                paramValue = PropertiesHandler.expand(paramValue, inProperties, object, extra);
            }            
        }
        return paramValue;
    }
    
    private static String expandObject(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException {
    	
    	String propName = str;
    	if ("this".equals(propName)){
    		if(object instanceof GVBuffer) {
        		object = GVBuffer.class.cast(object).getObject();
        	} 
       		return Objects.nonNull(object)? object.toString() : "";
    	}
        if (!PropertiesHandler.isExpanded(propName)) {
            propName = PropertiesHandler.expand(propName, inProperties, object, extra);
        }
        
        if (object == null) {
            return "#" + PROP_START + str + PROP_END;
        }
        
        String value = null;
        try {
        	value = BeanUtils.getProperty(object, propName);
        } catch (Exception e) {
        	value = "#" + PROP_START + str + PROP_END;        	
        }
                
        return value;
    }
    
    private static String expandJSON(String str, Map<String, Object> inProperties, Object object, Object extra) throws PropertiesHandlerException {
    	
    	String propName = str;
        if (!PropertiesHandler.isExpanded(propName)) {
            propName = PropertiesHandler.expand(propName, inProperties, object, extra);
        }
        
        String value = null;
        
        Function<Object, Optional<JSONObject>>  objProvider = (Object o) -> {
        	JSONObject jsonObject;
        	 try {
             	
     	        if (o instanceof String) {
     	        	if (o.toString().startsWith("{")) {
     	        		jsonObject = new JSONObject(o.toString());	
     	        	} else if (o.toString().startsWith("[")){
     	        		jsonObject = new JSONObject("{ \"array\":"+o.toString()+"}");
     	        	} else {
     	        		jsonObject = new JSONObject("{ \"value\":\""+o.toString()+"\"}");
     	        	}
     	        } else if (o instanceof JSONArray)  {
    	        	jsonObject = new JSONObject("{ \"array\":"+o.toString()+"}");
    	        	    	             	        	
     	        } else if (o instanceof JSONObject)  {
     	        	jsonObject = JSONObject.class.cast(o);
     	        }  else {
     	        	jsonObject = new JSONObject(o);
     	        }
        	 } catch (Exception e) {
     	    	return Optional.empty();  
     	     }
     	
        	 return Optional.of(jsonObject);
        }; 
        
        try {
        	JSONObject jsonObject; 
        	
        	if (object instanceof GVBuffer) {
        		jsonObject = objProvider.apply(GVBuffer.class.cast(object).getObject()).orElseThrow(IllegalArgumentException::new);
        	} else if (object instanceof byte[])  {
        		String rawdata = new String((byte[])object, "UTF-8");
        		jsonObject = objProvider.apply(rawdata).orElseThrow(IllegalArgumentException::new);					
        	} else {
        		jsonObject = objProvider.apply(object).orElseThrow(IllegalArgumentException::new);
        	}        							
	        
	        if ("this".equals(propName)) {
	        	value = jsonObject.toString();
	        } else if (propName.contains(".")) {
	        	List<String> hieararchy = Arrays.asList(propName.split("\\."));
	        	for (String prop : hieararchy) {
	        		Object child =  null;
	        		if (prop.matches("^.*\\[[0-9]+\\]")) {
	        			String[] arrayRef = prop.split("[\\[,\\]]");
	        			child = jsonObject.getJSONArray(arrayRef[0]).get(Integer.valueOf(arrayRef[1]));	        		
	        		} else {
	        			child = jsonObject.get(prop);
	        		}
	        			        		
	        		if (child instanceof JSONObject) {
	        			jsonObject = JSONObject.class.cast(child);
	        		} else {
	        			value = child.toString();
	        			break;
	        		}
	        	}
	        } else {
	        	if (propName.matches("^.*\\[[0-9]+\\]")) {
        			String[] arrayRef = propName.split("[\\[,\\]]");
        			value = jsonObject.getJSONArray(arrayRef[0]).get(Integer.valueOf(arrayRef[1])).toString();	        		
        		} else {
        			value = jsonObject.get(propName).toString();
        		}
	        }
	        
        } catch (Exception e) {
        	value = "json" + PROP_START + str + PROP_END;
        }
        
        
        return value;
    }

    /**
     * @param str
     *        the string to valorize
     * @param object
     *        the object to work with
     * @return the expanded string
     */
    private static String expandClassProperties(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        String propName = str;
        String paramValue = "";
        if (object == null) {
            return "%" + PROP_START + str + PROP_END;
        }
        if (!PropertiesHandler.isExpanded(propName)) {
            propName = PropertiesHandler.expand(propName, inProperties, object, extra);
        }
        if (propName.equals("fqclass")) {
            paramValue = object.getClass().getName();
        }
        else if (propName.equals("package")) {
            paramValue = object.getClass().getPackage().getName();
        }
        else if (propName.equals("class")) {
            String fqClassName = object.getClass().getName();
            paramValue = fqClassName.substring(fqClassName.lastIndexOf(".") + 1);
        }
        if (paramValue.equals("")) {
            return "%" + PROP_START + str + PROP_END;
        }
        return paramValue;
    }


    private String expandXPathProperties(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        XMLUtils parser = null;
        String paramName = null;
        String paramValue = null;
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            int pIdx = str.indexOf("::");
            paramName = str.substring(0, pIdx);

            String xpath = str.substring(pIdx + 2);
            if (paramName.startsWith("file://")) {
                paramValue = XMLConfig.get(paramName.substring(7), xpath);
            }
            else {
                parser = XMLUtils.getParserInstance();
                DocumentBuilder db = parser.getDocumentBuilder(false, true, true);
                String xmlDoc = (String) inProperties.get(paramName);
                if ((xmlDoc == null) || ("".equals(xmlDoc))) {
                    xmlDoc = "<dummy/>";
                }
                Document doc = db.parse(new InputSource(new StringReader(xmlDoc)));
                paramValue = parser.get(doc, xpath);
            }

            return paramValue;
        }
        catch (Exception exc) {
            System.out.println("Error handling 'xpath' metadata '" + paramName + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'xpath' metadata '" + str + "'", exc);
            }
            return "xpath" + PROP_START + str + PROP_END;
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    private String expandTimestamp(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            String pattern = str;
            int pIdx = str.indexOf("::");
            String tZone = DateUtils.getDefaultTimeZone().getID();
            if (pIdx != -1) {
                tZone = str.substring(pIdx + 2);
                pattern = str.substring(0, pIdx);
            }
            String paramValue = DateUtils.nowToString(pattern, tZone);
            if (paramValue == null) {
                throw new PropertiesHandlerException("Error handling 'timestamp' metadata '" + str
                        + "'. FInvalid format.");
            }
            return paramValue;
        }
        catch (Exception exc) {
            System.out.println("Error handling 'timestamp' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'timestamp' metadata '" + str + "'", exc);
            }
            return "timestamp" + PROP_START + str + PROP_END;
        }
    }

    private String expandDateFormat(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            List<String> parts = TextUtils.splitByStringSeparator(str, "::");
            String sourceTZone = DateUtils.getDefaultTimeZone().getID();
            String destTZone = sourceTZone;
            String date = parts.get(0);
            String sourcePattern = parts.get(1);
            String destPattern = parts.get(2);
            if (parts.size() > 3) {
                sourceTZone = parts.get(3);
                destTZone = parts.get(4);
            }
            String paramValue = DateUtils.convertString(date, sourcePattern, sourceTZone, destPattern, destTZone);
            if (paramValue == null) {
                throw new PropertiesHandlerException("Error handling 'dateformat' metadata '" + str
                        + "'. Invalid format.");
            }
            return paramValue;
        }
        catch (Exception exc) {
            System.out.println("Error handling 'dateformat' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'dateformat' metadata '" + str + "'", exc);
            }
            return "dateformat" + PROP_START + str + PROP_END;
        }
    }

    private String expandDecode(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            String sep = "::";
            int sepLen = sep.length();
            int pIdx = str.indexOf(sep);
            String field = str.substring(0, pIdx);
            boolean match = false;
            int pIdx2 = str.indexOf(sep, pIdx + sepLen);
            String cond = null;
            String val = null;
            while (pIdx2 != -1) {
                cond = str.substring(pIdx + sepLen, pIdx2);
                pIdx = str.indexOf(sep, pIdx2 + sepLen);
                if (cond.equals(field)) {
                    val = str.substring(pIdx2 + sepLen, pIdx);
                    match = true;
                    break;
                }
                pIdx2 = str.indexOf(sep, pIdx + sepLen);
            }
            if (!match) {
                val = str.substring(pIdx + 2);
            }
            return val;
        }
        catch (Exception exc) {
            System.out.println("Error handling 'decode' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'decode' metadata '" + str + "'", exc);
            }
            return "decode" + PROP_START + str + PROP_END;
        }
    }

    private String expandDecodeL(String str, Map<String, Object> inProperties, Object obj,
            Object extra) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, obj, extra);
            }
            String sep = "::";
            int sepLen = sep.length();
            int pIdx = str.indexOf(sep);
            String separator = str.substring(0, pIdx);
            boolean match = false;
            int pIdx2 = str.indexOf(sep, pIdx + sepLen);
            String field = str.substring(pIdx + sepLen, pIdx2);
            pIdx = pIdx2;
            pIdx2 = str.indexOf(sep, pIdx2 + sepLen);
            String condL = null;
            String val = null;
            while (pIdx2 != -1) {
                condL = str.substring(pIdx + sepLen, pIdx2);
                pIdx = str.indexOf(sep, pIdx2 + sepLen);
                List<String> condLV = TextUtils.splitByStringSeparator(condL, separator);
                for (String cond : condLV) {
                    if (cond.equals(field)) {
                        val = str.substring(pIdx2 + sepLen, pIdx);
                        match = true;
                        break;
                    }
                }
                if (match) {
                    break;
                }
                pIdx2 = str.indexOf(sep, pIdx + sepLen);
            }
            if (!match) {
                val = str.substring(pIdx + 2);
            }
            return val;
        }
        catch (Exception exc) {
            System.out.println("Error handling 'decodeL' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'decodeL' metadata'" + str + "'", exc);
            }
            return "decodeL" + PROP_START + str + PROP_END;
        }
    }

    private String expandJavaScript(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        String lStr = str;
        String scopeName = "basic";
        String script = "";
        try {
            if (!PropertiesHandler.isExpanded(lStr)) {
                lStr = PropertiesHandler.expand(lStr, inProperties, object, extra);
            }
            int pIdx = lStr.indexOf("::");
            if (pIdx != -1) {
                scopeName = lStr.substring(0, pIdx);
                script = lStr.substring(pIdx + 2);
            }
            else {
                script = lStr;
            }
            
            return execScript("js", scopeName, script, inProperties, object, extra);
        }
        catch (Exception exc) {
            System.out.println("Error handling 'js' metadata '" + script + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'js' metadata '" + str + "'", exc);
            }
            return "js" + PROP_START + str + PROP_END;
        }
    }

    private String expandOGNL(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }

            return execScript("ognl", null, str, inProperties, object, extra);
        }
        catch (Exception exc) {
            System.out.println("Error handling 'ognl' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'ognl' metadata '" + str + "'", exc);
            }
            return "ognl" + PROP_START + str + PROP_END;
        }
    }
    
    private String expandScript(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        String lStr = str;
        String script = "";
        String lang = null;
        try {
            if (!PropertiesHandler.isExpanded(lStr)) {
                lStr = PropertiesHandler.expand(lStr, inProperties, object, extra);
            }
            String bcName = null;
            List<String> parts = TextUtils.splitByStringSeparator(lStr, "::");
            lang = parts.get(0);
            if (parts.size() > 2) {
                bcName = parts.get(1);
                script = parts.get(2);
            }
            else {
                script = parts.get(1);
            }
            
            return execScript(lang, bcName, script, inProperties, object, extra);
        }
        catch (Exception exc) {
            System.out.println("Error handling 'script[" + lang + "]' metadata '" + script + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'script' metadata '" + str + "'", exc);
            }
            return "script" + PROP_START + str + PROP_END;
        }
    }
    
    private String execScript(String lang, String  bcName, String script, Map<String, Object> inProperties,
            Object object, Object extra) throws Exception
    {
        Map<String, Object> bindings = new HashMap<String, Object>();
        bindings.put("inProperties", inProperties);
        bindings.put("object", object);
        bindings.put("extra", extra);
        
        Object obj = ScriptExecutor.execute(lang, script, bindings, bcName);       
        String paramValue = (obj == null) ? "" : obj.toString();
        
        return paramValue;
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandEscJS(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        String string = str;
        if (!PropertiesHandler.isExpanded(string)) {
            string = PropertiesHandler.expand(string, inProperties, object, extra);
        }
        String escaped = TextUtils.replaceJSInvalidChars(string);
        return escaped;
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandEscSQL(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        String string = str;
        if (!PropertiesHandler.isExpanded(string)) {
            string = PropertiesHandler.expand(string, inProperties, object, extra);
        }
        String escaped = TextUtils.replaceSQLInvalidChars(string);
        return escaped;
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandEscXML(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        String string = str;
        if (!PropertiesHandler.isExpanded(string)) {
            string = PropertiesHandler.expand(string, inProperties, object, extra);
        }
        String escaped = XMLUtils.replaceXMLInvalidChars(string);
        return escaped;
    }

    private String expandReplace(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            int pIdx = str.indexOf("::");
            String string = str.substring(0, pIdx);
            int pIdx2 = str.indexOf("::", pIdx + 2);
            String search = str.substring(pIdx + 2, pIdx2);
            String subst = str.substring(pIdx2 + 2);
            String result = TextUtils.replaceSubstring(string, search, subst);
            return result;
        }
        catch (Exception exc) {
            System.out.println("Error handling 'replace' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'replace' metadata '" + str + "'", exc);
            }
            return "replace" + PROP_START + str + PROP_END;
        }
    }
    
    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandUrlEnc(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
    	try {
    		String string = str;
    		if (!PropertiesHandler.isExpanded(string)) {
    			string = PropertiesHandler.expand(string, inProperties, object, extra);
    		}
    		if (!PropertiesHandler.isExpanded(string)) {
    			return "urlEnc" + PROP_START + str + PROP_END;
    		}
    		return TextUtils.urlEncode(string);
    	}
        catch (Exception exc) {
            System.out.println("Error handling 'urlEnc' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'urlEnc' metadata '" + str + "'", exc);
            }
            return "urlEnc" + PROP_START + str + PROP_END;
        }
    }

    /**
     * @param str
     *        the string to valorize
     * @return the expanded string
     */
    private static String expandUrlDec(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
    	try {
    		String string = str;
    		if (!PropertiesHandler.isExpanded(string)) {
    			string = PropertiesHandler.expand(string, inProperties, object, extra);
    		}
    		if (!PropertiesHandler.isExpanded(string)) {
    			return "urlDec" + PROP_START + str + PROP_END;
    		}
    		return TextUtils.urlDecode(string);
    	}
        catch (Exception exc) {
           
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'urlDec' metadata '" + str + "'", exc);
            }
            return "urlDec" + PROP_START + str + PROP_END;
        }
    }
    
    private static String expandEncoding(String type, String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
    	try {
    		String string = str;
    		
    		if (!PropertiesHandler.isExpanded(string)) {
    			string = PropertiesHandler.expand(string, inProperties, object, extra);
    		}
    		if (!PropertiesHandler.isExpanded(string)) {
    			return "enc" + PROP_START + str + PROP_END;
    		}
    		
    		String encoder = "base64";
    		if (string.matches("^.+::.+$")){
    			String[] parts = string.split("::");
    			encoder = parts[0];
    			string = parts[1];
    		}
    		    		
    		return type.startsWith("enc") ? encode(encoder, string): decode(encoder, string);
    		 		
    		
    	} catch (Exception exc) {
           
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'urlDec' metadata '" + str + "'", exc);
            }
            return "enc" + PROP_START + str + PROP_END;
        }
    }
    
    private static String encode(String encoder, String string) throws UnsupportedEncodingException {
    	switch (encoder) {
			
			case "base64":
				return Base64.getEncoder().encodeToString(string.getBytes());
				
			case "url":
				return URLEncoder.encode(string, "UTF-8");
								
			case "hex":
				return string.chars().mapToObj(Integer::toHexString).collect(Collectors.joining());
				
			default:
				return "enc" + PROP_START + string + PROP_END;
    	}   
    }
    
    private static String decode(String encoder, String string) throws UnsupportedEncodingException {
    	switch (encoder) {
			
			case "base64":
				return new String(Base64.getDecoder().decode(string.getBytes()));
				
			case "url":
				return URLDecoder.decode(string, "UTF-8");
								
			case "hex":
				return Stream.of(string.split("(?<=\\G.{2})"))
						.map(h->(char)Integer.parseInt(h, 16))
						.reduce(new StringBuffer(), (b,h)->b.append(h), (b,b1) ->b.append(b1.toString())).toString();
				
			default:
				return "dec" + PROP_START + string + PROP_END;
    	}   
    }

}
