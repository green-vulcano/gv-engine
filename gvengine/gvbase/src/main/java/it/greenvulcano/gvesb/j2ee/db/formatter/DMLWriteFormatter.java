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
package it.greenvulcano.gvesb.j2ee.db.formatter;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import java.sql.PreparedStatement;
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
public class DMLWriteFormatter implements ResponseFormatter
{
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(DMLWriteFormatter.class);

    /**
     * The success result
     */
    private int                 resultSuccess;

    /**
     * Initialize
     *
     * @param node
     *        the configuration node
     * @throws GVDBException
     *         if an error occurred
     */
    public void init(Node node) throws GVDBException
    {
        try {
            resultSuccess = XMLConfig.getInteger(node, "@success");
            logger.debug("value attribute Success :" + resultSuccess);
        }
        catch (XMLConfigException exc) {
            logger.error("init - Error while accessing configuration informations via XMLConfig: ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message",
                    "Error while accessing configuration informations via XMLConfig" + exc}}, exc);
        }
        catch (Throwable exc) {
            logger.error("init - Generic Error : ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message", "Generic Error: " + exc}}, exc);
        }
    }

    /**
     * This method call an executeUpdate
     *
     * @param stmt
     *        the statement
     * @param gvdIn
     *        the GVBufferererer object
     * @throws GVDBException
     *         if an error occurred
     */
    public void execute(Statement stmt, GVBuffer gvdIn) throws GVDBException
    {
        int count;

        try {
            count = ((PreparedStatement) stmt).executeUpdate();

            logger.debug("record modified --> " + count);
            gvdIn.setRetCode(resultSuccess);
            gvdIn.setProperty("REC_UPDATE", String.valueOf(count));
            logger.debug("SQL Statement executes with success -- RetCode --> " + resultSuccess);
        }
        catch (SQLException exc) {
            logger.error("execute - Error while executing SQL PreparedStatement", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{
                    {"msg", "Error while executing SQL PreparedStatement" + exc}, {"system", gvdIn.getSystem()},
                    {"service", gvdIn.getService()}, {"id", gvdIn.getId().toString()}}, exc);
        }
        catch (Throwable exc) {
            logger.error("execute - Generic Error : ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message", "Generic Error: " + exc},
                    {"system", gvdIn.getSystem()}, {"service", gvdIn.getService()}, {"id", gvdIn.getId().toString()}},
                    exc);
        }
    }
}
