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
