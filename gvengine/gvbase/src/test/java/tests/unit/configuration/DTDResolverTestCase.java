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
package tests.unit.configuration;

import java.net.URLDecoder;

import tests.unit.BaseTestCase;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;

/**
 * @version 3.0.0 Apr 18, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class DTDResolverTestCase extends BaseTestCase {
    /**
     * @throws Exception
     */
    public void testDTDResolver() throws Exception  {
    	  	
        CatalogManager catalogManager = CatalogManager.getStaticManager();
        Catalog catalog = catalogManager.getCatalog();
        
        assertEquals("file://${{gv.dtds.home}}/GVCore.dtd", URLDecoder.decode(catalog.resolveSystem("http://www.greenvulcano.com/gvesb/dtds/GVCore.dtd"), "ISO-8859-1"));
        assertEquals("file://${{gv.dtds.home}}/GVAdapters.dtd", URLDecoder.decode(catalog.resolveSystem("http://www.greenvulcano.com/gvesb/dtds/GVAdapters.dtd"), "ISO-8859-1"));
        assertEquals("file://${{gv.dtds.home}}/GVSupport.dtd", URLDecoder.decode(catalog.resolveSystem("http://www.greenvulcano.com/gvesb/dtds/GVSupport.dtd"), "ISO-8859-1"));
        assertEquals("file://${{gv.dtds.home}}/GVCommons.dtd", URLDecoder.decode(catalog.resolveSystem("http://www.greenvulcano.com/gvesb/dtds/GVCommons.dtd"), "ISO-8859-1"));
        assertEquals("file://${{gv.dtds.home}}/GVVariables.dtd", URLDecoder.decode(catalog.resolveSystem("http://www.greenvulcano.com/gvesb/dtds/GVVariables.dtd"), "ISO-8859-1"));
        assertEquals("file://${{gv.dtds.home}}/gv-jmx.dtd", URLDecoder.decode(catalog.resolveSystem("http://www.greenvulcano.com/gvesb/dtds/gv-jmx.dtd"), "ISO-8859-1"));
        assertEquals("file://${{gv.dtds.home}}/gv-log4j.dtd", URLDecoder.decode(catalog.resolveSystem("http://www.greenvulcano.com/gvesb/dtds/gv-log4j.dtd"), "ISO-8859-1"));
        assertEquals("file://${{gv.dtds.home}}/Extra/Extra.dtd", URLDecoder.decode(catalog.resolveSystem("http://www.greenvulcano.com/gvesb/dtds/Extra.dtd"), "ISO-8859-1"));
    }
}
