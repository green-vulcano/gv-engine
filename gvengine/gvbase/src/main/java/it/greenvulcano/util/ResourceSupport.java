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
package it.greenvulcano.util;

import java.util.ResourceBundle;

/**
 * Used in test classes according to the following rules:
 * <ol>
 * <li>Defines you test class extending ResourceSupport
 * <li>Put in a property file the test parameters you need
 * <li>Make the property file reachable by the classpath
 * <li>The test class must call the readResources(fileName) method. <br>
 * The fileName is the parameters file without .properties extension
 * <li>Call getXXX() methods in order to read test parameters
 * </ol>
 *
 * Follows a simple test code:
 *
 * <pre>
 * public class Test extends ResourceSupport
 * {
 *     public static void main(String args[]) throws Exception
 *     {
 *         // Read the property file
 *         //
 *         readResources(&quot;testParams&quot;);
 *
 *         // Read the parameters
 *         //
 *         String xpath = getString(&quot;xpath&quot;);
 *         String numRepeat = getInt(&quot;numRepeat&quot;);
 *
 *         // Here uses the parameters
 *         // ...
 *         //
 *     }
 * }
 * </pre>
 *
 * A property file looks like:
 *
 * <pre>
 * xpath=/root/service[@name='TOUPPER']
 * numRepeat = 10
 * </pre>
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ResourceSupport
{
    private static ResourceBundle res;
    private static boolean        verbose = false;

    /**
     * @param resName
     */
    public static void readResources(String resName)
    {
        res = ResourceBundle.getBundle(resName);
    }

    /**
     * @param verbose
     */
    public static void setVerbose(boolean verbose)
    {
        ResourceSupport.verbose = verbose;
    }

    /**
     * @return if verbose mode
     */
    public static boolean getVerbose()
    {
        return verbose;
    }

    /**
     * @param resName
     * @return if resource exists
     */
    public static boolean exists(String resName)
    {
        return getString(resName, null) != null;
    }

    /**
     * @param name
     * @param def
     * @return the resolved string
     */
    public static String getString(String name, String def)
    {
        try {
            return getString(name);
        }
        catch (Exception exc) {
            return def;
        }
    }

    /**
     * @param name
     * @return the resolved string
     */
    public static String getString(String name)
    {
        String ret = res.getString(name);
        if (verbose) {
            System.out.println(name + " = " + ret);
        }
        return ret;
    }

    /**
     * @param name
     * @return an integer
     */
    public static int getInt(String name)
    {
        return Integer.parseInt(getString(name));
    }

    /**
     * @param name
     * @return a long
     */
    public static long getLong(String name)
    {
        return Long.parseLong(getString(name));
    }

    /**
     * @param name
     * @return a boolean
     */
    public static boolean getBool(String name)
    {
        return Boolean.valueOf(getString(name)).booleanValue();
    }

    /**
     * @param name
     * @return a float
     */
    public static float getFloat(String name)
    {
        return (new Float(getString(name))).floatValue();
    }

    /**
     * @param name
     * @return a double
     */
    public static double getDouble(String name)
    {
        return (new Double(getString(name))).doubleValue();
    }
}
