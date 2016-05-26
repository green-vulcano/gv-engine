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
package it.greenvulcano.util.runtime;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class provides utility methods to read the value of runtime environment
 * variables. It is possible to choose between using <i>up-to-date</i> values or
 * <i>cached</i> values.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class Environment
{

    /**
     * Private environment variable map
     */
    private static Map<String, String> environmentVars;

    /**
     * Returns the <i>cached</i> value of a runtime environment value, given its
     * name.
     *
     * @param varName
     *        the name of the environment variable.
     * @return the value of the environment variable.
     * @throws IOException
     *         if any error occurs.
     */
    public static synchronized String getVariable(String varName) throws IOException
    {
        return getVariable(varName, false);
    }

    /**
     * Returns the value of a runtime environment value, given its name. If the
     * <code>refresh</code> argument is <code>true</code>, the environment
     * variable map is reloaded each time this method is called.
     *
     * @param varName
     *        the name of the environment variable.
     * @param refresh
     *        <code>true</code> if we want to force environment variable map
     *        reload.
     * @return the value of the environment variable.
     * @throws IOException
     *         if any error occurs.
     */
    public static synchronized String getVariable(String varName, boolean refresh) throws IOException
    {
        String result = null;
        boolean firstTime = (environmentVars == null);
        if (firstTime || refresh) {
            loadVariables();
        }

        result = environmentVars.get(varName);
        return result;
    }

    /**
     * Returns a <i>cached</i> set view of the names of the current runtime
     * environment variables.
     *
     * @return a set view of the names of the current runtime environment
     *         variables.
     * @throws IOException
     */
    public static synchronized Set<String> variableNamesSet() throws IOException
    {
        return variableNamesSet(false);
    }

    /**
     * Returns a set view of the names of the current runtime environment
     * variables. If the <code>refresh</code> argument is <code>true</code>, the
     * environment variable map is reloaded each time this method is called.
     *
     * @param refresh
     *        <code>true</code> if we want to force environment variable map
     *        reload.
     * @return a set view of the names of the current runtime environment
     *         variables.
     * @throws IOException
     */
    public static synchronized Set<String> variableNamesSet(boolean refresh) throws IOException
    {
        boolean firstTime = (environmentVars == null);
        if (firstTime || refresh) {
            loadVariables();
        }

        return environmentVars.keySet();
    }

    /**
     * Private environment map loader method.
     *
     * @throws IOException
     *         if any error occurs.
     */
    private static void loadVariables() throws IOException
    {
        environmentVars = new TreeMap<String, String>(System.getenv());
    }
}
