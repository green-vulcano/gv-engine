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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.karaf.config.core.ConfigRepository;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.GVConfigurationManager;

public class BaseConfigurationManager implements GVConfigurationManager {

	private final Logger LOG = LoggerFactory.getLogger(getClass());	
	private final ReentrantLock LOCK = new ReentrantLock();
	
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
	}
	
	@Override
	public String getCurrentConfigurationName() {
		Path current = Paths.get(XMLConfig.getBaseConfigPath());
		
		return  current.getFileName().toString();
	}

	@Override
	public void reload() throws XMLConfigException {
		XMLConfig.reloadAll();
		
	}	

	@Override
	public byte[] export(String name) throws IOException, FileNotFoundException{
		
		Path configurationPath = getConfigurationPath(name);
		
		if (Files.exists(configurationPath) && !Files.isDirectory(configurationPath)) {
			return Files.readAllBytes(configurationPath);
		} else {
			throw new FileNotFoundException(configurationPath.toString());
		}
	}

	@Override
	public Set<File> getHistory() throws IOException {
		Path history = getHistoryPath();
		if (Files.exists(history)) {
			
			Path currentConfigArchive = getConfigurationPath(getCurrentConfigurationName());
			Predicate<Path> currentConfig = p -> {
				try {
					return Files.isSameFile(p, currentConfigArchive);
				} catch (IOException e) {
					return false;
				}
			};
			
			return Files.list(history).filter(currentConfig.negate()).map(Path::toFile).collect(Collectors.toSet());			
		}
		
		return new LinkedHashSet<>();
	}

	@Override
	public void install(String name, byte[] archive) throws IOException {
		
		Path configurationPath = getConfigurationPath(name);				
		try (ZipInputStream zipArchive = new ZipInputStream( new ByteArrayInputStream(archive))){
			if (zipArchive.getNextEntry()!=null) {
				Files.write(configurationPath, archive, StandardOpenOption.WRITE, 
									                     StandardOpenOption.CREATE,
									                     StandardOpenOption.TRUNCATE_EXISTING);
			} else {
				throw new IOException("Empty or invalid zip archive");
			}
			
		}
				
	}

	@Override
	public void delete(String name) throws IOException, FileNotFoundException {
		Path configurationPath = getConfigurationPath(name);
		if (!Files.deleteIfExists(configurationPath)) {
			throw new FileNotFoundException(configurationPath.toString());
		}
	}
	
	@Override
	public byte[] extract(String name, String entry) {
		
		Path configurationArchivePath = getConfigurationPath(name);
		
		try (ZipInputStream configurationArchive = new ZipInputStream(Files.newInputStream(configurationArchivePath, StandardOpenOption.READ))) {												
					
			ZipEntry zipEntry = null;
			while ((zipEntry=configurationArchive.getNextEntry())!=null) {
				
				if (zipEntry.getName().equals(entry)) {
					byte[] entryData = IOUtils.toByteArray(configurationArchive);
					return entryData;
				}
			}
		} catch (Exception e) {
			LOG.error("Failed to extract entry "+entry+ " from archive "+configurationArchivePath, e);
		}
		
		return new byte[]{};
	}
	
	@Override
	public Properties getXMLConfigProperties() throws FileNotFoundException, IOException {
			
		Path xmlConfigPath = Paths.get(XMLConfig.getBaseConfigPath(), "XMLConfig.properties");
		
		if (Files.exists(xmlConfigPath)) {
			Properties properties = new Properties();
			properties.load(Files.newInputStream(xmlConfigPath, StandardOpenOption.READ));
			return properties;
		} else {
			throw new FileNotFoundException("XMLConfig.properties");
		}	
    	
    }
	  
	@Override
	public synchronized void saveXMLConfigProperties(Properties xmlConfigProperties) throws IOException {
			
		if (xmlConfigProperties!=null) {
			
			try (OutputStream xmlConfigPropertiesOutputStream = Files.newOutputStream(Paths.get(XMLConfig.getBaseConfigPath(), "XMLConfig.properties"), 
					                                                                  StandardOpenOption.WRITE, 
					                                                                  StandardOpenOption.CREATE,
					                                                                  StandardOpenOption.TRUNCATE_EXISTING)) {
				xmlConfigProperties.store(xmlConfigPropertiesOutputStream, null);
			}			
		}		
	}

	@Override
	public void deploy(String name) throws XMLConfigException, FileNotFoundException {
		
		Path configurationArchivePath = getConfigurationPath(name);
				
		Path current = Paths.get(XMLConfig.getBaseConfigPath());		
		Path staging = current.getParent().resolve("deploy");
		Path destination = current.getParent().resolve(name);
		
		if (LOCK.tryLock()) {
			
			if (Files.exists(configurationArchivePath) && !Files.isDirectory(configurationArchivePath)) {	
			
				try {												
					
					ZipInputStream configurationArchive = new ZipInputStream(Files.newInputStream(configurationArchivePath, StandardOpenOption.READ));
					
				
					LOG.debug("Starting deploy of configuration "+name);
					ZipEntry zipEntry = null;				
					
					for (Path cfgFile : Files.walk(current).collect(Collectors.toSet())){
						
						if (!Files.isDirectory(cfgFile)) {
							
							Path target =  staging.resolve(current.relativize(cfgFile));							
							Files.createDirectories(target);
							
							Files.copy(cfgFile, target, StandardCopyOption.REPLACE_EXISTING );
						}
						
					}				
					
					LOG.debug("Staging new config "+name);
					
					while ((zipEntry=configurationArchive.getNextEntry())!=null) {
						
						Path entryPath = staging.resolve(zipEntry.getName());
										
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
									
					//**** Deleting old config dir
					LOG.debug("Removing old config: "+current);
					Files.walk(current, FileVisitOption.FOLLOW_LINKS)
						 .sorted(Comparator.reverseOrder())
						 .map(java.nio.file.Path::toFile)
						 .forEach(File::delete);
					
					LOG.debug("Deploy new config "+name + " in path " + destination);
					Files.move(staging, destination, StandardCopyOption.ATOMIC_MOVE);
					
					setXMLConfigBasePath(destination.toString());				
					LOG.debug("Deploy complete");
					deployListeners.forEach(l-> l.onDeploy(destination));
									
				} catch (Exception e) {
					
					if (Objects.nonNull(staging) && Files.exists(staging)) {
						LOG.error("Deploy failed, rollback to previous configuration",e);
						try {												 
							 Files.walk(staging, FileVisitOption.FOLLOW_LINKS)
								  .sorted(Comparator.reverseOrder())
								  .map(java.nio.file.Path::toFile)
								  .forEach(File::delete);
							 
							 setXMLConfigBasePath(current.toString());	
						} catch (IOException | InvalidSyntaxException rollbackException) {
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
				throw new FileNotFoundException(configurationArchivePath.toString());
			}
		} else {
			throw new IllegalStateException("A deploy is already in progress");
		}
		
	}
	
	private void setXMLConfigBasePath(String path) throws IOException, InvalidSyntaxException{
		@SuppressWarnings("unchecked")
		Map<String, Object> gvesbCfg = Optional.ofNullable(configRepository.getConfig(XMLConfig.CONFIG_PID))
		                                                                  .map(Map.class::cast)
									          .orElse(new HashMap<String, Object>());
		
		gvesbCfg.put(XMLConfig.CONFIG_KEY_HOME, path);
		configRepository.update(XMLConfig.CONFIG_PID, gvesbCfg);
							 				
		XMLConfig.setBaseConfigPath(path);
	}
	
	private Path getConfigurationPath(String name) {
		return getHistoryPath().resolve(name.concat(".zip"));
	}
	
	private Path getHistoryPath(){
		return Paths.get(XMLConfig.getBaseConfigPath()).getParent().resolve("history");
	}

}
