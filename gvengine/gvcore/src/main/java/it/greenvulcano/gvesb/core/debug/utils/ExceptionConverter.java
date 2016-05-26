/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.debug.utils;

import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.debug.model.Variable;

/**
 * @version 3.3.0 Feb 17, 2013
 * @author GreenVulcano Developer Team
 */
public class ExceptionConverter
{
    public static final void toDebugger(Variable envVar, Throwable exception)
    {
        Variable v = new Variable("message", String.class, exception.getMessage());
        envVar.addVar(v);
        
        Throwable cause = exception.getCause();
        if (cause != null) {
            v = new Variable("cause", cause.getClass(), cause);
            envVar.addVar(v);
        }
    }
    
    public static final void toESB(Variable envVar, String varName, String varValue) throws GVException {
        Variable variable = envVar.getVar(varName);
        // TODO: to be implemented
        variable.setVar(varName, varValue);
    }
}
