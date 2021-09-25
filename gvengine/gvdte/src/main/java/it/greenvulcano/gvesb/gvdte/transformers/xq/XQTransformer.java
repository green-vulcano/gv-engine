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
package it.greenvulcano.gvesb.gvdte.transformers.xq;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.gvdte.config.DataSource;
import it.greenvulcano.gvesb.gvdte.config.DataSourceFactory;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransfException;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransformer;
import it.greenvulcano.gvesb.gvdte.util.TransformerHelper;
import it.greenvulcano.gvesb.gvdte.util.UtilsException;
import it.greenvulcano.gvesb.gvdte.util.xml.EntityResolver;
import it.greenvulcano.gvesb.gvdte.util.xml.ErrorHandler;
import it.greenvulcano.util.xml.ErrHandler;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import net.sf.saxon.xqj.SaxonXQDataSource;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is dedicated to XQuery transformations.
 *
 *
 * @version 3.2.0 17/10/2011
 * @author GreenVulcano Developer Team
 */
public class XQTransformer implements DTETransformer
{
    private static final Logger               logger            = org.slf4j.LoggerFactory.getLogger(XQTransformer.class);

    private String                            name;
    private String                            validationType;

    private String                            xqMapName         = null;

    private String                            outputType        = null;

    private Properties                        outProps          = new Properties();

    private boolean                           validateMap       = false;

    private String                            validateDirection = "in-out";

    private String                            dataSourceSet;

    private DataSourceFactory                 dsf;

    private XQDataSource                      xqDS              = null;
    private XQConnection                      xqConn            = null;

    private Map<String, XQPreparedExpression> templHashMap      = null;

    private List<TransformerHelper>           helpers           = new ArrayList<TransformerHelper>();

    public XQTransformer()
    {
        // do nothing
    }

