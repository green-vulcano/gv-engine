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
package it.greenvulcano.gvesb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipInputStream;

import it.greenvulcano.configuration.XMLConfigException;

public interface GVConfigurationManager {
	
	public final static class Authority {
		
		public final static String ADMINISTRATOR = "gvadmin";
		public final static String MANAGER = "gvmanager_config";			
		public final static String GUEST = "gvguest_config";
		
		public final static Set<String> entries;
		
		static {
			Set<String> e = new LinkedHashSet<>();
			e.add(ADMINISTRATOR);
			e.add(MANAGER);			
			e.add(GUEST);
			
			entries = Collections.unmodifiableSet(e);
		}
		
	}
	
	String getCurrentConfigurationName();
	
	byte[] export(String name) throws IOException, FileNotFoundException;
	
	Set<File> getHistory() throws IOException;
	
	void install(String name, ZipInputStream archive) throws IOException;
	
	byte[] extract(String name, String entry);
	
	void delete(String name) throws IOException, FileNotFoundException;
	
	void deploy(String name) throws XMLConfigException, FileNotFoundException;	
	
	void reload() throws XMLConfigException;
		
	public static interface DeployListener {
		
		void onDeploy(Path destination);
	}

}
