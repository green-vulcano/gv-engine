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

/**
 * The interface defines method that must be implemented to handle configuration data.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface IConfigLoader
{
    /**
     * Initialize the instance.
     *
     * @param node
     * @throws ConfigException
     */
    public void init(String resource) throws ConfigException;

    /**
     * Return an object that complains selection criteria defined by an XPath's
     * like identifier passed as input parameter.
     *
     * @param name
     * @return the selected object
     * @throws ConfigException
     */
    public Object getData(String name) throws ConfigException;

    /**
     * Return a string array containing XPath's like paths to the objects matching
     * the input parameter.
     *
     * @param name
     * @return a string array of matching XPaths
     * @throws ConfigException
     */
    public String[] getSectionList(String name) throws ConfigException;

}
