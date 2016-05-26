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

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.script.GVScriptException;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.BaseThread;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @version 3.5.0 08/ago/2014
 * @author GreenVulcano Developer Team
 * 
 */
public class BaseContextManager implements ConfigurationListener, ShutdownEventListener
{
    private static Logger             logger       = org.slf4j.LoggerFactory.getLogger(BaseContextManager.class);
    /**
     * XMLConfig file name.
     */
    public static final String        CFG_FILE     = "GVScriptConfig.xml";
    /**
     * Singleton reference.
     */
    private static BaseContextManager instance     = null;
    private static ScriptCache        cache        = ScriptCache.instance();
    /**
     * If true the instance is ready to work.
     */
    private boolean                   initialized  = false;
    /**
     * The engine/context map.
     */
    private Map<String, ScriptEngine> engineCtxMap = new HashMap<String, ScriptEngine>();

    public static class BaseContext
    {
        private String name;
        private String script;
        private String scriptName;

        public void init(Node node) throws GVScriptException {
            try {
                name = XMLConfig.get(node, "@name");
                String file = XMLConfig.get(node, "@file", "");
                if (!"".equals(file)) {
                    scriptName = file;
                    script = cache.getScript(file);
                }
                else {
                    scriptName = ScriptCache.INTERNAL_SCRIPT;
                    script = XMLConfig.get(node, ".", "");
                }

                /*
                 * if ((script == null) || "".equals(script)) { throw new
                 * GVScriptException("BaseContext[" + name +
                 * "] - Empty configured script!"); }
                 */
            }
            catch (GVScriptException exc) {
                throw exc;
            }
            catch (Exception exc) {
                throw new GVScriptException("BaseContext[" + name + "] - Error initializing BaseContextManager", exc);
            }
        }

        public String getName() {
            return this.name;
        }

        public String getScript() {
            return this.script;
        }

        @Override
        public String toString() {
            return "BaseContext[" + name + "] -> " + scriptName;
        }
    }

    public static class ScriptEngine
    {
        private String                   lang;
        private String                   defContextName;
        private BaseContext              defContext;
        private Map<String, BaseContext> contexts = new HashMap<String, BaseContext>();

        public void init(Node node) throws GVScriptException {
            try {
                lang = XMLConfig.get(node, "@lang");
                logger.debug("Start init ScriptEngine[" + lang + "]");

                defContextName = XMLConfig.get(node, "@default-context", null);
                NodeList nlc = XMLConfig.getNodeList(node, "BaseContext");
                for (int j = 0; j < nlc.getLength(); j++) {
                    Node nc = nlc.item(j);
                    BaseContext bc = new BaseContext();
                    bc.init(nc);
                    contexts.put(bc.getName(), bc);
                    logger.debug("Initialized: " + bc);
                }

                if (defContextName != null) {
                    defContext = contexts.get(defContextName);

                    if (defContext == null) {
                        throw new GVScriptException("ScriptEngine[" + lang + "] - Missing defaultContext["
                                + defContextName + "]");
                    }
                }

                logger.debug("End init ScriptEngine[" + lang + "]");
            }
            catch (GVScriptException exc) {
                throw exc;
            }
            catch (Exception exc) {
                throw new GVScriptException("ScriptEngine[" + lang + "] - Error initializing ScriptEngine", exc);
            }
        }

        public String getEngineName() {
            return this.lang;
        }

        public String getScript(String bcName) {
            if (bcName == null) {
                if (defContext != null) {
                    return defContext.getScript();
                }
            }
            else {
                BaseContext bc = contexts.get(bcName);
                if (bc != null) {
                    return bc.getScript();
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return "ScriptEngine[" + lang + "] -> " + contexts.keySet();
        }
    }

    /**
     * 
     */
    private BaseContextManager() {
        // do nothing
    }

    /**
     * Singleton entry-point.
     * 
     * @return the instance reference
     */
    public static synchronized BaseContextManager instance() throws GVScriptException {
        if (instance == null) {
            instance = new BaseContextManager();
            try {
                instance.init();
            }
            catch (GVScriptException exc) {
                instance = null;
                throw exc;
            }
            XMLConfig.addConfigurationListener(instance, CFG_FILE);
            ShutdownEventLauncher.addEventListener(instance);
        }
        return instance;
    }

    /**
     * Return the script to be used as base context.
     * 
     * @param lang
     *        the script engine language
     * @param bcName
     *        the base context name; if null is used the engine default base
     *        context, if defined
     * @return the script to be used as base context or null if not found
     * @throws GVScriptException
     */
    public String getBaseContextScript(String lang, String bcName) throws GVScriptException {
        if (!initialized) {
            throw new GVScriptException("BaseContextManager not inizialized!");
        }
        ScriptEngine engine = engineCtxMap.get(lang);
        if (engine != null) {
            return engine.getScript(bcName);
        }
        return null;
    }

    private void init() throws GVScriptException {
        try {
            String basePath = XMLConfig.get(CFG_FILE, "/GVScriptConfig/ScriptCache/@base-path", "");
            if (!"".equals(basePath)) {
                basePath = PropertiesHandler.expand(basePath);
                ScriptCache.setBasePath(basePath);
                logger.debug("Setting ScriptCache base directory to: " + basePath);
            }

            NodeList nle = XMLConfig.getNodeList(CFG_FILE, "/GVScriptConfig/ScriptEngines/*[@type='script-engine']");
            for (int i = 0; i < nle.getLength(); i++) {
                Node ne = nle.item(i);
                ScriptEngine en = new ScriptEngine();
                en.init(ne);
                engineCtxMap.put(en.getEngineName(), en);
                logger.debug("Initialized: " + en);
            }
            initialized = true;
        }
        catch (GVScriptException exc) {
            logger.error("Error initializing BaseContextManager", exc);
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error initializing BaseContextManager", exc);
            throw new GVScriptException("Error initializing BaseContextManager", exc);
        }
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    public void configurationChanged(ConfigurationEvent event) {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && event.getFile().equals(CFG_FILE)) {
            initialized = false;
            engineCtxMap.clear();
            cache.clearCache();
            // initialize after a delay
            Runnable rr = new Runnable() {
                @Override
                public void run()
                {
                    try {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException exc) {
                        // do nothing
                    }
                    try {
                        init();
                    }
                    catch (GVScriptException exc) {
                        logger.error("Error reloading configuration", exc);
                    }
                }
            };

            BaseThread bt = new BaseThread(rr, "Config reloader for BaseContextManager");
            bt.setDaemon(true);
            bt.start();
        }
    }

    /**
     *
     */
    @Override
    public void shutdownStarted(ShutdownEvent event) {
        destroy();
    }

    /**
     * Perform cleanup operation.
     */
    private void destroy() {
        initialized = false;
        engineCtxMap.clear();
        cache.clearCache();
        XMLConfig.removeConfigurationListener(instance);
        ShutdownEventLauncher.removeEventListener(instance);
    }
}
