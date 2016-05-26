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
package it.greenvulcano.gvesb.core.savepoint;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.thread.BaseThread;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.1.0 Feb 18, 2011
 * @author GreenVulcano Developer Team
 * 
 */
public class SavePointController implements ShutdownEventListener, ConfigurationListener
{
    private static final Logger        logger         = org.slf4j.LoggerFactory.getLogger(SavePointController.class);
    private static SavePointController instance       = null;

    public static final String         STATE_RUNNING  = "RUNNING";
    public static final String         STATE_SAVED    = "SAVED";

    private String                     serverName     = null;
    private String                     connectionName = null;
    private String                     sequenceSQL    = null;
    private boolean                    enabled        = false;

    private boolean                    initialized    = false;

    private SavePointController() throws GVCoreException
    {
        // do nothing
    }

    public static synchronized SavePointController instance() throws GVCoreException
    {
        if (instance == null) {
            instance = new SavePointController();
            ShutdownEventLauncher.addEventListener(instance);
            XMLConfig.addConfigurationListener(instance, GreenVulcanoConfig.getServicesConfigFileName());
            instance.init();
        }
        return instance;
    }

    private void init() throws GVCoreException
    {
        if (initialized) {
            return;
        }
        synchronized (instance) {
            if (initialized) {
                return;
            }

            try {
                serverName = JMXEntryPoint.getServerName();
                Node persNode = XMLConfig.getNode(GreenVulcanoConfig.getServicesConfigFileName(),
                        "/GVServices/Persistence");

                if (persNode != null) {
                    connectionName = XMLConfig.get(persNode, "@jdbc-connection-name");
                    sequenceSQL = XMLConfig.get(persNode, "SequenceSQL");
                    enabled = XMLConfig.getBoolean(persNode, "@enabled", false);
                }
                initialized = true;
            }
            catch (Exception exc) {
                throw new GVCoreException("Error initializing SavePointController", exc);
            }
        }

    }


