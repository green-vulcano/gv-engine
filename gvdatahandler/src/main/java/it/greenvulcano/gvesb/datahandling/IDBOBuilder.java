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
package it.greenvulcano.gvesb.datahandling;

import it.greenvulcano.gvesb.gvdte.controller.DTEController;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * IDBOBuilder interface to implement to create and execute the IDBO objects. It
 * should be designed as thread-safe module to handle concurrent calls.
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface IDBOBuilder
{

    /**
     *
     */
    public static final String DBO_JDBC_CONNECTION_NAME = "DBO_JDBC_CONNECTION_NAME";
    /**
     *
     */
    public static final int    DUMP_NONE                = 0;
    /**
     *
     */
    public static final int    DUMP_HEX                 = 1;
    /**
     *
     */
    public static final int    DUMP_TEXT                = 2;

    /**
     * @param builder
     * @throws DataHandlerException
     */
    public void init(Node builder) throws DataHandlerException;

    /**
     * @param operation
     * @param file
     * @param params
     * @throws DataHandlerException
     */
    public void XML2DB(String operation, byte[] file, Map<String, Object> params) throws DataHandlerException, 
            InterruptedException;

    /**
     * @param operation
     * @param file
     * @param params
     * @return the result of data handling
     * @throws DataHandlerException
     */
    public byte[] DB2XML(String operation, byte[] file, Map<String, Object> params) throws DataHandlerException, 
            InterruptedException;

    /**
     * @param operation
     * @param file
     * @param params
     * @return the result of data handling
     * @throws DataHandlerException
     */
    public byte[] CALL(String operation, byte[] file, Map<String, Object> params) throws DataHandlerException, 
            InterruptedException;

    /**
     * @param operation
     * @param object
     * @param params
     * @return the result of data handling
     * @throws DataHandlerException
     */
    public DHResult EXECUTE(String operation, Object object, Map<String, Object> params) throws DataHandlerException, 
            InterruptedException;

    /**
     * Cleanup operations.
     */
    public void cleanup();

    /**
     * Finalizing operations.
     */
    public void destroy();

    /**
     * @return the configuration node
     */
    public Node getConfigurationNode();

    /**
     * @param configurationNode
     *        the configuration node to set
     */
    public void setConfigurationNode(Node configurationNode);

    /**
     *
     * @param dteController
     *        the Data Transformation Controller
     */
    public void setDteController(DTEController dteController);

}