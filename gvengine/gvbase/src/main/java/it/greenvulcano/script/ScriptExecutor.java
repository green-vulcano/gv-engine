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
package it.greenvulcano.script;

import it.greenvulcano.script.util.BaseContextManager;

import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.5.0 06/ago/2014
 * @author GreenVulcano Developer Team
 */
public abstract class ScriptExecutor
{
    private static Logger                logger          = org.slf4j.LoggerFactory.getLogger(ScriptExecutor.class);

    protected static ScriptEngineManager engManager      = new ScriptEngineManager(ClassLoader.getSystemClassLoader());

    protected String                     lang            = null;
    protected String                     scriptName      = null;

    /**
     * 
     */
    protected ScriptExecutor() {
        // do nothing
    }

    /**
     * Initialize the instance.
     * 
     * @param node
     *        the configuration node
     * @throws GVScriptException
     */
    public abstract void init(Node node) throws GVScriptException;

    /**
     * Initialize the instance.
     * 
     * @param lang
     *        script engine language
     * @param script
     *        the script to configure; if null the script must be passes in the execute method; overridden by 'file'
     * @param file
     *        if not null, defines the script file to read
     * @param bcName
     *        defines the BaseContext to be used to enrich the script;
     *        if null is used the default context for the given language, if defined
     * @throws GVScriptException
     */
    public abstract void init(String lang, String script, String file, String bcName) throws GVScriptException;

    public String getEngineName() {
        return this.lang;
    }

    public String getScriptName() {
        return this.scriptName;
    }

    /**
     * Add the name/value pair to the script Bindings.
     * 
     * @param name
     *        property name
     * @param value
     *        property value
     * @throws GVScriptException
     */
    public abstract void putProperty(String name, Object value) throws GVScriptException;


    /**
     * Add all the Map entries to the script Bindings.
     * 
     * @param props
     *        the properties to set
     * @throws GVScriptException
     */
    public abstract void putAllProperties(Map<String, Object> props) throws GVScriptException;


    /**
     * Read the named property from the script Bindings.
     * 
     * @param name
     *        the property name
     * @return the property value, or null if not present
     * @throws GVScriptException
     */
    public abstract Object getProperty(String name) throws GVScriptException;


    /**
     * Remove the named property from the script Bindings.
     * 
     * @param name
     *        the property name
     * @return the previous property value, or null if not present
     * @throws GVScriptException
     */
    public abstract Object removeProperty(String name) throws GVScriptException;

    /**
     * Execute the read script. If the script uses metadata, then these are
     * resolved prior to execution.
     * 
     * @param object
     *        used as 'object' to resolve script's metadata, if used
     * @return the script execution result
     * @throws GVScriptException
     */
    public Object execute(Object object) throws GVScriptException {
        return execute(new HashMap<String, Object>(), object);
    }

    /**
     * Execute the passed script. If the script uses metadata, then these are
     * resolved prior to execution.
     * 
     * @param script
     *        the script to execute
     * @param object
     *        used as 'object' to resolve script's metadata, if used
     * @return the script execution result
     * @throws GVScriptException
     */
    public Object execute(String script, Object object) throws GVScriptException {
        return execute(script, new HashMap<String, Object>(), object);
    }

    /**
     * Execute the read script. If the script uses metadata, then these are
     * resolved prior to execution.
     * 
     * @param properties
     *        used as '@{{...}}' to resolve script's metadata, if used
     * @param object
     *        used as 'object' to resolve script's metadata, if used
     * @return the script execution result
     * @throws GVScriptException
     */
    public abstract Object execute(Map<String, Object> properties, Object object) throws GVScriptException;

    /**
     * Execute the passed script. If the script uses metadata, then these are
     * resolved prior to execution.
     * 
     * @param script
     *        the script to execute
     * @param properties
     *        used as '@{{...}}' to resolve script's metadata, if used
     * @param object
     *        used as 'object' to resolve script's metadata, if used
     * @return the script execution result
     * @throws GVScriptException
     */
    public abstract Object execute(String script, Map<String, Object> properties, Object object) throws GVScriptException;

    /**
     * Utility method to execute the given script in the given language engine.
     * Doesn't resolve metadata.
     * 
     * @param lang
     *        script language
     * @param script
     *        script to execute
     * @param bindings
     *        name/value pairs to be set in the script context
     * @param bcName
     *        script base context; if null is used the script engine default
     * @return the script execution result
     * @throws GVScriptException
     */
    public static Object execute(String lang, String script, Map<String, Object> bindings, String bcName) throws GVScriptException {
        try {
            ScriptEngine engine = engManager.getEngineByName(lang);
            if (engine == null) {
                throw new GVScriptException("ScriptEngine[" + lang + "] not found!");
            }
            String baseContext = BaseContextManager.instance().getBaseContextScript(lang, bcName);
            if (baseContext != null) {
                script = baseContext + "\n\n" + (script != null ? script : "");
            }
            else if (bcName != null) {
                throw new GVScriptException("BaseContext[" + lang + "/" + bcName + "] not found!");
            }
            Bindings b = engine.createBindings();
            b.putAll(bindings);
            //return engine.eval(script, b);
            Object res = engine.eval(script, b);
            logger.debug("Engine[" + lang +"] result: " + res);
            if (res == null) {
                res = b.get("RESULT");
                logger.debug("Engine[" + lang +"] RESULT: " + res);
            }
            return res;
        }
        catch (Exception exc) {
            logger.error("Error executing script[" + lang + "]:\n" + script, exc);
            throw new GVScriptException("Error executing script[" + lang + "]", exc);
        }
    }

    /**
     * Clean-up the script environment after every execution
     */
    public abstract void cleanUp();

    /**
     * Release allocated resources. After calling this method, the current
     * instance can't be reused.
     */
    public abstract void destroy();
}
