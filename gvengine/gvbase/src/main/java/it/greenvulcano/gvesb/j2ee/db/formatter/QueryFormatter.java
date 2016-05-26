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
package it.greenvulcano.gvesb.j2ee.db.formatter;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import it.greenvulcano.gvesb.j2ee.db.formatter.base.BaseFormatter;
import it.greenvulcano.gvesb.j2ee.db.formatter.base.BaseFormatterFactory;
import it.greenvulcano.gvesb.j2ee.db.utils.ParameterType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
public class QueryFormatter implements ResponseFormatter
{
    private static final Logger logger        = org.slf4j.LoggerFactory.getLogger(QueryFormatter.class);

    /**
     * The success result
     */
    private int                 resultSuccess;

    /**
     * The failure result
     */
    private int                 resultFailure;

    /**
     * BaseFormatter used to format Query output
     */
    private BaseFormatter       baseFormatter = null;

    /**
     * Initialize
     *
     * @param node
     *        configuration node
     * @throws GVDBException
     *         if an error occurred
     */
    public void init(Node node) throws GVDBException
    {
        try {
            resultSuccess = XMLConfig.getInteger(node, "@success");
            logger.debug("value attribute Success :" + resultSuccess);
            resultFailure = XMLConfig.getInteger(node, "@failure");
            logger.debug("value attribute Failure:" + resultFailure);

            if (XMLConfig.exists(node, "*[@type='base_formatter']")) {
                baseFormatter = BaseFormatterFactory.create(XMLConfig.getNode(node, "*[@type='base_formatter']"));
            }
        }
        catch (XMLConfigException exc) {
            logger.error("init - Error while accessing configuration informations via XMLConfig: ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message",
                    "Error while accessing configuration informations via XMLConfig" + exc}}, exc);
        }
        catch (Throwable exc) {
            logger.error("init - Generic Error : ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
    }

    /**
     * The execute method
     *
     * @param stmt
     *        the statement
     * @param gvBuffer
     *        GVBuffer object
     * @throws GVDBException
     *         if an error occurred
     */
    public void execute(Statement stmt, GVBuffer gvBuffer) throws GVDBException
    {
        ResultSet rs = null;

        try {
            rs = ((PreparedStatement) stmt).executeQuery();
            logger.debug("select retuns DATA -- RetCode --> " + resultSuccess);

            if (baseFormatter != null) {
                byte[] objValue = (byte[]) baseFormatter.getValue((stmt), ParameterType.JAVA_BYTE_ARRAY, rs);
                if (objValue != null) {
                    logger.debug("objValue: " + objValue + " objValue class: " + objValue.getClass());
                    insert(objValue, gvBuffer);
                }
                else {
                    logger.debug("objValue is null");
                }
            }
        }
        catch (SQLException exc) {
            logger.error("execute - Error while executing SQL CallableStatement.", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{
                    {"message", "Error while executing SQL CallableStatement" + exc}, {"system", gvBuffer.getSystem()},
                    {"service", gvBuffer.getService()}, {"id", gvBuffer.getId().toString()}}, exc);
        }
        catch (Throwable exc) {
            logger.error("execute - Generic Error : ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message", exc.getMessage()},
                    {"system", gvBuffer.getSystem()}, {"service", gvBuffer.getService()},
                    {"id", gvBuffer.getId().toString()}}, exc);
        }
        finally {
            try {
                if (rs != null) {
                    logger.debug("close ResultSet");
                    rs.close();
                }
            }
            catch (Exception exc) {
                logger.error("execute - Error while closing ResultSet.", exc);
                throw new GVDBException("GV_GENERIC_ERROR", new String[][]{
                        {"message", "Error while closing ResultSet" + exc}, {"system", gvBuffer.getSystem()},
                        {"service", gvBuffer.getService()}, {"id", gvBuffer.getId().toString()}}, exc);
            }
        }
    }

    /**
     * The execute method
     *
     * @param stmt
     *        the statement
     * @param gvdIn
     *        GVBuffer object
     * @throws GVException
     */
    private void insert(byte[] objValue, GVBuffer gvdIn) throws Throwable
    {
        if (baseFormatter.isRsEmpty()) {
            gvdIn.setRetCode(resultFailure);
        }
        else {
            gvdIn.setRetCode(resultSuccess);
        }

        gvdIn.setObject(objValue);
    }
}
