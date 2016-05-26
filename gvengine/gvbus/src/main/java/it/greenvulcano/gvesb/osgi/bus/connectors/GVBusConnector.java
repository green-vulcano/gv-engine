package it.greenvulcano.gvesb.osgi.bus.connectors;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;

public interface GVBusConnector extends MessageListener {	
		
	void connect(Session session, String busId) throws JMSException ;
	
}
