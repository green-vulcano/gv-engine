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
package it.greenvulcano.gvesb.gvdte.transformers.bin.xml2bin;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.gvdte.config.ConfigException;
import it.greenvulcano.gvesb.gvdte.config.DataSourceFactory;
import it.greenvulcano.gvesb.gvdte.config.IConfigLoader;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransfException;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransformer;
import it.greenvulcano.gvesb.gvdte.transformers.bin.converters.ConversionException;
import it.greenvulcano.gvesb.gvdte.transformers.bin.utils.Field;
import it.greenvulcano.gvesb.gvdte.util.TransformerHelper;
import it.greenvulcano.gvesb.gvdte.util.UtilsException;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class handle data transformations from XML to binary buffer.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XMLToBinaryTransformer implements DTETransformer
{
    private static Logger            logger            = org.slf4j.LoggerFactory.getLogger(XMLToBinaryTransformer.class);

    private String                   name;
    /**
     * The output buffer, which is allocated once and re-used at every
     * XML-To-Binary transformation request.
     */
    private byte[]                   outputBuffer;

    /**
     * The size o fteh buffer.
     */
    private int                      size              = 0;
    private int                      lastSize          = -1;

    private String                   conversionMapName = "";
    private String                   dataSourceSet     = "";

    private Map<String, List<Field>> fieldListMap      = null;
    private Map<String, Integer>     sizeMap           = null;

    private DataSourceFactory        dataSourceF       = null;

    private List<TransformerHelper> helpers = new ArrayList<TransformerHelper>();

    public XMLToBinaryTransformer()
    {
        // do nothing
    }

    /**
     * Initialize the instance.
     *
     * @param node
     * @param dsf
     *
     * @throws DTETransfException
     *         if configuration parameters or conversion properties can't be
     *         accessed for any reason.
     */
    public void init(Node node, DataSourceFactory dsf) throws DTETransfException
    {
        dataSourceF = dsf;
        logger.debug("Init start");
        try {
            name = XMLConfig.get(node, "@name", "NO_NAME");
            conversionMapName = XMLConfig.get(node, "@ConversionMapName");
            dataSourceSet = XMLConfig.get(node, "@DataSourceSet", "Default");
            logger.debug("Loaded parameters: conversionMapName = " + conversionMapName + " - DataSourceSet: "
                    + dataSourceSet);
            initFieldListMap();
            logger.debug("OutputBuffer size: " + outputBuffer.length);
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
            throw new DTETransfException("GVDTEXDT_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, exc);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * This method initialize the FileList.
     *
     * @throws DTETransfException
     *         if error occurs
     */
    private void initFieldListMap() throws DTETransfException
    {
        try {
            fieldListMap = new HashMap<String, List<Field>>();
            sizeMap = new HashMap<String, Integer>();
            getFieldList(null);
        }
        catch (DTETransfException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, exc);
        }
    }

    /**
     * The <code>input</code> parameter may be a Document, an XML string, a byte
     * array containing an XML, or an <tt>InputStream</tt> from which an
     * XML can be read. The <code>buffer</code> parameter, if it's not <code>null</code>,
     * is a byte array on which write the result of the transformation. The return
     * value is a byte array representing the result of the transformation.
     *
     * @param input
     *        the input data of the transformation.
     * @param buffer
     *        the intermediate result of the transformation (if needed).
     * @param mapParam
     * @return a byte array representing the result of the data transformation.
     * @throws DTETransfException
     *         if any transformation error occurs.
     */
    public Object transform(Object input, Object buffer, Map<String, Object> mapParam) throws DTETransfException, 
            InterruptedException {
        logger.debug("Transform start");
        try {
            Document inDocument = convertInputFormat(input);
            List<Field> fieldList = getFieldList(mapParam);

            for (Field field : fieldList) {
                field.convertToBin(outputBuffer, inDocument);
            }

            logger.debug("Transform stop");
            return outputBuffer;
        }
        catch (DTETransfException exc) {
            throw exc;
        }
        catch (UtilsException exc) {
            logger.error("Error while converting input format to DOM", exc);
            throw new DTETransfException("GVDTE_CONVERSION_ERROR", new String[][]{{"msg",
                    "converting input format to DOM"}}, exc);
        }
        catch (ConversionException exc) {
            logger.error("Error while converting input DOM nodes to binary buffer fields", exc);
            throw new DTETransfException("GVDTE_CONVERSION_ERROR", new String[][]{{"msg",
                    "Error while converting input DOM nodes to binary buffer fields"}}, exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, exc);
        }
    }

    /**
     * This method returns or create the FieldList.
     *
     * @param mapParam
     *        The hashMap containing the dataSource and xslMapName client
     * @return transformer The Transformer object
     * @throws DTETransfException
     */
    private List<Field> getFieldList(Map<String, Object> mapParam) throws DTETransfException
    {
        List<Field> fieldL = null;
        String key = "";
        String dss = "";
        String mn = "";
        // mapParam has the datasourceset/mapname values
        // provided by client...
        // overwrite configuration parameters
        //
        if (mapParam != null) {
            logger.debug("getFieldList - reading data from hashMapParam");
            dss = (String) mapParam.get("datasourceset");
            mn = (String) mapParam.get("x2bmapname");
        }
        if ((dss == null) || (dss.equals(""))) {
            dss = dataSourceSet;
        }
        if ((mn == null) || (mn.equals(""))) {
            mn = conversionMapName;
        }

        key = dss + "::" + mn;

        logger.debug("getFieldList - using key: " + key);

        try {
            fieldL = fieldListMap.get(key);

            if (fieldL == null) {
                IConfigLoader cfgLdr = dataSourceF.getConfigLoader(dss, mn);
                fieldL = parseConversionMap(cfgLdr);
                fieldListMap.put(key, fieldL);
                sizeMap.put(key, new Integer(size));
            }
            else {
                size = (sizeMap.get(key)).intValue();
            }

            if (lastSize != size) {
                outputBuffer = allocateOutputBuffer(size);
                lastSize = size;
            }
        }
        catch (UtilsException exc) {
            logger.error("Error while instantiating or using DataSource object", exc);
            throw new DTETransfException("GVDTE_CONFIGURATION_ERROR", new String[][]{{"cause",
                    "while instantiating or using DataSource object"}}, exc);
        }
        catch (Throwable exc) {
            logger.error("Error while performing Bin2XML transformation", exc);
            throw new DTETransfException("GVDTE_XSLT_ERROR", new String[][]{{"cause",
                    "while performing Bin2XML transformation."}}, exc);
        }

        return fieldL;
    }

    /**
     * This method parses the XML conversion map and uses it's informations
     * to populate the List of <tt>Field</tt> objects.
     *
     * @param map
     *        an XML string containing the XML conversion map.
     *
     * @throws UtilsException
     *         if a parsing error or a <tt>Conversion</tt> initialization error
     *         occurs.
     */
    private List<Field> parseConversionMap(IConfigLoader cfgLdr) throws ConfigException
    {
        Field result = null;
        List<Field> fieldList = new ArrayList<Field>();

        logger.debug("Parse ConversionMap start");
        try {
            String sSize = (String) cfgLdr.getData("/BinaryBuffer/@Size");
            size = Integer.parseInt(sSize.trim());
            String[] fieldsElemList = cfgLdr.getSectionList("/BinaryBuffer/Field");
            if (fieldsElemList.length > 0) {
                for (String element : fieldsElemList) {
                    result = new Field();
                    result.init(element, cfgLdr);
                    fieldList.add(result);
                }
            }
            logger.debug("Parse ConversionMap stop");
        }
        catch (ConversionException exc) {
            logger.error("Error while initializing Conversion object", exc);
            throw new ConfigException("GVDTE_INITIALIZATION_ERROR", new String[][]{{"class", " Conversion object"},
                    {"key", ""}}, exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new ConfigException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, exc);
        }

        return fieldList;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#getMapName()
     */
    public String getMapName()
    {
        return conversionMapName;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#setValidate(java.lang.String)
     */
    public void setValidate(String validate)
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#validate()
     */
    public boolean validate()
    {
        return false;
    }

    /**
     * This method checks input object type: If it's an XML string, it parses it
     * into a Document; If it's a byte array, it converts it to a string and parses
     * it into a Document, If it's an <tt>InputStream</tt>, it reads from it
     * returning its content as a Document.
     *
     * @param input
     *        the input data object for the transformation.
     * @return the input data object converted to a Document.
     * @throws UtilsException
     *         if any error occurs while converting input object.
     */
    private Document convertInputFormat(Object input) throws UtilsException
    {
        Document inputDoc = null;

        try {
            if (input instanceof Document) {
                logger.debug("Input object is a Document");
                inputDoc = (Document) input;
            }
            else if (input instanceof String) {
                logger.debug("Input object is a String");
                inputDoc = XMLUtils.parseDOM_S((String) input, false, false);
            }
            else if (input instanceof byte[]) {
                logger.debug("Input object is a byte array");
                inputDoc = XMLUtils.parseDOM_S((byte[]) input, false, false);
            }
            else if (input instanceof InputStream) {
                logger.debug("Input object is an InputStream");
                inputDoc = XMLUtils.parseDOM_S((InputStream) input, false, false);
            }
            return inputDoc;
        }
        catch (XMLUtilsException exc) {
            logger.error("Error while converting input format", exc);
            throw new UtilsException("GVDTE_CONVERSION_ERROR", new String[][]{{"msg", "converting input format"}}, exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new UtilsException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, exc);
        }
    }

    /**
     * This method allocates a byte array of the right size to hold the
     * bytes obtained from conversion of the data read from the input DOM. The
     * size of the array is determined based on the declared size of each binary
     * field in the conversion map.
     *
     * @return an empty byte array that will host the results of each
     *         transformation performed by this transformer.
     * @throws DTETransfException
     *         if a runtime error occurs.
     */
    private byte[] allocateOutputBuffer(int bufferSize) throws DTETransfException
    {
        try {
            return new byte[bufferSize];
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, exc);
        }
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
    public void destroy()
    {
        fieldListMap.clear();
        sizeMap.clear();
        dataSourceF = null;
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
