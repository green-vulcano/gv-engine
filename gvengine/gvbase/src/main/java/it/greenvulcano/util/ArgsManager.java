/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.greenvulcano.util;

import java.util.HashMap;

/**
 * ArgsManager class
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
*/
public class ArgsManager {
    /**
     * the args -> value mapping.
     */
    private HashMap<String, String> argsMap = new HashMap<String, String>();

    /**
     * Constructor.
     *
     * @param def
     *            the arguments definition representing a list of valid option
     *            ID, the IDs followed by a semicolon requires a value: ex.
     *            "sS:t:" -> -s -S <value> -t <value>
     * @param args
     *            the input arguments
     * @exception ArgsManagerException
     *                if errors occurs
     */
    public ArgsManager(String def, String[] args) throws ArgsManagerException {
        int i = 0;

        if (args.length == 0) {
            throw new ArgsManagerException("Empty arguments list.");
        }

        if (def.length() == 0) {
            throw new ArgsManagerException("Empty argument IDs list.");
        }

        while (i < args.length) {
            String argument = args[i];
            if (args[i].startsWith("-")) {
                argument = argument.substring(1);
                int idx = def.indexOf(argument);
                if (idx != -1) {
                    if ((def.length() > (idx + 1)) && (def.charAt(idx + 1) == ':')) {
                        if ((i + 1) < args.length) {
                            if (args[i + 1].charAt(0) == '-') {
                                if (def.indexOf(args[i + 1].substring(1)) != -1) {
                                    throw new ArgsManagerException("Missing value for option ID=<" + args[i] + ">");
                                }
                            }
                            argsMap.put(argument, args[i + 1]);
                            i += 2;
                        }
                        else {
                            throw new ArgsManagerException("Missing value for option ID=<" + args[i] + ">");
                        }
                    }
                    else {
                        argsMap.put(argument, "");
                        i++;
                    }
                }
                else {
                    throw new ArgsManagerException("Unexpected option ID=<" + args[i] + ">");
                }
            }
            else {
                i++;
            }
        }
    }

    /**
     * Return the string value associated to 'argument'.
     *
     * @param argument
     *            the argument to search
     * @return the argument string value
     * @throws ArgsManagerException
     *             of error occurs
     */
    public final String get(String argument) throws ArgsManagerException {
        String value = argsMap.get(argument);
        if (value == null) {
            throw new ArgsManagerException("Option ID=<-" + argument + "> not found.");
        }
        return value;
    }

    /**
     * Return the string value associated to 'argument'.
     *
     * @param argument
     *            the argument to search
     * @param defaultValue
     *            the default value
     * @return the argument string value
     */
    public final String get(String argument, String defaultValue) {
        String value = argsMap.get(argument);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Return the integer value associated to 'argument'.
     *
     * @param argument
     *            the argument to search
     * @return the argument integer value
     * @throws ArgsManagerException
     *             of error occurs
     */
    public final int getInteger(String argument) throws ArgsManagerException {
        String value = argsMap.get(argument);
        if (value == null) {
            throw new ArgsManagerException("Option ID=<-" + argument + "> not found.");
        }
        return Integer.parseInt(value);
    }

    /**
     * Return the integer value associated to 'argument'.
     *
     * @param argument
     *            the argument to search
     * @param defaultValue
     *            the default value
     * @return the argument integer value
     */
    public final int getInteger(String argument, int defaultValue) {
        String value = argsMap.get(argument);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    /**
     * Check if 'argument' exist.
     *
     * @param argument
     *            the argument to search
     * @return true if the argument exist
     */
    public final boolean exist(String argument) {
        String value = argsMap.get(argument);
        return (value != null);
    }

    /**
     * @return the number of decoded arguments
     */
    public final int getArgumentCount() {
        return argsMap.size();
    }
}
