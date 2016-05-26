/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.script.util;

import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.txt.TextUtils;

import java.io.File;
import java.util.HashMap;

/**
 * Perform a script file cache.
 * 
 * @version 3.5.0 06/ago/2014
 * @author GreenVulcano Developer Team
 */
public final class ScriptCache
{
    /**
     * #include directive.
     */
    private static final String     INCLUDE_DIR      = "#include";
    /**
     * #include directive length.
     */
    private static final int        INCLUDE_DIR_SIZE = INCLUDE_DIR.length();
    /**
     * define the default script name.
     */
    public static final String      INTERNAL_SCRIPT  = "internal";

    /**
     * Base script directory.
     */
    private static String           basePath;
    /**
     * Singleton reference.
     */
    private static ScriptCache      instance         = null;
    /**
     * Script file map.
     */
    private HashMap<String, String> scriptMap        = new HashMap<String, String>();

    static {
        try {
            basePath = PropertiesHandler.expand("sp{{gv.app.home}}" + File.separator + "scripts");
        }
        catch (PropertiesHandlerException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Constructor.
     */
    private ScriptCache() {
        // do nothing
    }

    /**
     * Singleton entry point.
     * 
     * @return the instance reference
     */
    public static synchronized ScriptCache instance() {
        if (instance == null) {
            instance = new ScriptCache();
        }
        return instance;
    }

    /**
     * Set the base script folder.
     * If the new value differs from the current value, reset the script cache.
     * 
     * @param basePath
     */
    public static void setBasePath(String basePath) {
        if (ScriptCache.basePath.equals(basePath)) {
            return;
        }
        ScriptCache.basePath = basePath;
        if (instance != null) {
            instance.clearCache();
        }
    }

    public static String getBasePath() {
        return basePath;
    }

    /**
     * Get the requested script from cache.
     * 
     * @param name
     *        the script file name
     * @return the requested script
     * @throws Exception
     *         if error occurs
     */
    public synchronized String getScript(String name) throws Exception {
        String script = scriptMap.get(name);

        if (script == null) {
            script = readScript(name);
            scriptMap.put(name, script);
        }

        return script;
    }

    /**
     * Read the requested script from $gv.app.home/scripts path.
     * 
     * @param name
     *        the script file name
     * @return the requested script
     * @throws Exception
     *         if error occurs
     */
    private String readScript(String name) throws Exception {
        String script = TextUtils.readFile(ScriptCache.getBasePath() + File.separator + name);
        if (script.indexOf(INCLUDE_DIR) != -1) {
            StringBuilder sb = new StringBuilder(script);
            handleImport(sb);
            return sb.toString();
        }
        return script;
    }

    /**
     * Called by BaseContextManager on configuration reload.
     */
    public synchronized void clearCache() {
        scriptMap.clear();
    }

    /**
     * Resolve '#include' directive.
     * 
     * @param sb
     *        current script to process.
     * @throws Exception
     */
    private void handleImport(StringBuilder sb) throws Exception {
        int idx = sb.indexOf(INCLUDE_DIR);
        while (idx != -1) {
            int start = sb.indexOf("\"", idx + INCLUDE_DIR_SIZE) + 1;
            int stop = sb.indexOf("\"", start);
            String name = sb.substring(start, stop);
            String include = readScript(name);
            sb.replace(idx, sb.indexOf("\n", stop), include);
            idx = sb.indexOf(INCLUDE_DIR);
        }
    }
}
