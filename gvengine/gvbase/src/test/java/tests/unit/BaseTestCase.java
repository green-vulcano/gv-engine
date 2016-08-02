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
package tests.unit;

import java.io.File;

import it.greenvulcano.configuration.XMLConfig;
import junit.framework.TestCase;

public abstract class BaseTestCase extends TestCase {
	
	protected static final String BASE_DIR = "target" + File.separator + "test-classes";
	
	@Override
    protected void setUp() throws Exception {    
        super.setUp();
        XMLConfig.setBaseConfigPath(getClass().getClassLoader().getResource(".").getPath());
        System.setProperty("gv.app.home", BASE_DIR);
        System.setProperty("it.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath", "GVCore.xml|/GVCore/GVXPath/XPath");
    }

}
