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
package it.greenvulcano.gvesb.j2ee.db.resolver;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import it.greenvulcano.gvesb.j2ee.db.utils.ParameterType;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XPathResolver implements ParamResolver
{
    private static final Logger  logger             = org.slf4j.LoggerFactory.getLogger(XPathResolver.class);

    /**
     * The XPath resolver
     */
    private XPathResolverParam[] xpathResolverParam = null;

    public XPathResolver()
    {
        // do nothing
    }

    /**
     * initializes ParamResolver
     *
     * @param node
     *        the configuration node
     * @throws GVDBException
     *         if an error occurred
     */
    public void init(Node node) throws GVDBException
    {
        NodeList nlParameters = null;
        try {
            nlParameters = XMLConfig.getNodeList(node, "Param");
            int iCountParam = nlParameters.getLength();
            xpathResolverParam = new XPathResolverParam[iCountParam];
            for (int i = 0; i < iCountParam; i++) {
                xpathResolverParam[i] = new XPathResolverParam(nlParameters.item(i));
            }
        }
        catch (XMLConfigException exc) {
            logger.error("init - Error while accessing configuration informations(<Param>) via XMLConfig: ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"msg",
                    "Error while accessing configuration informations(<Param>) via XMLConfig:" + exc}}, exc);
        }
        catch (Throwable exc) {
            logger.error("init - Generic Error : ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"msg", exc.toString()}}, exc);
        }
    }

    /**
     * Resolves SQL parameters
     *
     * @param stmt
     *        the statement
     * @param gvBuffer
     *        The GVBuffer object
     * @throws GVDBException
     *         if an error occurred
     */
    public void resolve(Statement stmt, GVBuffer gvBuffer) throws GVDBException
    {
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            Node gvBufferNode = null;
            Object val = gvBuffer.getObject();
            logger.debug("create node");
            if (val instanceof byte[]) {
                gvBufferNode = parser.parseDOM((byte[]) val, false, true);
            }
            else if (val instanceof String) {
                gvBufferNode = parser.parseDOM((String) val, false, true);
            }
            else if (val instanceof Node) {
                gvBufferNode = (Node) val;
            }
            else {
                logger.error("resolve - Incompatible Object type for XML parsing : " + val.getClass());
                throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message", "Incompatible Object type for XML parsing: " + val.getClass()},
                        {"system", gvBuffer.getSystem()}, {"service", gvBuffer.getService()}, {"id", gvBuffer.getId().toString()}});
            }
            for (XPathResolverParam param : xpathResolverParam) {
                int posiz = param.getPosition();
                String type = param.getType().trim();
                String strValue = parser.get(gvBufferNode, param.getXPath());
                logger.debug("resolve - parameter (" + posiz + ") XPath: " + param.getXPath() + " Type: " + type
                        + " Value: " + strValue);
                if (type.equals(ParameterType.DB_STRING)) {
                    ((PreparedStatement) stmt).setString(posiz, strValue);
                }
                else if (type.equals(ParameterType.DB_DATE)) {
                    String format = param.getFormat();
                    java.util.Date date = null;
                    if (format != null) {
                        date = DateUtils.stringToDate(strValue, format);
                    }
                    else {
                        throw new GVDBException("GV_GENERIC_ERROR", new String[][]{
                                {"message", "DateFormat not specified"}, {"system", gvBuffer.getSystem()},
                                {"service", gvBuffer.getService()}, {"id", gvBuffer.getId().toString()}}, null);
                    }

                    ((PreparedStatement) stmt).setTimestamp(posiz, new java.sql.Timestamp(date.getTime()));
                    logger.debug("Date format: " + format);
                    logger.debug("Date: " + date);
                }
                else if (type.equals(ParameterType.DB_INT)) {
                    int iValue = Integer.parseInt(strValue);
                    ((PreparedStatement) stmt).setInt(posiz, iValue);
                }
                else if (type.equals(ParameterType.DB_LONG)) {
                    long lValue = Long.parseLong(strValue);
                    ((PreparedStatement) stmt).setLong(posiz, lValue);
                }
                else if (type.equals(ParameterType.DB_FLOAT)) {
                    float fValue = Float.parseFloat(strValue);
                    ((PreparedStatement) stmt).setFloat(posiz, fValue);
                }
                else if (type.equals(ParameterType.DB_LONG_RAW)) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(strValue.getBytes());
                    ((PreparedStatement) stmt).setBinaryStream(posiz, bais, strValue.length());
                }
                else if (type.equals(ParameterType.DB_BLOB)) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(strValue.getBytes());
                    ((PreparedStatement) stmt).setBinaryStream(posiz, bais, strValue.length());
                }
                else if (type.equals(ParameterType.DB_CLOB)) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(strValue.getBytes());
                    ((PreparedStatement) stmt).setAsciiStream(posiz, bais, strValue.length());
                }
                else {
                    logger.error("resolve - "
                            + "Error while setting parameters for CallableStatement: paramter type not supported");
                    throw new GVDBException(
                            "GV_GENERIC_ERROR",
                            new String[][]{
                                    {"msg",
                                            "Error while setting parameters for CallableStatement: paramter type not supported"},
                                    {"system", gvBuffer.getSystem()}, {"service", gvBuffer.getService()},
                                    {"id", gvBuffer.getId().toString()}}, null);
                }
            }
        }
        catch (XMLUtilsException exc) {
            logger.error("resolve - Error while searching informations into buffer via XMLUtils: ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{
                    {"msg", "Error while searching informations into buffer via XMLUtils" + exc},
                    {"system", gvBuffer.getSystem()}, {"service", gvBuffer.getService()},
                    {"id", gvBuffer.getId().toString()}}, exc);
        }
        catch (SQLException exc) {
            logger.error("resolve - Error while setting parameters for CallableStatement", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{
                    {"msg", "Error while setting parameters for CallableStatement" + exc},
                    {"system", gvBuffer.getSystem()}, {"service", gvBuffer.getService()},
                    {"id", gvBuffer.getId().toString()}}, exc);
        }
        catch (Throwable exc) {
            logger.error("resolve - Generic Error : ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"msg", exc.toString()},
                    {"system", gvBuffer.getSystem()}, {"service", gvBuffer.getService()},
                    {"id", gvBuffer.getId().toString()}}, exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

}
