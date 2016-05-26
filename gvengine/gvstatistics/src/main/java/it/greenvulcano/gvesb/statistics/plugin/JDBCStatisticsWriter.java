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
package it.greenvulcano.gvesb.statistics.plugin;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder;
import it.greenvulcano.gvesb.statistics.GVStatisticsException;
import it.greenvulcano.gvesb.statistics.IStatisticsWriter;
import it.greenvulcano.gvesb.statistics.StatisticsData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class JDBCStatisticsWriter implements IStatisticsWriter
{
    private static Logger   logger                = LoggerFactory.getLogger(JDBCStatisticsWriter.class);
    private String        dataSourceName = null;

    /**
     * 
     * @param node
     * @throws GVStatisticsException
     */
    @Override
    public void init(Node node) throws GVStatisticsException
    {
        logger.debug("JDBCStatisticsWriter init");
        try {
        	dataSourceName = XMLConfig.get(node, "@dataSource");
            logger.debug("JDBCStatisticsWriter - dataSourceName: " + dataSourceName);
        }
        catch (Exception exc) {
            logger.error("Error initializing JDBCStatisticsWriter", exc);
            throw new GVStatisticsException("Error initializing JDBCStatisticsWriter", exc);
        }
    }

    /**
     * This method get the statistics data information and write them on JDBC
     * support
     * 
     * @param statisticsData
     * @return
     * @throws GVStatisticsException
     */
    @Override
    public boolean writeStatisticsData(StatisticsData statisticsData) throws GVStatisticsException
    {
        boolean insertFlag = false;
        Connection dbConnection = null;
        PreparedStatement insertStatement = null;
        try {
            dbConnection =JDBCConnectionBuilder.getConnection(dataSourceName);
            dbConnection.setAutoCommit(true);
            insertStatement = prepareStatement(dbConnection, statisticsData);
            if (insertStatement.executeUpdate() > 0) {
                insertFlag = true;
                logger.debug("Insert into the SERVICE_INFORMATIONS.");
            }
            else {
                logger.error("Insert into the SERVICE_INFORMATIONS table failed. No line inserted.");
            }
        }
        catch (Exception exc) {
            logger.error("Error during insert in SERVICE_INFORMATIONS", exc);
            throw new GVStatisticsException("GVSTATISTICS_DBSTORE_ERROR",
                    new String[][]{{"exception", exc.getMessage()}});
        }
        finally {
            freeResources(dbConnection, insertStatement);
        }
        return insertFlag;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.statistics.IStatisticsWriter#destroy()
     */
    @Override
    public void destroy()
    {
        // do nothing
    }

    /**
     * 
     * @param dbConnection
     * @param statisticsData
     * @return
     */
    private PreparedStatement prepareStatement(Connection dbConnection, StatisticsData statisticsData)
            throws SQLException
    {
        PreparedStatement pstm = dbConnection.prepareStatement(getInsertString(statisticsData.getPropertiesMap()));
        setStatementValues(pstm, statisticsData);
        return pstm;
    }


    /**
     * 
     * @param propertiesMap
     * 
     * @return
     */
    private String getInsertString(Map<String, String> propertiesMap)
    {

        StringBuffer insertColumns = new StringBuffer(
                "Insert into SERVICE_INFORMATIONS(SYSTEM,SERVICE,ID,START_TIME,STOP_TIME,");
        insertColumns.append("START_DATE,STOP_DATE,STATE,ERROR_CODE,PACKAGE_NAME,PROCESS_NAME,PROCESS_TIME");

        StringBuffer insertValues = new StringBuffer("values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");

        for (String columnName : propertiesMap.keySet()) {
            insertColumns.append(",").append(columnName);
            insertValues.append(", ?");
        }

        StringBuffer insert = new StringBuffer();
        insert.append(insertColumns).append(") ").append(insertValues).append(")");

        String statement = insert.toString();
        logger.debug("Statement: " + statement);
        return statement;
    }

    /**
     * 
     * @param pstm
     * @param statisticsData
     * @throws GVStatisticsException
     */
    private void setStatementValues(PreparedStatement pstm, StatisticsData statisticsData) throws SQLException
    {
        String id = (statisticsData.getID()).toString();
        long startTime = statisticsData.getStartTime();
        java.sql.Timestamp startTimestamp = new java.sql.Timestamp(startTime);
        long stopTime = statisticsData.getStopTime();
        int status = statisticsData.getServiceStatus();
        Map<String, String> propertiesMap = statisticsData.getPropertiesMap();

        pstm.setString(1, statisticsData.getSystem());
        pstm.setString(2, statisticsData.getService());
        pstm.setString(3, id);
        pstm.setLong(4, startTime);
        pstm.setLong(5, stopTime);
        pstm.setTimestamp(6, startTimestamp);
        pstm.setTimestamp(7, startTimestamp);
        if (status == StatisticsData.SERVICE_STATUS_UNDEFINED) {
            pstm.setNull(8, java.sql.Types.INTEGER);
            pstm.setNull(9, java.sql.Types.INTEGER);
        }
        else {
            pstm.setInt(8, statisticsData.getServiceStatus());
            pstm.setInt(9, statisticsData.getErrorCode());
        }
        pstm.setString(10, statisticsData.getPackageName());
        pstm.setString(11, statisticsData.getProcessName());
        pstm.setLong(12, statisticsData.getProcessTime());

        int count = 13;
        for (String key : propertiesMap.keySet()) {
            String value = propertiesMap.get(key);
            if (value != null) {
                logger.debug("Value: (" + value + ") at " + count);
                pstm.setString(count++, value);
            }
            else {
                logger.debug("Value: null at " + count);
                pstm.setNull(count++, java.sql.Types.VARCHAR);
            }
        }

    }


    /**
     * Releases the allocated resources.
     * 
     * @param conn
     *        the connection.
     * @param stmt
     *        the statement.
     */
    private void freeResources(Connection conn, Statement stmt)
    {
        try {
            if (stmt != null) {
                stmt.close();
            }
        }
        catch (SQLException exc) {
            // do nothing
        }
        try {
        	JDBCConnectionBuilder.releaseConnection(dataSourceName, conn);
        }
        catch (Exception exc) {
            // do nothing
        }
    }

}