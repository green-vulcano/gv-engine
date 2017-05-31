/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.util.json;

import it.greenvulcano.util.xml.XMLUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.5.0 29/ago/2014
 * @author GreenVulcano Developer Team
 *
 */
public class JSONUtils
{
    /**
     * Convert the input XML to a JSONObject.
     * JSON does not distinguish between elements and attributes.
     * Sequences of similar elements are represented as JSONArrays.
     * If an element have attributes, content text/cdata may be placed in a "contentText" member.
     * Comments and namespaces are ignored.
     * If the root element is 'DEFAULT_ROOT' then isn't included into JSON output.
     * 
     * @param xml
     *        the document to convert
     * @return
     * @throws JSONUtilsException
     */
    public static JSONObject xmlToJson(Object xml) throws JSONUtilsException {
        return xmlToJson(xml, new HashSet<String>(), new HashSet<String>());
    }

    /**
     * Convert the input XML to a JSONObject.
     * JSON does not distinguish between elements and attributes.
     * Sequences of similar elements (or elements which local-name are in forceElementsArray)
     * are represented as JSONArrays. 
     * If an element have attributes, content text/cdata may be placed in a "contentText" member.
     * Comments and namespaces are ignored.
     * If the root element is 'DEFAULT_ROOT' then isn't included into JSON output.
     *
     * @param xml
     *        the document to convert
     * @param forceElementsArray
     *        a set containing element's local-name to be forced as JSONArray also if in single instance
     * @param forceStringValue
     *        a set containing element's local-name to be forced as String values, ignoring type conversions
     * @return
     * @throws JSONUtilsException
     */
    public static JSONObject xmlToJson(Object xml, Set<String> forceElementsArray, Set<String> forceStringValue) throws JSONUtilsException {
        XMLUtils parser = null;
        JSONObject result = new JSONObject();
        try {
            parser = XMLUtils.getParserInstance();
            Node node = parser.parseObject(xml, false, true, true);
            Element el = null;

            if (node instanceof Document) {
                el = ((Document) node).getDocumentElement();
            }
            else {
                el = (Element) node;
            }

            String name = el.getLocalName();

            if (forceElementsArray.contains(name)) {
                result.append(name, processElement(parser, el, forceElementsArray, forceStringValue));
            }
            else {
                result.put(name, processElement(parser, el, forceElementsArray, forceStringValue));
            }

            if (result.has("DEFAULT_ROOT")) {
                result = result.getJSONObject("DEFAULT_ROOT");
            }
            return result;
        }
        catch (JSONUtilsException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new JSONUtilsException("Error converting XML to JSON", exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    private static Object processElement(XMLUtils parser, Element el, Set<String> forceElementsArray, Set<String> forceStringValue) throws JSONUtilsException {
        try {
        	boolean forceString = forceStringValue.contains("*");
            if (el.hasAttributes() || el.hasChildNodes()) {
                JSONObject current = new JSONObject();
                boolean hasAttributes = el.hasAttributes();
                boolean usableAttribute = false;
                if (hasAttributes) {
                    NamedNodeMap attrs = el.getAttributes();
                    int len = attrs.getLength();
                    for (int i = 0; i < len; i++) {
                        Node att = attrs.item(i);
                        String name = att.getLocalName();
                        if (!name.equals("xmlns") && !"xmlns".equals(att.getPrefix())) {
                            usableAttribute = true;
                            String value = parser.getNodeContent(att);
                            if (forceString || forceStringValue.contains(name)) {
                                current.put(name, value);
                            }
                            else {
                                current.put(name, stringToValue(value));
                            }
                        }
                    }
                }
                hasAttributes = usableAttribute;

                if (el.hasChildNodes()) {
                    NodeList nl = el.getChildNodes();
                    boolean hasElementChild = false;
                    int len = nl.getLength();
                    for (int i = 0; i < len; i++) {
                        Node n = nl.item(i);
                        String name = n.getLocalName();
                        short nodeType = n.getNodeType();
                        switch (nodeType) {
                            case Node.ELEMENT_NODE :
                                if (forceElementsArray.contains(name)) {
                                	if (n.hasChildNodes() || n.hasAttributes()) {
                                		current.append(name, processElement(parser, (Element) n, forceElementsArray, forceStringValue));
                                	}
                                	else {
                                		current.putOnce(name,  new JSONArray());
                                	}
                                }
                                else {
                                    current.accumulate(name, processElement(parser, (Element) n, forceElementsArray, forceStringValue));
                                }
                                hasElementChild = true;
                                break;
                            case Node.CDATA_SECTION_NODE :
                            case Node.TEXT_NODE :
                                if (hasElementChild) {
                                    break;
                                }
                                if (!"".equals(n.getTextContent().trim())) {
                                	String valStr = parser.getNodeContent(el);
                                    Object value = valStr; 
                                    if (!(forceString || forceStringValue.contains(el.getLocalName()))) {
                                    	value = stringToValue(valStr);
                                    }
                                    if (hasAttributes) {
                                        current.put("contentText", value);
                                        return current;
                                    }

                                    return value;
                                }
                            default :
                        }
                    }
                }
                return (current.length() > 0) ? current : "";
            }
            else {
                return "";
            }
        }
        catch (JSONUtilsException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new JSONUtilsException("Error converting Element[" + el.getTagName() + "] to JSON", exc);
        }
    }
    
    /**
     * Convert a JSONObject into an XML structure.
     * If the JSON to be converted doesn't have a single root element 
     * then is automatically created a 'DEFAULT_ROOT' root element.
     * 
     * @param json 
     *        a JSONObject
     * @return 
     * @throws  JSONException
     */
    public static Node jsonToXml(Object json) throws JSONUtilsException {
        return jsonToXml(json, null, new HashSet<String>()); 
    }

    /**
     * Convert a JSONObject into an XML structure.
     * If the JSON to be converted doesn't have a single root element 
     * then is automatically created a 'DEFAULT_ROOT' root element.
     * 
     * @param json 
     *        a JSONObject
     * @param forceAttribute
     *        a set containing keys name to be set as XML attributes
     * @return 
     * @throws  JSONException
     */
    public static Node jsonToXml(Object json, Set<String> forceAttributes) throws JSONUtilsException {
        return jsonToXml(json, null, forceAttributes); 
    }

    

    /**
     * Convert a JSONObject into Node structure.
     * If not specified a rootName and the JSON to be converted doesn't have
     * a single root element then is automatically created a 'DEFAULT_ROOT' root element.
     * 
     * @param json
     *        a JSONObject
     * @param rootName
     *        the optional name of the root element
     * @return
     * @throws JSONUtilsException
     */
    public static Node jsonToXml(Object json, String rootName) throws JSONUtilsException {
        return jsonToXml(json, rootName, new HashSet<String>());
    }

    /**
     * Convert a JSONObject into Node structure.
     * If not specified a rootName and the JSON to be converted doesn't have
     * a single root element then is automatically created a 'DEFAULT_ROOT' root element.
     * 
     * @param json
     *        a JSONObject
     * @param rootName
     *        the optional name of the root element
     * @param forceAttributes
     *        a set containing keys name to be set as XML attributes
     * @return
     * @throws JSONUtilsException
     */
    public static Node jsonToXml(Object json, String rootName, Set<String> forceAttributes) throws JSONUtilsException {
            XMLUtils parser = null;
        Document doc = null;
        try {
            parser = XMLUtils.getParserInstance();

            if (json instanceof String) {
                json = new JSONObject((String) json);
            }
            else if (json instanceof byte[]) {
                json = new JSONObject(new String((byte[]) json));
            }
            if ((rootName == null) && (json.getClass().isArray() || ((json instanceof JSONObject) && ((JSONObject) json).length() != 1))) {
                rootName = "DEFAULT_ROOT";
            }

            if (rootName != null) {
                doc = parser.newDocument(rootName);
                jsonToXml(parser, doc, json, null, doc.getDocumentElement(), forceAttributes);
            }
            else {
                doc = parser.newDocument();
                jsonToXml(parser, doc, json, null, null, forceAttributes);
            }

            return doc;
        }
        catch (JSONUtilsException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new JSONUtilsException("Error converting JSON to XML", exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }    
        
    private static Node jsonToXml(XMLUtils parser, Document doc, Object json, String tagName,
            Element context, Set<String> forceAttributes) throws JSONUtilsException {
        Element el = null;

        try {
            // create tagName element, if needed
            if (tagName != null) {
                el = parser.insertElement(context, tagName);
            }
            else {
                el = context;
            }

            if (json instanceof JSONObject) {
                // Loop thru the keys.
                JSONObject jo = (JSONObject) json;
                Iterator<String> keys = jo.keys();
                while (keys.hasNext()) {
                    String key = keys.next().toString();
                    Object value = jo.opt(key);
                    if (value == null) {
                        value = "";
                    }

                    // Emit contentText in body
                    if ("contentText".equals(key)) {
                        if (value instanceof JSONArray) {
                            StringBuffer sb = new StringBuffer();
                            JSONArray ja = (JSONArray) value;
                            int length = ja.length();
                            for (int i = 0; i < length; i += 1) {
                                if (i > 0) {
                                    sb.append('\n');
                                }
                                sb.append(ja.get(i).toString());
                            }
                            parser.insertText(el, sb.toString());
                        } else {
                            parser.insertText(el, value.toString());
                        }
    
                    // Emit an array of similar keys
                    } else if (value instanceof JSONArray) {
                        JSONArray ja = (JSONArray) value;
                        int length = ja.length();
                        if (el == null) {
                        	el = parser.createElement(doc, "DEFAULT_ROOT");
                        	doc.appendChild(el);
                        }
                        for (int i = 0; i < length; i += 1) {
                            value = ja.get(i);
                            jsonToXml(parser, doc, value, key, el, forceAttributes);
                        }
                    } else if ("".equals(value)) {
                        if (forceAttributes.contains(key)) {
                            parser.setAttribute(el, key, "");
                        }
                        else {
                            parser.insertElement(el, key);
                        }
    
                    // Emit a new tag <k>
                    } else {
                        if (doc.getDocumentElement() == null) {
                            jsonToXml(parser, doc, value, null, 
                                    (Element) doc.appendChild(parser.createElement(doc, key)), forceAttributes);
                        }
                        else {
                            if (forceAttributes.contains(key)) {
                                parser.setAttribute(el, key, value.toString());
                            }
                            else {
                                jsonToXml(parser, doc, value, key, el, forceAttributes);
                            }
                        }
                    }
                }
    
            // XML does not have good support for arrays. If an array appears in a place
            // where XML is lacking, synthesize an <array> element.
            } else {
                if (json.getClass().isArray()) {
                    json = new JSONArray(json);
                }
                if (json instanceof JSONArray) {
                    JSONArray ja = (JSONArray) json;
                    int length = ja.length();
                    for (int i = 0; i < length; i += 1) {
                        jsonToXml(parser, doc, ja.opt(i), tagName == null ? "array" : tagName, el, forceAttributes);
                    }
                } else {
                    String string = (json == null) ? "null" : json.toString();
                    //el = parser.insertElement(context, tagName);
                    parser.insertText(el, string);
                }
            }
            return el;
        }
        catch (Exception exc) {
            throw new JSONUtilsException("Error converting JSON[" + tagName + "] to XML[" + 
                                         (el != null ? el.getLocalName() : "null") + "]", exc);
        }
    }
    
    /**
     * Try to convert a string into a number, boolean, or null. If the string
     * can't be converted, return the string. This is much less ambitious than
     * JSONObject.stringToValue, especially because it does not attempt to
     * convert plus forms, octal forms, hex forms, or E forms lacking decimal
     * points.
     * @param string A String.
     * @return A simple JSON value.
     */
    private static Object stringToValue(String string) {
        if ("true".equalsIgnoreCase(string)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(string)) {
            return Boolean.FALSE;
        }
        if ("null".equalsIgnoreCase(string)) {
            return JSONObject.NULL;
        }

// If it might be a number, try converting it, as a Double or as a Long.
// If that doesn't work, return the string.

        char b = string.charAt(0);
        if ((b >= '0' && b <= '9') || b == '-') {
            try {
                if (string.indexOf('.') > -1 || string.indexOf('e') > -1
                        || string.indexOf('E') > -1) {
                    Double d = Double.valueOf(string);
                    if (!d.isInfinite() && !d.isNaN()) {
                        return d;
                    }
                } else {
                    Long myLong = new Long(string);
                    if (string.equals(myLong.toString())) {
                        if (myLong == myLong.intValue()) {
                            return myLong.intValue();
                        } else {
                            return myLong;
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return string;
    }
    
    /**
     * Convert the input XML to a JSONObject using BadgerFish convention.
     * See <a href="http://badgerfish.ning.com">http://badgerfish.ning.com</a>
     * 
     * @param xml
     *        the document to convert
     * @return
     * @throws JSONUtilsException
     */
    public static JSONObject xmlToJson_BadgerFish(Object xml) throws JSONUtilsException {
        XMLUtils parser = null;
        JSONObject result = new JSONObject();
        try {
            parser = XMLUtils.getParserInstance();
            Node node = parser.parseObject(xml, false, true, true);
            Element el = null;

            if (node instanceof Document) {
                el = ((Document) node).getDocumentElement();
            }
            else {
                el = (Element) node;
            }

            String name = el.getNodeName();

            result.accumulate(name, processElement_BadgerFish(parser, el));

            return result;
        }
        catch (JSONUtilsException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new JSONUtilsException("Error converting XML to JSON BadgerFish", exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    private static Object processElement_BadgerFish(XMLUtils parser, Element el) throws JSONUtilsException {
        try {
            JSONObject current = new JSONObject();
            JSONObject xmlns = new JSONObject();

            NamedNodeMap attrs = el.getAttributes();
            int len = attrs.getLength();
            for (int i = 0; i < len; i++) {
                Node att = attrs.item(i);
                String name = att.getLocalName();
                String value = parser.getNodeContent(att);

                /*if (name.equals("xmlns")) {
                    xmlns.putOnce("$", value);
                }
                else if ("xmlns".equals(att.getPrefix())) {
                    xmlns.putOnce(name, value);
                }
                else {
                    current.put("@" + name, value);
                }*/
                if (!name.equals("xmlns") && !"xmlns".equals(att.getPrefix())) {
                    current.putOnce("@" + name, value);
                }
            }
            
            String ns = el.getNamespaceURI();
            if (ns != null) {
                if (el.isDefaultNamespace(ns)) {
                    xmlns.putOnce("$", ns);
                }
                else {
                    xmlns.putOnce(el.lookupPrefix(ns), ns);
                }
            }
            if (xmlns.keys().hasNext()) {
                current.putOnce("@xmlns", xmlns);
            }

            if (el.hasChildNodes()) {
                NodeList nl = el.getChildNodes();
                boolean hasElementChild = false;
                len = nl.getLength();
                for (int i = 0; i < len; i++) {
                    Node n = nl.item(i);
                    String name = n.getNodeName();
                    short nodeType = n.getNodeType();
                    switch (nodeType) {
                        case Node.ELEMENT_NODE :
                            hasElementChild = true;
                            current.accumulate(name, processElement_BadgerFish(parser, (Element) n));
                            break;
                        case Node.CDATA_SECTION_NODE :
                        case Node.TEXT_NODE :
                            if (hasElementChild) {
                                break;
                            }
                            if (!"".equals(n.getTextContent().trim())) {
                                String value = parser.getNodeContent(el);
                                current.put("$", value);
                                return current;
                            }
                        default :
                    }
                }
            }
            return current;
        }
        catch (JSONUtilsException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new JSONUtilsException("Error converting Element[" + el.getTagName() + "] to JSON BadgerFish", exc);
        }
    }
    
    
    /**
     * Convert a JSONObject in BadgerFish notation into Node structure.
     * See <a href="http://badgerfish.ning.com">http://badgerfish.ning.com</a>
     * 
     * @param json
     *        a JSONObject
     * @return
     * @throws JSONUtilsException
     */
    public static Node jsonToXml_BadgerFish(Object json) throws JSONUtilsException {
            XMLUtils parser = null;
        Document doc = null;
        try {
            parser = XMLUtils.getParserInstance();

            if (json instanceof String) {
                json = new JSONObject((String) json);
            }
            else if (json instanceof byte[]) {
                json = new JSONObject(new String((byte[]) json));
            }
            
            doc = parser.newDocument();
            jsonToXml_BadgerFish(parser, doc, json, null, null);

            return doc;
        }
        catch (JSONUtilsException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new JSONUtilsException("Error converting JSON BadgerFish to XML", exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }    
        
    private static Node jsonToXml_BadgerFish(XMLUtils parser, Document doc, Object json, String tagName,
            Element context) throws JSONUtilsException {
        Element el = null;

        try {
            if (json instanceof JSONObject) {
                JSONObject jo = (JSONObject) json;
                
                // create tagName element, if needed
                if (tagName != null) {
                    el = createElementNS(parser, doc, jo, tagName);
                    context.appendChild(el);
                }
                else {
                    el = context;
                }

                // Loop thru the keys.
                Iterator<String> keys = jo.keys();
                while (keys.hasNext()) {
                    String key = keys.next().toString();
                    Object value = jo.opt(key);
                    if (value == null) {
                        value = "";
                    }

                    // Skip element's namespace
                    if ("@xmlns".equals(key)) {
                       // do nothing 
                    }
                    // Emit element's attributes
                    else if (key.startsWith("@")) {
                       String an = key.substring(1);
                       parser.setAttribute(el, an, value.toString());
                    }
                    // Emit content Text in body
                    else if ("$".equals(key)) {
                       parser.insertText(el, value.toString());
                    }
                    // Emit an array of similar keys
                    else if (value instanceof JSONArray) {
                        JSONArray ja = (JSONArray) value;
                        int length = ja.length();
                        for (int i = 0; i < length; i += 1) {
                            value = ja.get(i);
                            jsonToXml_BadgerFish(parser, doc, value, key, el);
                        }
                    // Emit a new tag <k>
                    } else {
                        if (doc.getDocumentElement() == null) {
                            jsonToXml_BadgerFish(parser, doc, value, null, 
                                    (Element) doc.appendChild(createElementNS(parser, doc, (JSONObject) value, key)));
                        }
                        else {
                            jsonToXml_BadgerFish(parser, doc, value, key, el);
                        }
                    }
                }
            }
            return el;
        }
        catch (Exception exc) {
            throw new JSONUtilsException("Error converting JSON[" + tagName + "] BadgerFish to XML[" + 
                                         (el != null ? el.getLocalName() : "null") + "]", exc);
        }
    }
    
    private static Element createElementNS(XMLUtils parser, Document doc, JSONObject json, String tagName) throws Exception {
        Element el = null;
        if (json.has("@xmlns")) {
            JSONObject xmlns = json.getJSONObject("@xmlns");
            if (xmlns.has("$")) {
                el = parser.createElementNS(doc, tagName, xmlns.get("$").toString());
            }
            else if (tagName.contains(":")) {
                String prefix = tagName.split(":")[0];
                tagName = tagName.split(":")[1];
                
                el = parser.createElementNS(doc, tagName, xmlns.get(prefix).toString());
                el.setPrefix(prefix);
            }
        }
        else {
            el = parser.createElement(doc, tagName);
        }

        return el;
    }

}
