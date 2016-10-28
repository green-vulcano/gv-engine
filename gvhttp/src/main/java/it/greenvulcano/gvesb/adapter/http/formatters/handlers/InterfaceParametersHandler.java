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
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.utils.GVBufferAccess;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.HashMap;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This abstract class implements functionalities which are needed to parse HTTP
 * request parameters to <tt>GVBuffer</tt> objects and viceversa, acting as an
 * interface translator on inbound and outbound sides of GreenVulcano ESB.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
@SuppressWarnings("rawtypes")
public abstract class InterfaceParametersHandler
{
    private static Logger   logger             = org.slf4j.LoggerFactory.getLogger(InterfaceParametersHandler.class);

    /**
     * Constant indicating an handler whose output must be an
     * <code>GVBuffer</code> object.
     */
    public static final int GVBUFFER_HANDLER   = 0;

    /**
     * Constant indicating an handler whose output must be a string containing
     * the communication paradigm with GVJ (operation type).
     */
    public static final int OPTYPE_HANDLER     = 1;

    /**
     * Constant indicating an handler whose output must be the value of an HTTP
     * request (response) parameter.
     */
    public static final int HTTP_PARAM_HANDLER = 2;

    /**
     * This flag indicates if this is an <code>GVBuffer</code> handler or an
     * <code>OpType</code> Handler.
     */
    private int             handlerType;

    /**
     * This string indicates the character encoding to be used in conversions
     * from byte arrays to string and vice versa.
     */
    private String          charEncoding;
   
	protected HashMap       cache              = null;

    /**
     * Accessor method for handler type
     * 
     * @param type
     *        the type to be set (InterfaceParametersHandler.GVBUFFER_HANDLER,
     *        InterfaceParametersHandler.OPTYPE_HANDLER or
     *        InterfaceParametersHandler.HTTP_PARAM_HANDLER)
     */
    private void setHandlerType(int type)
    {
        handlerType = type;
    }

    /**
     * Accessor method for handler type
     * 
     * @return the handler type (InterfaceParametersHandler.GVBUFFER_HANDLER,
     *         InterfaceParametersHandler.OPTYPE_HANDLER or
     *         InterfaceParametersHandler.HTTP_PARAM_HANDLER)
     */
    public int getHandlerType()
    {
        return handlerType;
    }

    /**
     * Accessor setter method for character encoding
     * 
     * @param enc
     *        the character encoding to be set
     */
    public void setCharEncoding(String enc)
    {
        charEncoding = enc;
    }

    /**
     * Accessor getter method for character encoding
     * 
     * @return the current character encoding
     */
    public String getCharEncoding()
    {
        return charEncoding;
    }

    /**
     * Initialization method. This method reads its section of GVAdapters
     * configuration file to determine which kind of handler is it, then calls
     * the
     * appropriate initialization method (implemented by subclasses of this
     * class).
     * 
     * @param configNode
     *        The node, within the DOM encapsulating AdapterHTTP XML
     *        configuration file, which contains configuration info about this
     *        Handler.
     * @throws AdapterHttpConfigurationException
     *         if any error occurs during initialization.
     */
    public void init(Node configNode) throws AdapterHttpConfigurationException
    {
        try {
            String outputType = XMLConfig.get(configNode, "@OutputType");
            logger.debug("init - this is an " + outputType + " handler");
            if (outputType.equals("GVBuffer")) {
                setHandlerType(GVBUFFER_HANDLER);
            }
            else if (outputType.equals("OpType")) {
                setHandlerType(OPTYPE_HANDLER);
            }
            else {
                setHandlerType(HTTP_PARAM_HANDLER);
            }

            logger.debug("init - This handler is for a " + configNode.getNodeName());
            initSpecific(configNode);
        }
        catch (AdapterHttpConfigurationException ex) {
            logger.error("init - Error while configuring InterfaceParametersHandler object: " + ex);
            throw ex;
        }
        catch (Throwable ex) {
            logger.error("init - Generic error while initializing InterfaceParametersHandler object: " + ex);
            throw new AdapterHttpConfigurationException("GVHA_RUNTIME_ERROR", new String[][]{
                    {"phase", "InterfaceParametersHandler object initialization"}, {"errorName", "" + ex}}, ex);
        }
    }

    public void setCache(HashMap cache)
    {
        this.cache = cache;
    }

    public void resetCache()
    {
        cache = null;
    }

    /**
     * Parameter handler method. This method calls a more specific method
     * implemented by subclasses of this class.
     * 
     * @param input
     *        the input object (an <code>GVBuffer</code> object or a plain
     *        String)
     * @return an output object which can be an <code>GVBuffer</code> or a
     *         String
     * @throws HandlerException
     *         if any error occurs.
     */
    public Object build(Object input) throws AdapterHttpException
    {
        return build(input, null);
    }