    /**
     * Initialize the instance.
     *
     * @see DTETransformer#init(Node, DataSourceFactory)
     */
    @Override
    public void init(Node node, DataSourceFactory dsf) throws DTETransfException
    {
        logger.debug("Init start");
        try {
            this.dsf = dsf;
            name = XMLConfig.get(node, "@name", "NO_NAME");
            xqMapName = XMLConfig.get(node, "@XQMapName");
            outputType = XMLConfig.get(node, "@OutputType", "xml");
            dataSourceSet = XMLConfig.get(node, "@DataSourceSet", "Default");

            String validateXSL = XMLConfig.get(node, "@validate");
            String validateTransformations = XMLConfig.get(node, "../@validate");
            validateDirection = XMLConfig.get(node, "@validateDirection", "in-out");
            String lvalidationType = XMLConfig.get(node, "@validationType");
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
            logger.debug("init - loaded parameters: xqMapName = " + xqMapName + " - DataSourceSet: " + dataSourceSet
                    + " - outputType = " + outputType + " - validate = " + validateMap + " - validateDirection = "
                    + validateDirection);

            xqDS = new SaxonXQDataSource();
            xqConn = xqDS.getConnection();
            //xqConn.setErrorListener(new ErrorListener());

            initTemplMap();

            initHelpers(node);

            //if (outputType.equals("xml")) {
            initOutFormat(node);
            //}

            logger.debug("Init stop");
        }
        catch (DTETransfException exc) {
            throw exc;
        }
        catch (XMLConfigException exc) {
            logger.error("Error while accessing configuration", exc);
            throw new DTETransfException("GVDTE_CONFIGURATION_ERROR", new String[][]{{"cause",
                    "while accessing configuration"}}, exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, exc);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    private void initOutFormat(Node node)
    {
        outProps.setProperty("method", outputType);
        if (outputType.equals("xml")) {
            outProps.setProperty("indent", "no");
            outProps.setProperty("omit-xml-declaration", "no");
            outProps.setProperty("{http://saxon.sf.net/}indent-spaces", "4");
        }
    }

    /**
     * This method initialize the Map containing templates for certain
     * dataSource and xqMapName
     *
     * @return templHashMap the Map templates object
     * @throws DTETransfException
     */
    private Map<String, XQPreparedExpression> initTemplMap() throws DTETransfException
    {
        String key = dataSourceSet + "::" + xqMapName;
        try {
            templHashMap = new HashMap<String, XQPreparedExpression>();
            XQPreparedExpression expression = getExpression(dataSourceSet, xqMapName);
            templHashMap.put(key, expression);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, exc);
        }
        return templHashMap;
    }

    /**
     * This method get the XQPreparedExpression corresponding to the xqMapName.
     *
     * @param dss
     *        the DataSourceSet name
     * @param mn
     *        the MapName
     * @throws Exception
     */
    private XQPreparedExpression getExpression(String dss, String mn) throws Exception
    {
        int idx = mn.lastIndexOf("://");
        if (idx == -1) {
            mn = "gvdte://" + mn;
        }
        DataSource reposManager = dsf.getDataSource(dss, mn);
        return xqConn.prepareExpression(new ByteArrayInputStream(reposManager.getResourceAsByteArray(mn)));
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
     * The <code>input</code> parameter may be a Document, an XML string, a byte
     * array containing an XML, or an <tt>InputStream</tt> from which an
     * XML can be read. The <code>buffer</code> parameter is not used. The
     * return value is a Document representing the result of the transformation.
     *
     *
     * @param input
     *        the input data of the transformation.
     * @param buffer
     *        the intermediate result of the transformation (if needed).
     * @param mapParam
     * @return a DOM representing the result of the XSL transformation.
     * @throws DTETransfException
     *         if any transformation error occurs.
     */
    @Override
    public Object transform(Object input, Object buffer, Map<String, Object> mapParam) throws DTETransfException, 
            InterruptedException {
        logger.debug("Transform start");
        XQPreparedExpression expression = null;
        try {
            expression = getExpression(mapParam);
            setParams(expression, mapParam);
            Source theSource = convertInputFormat(input);
            expression.bindDocument(XQConstants.CONTEXT_ITEM, theSource, null);
            XQResultSequence result = expression.executeQuery();
            result.next();
            XQItem resultItem = result.getItem();

            if (outputType.equals("xml")) {
                DOMResult domRes = new DOMResult();
                resultItem.writeItemToResult(domRes);
                Document docValidation = (Document) domRes.getNode();
                if (validate() && (validateDirection.indexOf("out") != -1)) {
                    executeValidation(docValidation, mapParam);
                }
                logger.debug("Transform stop");
                return domRes.getNode();
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            StreamResult sr = new StreamResult(bos);
            resultItem.writeItemToResult(sr);
            byte[] byteResult = bos.toByteArray();

            logger.debug("Transform stop");
            return byteResult;
        }
        catch (DTETransfException exc) {
            throw exc;
        }
        catch (UtilsException exc) {
            logger.error("Error while converting input object", exc);
            throw new DTETransfException("GVDTE_CONVERSION_ERROR", new String[][]{{"msg", "converting input object"}},
                    exc);
        }
        catch (XQException exc) {
            logger.error("Error while performing XQuery transformation", exc);
            throw new DTETransfException("GVDTE_XQUERY_ERROR", new String[][]{{"cause",
                    "while performing XQuery transformation"}}, exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error"}}, exc);
        }
        finally {
            if (expression != null) {
                //expression.clearParameters();
            }
        }
    }

    /**
     * This method get or create the Transformer reading from HashMap
     *
     * @param hashMapParam
     *        The hashMap containing the dataSource and xqMapName
     * @return The XQPreparedExpression object
     * @throws DTETransfException
     */
    private XQPreparedExpression getExpression(Map<String, Object> mapParam) throws DTETransfException
    {
        XQPreparedExpression expression = null;
        String key = "";
        String dss = "";
        String mn = "";
        // HashMapParam has the datasourceset/xqmapname values
        // required by client
        //
        if (mapParam != null) {
            dss = (String) mapParam.get("datasourceset");
            mn = (String) mapParam.get("xqmapname");
        }
        if ((dss == null) || (dss.equals(""))) {
            dss = dataSourceSet;
        }
        if ((mn == null) || (mn.equals(""))) {
            mn = xqMapName;
        }
        key = dss + "::" + mn;
        try {
            expression = templHashMap.get(key);
            if (expression == null) {
                expression = getExpression(dss, mn);
                templHashMap.put(key, expression);
            }
            //expression.setErrorListener(new ErrorListener());
        }
        catch (Throwable exc) {
            logger.error("Error while creating XQuery transformer", exc);
            throw new DTETransfException("GVDTE_XQUERY_ERROR", new String[][]{{"cause",
                    "while creating XQuery transformer."}}, exc);
        }

        return expression;
    }

    /**
     * Execute the document validation
     *
     * @param docValidation
     *        The document to validate
     * @param mapParam
     *        The Map object
     * @throws DTETransfException
     */
    private void executeValidation(Document docValidation, Map<String, Object> mapParam) throws DTETransfException
    {
        XMLUtils xmlParser = null;
        try {
            xmlParser = XMLUtils.getParserInstance();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                    xmlParser.serializeDOMToByteArray(docValidation));
            logger.debug("Validate output with '" + getValidationType() + "'");
            String dss = "";
            if (mapParam != null) {
                dss = (String) mapParam.get("datasourceset");
            }
            if ((dss == null) || (dss.equals(""))) {
                dss = dataSourceSet;
            }
            XMLUtils.parseDOMValidating(getValidationType(), byteArrayInputStream, new EntityResolver(dss, dsf),
                    new ErrorHandler());
        }
        catch (XMLUtilsException exc) {
            logger.error("Error while performing XML validation", exc);
            throw new DTETransfException("GVDTE_XQUERY_ERROR", new String[][]{{"cause",
                    "while performing XML validation."}}, exc);
        }
        finally {
            XMLUtils.releaseParserInstance(xmlParser);
        }
    }

    /**
     * This method checks input object type: If it's an XML string, it parses it
     * into a Document; If it's a byte array, it parses it into a Document,
     * If it's an <tt>InputStream</tt>, it reads from it returning its content
     * as a Document.
     *
     * @param input
     *        the input data object for the transformation.
     * @return the input data object converted to a DOM.
     * @throws UtilsException
     *         if any error occurs while converting input object.
     */
    private Source convertInputFormat(Object input) throws UtilsException
    {
        Source inputSrc = null;
        boolean validateIn = validate() && (validateDirection.indexOf("in") != -1);
        try {
            if (input instanceof Node) {
                ByteArrayInputStream bis = null;
                if (validateIn) {
                    XMLUtils xmlParser = null;
                    try {
                        logger.debug("Validate input with '" + getValidationType() + "'");
                        xmlParser = XMLUtils.getParserInstance();
                        bis = new ByteArrayInputStream(xmlParser.serializeDOMToByteArray((Node) input));
                        XMLUtils.parseDOMValidating(getValidationType(), bis, new EntityResolver(dataSourceSet, dsf),
                                new ErrHandler());
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
                    if (bis != null) {
                        bis.reset();
                        inputSrc = new StreamSource(bis);
                    }
                    else {
                        inputSrc = new DOMSource(((Node) input).cloneNode(true));
                    }
                }
            }
            else if (input instanceof String) {
                if (validateIn) {
                    logger.debug("Validate input with '" + getValidationType() + "'");
                    String valStr = (String) input;
                    ByteArrayInputStream bis = new ByteArrayInputStream(valStr.getBytes());
                    XMLUtils.parseDOMValidating(getValidationType(), bis, new EntityResolver(dataSourceSet, dsf),
                            new ErrorHandler());
                }
                logger.debug("Input object is a String");
                inputSrc = new StreamSource(new StringReader((String) input));
            }
            else if (input instanceof byte[]) {
                ByteArrayInputStream valBis = new ByteArrayInputStream((byte[]) input);
                ByteArrayInputStream bis = valBis;
                if (validateIn) {
                    logger.debug("Validate input with '" + getValidationType() + "'");
                    bis = new ByteArrayInputStream((byte[]) input);
                    XMLUtils.parseDOMValidating(getValidationType(), valBis, new EntityResolver(dataSourceSet, dsf),
                            new ErrorHandler());
                }
                logger.debug("Input object is a byte array");
                inputSrc = new StreamSource(bis);
            }
            else if (input instanceof InputStream) {
                InputStream valIS = (InputStream) input;
                InputStream is = valIS;
                if (validateIn) {
                    logger.debug("Validate input with '" + getValidationType() + "'");
                    XMLUtils.parseDOMValidating(getValidationType(), valIS, new EntityResolver(dsf), new ErrorHandler());
                }
                logger.debug("Input object is an InputStream");
                inputSrc = new StreamSource(is);
            }
            else {
                throw new UtilsException("GVDTE_GENERIC_ERROR", new String[][]{{"msg",
                        "Invalid input type: " + input.getClass()}});
            }
            return inputSrc;
        }
        catch (XMLUtilsException exc) {
            logger.error("Error while performing XML validation", exc);
            throw new UtilsException("GVDTE_XQUERY_ERROR",
                    new String[][]{{"cause", "while performing XML validation."}}, exc);
        }
        catch (UtilsException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new UtilsException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, exc);
        }
    }

    /**
     * Set the parameters key:value pair as 'transformer' parameters
     *
     * @param expression
     *        the XQPreparedExpression on which set parameters
     * @param mapParam
     *        the key:value pair to use as transformation parameters
     * @throws XQException
     */
    private void setParams(XQPreparedExpression expression, Map<String, Object> mapParam) throws XQException
    {
        if (mapParam == null) {
            return;
        }
        String parameters = "XSL parameters:";
        try {
            for (String name : mapParam.keySet()) {
                Object value = mapParam.get(name);
                parameters += "\n" + name + "=" + value;
                expression.bindString(new QName(name), (value != null) ? value.toString() : "NULL", null);
            }
        }
        finally {
            logger.debug(parameters);
        }
    }

    /**
     * Get the xqMapName object
     *
     * @return xqMapName The XQuery Map name
     */
    @Override
    public String getMapName()
    {
        return xqMapName;
    }

    /**
     * Set the validation map attribute at true
     *
     * @param validate
     *        if true validation is ok
     */
    @Override
    public void setValidate(String validate)
    {
        if (validate.equals("true")) {
            validateMap = true;
        }
    }

    /**
     * Set the type of validation
     *
     * @param type
     *        the validation type
     */
    public void setValidationType(String type)
    {
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
    public String getValidationType()
    {
        return validationType;
    }

    /**
     * Return the validation of map
     *
     * @return validateMap the validate map value
     */
    @Override
    public boolean validate()
    {
        return validateMap;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#clean()
     */
    @Override
    public void clean()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#destroy()
     */
    @Override
    public void destroy()
    {
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
