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
package it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.log.NMDC;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * OracleExceptionHandler class
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class OracleExceptionHandler
{
    private static final String             CONFIG_FILE        = "SQLErrorMapping.xml";

    private static final Logger             logger             = org.slf4j.LoggerFactory.getLogger(OracleExceptionHandler.class);

    private static final String             NOT_FOUND          = "NOT_FOUND";

    private static Map<String, OracleError> codeMap            = new HashMap<String, OracleError>();

    private static Map<String, String>      platformErrorMap   = new HashMap<String, String>();

    private static Map<String, String>      dataErrorMap       = new HashMap<String, String>();

    private static Map<String, String>      constraintErrorMap = new HashMap<String, String>();

    private static Map<String, String>      statementErrorMap  = new HashMap<String, String>();

    private static Map<String, String>      securityErrorMap   = new HashMap<String, String>();

    private static boolean                  initialized        = false;

    private static boolean                  invalidConfig      = false;

    private synchronized static boolean init()
    {
        if (!initialized && !invalidConfig) {
            platformErrorMap.clear();
            dataErrorMap.clear();
            constraintErrorMap.clear();
            statementErrorMap.clear();
            securityErrorMap.clear();
            try {
                fillErrorMap(XMLConfig.getNodeList(CONFIG_FILE, "//OracleErrorClasses/PlatformErrors/Error"),
                        platformErrorMap);
                fillErrorMap(XMLConfig.getNodeList(CONFIG_FILE, "//OracleErrorClasses/DataErrors/Error"), dataErrorMap);
                fillErrorMap(XMLConfig.getNodeList(CONFIG_FILE, "//OracleErrorClasses/ConstraintErrors/Error"),
                        constraintErrorMap);
                fillErrorMap(XMLConfig.getNodeList(CONFIG_FILE, "//OracleErrorClasses/StatementErrors/Error"),
                        statementErrorMap);
                fillErrorMap(XMLConfig.getNodeList(CONFIG_FILE, "//OracleErrorClasses/SecurityErrors/Error"),
                        securityErrorMap);
                initialized = true;
            }
            catch (Throwable exc) {
                logger.error("Invalid configuration for 'OracleErrorClasses'.", exc);
                invalidConfig = true;
            }
        }
        return initialized && !invalidConfig;
    }

    private static void fillErrorMap(NodeList nl, Map<String, String> errorMap) throws XMLConfigException
    {
        for (int i = 0; i < nl.getLength(); i++) {
            Node oraErr = nl.item(i);
            String code = XMLConfig.get(oraErr, "@code");
            String description = XMLConfig.get(oraErr, "@description", "");
            errorMap.put(code, description);
        }
    }

    /**
     * @param sqlExc
     * @return the oracle error
     */
    public static synchronized OracleError handleSQLException(SQLException sqlExc)
    {
        String sqlErrorCode = Integer.toString(sqlExc.getErrorCode());
        if (!codeMap.containsKey(sqlErrorCode)) {
            handleSQLException(sqlErrorCode, sqlExc);
        }
        OracleError oerr = codeMap.get(sqlErrorCode);
        oerr.setSqlException(sqlExc);
        return oerr;
    }

    private static synchronized void handleSQLException(String sqlErrorCode, SQLException sqlExc)
    {
        boolean codeFound = false;
        String module = null;
        try {
            Node node = null;
            String callerModule = (String) NMDC.get(NMDC.MODULE_KEY);
            if ((callerModule != null) && !callerModule.equals("")) {
                node = XMLConfig.getNode(CONFIG_FILE, "//CustomMapping[@caller='" + callerModule + "']");
            }
            if (node == null) {
                node = XMLConfig.getNode(CONFIG_FILE, "//DefaultMapping");
            }
            if (node != null) {
                Node mapping = XMLConfig.getNode(node, "CatalogMapping[SQLErrorCode[@code='" + sqlErrorCode + "']]");
                if (mapping != null) {
                    module = XMLConfig.get(mapping, "@module");
                }
                if (module == null) {
                    module = XMLConfig.get(node, "@module");
                }
                codeFound = true;
            }
        }
        catch (Throwable exc) {
            logger.error("Error while searching for code '" + sqlErrorCode + "' in configuration file.", exc);
        }

        if (!codeFound) {
            module = NOT_FOUND;
            logger.error("Invalid configuration for 'OracleExceptionHandler'. Original Exception:", sqlExc);
        }
        int errorType = getErrorType(sqlErrorCode);
        codeMap.put(sqlErrorCode, new OracleError(errorType, module));
    }

    private static synchronized int getErrorType(String sqlErrorCode)
    {
        int errorType = OracleError.DATA_ERROR;
        if (init()) {
            if (platformErrorMap.containsKey(sqlErrorCode)) {
                errorType = OracleError.PLATFORM_ERROR;
            }
            else if (constraintErrorMap.containsKey(sqlErrorCode)) {
                errorType = OracleError.CONSTRAINT_ERROR;
            }
            else if (statementErrorMap.containsKey(sqlErrorCode)) {
                errorType = OracleError.STATEMENT_ERROR;
            }
            else if (dataErrorMap.containsKey(sqlErrorCode)) {
                errorType = OracleError.DATA_ERROR;
            }
            else if (securityErrorMap.containsKey(sqlErrorCode)) {
                errorType = OracleError.SECURITY_ERROR;
            }
        }
        return errorType;
    }
}
