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
package it.greenvulcano.gvesb.j2ee.db.formatter.base;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import it.greenvulcano.gvesb.j2ee.db.utils.ParameterType;
import it.greenvulcano.gvesb.j2ee.db.utils.XmlWriter;
import it.greenvulcano.gvesb.j2ee.db.utils.XmlWriterImpl;
import it.greenvulcano.util.bin.BinaryUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.Statement;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * This class maps the parameters with the GVBuffer fields.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class CallSPSimpleBaseFormatter implements BaseFormatter
{
    private static Logger logger           = org.slf4j.LoggerFactory.getLogger(CallSPSimpleBaseFormatter.class);
    /**
     * The index of the output parameter
     */
    private int           outputParamIndex = 0;

    /**
     * The stylesheet
     */
    private String        strStylesheet    = null;

    /**
     * The encoding
     */
    private String        encoding         = "UTF-8";

    /**
     * Defines if the return buffer has the metadata or not
     */
    private String        structure        = "ONLY_DATA";

    /**
     * Initializes CallSPSimpleBaseFormatter
     *
     * @param node
     *        the configuration node
     * @throws GVDBException
     *         if an error occurred
     */
    public void init(Node node) throws GVDBException
    {
        try {
            outputParamIndex = XMLConfig.getInteger(node, "@outputParamIndex");
            logger.debug("value attribute outputParamIndex :" + outputParamIndex);

            if (XMLConfig.exists(node, "@stylesheet")) {
                strStylesheet = XMLConfig.get(node, "@stylesheet");
                logger.debug("value attribute stylesheet:" + strStylesheet);
            }
            if (XMLConfig.exists(node, "@encoding")) {
                encoding = XMLConfig.get(node, "@encoding");
                logger.debug("value attribute encoding:" + encoding);
            }
            if (XMLConfig.exists(node, "@structure")) {
                structure = XMLConfig.get(node, "@structure");
                logger.debug("value attribute structure:" + structure);
            }
        }
        catch (XMLConfigException exc) {
            logger.error("init - Error while accessing configuration informations via XMLConfig", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message",
                    "Error while accessing configuration informations via XMLConfig" + exc}}, exc);
        }
        catch (Throwable exc) {
            logger.error("init - Error while initializing CallSPSimpleBaseFormatter", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message", exc.toString()}}, exc);
        }
    }

    /**
     * Set the outputParamIndex.
     *
     * @param outputParamIndex
     *        output index parameter.
     */
    public void setOutputParamIndex(int outputParamIndex)
    {
        this.outputParamIndex = outputParamIndex;
    }

    /**
     * Get the outputParamIndex attribute.
     *
     * @return the output index parameter
     */
    public int getOutputParamIndex()
    {
        return outputParamIndex;
    }

    /**
     * Extracts the output parameter by the CallableStatement and cast it with
     * right type
     *
     * @param callStmt
     *        The CallableStatement object.
     * @param type
     *        The type of output parameter.
     * @return an output parameter casted with his own type.
     * @throws GVDBException
     *         if an error occurred
     */
    public Object getValue(Statement callStmt, String type) throws GVDBException
    {
        logger.debug("Begin Get Value");

        try {
            if (type.equals(ParameterType.JAVA_STRING)) {
                String value = ((CallableStatement) callStmt).getString(outputParamIndex);
                if (value == null) {
                    value = "";
                }
                return value;
            }
            else if (type.equals(ParameterType.JAVA_INT)) {
                return ((CallableStatement) callStmt).getInt(outputParamIndex);
            }
            else if (type.equals(ParameterType.JAVA_LONG)) {
                return ((CallableStatement) callStmt).getLong(outputParamIndex);
            }
            else if (type.equals(ParameterType.JAVA_BYTE_ARRAY)) {
                Object obj = ((CallableStatement) callStmt).getObject(outputParamIndex);

                if (obj != null) {
                    logger.debug("classe:" + obj.getClass().getName());

                    if (obj instanceof Blob) {
                        logger.debug("blob...");
                        return BinaryUtils.inputStreamToBytes(((Blob) obj).getBinaryStream());
                    }
                    if (obj instanceof Clob) {
                        logger.debug("clob...");
                        return BinaryUtils.inputStreamToBytes(((Clob) obj).getAsciiStream());
                    }
                    else if (obj instanceof String) {
                        logger.debug("string...");
                        return ((String) obj).getBytes();
                    }
                    else if (obj instanceof ResultSet) {
                        logger.debug("ParameterType DB_CURSOR");
                        InputStream inStream = null;
                        ResultSet rs = (ResultSet) obj;

                        logger.debug("Get Object");

                        ClassLoader loader = getClass().getClassLoader();
                        if (strStylesheet != null) {
                            inStream = loader.getResourceAsStream("xsl/" + strStylesheet);
                            if (inStream == null) {
                                logger.error("getValue - The resource xsl/" + strStylesheet + " cannot be found");
                                rs.close();
                                throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"msg",
                                        "The resource xsl/" + strStylesheet + " cannot be found"}}, null);
                            }
                        }

                        XmlWriter xmlWriter = new XmlWriterImpl();
                        if ((structure != null) && (structure.equals("ONLY_DATA"))) {
                            xmlWriter.setStructure(XmlWriter.ONLY_DATA);
                        }
                        else {
                            xmlWriter.setStructure(XmlWriter.METADATA_AND_DATA);
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        xmlWriter.writeXML(rs, baos, encoding, inStream);

                        if (logger.isDebugEnabled()) {
                            logger.debug("result cursor after trasformation:["
                                    + new String(baos.toString(encoding).getBytes(), "UTF-8") + "]");
                        }
                        return baos.toString(encoding).getBytes();
                    }
                    else {
                        logger.debug("NO blob NO string...");
                        return obj;
                    }
                }
                logger.error("getValue - blob non valorizzato ");
                return new byte[0];
            }
            else {
                logger.error("getValue - Error while getting parameters by CallableStatement: parameter type not supported"
                        + type);
                throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message",
                        "Error while getting parameters by CallableStatement: paramter type not supported"}}, null);
            }
        }
        catch (Throwable exc) {
            logger.error("getValue - Generic Error", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message", exc.toString()}}, exc);
        }

    }

    /**
     * @see it.greenvulcano.gvesb.j2ee.db.formatter.base.BaseFormatter#getValue(Statement,
     *      String, ResultSet)
     */
    public Object getValue(Statement stmt, String type, ResultSet rs) throws GVDBException
    {
        return null;
    }

    /**
     * @see it.greenvulcano.gvesb.j2ee.db.formatter.base.BaseFormatter#isRsEmpty()
     */
    public boolean isRsEmpty()
    {
        return false;
    }
}
