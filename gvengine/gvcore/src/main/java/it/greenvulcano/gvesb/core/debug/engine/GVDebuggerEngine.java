package it.greenvulcano.gvesb.core.debug.engine;

import java.util.Map;
import it.greenvulcano.gvesb.core.debug.DebuggerAdapter;
import it.greenvulcano.gvesb.core.debug.DebuggerException;
import it.greenvulcano.gvesb.core.debug.GVDebugger;
import it.greenvulcano.gvesb.core.debug.model.DebuggerObject;


public class GVDebuggerEngine implements GVDebugger {

	private DebuggerAdapter debugger;	
	
	@Override
	public DebuggerObject processCommand(DebugCommand command, Map<DebugKey, String> params) throws DebuggerException {
			       	     	       
	        synchronized (this) {
	            switch (command) {
	                case CONNECT : {
	                    String service = params.get(DebugKey.service);
	                    String operation = params.get(DebugKey.operation);
	                    String version = params.get(DebugKey.debuggerVersion);
	                    debugger = new DebuggerAdapter();
	            		return debugger.connect(version, service, operation);
	                   
	                }
	                    
	                case START : 
	                    return debugger.start();
	                	                  
	                case STACK : {
	                    String threadName = params.get(DebugKey.threadName);
	                    return debugger.stack(threadName);
	                }
	                   
	                case VAR : {
	                    String stackFrame = params.get(DebugKey.stackFrame);
	                    String varEnv = params.get(DebugKey.varEnv);
	                    String varID = params.get(DebugKey.varID);
	                    String threadName = params.get(DebugKey.threadName);
	                    return debugger.var(threadName, stackFrame, varEnv, varID);
	                }
	                   
	                case SET_VAR : {
	                    String stackFrame = params.get(DebugKey.stackFrame);
	                    String varEnv = params.get(DebugKey.varEnv);
	                    String varID = params.get(DebugKey.varID);
	                    String varValue = params.get(DebugKey.varValue);
	                    String threadName = params.get(DebugKey.threadName);
	                    return debugger.set_var(threadName, stackFrame, varEnv, varID, varValue);
	                }
	                    
	                case DATA :
	                    return debugger.data();
	                   
	                case SET : {
	                    String threadName = params.get(DebugKey.threadName);
	                    String sBreakpoint = params.get(DebugKey.breakpoint);
	                    String subflow = params.get(DebugKey.subflow);
	                    return debugger.set(threadName, subflow, sBreakpoint);
	                }
	                   
	                case CLEAR : {
	                    String threadName = params.get(DebugKey.threadName);
	                    String cBreakpoint = params.get(DebugKey.breakpoint);
	                    String subflow = params.get(DebugKey.subflow);
	                    return debugger.clear(threadName, subflow, cBreakpoint);
	                }
	                    
	                case SKIP_ALL_BP : {
	                    boolean enabled = Boolean.getBoolean(params.get(DebugKey.enabled));
	                    return debugger.skipAllBreakpoints(enabled);
	                }
	                   
	                case STEP_OVER : {
	                    String threadName = params.get(DebugKey.threadName);
	                    return debugger.stepOver(threadName);
	                }
	                   
	                case STEP_INTO : {
	                    String threadName = params.get(DebugKey.threadName);
	                    return debugger.stepInto(threadName);
	                }
	                   
	                case STEP_RETURN : {
	                    String threadName = params.get(DebugKey.threadName);
	                    return debugger.stepReturn(threadName);
	                }
	                   
	                case RESUME : {
	                    String threadName = params.get(DebugKey.threadName);
	                    return debugger.resume(threadName);
	                }
	                   
	                case EXIT :	                	
	            		if (debugger != null) {
	            			DebuggerObject object = debugger.exit();
	            			debugger = null;
	            			return object;
	            		}
	            		return DebuggerObject.TERM_DEBUGGER_OBJECT;
	                   
	                case EVENT :
	                    return debugger.event();	            
	                   
	            }
	
	        }
	        
	        return DebuggerObject.FAIL_DEBUGGER_OBJECT;
		      
		}

}