    /**
     * Parameter handler method. This method calls a more specific method
     * implemented by subclasses of this class.
     * 
     * @param input
     *        the input object (an <code>GVBuffer</code> object or a plain
     *        String)
     * @param previous
     *        the output object from a previous handler.
     * @return an output object which can be an <code>GVBuffer</code> or a
     *         String.
     * @throws HandlerException
     *         if any error occurs.
     */
    public Object build(Object input, Object previous) throws AdapterHttpException
    {
        logger.debug("build start");
        Object output = null;
        try {
            switch (handlerType) {
                case GVBUFFER_HANDLER :
                    output = buildGVBuffer(input, (GVBuffer) previous);
                    break;
                case OPTYPE_HANDLER :
                    output = buildOpTypeString(input);
                    break;
                default :
                    if (input instanceof GVBuffer) {
                        output = buildHttpParam((GVBuffer) input, (String) previous);
                    }
                    else if (input instanceof GVTransactionInfo) {
                        GVTransactionInfo tInfo = (GVTransactionInfo) input;
                        if (tInfo.isError()) {
                            output = buildHttpParamError(tInfo, (String) previous);
                        }
                        else {
                            output = buildHttpParamACK(tInfo, (String) previous);
                        }
                    }
                    break;
            }
            logger.debug("build stop");
            return output;
        }
        catch (DataHandlerException exc) {
            logger.error("build - Error while generating GVBuffer object: " + exc);
            throw exc;
        }
        catch (OpTypeHandlerException exc) {
            logger.error("build - Error while generating operation type string: " + exc);
            throw exc;
        }
        catch (HttpParamHandlerException exc) {
            logger.error("build - Error while generating http parameter value: " + exc);
            throw exc;

        }
        catch (Throwable exc) {
            logger.error("build - Runtime error while handling input object: " + exc);
            throw new HandlerException("GVHA_RUNTIME_ERROR", new String[][]{{"phase", "input object parsing"},
                    {"errorName", "" + exc}}, exc);
        }
    }

    /**
     * Handler type-related initialization method. This method has to be
     * implemented by subclasses of this class.
     * 
     * @param specificParamHandlerSection
     *        the node, within the DOM encapsulating AdapterHTTP configuration
     *        file, containing specific initialization info about a particular
     *        handler type.
     * @throws AdapterHttpConfigurationException
     *         if any error occurs.
     */
    protected abstract void initSpecific(Node specificParamHandlerSection) throws AdapterHttpConfigurationException;

    /**
     * Parameter handler method which takes an HTTP param as input and generates
     * an <tt>GVBuffer</tt> object as output. This method has to be implemented
     * by subclasses of this class.
     * 
     * @param input
     *        the input HTTP parameter (as a <tt>String</tt>) to be used to
     *        populate an GVBuffer object.
     * @param previous
     *        the output GVBuffer object from a previous handler.
     * @return the resulting output GVBuffer object.
     * @throws DataHandlerException
     *         if any error occurs.
     */
    protected abstract GVBuffer buildGVBuffer(Object input, GVBuffer previous) throws DataHandlerException;


    /**
     * Parameter handler method which takes an HTTP param as input and generates
     * a string (<tt>OpType</tt>) indicating a valid GreenVulcano ESB
     * communication paradigm as output. This method has to be implemented by
     * subclasses of this class.
     * 
     * @param input
     *        the input HTTP parameter (as a <tt>String</tt>) from which the
     *        OpType parameter has to be parsed.
     * @return the resulting OpType parameter value (as a String).
     * @throws OpTypeHandlerException
     *         if any error occurs.
     */
    protected abstract String buildOpTypeString(Object input) throws OpTypeHandlerException;


    /**
     * Parameter handler method which takes an <tt>GVBuffer</tt> object as input
     * and generates an HTTP parameter value (a String) as output. This method
     * has to be implemented by subclasses of this class.
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
    protected abstract String buildHttpParam(GVBuffer input, String previous) throws HttpParamHandlerException;

    /**
     * Parameter handler method which takes an <tt>GVTransactionInfo</tt> object
     * as input and generates an HTTP parameter error value (a String) as
     * output. This method has to be implemented by subclasses of this class.
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
    protected abstract String buildHttpParamError(GVTransactionInfo input, String previous)
            throws HttpParamHandlerException;

    /**
     * Parameter handler method which takes an <tt>GVTransactionInfo</tt> object
     * as input and generates an HTTP parameter ACK value (a String) as output.
     * This method has to be implemented by subclasses of this class.
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
    protected abstract String buildHttpParamACK(GVTransactionInfo input, String previous)
            throws HttpParamHandlerException;

    /**
     * Returns <tt>true</tt> if <tt>variable</tt> indicates an <tt>GVBuffer</tt>
     * field.
     * 
     * @param variable
     *        the variable to be checked.
     * @return <tt>true</tt> if <tt>variable</tt> indicates an <tt>GVBuffer</tt>
     *         field.
     */
    protected boolean isGVBufferField(String variable)
    {
        return GVBufferAccess.isGVBufferField(variable);
    }

    /**
     * Returns <tt>true</tt> if <tt>variable</tt> indicates an <tt>GVBuffer</tt>
     * property.
     * 
     * @param variable
     *        the variable to be checked.
     * @return <tt>true</tt> if <tt>variable</tt> indicates an <tt>GVBuffer</tt>
     *         property.
     */
    protected boolean isGVBufferProperty(String variable)
    {
        return GVBufferAccess.isGVBufferProperty(variable);
    }

