package it.greenvulcano.gvesb.osgi.commands;


import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import it.greenvulcano.gvesb.osgi.bus.BusLink;

@Command(scope = "gvesb", name = "vulcon-link", description = "Connect this instance of GreenVulcano ESB to VulCon.io")
@Service
public class GVConnector implements Action {
	
	@Argument(name = "apikey", description = "A VulCon.io API key", required = false, multiValued = false)
	private String uuid;

	@Reference
	private BusLink busLink;
	
	public void setBusConnector(BusLink busLink) {
		this.busLink = busLink;
	}   
	
	@Override
	public Object execute() throws Exception {
		String message = "Connecting to Vulcon.io";
		if (uuid==null || uuid.trim().equals("")) {
			message = "An API key is required";
		} else {
			message = busLink.connect(uuid);
		}
		return message;
	}

}
