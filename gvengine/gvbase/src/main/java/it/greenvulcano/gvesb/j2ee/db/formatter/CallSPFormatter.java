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
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import it.greenvulcano.gvesb.j2ee.db.utils.ParameterType;
import java.sql.CallableStatement;
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
public class CallSPFormatter implements ResponseFormatter
{
    private static final Logger logger               = org.slf4j.LoggerFactory.getLogger(CallSPFormatter.class);

    /**
     * The class having parameter information read in configuration (name and
     * type of parameter)
     */
    private SPOutputParam[]     spOutputParam        = null;

    /**
     * The GVBuffer output fields
     */
    private GVBufferFieldOutput[] gvBufferFieldsOutput = null;

    /**
     * The result value read from configuration file to value the return code
     */
    private String              resultSuccess        = null;


    /**
     * Initialize. Read formatter info from configuration file
     *
     * @param node
     *        the configuration node
     * @throws GVDBException
     *         if an error occurred
     */
    public void init(Node node) throws GVDBException
    {
        logger.debug("INIT... CallSPFormatter");
        try {
            if (XMLConfig.exists(node, "@success")) {
                resultSuccess = XMLConfig.get(node, "@success");
                logger.debug("value attribute Success :" + resultSuccess);
            }

            NodeList nlParameters = XMLConfig.getNodeList(node, "SPOutputParams/SPOutputParam");
            int iCountOutputParam = nlParameters.getLength();
            logger.debug("Output paramenter configured:" + iCountOutputParam);

            spOutputParam = new SPOutputParam[iCountOutputParam];
            for (int i = 0; i < iCountOutputParam; i++) {
                spOutputParam[i] = new SPOutputParam(nlParameters.item(i));
            }

            nlParameters = XMLConfig.getNodeList(node, "GVBufferMapping/GVBufferFieldOutput");
            int iCountDataFieldOutput = nlParameters.getLength();
            logger.debug("number GVBuffer field to set:" + iCountDataFieldOutput);
            gvBufferFieldsOutput = new GVBufferFieldOutput[iCountDataFieldOutput];

            for (int i = 0; i < iCountDataFieldOutput; i++) {
                gvBufferFieldsOutput[i] = new GVBufferFieldOutput(nlParameters.item(i));
            }
        }
        catch (XMLConfigException exc) {
            logger.error("Init - Error while accessing configuration informations via XMLConfig: ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message",
                    "Error while accessing configuration informations via XMLConfig" + exc}}, exc);
        }
        catch (Throwable exc) {
            logger.error("init - Generic Error : ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
    }

    /**
     * Execute the DB operation
     *
     * @param callStmt
     *        the statement
     * @param inputGVBuffer
     *        the input GVBuffer Object
     * @throws GVDBException
     *         if an error occurred
     */
    public void execute(Statement callStmt, GVBuffer inputGVBuffer) throws GVDBException
    {
        try {
            logger.debug("Setting output parameters...");

            if (spOutputParam.length > 0) {
                specifyOutputParameter(callStmt, inputGVBuffer);
            }
            else {
                logger.debug("No output parameters.");
            }

            logger.debug("calling the Stored Procedure...");
            ((CallableStatement) callStmt).execute();

            if (resultSuccess != null) {
                inputGVBuffer.setRetCode(Integer.parseInt(resultSuccess));
            }

            if (spOutputParam.length > 0) {
                logger.debug("Recovery output parameters");
                for (GVBufferFieldOutput element : gvBufferFieldsOutput) {
                    element.set(((CallableStatement) callStmt), inputGVBuffer);
                }
            }
        }
        catch (SQLException exc) {
            logger.error("execute - Error while executing SQL CallableStatement", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{
                    {"message", "Error while executing SQL CallableStatement" + exc},
                    {"system", inputGVBuffer.getSystem()}, {"service", inputGVBuffer.getService()},
                    {"id", inputGVBuffer.getId().toString()}}, exc);
        }
        catch (GVDBException exc) {
            logger.error("execute - GVDBException error : ", exc);
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("execute - Generic Error : ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message", "Generic Error: " + exc},
                    {"system", inputGVBuffer.getSystem()}, {"service", inputGVBuffer.getService()},
                    {"id", inputGVBuffer.getId().toString()}}, exc);
        }
    }

    /**
     * Specify the output parameter from store procedure
     *
     * @param callStmt
     *        the statement
     * @param inputGVBuffer
     *        the GVBuffer input object
     * @throws GVDBException
     *         if an error occurred
     */
    private void specifyOutputParameter(Statement callStmt, GVBuffer inputGVBuffer) throws GVDBException
    {
        try {
            for (int i = 0; i < spOutputParam.length; i++) {
                String type = (spOutputParam[i].getType()).trim();
                int iPos = spOutputParam[i].getPosition();
                logger.debug("Parameter Output[" + (i + 1) + "] Type [" + type + "] Position [" + iPos + "]...");

                if (type.equals(ParameterType.DB_STRING)) {
                    ((CallableStatement) callStmt).registerOutParameter(iPos, java.sql.Types.VARCHAR);
                }
                else if (type.equals(ParameterType.DB_INT)) {
                    ((CallableStatement) callStmt).registerOutParameter(iPos, java.sql.Types.INTEGER);
                }
                else if (type.equals(ParameterType.DB_LONG)) {
                    ((CallableStatement) callStmt).registerOutParameter(iPos, java.sql.Types.BIGINT);
                }
                else if (type.equals(ParameterType.DB_FLOAT)) {
                    ((CallableStatement) callStmt).registerOutParameter(iPos, java.sql.Types.FLOAT);
                }
                else if (type.equals(ParameterType.DB_DATE)) {
                    ((CallableStatement) callStmt).registerOutParameter(iPos, java.sql.Types.DATE);
                }
                else if (type.equals(ParameterType.DB_LONG_RAW)) {
                    ((CallableStatement) callStmt).registerOutParameter(iPos, java.sql.Types.LONGVARBINARY);
                }
                else if (type.equals(ParameterType.DB_BLOB)) {
                    ((CallableStatement) callStmt).registerOutParameter(iPos, java.sql.Types.BLOB);
                }
                else if (type.equals(ParameterType.DB_CLOB)) {
                    ((CallableStatement) callStmt).registerOutParameter(iPos, java.sql.Types.CLOB);
                }
                else {
                    logger.error("specifyOutputParameter - "
                            + "Error while registring parameters for CallableStatement: "
                            + "parameter type not supported " + type);

                    throw new GVDBException(
                            "GV_GENERIC_ERROR",
                            new String[][]{
                                    {"message",
                                            "Error while registring parameters for CallableStatement: paramter type not supported"},
                                    {"system", inputGVBuffer.getSystem()}, {"service", inputGVBuffer.getService()},
                                    {"id", inputGVBuffer.getId().toString()}}, null);
                }
            }
        }
        catch (SQLException exc) {
            logger.error("specifyOutputParameter - Error while specifing Output Parameters", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{
                    {"message", "Error while specifing Output Parameters" + exc},
                    {"system", inputGVBuffer.getSystem()}, {"service", inputGVBuffer.getService()},
                    {"id", inputGVBuffer.getId().toString()}}, exc);
        }
    }
}
