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
