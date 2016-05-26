package it.greenvulcano.gvesb.osgi.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import it.greenvulcano.gvesb.GVConfigurationManager;
import it.greenvulcano.gvesb.osgi.repository.GVConfigurationRepository;

@Command(scope = "gvesb", name = "deploy", description = "Deploy a project developed with VulCon.io")
@Service
public class GVDeployer implements Action {

	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Argument(name = "uuid", description = "The UUID of the project to install", required = false, multiValued = false)
	private String uuid;
	
	@Reference
	private GVConfigurationManager configurationManager;
	
	@Reference
	private GVConfigurationRepository configurationRepository;
		
	public void setConfigurationManager(GVConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}

	public void setConfigurationRepository(GVConfigurationRepository configurationRepository) {
		this.configurationRepository = configurationRepository;
	}
		
	@Override
	public Object execute() throws Exception {
		String message = null;
		if (uuid==null || uuid.trim().equals("")) {
			message = "An UUID is required to retrieve configuration";
		} else {
		
			try {
			
				Document config = configurationRepository.retrieveConfiguration(uuid);			
				configurationManager.updateConfiguration(config);
				
				message = "Configuration deployed";
				
			} catch (Exception exception) {
				System.err.println(exception.getMessage());
				LOG.error("GVDeployer - Deployment failed", exception);
				message = "Deployment failed";
			}		
		}
		return message;
	}

}
