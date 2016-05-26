package it.greenvulcano.gvesb.osgi.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.greenvulcano.gvesb.GVConfigurationManager;

@Command(scope = "gvesb", name = "reload", description = "Reload all the configuration files")
@Service
public class GVReloader implements Action {

	private final Logger LOG = LoggerFactory.getLogger(getClass());
		
	@Reference
	private GVConfigurationManager configurationManager;
			
	public void setConfigurationManager(GVConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}

	@Override
	public Object execute() throws Exception {
		String message = null;		
		
		try {		
			
			configurationManager.reload();			
			message = "Configuration reloaded";
			
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
			LOG.error("GVReloader - Reload failed", exception);
			message = "Fail to reload configuration";
		}		
		
		return message;
	}

}
