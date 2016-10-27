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
package it.greenvulcano.gvesb.virtual.ws.module;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.virtual.InitializationException;

import org.w3c.dom.Node;

/**
 * ModuleHandlerFactory class
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class ModuleHandlerFactory
{
    private ModuleHandlerFactory()
    {
        // do nothing
    }

    /**
     * @param config
     * @return the <code>ModuleHandler</code> object initialized.
     * @throws InitializationException
     */
    public static ModuleHandler getModuleHandler(Node config) throws InitializationException
    {
        String type = XMLConfig.get(config, "@type", "it.greenvulcano.gvesb.virtual.ws.module.DefaultModuleHandler");
        try {
            Class<?> className = Class.forName(type);
            ModuleHandler handler = (ModuleHandler) className.newInstance();
            handler.init(config);
            return handler;
        }
        catch (Exception exc) {
            throw new InitializationException("Error initializing Module", exc);
        }
    }
}
