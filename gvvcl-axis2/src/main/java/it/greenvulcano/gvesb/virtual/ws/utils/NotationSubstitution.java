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
package it.greenvulcano.gvesb.virtual.ws.utils;

import it.greenvulcano.gvesb.virtual.ws.WSCallException;
import it.greenvulcano.gvesb.virtual.ws.dynamic.descr.OperationDescription;
import it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ParamDescription;
import it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ServiceDescription;
import it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.ArrayParameter;
import it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.ComplexParameter;
import it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.IParameterHolder;
import it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.OMParameter;
import it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.OMParameterHolder;
import it.greenvulcano.gvesb.virtual.ws.dynamic.io.parameter.SimpleParameter;
import it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IOMParameterWriter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.util.XMLUtils;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
@SuppressWarnings("deprecation")
public class NotationSubstitution
{
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(NotationSubstitution.class);

    /**
     * Creates a <code>ParameterHolder</code> for a given
     * <code>OperationDescription</code>.
     *
     * @param description
     *        the description of the operation inclusive parameter descriptions
     * @return the ParameterHolder
     */
    public static OMParameterHolder createParameterHolder(OperationDescription description)
    {
        return new OMParameterHolder(description);
    }

    /**
     * @param node
     * @param sb
     */
    public static void appendOMToText(OMNode node, StringBuffer sb)
    {
        if (node instanceof OMText) {
            sb.append(((OMText) node).getText());
        }
        else {
            sb.append(node);
        }
    }

    /**
     * Converts a given XML document to an OMElement.
     *
     * @param xmlDoc
     *        the XML document
     * @return the OMElement representation for the XML document
     */
    public static OMNode toOM(String xmlDoc)
    {
        try {
            return XMLUtils.toOM(new StringReader(xmlDoc));
        }
        catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
        catch (FactoryConfigurationError ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * @param xmlStream
     * @return the element
     */
    public static OMElement toOM(InputStream xmlStream)
    {
        OMElement element = null;

        try {
            try {
                int ch = -1;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((ch = xmlStream.read()) != -1) {
                    baos.write(ch);
                }
             
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            element = (OMElement) XMLUtils.toOM(xmlStream);
        }
        catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
        catch (FactoryConfigurationError ex) {
            ex.printStackTrace();
        }

        return element;
    }

    /**
     * Converts one <code>OMElement</code> into another which implements the DOM
     * interface <code>org.w3c.dom.Element</code>.
     *
     * @param element
     *        Simple OMElement
     * @return OMElement which implements org.w3c.dom.Element interface
     */
    public static Element toDOM(OMElement element)
    {
        OMFactory factory = DOOMAbstractFactory.getOMFactory();

        // getXmlStreamReader is cached to allow building the OM Model in
        // memory, so we can access the values through the AXIS OM later again
        return (Element) new StAXOMBuilder(factory, element.getXMLStreamReader()).getDocumentElement();
    }

    /**
     * @param xmlDoc
     * @return a <code>org.w3c.dom.Element</code>.
     */
    public static Element toDOM(String xmlDoc)
    {
        return toDOM(new ByteArrayInputStream(xmlDoc.getBytes()));
    }

    /**
     * @param stream
     * @return a <code>org.w3c.dom.Element</code>.
     */
    public static Element toDOM(InputStream stream)
    {
        Element e = null;

        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = f.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(stream));

            e = doc.getDocumentElement();
        }
        catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        catch (SAXException ex) {
            ex.printStackTrace();
        }

        return e;
    }

    /**
     * @param notation
     * @return the loaded notation
     * @throws WSCallException
     */
    public static String loadNotation(String notation) throws WSCallException
    {
        if (notation != null) {
            try {
                if (notation.startsWith("file:") || notation.startsWith("http:")) {
                    URL url = new URL(notation);
                    BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream()));
                    StringBuilder sb = new StringBuilder();
                    String x;

                    while ((x = r.readLine()) != null) {
                        sb.append(x);
                    }

                    notation = sb.toString();
                }
            }
            catch (MalformedURLException ex) {
                throw new WSCallException("Malformed URL", ex);
            }
            catch (IOException ex) {
                throw new WSCallException("IO Exception", ex);
            }
        }

