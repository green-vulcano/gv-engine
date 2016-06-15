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
package it.greenvulcano.util.remotefs.ftp;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.file.RegExFileFilter;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.remotefs.RemoteManager;
import it.greenvulcano.util.remotefs.RemoteManagerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Level;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Apr 24, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 * 
 */
public class FTPManager extends RemoteManager
{
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(FTPManager.class);

    /**
     * @version 3.0.0 Apr 24, 2010
     * @author GreenVulcano Developer Team
     * 
     */
    public enum TargetType {
        /**
         *
         */
        MVS,
        /**
         *
         */
        NT,
        /**
         *
         */
        OS2,
        /**
         *
         */
        OS400,
        /**
         *
         */
        UNIX,
        /**
         *
         */
        VMS
    }

    /**
     *
     */
    protected TargetType hostType;

    /**
     * An instance of <code>FTPClient</code> class for FTP operations.
     */
    protected FTPClient  ftpClient;

    /**
     * A flag indicating if the RemoteManager object is connected and logged on
     * FTP server
     */
    private boolean      isConnected = false;

    /**
     * @throws RemoteManagerException
     */
    public FTPManager() throws RemoteManagerException
    {
        super();

        ftpClient = createFTPClient();
    }

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#init(Node)
     */
    @Override
    public void init(Node node) throws RemoteManagerException
    {
        super.init(node);
        try {
            hostType = TargetType.valueOf(XMLConfig.get(node, "@hostType"));
            logger.debug("host-type          : " + hostType);

            FTPClientConfig conf = null;

            switch (hostType) {
                case MVS : {
                    conf = new FTPClientConfig(FTPClientConfig.SYST_MVS);
                    break;
                }
                case NT : {
                    conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
                    break;
                }
                case OS2 : {
                    conf = new FTPClientConfig(FTPClientConfig.SYST_OS2);
                    break;
                }
                case OS400 : {
                    conf = new FTPClientConfig(FTPClientConfig.SYST_OS400);
                    break;
                }
                case UNIX : {
                    conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
                    break;
                }
                case VMS : {
                    conf = new FTPClientConfig(FTPClientConfig.SYST_VMS);
                    break;
                }
            }

            ftpClient.configure(conf);
            ftpClient.setDefaultTimeout(connectTimeout);
            ftpClient.setDataTimeout(dataTimeout);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Initialization error", exc);
        }
    }


    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#connect(Map<String,
     *      String>)
     */
    @SuppressWarnings("deprecation")
	@Override
    public void connect(Map<String, String> optProperties) throws RemoteManagerException
    {
        if (!isConnected) {
            int reply = 0;
            boolean errorsOccurred = true;
            String localHostname = hostname;
            try {
                Map<String, Object> localProps = MapUtils.convertToHMStringObject(optProperties);
                String localUsername = PropertiesHandler.expand(username, localProps);
                String localPassword = XMLConfig.getDecrypted(PropertiesHandler.expand(password, localProps));
                localHostname = PropertiesHandler.expand(hostname, localProps);

                logger.debug("Connecting to FTP server " + localHostname + ":" + port + "...");
                ftpClient.connect(localHostname, port);
                reply = ftpClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    throw new RemoteManagerException("FTP server (" + getManagerKey(optProperties)
                            + ") refused connection: " + ftpClient.getReplyString());
                }
                logger.debug("FTP server reply: " + ftpClient.getReplyString().trim());

                //logger.debug("Logging in as FTP user " + localUsername + "/" + localPassword + "...");
                logger.debug("Logging in as FTP user " + localUsername + "...");
                ftpClient.login(localUsername, localPassword);
                reply = ftpClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    throw new RemoteManagerException("FTP server (" + getManagerKey(optProperties)
                            + ") refused user login: " + ftpClient.getReplyString());
                }
                logger.debug("FTP server reply: " + ftpClient.getReplyString().trim());

                logger.debug("Entering local passive mode...");
                ftpClient.enterLocalPassiveMode();

                logger.debug("Connected to FTP server " + localHostname + " and logged in as FTP user " + localUsername);
                logger.debug("Current working directory is: " + ftpClient.printWorkingDirectory());
                logger.debug("FTP Server name is: " + ftpClient.getSystemName());
                isConnected = true;
                errorsOccurred = false;
            }
            catch (RemoteManagerException exc) {
                throw exc;
            }
            catch (SocketException exc) {
                throw new RemoteManagerException("Protocol error. " + getManagerKey(optProperties), exc);
            }
            catch (IOException exc) {
                throw new RemoteManagerException("I/O error. " + getManagerKey(optProperties), exc);
            }
            catch (Exception exc) {
                throw new RemoteManagerException("Generic error. " + getManagerKey(optProperties), exc);
            }
            finally {
                if (errorsOccurred) {
                    if (ftpClient.isConnected()) {
                        try {
                            logger.debug("Disconnecting from FTP server " + localHostname + "...");
                            ftpClient.disconnect();
                        }
                        catch (Exception exc) {
                            logger.warn("Disconnection from FTP server " + localHostname + " failed", exc);
                        }
                    }
                }
            }
        }
    }


    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#disconnect(Map<String,
     *      String>)
     */
    @Override
    public void disconnect(Map<String, String> optProperties)
    {
        if (isConnected) {
            String localHostname = hostname;
            try {
                Map<String, Object> localProps = MapUtils.convertToHMStringObject(optProperties);
                String localUsername = PropertiesHandler.expand(username, localProps);
                localHostname = PropertiesHandler.expand(hostname, localProps);

                if (ftpClient.isConnected()) {
                    logger.debug("Logging out FTP user " + localUsername + " from FTP server " + localHostname + "...");
                    ftpClient.logout();
                    int reply = ftpClient.getReplyCode();
                    if (!FTPReply.isPositiveCompletion(reply)) {
                        logger.warn("FTP server " + localHostname + " refused user logout : "
                                + ftpClient.getReplyString());
                    }
                    else {
                        logger.debug("FTP server " + localHostname + " reply: " + ftpClient.getReplyString().trim());
                    }
                    logger.debug("FTP user " + localUsername + " logged out");
                }
            }
            catch (IOException exc) {
                logger.warn("I/O error while logging out", exc);
            }
            catch (Exception exc) {
                logger.warn("Generic error while logging out", exc);
            }
            finally {
                if (ftpClient.isConnected()) {
                    try {
                        logger.debug("Disconnecting from FTP server " + localHostname + "...");
                        ftpClient.disconnect();
                    }
                    catch (Exception exc) {
                        logger.warn("Disconnection from FTP server " + localHostname + " failed", exc);
                    }
                }
                isConnected = false;
            }
        }
    }

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#ls(String, String, date,
     *      int, java.util.Map)
     */
    @Override
    public Set<FileProperties> ls(String remoteDirectory, String filenamePattern, Date modifiedSince, int fileTypeFilter,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        Set<FileProperties> resultsSet = new HashSet<FileProperties>();
        try {
            changeWorkingDirectory(remoteDirectory);

            FTPFile[] results = ftpClient.listFiles();
            int detectedFiles = (results != null ? results.length : 0);
            logger.debug(detectedFiles + " file entries DETECTED into current remote working directory");

            if (results != null) {
                RegExFileFilter fileFilter = new RegExFileFilter(filenamePattern, fileTypeFilter,
                        (modifiedSince != null) ? modifiedSince.getTime() : -1);
                for (FTPFile currFTPFile : results) {
                    if (currFTPFile != null) {
                        if (fileFilter.accept(currFTPFile)) {
                            FileProperties currFile = new FileProperties(currFTPFile.getName(),
                                    currFTPFile.getTimestamp().getTimeInMillis(), currFTPFile.getSize(),
                                    currFTPFile.isDirectory(), currFTPFile.hasPermission(FTPFile.USER_ACCESS,
                                            FTPFile.READ_PERMISSION), currFTPFile.hasPermission(FTPFile.USER_ACCESS,
                                            FTPFile.WRITE_PERMISSION), currFTPFile.hasPermission(FTPFile.USER_ACCESS,
                                            FTPFile.EXECUTE_PERMISSION));
                            resultsSet.add(currFile);
                        }
                    }
                    else {
                        logger.debug("Remote file entry NULL");
                    }
                }
            }
            return resultsSet;
        }
        catch (Exception exc) {
            throw new RemoteManagerException("FTP directory scan error", exc);
        }
        finally {
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#get(String, String,
     *      outputDataStream, java.util.Map)
     */
    @Override
    public boolean get(String remoteDirectory, String remoteFile, OutputStream outputStream,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        boolean result = false;
        try {
            logger.debug("Downloading remote file "
                    + remoteFile
                    + " from "
                    + (remoteDirectory != null
                            ? " remote directory " + remoteDirectory
                            : " current remote working directory") + "...");

            if (remoteDirectory != null) {
                changeWorkingDirectory(remoteDirectory);
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.retrieveFile(remoteFile, outputStream);

            int reply = ftpClient.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {
                logger.debug("Remote file " + remoteFile + " saved to OutputStream");
                result = true;
            }
            else {
                logger.warn("FTP Server NEGATIVE response: ");
                logServerReply(Level.WARN);
            }
            return result;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#get(String, String,
     *      String, String, java.util.Map)
     */
    @Override
    public boolean get(String remoteDirectory, String remoteFile, String localDirectory, String localFile,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        boolean result = false;
        OutputStream output = null;
        try {
            logger.debug("Downloading remote file "
                    + remoteFile
                    + " from "
                    + (remoteDirectory != null
                            ? " remote directory " + remoteDirectory
                            : " current remote working directory") + "...");
            File localPathname = new File(localDirectory, (localFile != null ? localFile : remoteFile));
            if (!localPathname.isAbsolute()) {
                throw new RemoteManagerException("Local pathname (" + localPathname + ") is NOT absolute.");
            }

            logger.debug("Saving to " + localPathname);
            output = new FileOutputStream(localPathname);

            if (remoteDirectory != null) {
                changeWorkingDirectory(remoteDirectory);
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.retrieveFile(remoteFile, output);

            int reply = ftpClient.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {
                logger.debug("Remote file " + remoteFile + " saved to " + localPathname);
                result = true;
            }
            else {
                logger.warn("FTP Server NEGATIVE response: ");
                logServerReply(Level.WARN);
            }
            return result;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            if (output != null) {
                try {
                    output.close();
                }
                catch (IOException exc) {
                    logger.warn("Error while closing local file output stream");
                }
            }
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#get(String, String,
     *      String, java.util.Map)
     */
    @Override
    public boolean get(String remoteDirectory, String remoteFilePattern, String localDirectory,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        boolean oldAutoConnect = isAutoconnect();
        setAutoconnect(false);

        boolean result = false;
        try {
            changeWorkingDirectory(remoteDirectory);
            String currentlyDownloading = ftpClient.printWorkingDirectory();

            File localDirectoryObj = new File(localDirectory);
            if (!localDirectoryObj.exists()) {
                if (!localDirectoryObj.mkdir()) {
                    throw new RemoteManagerException("Cannot create local directory "
                            + localDirectoryObj.getAbsolutePath());
                }
                logger.debug("Local directory " + localDirectoryObj.getAbsolutePath() + " created");
            }

            FTPFile[] results = ftpClient.listFiles();

            if (results != null) {
                RegExFileFilter fileFilter = new RegExFileFilter(remoteFilePattern, RegExFileFilter.ALL, -1);
                for (FTPFile currFTPFile : results) {
                    if (currFTPFile != null) {
                        if (fileFilter.accept(currFTPFile)) {
                            boolean partialResult = true;
                            if (currFTPFile.isDirectory()) {
                                partialResult = getDir(currFTPFile.getName(), localDirectoryObj.getAbsolutePath(), null,
                                        optProperties);
                            }
                            else {
                                partialResult = get(null, currFTPFile.getName(), localDirectoryObj.getAbsolutePath(),
                                        null, optProperties);
                            }
                            if (!partialResult) {
                                break;
                            }
                        }
                    }
                    else {
                        logger.debug("Remote file entry NULL");
                    }
                }
            }

            int reply = ftpClient.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {
                logger.debug("Remote directory " + currentlyDownloading + " downloaded");
                result = true;
            }
            else {
                logger.warn("Could not download remote directory " + currentlyDownloading
                        + " (FTP Server NEGATIVE response):");
                logServerReply(Level.WARN);
            }
            return result;
        }
        catch (RemoteManagerException exc) {
            throw exc;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            setAutoconnect(oldAutoConnect);
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#getDir(String, String,
     *      String, java.util.Map)
     */
    @Override
    public boolean getDir(String remoteDirectory, String localParentDirectory, String localDirectory,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        boolean oldAutoConnect = isAutoconnect();
        setAutoconnect(false);

        boolean result = false;
        try {
            changeWorkingDirectory(remoteDirectory);
            String currentlyDownloading = ftpClient.printWorkingDirectory();

            if ((localDirectory == null) || localDirectory.equals("")) {
                localDirectory = new File(remoteDirectory).getName();
            }

            File localDirectoryObj = new File(localParentDirectory, localDirectory);
            if (!localDirectoryObj.exists()) {
                if (!localDirectoryObj.mkdir()) {
                    throw new RemoteManagerException("Cannot create local directory "
                            + localDirectoryObj.getAbsolutePath());
                }
                logger.debug("Local directory " + localDirectoryObj.getAbsolutePath() + " created");
            }

            FTPFile[] results = ftpClient.listFiles();

            if (results != null) {
                for (FTPFile currFTPFile : results) {
                    if (currFTPFile != null) {
                        boolean partialResult = true;
                        if (currFTPFile.isDirectory()) {
                            partialResult = getDir(currFTPFile.getName(), localDirectoryObj.getAbsolutePath(), null,
                                    optProperties);
                        }
                        else {
                            partialResult = get(null, currFTPFile.getName(), localDirectoryObj.getAbsolutePath(), null,
                                    optProperties);
                        }
                        if (!partialResult) {
                            break;
                        }
                    }
                    else {
                        logger.debug("Remote file entry NULL");
                    }
                }
            }

            ftpClient.changeToParentDirectory();

            int reply = ftpClient.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {
                logger.debug("Remote directory " + currentlyDownloading + " downloaded");
                result = true;
            }
            else {
                logger.warn("Could not download remote directory " + currentlyDownloading
                        + " (FTP Server NEGATIVE response):");
                logServerReply(Level.WARN);
            }
            return result;
        }
        catch (RemoteManagerException exc) {
            throw exc;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            setAutoconnect(oldAutoConnect);
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#put(String, String,
     *      String, String, java.util.Map)
     */
    @Override
    public boolean put(String localDirectory, String localFile, String remoteDirectory, String remoteFile,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        boolean result = false;
        FileInputStream input = null;
        try {
            File localPathname = new File(localDirectory, localFile);
            if (!localPathname.isAbsolute()) {
                throw new RemoteManagerException("Local pathname (" + localPathname + ") is NOT absolute.");
            }

            input = new FileInputStream(localPathname);
            logger.debug("Uploading local file "
                    + localPathname
                    + (remoteDirectory != null
                            ? " to remote directory " + remoteDirectory
                            : " to current remote working directory") + "...");

            if (remoteFile != null) {
                logger.debug("Renaming remote file to " + remoteFile);
            }

            if (remoteDirectory != null) {
                changeWorkingDirectory(remoteDirectory);
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.storeFile((remoteFile != null ? remoteFile : localFile), input);

            int reply = ftpClient.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {
                logger.debug("Local file " + localPathname + " uploaded.");
                result = true;
            }
            else {
                logger.warn("FTP Server NEGATIVE response: ");
                logServerReply(Level.WARN);
            }
            return result;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException exc) {
                    logger.warn("Error while closing local file input stream", exc);
                }
            }
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#put(InputStream, String,
     *      String, java.util.Map)
     */
    @Override
    public boolean put(InputStream inputDataStream, String remoteDirectory, String remoteFile,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        boolean result = false;
        try {
            logger.debug("Uploading stream "
                    + (remoteDirectory != null
                            ? " to remote directory " + remoteDirectory
                            : " to current remote working directory") + "...");

            if (remoteFile != null) {
                logger.debug("Renaming remote file to " + remoteFile);
            }

            if (remoteDirectory != null) {
                changeWorkingDirectory(remoteDirectory);
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.storeFile(remoteFile, inputDataStream);

            int reply = ftpClient.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {
                logger.debug("Stream uploaded.");
                result = true;
            }
            else {
                logger.warn("FTP Server NEGATIVE response: ");
                logServerReply(Level.WARN);
            }
            return result;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#put(String, String,
     *      String, java.util.Map)
     */
    @Override
    public boolean put(String localDirectory, String localFilePattern, String remoteDirectory,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        boolean oldAutoConnect = isAutoconnect();
        setAutoconnect(false);

        boolean result = false;
        try {
            File localDirectoryObj = new File(localDirectory);

            changeWorkingDirectory(remoteDirectory);

            File[] localFiles = localDirectoryObj.listFiles(new RegExFileFilter(localFilePattern, RegExFileFilter.ALL));
            for (File currLocalFile : localFiles) {
                boolean partialResult = true;
                if (currLocalFile.isDirectory()) {
                    partialResult = putDir(currLocalFile.getAbsolutePath(), null, null, optProperties);
                }
                else {
                    partialResult = put(localDirectoryObj.getAbsolutePath(), currLocalFile.getName(), null, null,
                            optProperties);
                }

                if (!partialResult) {
                    break;
                }
            }

            int reply = ftpClient.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {
                logger.debug("Local directory " + localDirectory + " uploaded");
                result = true;
            }
            else {
                logger.warn("Could not upload local directory " + localDirectory + " (FTP Server NEGATIVE response):");
                logServerReply(Level.WARN);
            }
            return result;
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            setAutoconnect(oldAutoConnect);
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#putDir(String, String,
     *      String, java.util.Map)
     */
    @Override
    public boolean putDir(String localDirectory, String remoteParentDirectory, String remoteDirectory,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        boolean oldAutoConnect = isAutoconnect();
        setAutoconnect(false);

        boolean result = false;
        try {
            File localDirectoryObj = new File(localDirectory);

            if (remoteParentDirectory != null) {
                changeWorkingDirectory(remoteParentDirectory);
            }

            if (remoteDirectory == null) {
                remoteDirectory = localDirectoryObj.getName();
            }
            ftpClient.makeDirectory(remoteDirectory);
            logger.debug("Remote directory " + remoteDirectory + " created into " + ftpClient.printWorkingDirectory());
            changeWorkingDirectory(remoteDirectory);

            File[] localFiles = localDirectoryObj.listFiles();
            for (File currLocalFile : localFiles) {
                boolean partialResult = true;
                if (currLocalFile.isDirectory()) {
                    partialResult = putDir(currLocalFile.getAbsolutePath(), null, null, optProperties);
                }
                else {
                    partialResult = put(localDirectoryObj.getAbsolutePath(), currLocalFile.getName(), null, null,
                            optProperties);
                }

                if (!partialResult) {
                    break;
                }
            }

            ftpClient.changeToParentDirectory();

            int reply = ftpClient.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {
                logger.debug("Local directory " + localDirectory + " uploaded");
                result = true;
            }
            else {
                logger.warn("Could not upload local directory " + localDirectory + " (FTP Server NEGATIVE response):");
                logServerReply(Level.WARN);
            }
            return result;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            setAutoconnect(oldAutoConnect);
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#rm(String, String, java.util.Map)
     */
    @Override
    public boolean rm(String remoteDirectory, String entryNamePattern, Map<String, String> optProperties)
            throws RemoteManagerException
    {
        checkConnected();

        boolean oldAutoConnect = isAutoconnect();
        setAutoconnect(false);

        boolean result = false;
        try {
            if (remoteDirectory != null) {
                changeWorkingDirectory(remoteDirectory);
            }

            String[] filenames = ftpClient.listNames();
            int detectedFiles = (filenames != null ? filenames.length : 0);
            FTPFile[] results = ftpClient.listFiles();
            int parsedFiles = (results != null ? results.length : 0);
            if (detectedFiles != parsedFiles) {
                logger.warn("Some of all of the detected (" + detectedFiles + ") file entries couldn't be parsed ("
                        + parsedFiles + "), recursive delete may fail");
            }

            if (results != null) {
                RegExFileFilter fileFilter = new RegExFileFilter(entryNamePattern, RegExFileFilter.ALL, -1);
                for (FTPFile currFTPFile : results) {
                    if (currFTPFile != null) {
                        if (fileFilter.accept(currFTPFile)) {
                            if (currFTPFile.isDirectory()) {
                                result = rm(currFTPFile.getName(), ".*", optProperties); // remove all sub-directory content
                                ftpClient.changeToParentDirectory();
                                if (result) {
                                    ftpClient.removeDirectory(currFTPFile.getName());
                                    int reply = ftpClient.getReplyCode();
                                    if (FTPReply.isPositiveCompletion(reply)) {
                                        logger.debug("Remote directory " + currFTPFile.getName() + " deleted.");
                                        result = true;
                                    }
                                    else {
                                        logger.warn("FTP Server NEGATIVE response: ");
                                        logServerReply(Level.WARN);
                                    }
                                }
                            }
                            else {
                                ftpClient.deleteFile(currFTPFile.getName());
                                int reply = ftpClient.getReplyCode();
                                if (FTPReply.isPositiveCompletion(reply)) {
                                    logger.debug("Remote file " + currFTPFile.getName() + " deleted.");
                                    result = true;
                                }
                                else {
                                    logger.warn("FTP Server NEGATIVE response: ");
                                    logServerReply(Level.WARN);
                                }
                            }

                            if (!result) {
                                break;
                            }
                        }
                    }
                }
            }
            return result;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            setAutoconnect(oldAutoConnect);
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }


    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#mv(String, String,
     *      String, java.util.Map)
     */
    @Override
    public boolean mv(String remoteParentDirectory, String oldEntryName, String newEntryName, 
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        boolean result = false;
        try {
            if (remoteParentDirectory != null) {
                changeWorkingDirectory(remoteParentDirectory);
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.rename(oldEntryName, newEntryName);

            int reply = ftpClient.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {
                logger.debug("Remote entry " + oldEntryName + " renamed to " + newEntryName + ".");
                result = true;
            }
            else {
                logger.warn("FTP Server NEGATIVE response: ");
                logServerReply(Level.WARN);
            }
            return result;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    /**
     * @see it.greenvulcano.util.remotefs.RemoteManager#mkdir(String, String, java.util.Map)
     */
    @Override
    public boolean mkdir(String remoteParentDirectory, String remoteDirectory,
            Map<String, String> optProperties) throws RemoteManagerException
    {
        checkConnected();

        boolean result = false;
        try {
            if (remoteParentDirectory != null) {
                changeWorkingDirectory(remoteParentDirectory);
            }

            ftpClient.makeDirectory(remoteDirectory);
            logger.debug("Remote directory " + remoteDirectory + " created into " + ftpClient.printWorkingDirectory());

            int reply = ftpClient.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {
                logger.debug("Remote directory " + remoteDirectory + " created");
                result = true;
            }
            else {
                logger.warn("Could not create remote directory " + remoteDirectory + " (FTP Server NEGATIVE response):");
                logServerReply(Level.WARN);
            }
            return result;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Generic error", exc);
        }
        finally {
            if (isAutoconnect()) {
                disconnect();
            }
        }
    }

    /**
     * Create a new FTPSClient object
     * 
     */
    protected FTPClient createFTPClient() throws RemoteManagerException
    {
        try {
            return new FTPClient();
        }
        catch (Exception exc) {
            throw new RemoteManagerException("Error instantiating FTPClient", exc);
        }
    }

    /**
     * Changes current working directory on the remote FTP server.
     * 
     * @param remoteDirectory
     *        the new working directory
     * @throws RemoteManagerException
     *         if any error occurs
     */
    private void changeWorkingDirectory(String remoteDirectory) throws RemoteManagerException
    {
        logger.debug("Changing remote working directory to " + remoteDirectory + "...");
        try {
            ftpClient.changeWorkingDirectory(remoteDirectory);
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new RemoteManagerException("FTP server refused remote working directory change: "
                        + ftpClient.getReplyString());
            }
            logger.debug("Current working directory is: " + ftpClient.printWorkingDirectory());
        }
        catch (RemoteManagerException exc) {
            throw exc;
        }
        catch (IOException exc) {
            throw new RemoteManagerException("I/O error", exc);
        }
    }

    /**
     * Logs the most recently received FTP server reply to a client command.
     */
    private void logServerReply(Level level)
    {
        String[] ftpResponse = ftpClient.getReplyStrings();
        for (String element : ftpResponse) {
            logger.log(level, "[FTP] " + element);
        }
    }

    /**
     * @throws RemoteManagerException
     *         if not connected
     */
    protected void checkConnected() throws RemoteManagerException
    {
        if (isAutoconnect()) {
            connect();
        }
        if (!isConnected) {
            throw new RemoteManagerException("NOT connected to FTP server.");
        }

        if (!ftpClient.isConnected()) {
            isConnected = false;
            throw new RemoteManagerException("Connection to FTP server expired. Please reconnect.");
        }
    }

    /**
     * Field <code>hostType</code>.
     * 
     * @return the host type
     */
    public TargetType getHostType()
    {
        return hostType;
    }

    /*
     * (non-Javadoc)
     * @see it.greenvulcano.util.remotefs.RemoteManager#getManagerKey()
     */
    @Override
    public String getManagerKey()
    {
        return "ftp://" + username + "@" + hostname + ":" + port;
    }

    @Override
    public String getManagerKey(Map<String, String> optProperties) throws RemoteManagerException
    {
        try {
            Map<String, Object> localProps = MapUtils.convertToHMStringObject(optProperties);
            return "ftp://" + PropertiesHandler.expand(username, localProps) + "@"
                    + PropertiesHandler.expand(hostname, localProps) + ":" + port;
        }
        catch (Exception exc) {
            throw new RemoteManagerException(exc);
        }
    }

    /**
     * Returns a <code>String</code> representation of this RemoteManager object
     * in the format:<br>
     * 
     * <pre>
     * user/password@hostname:hosttype
     * </pre>
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return super.toString() + ":" + hostType;
    }

}