    /**
     * Returns <tt>true</tt> if <tt>variable</tt> indicates an
     * <tt>GVTransactionInfo</tt> field.
     * 
     * @param variable
     *        the variable to be checked.
     * @return <tt>true</tt> if <tt>variable</tt> indicates an
     *         <tt>GVTransactionInfo</tt> field.
     */
    protected boolean isGVTransInfoField(String variable)
    {
        return GVTransactionInfo.isGVTransInfoField(variable);
    }

    /**
     * Returns <tt>true</tt> if <tt>variable</tt> indicates the <tt>OpType</tt>
     * variable.
     * 
     * @param variable
     *        the variable to be checked.
     * @return <tt>true</tt> if <tt>variable</tt> indicates the <tt>OpType</tt>
     *         variable.
     */
    protected boolean isOpType(String variable)
    {
        return variable.equals(AdapterHttpConfig.MAPPING_VARIABLE_VALUE_OPTYPE);
    }

    /**
     * Returns the name of a configured <tt>GVBuffer</tt> (or
     * <tt>GVTransactionInfo</tt>) property.
     * 
     * @param variable
     *        the string from which the <tt>GVBuffer</tt> (or
     *        <tt>GVTransactionInfo</tt>) property name must be extracted.
     * @return the name of a configured <tt>GVBuffer</tt> (or
     *         <tt>GVTransactionInfo</tt>) property.
     */
    protected String getPropertyName(String variable)
    {
        return null;
    }

    /**
     * Trim the given string
     * 
     * @param input
     *        the string to trim
     * @param trimLocation
     *        the trim operation to apply
     * @return the trimmed string
     */
    protected String trim(String input, String trimLocation)
    {
        String output = input;
        if (trimLocation.equalsIgnoreCase("left")) {
            int index = 0;
            while (Character.isWhitespace(input.charAt(index))) {
                index++;
            }
            output = input.substring(index);
        }
        else if (trimLocation.equalsIgnoreCase("right")) {
            int index = input.length();
            while (Character.isWhitespace(input.charAt(index - 1))) {
                index--;
            }
            output = input.substring(0, index);
        }
        else if (trimLocation.equalsIgnoreCase("both")) {
            output = input.trim();
        }
        return output;
    }

    /**
     * @param input
     * @param key
     * @return the parsed <code>org.w3c.Document</code>
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	protected Document parseDOM(Object input, String key) throws Exception
    {
        Document doc = null;

        if (cache != null) {
            doc = (Document) cache.get(key);
            if (doc != null) {
                return doc;
            }
        }

        try {
            doc = (Document) XMLUtils.parseObject_S(input, false, true);

            if (cache != null) {
                cache.put(key, doc);
            }

            return doc;
        }
        catch (Exception exc) {
            throw exc;
        }
    }

    @SuppressWarnings("unchecked")
	protected void cacheDOM(Document doc, String key)
    {
        if (cache != null) {
            cache.put(key, doc);
        }
    }

    protected String getNodeContent(Document doc, String xpath) throws Exception
    {
        String xmlValue = XMLUtils.get_S(doc, xpath);
        return xmlValue;

    }

    protected String getStringValue(Object input, MappingData mappingData) throws Exception
    {
        String inputString = null;
        String output = null;
        String charEnc = getCharEncoding();
        
        if (input instanceof byte[]) {
            if (charEnc != null) {
                logger.debug("getStringValue - converting bytes to string using " + charEnc + " encoding");
                inputString = new String((byte[]) input, charEnc);
            }
            else {
                logger.debug("getStringValue - converting bytes to string using Java default encoding");
                inputString = new String((byte[]) input);
            }
        }
        else if (input instanceof Node) {
            if (charEnc != null) {
                logger.debug("getStringValue - serializing DOM to string using " + charEnc + " encoding");
                inputString = XMLUtils.serializeDOM_S((Node) input, charEnc);
            }
            else {
                logger.debug("getStringValue - converting bytes to string using UTF-8 encoding");
                inputString = XMLUtils.serializeDOM_S((Node) input);
            }
        }
        else if (input instanceof String) {
            inputString = (String) input;
        }

        int endIdx = (mappingData.length < Integer.MAX_VALUE)
                ? mappingData.offset + mappingData.length
                : inputString.length();
        if ((mappingData.offset > 0) || (endIdx != inputString.length())) {
            output = inputString.substring(mappingData.offset, endIdx);
        }
        else {
            output = inputString;
        }
        output = trim(output, mappingData.trim);
        return output;
    }

    protected byte[] getByteArrValue(byte[] input, MappingData mappingData) throws Exception
    {
        int size = (mappingData.length < Integer.MAX_VALUE) ? mappingData.length : (input.length - mappingData.offset);
        byte[] result = new byte[size];
        System.arraycopy(input, mappingData.offset, result, 0, size);
        return result;
    }
}
