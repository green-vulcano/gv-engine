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
package it.greenvulcano.gvesb.j2ee.db.formatter.base;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import it.greenvulcano.gvesb.j2ee.db.utils.ParameterType;
import it.greenvulcano.gvesb.j2ee.db.utils.XmlWriter;
import it.greenvulcano.gvesb.j2ee.db.utils.XmlWriterImpl;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class QueryXMLBaseFormatter implements BaseFormatter
{
    private static final Logger logger        = org.slf4j.LoggerFactory.getLogger(QueryXMLBaseFormatter.class);
    /**
     * The stylesheet
     */
    private String              strStylesheet = null;

    /**
     * The encoding
     */
    private String              encoding      = "UTF-8";

    /**
     * Defines if the return buffer has the metadata or not
     */
    private String              structure     = "ONLY_DATA";

    private boolean             rsEmpty       = true;

    /**
     * Initializes QueryXMLBaseFormatter
     *
     * @param node
     *        the configuration node
     * @throws GVDBException
     *         if an error occurred
     */
    public void init(Node node) throws GVDBException
    {
        try {
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
            logger.error("init - Error while accessing configuration informations via XMLConfig: ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"msg",
                    "Error while accessing configuration informations via XMLConfig" + exc}}, exc);
        }
        catch (Throwable exc) {
            logger.error("init - Error while initializing SimpleBaseFormatter", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message", exc.toString()}}, exc);
        }
    }

    /**
     * Build a XML with SQL Statement output parameters
     *
     * @param stmt
     *        the statement
     * @param type
     *        the type
     * @param rs
     *        the result set
     * @return The object
     * @throws GVDBException
     *         if an error occurred
     */
    public Object getValue(Statement stmt, String type, ResultSet rs) throws GVDBException
    {
        InputStream inStream = null;
        Object obj = null;

        try {
            ClassLoader loader = getClass().getClassLoader();
            if (strStylesheet != null) {
                inStream = loader.getResourceAsStream("xsl/" + strStylesheet);
                if (inStream == null) {
                    logger.error("getValue - The resource xsl/" + strStylesheet + " cannot be found");
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
            rsEmpty = xmlWriter.getRsEmpty();
            if (type.equals(ParameterType.JAVA_STRING)) {
                logger.debug("Type JAVA_STRING");
                obj = baos.toString(encoding);
            }
            else if (type.equals(ParameterType.JAVA_BYTE_ARRAY)) {
                obj = baos.toByteArray();
                if (obj != null) {
                    logger.debug("OBJ in query = " + obj.toString());
                }
                else {
                    logger.debug("OBJ is NULL");
                }
                logger.debug("Type JAVA_BYTE_ARRAY");
            }
            else {
                logger.error("getValue - Error while getting parameters by CallableStatement: paramter type not supported");
                throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"msg",
                        "Error while getting parameters by CallableStatement: paramter type not supported"}}, null);
            }

            return obj;
        }
        catch (Throwable exc) {
            logger.error("getValue - Error while getting Output ", exc);
            throw new GVDBException("GV_GENERIC_ERROR",
                    new String[][]{{"message", "Error while getting Output :" + exc}}, exc);
        }

    }

    /**
     * @return Returns the rsEmpty.
     */
    public boolean isRsEmpty()
    {
        return rsEmpty;
    }

    /**
     * @param rsEmpty
     *        The rsEmpty to set.
     */
    public void setRsEmpty(boolean rsEmpty)
    {
        this.rsEmpty = rsEmpty;
    }

    /**
     * @see it.greenvulcano.gvesb.j2ee.db.formatter.base.BaseFormatter#getValue(Statement,
     *      String)
     */
    public Object getValue(Statement stmt, String type) throws GVDBException
    {
        return null;
    }
}
