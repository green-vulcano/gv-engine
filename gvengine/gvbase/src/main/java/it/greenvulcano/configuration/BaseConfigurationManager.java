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
