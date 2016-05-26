package it.greenvulcano.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import it.greenvulcano.gvesb.GVConfigurationManager;

public class BaseConfigurationManager implements GVConfigurationManager {

	private final Logger LOG = LoggerFactory.getLogger(getClass());	
	
	@Override
	public void updateConfiguration(Document xmlConfiguration) throws XMLConfigException {
		String fileName = xmlConfiguration.getDocumentElement().getTagName().concat(".xml");
		
		LOG.debug("BaseConfigurationManager - Updating config file "+fileName);
		
		File xmlConfigurationFile =  new File(XMLConfig.getBaseConfigPath(), fileName);
		
		try {
			LOG.debug("BaseConfigurationManager - Writing "+fileName);
			writeXMLtoFile(xmlConfiguration, xmlConfigurationFile);
		} catch (IOException ioException) {
			throw new XMLConfigException("Failed to update "+xmlConfigurationFile, ioException);
		
		}
		
		LOG.debug("BaseConfigurationManager - Reloading "+fileName);
		XMLConfig.reload(fileName);

	}
	
	private void writeXMLtoFile(Document xmlConfiguration, File xmlConfigurationFile) throws IOException {
		try {
			if(!xmlConfigurationFile.exists()) {
				xmlConfigurationFile.createNewFile();
			}
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
		    DOMSource source = new DOMSource(xmlConfiguration);
		    StreamResult file = new StreamResult(Files.newOutputStream(xmlConfigurationFile.toPath(), StandardOpenOption.TRUNCATE_EXISTING));
		    transformer.transform(source, file);
		} catch (TransformerException transformerException) {
			throw new IOException(transformerException);
		}
	}

	@Override
	public void reload() throws XMLConfigException {
		XMLConfig.reloadAll();
		
	}

}
