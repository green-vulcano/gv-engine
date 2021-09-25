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
package it.greenvulcano.gvesb.gvdte.transformers.json;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.gvdte.DTEException;
import it.greenvulcano.gvesb.gvdte.config.DataSource;
import it.greenvulcano.gvesb.gvdte.config.DataSourceFactory;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransfException;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransformer;
import it.greenvulcano.gvesb.gvdte.util.TransformerHelper;
import it.greenvulcano.gvesb.gvdte.util.UtilsException;
import it.greenvulcano.gvesb.gvdte.util.xml.EntityResolver;
import it.greenvulcano.gvesb.gvdte.util.xml.ErrorHandler;
import it.greenvulcano.gvesb.gvdte.util.xml.URIResolver;

import it.greenvulcano.util.json.JSONUtils;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.ErrHandler;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is dedicated to XML/JSON transformations.
 *
 *
 * @version 3.3.4 Apr 15, 2014
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XML2JSONTransformer implements DTETransformer {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(XML2JSONTransformer.class);
    
    public enum ConversionPolicy {
        // simple conversion policy
        SIMPLE("simple"), 
        // BadgerFish conversion policy
        BADGERFISH("badgerfish"); 

        private String desc;

        private ConversionPolicy(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }

        public static ConversionPolicy fromString(String name) throws DTEException {
            if ("simple".equals(name)) {
                return SIMPLE;
            }
            if ("badgerfish".equals(name)) {
                return BADGERFISH;
            }
            throw new DTEException("Invalid conversion policy [" + name + "]");
        }
    }
    
    private String                 name;
    private String                 validationType;

    private String                 xslMapName = null;

    private boolean                validateMap  = false;

    private String                 dataSourceSet;

    private DataSourceFactory      dsf;
    
    private String                 transformerFactory;

    private Map<String, Templates> templHashMap = null;

    private boolean                 cacheTransformer = true;

    private Transformer             transformer = null;

    private final List<TransformerHelper> helpers = new ArrayList<TransformerHelper>();

    private ConversionPolicy        policy             	= ConversionPolicy.SIMPLE;
    private String 					forceElementsArray 	= "";
    private Set<String>             forceElementsArraySet = new HashSet<String>();
    private String             		forceStringValue    = "";
    private Set<String>             forceStringValueSet = new HashSet<String>();

    public XML2JSONTransformer() {
        // do nothing
    }

    /**
     * Initialize the instance.
     *
     * @throws DTETransfException
     */
    public void init(Node nodo, DataSourceFactory dsf) throws DTETransfException {
        logger.debug("Init start");
        try {
            this.dsf = dsf;

            name = XMLConfig.get(nodo, "@name", "NO_NAME");
            xslMapName = XMLConfig.get(nodo, "@InputXSLMapName");
            dataSourceSet = XMLConfig.get(nodo, "@DataSourceSet", "Default");
            cacheTransformer = XMLConfig.getBoolean(nodo, "@CacheTransformer", true);
            
            transformerFactory = XMLConfig.get(nodo, "@TransformerFactory", "");
            
            String validateXSL = XMLConfig.get(nodo, "@validate");
            String validateTransformations = XMLConfig.get(nodo, "../@validate");
            String lvalidationType = XMLConfig.get(nodo, "@validationType");
            setValidationType(lvalidationType);

            if (validateTransformations != null) {
                if (validateTransformations.equals("true")) {
                    setValidate("true");
                }
                else if (validateXSL != null) {
                    if (validateXSL.equals("true")) {
                        setValidate("true");
                    }
                }
            }
            else {
                if (validateXSL != null) {
                    if (validateXSL.equals("true")) {
                        setValidate("true");
                    }
                }
            }

            policy = ConversionPolicy.fromString(XMLConfig.get(nodo, "@ConversionPolicy", ConversionPolicy.SIMPLE.toString()));
            if (policy == ConversionPolicy.SIMPLE) {
            	forceElementsArray = XMLConfig.get(nodo, "@ForceElementsArray", "");
            	forceElementsArraySet = listToSet(forceElementsArray);
                forceStringValue = XMLConfig.get(nodo, "@ForceStringValue", "");
                forceStringValueSet = listToSet(forceStringValue);
            }

            logger.debug("Loaded parameters: inputXslMapName = " + xslMapName + " - DataSourceSet: "
                    + dataSourceSet + " - validate = " + validateXSL + " - transformerFactory = " + transformerFactory
                    + " - conversionPolicy = " + policy
                    + " - forceElementsArray = " + forceElementsArray
                    + " - forceStringValue = " + forceStringValue);

            initTemplMap();

            initHelpers(nodo);

            logger.debug("Init stop");
        }
        catch (DTETransfException exc) {
            throw exc;
        }
        catch (XMLConfigException exc) {
            logger.error("Error while accessing configuration", exc);
            throw new DTETransfException("GVDTE_CONFIGURATION_ERROR", new String[][] { { "cause",
                    "while accessing configuration" } }, exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][] { { "msg", "Unexpected error." } }, exc);
        }
    }


    @Override
    public String getName() {
        return name;
    }

	private Set<String> listToSet(String list) {
		Set<String> set = new HashSet<String>();
		for (String el : list.split(",")) {
		    set.add(el.trim());
		}
		return set;
	}

    /**
     * This method initialize the Map containing templates for certain
     * dataSource and xslMapName
     *
     * @return templHashMap the Map templates object
     * @throws DTETransfException
     */
    private Map<String, Templates> initTemplMap() throws DTETransfException {
        if (xslMapName == null) {
            return null;
        }
        String key = dataSourceSet + "::" + xslMapName;
        try {
            templHashMap = new HashMap<String, Templates>();
            Templates templates = getTemplate(dataSourceSet, xslMapName);
            templHashMap.put(key, templates);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][] { { "msg", "Unexpected error." } }, exc);
        }
        return templHashMap;
    }

    /**
     * This method get the Template corresponding at the xslMapName.
     *
     * @param dss
     *            the DataSourceSet name
     * @param mn
     *            the MapName
     * @throws Exception
     */
    private Templates getTemplate(String dss, String mn) throws Exception {
        int idx = mn.lastIndexOf("://");
        if (idx == -1) {
            mn = "gvdte://" + mn;
        }
        DataSource reposManager = dsf.getDataSource(dss, mn);
        TransformerFactory tFactory = null;
        if (transformerFactory.equals("")) {
            tFactory = TransformerFactory.newInstance();
        }
        else {
            tFactory = TransformerFactory.newInstance(transformerFactory, null);
        }
        tFactory.setURIResolver(new URIResolver(dss, dsf));
        Source source = new StreamSource(new ByteArrayInputStream(reposManager.getResourceAsByteArray(mn)));
        source.setSystemId(reposManager.getResourceURL(mn));
        return tFactory.newTemplates(source);
    }

    /**
     * The <code>input</code> parameter may be a Document, an XML string, a byte
     * array containing an XML, or an <tt>InputStream</tt> from which an
     * XML can be read. The <code>buffer</code> parameter is not used.
     * The return value is a String containing the JSON representing
     * the result of the transformation.
     *
     * @param input
     *            the input data of the transformation.
     * @param buffer
     *            the intermediate result of the transformation (if needed).
     * @return a String representing the result of the JSON transformation.
     * @throws DTETransfException
     *             if any transformation error occurs.
     */
    public Object transform(Object input, Object buffer, Map<String, Object> mapParam) throws DTETransfException, 
            InterruptedException {
        logger.debug("Transform start");
        Transformer localTransformer = null;
        try {
            Node docXML = null;
            if (xslMapName != null) {
            	if (cacheTransformer) {
                    if (transformer == null) {
                    	transformer = getTransformer(mapParam);
                    }
                    localTransformer = transformer;
                }
                else {
                    localTransformer = getTransformer(mapParam);
                }
                setParams(localTransformer, mapParam);
                Source theSource = convertInputFormat(input);
                String outputType = localTransformer.getOutputProperty(OutputKeys.METHOD);
                if (outputType == null) {
                    outputType = "xml";
                }
                DOMResult theDOMResult = new DOMResult();
                localTransformer.transform(theSource, theDOMResult);
                docXML = theDOMResult.getNode();
            }
            else {
                docXML = XMLUtils.parseObject_S(input, false, true);
            }
            JSONObject json = null;
            if (policy == ConversionPolicy.SIMPLE) {
            	Set<String> currForceElementsArraySet = forceElementsArraySet;
            	if (!PropertiesHandler.isExpanded(forceElementsArray)) {
            		currForceElementsArraySet = listToSet(PropertiesHandler.expand(forceElementsArray, mapParam));
            	}
            	Set<String> currForceStringValueSet = forceStringValueSet;
            	if (!PropertiesHandler.isExpanded(forceStringValue)) {
            		currForceStringValueSet = listToSet(PropertiesHandler.expand(forceStringValue, mapParam));
            	}
            	
                json = JSONUtils.xmlToJson(docXML, currForceElementsArraySet, currForceStringValueSet);
            }
            else {
                json = JSONUtils.xmlToJson_BadgerFish(docXML);
            }
            logger.debug("Transform stop");
            //return json.toString(2);
            return json.toString();
        }
        catch (DTETransfException exc) {
            throw exc;
        }
        catch (UtilsException exc) {
            logger.error("Error while converting input object", exc);
            throw new DTETransfException("GVDTE_CONVERSION_ERROR",
                    new String[][] { { "msg", "converting input object" } }, exc);
        }
        catch (TransformerException exc) {
            logger.error("Error while performing XSL transformation", exc);
            throw new DTETransfException("GVDTE_XSLT_ERROR", new String[][] { { "cause",
                    "while performing XSL transformation." } }, exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][] { { "msg", "Unexpected error." } }, exc);
        }
        finally {
            if (localTransformer != null) {
            	localTransformer.clearParameters();
            }
        }
    }

    /**
     * This method return or create the Transformer.
     *
     * @param hashMapParam
     *            The hashMap containing the dataSource and xslMapName client
     * @return transformer The Transformer object
     * @throws DTETransfException
     */
    private Transformer getTransformer(Map<String, Object> mapParam) throws DTETransfException {
        Transformer transformer = null;
        String key = "";
        String dss = "";
        String mn = "";
        // HashMapParam has the datasourceset/xslmapname values
        // provided by client
        //
        if (mapParam != null) {
            dss = (String) mapParam.get("datasourceset");
            mn = (String) mapParam.get("xslmapname");
        }
        if ((dss == null) || (dss.equals(""))) {
            dss = dataSourceSet;
        }
        if ((mn == null) || (mn.equals(""))) {
            mn = xslMapName;
        }
        key = dss + "::" + mn;
        try {
            Templates templates = templHashMap.get(key);

            if (templates == null) {
                templates = getTemplate(dss, mn);
                templHashMap.put(key, templates);
            }
            transformer = templates.newTransformer();
        }
        catch (Throwable exc) {
            logger.error("Error while creating XSL transformer", exc);
            throw new DTETransfException("GVDTE_XSLT_ERROR", new String[][] { { "cause",
                    "while creating XSL transformer." } }, exc);
        }

        return transformer;
    }

    /**
     * This method checks input object type: If it's an XML string, it parses it
     * into a Document; If it's a byte array, it converts it to a string and parses
     * it into a Document, If it's an <tt>InputStream</tt>, it reads from it
     * returning its content as a Document.
     *
     * @param input
     *            the input data object for the transformation.
     * @return the input data object converted to a DOM.
     * @throws UtilsException
     *             if any error occurs while converting input object.
     */
    private Source convertInputFormat(Object input) throws UtilsException {
        Source inputSrc = null;
        try {
            if (input instanceof Node) {
                ByteArrayInputStream byteArrayInputStream = null;
                if (validate()) {
                    XMLUtils xmlParser = null;
                    try {
                        logger.debug("Validate input with '" + getValidationType() + "'");
                        xmlParser = XMLUtils.getParserInstance();
                        byteArrayInputStream = new ByteArrayInputStream(xmlParser.serializeDOMToByteArray((Node) input));
                        XMLUtils.parseDOMValidating(getValidationType(), byteArrayInputStream, new EntityResolver(
                                dataSourceSet, dsf), new ErrHandler());
                    }
                    finally {
                        XMLUtils.releaseParserInstance(xmlParser);
                    }
                }
                if (input instanceof Document) {
                    logger.debug("Input object is a Document");
                    inputSrc = new DOMSource((Document) input);
                }
                else {
                    logger.debug("Input object is a Node");
                    if (byteArrayInputStream != null) {
                        byteArrayInputStream.reset();
                        inputSrc = new StreamSource(byteArrayInputStream);
                    }
                    else {
                        inputSrc = new DOMSource(((Node) input).cloneNode(true));
                    }
                }
            }
            else if (input instanceof String) {
                if (validate()) {
                    logger.debug("Validate input with '" + getValidationType() + "'");
                    String validateString = (String) input;
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(validateString.getBytes());
                    XMLUtils.parseDOMValidating(getValidationType(), byteArrayInputStream, new EntityResolver(
                            dataSourceSet, dsf), new ErrorHandler());
                }
                logger.debug("Input object is a String");
                inputSrc = new StreamSource(new StringReader((String) input));
            }
            else if (input instanceof byte[]) {
                ByteArrayInputStream validateByteArrayInputStream = new ByteArrayInputStream((byte[]) input);
                ByteArrayInputStream byteArrayInputStream = validateByteArrayInputStream;
                if (validate()) {
                    logger.debug("Validate input with '" + getValidationType() + "'");
                    byteArrayInputStream = new ByteArrayInputStream((byte[]) input);
                    XMLUtils.parseDOMValidating(getValidationType(), validateByteArrayInputStream, new EntityResolver(
                            dataSourceSet, dsf), new ErrorHandler());
                }
                logger.debug("Input object is a byte array");
                inputSrc = new StreamSource(byteArrayInputStream);
            }
            else if (input instanceof InputStream) {
                InputStream validateInputStream = (InputStream) input;
                InputStream inputStream = validateInputStream;
                if (validate()) {
                    logger.debug("Validate input with '" + getValidationType() + "'");
                    XMLUtils.parseDOMValidating(getValidationType(), validateInputStream, new EntityResolver(dsf),
                            new ErrorHandler());
                }
                logger.debug("Input object is an InputStream");
                inputSrc = new StreamSource(inputStream);
            }
            else {
                throw new UtilsException("GVDTE_GENERIC_ERROR", new String[][] { { "msg", "Invalid input type: " + input.getClass() } });
            }
            return inputSrc;
        }
        catch (XMLUtilsException exc) {
            logger.error("Error while performing XML validation", exc);
            throw new UtilsException("GVDTE_XSLT_ERROR",
                    new String[][] { { "cause", "while performing XML validation." } }, exc);
        }
        catch (UtilsException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new UtilsException("GVDTE_GENERIC_ERROR", new String[][] { { "msg", "Unexpected error." } }, exc);
        }
    }


    /**
     * Set the 'params' key:value pair as 'transformer' params
     *
     * @param transformer
     *            the transformer on wich set params
     * @param mapParam
     *            the key:value pair to use as transformation params
     */
    private void setParams(Transformer transformer, Map<String, Object> mapParam) {
        if (mapParam == null) {
            return;
        }
        String parameters = "XSL parameters:";
        try {
            for (String name : mapParam.keySet()) {
                Object value = mapParam.get(name);
                parameters += "\n" + name + "=" + value;
                transformer.setParameter(name, (value != null) ? value.toString() : "NULL");
            }
        }
        finally {
            logger.debug(parameters);
        }
    }

    /**
     * @param node
     */
    private void initHelpers(Node node) throws Exception
    {
        NodeList nl = XMLConfig.getNodeList(node, "*[@type='transformer-helper']");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            TransformerHelper helper = (TransformerHelper) Class.forName(XMLConfig.get(n, "@class")).newInstance();
            helper.init(n);
            helpers.add(helper);
        }
    }

    /**
     * Get the xslMapName object
     *
     * @return xslMapName The xslMap name
     */
    public String getMapName() {
        return xslMapName;
    }

    /**
     * Set the validation map attribute at true
     *
     * @param validate
     *            if true validation is ok
     */
    public void setValidate(String validate) {
        if (validate.equals("true")) {
            validateMap = true;
        }
    }

    /**
     * Set the type of validation
     *
     * @param type
     *            the validation type
     */
    public void setValidationType(String type) {
        if (type != null) {
            validationType = type;
        }
        else {
            validationType = "xsd";
        }
    }

    /**
     * Get the validation type
     *
     * @return validationType the validation type
     */
    public String getValidationType() {
        return validationType;
    }

    /**
     * Return the validation of map
     *
     * @return validateMap the validate map value
     */
    public boolean validate() {
        return validateMap;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#clean()
     */
    public void clean()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#destroy()
     */
    public void destroy() {
        templHashMap.clear();
        dsf = null;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#getHelpers()
     */
    @Override
    public List<TransformerHelper> getHelpers()
    {
        return helpers;
    }
}
