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

import it.greenvulcano.script.impl.ScriptExecutorImpl;

import org.w3c.dom.Node;

/**
 *
 * @version 3.5.0 08/ago/2014
 * @author GreenVulcano Developer Team
 *
 */
public final class ScriptExecutorFactory
{

    /**
     * 
     */
    private ScriptExecutorFactory() {
        // do nothing
    }

    /**
     * Create and initialize a ScriptExecutor using the given node.
     * 
     * @param node
     *        the configuration node
     * @return a ScriptExecutor instance
     * @throws GVScriptException
     */
    public static ScriptExecutor createSE(Node node) throws GVScriptException {
        ScriptExecutor se = new ScriptExecutorImpl();
        se.init(node);
        return se;
    }
    
    /**
     * Create and initialize a ScriptExecutor using the given parameters.
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
     * @return a ScriptExecutor instance
     * @throws GVScriptException
     */
    public static ScriptExecutor createSE(String lang, String script, String file, String bcName) throws GVScriptException {
        ScriptExecutor se = new ScriptExecutorImpl();
        se.init(lang, script, file, bcName);
        return se;
    }
}
