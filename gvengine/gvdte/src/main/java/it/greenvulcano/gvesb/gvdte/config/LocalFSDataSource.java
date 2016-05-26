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
package it.greenvulcano.gvesb.gvdte.config;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvdte.util.UtilsException;
import it.greenvulcano.util.bin.BinaryUtils;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * This class is an implementation of the DataSource interface in the case of a
 * resource repository lying on a local file system
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class LocalFSDataSource implements DataSource
{
    private static final Logger logger               = org.slf4j.LoggerFactory.getLogger(LocalFSDataSource.class);

    /**
     * The root directory of the resource repository on the local FS
     */
    private String              localFSRepositoryHomeDir;

    /**
     * The root directory of the resource repository on the local FS
     */
    private String              formatHandlerClass;

    /**
     * The root directory of the resource repository on the local FS
     */
    private String              dataSourceName;

    /**
     * A boolean indicating if this DataSource object has been initialized
     */
    private boolean             isInitialized;

    /**
     * If true the DataSource instance cache the requested resource
     */
    private boolean             cache;

    /**
     * The DataSource instance cache of the requested resource
     */
    private Map<String, byte[]> resourceByteCache    = new HashMap<String, byte[]>();
    private Map<String, String> resourceTextCache    = new HashMap<String, String>();

    /**
     * List of resources handled by this datasource
     */
    private List<String>        localFSResourcesList = new ArrayList<String>();

    private String              workingDir           = System.getProperty("user.dir");

    /**
     *
     */
    public LocalFSDataSource()
    {
        // do nothing
    }

    /**
     * Initialize the DataSource instance
     *
     * @param node
     * @throws UtilsException
     */
    public void init(Node node) throws UtilsException
    {
        boolean isValidRoot = true;
        try {
            String reposRoot = PropertiesHandler.expand(XMLConfig.get(node, "@repositoryHome"));
            dataSourceName = XMLConfig.get(node, "@name");
            cache = XMLConfig.get(node, "@cache", "no").equals("yes");
            formatHandlerClass = XMLConfig.get(node, "@formatHandlerClass", "");
            setFormatHandlerClass(formatHandlerClass);

            if ((reposRoot == null) || reposRoot.equals("")) {
                logger.error("Local FS " + dataSourceName + " repository root directory not specified or null.");
                isValidRoot = false;
            }
            else {
                File reposRootObj = new File(reposRoot);
                if (!reposRootObj.exists()) {
                    logger.error("The specified local FS " + dataSourceName + " repository root directory does not exist");
                    isValidRoot = false;
                }
                else {
                    if (!reposRootObj.isDirectory()) {
                        logger.error("The specified local FS " + dataSourceName + " repository root directory is NOT a directory");
                        isValidRoot = false;
                    }
                }
            }

            if (!isValidRoot) {
                logger.error("Invalid local FS " + dataSourceName + " repository root directory: " + reposRoot + " working directory: " + workingDir);
                throw new UtilsException("GVDTE_LOCAL_DATASOURCE_INVALID", new String[][]{{"dataSourceName",
                        dataSourceName}});
            }

            String result = null;
            if (reposRoot.lastIndexOf('\\') != -1) {
		            result = reposRoot.replace('\\', File.separatorChar);
		        }
		        else if (reposRoot.lastIndexOf('/') != -1) {
		            result = reposRoot.replace('/', File.separatorChar);
		        }
            logger.debug("local FS " + dataSourceName + " repository root directory: " + result + " working directory: " + workingDir);
            localFSRepositoryHomeDir = result;
            isInitialized = true;
        }
        catch (UtilsException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("initLocalFSDataSource - Error initializing DataSource", exc);
            throw new UtilsException("GVDTE_XML_CONFIG_ERROR", exc);
        }
    }

    /**
     * Ritorna il nome del DataSource
     *
     * @return the name of DataSource
     *
     */
    public String getName()
    {
        return dataSourceName;
    }

    /**
     * Return a resource as a string.
     *
     * @param resourceName
     * @return the resource or null if not found
     * @throws UtilsException
     */
    public String getResourceAsString(String resourceName) throws UtilsException
    {
        if (!isInitialized) {
            throw new UtilsException("GVDTE_INITIALIZATION_ERROR", new String[][]{{"class", "LocalFSDataSource"},
                    {"key", resourceName}});
        }
        return (String) getResourceFromLocalFS(resourceName, false);
    }

    /**
     * Return a resource as a bytes array.
     *
     * @param resourceName
     * @return the resource or null if not found
     * @throws UtilsException
     */
    public byte[] getResourceAsByteArray(String resourceName) throws UtilsException
    {
        if (!isInitialized) {
            throw new UtilsException("GVDTE_INITIALIZATION_ERROR", new String[][]{{"class", "LocalFSDataSource"},
                    {"key", resourceName}});
        }
        return (byte[]) getResourceFromLocalFS(resourceName, true);
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.config.DataSource#getRepositoryHome()
     */
    public String getRepositoryHome() throws UtilsException
    {
        if (!isInitialized) {
            throw new UtilsException("GVDTE_INITIALIZATION_ERROR", new String[][]{{"class", "LocalFSDataSource"},
                    {"key", ""}});
        }
        return localFSRepositoryHomeDir;
    }

    /**
     * Save in the repository the resource as a string, with the given name.
     *
     * @param resourceName
     * @param resourceContent
     * @throws UtilsException
     *
     */
    public void setResourceAsString(String resourceName, String resourceContent) throws UtilsException
    {
        if (!isInitialized) {
            throw new UtilsException("GVDTE_INITIALIZATION_ERROR", new String[][]{{"class", "LocalFSDataSource"},
                    {"key", resourceName}});
        }
        setLocalFSResource(resourceName, resourceContent);
    }

    /**
     * Save in the repository the resource as a bytes array, with the given name.
     *
     * @param resourceName
     * @param resourceContent
     * @throws UtilsException
     *
     */
    public void setResourceAsByteArray(String resourceName, byte[] resourceContent) throws UtilsException
    {
        if (!isInitialized) {
            throw new UtilsException("GVDTE_INITIALIZATION_ERROR", new String[][]{{"resourceName", resourceName}});
        }
        setLocalFSResource(resourceName, resourceContent);
    }

    /**
     * Remove the named resource from the repository.
     *
     * @param resourceName
     * @return true is the resource has been deleted, false otherwise
     * @throws UtilsException
     *
     */
    public boolean deleteResource(String resourceName) throws UtilsException
    {
        if (!isInitialized) {
            throw new UtilsException("GVDTE_INITIALIZATION_ERROR", new String[][]{{"resourceName", resourceName}});
        }
        resourceName = handleFileSeparator(resourceName);
        boolean result = false;
        try {
            File file = getResourceAsFile(resourceName);
            logger.debug("deleteLocalFSResource - deleting file " + file.getCanonicalPath() + " on local FS... ");
            result = file.delete();
        }
        catch (Exception exc) {
            // do nothing
        }
        if (!result) {
            logger.warn("deleteLocalFSResource - Can't delete file " + resourceName + " on local FS.");
        }
        return result;
    }

    /**
     * Returns a List containing all the names of the items in the repository
     *
     * @return a List of names
     * @throws UtilsException
     *
     */
    public List<String> getResourcesList() throws UtilsException
    {
        if (!isInitialized) {
            throw new UtilsException("GVDTE_INITIALIZATION_ERROR", new String[][]{{"class", "LocalFSDataSource"},
                    {"key", ""}});
        }
        if (localFSResourcesList.isEmpty()) {
            File file = new File(localFSRepositoryHomeDir);
            localFSResourcesList = populateDirectoryContentList(localFSResourcesList, file, File.separator);
        }
        return localFSResourcesList;
    }

    /**
     * Return the Format Handler class to be used tho read maps.
     *
     * @return the format handler class
     */
    public String getFormatHandlerClass()
    {
        return formatHandlerClass;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.config.DataSource#getResourceURL(java.lang.String)
     */
    public String getResourceURL(String resourceName) throws UtilsException
    {
        resourceName = handleFileSeparator(resourceName);
        String localFilename = "";
        try {
            File file = getResourceAsFile(resourceName);
            localFilename = file.getCanonicalPath();
            return "file://" + localFilename;
        }
        catch (FileNotFoundException exc) {
            logger.error("getResourceURL - resource " + localFilename + " not found on local FS: " + exc);
            throw new UtilsException("GVDTE_LOCAL_DATASOURCE_FILE_NOT_FOUND", new String[][]{{"name", localFilename}},
                    exc);
        }
        catch (IOException exc) {
            logger.error("getResourceURL - read error on resource " + localFilename + ": " + exc);
            throw new UtilsException("GVDTE_LOCAL_DATASOURCE_IO_ERROR", new String[][]{{"name", localFilename},
                    {"cause", ""}}, exc);
        }
    }

    private void setFormatHandlerClass(String handlerClass)
    {
        formatHandlerClass = handlerClass;
    }

    /**
     * Return a resource as a String or a bytes array, reading it from local file system (or cache).
     */
    private Object getResourceFromLocalFS(String resourceName, boolean isByteArray) throws UtilsException
    {
        resourceName = handleFileSeparator(resourceName);
        String localFilename = "";
        try {
            File file = getResourceAsFile(resourceName);
            localFilename = file.getCanonicalPath();
            if (cache) {
                Object output = null;
                if (isByteArray) {
                    output = resourceByteCache.get(resourceName);
                }
                else {
                    output = resourceTextCache.get(resourceName);
                }
                if (output != null) {
                    logger.debug("Reading Resource " + resourceName + " from cache... ");
                    return output;
                }
            }

            logger.debug("getResourceFromLocalFS - resource name on local FS is: " + localFilename);
            if (isByteArray) {
                byte[] output = BinaryUtils.readFileAsBytes(localFilename);
                if (cache) {
                    logger.debug("Caching byteResource " + resourceName + " from local FS... ");
                    resourceByteCache.put(resourceName, output);
                }
                return output;
            }

            String output = TextUtils.readFile(localFilename);
            if (cache) {
                logger.debug("Caching textResource " + resourceName + " from local FS... ");
                resourceTextCache.put(resourceName, output);
            }
            return output;
        }
        catch (FileNotFoundException exc) {
            logger.error("getResourceFromLocalFS - resource " + localFilename + " not found on local FS: " + exc);
            throw new UtilsException("GVDTE_LOCAL_DATASOURCE_FILE_NOT_FOUND", new String[][]{{"name", localFilename}},
                    exc);
        }
        catch (IOException exc) {
            logger.error("getResourceFromLocalFS - read error on resource " + localFilename + ": " + exc);
            throw new UtilsException("GVDTE_LOCAL_DATASOURCE_IO_ERROR", new String[][]{{"name", localFilename},
                    {"cause", ""}}, exc);
        }
    }

    /**
     * Save on local file system the resource as a string, with the given name.
     */
    private void setLocalFSResource(String resourceName, Object resourceContent) throws UtilsException
    {
        String localFilename = "";
        resourceName = handleFileSeparator(resourceName);

        try {
            File file = new File(localFSRepositoryHomeDir, resourceName);
            localFilename = file.getCanonicalPath();
            logger.debug("setLocalFSResource - resource name on local FS is: " + localFilename);
            String parentDirName = file.getParent();
            File parentDir = new File(parentDirName);
            parentDir.mkdirs();

            file.createNewFile();
            if (resourceContent instanceof String) {
                TextUtils.writeFile((String) resourceContent, localFilename);
            }
            else if (resourceContent instanceof byte[]) {
                BinaryUtils.writeBytesToFile((byte[]) resourceContent, localFilename);
            }

        }
        catch (IOException exc) {
            logger.error("setLocalFSResource - Can't store resource into repository: ", exc);
            throw new UtilsException("GVDTE_LOCAL_DATASOURCE_IO_ERROR", new String[][]{{"name", localFilename},
                    {"cause", ",Can't store resource into repository"}}, exc);

        }
    }


    /**
     * Recursive method that explores a given directory and its subdirectory
     * tree structure and fills a List with the names of all the files found
     */
    private List<String> populateDirectoryContentList(List<String> theList, File theDir, String theDirName)
    {
        if (theDir.isDirectory() && theDir.exists()) {
            File[] contentList = theDir.listFiles();
            for (File currFile : contentList) {
                if (currFile.isFile()) {
                    String fileName = theDirName + currFile.getName();
                    logger.debug("populateDirectoryContentList - adding file " + fileName + " to file list.");
                    theList.add(fileName);
                }
                else {
                    String dirName = theDirName + currFile.getName() + File.separator;
                    populateDirectoryContentList(theList, currFile, dirName);
                }
            }
        }
        return theList;
    }

    /**
     * In the case of a repository located on a file system, replaces file
     * separator character used into the resource name with the actual file
     * separator in use on the FS
     */
    private String handleFileSeparator(String resName)
    {
        int idx = resName.lastIndexOf("://");
        if (idx != -1) {
            resName = resName.substring(idx + 3);
        }
        // windows system: remove heading /
		if (resName.contains(":")){
    		resName = resName.substring(1);
    	}
    	if (File.separator.equals("/")) {
        	resName = resName.replace('\\', File.separatorChar);
        }
        else {
        	resName = resName.replace('/', File.separatorChar);
        }
        idx = resName.indexOf(localFSRepositoryHomeDir);
        if (idx != -1) {
            resName = resName.substring(idx + localFSRepositoryHomeDir.length()+1);
        }
        if (resName.startsWith(workingDir)) {
            resName = resName.substring(workingDir.length()+1);
        }
        return resName;
    }

    private File getResourceAsFile(String resourceName) throws FileNotFoundException
    {
        File file = new File(resourceName);
        if (file.isAbsolute()) {
            return file;
        }
        return new File(localFSRepositoryHomeDir, resourceName);
    }
}
