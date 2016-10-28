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
package it.greenvulcano.gvesb.adapter.http.formatters.handlers;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.utils.GVBufferAccess;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;
import it.greenvulcano.util.xpath.XPathDOMBuilder;
import it.greenvulcano.util.xpath.XPathUtilsException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This subclass of
 * <tt>InterfaceParametersHandler<tt> abstract class will handle
 * HTTP parameters containing XML strings.<p>
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
@SuppressWarnings("rawtypes")
public class XMLParameterHandler extends InterfaceParametersHandler
{
    class PropertyListHandler
    {
        private boolean     urlEncoding             = false;
        private String      fieldEntrySeparator     = null;
        private String      fieldNameValueSeparator = null;
        private String      charEnc                 = null;
        private Set<String> excluded                = new HashSet<String>();
        private Logger      logger                  = null;

        public PropertyListHandler(Logger logWriter)
        {
            this.logger = logWriter;
        }

        public void setCharEncoding(String charEnc)
        {
            this.charEnc = charEnc;
        }

        public void init(Node node) throws Exception
        {
            fieldEntrySeparator = XMLConfig.get(node, "@FieldEntrySeparator", ";");
            fieldNameValueSeparator = XMLConfig.get(node, "@FieldNameValueSeparator", "=");
            urlEncoding = XMLConfig.getBoolean(node, "@URLEncoding", false);
            String sExcluded = XMLConfig.get(node, "@ExcludeFields", "");

            StringTokenizer stExcluded = new StringTokenizer(sExcluded, ",");
            while (stExcluded.hasMoreTokens()) {
                excluded.add(stExcluded.nextToken());
            }
        }

        /**
         * @param currGVBufferFieldValue
         * @param previous
         * @throws Exception
         */
        public void buildGVBuffer(String currGVBufferFieldValue, GVBuffer previous) throws Exception
        {
            logger.debug("buildGVBuffer - GVBuffer properties list '" + currGVBufferFieldValue + "'");
            StringTokenizer stFV = new StringTokenizer(currGVBufferFieldValue, fieldEntrySeparator);
            while (stFV.hasMoreTokens()) {
                String field = stFV.nextToken();
                int idx = field.indexOf(fieldNameValueSeparator);
                if (idx != -1) {
                    String fieldName = field.substring(0, idx);
                    if (!excluded.contains(fieldName)) {
                        String fieldValue = field.substring(idx + 1);
                        if (urlEncoding && (charEnc != null)) {
                            fieldValue = URLDecoder.decode(fieldValue, charEnc);
                        }
                        previous.setProperty(fieldName, fieldValue);
                        logger.debug("buildGVBuffer - GVBuffer field '" + fieldName + "' populated with value: "
                                + fieldValue);
                    }
                }
            }
        }

        /**
         * @param input
         * @return the HTTP parameter value
         * @throws Exception
         */
        public String buildHttpParam(GVBuffer input) throws Exception
        {
            logger.debug("buildHttpParam - build GVBuffer properties list");
            String result = "";
            for (String name : input.getPropertyNamesSet()) {
                if (!excluded.contains(name)) {
                    String value = input.getProperty(name);
                    if (urlEncoding && (charEnc != null)) {
                        value = URLEncoder.encode(value, charEnc);
                    }
                    result += name + fieldNameValueSeparator + value + fieldEntrySeparator;
                    logger.debug("buildHttpParam - adding to list GVBuffer field '" + name + "' populated with value: "
                            + value);
                }
            }
            return result;
        }
    }

    /**
     * The level separator char within an XPath
     */
    private static final String XPATH_SEPARATOR_CHAR = "/";

    /**
     * The attribute node specifier char within an XPath
     */
    private static final String XPATH_ATTRIBUTE_CHAR = "@";

    /**
     * Log4j object used to write the logs
     */
    private static final Logger logger               = org.slf4j.LoggerFactory.getLogger(XMLParameterHandler.class);

