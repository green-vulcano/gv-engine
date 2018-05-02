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
package it.greenvulcano.gvesb.gvdte.transformers.bin.utils;

import it.greenvulcano.gvesb.gvdte.config.ConfigException;
import it.greenvulcano.gvesb.gvdte.config.IConfigLoader;
import it.greenvulcano.gvesb.gvdte.transformers.bin.converters.Conversion;
import it.greenvulcano.gvesb.gvdte.transformers.bin.converters.ConversionException;
import it.greenvulcano.gvesb.gvdte.util.UtilsException;
import it.greenvulcano.util.xpath.XPathDOMBuilder;
import it.greenvulcano.util.xpath.XPathLocator;
import it.greenvulcano.util.xpath.XPathUtilsException;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class encapsulates informations (read from the XML conversion map) which
 * describe a particular field (single or aggregate) of a corresponding binary
 * buffer.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class Field
{

    /**
     *
     */
    public static final int     TYPE_SINGLE_FIELD    = 1;
    /**
     *
     */
    public static final int     TYPE_SEQUENCE_FIELD  = 2;

    private final static String XPATH_SEPARATOR_CHAR = "/";
    private final static String XPATH_ATTRIBUTE_CHAR = "@";

    /**
     * Ordered <tt>List</tt> of <tt>Conversion</tt> objects which represents the
     * list of conversions (configured by the XML conversion map) to be
     * performed on the sub-fields of the binary field mapped to this
     * <tt>Field</tt> object. if this binary field is a single field (opposite
     * to an aggregate field) only one conversion is performed on it (the
     * <tt>convList</tt> object contains only one <tt>Conversion</tt> object).
     */
    private List<Conversion>    convList             = null;

    /**
     * XPathDOMBuilder object used during binary->XML trasformations to convert
     * an XPath (which specifies the position inside XML output document on
     * which the value of a field of the input binary buffer has to be mapped)
     * in the corresponding DOM branch.
     */
    private XPathDOMBuilder     domBuilder;

    private int                 fieldOffset          = 0;
    private int                 fieldLength          = 0;
    // private byte fieldType = -1;
    private String              fieldXPath           = "";
    private int                 xPathElemType        = -1;

    /**
     * Log4j object used to write the logs
     */
    private static Logger       logger               = org.slf4j.LoggerFactory.getLogger(Field.class);

    /**
     * Public constructor.
     */
    public Field()
    {
        convList = new ArrayList<Conversion>();
        domBuilder = new XPathDOMBuilder();
    }

    /**
     * Initializes this <tt>Field</tt> object by loading from the corresponding
     * section of the binary buffer configuration map all needed parameters:
     * field's max length, field offset, type etc.
     *
     * @param base
     * @param cfg
     *
     * @throws ConversionException
     *         if any XML parsing or <tt>Conversion</tt> object instantiation
     *         occurs.
     */

    public void init(String base, IConfigLoader cfg) throws ConversionException
    {
        String fldLen = "";
        String fldOff = "";
        String xPathElementType = "";

        try {
            fldLen = (String) cfg.getData(base + "/@FieldLength");
            fldOff = (String) cfg.getData(base + "/@FieldOffset");
            fieldXPath = (String) cfg.getData(base + "/@XPath");
            xPathElementType = (String) cfg.getData(base + "/@ElementType");
        }
        catch (ConfigException ex) {
            throw new ConversionException("GVDTE_CONFIGURATION_ERROR", new String[][]{{"cause",
                    "while retrieving attribute "}}, ex);
        }
        fieldLength = Integer.parseInt(fldLen);
        fieldOffset = Integer.parseInt(fldOff);

        if (xPathElementType != null) {
            if (xPathElementType.equals(FieldConstants.BUFFER_FIELD_ELEMTYPE_ELEMENT)) {
                xPathElemType = XPathDOMBuilder.CHILD_TYPE_ELEMENT;
            }
            else if (xPathElementType.equals(FieldConstants.BUFFER_FIELD_ELEMTYPE_TEXT)) {
                xPathElemType = XPathDOMBuilder.CHILD_TYPE_TEXT;
            }
            else if (xPathElementType.equals(FieldConstants.BUFFER_FIELD_ELEMTYPE_CDATA)) {
                xPathElemType = XPathDOMBuilder.CHILD_TYPE_CDATA;
            }
        }

        try {
            makeConversionList(base, cfg);
        }
        catch (ConversionException exc) {
            logger.error("Error while getting Conversion class from ConversionFactory: ", exc);
            convList.clear();
            throw exc;
        }
        catch (Exception ex) {
            logger.error("Error initializing Converters", ex);
            convList.clear();
            throw new ConversionException("GVDTE_GENERIC_ERROR", new String[][]{{"msg",
                    "Generic error while getting appropriate Conversion class object."}}, ex);
        }
    }

    /**
     * Returns the maximum length of the binary field represented by this
     * <tt>Field object</tt>.
     *
     * @return the maximum length of the binary field represented by this
     *         <tt>Field</tt> object.
     */
    public int getSize()
    {
        return fieldLength;
    }

    /**
     * This method performs the conversion of the value of a field (or
     * sub-field) of input binary buffer to the value of an element or attribute
     * (represented by its corresponding XPath) of output XML document.
     *
     * @param inBuffer
     *        the byte array representing the binary buffer to be converted.
     * @param outDocument
     *        the DOM representing the XML document resulting from the
     *        conversion of input binary buffer.
     * @throws ConversionException
     *         if any error occurs during field conversion, output DOM branch
     *         generation or runtime errors.
     */
    public void convertToDOM(byte[] inBuffer, Document outDocument) throws ConversionException
    {

        logger.debug("convertToDOM start");
        logger.debug("convertToDOM - field number: " + convList.size());
        int currOffset = fieldOffset;
        int currLength = 0;

        StringBuffer outputElemValue = new StringBuffer();

        try {
            for (int i = 0; i < convList.size(); i++) {

                Conversion conversion = convList.get(i);
                outputElemValue.setLength(0);
                currLength = conversion.convertFieldToXML(inBuffer, currOffset, outputElemValue);
                currOffset = currOffset + currLength;
                logger.debug("convertToDOM - outputElemValue: " + outputElemValue);

                // TBD - gestire la variazione dello XPath al variare del
                // sottocampo

                // Create the corresponding element into output DOM
                createNodeIntoDOM(outDocument, fieldXPath, outputElemValue.toString(), xPathElemType);
            }
            logger.debug("convertToDOM stop");

        }
        catch (ConversionException ex) {
            logger.error("convertToDOM - Error while converting field : ", ex);
            throw new ConversionException("GVDTE_CONVERSION_ERROR", new String[][]{{"msg", "converting field"}}, ex);
        }
        catch (XPathUtilsException ex) {
            logger.error("convertToDOM - Error while creating element into output DOM : ", ex);
            throw new ConversionException("GVDTE_XPATH_UTILS_ERROR", new String[][]{{"cause",
                    "creating element into output DOM."}}, ex);
        }
        catch (Throwable ex) {
            logger.error("convertToDOM - Unexpected error");
            throw new ConversionException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, ex);
        }
    }

    /**
     * This method performs the conversion of the value of an element or
     * attribute (represented by its corresponding XPath) of input XML document
     * (represented by the corresponding DOM) to a field (or sub-field) of the
     * output binary buffer.
     *
     * @param outBuffer
     *        the byte array representing the binary buffer resulting from the
     *        conversion of input XML document.
     * @param inDocument
     *        the DOM representing the XML document to be converted.
     * @throws ConversionException
     *         if any error occurs during input DOM node reading, output buffer
     *         generation or runtime errors.
     */
    public void convertToBin(byte[] outBuffer, Document inDocument) throws ConversionException
    {

        logger.debug("convertToBin - start ");
        // MC: Al posto di CachedXPathAPI
        // utilizziamo delle API Custom
        // che navigano l'XPath utilizzando
        // un algoritmo di backtracking.
        // Queste API sono state testate e risultano
        // piu' veloci delle XPathAPI a parita' di input
        XPathLocator xpLocator = new XPathLocator(inDocument);

        try {
            for (int i = 0; i < convList.size(); i++) {
                // ottengo la conversion da effettuare sul campo
                Conversion conversion = convList.get(i);

                // cerco tramite l'XPath il valore del Nodo
                logger.debug("XPATHSEARCH_BEGIN");
                String inputNodeValue = readInputNodeValue(fieldXPath, inDocument, xpLocator);
                logger.debug("XPATHSEARCH_END");

                logger.debug("convertToBin - inputNodeValue: " + inputNodeValue);

                // effettuo la conversione del campo in Binario
                conversion.convertFieldToBin(outBuffer, fieldOffset, inputNodeValue);
                logger.debug("convertToBin - stop ");
            }
            logger.debug("convertToBin stop");

        }
        catch (UtilsException ex) {
            logger.error("convertToBin - Error while reading XML input node value", ex);
            throw new ConversionException("GVDTE_XPATH_UTILS_ERROR", new String[][]{{"cause",
                    "reading XML input node value"}}, ex);
        }
        catch (ConversionException ex) {
            logger.error("convertToBin - Error while converting value to binary field: ", ex);
            throw new ConversionException("GVDTE_CONVERSION_ERROR", new String[][]{{"msg",
                    "converting value to binary field"}}, ex);
        }
        catch (Throwable ex) {
            logger.error("convertToBin - Unexpected error", ex);
            throw new ConversionException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, ex);
        }
    }

    /**
     * This method returns the value (as text) of the DOM node (element or
     * attribute) whose xPath within DOM <tt>doc</tt> is given by the string
     * <tt>xPath</tt>.
     *
     * @param xPath
     *        the xPath of the node within the DOM.
     * @param doc
     *        the DOM the node belongs to.
     * @param xpLocator
     *        An instance of <tt>XPathLocator</tt> class for XPath navigation.
     * @return the node value (as text).
     * @throws UtilsException
     *         if any <tt>XPathLocator</tt>-related error occurs.
     */
    private String readInputNodeValue(String xPath, Document doc, XPathLocator xpLocator) throws UtilsException
    {
        try {
            logger.debug("readInputNodeValue - input DOM root element name is: "
                    + doc.getDocumentElement().getNodeName());
            if (xPath.indexOf("@") != -1) {
                return xpLocator.getAttributeValue(xPath);
            }
            Node node = xpLocator.getElement(xPath);
            return getNodeValue(node);
        }
        catch (XPathUtilsException ex) {
            logger.error("readInputNodeValue - Error while selecting XML input node value via XPath: " + ex);
            throw new UtilsException("GVDTE_XPATH_UTILS_ERROR", new String[][]{{"cause",
                    "selecting XML input node value via."}}, ex);
        }
        catch (Throwable ex) {
            logger.error("readInputNodeValue - Unexpected error: " + ex);
            throw new UtilsException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, ex);
        }
    }

    /**
     * This method creates the List of Conversion objects for this Field.
     *
     * @param element
     *        The <tt>Element</tt> of the DOM encapsulating the binary buffer
     *        configuration map containing the configuration section regarding
     *        this <tt>Field</tt> object.
     *
     * @throws ClassNotFoundException
     *         if the conversion handler class can't be loaded.
     * @throws InstantiationException
     *         if the conversion handler class can't be instantiated.
     * @throws IllegalAccessException
     *         if the conversion handler class can't be accessed.
     * @throws ConversionException
     *         if <tt>Conversion</tt> object can't be created or initialized.
     */
    private void makeConversionList(String base, IConfigLoader cfg) throws Exception
    {
        convList = new ArrayList<Conversion>();
        String conversionClassName = "";
        try {
            String[] conversionElemList = cfg.getSectionList(base + "/*[@type=\"conversion\"]");

            if (conversionElemList.length > 0) {
                for (String conversionE : conversionElemList) {
                    conversionClassName = (String) cfg.getData(conversionE + "/@class");
                    Conversion conversion = (Conversion) Class.forName(conversionClassName).newInstance();
                    conversion.init(conversionE, cfg);
                    convList.add(conversion);
                }
            }
        }
        catch (ConfigException exc) {
            logger.error("Error retrieving attribute", exc);
            throw new ConversionException("GVDTE_CONFIGURATION_ERROR", new String[][]{{"cause",
                    "while retrieving attribute "}}, exc);
        }
        catch (Exception exc) {
            logger.error("Error instantiating Conversion", exc);
            throw new ConversionException("GVDTE_INSTANCE_ERROR", new String[][]{{"className", conversionClassName},
                    {"cause", "Error instantiating Conversion"}}, exc);
        }
    }

    /**
     * This method creates within output DOM the node which corresponds to the
     * XPath specified into the map, with the string <tt>nodeValue</tt> as
     * value.
     *
     * @param doc
     *        the output DOM.
     * @param xPath
     *        the XPath of the element to be added.
     * @param nodeValue
     *        the (text) value to assign to the node to be added to DOM.
     * @param elementType
     *        the type (<tt>Element</tt>, <tt>CDATA</tt>, or <tt>Text</tt>) of
     *        the node to be added to DOM.
     */
    private void createNodeIntoDOM(Document doc, String xPath, String nodeValue, int elementType)
            throws XPathUtilsException
    {

        // boolean isAttribute = false;

        // Find the element (or attribute) name
        int idx = xPath.lastIndexOf(XPATH_SEPARATOR_CHAR);
        String parentXPath = xPath.substring(0, idx);
        String nodeName = xPath.substring(idx + 1);

        logger.debug("createNodeIntoDOM - doc 1 : " + doc);

        // Process node name
        if (nodeName.indexOf(XPATH_ATTRIBUTE_CHAR) != -1) {
            // It's an attribute
            String attrName = nodeName.substring(1);
            domBuilder.addAttribute(doc, parentXPath, attrName, nodeValue);
        }
        else {
            // It's a CDATA or a TEXT element: Add node with its value
            if ((nodeValue != null) && (nodeValue.length() > 0)) {
                switch (elementType) {
                    case XPathDOMBuilder.CHILD_TYPE_TEXT :
                        domBuilder.addTextElement(doc, xPath, nodeValue);
                        break;
                    case XPathDOMBuilder.CHILD_TYPE_CDATA :
                        domBuilder.addCDATAElement(doc, xPath, nodeValue);
                        break;
                }
            }
            else {
                // It's a generic element
                domBuilder.addElement(doc, xPath);
            }
        }
        logger.debug("createNodeIntoDOM - doc 2 : " + doc);
    }

    /**
     * @param node
     * @return the node value
     */
    public String getNodeValue(Node node)
    {
        if (node instanceof Element) {
            StringBuilder buf = new StringBuilder();
            Node child = node.getFirstChild();
            while (child != null) {
                String val = getNodeValue(child);
                if (val != null) {
                    buf.append(val);
                }
                child = child.getNextSibling();
            }
            return buf.toString();
        }
        return node.getNodeValue();
    }

}
