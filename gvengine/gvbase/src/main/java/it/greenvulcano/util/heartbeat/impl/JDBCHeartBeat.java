/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 */
package it.greenvulcano.util.heartbeat.impl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder;
import it.greenvulcano.util.heartbeat.HeartBeat;
import it.greenvulcano.util.heartbeat.HeartBeatException;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.StringTokenizer;

import org.w3c.dom.Node;

/**
 * @version 3.2.0 20/nov/2011
 * @author GreenVulcano Developer Team
 */
public class JDBCHeartBeat extends HeartBeat
{
    private static String     JDBC_CONNECTION_NAME = "HeartBeat";
    private Connection        conn                 = null;
    private PreparedStatement insStm               = null;
    private PreparedStatement selStm               = null;
    private boolean           initialized          = false;
    private String            jdbcConnectionName   = JDBC_CONNECTION_NAME;
    private String            hostName             = "";

    /**
     * 
     */
    public JDBCHeartBeat()
    {
        super();
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.util.heartbeat.HeartBeat#internalInit(org.w3c.Node)
     */
    @Override
    protected void internalInit(Node node) throws HeartBeatException
    {
        try {
            InetAddress localMachine = InetAddress.getLocalHost();
            hostName = localMachine.getHostName();
        }
        catch (Exception exc) {
            //handle exception
        }
        if (node != null) {
            jdbcConnectionName = XMLConfig.get(node, "@jdbc-connection-name", JDBC_CONNECTION_NAME);
        }
        initJDBC();
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.util.heartbeat.HeartBeat#beat(java.lang.String, long, boolean)
     */
    @Override
    protected void beat(String subsystem, long timestamp, boolean success) throws HeartBeatException
    {
        if (!initialized) {
            initJDBC();
        }
        try {
            try {
                insStm.setString(1, hostName);
                insStm.setString(2, subsystem);
                insStm.setTimestamp(3, new Timestamp(timestamp));
                insStm.setString(4, (success ? "S" : "F"));
                insStm.setLong(5, System.currentTimeMillis() - timestamp);
                insStm.executeUpdate();
            }
            catch (SQLException exc) {
                closeJDBC();
                initJDBC();
                insStm.setString(1, hostName);
                insStm.setString(2, subsystem);
                insStm.setTimestamp(3, new Timestamp(timestamp));
                insStm.setString(4, (success ? "S" : "F"));
                insStm.setLong(5, System.currentTimeMillis() - timestamp);
                insStm.executeUpdate();
            }
        }
        catch (SQLException exc) {
            System.out.println("SQL Error inserting beat for subsystem[" + subsystem + "]");
            exc.printStackTrace();
            closeJDBC();
            throw new HeartBeatException("SQL Error inserting beat for subsystem[" + subsystem + "]", exc);
        }
        catch (Exception exc) {
            System.out.println("Error inserting beat for subsystem[" + subsystem + "]");
            exc.printStackTrace();
            throw new HeartBeatException("Error inserting beat for subsystem[" + subsystem + "]", exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.util.heartbeat.HeartBeat#lastBeat(java.lang.String, long)
     */
    @Override
    public long lastBeat(String subsystems, long fromTime) throws HeartBeatException
    {
        ResultSet rs = null;
        if (!initialized) {
            initJDBC();
        }
        try {
            if (selStm != null) {
                try {
                    selStm.close();
                }
                catch (Exception exc) {
                    // TODO: handle exception
                }
                selStm = null;
            }
            StringTokenizer st = new StringTokenizer(subsystems, ",");
            StringBuffer list = new StringBuffer();
            while (st.hasMoreTokens()) {
                list.append("'").append(st.nextToken().trim()).append("',");
            }
            list.deleteCharAt(list.length() - 1);
            String statement = "select max(BEAT) from HEARTBEAT where SUBSYSTEM in (" + list + ")";
            if (fromTime > 0) {
                statement += " and BEAT > ?";
            }
            selStm = conn.prepareStatement(statement);
            if (fromTime > 0) {
                selStm.setTimestamp(1, new Timestamp(fromTime));
            }
            rs = selStm.executeQuery();
            if (!rs.next()) {
                return -1;
            }
            Timestamp result = rs.getTimestamp(1);
            if (result != null) {
                return result.getTime();
            }
            return -1;
        }
        catch (SQLException exc) {
            System.out.println("SQL Error selecting last beat for subsystem[" + subsystems + "]");
            exc.printStackTrace();
            closeJDBC();
            throw new HeartBeatException("SQL Error selecting last beat for subsystem[" + subsystems + "]", exc);
        }
        catch (Exception exc) {
            System.out.println("Error selecting last beat for subsystem[" + subsystems + "]");
            exc.printStackTrace();
            throw new HeartBeatException("Error selecting last beat for subsystem[" + subsystems + "]", exc);
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }

    private void initJDBC() throws HeartBeatException
    {
        try {
            conn = JDBCConnectionBuilder.getConnection(jdbcConnectionName);
            insStm = conn.prepareStatement("insert into HEARTBEAT (HOST, SUBSYSTEM, BEAT, STATE, DURATION) values (?, ?, ?, ?, ?)");
            initialized = true;
        }
        catch (Exception exc) {
            closeJDBC();
            throw new HeartBeatException("Errore inizializzazione jdbc", exc);
        }
    }

    private void closeJDBC()
    {
        initialized = false;
        if (insStm != null) {
            try {
                insStm.close();
            }
            catch (Exception exc) {
                // do nothing
            }
            insStm = null;
        }
        if (selStm != null) {
            try {
                selStm.close();
            }
            catch (Exception exc) {
                // do nothing
            }
            selStm = null;
        }
        if (conn != null) {
            try {
                JDBCConnectionBuilder.releaseConnection(jdbcConnectionName, conn);
            }
            catch (Exception exc) {
                // do nothing
            }
            conn = null;
        }
    }

}
