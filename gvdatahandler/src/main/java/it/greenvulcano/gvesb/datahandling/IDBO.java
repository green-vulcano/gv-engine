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
package it.greenvulcano.gvesb.datahandling;

import java.io.OutputStream;
import java.sql.Connection;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * Interface implemented from classes that interact with the DB.
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface IDBO
{
    /**
     *
     */
    public static final String MODE_CALLER = "caller";

    /**
     *
     */
    public static final String MODE_XML2DB = "xml2db";

    /**
     *
     */
    public static final String MODE_DB2XML = "db2xml";

    /**
     *
     */
    public static final String MODE_CALL   = "call";

    /**
     * Method to implement to configure the object that implements this
     * interface.
     *
     * @param config
     *        XML node that contains configuration parameters.
     * @throws DBOException
     *         whenever the configuration is wrong.
     */
    public void init(Node config) throws DBOException;

    /**
     * Method <i>execute</i> implemented by IDBO classes having update
     * interaction with DB.
     *
     * @param input
     *        data to insert or update on DB.
     * @param conn
     *        connection towards the DB.
     * @param props
     *        parameters to substitute in the SQL statement to execute.
     * @throws DBOException
     *         if any error occurs.
     */
    public void execute(Object input, Connection conn, Map<String, Object> props, Object object) throws DBOException, 
            InterruptedException;

    /**
     * Method <i>execute</i> implemented by IDBO classes having read interaction
     * with DB.
     *
     * @param data
     *        data returned from the DB.
     * @param conn
     *        connection towards the DB.
     * @param props
     *        parameters to substitute in the SQL statement to execute.
     * @throws DBOException
     *         if any error occurs.
     */
    public void execute(OutputStream data, Connection conn, Map<String, Object> props) throws DBOException, 
            InterruptedException;

    /**
     * @param dataIn
     * @param dataOut
     * @param conn
     * @param props
     * @throws DBOException
     */
    public void execute(Object dataIn, OutputStream dataOut, Connection conn, Map<String, Object> props)
            throws DBOException, InterruptedException;

    /**
     * @return the configured name of this IDBO
     */
    public String getName();

    /**
     * Sets the transaction state of the operation. If false, the IDBO object
     * must internally handle errors occurred in case of inserting or updating.
     *
     * @param transacted
     *
     */
    public void setTransacted(boolean transacted);

    /**
     * Returns the transformation's name to execute.
     *
     * @return the transformation's name to execute.
     */
    public String getTransformation();

    /**
     * Executes cleanup operations.
     */
    public void cleanup();

    /**
     * Executes finalization operations.
     */
    public void destroy();

    /**
     * @param serviceName
     */
    public void setServiceName(String serviceName);

    /**
     * @param connectionName
     */
    public void setJdbcConnectionName(String connectionName);

    /**
     * @return the input data name
     */
    public String getInputDataName();

    /**
     * @return the output data name
     */
    public String getOutputDataName();

    /**
     * @return if forced mode enabled
     */
    public String getForcedMode();

    /**
     * @return the execution result
     */
    public DHResult getExecutionResult();

    /**
     * @return if returns data.
     */
    public boolean isReturnData();
}
