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

import it.greenvulcano.gvesb.gvdte.util.UtilsException;

import java.util.List;

import org.w3c.dom.Node;

/**
 * This interface must be implemented by Java classes that allow interactions
 * with a Resource Repository. It contains methods that can handle the
 * resource's content as a Java String or as an array of bytes
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface DataSource
{

    /**
     * Initializes the DataSource object using right properties
     *
     * @param node
     *        Information loaded into DataSource
     * @throws UtilsException
     *         when errors occur.
     */
    public void init(Node node) throws UtilsException;

    /**
     * Return into a string the FormatHandlerClass value
     *
     * @return A String that contain the dataSource name
     *
     */
    public String getName();

    /**
     * Return into a string the FormatHandlerClass value
     *
     * @return A String that contain the FormatHandlerClass that manage maps
     *         transformation
     */
    public String getFormatHandlerClass();

    /**
     * Return into a string the resource value or if don't find resource
     *
     * @return A String that contain resource value or null if don't find
     *         resource
     * @param resourceName
     *        the name of the repository that must be find
     * @throws UtilsException
     *         when errors occur.
     */
    public String getResourceAsString(String resourceName) throws UtilsException;

    /**
     * @return the repository home path
     * @throws UtilsException
     */
    public String getRepositoryHome() throws UtilsException;

    /**
     * Save into repository the resource value (String format) and join it the
     * name specify
     *
     * @param resourceName
     * @param resourceContent
     *
     * @throws UtilsException
     *         when errors occur.
     */
    public void setResourceAsString(String resourceName, String resourceContent) throws UtilsException;

    /**
     * Return a bytes array that contain value of resource or null if don't find
     * resource
     *
     *
     * @return A Bytes array that contain value of resource or null if don't
     *         find resource
     * @param resourceName
     *        the name of the repository that must be find
     * @throws UtilsException
     *         when errors occur.
     */
    public byte[] getResourceAsByteArray(String resourceName) throws UtilsException;

    /**
     * Save into repository the resource value (Bytes array format) and join it
     * the name specify
     *
     * @param resourceName
     *        the name of the repository that must be saved
     * @param resourceContent
     *        A Bytes array that must be saved into Repository
     * @throws UtilsException
     *         when errors occur
     */
    public void setResourceAsByteArray(String resourceName, byte[] resourceContent) throws UtilsException;

    /**
     * Delete the resource specify from repository. Returns true if the resource
     * have been deleted with success, else false
     *
     * @param resourceName
     *        the name of the repository that must be saved
     * @return true if the resource have been deleted with success, otherwise
     *         false
     * @throws UtilsException
     *         when errors occur.
     */
    public boolean deleteResource(String resourceName) throws UtilsException;

    /**
     * Returns a List containing all the names of the items in the repository
     *
     * @return a List containing all the names of the items in the repository
     *
     * @throws UtilsException
     *         when errors occur.
     */
    public List<String> getResourcesList() throws UtilsException;

    /**
     * @param resourceName
     * @return the resource URL
     * @throws UtilsException
     */
    public String getResourceURL(String resourceName) throws UtilsException;
}
