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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.karaf.config.core.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import it.greenvulcano.gvesb.GVConfigurationManager;

public class BaseConfigurationManager implements GVConfigurationManager {

	private final Logger LOG = LoggerFactory.getLogger(getClass());	
	private final ReentrantLock LOCK = new ReentrantLock();
	
<<<<<<< HEAD
	private ConfigRepository configRepository;
	
	public void setConfigRepository(ConfigRepository configRepository) {
		this.configRepository = configRepository;
=======
	private ConfigRepository configRepository;	
	private final List<DeployListener> deployListeners = Collections.synchronizedList(new LinkedList<>());
	
	public void setConfigRepository(ConfigRepository configRepository) {
		this.configRepository = configRepository;
	}	
	
	public void setDeployListeners(List<DeployListener> deployListeners) {
		
		this.deployListeners.clear();
		if (deployListeners!=null) {
			this.deployListeners.addAll(deployListeners);
		}
>>>>>>> 556629a... Improved role management
	}
	
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

	@Override
	public void deployConfiguration(ZipInputStream configurationArchive, Path destination) throws XMLConfigException, IllegalStateException {
		if (LOCK.tryLock()) {		
			try {												
				
				String configPath = destination.toString();
				LOG.debug("Deploy started on path "+configPath);
				ZipEntry zipEntry = null;
				
				if (Files.notExists(destination)){
					Files.createDirectories(destination);
				}
				
				while ((zipEntry=configurationArchive.getNextEntry())!=null) {
					
					Path entryPath = Paths.get(configPath, zipEntry.getName());
									
					LOG.debug("Adding resource: "+entryPath);
					if (zipEntry.isDirectory()) {
						entryPath.toFile().mkdirs();
					} else {
						
						Path parent  = entryPath.getParent();
						if(!Files.exists(parent)){
							Files.createDirectories(parent);
						}
						
						Files.copy(configurationArchive, entryPath, StandardCopyOption.REPLACE_EXISTING);					
					}
					
				}
				
				if (configPath.equals(XMLConfig.getBaseConfigPath())) {
					LOG.debug("Config merged in path: "+configPath);
				} else {
				
					@SuppressWarnings("unchecked")
					Dictionary<String, Object> gvesbCfg = Optional.of(configRepository.getConfigProperties(XMLConfig.CONFIG_PID))
																  .orElse(new Hashtable<>());
					
					gvesbCfg.put(XMLConfig.CONFIG_KEY_HOME, configPath);
					configRepository.update(XMLConfig.CONFIG_PID, gvesbCfg);
					
					//**** Deleting old config dir
					LOG.debug("Removing old config: "+XMLConfig.getBaseConfigPath());
					try {
						 File currentConfig = new File(XMLConfig.getBaseConfigPath());
						 
						 Files.walk(currentConfig.toPath(), FileVisitOption.FOLLOW_LINKS)
							  .sorted(Comparator.reverseOrder())
							  .map(java.nio.file.Path::toFile)
							  .forEach(File::delete);
					} catch (IOException e) {
						 LOG.error("Failed to delete old configuration",e); 
					}
					 				
					XMLConfig.setBaseConfigPath(configPath);
				}
				
				LOG.debug("Deploy complete");
				deployListeners.forEach(l-> l.onDeploy(destination));
								
			} catch (Exception e) {
				
				if (Objects.nonNull(destination) && Files.exists(destination)) {
					LOG.error("Deploy failed, rollback to previous configuration",e);
					try {												 
						 Files.walk(destination, FileVisitOption.FOLLOW_LINKS)
							  .sorted(Comparator.reverseOrder())
							  .map(java.nio.file.Path::toFile)
							  .forEach(File::delete);
					} catch (IOException rollbackException) {
						 LOG.error("Failed to delete old configuration",e); 
					}
				} else {
					LOG.error("Deploy failed",e);
				}
				
				throw new XMLConfigException("Deploy failed", e);
			} finally {
				LOCK.unlock();
			}
		} else {
			throw new IllegalStateException("A deploy is already in progress");
		}
	}

	@Override
	public byte[] exportConfiguration() throws XMLConfigException{
		
		Path configPath = Paths.get(XMLConfig.getBaseConfigPath());
		
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
				
		try (final ZipOutputStream zipExport = new ZipOutputStream(output)) {
			
			Predicate<Path> isSamePath = p-> p.getFileName().equals(configPath.getFileName());
			
			for (Path path : Files.walk(configPath)
								  .filter(isSamePath.negate())
								  .collect(Collectors.toList())) {
			
				ZipEntry entry = new ZipEntry(configPath.relativize(path).toString());
							
				if (Files.isRegularFile(path)) {
					zipExport.putNextEntry(entry);
					zipExport.write(Files.readAllBytes(path));	
				}
			}			
			
		} catch (IOException e) {
			throw new XMLConfigException("Fail to export configuration",e);
		}		
		
		return output.toByteArray();
	}

}
