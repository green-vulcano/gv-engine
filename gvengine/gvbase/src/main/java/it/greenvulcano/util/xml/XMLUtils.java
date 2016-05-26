/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.util.xml;

import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xpath.search.XPath;
import it.greenvulcano.util.xpath.search.XPathAPI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

/**
 * XMLUtils: utility class that contains
 * <p>
 * - methods used to parse DOMs (validating or not) from Strings/Streams.<br/>
 * - serialization of DOMs.<br/>
 * - replacement/restore of XML entities.<br/>
 * - accessor methods to elements.<br/>
 * </p>
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class XMLUtils
{
    /**
     * XMLSchema validation feature name.
     */
    private static final String              JAXP_SCHEMA_LANGUAGE     = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /**
     * XMLSchema validation feature value.
     */
    public static final String              W3C_XML_SCHEMA           = "http://www.w3.org/2001/XMLSchema";
    /**
     * A string containing (some) invalid XML chars.
     */
    private static String                    invalidXMLChars          = "&<>\"'";
    /**
     * The XPath api implementation.
     */
    private XPathAPI                         xpathAPI                 = new XPathAPI();

    /**
     * A string array containing the corresponding XML entities.
     */
    private static String[]                  correspondingXMLEntities = {"&amp;", "&lt;", "&gt;", "&quot;", "&apos;"};

    /**
     * A <tt>Stack</tt> containing unused <tt>XMLUtils</tt> instances.
     */
    private static Stack<XMLUtils>           freeInstances            = null;

    private static EntityResolver            defaultEntityResolver    = new it.greenvulcano.catalog.GVCatalogResolver();

    /**
     * Used to parse a DOM from a given XML string.
     */
    private DocumentBuilder                  docBuilder;
    private DocumentBuilder                  docBuilderComments;
    /**
     * Used to parse a DOM (with namespaceAware) from a given XML string.
     */
    private DocumentBuilder                  docBuilderNamespace;
    private DocumentBuilder                  docBuilderNamespaceComments;
    /**
     * Used to parse a DOM (with namespaceAware and standard or non-standard
     * validation) from a given XML string.
     */
    private DocumentBuilder                  docBuilderValidatingNamespace;
    private DocumentBuilder                  docBuilderValidatingNamespaceComments;

    /**
     * Used to parse a DOM (with standard or non-standard validation) from a
     * given XML string.
     */
    private DocumentBuilder                  docBuilderValidating;
    private DocumentBuilder                  docBuilderValidatingComments;

    private HashMap<String, DocumentBuilder> docBuilderMap            = null;

    static {
        freeInstances = new Stack<XMLUtils>();
        // defaultEntityResolver = new
        // it.greenvulcano.catalog.GVCatalogResolver();
    }

    /**
     * Entity resolver usable by XMLUtils. This entity resolver does not resolve
     * any entity. To use an actual entity resolver use the
     * <code>setEntityResolver()</code> method of <code>XMLUtils</code>.
     * 
     * @see #setDefaultEntityResolver()
     * @see #setEntityResolver(org.xml.sax.EntityResolver)
     */
    static class NoEntityResolver implements EntityResolver
    {
        /**
         * @param publicId
         * @param systemId
         * @return an InputSource for an empty string.
         */
        @Override
        public InputSource resolveEntity(String publicId, String systemId)
        {
            return new InputSource(new StringReader(""));
        }
    }

    /**
     * private constructor.
     */
    private XMLUtils() throws XMLUtilsException
    {
        try {
            setupDOMParser();
        }
        catch (Throwable exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Gets an instance of <tt>XMLUtils</tt> from the private stack. If an
     * instance is not available, a new instance will be created.
     * 
     * @return the XMLUtils parser instance
     * @throws XMLUtilsException
     */
    public static synchronized XMLUtils getParserInstance() throws XMLUtilsException
    {
        if (freeInstances == null) {
            freeInstances = new Stack<XMLUtils>();
        }

        XMLUtils theInstance = null;
        if (!freeInstances.empty()) {
            theInstance = freeInstances.pop();
        }
        else {
            theInstance = new XMLUtils();
        }
        return theInstance;
    }

    /**
     * Release an instance of <tt>XMLUtils</tt>.
     * 
     * @param theInstance
     */
    public static synchronized void releaseParserInstance(XMLUtils theInstance)
    {
        if (theInstance != null) {
            freeInstances.push(theInstance);
        }
    }

    /**
     * Replaces XML invalid chars within <tt>input</tt> String with the
     * corresponding entities.
     * 
     * @param input
     *        the input <tt>String</tt>.
     * @return the input string, with XML invalid chars replaced by the
     *         corresponding entities.
     */
    public static String replaceXMLInvalidChars(String input)
    {
        if (input == null) {
            return null;
        }
        if (input.equals("")) {
            return "";
        }

        boolean foundInvalidChar = false;
        for (int i = 0; i < input.length(); i++) {
            if (invalidXMLChars.indexOf(input.charAt(i)) != -1) {
                foundInvalidChar = true;
                break;
            }
        }

        if (!foundInvalidChar) {
            return input;
        }

        StringBuilder buf = new StringBuilder("");
        for (int i = 0; i < input.length(); i++) {
            int idx = invalidXMLChars.indexOf(input.charAt(i));
            if (idx != -1) {
                buf.append(correspondingXMLEntities[idx]);
            }
            else {
                buf.append(input.charAt(i));
            }
        }
        return buf.toString();
    }

    /**
     * Replaces XML entities within <tt>input</tt> String with the corresponding
     * invalid characters.
     * 
     * @param input
     *        the input <tt>String</tt>.
     * @return the input string, with XML entities replaced by the corresponding
     *         invalid chars.
     */
    public static String replaceXMLEntities(String input)
    {
        if (input == null) {
            return null;
        }
        if (input.equals("")) {
            return "";
        }

        boolean foundEntity = false;

        for (String correspondingXMLEntitie : correspondingXMLEntities) {
            if (input.indexOf(correspondingXMLEntitie) != -1) {
                foundEntity = true;
                break;
            }
        }
        if (!foundEntity) {
            return input;
        }

        for (int i = 0; i < correspondingXMLEntities.length; i++) {
            input = TextUtils.replaceSubstring(input, correspondingXMLEntities[i], "" + invalidXMLChars.charAt(i));
        }
        return input;
    }

    /**
     * Check for XML invalid chars within <tt>input</tt> String.
     *
     * @param input
     *        the input <tt>String</tt>.
     * @return true if the input string contains XML invalid chars.
     */
    public static boolean checkXMLInvalidChars(String input)
    {
        if (input == null) {
            return false;
        }
        if (input.equals("")) {
            return false;
        }

        boolean foundInvalidChar = false;
        for (int i = 0; i < input.length(); i++) {
            if (invalidXMLChars.indexOf(input.charAt(i)) != -1) {
                foundInvalidChar = true;
                break;
            }
        }

        return foundInvalidChar;
    }

    /**
     * Returns a DOM parsed from the given XML string.
     * 
     * @param xmlString
     *        A String containing an XML document
     * @return The corresponding DOM
     * @throws XMLUtilsException
     *         when errors occur
     */
    public Document parseDOM(String xmlString) throws XMLUtilsException
    {
        try {
            xmlString = removeDocType(xmlString);
            InputSource xmlSource = new InputSource(new StringReader(xmlString));
            return docBuilderNamespace.parse(xmlSource);
        }
        catch (Throwable exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    public Document parseDOM(byte[] xml) throws XMLUtilsException
    {
        try {
            InputSource xmlSource = new InputSource(new ByteArrayInputStream(xml));
            return docBuilderNamespace.parse(xmlSource);
        }
        catch (Throwable exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Returns a DOM parsed from the given XML input stream.
     * 
     * @param xmlStream
     *        An InputStream containing an XML document
     * @return The corresponding DOM
     * @throws XMLUtilsException
     */
    public Document parseDOM(InputStream xmlStream) throws XMLUtilsException
    {
        try {
            InputSource xmlSource = new InputSource(xmlStream);
            return docBuilderNamespace.parse(xmlSource);
        }
        catch (Throwable exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Returns a DOM parsed from the given XML string.
     * 
     * @param xmlString
     *        A String containing an XML document
     * @param isValidating
     * @param isNamespaceAware
     * @return The corresponding DOM
     * @throws XMLUtilsException
     *         when errors occur
     */
    public Document parseDOM(String xmlString, boolean isValidating, boolean isNamespaceAware) throws XMLUtilsException
    {
    	return parseDOM(xmlString, isValidating, isNamespaceAware, true);
    }

	/**
     * Returns a DOM parsed from the given XML string.
     * 
     * @param xmlString
     *        A String containing an XML document
     * @param isValidating
     * @param isNamespaceAware
     * @param isIgnoringComments
     * @return The corresponding DOM
     * @throws XMLUtilsException
     *         when errors occur
     */
    public Document parseDOM(String xmlString, boolean isValidating, boolean isNamespaceAware, boolean isIgnoringComments) throws XMLUtilsException
    {
        try {
            xmlString = removeDocType(xmlString);
            InputSource xmlSource = new InputSource(new StringReader(xmlString));
            DocumentBuilder docBuilderX = docBuilderMap.get(getKey(isValidating, isNamespaceAware, isIgnoringComments));
            return docBuilderX.parse(xmlSource);
        }
        catch (Throwable exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Returns a DOM parsed from the given XML string.
     * 
     * @param xml
     *        A byte array containing an XML document
     * @param isValidating
     * @param isNamespaceAware
     * @return The corresponding DOM
     * @throws XMLUtilsException
     *         when errors occur
     */
    public Document parseDOM(byte[] xml, boolean isValidating, boolean isNamespaceAware) throws XMLUtilsException
    {
    	return parseDOM(xml, isValidating, isNamespaceAware, true);
    }
    
    /**
     * Returns a DOM parsed from the given XML string.
     * 
     * @param xml
     *        A byte array containing an XML document
     * @param isValidating
     * @param isNamespaceAware
     * @param isIgnoringComments
     * @return The corresponding DOM
     * @throws XMLUtilsException
     *         when errors occur
     */
    public Document parseDOM(byte[] xml, boolean isValidating, boolean isNamespaceAware, boolean isIgnoringComments) throws XMLUtilsException
    {
        try {
            InputSource xmlSource = new InputSource(new ByteArrayInputStream(xml));
            DocumentBuilder docBuilderX = docBuilderMap.get(getKey(isValidating, isNamespaceAware, isIgnoringComments));
            return docBuilderX.parse(xmlSource);
        }
        catch (Throwable exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Returns a DOM parsed from the given XML input stream.
     * 
     * @param xmlStream
     *        input document
     * @param isValidating
     *        if true try to validating the XML
     * @param isNamespaceAware
     *        if true ability to resolver namespace
     * @return a DOM parsed from the given XML input stream.
     * @throws XMLUtilsException
     */
    public Document parseDOM(InputStream xmlStream, boolean isValidating, boolean isNamespaceAware) throws XMLUtilsException
    {
    	return parseDOM(xmlStream, isValidating, isNamespaceAware, true);
    }

    /**
     * Returns a DOM parsed from the given XML input stream.
     * 
     * @param xmlStream
     *        input document
     * @param isValidating
     *        if true try to validating the XML
     * @param isNamespaceAware
     *        if true ability to resolver namespace
     * @param isIgnoringComments
     * 		  if true ignore XML comments in input
     * @return a DOM parsed from the given XML input stream.
     * @throws XMLUtilsException
     */
    public Document parseDOM(InputStream xmlStream, boolean isValidating, boolean isNamespaceAware, boolean isIgnoringComments) throws XMLUtilsException
    {
    	try {
            InputSource xmlSource = new InputSource(xmlStream);
            DocumentBuilder docBuilderX = docBuilderMap.get(getKey(isValidating, isNamespaceAware, isIgnoringComments));
            return docBuilderX.parse(xmlSource);
        }
        catch (Throwable exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Returns a DOM/Node parsed from the given input object.
     * 
     * @param object
     *        input object
     * @param isValidating
     *        if true try to validating the XML
     * @param isNamespaceAware
     *        if true ability to resolver namespace
     * @return a DOM/Node parsed from the given input object.
     * @throws XMLUtilsException
     */
    public Node parseObject(Object input, boolean isValidating, boolean isNamespaceAware) throws XMLUtilsException
    {
    	return parseObject(input, isValidating, isNamespaceAware, true);
    }
    
    /**
     * Returns a DOM/Node parsed from the given input object.
     * 
     * @param object
     *        input object
     * @param isValidating
     *        if true try to validating the XML
     * @param isNamespaceAware
     *        if true ability to resolver namespace
     * @param isIgnoringComments
     * 		  if true ignore XML comments in input
     * @return a DOM/Node parsed from the given input object.
     * @throws XMLUtilsException
     */
    public Node parseObject(Object input, boolean isValidating, boolean isNamespaceAware, boolean isIgnoringComments) throws XMLUtilsException
    {
    	try {
            InputSource xmlSource = null;
            if (input instanceof Document) {
                return (Document) input;
            }
            else if (input instanceof Node) {
                return (Node) input;
            }
            else if (input instanceof String) {
                xmlSource = new InputSource(new StringReader((String) input));
            }
            else if (input instanceof byte[]) {
                xmlSource = new InputSource(new ByteArrayInputStream((byte[]) input));
            }
            else if (input instanceof InputStream) {
                xmlSource = new InputSource((InputStream) input);
            }
            else {
                throw new XMLUtilsException("Invalid input type: " + input.getClass());
            }
            DocumentBuilder docBuilderX = docBuilderMap.get(getKey(isValidating, isNamespaceAware, isIgnoringComments));
            return docBuilderX.parse(xmlSource);
        }
        catch (XMLUtilsException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Returns a DOM parsed from the given XML string.
     * 
     * @param xmlString
     *        A String containing an XML document
     * @param isValidating
     * @param isNamespaceAware
     * @return The corresponding DOM
     * @throws XMLUtilsException
     *         when errors occur
     */
    public static Document parseDOM_S(String xmlString) throws XMLUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.parseDOM(xmlString);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Returns a DOM parsed from the given XML string.
     * 
     * @param xmlString
     *        A String containing an XML document
     * @param isValidating
     * @param isNamespaceAware
     * @return The corresponding DOM
     * @throws XMLUtilsException
     *         when errors occur
     */
    public static Document parseDOM_S(String xmlString, boolean isValidating, boolean isNamespaceAware)
            throws XMLUtilsException
    {
    	return parseDOM_S(xmlString, isValidating, isNamespaceAware, true);
    }
    
    /**
     * Returns a DOM parsed from the given XML string.
     * 
     * @param xmlString
     *        A String containing an XML document
     * @param isValidating
     * @param isNamespaceAware
     * @param isIgnoringComments
     * 		  if true ignore XML comments in input
     * @return The corresponding DOM
     * @throws XMLUtilsException
     *         when errors occur
     */
    public static Document parseDOM_S(String xmlString, boolean isValidating, boolean isNamespaceAware, boolean isIgnoringComments)
            throws XMLUtilsException
    {
    	XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.parseDOM(xmlString, isValidating, isNamespaceAware, isIgnoringComments);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Returns a DOM parsed from the given XML input stream.
     * 
     * @param xmlStream
     *        input document
     * @param isValidating
     *        if true try to validating the XML
     * @param isNamespaceAware
     *        if true ability to resolver namespace
     * @return a DOM parsed from the given XML input stream.
     * @throws XMLUtilsException
     */
    public static Document parseDOM_S(InputStream xmlStream, boolean isValidating, boolean isNamespaceAware)
            throws XMLUtilsException
    {
    	return parseDOM_S(xmlStream, isValidating, isNamespaceAware, true);
    }
    
    /**
     * Returns a DOM parsed from the given XML input stream.
     * 
     * @param xmlStream
     *        input document
     * @param isValidating
     *        if true try to validating the XML
     * @param isNamespaceAware
     *        if true ability to resolver namespace
     * @param isIgnoringComments
     * 		  if true ignore XML comments in input
     * @return a DOM parsed from the given XML input stream.
     * @throws XMLUtilsException
     */
    public static Document parseDOM_S(InputStream xmlStream, boolean isValidating, boolean isNamespaceAware, boolean isIgnoringComments)
            throws XMLUtilsException
    {
    	XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.parseDOM(xmlStream, isValidating, isNamespaceAware, isIgnoringComments);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Returns a DOM parsed from the given byte array.
     * 
     * @param xml
     *        A byte array containing an XML document
     * @param isValidating
     * @param isNamespaceAware
     * @return The corresponding DOM
     * @throws XMLUtilsException
     *         when errors occur
     */
    public static Document parseDOM_S(byte[] xml, boolean isValidating, boolean isNamespaceAware)
            throws XMLUtilsException
    {
    	return parseDOM_S(xml, isValidating, isNamespaceAware, true);
    }

    /**
     * Returns a DOM parsed from the given byte array.
     * 
     * @param xml
     *        A byte array containing an XML document
     * @param isValidating
     * @param isNamespaceAware
     * @param isIgnoringComments
     * 		  if true ignore XML comments in input
     * @return The corresponding DOM
     * @throws XMLUtilsException
     *         when errors occur
     */
    public static Document parseDOM_S(byte[] xml, boolean isValidating, boolean isNamespaceAware, boolean isIgnoringComments)
            throws XMLUtilsException
    {
    	XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.parseDOM(new ByteArrayInputStream(xml), isValidating, isNamespaceAware, isIgnoringComments);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Returns a DOM/Node parsed from the given input object.
     * 
     * @param object
     *        input object
     * @param isValidating
     *        if true try to validating the XML
     * @param isNamespaceAware
     *        if true ability to resolver namespace
     * @return a DOM/Node parsed from the given input object.
     * @throws XMLUtilsException
     */
    public static Node parseObject_S(Object input, boolean isValidating, boolean isNamespaceAware)
            throws XMLUtilsException
    {
    	return parseObject_S(input, isValidating, isNamespaceAware, true);
    }
    
    /**
     * Returns a DOM/Node parsed from the given input object.
     * 
     * @param object
     *        input object
     * @param isValidating
     *        if true try to validating the XML
     * @param isNamespaceAware
     *        if true ability to resolver namespace
     * @param isIgnoringComments
     * 		  if true ignore XML comments in input
     * @return a DOM/Node parsed from the given input object.
     * @throws XMLUtilsException
     */
    public static Node parseObject_S(Object input, boolean isValidating, boolean isNamespaceAware, boolean isIgnoringComments)
            throws XMLUtilsException
    {
    	XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.parseObject(input, isValidating, isNamespaceAware, isIgnoringComments);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Returns a DOM parsed from the given XML string. Note: This method
     * validates the document by using as DTD the document path specified within
     * the SYSTEM directive of the XML document.
     * 
     * @param xmlString
     *        A String containing an XML document
     * @return The corresponding DOM
     * @throws XMLUtilsException
     *         when errors occur
     */
    public Document parseDOMValidating(String xmlString) throws XMLUtilsException
    {
        try {
            InputSource xmlSource = new InputSource(new StringReader(xmlString));
            return docBuilderValidatingNamespace.parse(xmlSource);
        }
        catch (Throwable exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Returns a DOM parsed from the given XML input stream.
     * 
     * @param xmlStream
     *        An InputStream containing an XML document
     * @return The corresponding DOM
     * @throws XMLUtilsException
     */
    public Document parseDOMValidating(InputStream xmlStream) throws XMLUtilsException
    {
        try {
            InputSource xmlSource = new InputSource(xmlStream);
            return docBuilderValidatingNamespace.parse(xmlSource);
        }
        catch (Throwable exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Parses the DOM document and validates it.
     * 
     * @param type
     * @param docStream
     *        the XML document do parse
     * @param gvEntityResolver
     * @param gvErrorHandler
     * @return the document parsed
     * 
     * @throws XMLUtilsException
     */
    public static Document parseDOMValidating(String type, InputStream docStream, EntityResolver gvEntityResolver,
            ErrorHandler gvErrorHandler) throws XMLUtilsException
    {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            if (type.equals("xsd")) {
                factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            }
            DocumentBuilder parser = factory.newDocumentBuilder();
            parser.setEntityResolver(gvEntityResolver);

            parser.setErrorHandler(gvErrorHandler);
            return parser.parse(docStream);
        }
        catch (Throwable exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Aggregate documents or elements.
     * 
     * @param aggrRootElement
     *        aggregate document root element name
     * @param aggrNamespace
     *        namespace URI of the aggregate document root, can be null
     * @param docs
     *        an array of Document, Node, String, byte[] to be aggregated
     * @return an aggregate Document
     * @throws XMLUtilsException
     */
    public Document aggregateXML(String aggrRootElement, String aggrNamespace, Object[] docs) throws XMLUtilsException
    {
        Document docOut = null;
        if ((aggrNamespace != null) && !"".equals(aggrNamespace)) {
            docOut = newDocument(aggrRootElement, aggrNamespace);
        }
        else {
            docOut = newDocument(aggrRootElement);
        }
        Element root = docOut.getDocumentElement();

        for (int i = 0; i < docs.length; i++) {
            Object obj = docs[i];
            Node part = parseObject(obj, false, true, false);
            if (part instanceof Document) {
                root.appendChild(docOut.importNode(((Document) part).getDocumentElement(), true));
            }
            else {
                root.appendChild(docOut.importNode(part, true));
            }
        }

        return docOut;
    }

    /**
     * Aggregate an array of documents or elements.
     * 
     * @param aggrRootElement
     *        aggregate document root element name
     * @param aggrNamespace
     *        namespace URI of the aggregate document root, can be null
     * @param docs
     *        an array of Document, Node, String, byte[] to be aggregated
     * @return an aggregate Document
     * @throws XMLUtilsException
     */
    public static Document aggregateXML_S(String aggrRootElement, String aggrNamespace, Object[] docs)
            throws XMLUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.aggregateXML(aggrRootElement, aggrNamespace, docs);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Facility to aggregate two documents or elements.
     * 
     * @param aggrRootElement
     *        aggregate document root element name
     * @param aggrNamespace
     *        namespace URI of the aggregate document root, can be null
     * @param docA
     *        a Document, Node, String, byte[] to be aggregated
     * @param docB
     *        a Document, Node, String, byte[] to be aggregated
     * @return an aggregate Document
     * @throws XMLUtilsException
     */
    public static Document aggregateXML_S(String aggrRootElement, String aggrNamespace, Object docA, Object docB)
            throws XMLUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.aggregateXML(aggrRootElement, aggrNamespace, new Object[]{docA, docB});
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }
    
    /**
     * Merge documents or elements.
     * 
     * @param docs
     *        an array of Document, Node, String, byte[] to be merged,
     *        the first document is the destination of the merging process
     * @param xpaths
     *        an array of source##destination xpath: for the nth document 'source' is the search xpath for node to copy 
     *        and 'destination' is the xpath of the node of the first document on which add the selected nodes,
     *        the first entry of the array is ignored
     * @return a merged Document
     * @throws XMLUtilsException
     */
    public Document mergeXML(Object[] docs, Object[] xpaths) throws XMLUtilsException
    {
        Document docOut = null;
        if ((docs == null) || (docs.length < 1)) {
        	throw new XMLUtilsException("Invalid xpaths array");
        }
        if (xpaths == null) {
        	throw new XMLUtilsException("Invalid xpaths array");
        }
        
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            docOut = (Document) parser.parseObject(docs[0], false, true, false);
            for (int i = 1; i < docs.length; i++) {
            	String xpSD = (String) xpaths[i];
            	int idx = xpSD.indexOf("##");
                String xpathSrc = xpSD.substring(0, idx);
                String xpathDest = xpSD.substring(idx+2);
                Object srcD = docs[i];
                if (srcD != null) {
                    Document docS = (Document) parser.parseObject(srcD, false, true, false);
                    NodeList sources = parser.selectNodeList(docS, xpathSrc);
                    if (sources.getLength() > 0) {
                        Node destNode = parser.selectSingleNode(docOut, xpathDest);
                        int len = sources.getLength();
                        for (int j = 0; j < len; ++j) {
                            Node node = sources.item(j);
                            node = docOut.importNode(node, true);
                            destNode.appendChild(node);
                        }
                    }
                }
            }
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
        
        return docOut;
    }

    /**
     * Merge documents or elements.
     * 
     * @param docs
     *        an array of Document, Node, String, byte[] to be merged,
     *        the first document is the destination of the merging process
     * @param xpaths
     *        an array of source##destination xpath: for the nth document 'source' is the search xpath for node to copy 
     *        and 'destination' is the xpath of the node of the first document on which add the selected nodes,
     *        the first entry of the array is ignored
     * @return a merged Document
     * @throws XMLUtilsException
     */
    public static Document mergeXML_S(Object[] docs, Object[] xpaths) throws XMLUtilsException
    {
    	XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.mergeXML(docs, xpaths);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }


    /**
     * Serialize a given DOM to an XML string, using default character encoding
     * UTF-8, as default omit_xml_decl property false, as default indent
     * property false. It uses JAXP XSL APIs.
     * 
     * @param doc
     *        The input DOM
     * @return the serialized document (as a <tt>String</tt> encoded using
     *         default character encoding UTF-8)
     * @throws XMLUtilsException
     *         on errors
     */
    public String serializeDOM(Node doc) throws XMLUtilsException
    {
        String result = null;
        try {
            byte[] xmlArrUtf8 = serializeDOMToByteArray(doc);
            result = new String(xmlArrUtf8, "UTF-8");
        }
        catch (UnsupportedEncodingException exc) {
            // This should never happen
            // because UTF-8 is guaranteed to be supported on
            // any Java platform implementation
        }
        return result;
    }

    /**
     * Serialize a given DOM to an XML string, using default character encoding
     * UTF-8, as default omit_xml_decl property false, as default indent
     * property false. It uses JAXP XSL APIs.
     * 
     * @param doc
     *        The input DOM
     * @return the serialized document (as a <tt>String</tt> encoded using
     *         default character encoding UTF-8)
     * @throws XMLUtilsException
     *         on errors
     */
    public static String serializeDOM_S(Node doc) throws XMLUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.serializeDOM(doc);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Serialize a given DOM to an XML string, using a specified character
     * encoding for the output XML string. If the specified encoding is not
     * supported or is null, defaults on UTF-8. Use as default omit_xml_decl
     * property false, as default indent property falseIt. uses JAXP XSL APIs.
     * 
     * @param doc
     *        The input DOM
     * @param encoding
     *        The desiderd character encoding for the output XML string
     * @return the serialized document (as a <tt>String</tt> encoded using the
     *         specified character encoding)
     * @throws XMLUtilsException
     *         on errors
     */
    public static String serializeDOM_S(Node doc, String encoding) throws XMLUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.serializeDOM(doc, encoding);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Serialize a given DOM to an XML string, using default character encoding
     * UTF-8. It uses JAXP XSL APIs.
     * 
     * @param doc
     *        The input DOM
     * @return the serialized document (as a <tt>String</tt> encoded using
     *         default character encoding UTF-8)
     * @throws XMLUtilsException
     *         on errors
     */
    public String serializeDOM(Document doc) throws XMLUtilsException
    {
        return serializeDOM((Node) doc);
    }

    /**
     * Serialize a given DOM to an XML string, using a specified character
     * encoding for the output XML string. If the specified encoding is not
     * supported or is null, defaults on UTF-8. Use as default omit_xml_decl
     * property false, as default indent property falseIt. uses JAXP XSL APIs.
     * 
     * @param doc
     *        The input DOM
     * @param encoding
     *        The desiderd character encoding for the output XML string
     * @return the serialized document (as a <tt>String</tt> encoded using the
     *         specified character encoding)
     * @throws XMLUtilsException
     *         on errors
     */
    public String serializeDOM(Node doc, String encoding) throws XMLUtilsException
    {
        String result;
        try {
            byte[] xmlArr = serializeDOMToByteArray(doc, encoding, false, false);
            result = new String(xmlArr, encoding);
            return result;
        }
        catch (UnsupportedEncodingException exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Serialize a given DOM to an XML string, using a specified character
     * encoding for the output XML string. If the specified encoding is not
     * supported or is null, defaults on UTF-8. It uses JAXP XSL APIs.
     * 
     * @param doc
     *        The input DOM
     * @param encoding
     *        The desired character encoding for the output XML string
     * @return the serialized document (as a <tt>String</tt> encoded using the
     *         specified character encoding)
     * @throws XMLUtilsException
     *         on errors
     */
    public String serializeDOM(Document doc, String encoding) throws XMLUtilsException
    {
        return serializeDOM((Node) doc, encoding);
    }

    /**
     * Serialize a given DOM to an XML string, using default character encoding
     * UTF-8. It uses JAXP XSL APIs.
     * 
     * @param doc
     *        The input DOM
     * @param omit_xml_decl
     * @param indent
     * @return the serialized document (as a <tt>String</tt> encoded using
     *         default character encoding UTF-8)
     * @throws XMLUtilsException
     *         on errors
     */
    public String serializeDOM(Node doc, boolean omit_xml_decl, boolean indent) throws XMLUtilsException
    {
        String result = null;
        try {
            byte[] xmlArrUtf8 = serializeDOMToByteArray(doc, omit_xml_decl, indent);
            result = new String(xmlArrUtf8, "UTF-8");
        }
        catch (UnsupportedEncodingException exc) {
            // This should never happen
            // because UTF-8 is guaranteed to be supported on
            // any Java platform implementation
        }
        return result;
    }

    /**
     * Serialize a given DOM to an XML string, using a specified character
     * encoding for the output XML string. If the specified encoding is not
     * supported or is null, defaults on UTF-8. It uses JAXP XSL APIs.
     * 
     * @param doc
     *        The input DOM
     * @param encoding
     *        The desired character encoding for the output XML string
     * @param omit_xml_decl
     * @param indent
     * @return the serialized document (as a <tt>String</tt> encoded using the
     *         specified character encoding)
     * @throws XMLUtilsException
     *         on errors
     */
    public static String serializeDOM_S(Node doc, String encoding, boolean omit_xml_decl, boolean indent)
            throws XMLUtilsException
    {
    	XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.serializeDOM(doc, encoding, omit_xml_decl, indent);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }
    
    /**
     * Serialize a given DOM to an XML string, using a specified character
     * encoding for the output XML string. If the specified encoding is not
     * supported or is null, defaults on UTF-8. It uses JAXP XSL APIs.
     * 
     * @param doc
     *        The input DOM
     * @param encoding
     *        The desired character encoding for the output XML string
     * @param omit_xml_decl
     * @param indent
     * @return the serialized document (as a <tt>String</tt> encoded using the
     *         specified character encoding)
     * @throws XMLUtilsException
     *         on errors
     */
    public String serializeDOM(Node doc, String encoding, boolean omit_xml_decl, boolean indent)
            throws XMLUtilsException
    {
        String result;
        try {
            byte[] xmlArr = serializeDOMToByteArray(doc, encoding, omit_xml_decl, indent);
            result = new String(xmlArr, encoding);
            return result;
        }
        catch (UnsupportedEncodingException exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Serialize a given DOM to a byte array containing an XML string, using
     * default character encoding UTF-8. It uses JAXP XSL APIs.
     * 
     * @param doc
     *        The input DOM
     * @return the serialized document (as a <tt>byte</tt> array)
     * @throws XMLUtilsException
     *         on errors
     */
    public static byte[] serializeDOMToByteArray_S(Node doc) throws XMLUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.serializeDOMToByteArray(doc);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Serialize a given DOM to a byte array containing an XML string, using a
     * specified character encoding for the output XML buffer. If the specified
     * encoding is not supported or is null, defaults on UTF-8. It uses JAXP XSL
     * APIs.
     * 
     * @param doc
     *        The input DOM
     * @param encoding
     *        The desired character encoding for the output XML buffer
     * @return the serialized document (as a <tt>byte</tt> array)
     * @throws XMLUtilsException
     *         on errors
     */
    public static byte[] serializeDOMToByteArray_S(Document doc, String encoding) throws XMLUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.serializeDOMToByteArray(doc, encoding);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Serialize a given DOM to a byte array containing an XML string, using
     * default character encoding UTF-8. It uses JAXP XSL APIs.
     * 
     * @param doc
     *        The input DOM
     * @return the serialized document (as a <tt>byte</tt> array)
     * @throws XMLUtilsException
     *         on errors
     */
    public byte[] serializeDOMToByteArray(Node doc) throws XMLUtilsException
    {
        return serializeDOMToByteArray(doc, "UTF-8", false, false);
    }

    /**
     * Serialize a given DOM to a byte array containing an XML string, using
     * default character encoding UTF-8. It uses JAXP XSL APIs.
     * 
     * @param doc
     *        The input DOM
     * @param omit_xml_decl
     * @param indent
     * @return the serialized document (as a <tt>byte</tt> array)
     * @throws XMLUtilsException
     *         on errors
     */
    public byte[] serializeDOMToByteArray(Node doc, boolean omit_xml_decl, boolean indent) throws XMLUtilsException
    {
        return serializeDOMToByteArray(doc, "UTF-8", omit_xml_decl, indent);
    }

    /**
     * Serialize a given DOM to a byte array containing an XML string, using a
     * specified character encoding for the output XML buffer. If the specified
     * encoding is not supported or is null, defaults on UTF-8. It uses JAXP XSL
     * APIs. Not Use omit_xml_decl property and indent property
     * 
     * @param doc
     *        The input DOM
     * @param encoding
     *        The desiderd character encoding for the output XML buffer
     * @return the serialized document (as a <tt>byte</tt> array)
     * @throws XMLUtilsException
     *         on errors
     */
    public byte[] serializeDOMToByteArray(Node doc, String encoding) throws XMLUtilsException
    {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            if (encoding == null) {
                encoding = "";
            }
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
            if (doc instanceof Document) {
                DocumentType docType = ((Document) doc).getDoctype();
                if (docType != null) {
                    String systemId = docType.getSystemId();
                    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemId);
                }
            }
            ByteArrayOutputStream arrOut = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(doc), new StreamResult(arrOut));
            return arrOut.toByteArray();
        }
        catch (Throwable exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Serialize a given DOM to a byte array containing an XML string, using a
     * specified character encoding for the output XML buffer. If the specified
     * encoding is not supported or is null, defaults on UTF-8. It uses JAXP XSL
     * APIs.
     * 
     * @param doc
     *        The input DOM
     * @param encoding
     *        The desired character encoding for the output XML buffer
     * @return the serialized document (as a <tt>byte</tt> array)
     * @throws XMLUtilsException
     *         on errors
     */
    public byte[] serializeDOMToByteArray(Document doc, String encoding) throws XMLUtilsException
    {
        return serializeDOMToByteArray((Node) doc, encoding);
    }

    /**
     * Serialize a given DOM to a byte array containing an XML string, using a
     * specified character encoding for the output XML buffer. If the specified
     * encoding is not supported or is null, defaults on UTF-8. It uses JAXP XSL
     * APIs.
     * 
     * @param doc
     *        The input DOM
     * @param encoding
     *        The desired character encoding for the output XML buffer
     * @param omit_xml_decl
     * @param indent
     * @return the serialized document (as a <tt>byte</tt> array)
     * @throws XMLUtilsException
     *         on errors
     */

    public static byte[] serializeDOMToByteArray_S(Node doc, String encoding, boolean omit_xml_decl, boolean indent)
            throws XMLUtilsException
    {
    	XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.serializeDOMToByteArray(doc, encoding, omit_xml_decl, indent);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }
    
    /**
     * Serialize a given DOM to a byte array containing an XML string, using a
     * specified character encoding for the output XML buffer. If the specified
     * encoding is not supported or is null, defaults on UTF-8. It uses JAXP XSL
     * APIs.
     * 
     * @param doc
     *        The input DOM
     * @param encoding
     *        The desired character encoding for the output XML buffer
     * @param omit_xml_decl
     * @param indent
     * @return the serialized document (as a <tt>byte</tt> array)
     * @throws XMLUtilsException
     *         on errors
     */

    public byte[] serializeDOMToByteArray(Node doc, String encoding, boolean omit_xml_decl, boolean indent)
            throws XMLUtilsException
    {
        try {
            ByteArrayOutputStream arrOut = new ByteArrayOutputStream();
            serializeDOMToStream(doc, arrOut, encoding, omit_xml_decl, indent);
            arrOut.close();
            return arrOut.toByteArray();
        }
        catch (Throwable exc) {
            exc.printStackTrace();
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Serialize a given DOM to an <tt>java.io.OutputStream</tt>, using default
     * character encoding UTF-8. It uses JAXP XSL APIs.
     * 
     * @param doc
     *        The input DOM
     * @param oStream
     *        the <tt>java.io.OutputStream</tt> to serialize the document to
     * @throws XMLUtilsException
     *         on errors
     */
    public static void serializeDOMToStream_S(Node doc, OutputStream oStream) throws XMLUtilsException
    {
    	XMLUtils parser = null;
        try {
            parser = getParserInstance();
            parser.serializeDOMToStream(doc, oStream);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }
    
    /**
     * Serialize a given DOM to an <tt>java.io.OutputStream</tt>, using default
     * character encoding UTF-8. It uses JAXP XSL APIs.
     * 
     * @param doc
     *        The input DOM
     * @param oStream
     *        the <tt>java.io.OutputStream</tt> to serialize the document to
     * @throws XMLUtilsException
     *         on errors
     */
    public void serializeDOMToStream(Node doc, OutputStream oStream) throws XMLUtilsException
    {
        serializeDOMToStream(doc, oStream, "UTF-8", false, false);
    }

    /**
     * Serialize a given DOM to an <tt>java.io.OutputStream</tt>, using a
     * specified character encoding for the output XML buffer. If the specified
     * encoding is not supported or is null, defaults on UTF-8. It uses JAXP XSL
     * APIs.
     * 
     * @param doc
     *        The input DOM
     * @param oStream
     *        the <tt>java.io.OutputStream</tt> to serialize the document to
     * @param encoding
     *        The desired character encoding for the output XML buffer
     * @param omit_xml_decl
     * @param indent
     * @throws XMLUtilsException
     *         on errors
     */
    public static void serializeDOMToStream_S(Node doc, OutputStream oStream, String encoding, boolean omit_xml_decl,
            boolean indent) throws XMLUtilsException
    {
	    XMLUtils parser = null;
	    try {
	        parser = getParserInstance();
	        parser.serializeDOMToStream(doc, oStream, encoding, omit_xml_decl, indent);
	    }
	    finally {
	        if (parser != null) {
	            XMLUtils.releaseParserInstance(parser);
	        }
	    }
    }
    /**
     * Serialize a given DOM to an <tt>java.io.OutputStream</tt>, using a
     * specified character encoding for the output XML buffer. If the specified
     * encoding is not supported or is null, defaults on UTF-8. It uses JAXP XSL
     * APIs.
     * 
     * @param doc
     *        The input DOM
     * @param oStream
     *        the <tt>java.io.OutputStream</tt> to serialize the document to
     * @param encoding
     *        The desired character encoding for the output XML buffer
     * @param omit_xml_decl
     * @param indent
     * @throws XMLUtilsException
     *         on errors
     */
    public void serializeDOMToStream(Node doc, OutputStream oStream, String encoding, boolean omit_xml_decl,
            boolean indent) throws XMLUtilsException
    {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, (omit_xml_decl ? "yes" : "no"));
            transformer.setOutputProperty(OutputKeys.INDENT, (indent ? "yes" : "no"));
            if (encoding == null) {
                encoding = "";
            }
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
            if (doc instanceof Document) {
                DocumentType docType = ((Document) doc).getDoctype();
                if (docType != null) {
                    String systemId = docType.getSystemId();
                    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemId);
                }
            }
            transformer.transform(new DOMSource(doc), new StreamResult(oStream));
        }
        catch (Throwable exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Gets the content of the given node. If the node has complex content (e.g
     * it is an Element node or a Document node), its serialization to XML
     * string is returned.
     * 
     * @param theNode
     *        The input DOM node.
     * @return its content as a string (as an XML string in case of complex
     *         content).
     * @throws XMLUtilsException
     *         when errors occur
     */
    public String getNodeContent(Node theNode) throws XMLUtilsException
    {
        if (theNode == null) {
            return "";
        }

        short nodeType = theNode.getNodeType();
        switch (nodeType) {
            case Node.ATTRIBUTE_NODE :
            case Node.CDATA_SECTION_NODE :
            case Node.COMMENT_NODE :
            case Node.TEXT_NODE :
                return theNode.getNodeValue().trim();
            default :
                StringBuilder buf = new StringBuilder("");
                NodeList childNodes = theNode.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node currChild = childNodes.item(i);
                    buf.append(getChildNodeContent(currChild));
                }
                return buf.toString().trim();
        }
    }

    /**
     * Detects if there's at least one node matching the XPath on the given
     * node.
     * 
     * @param theNode
     *        The input DOM node.
     * @param xPath
     *        The XPath to evaluate.
     * @return <code>true</code> if at least one node matching the XPath on the
     *         given node is found, <code>false</code> otherwise.
     * @throws XMLUtilsException
     *         when errors occurs.
     */
    public static boolean existNode_S(Node theNode, String xPath) throws XMLUtilsException
    {
    	XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.selectSingleNode(theNode, xPath) != null;
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Detects if there's at least one node matching the XPath on the given
     * node.
     * 
     * @param theNode
     *        The input DOM node.
     * @param xPath
     *        The XPath to evaluate.
     * @return <code>true</code> if at least one node matching the XPath on the
     *         given node is found, <code>false</code> otherwise.
     * @throws XMLUtilsException
     *         when errors occurs.
     */
    public boolean existNode(Node theNode, String xPath) throws XMLUtilsException
    {
        return selectSingleNode(theNode, xPath) != null;
    }

    /**
     * Gets the node matching the XPath on the given node.
     * 
     * @param theNode
     *        The input DOM node.
     * @param xPath
     *        The XPath to evaluate.
     * @return the node matching the XPath on the given node.
     * @throws XMLUtilsException
     *         when errors occurs.
     */
    public Node selectSingleNode(Node theNode, String xPath) throws XMLUtilsException
    {
        try {
            return (Node) xpathAPI.selectSingleNode(theNode, new XPath(xPath));
        }
        catch (Exception exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Gets the node matching the XPath on the given node.
     * 
     * @param theNode
     *        The input DOM node.
     * @param xPath
     *        The XPath to evaluate.
     * @return the node matching the XPath on the given node.
     * @throws XMLUtilsException
     *         when errors occurs.
     */
    public static Node selectSingleNode_S(Node theNode, String xPath) throws XMLUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.selectSingleNode(theNode, xPath);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Gets the node matching the XPath on the given xml.
     * 
     * @param xmlString
     *        The input XML as string.
     * @param xPath
     *        The XPath to evaluate.
     * @return the node matching the XPath on the given node.
     * @throws XMLUtilsException
     *         when errors occurs.
     */
    public static Node selectSingleNode_S(String xmlString, String xPath) throws XMLUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = getParserInstance();
            Node theNode = parser.parseDOM(xmlString);
            return parser.selectSingleNode(theNode, xPath);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Gets the list of nodes matching the XPath on the given node.
     * 
     * @param theNode
     *        The input DOM node.
     * @param xPath
     *        The XPath to evaluate.
     * @return the list of nodes matching the XPath on the given node.
     * @throws XMLUtilsException
     *         when errors occurs.
     */
    public NodeList selectNodeList(Node theNode, String xPath) throws XMLUtilsException
    {
        try {
            return (NodeList) xpathAPI.selectNodeList(theNode, new XPath(xPath));
        }
        catch (Exception exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Gets the list of nodes matching the XPath on the given node.
     * 
     * @param theNode
     *        The input DOM node.
     * @param xPath
     *        The XPath to evaluate.
     * @return the list of nodes matching the XPath on the given node.
     * @throws XMLUtilsException
     *         when errors occurs.
     */
    public static NodeList selectNodeList_S(Node theNode, String xPath) throws XMLUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.selectNodeList(theNode, xPath);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * @param theNode
     * @param xPath
     * @return the iterator to the list of nodes matching the XPath on the given
     *         node.
     * @throws XMLUtilsException
     */
    public NodeIterator selectNodeIterator(Node theNode, String xPath) throws XMLUtilsException
    {
        try {
            return (NodeIterator) xpathAPI.selectNodeIterator(theNode, new XPath(xPath));
        }
        catch (Exception exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Gets the string representation of the node content selected from XPath.
     * 
     * @param theNode
     *        The input DOM node.
     * @param xPath
     *        The XPath to evaluate.
     * @return the string representation of the node content selected from
     *         XPath.
     * @throws XMLUtilsException
     *         when errors occurs.
     */
    public String get(Node theNode, String xPath) throws XMLUtilsException
    {
        try {
            NodeList nl = (NodeList) xpathAPI.selectNodeList(theNode, new XPath(xPath));
            if (nl == null) {
                return null;
            }
            return XPathAPI.getNodeValue(nl);
        }
        catch (Exception exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * Gets the string representation of the node content selected from XPath.
     * 
     * @param theNode
     *        The input DOM node.
     * @param xPath
     *        The XPath to evaluate.
     * @param defaultValue
     *        The value returned if XPath does not select any node.
     * @return the string representation of the node content selected from
     *         XPath.
     * @throws XMLUtilsException
     *         when errors occurs.
     */
    public String get(Node theNode, String xPath, String defaultValue) throws XMLUtilsException
    {
        try {
            NodeList nl = (NodeList) xpathAPI.selectNodeList(theNode, new XPath(xPath));
            if ((nl != null) && (nl.getLength() > 0)) {
                return XPathAPI.getNodeValue(nl);
            }
        }
        catch (Exception exc) {
            // do nothing
        }
        return defaultValue;
    }

    /**
     * Static helper method the get the string representation of the node
     * content selected from XPath.
     * 
     * @param theNode
     *        The input DOM node.
     * @param xPath
     *        The XPath to evaluate.
     * @param defaultValue
     *        The value returned if XPath does not select any node.
     * @return the string representation of the node content selected from
     *         XPath.
     * @throws XMLUtilsException
     *         when errors occurs.
     * 
     * @see #get(Node, String, String)
     */
    public static String get_S(Node theNode, String xPath, String defaultValue) throws XMLUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.get(theNode, xPath, defaultValue);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Static helper method the get the string representation of the node
     * content selected from XPath.
     * 
     * @param theNode
     *        The input DOM node.
     * @param xPath
     *        The XPath to evaluate.
     * @return the string representation of the node content selected from
     *         XPath.
     * @throws XMLUtilsException
     *         when errors occurs.
     * 
     * @see #get(Node, String)
     */
    public static String get_S(Node theNode, String xPath) throws XMLUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = getParserInstance();
            return parser.get(theNode, xPath);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Sets a user-defined <tt>EntityResolver</tt> for the validating parser.
     * 
     * @param er
     *        an object implementing the <tt>EntityResolver</tt> interface
     */
    public static void setDefaultEntityResolver(EntityResolver er)
    {
        defaultEntityResolver = er;
    }

    /**
     * Sets a user-defined <tt>EntityResolver</tt> for the parser.
     * 
     * @param er
     *        an object implementing the <tt>EntityResolver</tt> interface
     */
    public void setEntityResolver(EntityResolver er)
    {
        EntityResolver erI = er;
        if (er == null) {
            erI = defaultEntityResolver;
        }
        docBuilder.setEntityResolver(erI);
        docBuilderNamespace.setEntityResolver(erI);
        docBuilderValidatingNamespace.setEntityResolver(erI);
        docBuilderValidating.setEntityResolver(erI);
    }

    /**
     * Create a new document without a name.
     * 
     * @return the new document created.
     */
    public Document newDocument()
    {
        return docBuilder.newDocument();
    }
    
    /**
     * Create a new document with a name.
     * 
     * @param rootName
     *        the name of the root element.
     * @return the new document created.
     * @throws XMLUtilsException
     */
    public Document newDocument(String rootName) throws NullPointerException
    {
        TextUtils.checkNull("Parmeter 'rootName' can't be null", rootName);
        Document doc = docBuilder.newDocument();
        Element root = doc.createElement(rootName);
        doc.appendChild(root);
        return doc;
    }

    /**
     * Create a new document with a name and a namespace.
     * 
     * @param rootName
     *        the name of the root element.
     * @param namespace
     *        the namespace URI.
     * @return the new document created.
     * @throws XMLUtilsException
     */
    public Document newDocument(String rootName, String namespace) throws NullPointerException
    {
        TextUtils.checkNull("Parmeter 'rootName' can't be null", rootName);
        TextUtils.checkNull("Parmeter 'namespace' can't be null", namespace);
        Document doc = docBuilder.newDocument();
        Element root = doc.createElementNS(namespace, rootName);
        doc.appendChild(root);
        return doc;
    }

    /**
     * Create a new document with prefix:name and a namespace.
     * 
     * @param rootName
     *        the name of the root element.
     * @param prefix
     *        the prefix of named root element.
     * @param namespace
     *        the namespace URI.
     * @return the new document created.
     * @throws NullPointerException
     */
    public Document newDocument(String rootName, String prefix, String namespace) throws NullPointerException
    {
        Document doc = newDocument(rootName, namespace);
        TextUtils.checkNull("Parmeter 'prefix' can't be null", prefix);
        Element root = doc.getDocumentElement();
        root.setPrefix(prefix);
        return doc;
    }

    /**
     * @param doc
     * @param name
     * @return the created element
     * @throws XMLUtilsException
     * @see org.w3c.dom.Document#createElement(String)
     */
    public Element createElement(Document doc, String name) throws NullPointerException
    {
        TextUtils.checkNull("Parmeter 'doc' can't be null", doc);
        TextUtils.checkNull("Parmeter 'name' can't be null", name);
        return doc.createElement(name);
    }

    /**
     * @param doc
     * @param name
     * @return the created element
     * @see org.w3c.dom.Document#createElement(String)
     */
    public Element createElementNS(Document doc, String name, String namespace) throws NullPointerException
    {
        TextUtils.checkNull("Parmeter 'doc' can't be null", doc);
        TextUtils.checkNull("Parmeter 'name' can't be null", name);
        TextUtils.checkNull("Parmeter 'namespace' can't be null", namespace);
        return doc.createElementNS(namespace, name);
    }

    /**
     * @param parent
     * @param name
     * @return the inserted element
     * @throws XMLUtilsException
     */
    public Element insertElement(Element parent, String name) throws XMLUtilsException
    {
        TextUtils.checkNull("Parmeter 'parent' can't be null", parent);
        TextUtils.checkNull("Parmeter 'name' can't be null", name);
        Document doc = parent.getOwnerDocument();
        if (doc == null) {
            throw new XMLUtilsException("Passed element have no owner document");
        }
        Element newElem;
        String namespace = parent.getNamespaceURI();
        if (namespace == null) {
            newElem = doc.createElement(name);
        }
        else {
            newElem = doc.createElementNS(namespace, name);
            String prefix = parent.lookupPrefix(namespace);
            if (prefix != null) {
                newElem.setPrefix(prefix);
            }
        }
        parent.appendChild(newElem);
        return newElem;
    }

    /**
     * @param parent
     * @param name
     * @return the parent element
     * @throws XMLUtilsException
     */
    public Element insertText(Element parent, String value) throws XMLUtilsException
    {
        TextUtils.checkNull("Parmeter 'parent' can't be null", parent);
        value = TextUtils.nullToEmpty(value);
        Document doc = parent.getOwnerDocument();
        if (doc == null) {
            throw new XMLUtilsException("Passed element have no owner document");
        }
        Text newText = doc.createTextNode(value);
        parent.appendChild(newText);
        return parent;
    }

    /**
     * @param parent
     * @param name
     * @return the parent element
     * @throws XMLUtilsException
     * @throws NullPointerException 
     */
    public Element insertCDATA(Element parent, String value) throws XMLUtilsException, NullPointerException
    {
        TextUtils.checkNull("Parmeter 'parent' can't be null", parent);
        value = TextUtils.nullToEmpty(value);
        Document doc = parent.getOwnerDocument();
        if (doc == null) {
            throw new XMLUtilsException("Passed element have no owner document");
        }
        CDATASection newCDATA = doc.createCDATASection(value);
        parent.appendChild(newCDATA);
        return parent;
    }

    /**
     * @param parent
     * @param name
     * @param value
     */
    public void setAttribute(Element parent, String name, String value) throws NullPointerException
    {
        TextUtils.checkNull("Parmeter 'parent' can't be null", parent);
        TextUtils.checkNull("Parmeter 'name' can't be null", name);
        value = TextUtils.nullToEmpty(value);
        String namespace = parent.getNamespaceURI();
        if (namespace == null) {
            parent.setAttribute(name, value);
        }
        else {
            parent.setAttributeNS(namespace, name, value);
        }
    }


    /**
     * Initialize the DOM parser setting up all needed features.
     * 
     * @throws ParserConfigurationException
     */
    private void setupDOMParser() throws ParserConfigurationException
    {
        docBuilderMap = new HashMap<String, DocumentBuilder>();
        // Instantiate non-validating DocumentBuilder
        docBuilder = getDocumentBuilder(false, false, true);
        docBuilderMap.put(getKey(false, false, true), docBuilder);
        docBuilderComments = getDocumentBuilder(false, false, false);
        docBuilderMap.put(getKey(false, false, false), docBuilderComments);
        docBuilderNamespace = getDocumentBuilder(false, true, true);
        docBuilderMap.put(getKey(false, true, true), docBuilderNamespace);
        docBuilderNamespaceComments = getDocumentBuilder(false, true, false);
        docBuilderMap.put(getKey(false, true, false), docBuilderNamespaceComments);
        // Instantiate validating DocumentBuilder
        docBuilderValidating = getDocumentBuilder(true, false, true);
        docBuilderMap.put(getKey(true, false, true), docBuilderValidating);
        docBuilderValidatingComments = getDocumentBuilder(true, false, false);
        docBuilderMap.put(getKey(true, false, false), docBuilderValidatingComments);
        docBuilderValidatingNamespace = getDocumentBuilder(true, true, true);
        docBuilderMap.put(getKey(true, true, true), docBuilderValidatingNamespace);
        docBuilderValidatingNamespaceComments = getDocumentBuilder(true, true, false);
        docBuilderMap.put(getKey(true, true, false), docBuilderValidatingNamespaceComments);
    }

    /**
     * Private method This method return correct access key
     * 
     * @param sourceType
     * @param destType
     * @return String sourceType::destType
     */
    private String getKey(boolean isValidating, boolean isNamespaceAware, boolean isIgnoringComments)
    {
        return (isValidating + "::" + isNamespaceAware + "::" + isIgnoringComments);
    }

    /**
     * Creates a new validating, or non-validating, DocumentBuilder.
     * 
     * @param isValidating
     *        true if we want a validating DOM Parser
     * @return DocumentBuilder
     * @throws ParserConfigurationException
     *         on errors
     */
    public DocumentBuilder getDocumentBuilder(boolean isValidating, boolean isNamespaceAware, boolean isIgnoringComments)
            throws ParserConfigurationException
    {
        // instantiate the parser
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Setup namespace awareness (true)
        factory.setNamespaceAware(isNamespaceAware);
        // Setup validation
        factory.setValidating(isValidating);
        // Enable XMLSchema validation (if needed)
        if (isValidating) {
            try {
                factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            }
            catch (IllegalArgumentException exc) {
                // Underlying parser doesn't support Jaxp 1.2
            }
        }
        // Ignore comments
        factory.setIgnoringComments(isIgnoringComments);
        // Ignore any whitespace
        factory.setIgnoringElementContentWhitespace(true);
        // Instantiate parser
        DocumentBuilder theDocBuilder = factory.newDocumentBuilder();
        // install a proper error handler
        theDocBuilder.setErrorHandler(new ErrHandler());
        // install a proper entity resolver
        theDocBuilder.setEntityResolver(defaultEntityResolver);
        // Return the DocumentBuilder object
        return theDocBuilder;
    }

    /**
     * Recursive helper method of <tt>getNodeContent</tt> method.
     * 
     * @param node
     *        The given DOM node.
     * @return its content as a string (as an XML string in case of complex
     *         content).
     * @throws XMLUtilsException
     * 
     */
    private String getChildNodeContent(Node node) throws XMLUtilsException
    {
        String nodeContent = null;
        short nodeType = node.getNodeType();
        // Get single node content
        switch (nodeType) {
            case Node.ATTRIBUTE_NODE :
            case Node.CDATA_SECTION_NODE :
            case Node.COMMENT_NODE :
            case Node.TEXT_NODE :
                nodeContent = node.getNodeValue();
                break;
            case Node.DOCUMENT_NODE :
                nodeContent = serializeDOM((Document) node);
                break;
            default :
                // Create a DOM having this node
                // as root element
                Document theDoc = docBuilder.newDocument();
                Node importedNode = theDoc.importNode(node, true);
                theDoc.appendChild(importedNode);
                // Serialize this DOM
                // omitting XML declaration
                try {
                    TransformerFactory tFactory = TransformerFactory.newInstance();
                    Transformer transformer = tFactory.newTransformer();
                    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    StringWriter strOut = new StringWriter();
                    transformer.transform(new DOMSource(theDoc), new StreamResult(strOut));
                    nodeContent = strOut.toString();
                }
                catch (TransformerConfigurationException exc) {
                    throw new XMLUtilsException(exc.getMessage(), exc);
                }
                catch (TransformerException exc) {
                    throw new XMLUtilsException(exc.getMessage(), exc);
                }
        }
        return nodeContent.trim();
    }

    /**
     * Used if we want to parse with no validation an XML Document containing a
     * DocType tag WARNING: Can't tell if the DocType tag was commented out.
     * 
     * @param xmlString
     *        The input XML string
     * @return The (eventually) modified XML string
     * 
     */
    private String removeDocType(String xmlString)
    {
        try {
            int tempIdx = xmlString.toUpperCase().indexOf("!DOCTYPE");
            if (tempIdx == -1) {
                return xmlString;
            }
            int beginTagIdx = xmlString.lastIndexOf('<', tempIdx);
            int endTagIdx = xmlString.indexOf('>', tempIdx);
            String beforeTag = xmlString.substring(0, beginTagIdx);
            String afterTag = xmlString.substring(endTagIdx + 1);
            return beforeTag + afterTag;
        }
        catch (Throwable exc) {
            return xmlString;
        }
    }

    /**
     * It applies the XPath on the document and returns the result.
     * 
     * @param doc
     *        The org.w3c.dom.Document
     * @param xpath
     *        The XPath
     * @return String the result
     * @throws Exception
     *         if an error occurred.
     */
    public String applyXPath(Document doc, String xpath) throws Exception
    {
        Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        serializer.setOutputProperty(OutputKeys.INDENT, "false");
        StringWriter stringWriter = new StringWriter();
        NodeIterator nl = selectNodeIterator(doc, xpath);
        Node n;
        while ((n = nl.nextNode()) != null) {
            if (isTextNode(n)) {
                stringWriter.write(n.getNodeValue());
                for (Node nn = n.getNextSibling(); isTextNode(nn); nn = nn.getNextSibling()) {
                    stringWriter.write(n.getNodeValue());
                }
            }
            else {
                serializer.transform(new DOMSource(n), new StreamResult(stringWriter));
            }
        }

        return stringWriter.toString();
    }

    /**
     * Return a boolean value that identify if the node is text
     * 
     * @param nodeToEvaluate
     *        the node to evaluate
     * @return boolean value
     */
    private boolean isTextNode(Node nodeToEvaluate)
    {
        if (nodeToEvaluate == null) {
            return false;
        }
        short nodeType = nodeToEvaluate.getNodeType();
        return (nodeType == Node.CDATA_SECTION_NODE) || (nodeType == Node.TEXT_NODE);
    }
}
