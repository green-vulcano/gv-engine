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
package it.greenvulcano.gvesb.gvdte.transformers.bin.bin2xml;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class handle data transformations from binary buffer to XML.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class BinaryToXMLTransformer implements DTETransformer
{
    private static final Logger      logger            = org.slf4j.LoggerFactory.getLogger(BinaryToXMLTransformer.class);

    private String                   name;
    private String                   conversionMapName = "";
    private String                   dataSourceSet;
    private List<TransformerHelper>  helpers           = new ArrayList<TransformerHelper>();
    /**
     * List of <tt>Field</tt> objects describing the fields of the
     * input binary buffer
     */
    private Map<String, List<Field>> fieldListMap      = null;
    private DataSourceFactory        dataSourceF       = null;

    public BinaryToXMLTransformer()
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
        logger.debug("Init start");
        dataSourceF = dsf;
        try {
            name = XMLConfig.get(node, "@name", "NO_NAME");
            conversionMapName = XMLConfig.get(node, "@ConversionMapName");
            dataSourceSet = XMLConfig.get(node, "@DataSourceSet", "Default");
            logger.debug("Loaded parameters: conversionMapName = " + conversionMapName + " - DataSourceSet: "
                    + dataSourceSet);
            initFieldListMap();
            logger.debug("Init stop");
        }
        catch (DTETransfException exc) {
            throw exc;
        }
        catch (XMLConfigException exc) {
            logger.error("Error while accessing configuration", exc);
            throw new DTETransfException("GVDTE_XML_CONFIG_ERROR", exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", " Unexpected error."}}, exc);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Initialize the HashMap containing templates for certain
     * dataSource and xslMapName
     *
     * @throws DTETransfException
     *         if error occurs
     */
    private void initFieldListMap() throws DTETransfException
    {
        try {
            fieldListMap = new HashMap<String, List<Field>>();
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
     * The <code>input</code> parameter is a byte array. The return value is a Document
     * representing the result of the transformation.
     *
     *
     * @param input
     *        the input data of the transformation.
     * @param buffer
     *        the intermediate result of the transformation (if needed).
     * @param mapParam
     * @return a Document representing the result of the data transformation.
     * @throws DTETransfException
     *         if any transformation error occurs.
     */
    public Object transform(Object input, Object buffer, Map<String, Object> mapParam) throws DTETransfException, 
            InterruptedException {
        logger.debug("Transform start");
        try {
            byte[] inputBuffer = (byte[]) input;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);

            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document outputDOM = docBuilder.newDocument();

            List<Field> fieldList = getFieldList(mapParam);
            for (int i = 0; i < fieldList.size(); i++) {
                Field field = fieldList.get(i);
                field.convertToDOM(inputBuffer, outputDOM);
            }
            logger.debug("Transform stop");
            return outputDOM;
        }
        catch (DTETransfException exc) {
            throw exc;
        }
        catch (ClassCastException exc) {
            logger.error("Input object is not a binary buffer", exc);
            throw new DTETransfException("GVDTE_CAST_ERROR", exc);
        }
        catch (ConversionException exc) {
            logger.error("Error while converting input buffer fields to DOM nodes", exc);
            throw new DTETransfException("GVDTE_CONVERSION_ERROR", new String[][]{{"msg",
                    " converting input buffer fields to DOM nodes."}}, exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error: ", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", " Unexpected error."}}, exc);
        }
    }

    /**
     * This method returns or create the FieldList.
     *
     * @param hashMapParam
     *        contains the dataSource and b2xmapname provided by the client
     * @return the FieldLis
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
            logger.debug("reading data from mapParam");
            dss = (String) mapParam.get("datasourceset");
            mn = (String) mapParam.get("b2xmapname");
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
            }
        }
        catch (UtilsException exc) {
            logger.error("Error while instantiating or using DataSource", exc);
            throw new DTETransfException("GVDTE_INITIALIZATION_ERROR", new String[][]{{"class", "DataSource"},
                    {"key", ""}}, exc);
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
     * @param cfgLdr
     *        configuration loader for the conversion map.
     * @throws UtilsException
     *         if a parsing error or a <tt>Conversion</tt> initialization error
     *         occurs.
     */
    private List<Field> parseConversionMap(IConfigLoader cfgLdr) throws ConfigException
    {
        Field result = null;
        List<Field> fieldL = new ArrayList<Field>();
        logger.debug("Parse ConversionMap start");
        try {
            String[] fieldsElemList = cfgLdr.getSectionList("/BinaryBuffer/Field");
            if (fieldsElemList.length > 0) {
                for (String field : fieldsElemList) {
                    result = new Field();
                    result.init(field, cfgLdr);
                    fieldL.add(result);
                }
            }
            logger.debug("Parse ConversionMap stop");
        }
        catch (Exception exc) {
            logger.error("Unexpected error", exc);
            throw new ConfigException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", " Unexpected error."}}, exc);
        }

        return fieldL;
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
        // do nothing
        return false;
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