    /**
     * @param name
     * @throws GVCoreException
     */
    public void save(String id, String system, String service, String operation, String recoveryNode,
            Map<String, Object> environment, Map<String, String> properties) throws GVCoreException
    {
        Connection conn = null;
        boolean isAutocommit = false;
        boolean success = false;
        long recId = -1;

        init();

        if (!enabled) {
            return;
        }

        NMDC.push();
        try {
            NMDC.setServer(serverName);

            conn = JDBCConnectionBuilder.getConnection(connectionName);
            isAutocommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            recId = findRecoveryPoint(conn, id, system, service, operation);

            if (recId == -1) {
                recId = insert(conn, id, system, service, operation, recoveryNode, environment);
            }
            else {
                update(conn, recId, id, system, service, operation, recoveryNode, environment);
            }

            saveProperties(conn, recId, properties);

            success = true;
        }
        catch (GVCoreException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error saving Recovery information for Flow[" + id + "#" + system + "#" + service + "#"
                    + operation + "]", exc);
            throw new GVCoreException("Error saving Recovery information for Flow[" + id + "#" + system + "#" + service
                    + "#" + operation + "]", exc);
        }
        finally {
            if (conn != null) {
                if (success) {
                    try {
                        conn.commit();
                    }
                    catch (Exception exc) {
                        logger.error("Error committing Recovery information for Flow[" + id + "#" + system + "#"
                                + service + "#" + operation + "]", exc);
                    }
                }
                else {
                    try {
                        conn.rollback();
                    }
                    catch (Exception exc) {
                        logger.error("Error rolling-back Recovery information for Flow[" + id + "#" + system + "#"
                                + service + "#" + operation + "]", exc);
                    }
                }
                try {
                    conn.setAutoCommit(isAutocommit);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            try {
                JDBCConnectionBuilder.releaseConnection(connectionName, conn);
            }
            catch (Exception exc) {
                // do nothing
            }
            NMDC.pop();
        }

    }


    /**
     * @param name
     * @throws GVCoreException
     */
    public void delete(long recId) throws GVCoreException
    {
        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement ps_par = null;
        boolean isAutocommit = false;
        boolean success = false;

        init();

        if (!enabled) {
            return;
        }

        NMDC.push();
        try {
            NMDC.setServer(serverName);

            conn = JDBCConnectionBuilder.getConnection(connectionName);
            isAutocommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            String sql_par = "delete from GV_RECOVERY_PROPERTY where REC_ID=?";
            ps_par = conn.prepareStatement(sql_par);
            ps_par.setLong(1, recId);
            ps_par.executeUpdate();

            String sql = "delete from GV_RECOVERY_POINT where REC_ID=?";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, recId);
            int delete = ps.executeUpdate();
            if (delete != 1) {
                logger.error("Error deleting Recovery information for SavePoint[" + recId + "]");
                throw new GVCoreException("Error deleting Recovery information for SavePoint[" + recId + "]");
            }

            logger.debug("Deleted Recovery information for SavePoint[" + recId + "]");

            success = true;
        }
        catch (GVCoreException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error deleting Recovery information for SavePoint[" + recId + "]", exc);
            throw new GVCoreException("Error deleting Recovery information for SavePoint[" + recId + "]", exc);
        }
        finally {
            if (conn != null) {
                if (success) {
                    try {
                        conn.commit();
                    }
                    catch (Exception exc) {
                        logger.error("Error committing delete Recovery information for SavePoint[" + recId + "]", exc);
                    }
                }
                else {
                    try {
                        conn.rollback();
                    }
                    catch (Exception exc) {
                        logger.error("Error rolling-back delete Recovery information for SavePoint[" + recId + "]", exc);
                    }
                }
                try {
                    conn.setAutoCommit(isAutocommit);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (ps_par != null) {
                try {
                    ps_par.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            try {
                JDBCConnectionBuilder.releaseConnection(connectionName, conn);
            }
            catch (Exception exc) {
                // do nothing
            }
            NMDC.pop();
        }
    }


    /**
     * @param name
     * @throws GVCoreException
     */
    @SuppressWarnings("unchecked")
	public void recover(long recId) throws GVCoreException
    {
        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement ps_upd = null;
        ResultSet rs = null;

        init();

        if (!enabled) {
            return;
        }

        NMDC.push();
        try {
            NMDC.setServer(serverName);

            conn = JDBCConnectionBuilder.getConnection(connectionName);

            String sql = "select ID, SYSTEM, SERVICE, OPERATION, RECOVERY_NODE, ENVIRONMENT from GV_RECOVERY_POINT where REC_ID=?";

            ps = conn.prepareStatement(sql);
            ps.setLong(1, recId);

            rs = ps.executeQuery();
            if (!rs.next()) {
                logger.error("Error reading Recovery information for SavePoint[" + recId + "]");
                throw new GVCoreException("Error reading Recovery information for SavePoint[" + recId + "]");
            }
            logger.debug("Read Recovery information for SavePoint[" + recId + "]");

            String id = rs.getString(1);
            String system = rs.getString(2);
            String service = rs.getString(3);
            String operation = rs.getString(4);
            String recoveryNode = rs.getString(5);
            Blob env = rs.getBlob(6);
            ObjectInputStream ois = new ObjectInputStream(env.getBinaryStream());
            Map<String, Object> environment = (Map<String, Object>) ois.readObject();
            logger.debug("Recovery information for SavePoint[" + recId + "]: Flow[" + id + "#" + system + "#" + service
                    + "#" + operation + "#" + recoveryNode + "]");

            GVFlowRunner fwRunner = new GVFlowRunner(id, system, service, operation, recoveryNode, environment);

            String sql_upd = "update GV_RECOVERY_POINT set STATE=? where REC_ID=?";
            ps_upd = conn.prepareStatement(sql_upd);
            ps_upd.setString(1, STATE_RUNNING);
            ps_upd.setLong(2, recId);
            ps_upd.executeUpdate();

            Thread th = new BaseThread(fwRunner, "Flow[" + id + "#" + system + "#" + service + "#" + operation + "#"
                    + recoveryNode + "]");
            th.setDaemon(true);
            th.start();
        }
        catch (GVCoreException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error recovering from SavePoint[" + recId + "]", exc);
            throw new GVCoreException("Error recovering from SavePoint[" + recId + "]", exc);
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
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (ps_upd != null) {
                try {
                    ps_upd.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            try {
                JDBCConnectionBuilder.releaseConnection(connectionName, conn);
            }
            catch (Exception exc) {
                // do nothing
            }
            NMDC.pop();
        }
    }

    /**
     * @param name
     * @throws GVCoreException
     */
    public void delete(String id, String system, String service, String operation) throws GVCoreException
    {
        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement ps_par = null;
        boolean isAutocommit = false;
        boolean success = false;

        init();

        if (!enabled) {
            return;
        }

        NMDC.push();
        try {
            NMDC.setServer(serverName);

            conn = JDBCConnectionBuilder.getConnection(connectionName);
            isAutocommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            long recId = findRecoveryPoint(conn, id, system, service, operation);

            if (recId != -1) {
                String sql_par = "delete from GV_RECOVERY_PROPERTY where REC_ID=?";
                ps_par = conn.prepareStatement(sql_par);
                ps_par.setLong(1, recId);
                ps_par.executeUpdate();

                String sql = "delete from GV_RECOVERY_POINT where REC_ID=?";
                ps = conn.prepareStatement(sql);
                ps.setLong(1, recId);
                ps.executeUpdate();

                logger.debug("Deleted Recovery information for SavePoint[" + recId + "]");

                success = true;
            }
            else {
                logger.debug("Recovery information not found for Flow[" + id + "#" + system + "#" + service + "#"
                        + operation + "]");
            }
        }
        catch (Exception exc) {
            logger.error("Error deleting Recovery information for Flow[" + id + "#" + system + "#" + service + "#"
                    + operation + "]", exc);
            throw new GVCoreException("Error deleting Recovery information for Flow[" + id + "#" + system + "#"
                    + service + "#" + operation + "]", exc);
        }
        finally {
            if (conn != null) {
                if (success) {
                    try {
                        conn.commit();
                    }
                    catch (Exception exc) {
                        logger.error("Error committing delete Recovery information for Flow[" + id + "#" + system + "#"
                                + service + "#" + operation + "]" + "]", exc);
                    }
                }
                else {
                    try {
                        conn.rollback();
                    }
                    catch (Exception exc) {
                        logger.error("Error rolling-back delete Recovery information for Flow[" + id + "#" + system
                                + "#" + service + "#" + operation + "]" + "]", exc);
                    }
                }
                try {
                    conn.setAutoCommit(isAutocommit);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (ps_par != null) {
                try {
                    ps_par.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            try {
                JDBCConnectionBuilder.releaseConnection(connectionName, conn);
            }
            catch (Exception exc) {
                // do nothing
            }
            NMDC.pop();
        }
    }

    /**
     * @param name
     * @throws GVCoreException
     */
    public void confirm(String id, String system, String service, String operation) throws GVCoreException
    {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isAutocommit = false;
        boolean success = false;

        init();

        if (!enabled) {
            return;
        }

        NMDC.push();
        try {
            NMDC.setServer(serverName);

            conn = JDBCConnectionBuilder.getConnection(connectionName);
            isAutocommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            long recId = findRecoveryPoint(conn, id, system, service, operation);

            if (recId != -1) {
                String sql = "update GV_RECOVERY_POINT set STATE=? where REC_ID=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, STATE_SAVED);
                ps.setLong(2, recId);
                ps.executeUpdate();

                logger.debug("Confirmed Recovery information for SavePoint[" + recId + "]");

                success = true;
            }
            else {
                logger.debug("Recovery information not found for Flow[" + id + "#" + system + "#" + service + "#"
                        + operation + "]");
            }
        }
        catch (Exception exc) {
            logger.error("Error confirming Recovery information for Flow[" + id + "#" + system + "#" + service + "#"
                    + operation + "]", exc);
            throw new GVCoreException("Error confirming Recovery information for Flow[" + id + "#" + system + "#"
                    + service + "#" + operation + "]", exc);
        }
        finally {
            if (conn != null) {
                if (success) {
                    try {
                        conn.commit();
                    }
                    catch (Exception exc) {
                        logger.error("Error committing confirm Recovery information for Flow[" + id + "#" + system
                                + "#" + service + "#" + operation + "]" + "]", exc);
                    }
                }
                else {
                    try {
                        conn.rollback();
                    }
                    catch (Exception exc) {
                        logger.error("Error rolling-back confirm Recovery information for Flow[" + id + "#" + system
                                + "#" + service + "#" + operation + "]" + "]", exc);
                    }
                }
                try {
                    conn.setAutoCommit(isAutocommit);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            try {
                JDBCConnectionBuilder.releaseConnection(connectionName, conn);
            }
            catch (Exception exc) {
                // do nothing
            }
            NMDC.pop();
        }
    }

    /**
     *
     */
    public void resetSavePoints() throws GVCoreException
    {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isAutocommit = false;
        boolean success = false;

        init();

        if (!enabled) {
            return;
        }

        NMDC.push();
        try {
            NMDC.setServer(serverName);

            conn = JDBCConnectionBuilder.getConnection(connectionName);
            isAutocommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            String sql = "update GV_RECOVERY_POINT set STATE=? where SERVER=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, STATE_SAVED);
            ps.setString(2, serverName);
            ps.executeUpdate();

            logger.debug("Reset SavePoint state to [" + STATE_SAVED + "]");

            success = true;
        }
        catch (Exception exc) {
            logger.error("Error resetting SavePoint state", exc);
            throw new GVCoreException("Error resetting SavePoint state", exc);
        }
        finally {
            if (conn != null) {
                if (success) {
                    try {
                        conn.commit();
                    }
                    catch (Exception exc) {
                        logger.error("Error committing reset SavePoint state", exc);
                    }
                }
                else {
                    try {
                        conn.rollback();
                    }
                    catch (Exception exc) {
                        logger.error("Error rolling-back reset SavePoint state", exc);
                    }
                }
                try {
                    conn.setAutoCommit(isAutocommit);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            try {
                JDBCConnectionBuilder.releaseConnection(connectionName, conn);
            }
            catch (Exception exc) {
                // do nothing
            }
            NMDC.pop();
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.event.util.shutdown.ShutdownEventListener#shutdownStarted
     * (it.greenvulcano.event.util.shutdown.ShutdownEvent)
     */
    @Override
    public void shutdownStarted(ShutdownEvent event)
    {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.configuration.ConfigurationListener#configurationChanged
     * (it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public synchronized void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED)
                && event.getFile().equals(GreenVulcanoConfig.getServicesConfigFileName())) {
            initialized = false;
        }
    }


    /**
     * @param name
     * @throws GVCoreException
     */
    private long insert(Connection conn, String id, String system, String service, String operation,
            String recoveryNode, Map<String, Object> environment) throws GVCoreException
    {
        PreparedStatement ps = null;
        long recId = -1;
        try {
            recId = getSequenceId(conn);

            String sql = "insert into GV_RECOVERY_POINT(REC_ID, ID, SERVER, SYSTEM, SERVICE, OPERATION, RECOVERY_NODE, ENVIRONMENT, CREATION_DATE, UPDATE_DATE, STATE) "
                    + " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, recId);
            ps.setString(2, id);
            ps.setString(3, serverName);
            ps.setString(4, system);
            ps.setString(5, service);
            ps.setString(6, operation);
            ps.setString(7, recoveryNode);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(environment);
            byte[] objectByte = baos.toByteArray();
            baos.close();
            oos.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(objectByte);
            ps.setBinaryStream(8, bais, objectByte.length);
            java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
            ps.setTimestamp(9, now);
            ps.setTimestamp(10, now);
            ps.setString(11, STATE_RUNNING);
            int insert = ps.executeUpdate();
            if (insert == 1) {
                logger.debug("Saved Recovery information for Flow[" + id + "#" + system + "#" + service + "#"
                        + operation + "]");
            }
            else {
                logger.error("Error saving Recovery information for Flow[" + id + "#" + system + "#" + service + "#"
                        + operation + "]");
                throw new GVCoreException("Error saving Recovery information for Flow[" + id + "#" + system + "#"
                        + service + "#" + operation + "]");
            }

            return recId;
        }
        catch (GVCoreException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error saving Recovery information for Flow[" + id + "#" + system + "#" + service + "#"
                    + operation + "]", exc);
            throw new GVCoreException("Error saving Recovery information for Flow[" + id + "#" + system + "#" + service
                    + "#" + operation + "]", exc);
        }
        finally {
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }

    /**
     * @param name
     * @throws GVCoreException
     */
    private void update(Connection conn, long recId, String id, String system, String service, String operation,
            String recoveryNode, Map<String, Object> environment) throws GVCoreException
    {
        PreparedStatement ps = null;
        try {
            String sql = "update GV_RECOVERY_POINT set SERVER=?, RECOVERY_NODE=?, ENVIRONMENT=?, UPDATE_DATE=?, STATE=? "
                    + "where REC_ID=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, serverName);
            ps.setString(2, recoveryNode);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(environment);
            byte[] objectByte = baos.toByteArray();
            baos.close();
            oos.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(objectByte);
            ps.setBinaryStream(3, bais, objectByte.length);
            java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
            ps.setTimestamp(4, now);
            ps.setString(5, STATE_RUNNING);
            ps.setLong(6, recId);

            int update = ps.executeUpdate();
            if (update == 1) {
                logger.debug("Updated Recovery information for Flow[" + id + "#" + system + "#" + service + "#"
                        + operation + "]");
            }
            else {
                logger.error("Error updating Recovery information for Flow[" + id + "#" + system + "#" + service + "#"
                        + operation + "]");
                throw new GVCoreException("Error updating Recovery information for Flow[" + id + "#" + system + "#"
                        + service + "#" + operation + "]");
            }
        }
        catch (GVCoreException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error updating Recovery information for Flow[" + id + "#" + system + "#" + service + "#"
                    + operation + "]", exc);
            throw new GVCoreException("Error updating Recovery information for Flow[" + id + "#" + system + "#"
                    + service + "#" + operation + "]", exc);
        }
        finally {
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }


    /**
     * @param name
     * @throws GVCoreException
     */
    private void saveProperties(Connection conn, long recId, Map<String, String> properties) throws GVCoreException
    {
        PreparedStatement ps_del = null;
        PreparedStatement ps_ins = null;
        try {
            String sql_del = "delete from GV_RECOVERY_PROPERTY where REC_ID=?";
            ps_del = conn.prepareStatement(sql_del);
            ps_del.setLong(1, recId);
            ps_del.executeUpdate();

            String sql_ins = "insert into GV_RECOVERY_PROPERTY(REC_ID, NAME, VALUE) values(?, ?, ?)";
            ps_ins = conn.prepareStatement(sql_ins);

            for (Entry<String, String> param : properties.entrySet()) {
                ps_ins.setLong(1, recId);
                ps_ins.setString(2, param.getKey());
                if (param.getValue() != null) {
                    byte[] data = param.getValue().getBytes();
                    ByteArrayInputStream bais = new ByteArrayInputStream(data);
                    ps_ins.setAsciiStream(3, bais, data.length);
                }
                else {
                    ps_ins.setNull(3, Types.CLOB);
                }
                ps_ins.executeUpdate();
            }
        }
        catch (Exception exc) {
            logger.error("Error updating Recovery information Properties for SavePoint[" + recId + "]", exc);
            throw new GVCoreException("Error updating Recovery information Properties for SavePoint[" + recId + "]",
                    exc);
        }
        finally {
            if (ps_del != null) {
                try {
                    ps_del.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (ps_ins != null) {
                try {
                    ps_ins.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }


    private long getSequenceId(Connection conn) throws GVCoreException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        long recId = -1;
        try {
            ps = conn.prepareStatement(sequenceSQL);
            rs = ps.executeQuery();
            if (!rs.next()) {
                logger.error("Error generating sequence ID for Recovery information");
                throw new GVCoreException("Error generating sequence ID for Recovery information");
            }
            recId = rs.getLong(1);
        }
        catch (GVCoreException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error generating sequence ID for Recovery information", exc);
            throw new GVCoreException("Error generating sequence ID for Recovery information", exc);
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
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
        return recId;
    }


    private long findRecoveryPoint(Connection conn, String id, String system, String service, String operation)
            throws GVCoreException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        long recId = -1;
        try {
            String sql = "select REC_ID from GV_RECOVERY_POINT where ID=? and SYSTEM=? and SERVICE=? and OPERATION=?";

            ps = conn.prepareStatement(sql);

            ps.setString(1, id);
            ps.setString(2, system);
            ps.setString(3, service);
            ps.setString(4, operation);

            rs = ps.executeQuery();
            if (rs.next()) {
                recId = rs.getLong(1);
                logger.debug("Found Recovery information for Flow[" + id + "#" + system + "#" + service + "#"
                        + operation + "]: " + recId);
            }
        }
        catch (Exception exc) {
            logger.error("Error searching Recovery information for Flow[" + id + "#" + system + "#" + service + "#"
                    + operation + "]", exc);
            throw new GVCoreException("Error searching Recovery information for Flow[" + id + "#" + system + "#"
                    + service + "#" + operation + "]", exc);
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
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
        return recId;
    }


}
