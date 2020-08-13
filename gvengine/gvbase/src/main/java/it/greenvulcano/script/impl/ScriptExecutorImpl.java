/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.script.impl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.script.GVScriptException;
import it.greenvulcano.script.ScriptExecutor;
import it.greenvulcano.script.util.BaseContextManager;
import it.greenvulcano.script.util.ScriptCache;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.Map;
import java.util.Optional;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.5.0 06/ago/2014
 * @author GreenVulcano Developer Team
 */
public class ScriptExecutorImpl extends ScriptExecutor {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ScriptExecutorImpl.class);

    private static ScriptCache cache = ScriptCache.instance();

    private String script = null;
    private boolean initialized = false;
    
    private CompiledScript compScript = null;
    private Bindings bindings = null;

    private String name = null;

    /**
     * 
     */
    public ScriptExecutorImpl() {

        super();
    }

    /**
     * Initialize the instance.
     * 
     * @param node
     * the configuration node
     * @throws GVScriptException
     */
    public void init(Node node) throws GVScriptException {

        ClassLoader initialContextClassLoader = Thread.currentThread().getContextClassLoader();

        try {

            name = XMLConfig.get(node.getParentNode(), "@name");
            logger.debug("init script node " + name);
            lang = XMLConfig.get(node, "@lang", "js");

            if (lang.equals("js")) {

                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            }

            String file = XMLConfig.get(node, "@file", "");
            if (!"".equals(file)) {
                scriptName = file;
                script = cache.getScript(file);
            } else {
                scriptName = ScriptCache.INTERNAL_SCRIPT;
                script = XMLConfig.get(node, ".", null);
            }

            if ((script == null) || "".equals(script)) {
                throw new GVScriptException("Empty configured script!");
            }

            ScriptEngine engine = getScriptEngine(lang);
            if (engine == null) {
                throw new GVScriptException("ScriptEngine[" + lang + "] not found!");
            }

            String bcName = XMLConfig.get(node, "@base-context", null);
            String baseContext = BaseContextManager.instance().getBaseContextScript(lang, bcName);
            if (baseContext != null) {
                script = baseContext + "\n\n" + (script != null ? script : "");
            } else if (bcName != null) {
                throw new GVScriptException("BaseContext[" + lang + "/" + bcName + "] not found!");
            }

            if (engine instanceof Compilable && PropertiesHandler.isExpanded(script)) {
                String scriptKey = DigestUtils.sha256Hex(script);

                Optional<CompiledScript> cachedCompiledScript = cache.getCompiledScript(scriptKey);

                if (cachedCompiledScript.isPresent()) {
                    compScript = cachedCompiledScript.get();
                } else {
                    logger.debug("Static script[" + lang + "], can be compiled for performance");
                    compScript = ((Compilable) engine).compile(script);
                    cache.putCompiledScript(scriptKey, compScript);
                }
            }
            bindings = engine.createBindings();

            initialized = true;
        } catch (GVScriptException exc) {
            logger.error("Error initializing ScriptExecutorImpl", exc);
            throw exc;
        } catch (Exception exc) {
            logger.error("Error initializing ScriptExecutorImpl", exc);
            throw new GVScriptException("Error initializing ScriptExecutorImpl", exc);
        } finally {
            Thread.currentThread().setContextClassLoader(initialContextClassLoader);
        }
    }

    /**
     * Initialize the instance.
     * 
     * @param lang
     * script engine language
     * @param script
     * the script to configure; if null the script must be passes in the execute method; overridden by 'file'
     * @param file
     * if not null, defines the script file to read
     * @param bcName
     * defines the BaseContext to be used to enrich the script;
     * if null is used the default context for the given language, if defined
     * @throws GVScriptException
     */
    public void init(String lang, String script, String file, String bcName) throws GVScriptException {

        ClassLoader initialContextClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            this.lang = lang;
            if ( file != null && file.trim().length()>0) {
                this.script = cache.getScript(file);
            } else {
                this.script = script;               
            }
           
            if ("js".equals(lang)) {
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            }
            
            ScriptEngine engine = getScriptEngine(lang);
            if (engine == null) {
                throw new GVScriptException("ScriptEngine[" + this.lang + "] not found!");
            }
            bindings = engine.createBindings();

            String baseContext = BaseContextManager.instance().getBaseContextScript(lang, bcName);
            if (baseContext != null) {
                this.script = baseContext + "\n\n" + (this.script != null ? this.script : "");
                
            } else if (bcName != null) {
                throw new GVScriptException("BaseContext[" + this.lang + "/" + bcName + "] not found!");
            }

            if (engine instanceof Compilable && this.script != null && PropertiesHandler.isExpanded(this.script)) {
                String scriptKey = DigestUtils.sha256Hex(this.script);
                Optional<CompiledScript> cachedCompiledScript = cache.getCompiledScript(scriptKey);

                if (cachedCompiledScript.isPresent()) {
                    compScript = cachedCompiledScript.get();
                } else {
                    logger.debug("Static script[" + lang + "], can be compiled for performance");
                    compScript = ((Compilable) engine).compile(this.script);
                    cache.putCompiledScript(scriptKey, compScript);
                }
            }

            initialized = true;
        } catch (GVScriptException exc) {
            logger.error("Error initializing ScriptExecutorImpl", exc);
            throw exc;
        } catch (Exception exc) {
            logger.error("Error initializing ScriptExecutorImpl", exc);
            throw new GVScriptException("Error initializing ScriptExecutorImpl", exc);
        } finally {
            Thread.currentThread().setContextClassLoader(initialContextClassLoader);
        }
    }

    /**
     * Add the name/value pair to the script Bindings.
     * 
     * @param name
     * property name
     * @param value
     * property value
     * @throws GVScriptException
     */
    public void putProperty(String name, Object value) throws GVScriptException {

        isInitialized();
        bindings.put(name, value);
    }

    /**
     * Add all the Map entries to the script Bindings.
     * 
     * @param props
     * the properties to set
     * @throws GVScriptException
     */
    public void putAllProperties(Map<String, Object> props) throws GVScriptException {

        isInitialized();
        bindings.putAll(props);
    }

    /**
     * Read the named property from the script Bindings.
     * 
     * @param name
     * the property name
     * @return the property value, or null if not present
     * @throws GVScriptException
     */
    public Object getProperty(String name) throws GVScriptException {

        isInitialized();
        return bindings.get(name);
    }

    /**
     * Remove the named property from the script Bindings.
     * 
     * @param name
     * the property name
     * @return the previous property value, or null if not present
     * @throws GVScriptException
     */
    public Object removeProperty(String name) throws GVScriptException {

        isInitialized();
        return bindings.remove(name);
    }

    /**
     * Execute the read script. If the script uses metadata, then these are
     * resolved prior to execution.
     * 
     * @param properties
     * used as '@{{...}}' to resolve script's metadata, if used
     * @param object
     * used as 'object' to resolve script's metadata, if used
     * @return the script execution result
     * @throws GVScriptException
     */
    public Object execute(Map<String, Object> properties, Object object) throws GVScriptException {

        isInitialized();

        ClassLoader initialContextClassLoader = Thread.currentThread().getContextClassLoader();

        long start = System.currentTimeMillis();
        String localScript = script;
        try {

            if (lang.equals("js")) {

                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            }

            if (compScript != null) {

                Object res = compScript.eval(bindings);

                logger.debug("Engine[" + lang + "]  compiled script " + name + " execution time [" + (System.currentTimeMillis() - start) + "] " + " bindings size: "
                             + bindings.size() + " - result: " + res);

                return res;
            }
            if (!PropertiesHandler.isExpanded(localScript)) {
                try {
                    PropertiesHandler.enableExceptionOnErrors();
                    localScript = PropertiesHandler.expand(localScript, properties, object);
                    logger.debug("Executing script[" + lang + "]:\n" + localScript);
                } finally {
                    PropertiesHandler.disableExceptionOnErrors();
                }
            }

            Object res = getScriptEngine(lang).eval(localScript, bindings);

            logger.debug("Engine[" + lang + "]  script " + name + " execution time [" + (System.currentTimeMillis() - start) + "] " + " bindings size: " + bindings.size()
                         + " - result: " + res);

            return res;
        } catch (Exception exc) {
            logger.error("Error executing script[" + lang + "]:\n" + localScript, exc);
            throw new GVScriptException("Error executing script[" + lang + "]", exc);
        } finally {
            Thread.currentThread().setContextClassLoader(initialContextClassLoader);
        }
    }

    /**
     * Execute the passed script. If the script uses metadata, then these are
     * resolved prior to execution.
     * 
     * @param script
     * the script to execute
     * @param properties
     * used as '@{{...}}' to resolve script's metadata, if used
     * @param object
     * used as 'object' to resolve script's metadata, if used
     * @return the script execution result
     * @throws GVScriptException
     */
    public Object execute(String script, Map<String, Object> properties, Object object) throws GVScriptException {

        isInitialized();
        
        ClassLoader initialContextClassLoader = Thread.currentThread().getContextClassLoader();

        long start = System.currentTimeMillis();

        String localScript = script;
        try {
            
            if (lang.equals("js")) {

                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            }
            
            if (compScript != null) {

                Object res = compScript.eval(bindings);

                logger.debug("Engine[" + lang + "]  compiled script " + name + " execution time [" + (System.currentTimeMillis() - start) + "] " + " bindings size: "
                             + bindings.size() + " - result: " + res);
                bindings.clear();
                return res;
            }
            if (!PropertiesHandler.isExpanded(localScript)) {
                try {
                    PropertiesHandler.enableExceptionOnErrors();
                    localScript = PropertiesHandler.expand(localScript, properties, object);
                    logger.debug("Executing script[" + lang + "]:\n" + localScript);
                } finally {
                    PropertiesHandler.disableExceptionOnErrors();
                }
            }

            Object res = getScriptEngine(lang).eval(localScript, bindings);

            logger.debug("Engine[" + lang + "]  script " + name + " execution time [" + (System.currentTimeMillis() - start) + "] " + " - result: " + res);
            bindings.clear();
            return res;
        } catch (Exception exc) {
            logger.error("Error executing script[" + lang + "]:\n" + localScript, exc);
            throw new GVScriptException("Error executing script[" + lang + "]", exc);
        } finally {
            Thread.currentThread().setContextClassLoader(initialContextClassLoader);
        }
    }

    /**
     * Clean-up the script environment after every execution
     */
    public void cleanUp() {

        // do nothing
    }

    /**
     * Release allocated resources. After calling this method, the current
     * instance can't be reused.
     */
    public void destroy() {

        initialized = false;
        lang = null;
        script = null;
        compScript = null;
        bindings = null;
    }

    private void isInitialized() throws GVScriptException {

        if (!initialized) {
            throw new GVScriptException("ScriptExecutorImpl not inizialized!");
        }
    }
}
