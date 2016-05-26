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
package it.greenvulcano.gvesb.internal.jaas;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.internal.GVInternalException;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;

import javax.security.auth.Subject;

import org.w3c.dom.Node;

/**
 * This class creates the subject builder object.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public abstract class SubjectBuilder
{
    /**
     * This method creates the SubjectBuilder object.
     *
     * @param node
     *        the subject builder node on the configuration file to read the
     *        class name value
     * @return sBuilderClass The SubjectBuilder object
     * @throws GVInternalException
     *         if an error occurs
     */
    public static SubjectBuilder getSubjectBuilder(Node node) throws GVInternalException
    {
        String sBuilderClass = "";
        if (node == null) {
            throw new GVInternalException("JAAS_INIT_ERROR",
                    new String[][]{{"message", "The input Node can't be null"}});
        }

        sBuilderClass = XMLConfig.get(node, "@class", "");

        if (sBuilderClass.equals("")) {
            throw new GVInternalException("JAAS_INIT_ERROR", new String[][]{{"message",
                    "The '@class' attribute must be valid"}});
        }

        try {
            SubjectBuilder sBuilder = (SubjectBuilder) Class.forName(sBuilderClass).newInstance();
            sBuilder.init(node);
            return sBuilder;
        }
        catch (Exception exc) {
            throw new GVInternalException("JAAS_CLASS_INIT_ERROR", new String[][]{{"class", sBuilderClass},
                    {"exc", exc.getMessage()}});
        }
    }

    /**
     * This method creates the SubjectBuilder object.
     *
     * @param properties
     *        The map to read the class value to instance
     * @return sBuilderClass The SubjectBuilder object
     * @throws GVInternalException
     *         if an error occurs
     */
    public static SubjectBuilder getSubjectBuilder(Map<String, String> properties) throws GVInternalException
    {
        String sBuilderClass = "";
        if (properties == null) {
            throw new GVInternalException("JAAS_INIT_ERROR", new String[][]{{"message",
                    "The input Hashtable can't be null"}});
        }

        sBuilderClass = properties.get("class");

        if (sBuilderClass.equals("")) {
            throw new GVInternalException("JAAS_INIT_ERROR", new String[][]{{"message",
                    "The 'class' property must be valid"}});
        }

        try {
            SubjectBuilder sBuilder = (SubjectBuilder) Class.forName(sBuilderClass).newInstance();
            sBuilder.init(properties);
            return sBuilder;
        }
        catch (Exception exc) {
            throw new GVInternalException("JAAS_CLASS_INIT_ERROR", new String[][]{{"class", sBuilderClass},
                    {"exc", exc.getMessage()}});
        }
    }

    /**
     * Initializes the environment reading security values from the
     * configuration file.
     *
     * @param node
     *        the SubjectBuilder node
     * @throws GVInternalException
     *         if an error occurs
     */
    public abstract void init(Node node) throws GVInternalException;

    /**
     * Initializes the environment reading security values from the
     * configuration file.
     *
     * @param properties
     *        the map to read the security values
     * @throws GVInternalException
     *         if an error occurs
     */
    public abstract void init(Map<String, String> properties) throws GVInternalException;

    /**
     * This method returns the Subject object.
     *
     * @return Subject The subject object
     */
    public abstract Subject getSubject();

    /**
     * This method authenticates the instance to access.
     *
     * @throws GVInternalException
     */
    public abstract void login() throws GVInternalException;

    /**
     * This method authenticates the instance to access.
     *
     * @param force
     *        When the value is TRUE execute the authentication else (FALSE) the
     *        instance was already authenticated.
     * @throws GVInternalException
     *         if an error occurs
     */
    public abstract void login(boolean force) throws GVInternalException;

    /**
     * This method executes the logout.
     *
     * @throws GVInternalException
     *         if an error occurs
     */
    public abstract void logout() throws GVInternalException;

    /**
     * This method gives the privileges to access.
     *
     * @param action
     *        The PrivilegedExceptionAction object
     * @return Object the object
     * @throws GVInternalException
     *         if an error occurs
     * @throws PrivilegedActionException
     *         if an error occurs
     */
    @SuppressWarnings("unchecked")
    public abstract Object runAs(PrivilegedExceptionAction action) throws GVInternalException,
            PrivilegedActionException;
}