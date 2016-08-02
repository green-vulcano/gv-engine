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
package it.greenvulcano.gvesb.core.debug;

import java.util.Map;

import it.greenvulcano.gvesb.core.debug.model.DebuggerObject;

public interface GVDebugger {

	public enum DebugCommand {
	    CONNECT, START, STACK, VAR, SET_VAR, DATA, SET, CLEAR, STEP_OVER, STEP_INTO, STEP_RETURN, RESUME, EXIT, EVENT, SKIP_ALL_BP
	}

	public enum DebugKey {
        service, operation, debuggerVersion, threadName, stackFrame, varEnv, varID, varValue, breakpoint, subflow, enabled
    }

	public DebuggerObject processCommand(DebugCommand command, Map<DebugKey, String> params) throws DebuggerException;
	
}