        return notation;
    }

    /**
     * Creates the OMElement for Web Service parameter values in plain notation
     * from a String
     *
     * @param input
     *        the parameter values of a Web Service call in a
     *        <code>java.util.Map</code>
     * @param operationDesc
     *        the description of the operation, which should be invoked
     * @return the XML document as OMElement which contains the SOAP body of the
     *         SOAP Envelope
     * @throws WSCallException
     *         if an exception occurs
     */
    public static OMElement getBodyNotationAsOM(OperationDescription operationDesc, Map<String, String> input)
            throws WSCallException
    {
        OMParameterHolder holder = new OMParameterHolder(operationDesc);
        Map<String, ParamDescription> desc = operationDesc.getDescriptions();

        // cycling through parts
        for (Entry<String, ParamDescription> entry : desc.entrySet()) {
            ParamDescription pdesc = entry.getValue();
            String notation = input.get(entry.getKey());
            if (notation != null) {
                logger.debug("LOADING FOUND NOTATION: [" + notation + "] for param type: " + pdesc.getXmlType());
                handleParameterHolder(notation.toCharArray(), 0, holder, pdesc, null);
            }
            else {
                throw new WSCallException("Part " + entry.getKey() + " not found in input map");
            }
        }

        OMElement e = (OMElement) holder.getParameters();

        if (logger.isDebugEnabled()) {
            logger.debug("INPUT: " + e);
        }

        return e;
    }

    /**
     * @param operationDesc
     * @param notation
     * @param intermediateMap
     * @return the XML document as OMElement which contains the SOAP body of the
     *         SOAP Envelope
     * @throws WSCallException
     */
    public static OMElement getBodyNotationAsOM(OperationDescription operationDesc, String notation,
            Map<String, Object> intermediateMap) throws WSCallException
    {
        OMParameterHolder holder = new OMParameterHolder(operationDesc);
        Map<String, ParamDescription> desc = operationDesc.getDescriptions();

        // cycling through parts
        for (Entry<String, ParamDescription> entry : desc.entrySet()) {
            ParamDescription pdesc = entry.getValue();
            handleParameterHolder(notation.toCharArray(), 0, holder, pdesc, intermediateMap);
        }

        OMElement e = (OMElement) holder.getParameters();

        if (logger.isDebugEnabled()) {
            logger.debug("INPUT: " + e);
        }

        return e;
    }

    /**
     * @param operationDesc
     * @param key
     * @param notation
     * @param intermediateMap
     * @return the XML document as OMElement which contains the SOAP body of the
     *         SOAP Envelope
     * @throws WSCallException
     */
    public static OMElement getBodyNotationAsOM(OperationDescription operationDesc, String key, String notation,
            Map<String, Object> intermediateMap) throws WSCallException
    {
        OMParameterHolder holder = new OMParameterHolder(operationDesc);
        Map<String, ParamDescription> desc = operationDesc.getDescriptions();

        ParamDescription pdesc = desc.get(key);
        if (pdesc != null) {
            handleParameterHolder(notation.toCharArray(), 0, holder, pdesc, intermediateMap);
        }

        return (OMElement) holder.getParameters();
    }

    /**
     * Creates the OMElement for Web Service parameter values in plain notation
     * from a header string
     *
     * @param operationDesc
     *        the description of the operation, which should be invoked
     * @param key
     * @param notation
     *        the parameter values of a Web Service call in plain notation
     * @param intermediateMap
     * @return the XML document as OMElement which contains the SOAP body of the
     *         SOAP Envelope
     * @throws WSCallException
     *         if an exception occurs
     */
    public static OMElement getHeaderNotationAsOM(OperationDescription operationDesc, String key, String notation,
            Map<String, Object> intermediateMap) throws WSCallException
    {
        notation = loadNotation(notation);

        OMParameterHolder holder = new OMParameterHolder(operationDesc);
        Map<String, ParamDescription> desc = operationDesc.getHeaderDescriptions();
        if (desc != null) {
            handleParameterHolder(notation.toCharArray(), 0, holder, desc.get(key), intermediateMap);

            OMElement e = (OMElement) holder.getParameters();

            if (logger.isDebugEnabled()) {
                logger.debug("INPUT: " + e);
            }

            return e;
        }
        return null;
    }

    private static int getNextParameterSeparatorPosition(char[] chars, int i)
    {
        int tmp = chars.length;

        // avoid that '},' as well as '],' is handled as a parameter
        while (i < chars.length) {
            if (chars[i] == ',') {
                tmp = i;

                break;
            }

            i++;
        }

        return tmp;
    }

    private static void checkForException(ParamDescription[] descriptions, int descIndex) throws WSCallException
    {
        if ((descriptions == null)
                || ((descIndex >= descriptions.length) || (descriptions[descIndex].getXmlType() == ParamDescription.VOID))) {
            throw new WSCallException("Too much parameter values specified.");
        }
    }

    private static void checkForException(ParamDescription desc) throws WSCallException
    {
        if ((desc == null) || (desc.getXmlType() == ParamDescription.VOID)) {
            throw new WSCallException("Too much parameter values specified.");
        }
    }

    /**
     * Gets the ServiceDescription for a given name
     *
     * @param services
     *        the ServiceDescription
     * @param name
     *        the name of the ServiceDescription, which should be returned
     * @return the appropriate ServiceDescription
     */
    public static ServiceDescription getServiceDesciption(ServiceDescription[] services, String name)
    {
        ServiceDescription d = null;

        for (int i = 0; i < services.length; i++) {
            ServiceDescription s = services[i];
            if (s.getXmlName().getLocalPart().equals(name)) {
                d = s;

                break;
            }
        }

        return d;
    }

    /**
     * Gets the OperationDescription of a ServiceDescription for a given name
     *
     * @param service
     *        the description of a service
     * @param name
     *        the name of the operation
     * @return the appropriate OperationDescription
     */
    public static OperationDescription getOperationDescription(ServiceDescription service, String name)
    {
        return (OperationDescription) service.getDescriptions().get(name);
    }

    private static int checkSlashReplacement(StringBuilder sb, char[] chars, int i)
    {
        while ((i < chars.length) && (chars[i] == '\\')) {
            if (chars[++i] == '\\') {
                sb.append("\\");
            }
            else {
                if (chars[i] == '"') {
                    sb.append("\"");
                }
                else {

                    // decrement so that the method invoker
                    // can handle the character at position i
                    --i;
                }

                break;
            }

            i++;
        }

        return i;
    }

    private static int handleParameterHolder(char[] chars, int i, IParameterHolder holder,
            Map<String, ParamDescription> descriptions, Map<String, Object> intermediateMap) throws WSCallException
    {
        ParamDescription[] pDescriptions = new ParamDescription[descriptions.size()];
        pDescriptions = descriptions.values().toArray(pDescriptions);
        return handleParameterHolder(chars, i, holder, pDescriptions, 0, false, intermediateMap);
    }

    private static int handleParameterHolder(char[] chars, int i, IParameterHolder holder, ParamDescription desc,
            Map<String, Object> pSubstMap) throws WSCallException
    {
        logger.debug("descriptions " + desc.getXmlName());

        // simple1, simple2, [array1, array2], {complex1, complex2}, simple3
        StringBuilder sb = new StringBuilder();

        // we have a string which is wasQuoted "string"
        boolean isString = false;

        // helper variable to see if isString was before activated
        boolean wasQuoted = false;

        // we have a string which is an XML string
        boolean isXML = false;

        // we have a parameter to substitute from intermediate map
        boolean paramSubstitute = false;

        while (i < chars.length) {
            switch (chars[i]) {
                case '<' :{
                    if (!isString) {
                        isXML = true;
                    }
                    sb.append('<');
                    break;
                }
                case '>' :{
                    sb.append('>');
                    break;
                }
                case '"' :{
                    if (isXML) {
                        sb.append('"');
                        break;
                    }
                    isString = !isString;

                    // always true if " occurred, with handleQuoted... it
                    // is set to false
                    wasQuoted = true;

                    break;
                }
                case '\\' :{
                    if (isString) {
                        i = checkSlashReplacement(sb, chars, i);
                    }
                    else {
                        sb.append('\\');
                    }

                    break;
                }
                case ' ' :{
                    if (isString || isXML) {
                        sb.append(' ');
                    }

                    break;
                }
                case ',' :{
                    if (isXML) {
                        isXML = false;
                    }
                    if (isString) {
                        sb.append(',');
                    }
                    else {
                        checkForException(desc);

                        String content = sb.toString();

                        if (paramSubstitute) {
                            handleParameterSubstitution(holder, desc, content, pSubstMap);
                            paramSubstitute = false;
                            wasQuoted = false;
                        }
                        else if (handleQuotedSimpleParameter(holder, desc, content, wasQuoted)) {
                            wasQuoted = false;
                        }

                        sb.delete(0, sb.length());
                    }

                    break;
                }
                case '[' :{
                    if (isString) {
                        sb.append("[");
                    }
                    else {
                        checkForException(desc);

                        if (!desc.isArray()) {
                            logger.error("IS NO ARRAY: " + desc);

                            throw new WSCallException("Parameter " + desc.getXmlName() + " is not an array!");
                        }
                        else {
                            ArrayParameter array = new ArrayParameter();

                            holder.addParameter(array);
                            i = handleParameterHolder(chars, i + 1, array, desc, pSubstMap);
                            sb.delete(0, sb.length());
                            i = getNextParameterSeparatorPosition(chars, i + 1);
                        }
                    }

                    break;
                }
                case '#' :{
                    if (isString) {
                        sb.append('#');
                    }
                    else {
                        paramSubstitute = true;
                    }
                    break;
                }
                case '{' :{
                    if (isString) {
                        sb.append("{");
                    }
                    else {
                        checkForException(desc);

                        Map<String, ParamDescription> descs = desc.getDescriptions();

                        if ((descs == null) || (descs.size() == 0)) {
                            logger.error("IS NOT COMPLEX: " + desc);

                            throw new WSCallException("Parameter " + desc.getXmlName() + " is not complex!");
                        }
                        else {
                            ComplexParameter p = new ComplexParameter(desc);

                            holder.addParameter(p);
                            i = handleParameterHolder(chars, i + 1, p, descs, pSubstMap);
                            sb.delete(0, sb.length());
                            i = getNextParameterSeparatorPosition(chars, i + 1);
                        }
                    }
                    break;
                }
                case '}' :
                    if (isString) {
                        sb.append("}");
                    }
                    break;
                case ']' :
                    if (isString) {
                        sb.append("]");

                        break;
                    }

                    checkForException(desc);

                    String content = sb.toString();

                    if (paramSubstitute) {
                        handleParameterSubstitution(holder, desc, content, pSubstMap);
                        paramSubstitute = false;
                        wasQuoted = false;
                    }
                    else if (handleQuotedSimpleParameter(holder, desc, content, wasQuoted)) {
                        wasQuoted = false;
                    }

                    return i;
                default :{
                    sb.append(chars[i]);
                }
            }

            i++;
        }

        if (sb.length() > 0) {
            checkForException(desc);

            String content = sb.toString();
            if (paramSubstitute) {
                handleParameterSubstitution(holder, desc, content, pSubstMap);
                paramSubstitute = false;
                wasQuoted = false;
            }
            else if (handleQuotedSimpleParameter(holder, desc, content, wasQuoted)) {
                wasQuoted = false;
            }
        }

        // the return value here is not of belong, it should be the chars.length
        // value
        return i - 1;
    }

    private static int handleParameterHolder(char[] chars, int i, IParameterHolder holder,
            ParamDescription[] descriptions, int descIndex, boolean isArray, Map<String, Object> pSubstMap)
            throws WSCallException
    {
        // simple1, simple2, [array1, array2], {complex1, complex2}, simple3
        StringBuilder sb = new StringBuilder();

        // we have a string which is wasQuoted "string"
        boolean isString = false;

        // helper variable to see if isString was before activated
        boolean wasQuoted = false;

        // we have a string which is an XML string
        boolean isXML = false;

        // we have a parameter to substitute from intermediate map
        boolean paramSubstitute = false;

        while (i < chars.length) {
            switch (chars[i]) {
                case '<' :{
                    if (!isString) {
                        isXML = true;
                    }
                    sb.append('<');
                    break;
                }
                case '>' :{
                    sb.append('>');
                    break;
                }
                case '"' :{
                    if (isXML) {
                        sb.append('"');
                        break;
                    }
                    isString = !isString;

                    // always true if " occurred, with handleQuoted... it
                    // is set to false
                    wasQuoted = true;

                    break;
                }
                case '\\' :{
                    if (isString) {
                        i = checkSlashReplacement(sb, chars, i);
                    }
                    else {
                        sb.append("\\");
                    }

                    break;
                }
                case ' ' :{
                    if (isString || isXML) {
                        sb.append(" ");
                    }

                    break;
                }
                case ',' :{
                    if (isXML) {
                        isXML = false;
                    }
                    if (isString) {
                        sb.append(",");
                    }
                    else {
                        checkForException(descriptions, descIndex);

                        String content = sb.toString();
                        if (paramSubstitute) {
                            handleParameterSubstitution(holder, descriptions[descIndex], content, pSubstMap);
                            paramSubstitute = false;
                            wasQuoted = false;
                        }
                        else if (handleQuotedSimpleParameter(holder, descriptions[descIndex], content, wasQuoted)) {
                            wasQuoted = false;
                        }

                        sb.delete(0, sb.length());

                        if (!isArray) {
                            descIndex++;
                        }
                    }

                    break;
                }
                case '[' :{
                    if (isString) {
                        sb.append("[");
                    }
                    else {
                        checkForException(descriptions, descIndex);

                        if (!descriptions[descIndex].isArray()) {
                            logger.error("IS NO ARRAY: " + descriptions[descIndex]);

                            throw new WSCallException("Parameter " + descriptions[descIndex].getXmlName()
                                    + " is not an array!");
                        }
                        else {
                            ArrayParameter array = new ArrayParameter();

                            holder.addParameter(array);
                            i = handleParameterHolder(chars, i + 1, array, descriptions, descIndex, true, pSubstMap);
                            sb.delete(0, sb.length());
                            i = getNextParameterSeparatorPosition(chars, i + 1);

                            // next parameter pls
                            descIndex++;
                        }
                    }

                    break;
                }
                case '#' :{
                    if (isString) {
                        sb.append('#');
                    }
                    else {
                        paramSubstitute = true;
                    }
                    break;
                }
                case '{' :{
                    if (isString) {
                        sb.append("{");
                    }
                    else {
                        checkForException(descriptions, descIndex);

                        Map<String, ParamDescription> descs = descriptions[descIndex].getDescriptions();

                        if ((descs == null) || (descs.size() == 0)) {
                            logger.error("IS NOT COMPLEX: " + descriptions[descIndex]);

                            throw new WSCallException("Parameter " + descriptions[descIndex].getXmlName()
                                    + " is not complex!");
                        }
                        else {
                            ComplexParameter p = new ComplexParameter(descriptions[descIndex]);

                            holder.addParameter(p);
                            i = handleParameterHolder(chars, i + 1, p, descs, pSubstMap);
                            sb.delete(0, sb.length());
                            i = getNextParameterSeparatorPosition(chars, i + 1);

                            if (!isArray) {
                                descIndex++;
                            }
                        }
                    }

                    break;
                }
                case '}' :
                    if (isString) {
                        sb.append("}");
                    }
                    break;
                case ']' :
                    if (isString) {
                        sb.append("]");

                        break;
                    }

                    checkForException(descriptions, descIndex);

                    String content = sb.toString().trim();

                    if (paramSubstitute) {
                        handleParameterSubstitution(holder, descriptions[descIndex], content, pSubstMap);
                        paramSubstitute = false;
                        wasQuoted = false;
                    }
                    else if (handleQuotedSimpleParameter(holder, descriptions[descIndex], content, wasQuoted)) {
                        wasQuoted = false;
                    }

                    return i;

                default :{
                    sb.append(chars[i]);
                }
            }

            i++;
        }

        if (sb.length() > 0) {
            checkForException(descriptions, descIndex);

            String content = sb.toString();

            if (paramSubstitute) {
                handleParameterSubstitution(holder, descriptions[descIndex], content, pSubstMap);
                paramSubstitute = false;
                wasQuoted = false;
            }
            else if (handleQuotedSimpleParameter(holder, descriptions[descIndex], content, wasQuoted)) {
                wasQuoted = false;
            }
        }

        // the return value here is not of belong, it should be the chars.length
        // value
        return i - 1;
    }

    private static boolean handleParameterSubstitution(IParameterHolder holder, ParamDescription description,
            String content, Map<String, Object> substMap)
    {
        Object value = substMap.get(content);
        if (value != null) {
            OMNode node = null;
            if (value instanceof byte[]) {
                String str = "<" + IOMParameterWriter.MY_ROOT_NODE_CONST + ">";
                str += "</" + IOMParameterWriter.MY_ROOT_NODE_CONST + ">";
                OMNode child = null;
                node = toOM(str);
                ByteArrayDataSource ds = new ByteArrayDataSource((byte[]) value);
                DataHandler dh = new DataHandler(ds);
                child = new OMTextImpl(dh, OMAbstractFactory.getOMFactory());
                ((OMElement) node).addChild(child);
            }
            else {
                String str = "<" + IOMParameterWriter.MY_ROOT_NODE_CONST + ">";
                str += value.toString();
                str += "</" + IOMParameterWriter.MY_ROOT_NODE_CONST + ">";
                node = toOM(str);
            }
            holder.addParameter(new OMParameter(description, node));
        }
        else {
            holder.addParameter(new SimpleParameter(description, "#" + content));
        }
        return false;
    }

    private static boolean handleQuotedSimpleParameter(IParameterHolder holder, ParamDescription description,
            String content, boolean quoted)
    {
        if (!content.equals("x")) {
            if (quoted || (content.length() > 0)) {
                if (content.startsWith("<") && content.endsWith(">")) {
                    String str = "<" + IOMParameterWriter.MY_ROOT_NODE_CONST + ">";
                    str += content;
                    str += "</" + IOMParameterWriter.MY_ROOT_NODE_CONST + ">";
                    holder.addParameter(new OMParameter(description, toOM(str)));
                }
                else {
                    holder.addParameter(new SimpleParameter(description, content));
                }
            }

            // its quoted
            return true;
        }

        // its not quoted
        return false;
    }

    /**
     * @param message
     * @return the created SOAP envelope wrapping the message
     */
    public static SOAPEnvelope createSoapEnvelope(String message)
    {
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();

        envelope.getBody().addChild(toOM(message));

        return envelope;
    }
}
