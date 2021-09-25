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

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.greenvulcano.util.file.cache.FileCache;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;

/**
 * Helper class for basic metadata substitution in strings.
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class BasicPropertyHandler implements PropertyHandler {
	private final static List<String> managedTypes = new LinkedList<>();

	static {
		
		managedTypes.add("timestamp");
		managedTypes.add("dateformat");
		managedTypes.add("dateAdd");
		managedTypes.add("dateformatAdd");
		managedTypes.add("decode");
		managedTypes.add("decodeL");		
		managedTypes.add("escJS");
		managedTypes.add("escSQL");
		managedTypes.add("escXML");
		managedTypes.add("replace");
		managedTypes.add("urlEnc");
		managedTypes.add("urlDec");
		managedTypes.add("file");
		managedTypes.add("xmlp");

		Collections.unmodifiableList(managedTypes);
	}

	@Override
	public List<String> getManagedTypes() {
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
	 * - file{{path[::cached[::format]]}}   : return the content of file 'path' encoded as 'format', and set the content in cache if 'cache' = Y (default, cached entries are cleared 5 minutes after last access)
	 *                                        possible format value are:
	 *                                        - text (default): the file is assumed to contain UTF-8 text
	 *                                        - base64 : the file content is encoded as base 64 string
	 * </pre>
	 * 
	 * @param type
	 * 
	 * @param str
	 *            the string to value
	 * @param inProperties
	 *            the hashTable containing the properties
	 * @param object
	 *            the object to work with
	 * @param extra
	 * @return the expanded string
	 * @throws PropertiesHandlerException
	 */
	@Override
	public String expand(String type, String str, Map<String, Object> inProperties, Object object, Object extra)
			throws PropertiesHandlerException {
		if (type.startsWith("timestamp")) {
			return expandTimestamp(str, inProperties, object, extra);
		} else if (type.startsWith("dateformatAdd")) {
			return expandDateFormatAdd(str, inProperties, object, extra);
		} else if (type.startsWith("dateformat")) {
			return expandDateFormat(str, inProperties, object, extra);
		} else if (type.startsWith("dateAdd")) {
			return expandDateAdd(str, inProperties, object, extra);
		} else if (type.startsWith("decodeL")) {
			return expandDecodeL(str, inProperties, object, extra);
		} else if (type.startsWith("decode")) {
			return expandDecode(str, inProperties, object, extra);
		} else if (type.startsWith("escJS")) {
			return expandEscJS(str, inProperties, object, extra);
		} else if (type.startsWith("escSQL")) {
			return expandEscSQL(str, inProperties, object, extra);
		} else if (type.startsWith("escXML")) {
			return expandEscXML(str, inProperties, object, extra);
		} else if (type.startsWith("replace")) {
			return expandReplace(str, inProperties, object, extra);
		} else if (type.startsWith("urlEnc")) {
			return expandUrlEnc(str, inProperties, object, extra);
		} else if (type.startsWith("urlDec")) {
			return expandUrlDec(str, inProperties, object, extra);
		} else if (type.startsWith("file")) {
			return expandFile(str, inProperties, object, extra);
		} else if (type.startsWith("xmlp")) {
			// DUMMY replacement - Must be handled by XMLConfig
			return "xmlp" + PROP_START + str + PROP_END;
		}
		return str;
	}

    @Override
    public void cleanupResources() {
    	// do nothing
    }

	private String expandTimestamp(String str, Map<String, Object> inProperties, Object object, Object extra)
			throws PropertiesHandlerException {
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
				throw new PropertiesHandlerException(
						"Error handling 'timestamp' metadata '" + str + "'. FInvalid format.");
			}
			return paramValue;
		} catch (Exception exc) {
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

	private String expandDateFormat(String str, Map<String, Object> inProperties, Object object, Object extra)
			throws PropertiesHandlerException {
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
				throw new PropertiesHandlerException(
						"Error handling 'dateformat' metadata '" + str + "'. Invalid format.");
			}
			return paramValue;
		} catch (Exception exc) {
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

    private String expandDateAdd(String str, Map<String, Object> inProperties, Object object, 
            Object extra) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            String intType = "";
            List<String> parts = TextUtils.splitByStringSeparator(str, "::");
            String date = parts.get(0);
            String sourcePattern = parts.get(1);
            String type = parts.get(2);
            if ("s".equals(type)) {
            	intType = String.valueOf(Calendar.SECOND);
            }
            else if ("m".equals(type)) {
            	intType = String.valueOf(Calendar.MINUTE);
            }
            else if ("h".equals(type)) {
            	intType = String.valueOf(Calendar.HOUR_OF_DAY);
            }
            else if ("d".equals(type)) {
            	intType = String.valueOf(Calendar.DAY_OF_MONTH);
            }
            else if ("M".equals(type)) {
            	intType = String.valueOf(Calendar.MONTH);
            }
            else if ("y".equals(type)) {
            	intType = String.valueOf(Calendar.YEAR);
            }
            else {
            	throw new PropertiesHandlerException("Invalid value[" + type + "] for 'type'");
            }
            String value = parts.get(3);
            String paramValue = DateUtils.addTime(date, sourcePattern, intType, value);
            if (paramValue == null) {
                throw new PropertiesHandlerException("Error handling 'dateAdd' metadata '" + str
                        + "'. Invalid format.");
            }
            return paramValue;
        }
        catch (Exception exc) {
            System.out.println("Error handling 'dateAdd' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'dateAdd' metadata '" + str + "'", exc);
            }
            return "dateAdd" + PROP_START + str + PROP_END;
        }
    }

    private String expandDateFormatAdd(String str, Map<String, Object> inProperties, Object object, 
            Object extra) throws PropertiesHandlerException
    {
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            String intType = "";
            List<String> parts = TextUtils.splitByStringSeparator(str, "::");
            String sourceTZone = DateUtils.getDefaultTimeZone().getID();
            String destTZone = sourceTZone;
            String date = parts.get(0);
            String sourcePattern = parts.get(1);
            String destPattern = parts.get(2);
            String type = parts.get(3);
            if ("s".equals(type)) {
            	intType = String.valueOf(Calendar.SECOND);
            }
            else if ("m".equals(type)) {
            	intType = String.valueOf(Calendar.MINUTE);
            }
            else if ("h".equals(type)) {
            	intType = String.valueOf(Calendar.HOUR_OF_DAY);
            }
            else if ("d".equals(type)) {
            	intType = String.valueOf(Calendar.DAY_OF_MONTH);
            }
            else if ("M".equals(type)) {
            	intType = String.valueOf(Calendar.MONTH);
            }
            else if ("y".equals(type)) {
            	intType = String.valueOf(Calendar.YEAR);
            }
            else {
            	throw new PropertiesHandlerException("Invalid value[" + type + "] for 'type'");
            }
            String value = parts.get(4);
            if (parts.size() > 5) {
                sourceTZone = parts.get(5);
                destTZone = parts.get(6);
            }
            String paramValue = DateUtils.convertAddTime(date, sourcePattern, sourceTZone, destPattern, destTZone, intType, value);
            if (paramValue == null) {
                throw new PropertiesHandlerException("Error handling 'dateformatAdd' metadata '" + str
                        + "'. Invalid format.");
            }
            return paramValue;
        }
        catch (Exception exc) {
            System.out.println("Error handling 'dateformatAdd' metadata '" + str + "': " + exc);
            exc.printStackTrace();
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'dateformatAdd' metadata '" + str + "'", exc);
            }
            return "dateformatAdd" + PROP_START + str + PROP_END;
        }
    }

	private String expandDecode(String str, Map<String, Object> inProperties, Object object, Object extra)
			throws PropertiesHandlerException {
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
		} catch (Exception exc) {
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

	private String expandDecodeL(String str, Map<String, Object> inProperties, Object obj, Object extra)
			throws PropertiesHandlerException {
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
		} catch (Exception exc) {
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

	/**
	 * @param str
	 *            the string to valorize
	 * @return the expanded string
	 */
	private String expandEscJS(String str, Map<String, Object> inProperties, Object object, Object extra)
			throws PropertiesHandlerException {
		String string = str;
		if (!PropertiesHandler.isExpanded(string)) {
			string = PropertiesHandler.expand(string, inProperties, object, extra);
		}
		String escaped = TextUtils.replaceJSInvalidChars(string);
		return escaped;
	}

	/**
	 * @param str
	 *            the string to valorize
	 * @return the expanded string
	 */
	private String expandEscSQL(String str, Map<String, Object> inProperties, Object object, Object extra)
			throws PropertiesHandlerException {
		String string = str;
		if (!PropertiesHandler.isExpanded(string)) {
			string = PropertiesHandler.expand(string, inProperties, object, extra);
		}
		String escaped = TextUtils.replaceSQLInvalidChars(string);
		return escaped;
	}

	/**
	 * @param str
	 *            the string to valorize
	 * @return the expanded string
	 */
	private String expandEscXML(String str, Map<String, Object> inProperties, Object object, Object extra)
			throws PropertiesHandlerException {
		String string = str;
		if (!PropertiesHandler.isExpanded(string)) {
			string = PropertiesHandler.expand(string, inProperties, object, extra);
		}
		String escaped = XMLUtils.replaceXMLInvalidChars(string);
		return escaped;
	}

	private String expandReplace(String str, Map<String, Object> inProperties, Object object, Object extra)
			throws PropertiesHandlerException {
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
		} catch (Exception exc) {
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
	 *            the string to valorize
	 * @return the expanded string
	 */
	private  String expandUrlEnc(String str, Map<String, Object> inProperties, Object object, Object extra)
			throws PropertiesHandlerException {
		try {
			String string = str;
			if (!PropertiesHandler.isExpanded(string)) {
				string = PropertiesHandler.expand(string, inProperties, object, extra);
			}
			if (!PropertiesHandler.isExpanded(string)) {
				return "urlEnc" + PROP_START + str + PROP_END;
			}
			return TextUtils.urlEncode(string);
		} catch (Exception exc) {
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
	 *            the string to valorize
	 * @return the expanded string
	 */
	private  String expandUrlDec(String str, Map<String, Object> inProperties, Object object, Object extra)
			throws PropertiesHandlerException {
		try {
			String string = str;
			if (!PropertiesHandler.isExpanded(string)) {
				string = PropertiesHandler.expand(string, inProperties, object, extra);
			}
			if (!PropertiesHandler.isExpanded(string)) {
				return "urlDec" + PROP_START + str + PROP_END;
			}
			return TextUtils.urlDecode(string);
		} catch (Exception exc) {

			if (PropertiesHandler.isExceptionOnErrors()) {
				if (exc instanceof PropertiesHandlerException) {
					throw (PropertiesHandlerException) exc;
				}
				throw new PropertiesHandlerException("Error handling 'urlDec' metadata '" + str + "'", exc);
			}
			return "urlDec" + PROP_START + str + PROP_END;
		}
	}
	

	/**
	 * @param str
	 *            the string to valorize
	 * @return the expanded string
	 */
	private String expandFile(String str, Map<String, Object> inProperties, Object object, Object extra)
			throws PropertiesHandlerException {
		try {
			if (!PropertiesHandler.isExpanded(str)) {
				str = PropertiesHandler.expand(str, inProperties, object, extra);
			}
			List<String> parts = TextUtils.splitByStringSeparator(str, "::");
			String path = parts.get(0);
			String type = "text";
			boolean cached = true;
			if (parts.size() > 1) {
				cached = "Y".equalsIgnoreCase(parts.get(1));
			}
			if (parts.size() > 2) {
				type = parts.get(2);
			}
			String data = (String) FileCache.getContent(path, FileCache.Type.valueOf(type.toUpperCase()), cached);
			return data;
		} catch (Exception exc) {
			System.out.println("Error handling 'file' metadata '" + str + "': " + exc);
			exc.printStackTrace();
			if (PropertiesHandler.isExceptionOnErrors()) {
				if (exc instanceof PropertiesHandlerException) {
					throw (PropertiesHandlerException) exc;
				}
				throw new PropertiesHandlerException("Error handling 'file' metadata '" + str + "'", exc);
			}
			return "file" + PROP_START + str + PROP_END;
		}
	}

}