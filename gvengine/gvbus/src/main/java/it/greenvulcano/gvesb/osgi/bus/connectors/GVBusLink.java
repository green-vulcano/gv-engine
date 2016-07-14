package it.greenvulcano.gvesb.osgi.bus.connectors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Dictionary;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.karaf.config.core.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.osgi.bus.BusLink;

public class GVBusLink implements  BusLink {
	private final static Logger LOG = LoggerFactory.getLogger(GVBusLink.class);

	private String busId;
	private Connection connection;	
	private Session session;
	
	private ConfigRepository configRepository;
	
	private final static Set<GVBusConnector> connectors;
	
	static {
		connectors = Collections.synchronizedSet(new LinkedHashSet<>());
	}
		
	public void setBusId(String busId) {
		LOG.debug("Configured BUS "+busId);
		this.busId = busId;		
	}
		
	public void setConfigRepository(ConfigRepository configRepository) {
		this.configRepository = configRepository;
	}
	
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		LOG.debug("Creating connection on bus ");
		try {
			connection = connectionFactory.createConnection();
			connection.start();			
			
		} catch (JMSException jmsException) {
			LOG.error("Error connecting on bus", jmsException);
		}
	}
		
	public void setConnectors(Set<GVBusConnector> connectors) {			
		GVBusLink.connectors.addAll(connectors);
	}
	
	public void init() {
		
		if (busId != null && busId.trim().length()>1 && !busId.equals("undefined")){
			try {
				
				LOG.debug("Connection to "+busId);
				createSession();
			} catch (JMSException e) {
				LOG.error("Error connecting session on queue "+busId, e);
			}
		}
	}
	
	public void destroy(){
		LOG.debug("Destroing session on bus ");
		if (session!=null) {
			try {				
				session.close();
				connection.close();
			} catch (JMSException jmsException) {
				LOG.error("Error realeasing session on queue "+busId, jmsException);
			}
		}
		
	}	
	
	@Override
	public String connect(String busId) throws IOException {
		String message = "Bus connection established";
		
		this.busId = busId;
		
		try {
			if (busId != null && busId.trim().length()>1 && !busId.equals("undefined")){
				createSession();
			
				createUserFolder();
			} else {
				String defaultConfigPath = System.getProperty("gv.app.home") + File.separator + XMLConfig.DEFAULT_FOLDER;
				XMLConfig.setBaseConfigPath(defaultConfigPath);
				XMLConfig.reloadAll();
				
				if (session!=null) {
					try {
						session.close();
					} catch (JMSException jmsException) {
						LOG.error("Error disconnecting session on queue "+busId, jmsException);
					}
				}
				message = "Bus "+busId+ " disconnected";
			}
			
			@SuppressWarnings("unchecked")
			Dictionary<String, Object> gvesbCfg = configRepository.getConfigProperties("it.greenvulcano.gvesb.bus");				 
			gvesbCfg.put("gvbus.apikey", busId);					
			configRepository.update("it.greenvulcano.gvesb.bus", gvesbCfg);
						
		} catch (Exception e) {
			LOG.error("Error connecting session on queue "+busId, e);
			
			if (session!=null) {
				try {
					session.close();
				} catch (JMSException jmsException) {
					LOG.error("Error disconnecting session on queue "+busId, jmsException);
				}
			}
			
			throw new IOException("Bus connection failed: " + e.getMessage());
		} 
		
		return message;
		
	}	
	
	private void createSession() throws JMSException {						
			
			if (session!=null) {
				session.close();
			}			
			session  =  Optional.ofNullable(connection)
								.orElseThrow(()-> new JMSException("Bus connection not ready"))
								.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			synchronized (connectors) {
				for (GVBusConnector connector : connectors) {					
					connector.connect(session, busId);
				}	
			}					
			
			notifyConnection();
			
		
	}
	
	private void createUserFolder(){
		String defaultConfigPath = System.getProperty("gv.app.home") + File.separator + XMLConfig.DEFAULT_FOLDER;
		String configurationPath = System.getProperty("gv.app.home") + File.separator + busId;
		try {
			
			File defaultConfigDir = new File(defaultConfigPath);			
			File configDir = new File(configurationPath);
						
			if(!configDir.exists()){
				configDir.mkdir();
				Files.walk(defaultConfigDir.toPath()).forEach(path -> 
				{
					try {
						Files.copy(path, configDir.toPath().resolve(path.getFileName()));
					} catch (IOException ioException) {
						LOG.error("Fail to copy "+path+" to " + configurationPath);
					}
				});
			} 
			
			XMLConfig.setBaseConfigPath(configurationPath);
			XMLConfig.reloadAll();
		
		} catch (Exception exception) {
			LOG.error("Fail to set configuration path " + configurationPath);
		}
	}
	
	private void notifyConnection() throws JMSException {	
		Queue notificationQueue = session.createQueue("instance/connection"); 
		MessageProducer notificationProducer = session.createProducer(notificationQueue);									
		TextMessage notificationMessage = session.createTextMessage(String.format("{\"token\":\"%s\"}", busId));
		notificationProducer.send(notificationMessage);
		notificationProducer.close();
		
	}	
	
}