    /**
     * If this is an <tt>GVBuffer</tt> handler or an <tt>OpType</tt> handler,
     * this Map contains mappings (<tt>DestVariable, XPathSource</tt>);
     * <tt>XPathSource</tt> is the XPath of the value to be extracted from XML
     * parameter and <tt>DestVariable</tt> is the variable to be set with that
     * value (the variable may be an <tt>GVBuffer</tt> field, or the
     * <tt>OpType</tt> variable).
     * 
     * If this is an <tt>HttpParam</tt> handler, this HashMap contains mappings
     * (<tt>XPathDest, SourceVariable</tt>);<tt>XPathDest</tt> is the XPath,
     * within the XML parameter to be generated, of the XML element (or
     * attribute) which value is to be set with a value taken from an
     * <tt>GVBuffer</tt> field.
     */
    private Map<String, String> xmlParamMappings     = null;

    /**
     * XPathDOMBuilder object used to convert an XPath in the corresponding DOM
     * branch.
     */
    private XPathDOMBuilder     domBuilder           = null;

    /**
     * Object that can handle a mapping of a list if GVBuffer properties.
     */
    private PropertyListHandler listHandler          = null;

    /**
     * Initialization method.
     * 
     * @param specificParamHandlerSection
     *        the node, within the DOM encapsulating AdapterHTTP configuration
     *        file, containing specific initialization info about a particular
     *        handler type.
     * @throws AdapterHttpConfigurationException
     *         if any error occurs.
     */
    @Override
    protected void initSpecific(Node specificParamHandlerSection) throws AdapterHttpConfigurationException
    {
        try {
            xmlParamMappings = new HashMap<String, String>();
            domBuilder = new XPathDOMBuilder();

            NodeList xmlMappingNodes = XMLConfig.getNodeList(specificParamHandlerSection, "*[@ItemType='Mapping']");
            for (int i = 0; i < xmlMappingNodes.getLength(); i++) {
                Node currMapping = xmlMappingNodes.item(i);
                String xpath = XMLConfig.get(currMapping, "@XPath");
                String mappingVariable = null;
                Node variable = null;

                if ((variable = XMLConfig.getNode(currMapping, "GVBufferField")) != null) {
                    mappingVariable = XMLConfig.get(variable, "@FieldName");
                }
                else if ((variable = XMLConfig.getNode(currMapping, "GVBufferProperty")) != null) {
                    mappingVariable = AdapterHttpConfig.MAPPING_VARIABLE_VALUE_PROPERTY
                            + AdapterHttpConfig.TOKEN_SEPARATOR + XMLConfig.get(variable, "@FieldName");
                }
                else if ((variable = XMLConfig.getNode(currMapping, "GVBufferPropertyList")) != null) {
                    mappingVariable = AdapterHttpConfig.MAPPING_VARIABLE_VALUE_PROPERTYLIST;
                    listHandler = new PropertyListHandler(logger);
                    listHandler.init(variable);
                }
                else if ((variable = XMLConfig.getNode(currMapping, "OpType")) != null) {
                    mappingVariable = AdapterHttpConfig.MAPPING_VARIABLE_VALUE_OPTYPE;
                }
                else if ((variable = XMLConfig.getNode(currMapping, "GVTransactionInfo")) != null) {
                    mappingVariable = XMLConfig.get(variable, "@Value");
                }
                else if ((variable = XMLConfig.getNode(currMapping, "GVTransactionErrorInfo")) != null) {
                    mappingVariable = XMLConfig.get(variable, "@Value");
                }
                else if ((variable = XMLConfig.getNode(currMapping, "DefaultErrorMessage")) != null) {
                    mappingVariable = XMLConfig.getNodeValue(variable);
                }
                else if ((variable = XMLConfig.getNode(currMapping, "DefaultValue")) != null) {
                    mappingVariable = XMLConfig.getNodeValue(variable);
                }

                logger.debug("initSpecific - mappingVariable: " + mappingVariable);

                if ((getHandlerType() == GVBUFFER_HANDLER) || (getHandlerType() == OPTYPE_HANDLER)) {
                    xmlParamMappings.put(mappingVariable, xpath);
                }
                else {
                    xmlParamMappings.put(xpath, mappingVariable);
                }
            }
        }
        catch (XMLConfigException exc) {
            logger.error("initSpecific - Error while using XMLConfig: " + exc);
            throw new AdapterHttpConfigurationException("GVHTTP_ADAPTER_XML_CONFIG_ERROR", exc);
        }
        catch (Throwable exc) {
            logger.error("initSpecific - Generic error during XMLParameterHandler initialization: " + exc);
            throw new AdapterHttpConfigurationException("GVHTTP_ADAPTER_RUNTIME_ERROR", new String[][]{
                    {"phase", "XMLParameterHandler object initialization"}, {"errorName", "" + exc}}, exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.adapter.http.formatters.handlers.
     *      InterfaceParametersHandler#setCharEncoding(java.lang.String)
     */
    @Override
    public void setCharEncoding(String enc)
    {
        super.setCharEncoding(enc);
        if (listHandler != null) {
            listHandler.setCharEncoding(enc);
        }
    }

    /**
     * Parameter handler method.
     * 
     * @param input
     *        the input HTTP parameter to be used to populate an GVBuffer object.
     * @param previous
     *        the output GVBuffer object from a previous handler.
     * @return the resulting output GVBuffer object.
     * @throws DataHandlerException
     *         if any error occurs.
     */
    @Override
    protected GVBuffer buildGVBuffer(Object input, GVBuffer previous) throws DataHandlerException
    {
        Document doc = null;
        String currGVBufferField = null;
        String currGVBufferFieldValue = null;
        GVBufferAccess gvBufferAccess = null;

        try {
            Iterator iter = xmlParamMappings.keySet().iterator();
            while (iter.hasNext()) {
                String currDestVar = (String) iter.next();
                String currXPath = xmlParamMappings.get(currDestVar);

                logger.debug("buildGVBuffer - target variable: " + currDestVar + " - source XPath: " + currXPath);

                if ((currDestVar != null) && isGVBufferField(currDestVar)) {

                    if (doc == null) {
                        // parse XML parameter into a DOM
                        doc = parseDOM(input, "Input");
                    }

                    if (previous == null) {
                        // No handlers before this one:
                        // the GVBuffer object has to be created
                        // from scratch
                        previous = new GVBuffer();
                    }
                    if (gvBufferAccess == null) {
                        gvBufferAccess = new GVBufferAccess(previous);
                    }

                    currGVBufferFieldValue = getNodeContent(doc, currXPath);

                    if (currDestVar.equals(AdapterHttpConfig.MAPPING_VARIABLE_VALUE_PROPERTYLIST)) {
                        listHandler.buildGVBuffer(currGVBufferFieldValue, previous);
                    }
                    else {
                        gvBufferAccess.setField(currDestVar, currGVBufferFieldValue);
                        logger.debug("buildGVBuffer - GVBuffer field '" + currDestVar + "' populated with value: "
                                + currGVBufferFieldValue);
                    }
                }
            }

            return previous;
        }
        catch (XMLUtilsException exc) {
            logger.error("buildGVBuffer - Error while using XMLUtils object: " + exc);
            throw new DataHandlerException("GVHTTP_XML_UTILS_ERROR", new String[][]{{"action", "using"}}, exc);
        }
        catch (TransformerException exc) {
            logger.error("buildGVBuffer - Error while using XPath API: " + exc);
            throw new DataHandlerException("GVHTTP_XPATH_API_ERROR", exc);
        }
        catch (GVException exc) {
            logger.error("buildGVBuffer - Error while setting GVBuffer object field " + currGVBufferField + ": " + exc);
            throw new DataHandlerException("GVHTTP_EBDATA_FIELD_SETTING_ERROR", new String[][]{
                    {"fieldName", currGVBufferField}, {"fieldValue", currGVBufferFieldValue}, {"errorName", "" + exc}},
                    exc);
        }
        catch (UnsupportedEncodingException exc) {
            logger.error("buildGVBuffer - Encoding error while setting GVBuffer object field " + currGVBufferField
                    + ": " + exc);
            throw new DataHandlerException("GVHTTP_CHARACTER_ENCODING_ERROR", new String[][]{
                    {"encName", getCharEncoding()}, {"errorName", "" + exc}}, exc);
        }
        catch (Throwable exc) {
            logger.error("buildGVBuffer - Runtime error while populating GVBuffer object: " + exc);
            throw new DataHandlerException("GVHTTP_RUNTIME_ERROR", new String[][]{
                    {"phase", "GVBuffer object population"}, {"errorName", "" + exc}}, exc);
        }

    }

    /**
     * Parameter handler method. If there is more than one mapping to
     * <tt>OpType</tt> parameter, the corresponding value of <tt>OpType</tt> is
     * taken from the FIRST mapping found.
     * 
     * @param input
     *        the input HTTP parameter from which the <tt>OpType</tt> parameter has to be parsed.
     * @return the resulting <tt>OpType</tt> parameter value (as a String).
     * @throws OpTypeHandlerException
     *         if any error occurs.
     */
    @Override
    protected String buildOpTypeString(Object input) throws OpTypeHandlerException
    {
        String result = null;

        try {
            Iterator iter = xmlParamMappings.keySet().iterator();
            while (iter.hasNext()) {
                String currDestVar = (String) iter.next();
                String currXPath = xmlParamMappings.get(currDestVar);

                if ((currDestVar != null) && isOpType(currDestVar)) {
                    Document doc = parseDOM(input, "Input");
                    result = getNodeContent(doc, currXPath);
                    logger.debug("buildOpTypeString - OpType value is: " + result);
                    break;
                }
            }

            return result;
        }
        catch (XMLUtilsException exc) {
            logger.error("buildOpTypeString - Error while using XMLUtils object: " + exc);
            throw new OpTypeHandlerException("GVHTTP_XML_UTILS_ERROR", new String[][]{{"action", "using"}}, exc);
        }
        catch (TransformerException exc) {
            logger.error("buildOpTypeString - Error while using XPath API: " + exc);
            throw new OpTypeHandlerException("GVHTTP_XPATH_API_ERROR", exc);
        }
        catch (Throwable exc) {
            logger.error("buildOpTypeString - Runtime error while setting OpType variable value: " + exc);
            throw new OpTypeHandlerException("GVHTTP_RUNTIME_ERROR", new String[][]{
                    {"phase", "OpType variable value setting"}, {"errorName", "" + exc}}, exc);
        }
    }


    /**
     * Parameter handler method which takes an <tt>GVBuffer</tt> object as input
     * and generates an HTTP parameter value (a String) as output.
     * 
     * @param input
     *        the input GVBuffer object from which the value to assign to an
     *        HTTP parameter has to be parsed.
     * @param previous
     *        the output HTTP parameter value generated from a previous handler.
     * @return the resulting HTTP parameter value (as a String).
     * @throws HttpParamHandlerException
     *         if any error occurs.
     */
    @Override
    protected String buildHttpParam(GVBuffer input, String previous) throws HttpParamHandlerException
    {

        Document doc = null;
        XMLUtils xmlParser = null;
        String outputHttpParam = null;
        GVBufferAccess gvBufferAccess = null;

        try {
            if ((previous != null) && !previous.equals("")) {
                doc = parseDOM(previous, "Previous");
            }
            if (gvBufferAccess == null) {
                gvBufferAccess = new GVBufferAccess(input);
            }

            Iterator iter = xmlParamMappings.keySet().iterator();
            while (iter.hasNext()) {

                String currXPath = (String) iter.next();
                String currSourceVar = xmlParamMappings.get(currXPath);
                String nodeValue = null;

                logger.debug("buildHttpParam - source variable: " + currSourceVar + " - dest XPath: " + currXPath);

                // Get source value for the current mapping
                if (currSourceVar != null) {
                    // Create output DOM if still null
                    if (doc == null) {
                        // Create the output DOM
                        doc = domBuilder.createNewDocument();
                        cacheDOM(doc, "Previous");
                    }
                    if (currSourceVar.equals(AdapterHttpConfig.MAPPING_VARIABLE_VALUE_GVBUFFER)) {
                        String charEnc = getCharEncoding();
                        if (charEnc != null) {
                            //nodeValue = new String((byte[]) gvBufferAccess.getFieldAsObject(currSourceVar), charEnc);
                            nodeValue = gvBufferAccess.getFieldAsString(currSourceVar);
                            logger.debug("buildHttpParam - converting GVBuffer field 'object' to XML content using "
                                    + getCharEncoding() + " encoding");
                        }
                        else {
                            //nodeValue = new String((byte[]) gvBufferAccess.getFieldAsObject(currSourceVar));
                            nodeValue = gvBufferAccess.getFieldAsString(currSourceVar);
                            logger.debug("buildHttpParam - converting GVBuffer field 'data' to XML content using default Java encoding");
                        }
                    }
                    else if (currSourceVar.equals(AdapterHttpConfig.MAPPING_VARIABLE_VALUE_PROPERTYLIST)) {
                        nodeValue = listHandler.buildHttpParam(input);
                    }
                    else if (GVBufferAccess.isGVBufferField(currSourceVar)) {
                        // it's a mapping from an GVBuffer field
                        nodeValue = gvBufferAccess.getFieldAsString(currSourceVar);
                    }
                    else {
                        // it's a mapping from a default value
                        nodeValue = currSourceVar;
                    }

                    logger.debug("buildHttpParam - source variable value [" + currSourceVar + "] is: " + nodeValue);

                    // Create XML output:
                    // Find the element (or attribute) name
                    int idx = currXPath.lastIndexOf(XPATH_SEPARATOR_CHAR);
                    String parentXPath = currXPath.substring(0, idx);
                    String nodeName = currXPath.substring(idx + 1);

                    // Process node name
                    if (nodeName.indexOf(XPATH_ATTRIBUTE_CHAR) != -1) {
                        // It's an attribute
                        String attrName = nodeName.substring(1);
                        domBuilder.addAttribute(doc, parentXPath, attrName, nodeValue);
                    }
                    else {
                        // It's a generic element which will have a text
                        // content
                        domBuilder.addTextElement(doc, currXPath, nodeValue);
                    }
                }
            }

            // Serialize the output DOM
            xmlParser = XMLUtils.getParserInstance();
            outputHttpParam = xmlParser.serializeDOM(doc);
            logger.debug("buildHttpParam - outputHttpParam value set to: " + outputHttpParam);
            return outputHttpParam;
        }
        catch (XMLUtilsException exc) {
            logger.error("buildHttpParam - Error while using XMLUtils object: " + exc);
            throw new HttpParamHandlerException("GVHTTP_XML_UTILS_ERROR", new String[][]{{"action", "using"}}, exc);
        }
        catch (ParserConfigurationException exc) {
            logger.error("buildHttpParam - Error while instantiating output DOM factory: " + exc);
            throw new HttpParamHandlerException("GVHTTP_DOM_API_ERROR", exc);
        }
        catch (XPathUtilsException exc) {
            logger.error("buildHttpParam - Error while creating output DOM: " + exc);
            throw new HttpParamHandlerException("GVHTTP_XPATH_UTILS_ERROR", new String[][]{{"action", "using"}}, exc);
        }
        catch (UnsupportedEncodingException exc) {
            logger.error("buildHttpParam - Error while encoding GVBuffer field 'data' content to string: " + exc);
            throw new HttpParamHandlerException("GVHTTP_CHARACTER_ENCODING_ERROR", new String[][]{
                    {"encName", getCharEncoding()}, {"errorName", "" + exc}}, exc);
        }
        catch (Throwable exc) {
            logger.error("buildHttpParam - Runtime error while setting HTTP param value: " + exc);
            throw new HttpParamHandlerException("GVHTTP_RUNTIME_ERROR", new String[][]{
                    {"phase", "HTTP param value setting"}, {"errorName", "" + exc}}, exc);
        }
        finally {
            if (xmlParser != null) {
                XMLUtils.releaseParserInstance(xmlParser);
            }
        }

    }

    /**
     * Parameter handler method which takes an <tt>IBException</tt> object as
     * input and generates an HTTP parameter error value (a String) as output.
     * 
     * @param input
     *        the <tt>GVTransactionInfo</tt> object from which transaction error
     *        informations can be retrieved.
     * @param previous
     *        the output HTTP parameter value generated from a previous error
     *        handler.
     * @return the resulting HTTP parameter value (as a String).
     * @throws HttpParamHandlerException
     *         if any error occurs.
     */
    @Override
    protected String buildHttpParamError(GVTransactionInfo input, String previous) throws HttpParamHandlerException
    {
        Document doc = null;
        String outputHttpParam = null;
        XMLUtils xmlParser = null;

        try {
            if ((previous != null) && !previous.equals("")) {
                doc = parseDOM(previous.trim(), "Previous");
            }

            Iterator iter = xmlParamMappings.keySet().iterator();
            while (iter.hasNext()) {

                String currXPath = (String) iter.next();
                String currSourceVar = xmlParamMappings.get(currXPath);
                String nodeValue = null;

                logger.debug("buildHttpParamError - source variable: " + currSourceVar + " - dest XPath: " + currXPath);

                if ((currSourceVar != null) && !currSourceVar.equals("")) {
                    if (doc == null) {
                        doc = domBuilder.createNewDocument();
                    }

                    if (GVTransactionInfo.isGVTransInfoField(currSourceVar)) {
                        nodeValue = input.getFieldAsString(currSourceVar);
                    }
                    else {
                        nodeValue = currSourceVar;
                    }

                    logger.debug("buildHttpParamError - source variable value [" + currSourceVar + "] is: " + nodeValue);

                    int idx = currXPath.lastIndexOf(XPATH_SEPARATOR_CHAR);
                    String parentXPath = currXPath.substring(0, idx);
                    String nodeName = currXPath.substring(idx + 1);

                    if (nodeName.indexOf(XPATH_ATTRIBUTE_CHAR) != -1) {
                        String attrName = nodeName.substring(1);
                        domBuilder.addAttribute(doc, parentXPath, attrName, nodeValue);
                    }
                    else {
                        domBuilder.addTextElement(doc, currXPath, nodeValue);
                    }
                }
            }

            xmlParser = XMLUtils.getParserInstance();
            outputHttpParam = xmlParser.serializeDOM(doc);
            logger.debug("buildHttpParamError - outputHttpParam value set to: " + outputHttpParam);
            return outputHttpParam;
        }
        catch (XMLUtilsException exc) {
            logger.error("buildHttpParamError - Error while using XMLUtils object: " + exc);
            throw new HttpParamHandlerException("GVHTTP_XML_UTILS_ERROR", new String[][]{{"action", "using"}}, exc);
        }
        catch (ParserConfigurationException exc) {
            logger.error("buildHttpParamError - Error while instantiating output DOM factory: " + exc);
            throw new HttpParamHandlerException("GVHTTP_DOM_API_ERROR", exc);
        }
        catch (XPathUtilsException exc) {
            logger.error("buildHttpParamError - Error while creating output DOM: " + exc);
            throw new HttpParamHandlerException("GVHTTP_XPATH_UTILS_ERROR", new String[][]{{"action", "using"}}, exc);
        }
        catch (Throwable exc) {
            logger.error("buildHttpParamError - Runtime error while setting HTTP param error value: " + exc);
            throw new HttpParamHandlerException("GVHTTP_RUNTIME_ERROR", new String[][]{
                    {"phase", "HTTP param error value setting"}, {"errorName", "" + exc}}, exc);
        }
        finally {
            if (xmlParser != null) {
                XMLUtils.releaseParserInstance(xmlParser);
            }
        }
    }

    /**
     * Parameter handler method which takes an <tt>GVTransactionInfo</tt> object
     * as input and generates an HTTP parameter ACK value (a String) as output.
     * 
     * @param input
     *        the <tt>GVTransactionInfo</tt> object from which transaction
     *        informations can be retrieved.
     * @param previous
     *        the output HTTP parameter value generated from a previous ACK
     *        handler.
     * @return the resulting HTTP parameter value (as a String).
     * @throws HttpParamHandlerException
     *         if any error occurs.
     */
    @Override
    protected String buildHttpParamACK(GVTransactionInfo input, String previous) throws HttpParamHandlerException
    {
        Document doc = null;
        String outputHttpParam = null;
        XMLUtils xmlParser = null;

        try {
            if ((previous != null) && !previous.equals("")) {
                doc = parseDOM(previous.trim(), "Previous");
            }

            Iterator iter = xmlParamMappings.keySet().iterator();
            while (iter.hasNext()) {

                String currXPath = (String) iter.next();
                String currSourceVar = xmlParamMappings.get(currXPath);
                String nodeValue = null;

                logger.debug("buildHttpParamACK - source variable: " + currSourceVar + " - dest XPath: " + currXPath);

                if ((currSourceVar != null) && !currSourceVar.equals("")) {
                    if (doc == null) {
                        doc = domBuilder.createNewDocument();
                    }

                    if (GVTransactionInfo.isGVTransInfoField(currSourceVar)) {
                        nodeValue = input.getFieldAsString(currSourceVar);
                    }
                    else {
                        nodeValue = currSourceVar;
                    }

                    logger.debug("buildHttpParamACK - source variable value [" + currSourceVar + "] is: " + nodeValue);

                    int idx = currXPath.lastIndexOf(XPATH_SEPARATOR_CHAR);
                    String parentXPath = currXPath.substring(0, idx);
                    String nodeName = currXPath.substring(idx + 1);

                    if (nodeName.indexOf(XPATH_ATTRIBUTE_CHAR) != -1) {
                        String attrName = nodeName.substring(1);
                        domBuilder.addAttribute(doc, parentXPath, attrName, nodeValue);
                    }
                    else {
                        domBuilder.addTextElement(doc, currXPath, nodeValue);
                    }
                }
            }

            xmlParser = XMLUtils.getParserInstance();
            outputHttpParam = xmlParser.serializeDOM(doc);
            logger.debug("buildHttpParamACK - outputHttpParam ACK value set to: " + outputHttpParam);
            return outputHttpParam;
        }
        catch (XMLUtilsException exc) {
            logger.error("buildHttpParamACK - Error while using XMLUtils object: " + exc);
            throw new HttpParamHandlerException("GVHTTP_XML_UTILS_ERROR", new String[][]{{"action", "using"}}, exc);
        }
        catch (ParserConfigurationException exc) {
            logger.error("buildHttpParamACK - Error while instantiating output DOM factory: " + exc);
            throw new HttpParamHandlerException("GVHTTP_DOM_API_ERROR", exc);
        }
        catch (XPathUtilsException exc) {
            logger.error("buildHttpParamACK - Error while creating output DOM: " + exc);
            throw new HttpParamHandlerException("GVHTTP_XPATH_UTILS_ERROR", new String[][]{{"action", "using"}}, exc);
        }
        catch (Throwable exc) {
            logger.error("buildHttpParamACK - Runtime error while setting HTTP param ACK value: " + exc);
            throw new HttpParamHandlerException("GVHTTP_RUNTIME_ERROR", new String[][]{
                    {"phase", "HTTP param ACK value setting"}, {"errorName", "" + exc}}, exc);
        }
        finally {
            if (xmlParser != null) {
                XMLUtils.releaseParserInstance(xmlParser);
            }
        }
    }
}
