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
package it.greenvulcano.util.remotefs;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.net.time.TimeUDPClient;
import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * This abstract class provides some utility methods for accessing a remote
 * filesystem via FTP.
 * If remote path is not an absolute pathname, it will be considered relative
 * to current remote working directory. If <code>null</code> it is
 * assumed to be the current working directory.
 * The 'autoconnect' functionality manage connection to and disconnection from
 * FTP server at every method invocation.
 * 
 * @version 3.0.0 Apr 24, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public abstract class RemoteManager
{
    private static final Logger logger        = org.slf4j.LoggerFactory.getLogger(RemoteManager.class);

    /**
     * FTP access informations
     */
    protected String            hostname;
    /**
     *
     */
    protected int               port;
    /**
     *
     */
    protected String            username;
    /**
     *
     */
    protected String            password;

    /**
     * FTP connect timeout settings
     */
    protected int               connectTimeout;
    /**
     * FTP data timeout settings
     */
    protected int               dataTimeout;

    /**
     * An instance of <code>TimeUDPClient</code> class to retrieve remote system
     * time.
     */
    private TimeUDPClient       timeUdpClient;

    /**
     * Enable the autoconnect functionality.
     */
    private boolean             isAutoconnect = false;

    /**
     * Loads data from XML configuration section.
     * 
     * @param configNode
     * @throws RemoteManagerException
     */
    public void init(Node configNode) throws RemoteManagerException
    {
        try {
            hostname = XMLConfig.get(configNode, "@hostname");
            port = XMLConfig.getInteger(configNode, "@port", -1);
            username = XMLConfig.get(configNode, "@username");
            password = XMLConfig.getDecrypted(configNode, "@password");
            isAutoconnect = XMLConfig.getBoolean(configNode, "@autoConnect", false);

            connectTimeout = XMLConfig.getInteger(configNode, "@connectTimeout", 0);
            dataTimeout = XMLConfig.getInteger(configNode, "@dataTimeout", 0);

            logger.debug("hostname          : " + hostname);
            logger.debug("username          : " + username);
            logger.debug("password          : ******");
            logger.debug("connectTimeout    : " + connectTimeout);
            logger.debug("dataTimeout       : " + dataTimeout);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Initialization error", exc);
        }
    }

    /**
     * Return current time on the remote FTP server.
     * 
     * @return the current time on the remote FTP server.
     */
    public long getRemoteTime()
    {
        boolean useLocalTime = true;
        long time = -1;
        try {
            if (timeUdpClient == null) {
                timeUdpClient = new TimeUDPClient();
            }
            timeUdpClient.setDefaultTimeout(1000);
            timeUdpClient.open();
            time = (timeUdpClient.getTime(InetAddress.getByName(hostname)) - TimeUDPClient.SECONDS_1900_TO_1970) * 1000;
            timeUdpClient.close();
            useLocalTime = false;
        }
        catch (Exception exc) {
            logger.warn("Could not detect server time: " + exc);
            time = System.currentTimeMillis();
        }
        finally {
            logger.debug((useLocalTime ? "LOCAL" : "REMOTE") + " machine time [" + time + "]: " + new Date(time));
        }
        return time;
    }

    /**
     * 
     * @return the host name
     */
    public String getHostname()
    {
        return hostname;
    }

    /**
     * 
     * @return the port
     */
    public int getPort()
    {
        return port;
    }

    /**
     * 
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * 
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param isAutoconnect
     *        the isAutoconnect to set
     */
    public void setAutoconnect(boolean isAutoconnect)
    {
        this.isAutoconnect = isAutoconnect;
    }

    /**
     * @return the isAutoconnect
     */
    public boolean isAutoconnect()
    {
        return this.isAutoconnect;
    }

    /**
     * Returns a <code>String</code> representation of this RemoteManager object
     * in the format:<br>
     * 
     * <pre>
     * user/password@hostname
     * </pre>
     */
    @Override
    public String toString()
    {
        return username + "/******@" + hostname;
    }

    /**
     * Connect to the remote FTP server and performs login. Does nothing if
     * already connected and logged in.
     * 
     * @throws RemoteManagerException
     */
    public void connect() throws RemoteManagerException
    {
        connect(new HashMap<String, String>());
    }

    /**
     * Connect to the remote FTP server and performs login. Does nothing if
     * already connected and logged in.
     * 
     * @param optProperties
     *        optional properties
     * @throws RemoteManagerException
     */
    public abstract void connect(Map<String, String> optProperties) throws RemoteManagerException;

    /**
     * Disconnect from the remote FTP server after logging out. Does nothing if
     * already disconnected.
     */
    public void disconnect()
    {
        disconnect(new HashMap<String, String>());
    }

    /**
     * Disconnect from the remote FTP server after logging out. Does nothing if
     * already disconnected.
     * 
     * @param optProperties
     *        optional properties
     */
    public abstract void disconnect(Map<String, String> optProperties);

    /**
     * Returns a <code>java.util.Set</code> object containing a
     * <code>FileProperties</code> object for each filename, found within the
     * remote target directory <i>remoteDirectory</i>,
     * which matches the regular expression <i>filenamePattern</i> and was
     * modified
     * after the date <i>modifiedSince</i>.<br>
     * If the <i>filenamePattern</i> argument is <code>null</code>, all
     * filenames will be
     * considered as matching.<br>
     * If the <i>modifiedSince</i> argument is <code>null</code>, no
     * last-modified check
     * is performed.<br>
     * 
     * @param remoteDirectory
     *        a remote directory.
     * @param fileNamePattern
     *        the filename pattern.
     * @param modifiedSince
     *        a reference date.
     * @param fileTypeFilter
     *        file type filter:
     *        <ul>
     *        <li>0 = <i>files-only</i></li>
     *        <li>1 = <i>directories-only</i></li>
     *        <li>2 = <i>all</i></li>
     *        </ul>
     * @return a <code>java.util.Set</code> of <code>FileProperties</code>
     *         object for each entry
     *         that matches the search criteria.
     * @throws RemoteManagerException
     */
    public abstract Set<FileProperties> ls(String remoteDirectory, String fileNamePattern, Date modifiedSince,
            int fileTypeFilter, Map<String, String> optProperties) throws RemoteManagerException;

    /**
     * Downloads a file, whose name is <i>remoteFile</i>, from the
     * <i>remoteDirectory</i> write it's content in <i>outputDataStream</i>.
     * 
     * @param remoteDirectory
     *        the pathname of a remote directory accessible via an FTP account.
     * @param remoteFile
     *        the name of the remote file to download.
     * @param outputStream
     *        the output stream where to write the data received from the
     *        server.
     * @return <code>true</code> if the download is successful.
     * @throws RemoteManagerException
     */
    public abstract boolean get(String remoteDirectory, String remoteFile, OutputStream outputStream,
            Map<String, String> optProperties) throws RemoteManagerException;

    /**
     * Downloads from the <i>remoteDirectory</i> directory all file/directory
     * matching <i>remoteFilePattern</i>
     * and stores it into a local directory whose pathname is
     * <i>localDirectory</i>.
     * 
     * @param remoteDirectory
     *        the pathname of a remote directory accessible via an FTP account.
     * @param remoteFilePattern
     *        the name of the remote file to download. Can be a regular
     *        expression.
     * @param localDirectory
     *        the pathname of the local directory. Must be an absolute pathname.
     * @return <code>true</code> if the download is successful.
     * @throws RemoteManagerException
     */
    public abstract boolean get(String remoteDirectory, String remoteFilePattern, String localDirectory,
            Map<String, String> optProperties) throws RemoteManagerException;

    /**
     * Downloads a file, whose name is <i>remoteFile</i>, from the
     * <i>remoteDirectory</i> directory and stores it into a local directory
     * whose pathname is <i>localDirectory</i>. If the <i>localFile</i>
     * argument is not <code>null</code>, the downloaded file's name is changed
     * to <i>localFile</i>.
     * 
     * @param remoteDirectory
     *        the pathname of a remote directory accessible via an FTP account.
     * @param remoteFile
     *        the name of the remote file to download.
     * @param localDirectory
     *        the pathname of the local directory. Must be an absolute pathname.
     * @param localFile
     *        the name of the local copy of the file. If <code>null</code>, it
     *        will have the same name of the remote file.
     * @return <code>true</code> if the download is successful.
     * @throws RemoteManagerException
     */
    public abstract boolean get(String remoteDirectory, String remoteFile, String localDirectory, String localFile,
            Map<String, String> optProperties) throws RemoteManagerException;

    /**
     * Downloads a directory, whose path is <i>remoteDirectory</i>, with all its
     * content (files and sub-directories) and stores it inside a local
     * directory whose pathname is <i>localParentDirectory</i>. If the
     * <i>localDirectory</i> argument is not <code>null</code>, the downloaded
     * directory's name is changed to <i>localDirectory</i>.
     * 
     * @param remoteDirectory
     *        the pathname of a remote directory accessible via an FTP account.
     * @param localParentDirectory
     *        the pathname of the local parent directory. Must be an absolute
     *        pathname.
     * @param localDirectory
     *        the name of the local copy of the directory. If <code>null</code>,
     *        it will have the same name of the remote directory.
     * @return <code>true</code> if the download is successful.
     * @throws RemoteManagerException
     */
    public abstract boolean getDir(String remoteDirectory, String localParentDirectory, String localDirectory,
            Map<String, String> optProperties) throws RemoteManagerException;

    /**
     * Uploads a local file, read from <i>inputDataStream</i>, to a remote
     * directory, named <i>remoteDirectory</i> whith name <i>remoteFile</i>.
     * 
     * @param inputDataStream
     *        the input stream containing data to send to server.
     * @param remoteDirectory
     *        the pathname of a remote directory accessible via an FTP account.
     * @param remoteFile
     *        the name of the remote copy of the file.
     * @return <code>true</code> if the upload is successful.
     * @throws RemoteManagerException
     */
    public abstract boolean put(InputStream inputDataStream, String remoteDirectory, String remoteFile,
            Map<String, String> optProperties) throws RemoteManagerException;

    /**
     * Uploads a local file, whose name is <i>localFile</i>, from the
     * <i>localDirectory</i> directory, to a remote directory, named
     * <i>remoteDirectory</i>.<br>
     * If the <i>remoteFile</i> argument is not <code>null</code>, the
     * uploaded file's name is changed to <i>remoteFile</i>.
     * 
     * @param localDirectory
     *        the pathname of the local directory. Must be an absolute pathname.
     * @param localFile
     *        the name of the local file. Must be an existing file.
     * @param remoteDirectory
     *        the pathname of a remote directory accessible via an FTP account.
     * @param remoteFile
     *        the name of the remote copy of the file. If <code>null</code>, it
     *        will have the same name of the local file.
     * @return <code>true</code> if the upload is successful.
     * @throws RemoteManagerException
     */
    public abstract boolean put(String localDirectory, String localFile, String remoteDirectory, String remoteFile,
            Map<String, String> optProperties) throws RemoteManagerException;

    /**
     * Uploads a local file/directory, whose name match <i>localFilePattern</i>,
     * from the <i>localDirectory</i> directory, to a remote directory, named
     * <i>remoteDirectory</i>.<br>
     * 
     * @param localDirectory
     *        the pathname of the local directory. Must be an absolute pathname.
     * @param localFilePattern
     *        the name of the local file. Can be a regular exression.
     * @param remoteDirectory
     *        the pathname of a remote directory accessible via an FTP account.
     * @return <code>true</code> if the upload is successful.
     * @throws RemoteManagerException
     */
    public abstract boolean put(String localDirectory, String localFilePattern, String remoteDirectory,
            Map<String, String> optProperties) throws RemoteManagerException;

    /**
     * Uploads a local directory, whose name is <i>localDirectory</i>, with all
     * its content, and stores it inside a remote directory, named
     * <i>remoteParentDirectory</i>.<br>
     * If the <i>remoteDirectory</i> argument is not <code>null</code>, the
     * uploaded directory's name is changed to <i>remoteDirectory</i>.
     * 
     * @param localDirectory
     *        the pathname of the local directory. Must be an absolute pathname.
     * @param remoteParentDirectory
     *        the pathname of a remote directory accessible via an FTP account.
     * @param remoteDirectory
     *        the name of the remote copy of the directory. If <code>null</code>
     *        ,
     *        it will have the same name of the local directory.
     * @return <code>true</code> if the upload is successful.
     * @throws RemoteManagerException
     */
    public abstract boolean putDir(String localDirectory, String remoteParentDirectory, String remoteDirectory,
            Map<String, String> optProperties) throws RemoteManagerException;

    /**
     * Removes a remote entry, whose name is <i>remoteEntryName</i>, from the
     * remote directory <i>remoteDirectory</i>. If <i>remoteEntryName</i> is a
     * directory, then it will be deleted whith all contained files/directory<br>
     * 
     * @param remoteDirectory
     *        the pathname of a remote directory accessible via an FTP account.
     * @param entryNamePattern
     *        the name of the remote entry, can be a regular expression.
     * @return <code>true</code> if the removal is successful.
     * @throws RemoteManagerException
     */
    public abstract boolean rm(String remoteDirectory, String entryNamePattern, Map<String, String> optProperties)
            throws RemoteManagerException;

    /**
     * Renames a remote entry, whose name is <i>oldEntryName</i>, within the
     * remote directory <i>remoteDirectory</i>, assigning it the new name
     * <i>newEntryName</i>.<br>
     * 
     * @param remoteDirectory
     *        the pathname of a remote directory accessible via an FTP account.
     * @param oldEntryName
     *        the current name of the remote entry.
     * @param newEntryName
     *        the new name to be assigned to the remote entry.
     * @return <code>true</code> if the renaming is successful.
     * @throws RemoteManagerException
     */
    public abstract boolean mv(String remoteDirectory, String oldEntryName, String newEntryName,
            Map<String, String> optProperties) throws RemoteManagerException;

    /**
     * Creates an empty remote directory, whose name is <i>remoteDirectory</i>,
     * inside a remote directory, named <i>remoteParentDirectory</i>.<br>
     * 
     * @param remoteParentDirectory
     *        the pathname of a remote directory accessible via an FTP account.
     * @param remoteDirectory
     *        the name of the remote directory to be created.
     * @return <code>true</code> if the directory creation is successful.
     * @throws RemoteManagerException
     */
    public abstract boolean mkdir(String remoteParentDirectory, String remoteDirectory,
            Map<String, String> optProperties) throws RemoteManagerException;

    /**
     * Return a key identifying the Monitor instance.
     * 
     * @return
     */
    public String getManagerKey()
    {
        return username + "@" + hostname + ":" + port;
    }

    /**
     * Return a key identifying the Monitor instance.
     * Used by the <code>FileSystemStatus</code>.
     * 
     * @return
     */
    public String getManagerKey(Map<String, String> optProperties) throws RemoteManagerException
    {
        try {
            Map<String, Object> localProps = MapUtils.convertToHMStringObject(optProperties);
            return PropertiesHandler.expand(username, localProps) + "@"
                    + PropertiesHandler.expand(hostname, localProps) + ":" + port;
        }
        catch (Exception exc) {
            throw new RemoteManagerException(exc);
        }
    }
}
