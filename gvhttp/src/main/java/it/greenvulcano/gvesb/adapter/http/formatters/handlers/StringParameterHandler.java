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
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This subclass of
 * <tt>InterfaceParametersHandler<tt> abstract class will handle
 * HTTP parameters containing simple text strings.<p>
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
@SuppressWarnings("rawtypes")
public class StringParameterHandler extends InterfaceParametersHandler
{
    private static Logger            logger        = org.slf4j.LoggerFactory.getLogger(StringParameterHandler.class);

    /**
     * If this is an <tt>GVBuffer</tt> handler or an <tt>OpType</tt> handler,
     * this <tt>ArrayList</tt> contains mappings of the input variable string to
     * <tt>GVBuffer</tt> fields or <tt>OpType</tt> variable.
     * 
     * If this is an <tt>HttpParam</tt> handler, this <tt>ArrayList</tt>
     * contains mappings of <tt>GVBuffer</tt> fields values to an HTTP response
     * parameter. The first mapping found sets the parameter value.
     */
    private Map<String, MappingData> paramMappings = null;

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
            paramMappings = new HashMap<String, MappingData>();

            NodeList mappingNodes = XMLConfig.getNodeList(specificParamHandlerSection, "*[@ItemType='Mapping']");
            for (int i = 0; i < mappingNodes.getLength(); i++) {
                Node currMapping = mappingNodes.item(i);
                String mappingVariable = null;
                Node variable = null;
                MappingData mappingData = new MappingData();
                mappingData.trim = XMLConfig.get(currMapping, "@Trim", "");
                mappingData.xpath = XMLConfig.get(currMapping, "@XPath", "");

                if ((variable = XMLConfig.getNode(currMapping, "GVBufferField")) != null) {
                    mappingVariable = XMLConfig.get(variable, "@FieldName");
                }
                else if ((variable = XMLConfig.getNode(currMapping, "GVBufferProperty")) != null) {
                    mappingVariable = AdapterHttpConfig.MAPPING_VARIABLE_VALUE_PROPERTY
                            + AdapterHttpConfig.TOKEN_SEPARATOR + XMLConfig.get(variable, "@FieldName");
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
                paramMappings.put(mappingVariable, mappingData);
            }
        }
        catch (XMLConfigException exc) {
            logger.error("initSpecific - Error while using XMLConfig: " + exc);
            throw new AdapterHttpConfigurationException("GVHTTP_ADAPTER_XML_CONFIG_ERROR", exc);
        }
        catch (Throwable exc) {
            logger.error("initSpecific - Generic error during StringParameterHandler initialization: " + exc);
            throw new AdapterHttpConfigurationException("GVHTTP_ADAPTER_RUNTIME_ERROR", new String[][]{
                    {"phase", "StringParameterHandler object initialization"}, {"errorName", "" + exc}}, exc);
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
        String currDestVar = null;
        String currGVBufferFieldValue = null;
        GVBufferAccess gvBufferAccess = null;
        Document doc = null;

        try {
            Iterator iter = paramMappings.keySet().iterator();
            while (iter.hasNext()) {
                currDestVar = (String) iter.next();
                MappingData mappingData = paramMappings.get(currDestVar);
                logger.debug("buildGVBuffer - target variable: " + currDestVar);

                if ((currDestVar != null) && isGVBufferField(currDestVar)) {
                    if (previous == null) {
                        // No handlers before this one:
                        // the GVBuffer object has to be created
                        // from scratch
                        previous = new GVBuffer();
                    }
                    if (gvBufferAccess == null) {
                        gvBufferAccess = new GVBufferAccess(previous);
                    }
                    if (mappingData.xpath.equals("")) {
                        currGVBufferFieldValue = getStringValue(input, mappingData);
                    }
                    else {
                        if (doc == null) {
                            // parse XML parameter into a DOM
                            doc = parseDOM(input, "Input");
                        }
                        String xmlValue = getNodeContent(doc, mappingData.xpath);
                        currGVBufferFieldValue = getStringValue(xmlValue, mappingData);
                    }

                    gvBufferAccess.setField(currDestVar, currGVBufferFieldValue);
                    logger.debug("buildGVBuffer - GVBuffer field '" + currDestVar + "' populated with value: "
                            + currGVBufferFieldValue);
                }
            }

            return previous;
        }
        catch (GVException exc) {
            logger.error("buildGVBuffer - Error while setting GVBuffer object field " + currDestVar + ": " + exc);
            throw new DataHandlerException("GVHTTP_GVDATA_FIELD_SETTING_ERROR", new String[][]{
                    {"fieldName", currDestVar}, {"fieldValue", currGVBufferFieldValue}, {"errorName", "" + exc}}, exc);
        }
        catch (UnsupportedEncodingException exc) {
            logger.error("buildGVBuffer - Encoding error while setting GVBuffer object field " + currDestVar + ": "
                    + exc);
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
     * Parameter handler method.
     * 
     * @param input
     *        the input HTTP parameter from which the OpType parameter has to be
     *        parsed.
     * @return the resulting OpType parameter value (as a String).
     * @throws OpTypeHandlerException
     *         if any error occurs.
     */
    @Override
    protected String buildOpTypeString(Object input) throws OpTypeHandlerException
    {
        String result = null;
        try {
            Iterator iter = paramMappings.keySet().iterator();
            while (iter.hasNext()) {
                String currDestVar = (String) iter.next();
                MappingData mappingData = paramMappings.get(currDestVar);
                logger.debug("buildOpTypeString - target variable: " + currDestVar);

                if (isOpType(currDestVar)) {
                    logger.debug("buildOpTypeString - Found mapping for OpType value");
                    result = getStringValue(input, mappingData);
                    logger.debug("buildOpTypeString - OpType value is: " + result);
                    break;
                }
            }
            return result;
        }
        catch (Throwable exc) {
            logger.error("buildOpTypeString - Runtime error while setting OpType variable value: " + exc);
            throw new OpTypeHandlerException("GVHTTP_RUNTIME_ERROR", new String[][]{
                    {"phase", "OpType variable value setting"}, {"errorName", "" + exc}}, exc);
        }
    }


    /**
     * Parameter handler method.
     * 
     * @param input
     *        the input GVBuffer object from which the value to assign to an
     *        HTTP parameter has to be parsed.
     * @param previous
     *        the output HTTP parameter value generated from a previous handler
     *        (NOT USED).
     * @return the resulting HTTP parameter value (as a String).
     * @throws HttpParamHandlerException
     *         if any error occurs.
     */
    @Override
    protected String buildHttpParam(GVBuffer input, String previous) throws HttpParamHandlerException
    {
        String outputHttpParam = null;
        GVBufferAccess gvBufferAccess = null;
        Document doc = null;

        try {
            Iterator iter = paramMappings.keySet().iterator();
            while (iter.hasNext()) {
                String currSourceVar = (String) iter.next();
                MappingData mappingData = paramMappings.get(currSourceVar);
                logger.debug("buildHttpParam - source variable: " + currSourceVar);

                // Get source value for the current mapping
                if (currSourceVar != null) {
                    if (gvBufferAccess == null) {
                        gvBufferAccess = new GVBufferAccess(input);
                    }

                    if (currSourceVar.equals(AdapterHttpConfig.MAPPING_VARIABLE_VALUE_GVBUFFER)) {
                        if (mappingData.xpath.equals("")) {
                            outputHttpParam = getStringValue(gvBufferAccess.getFieldAsObject(currSourceVar),
                                    mappingData);
                        }
                        else {
                            if (doc == null) {
                                // parse XML parameter into a DOM
                                doc = parseDOM(gvBufferAccess.getFieldAsObject(currSourceVar), currSourceVar);
                            }
                            String xmlValue = getNodeContent(doc, mappingData.xpath);
                            outputHttpParam = getStringValue(xmlValue, mappingData);
                        }
                    }
                    else if (GVBufferAccess.isGVBufferField(currSourceVar)) {
                        // it's a mapping from an GVBuffer extended field
                        outputHttpParam = gvBufferAccess.getFieldAsString(currSourceVar);
                    }
                    else {
                        // it's a mapping from a default value
                        outputHttpParam = currSourceVar;
                    }

                    logger.debug("buildHttpParam - source variable value [" + currSourceVar + "] is: "
                            + outputHttpParam);
                    break;
                }
            }

            // Return the output HTTP parameter
            logger.debug("buildHttpParam - outputHttpParam value set to: " + outputHttpParam);

            return outputHttpParam;
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
     *        handler (NOT USED).
     * @return the resulting HTTP parameter value (as a String).
     * @throws HttpParamHandlerException
     *         if any error occurs.
     */
    @Override
    protected String buildHttpParamError(GVTransactionInfo input, String previous) throws HttpParamHandlerException
    {
        String outputHttpParam = null;

        try {
            Iterator iter = paramMappings.keySet().iterator();
            while (iter.hasNext()) {
                String currSourceVar = (String) iter.next();
                MappingData mappingData = paramMappings.get(currSourceVar);
                logger.debug("buildHttpParamError - source variable: " + currSourceVar);

                if ((currSourceVar != null) && !currSourceVar.equals("")) {
                    if (GVTransactionInfo.isGVTransInfoField(currSourceVar)) {
                        outputHttpParam = getStringValue(input.getFieldAsString(currSourceVar), mappingData);
                    }
                    else {
                        outputHttpParam = getStringValue(currSourceVar, mappingData);
                    }

                    logger.debug("buildHttpParamError - source variable value [" + currSourceVar + "] is: "
                            + outputHttpParam);
                    break;
                }
            }

            logger.debug("buildHttpParamError - outputHttpParam error value set to: " + outputHttpParam);
            return outputHttpParam;
        }
        catch (Throwable exc) {
            logger.error("buildHttpParamError - Runtime error while setting HTTP param error value: " + exc);
            throw new HttpParamHandlerException("GVHTTP_RUNTIME_ERROR", new String[][]{
                    {"phase", "HTTP param error value setting"}, {"errorName", "" + exc}}, exc);
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
        String outputHttpParam = null;

        try {
            Iterator iter = paramMappings.keySet().iterator();
            while (iter.hasNext()) {
                String currSourceVar = (String) iter.next();
                MappingData mappingData = paramMappings.get(currSourceVar);

                if ((currSourceVar != null) && !currSourceVar.equals("")) {
                    if (GVTransactionInfo.isGVTransInfoField(currSourceVar)) {
                        outputHttpParam = getStringValue(input.getFieldAsString(currSourceVar), mappingData);
                    }
                    else {
                        outputHttpParam = getStringValue(currSourceVar, mappingData);
                    }

                    logger.debug("buildHttpParamACK - source variable value [" + currSourceVar + "] is: "
                            + outputHttpParam);
                    break;
                }
            }

            logger.debug("buildHttpParamACK - outputHttpParam ACK value set to: " + outputHttpParam);

            return outputHttpParam;
        }
        catch (Throwable exc) {
            logger.error("buildHttpParamACK - Runtime error while setting HTTP param ACK value: " + exc);
            throw new HttpParamHandlerException("GVHTTP_RUNTIME_ERROR", new String[][]{
                    {"phase", "HTTP param ACK value setting"}, {"errorName", "" + exc}}, exc);
        }
    }
}
