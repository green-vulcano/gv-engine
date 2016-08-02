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
package tests.unit.gvdte;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvdte.controller.DTEController;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.skyscreamer.jsonassert.JSONAssert;
import org.w3c.dom.Node;

/**
 * GVDTETest class
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class GVDTETestCase extends XMLTestCase
{
    private static final String  TEST_STRING                = "GreenVulcano rules! ;)";
    private static final String  EXPECTED_CCHAR             = "Gr44nVu1can0 ru14s! ;)";

    private static final String  TEST_DOM                   = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><doc><child>child text</child></doc>";
    private static final String  EXPECTED_DOM               = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><out><node1>child text</node1><node2>child text</node2></out>";

    private static final String  TEST_DOM_DEFNS             = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><doc xmlns=\"http://www.greenvulcano.it/greenvulcano\"><child>child text</child></doc>";
    private static final String  EXPECTED_DOM_DEFNS         = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><out><node>child text</node></out>";

    private static final String  TEST_DOM_VAL               = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><doc xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"doc.xsd\"><child>child text</child></doc>";
    
    private static final String  EXPECTED_DOM_SAXON_VAL     = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><math><sum>$291.50</sum><mean>$72.88</mean><min>$39.95</min><max>$129.95</max></math>";

    private static final String  TEST_TEXT2XML              = "name1;value1\nname2;value2\nname3;value3";
    private static final String  EXPECTED_DOM_TEXT2XML      = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><list><entry name=\"name1\" value=\"value1\"/><entry name=\"name2\" value=\"value2\"/><entry name=\"name3\" value=\"value3\"/></list>";

    private static final String  TEST_BIN2XML               = "Pinco Pallino       Some City           Some Street, 25                                   ";
    private static final String  EXPECTED_DOM_BIN2XML       = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><list><entry city=\"Some City\" name=\"Pinco Pallino\" street=\"Some Street, 25\"/></list>";

    private static final String  TEST_DOM_EXTENSION         = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><date formatIn=\"dd/MM/yyyy HH:mm:SS\" formatOut=\"yyyyMMddHHmmSS\">11/07/2000 12:50:30</date>";
    private static final String  EXPECTED_DOM_EXTENSION     = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><out>20000711125030</out>";

    private static final String  TEST_CSV2XML               = "field1.1,field1.   2,   field1.3    \r\nfield2.1,field2.2,field2.3";
    private static final String  EXPECTED_DOM_CSV2XML       = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><RowSet><row><col>field1.1</col><col>field1.   2</col><col>   field1.3    </col></row><row><col>field2.1</col><col>field2.2</col><col>field2.3</col></row></RowSet>";

    private static final String  TEST_CSV2XML_EMPTY         = "field1.1,,field1.3\r\n,field2.2,field2.3";
    private static final String  EXPECTED_DOM_CSV2XML_EMPTY = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><RowSet><row><col>field1.1</col><col/><col>field1.3</col></row><row><col/><col>field2.2</col><col>field2.3</col></row></RowSet>";

    private static final String  TEST_CSV2XML_GRP           = "field1.1,field1.2,field1.3,field1.4\r\nfield1.1,field2.2,field1.3,field2.4\r\nfield3.1,field3.2,field3.3,field3.4";
    private static final String  EXPECTED_DOM_CSV2XML_GRP   = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><RowSet><data key_1=\"field1.1\" key_3=\"field1.3\"><row><col>field1.2</col><col>field1.4</col></row><row><col>field2.2</col><col>field2.4</col></row></data><data key_1=\"field3.1\" key_3=\"field3.3\"><row><col>field3.2</col><col>field3.4</col></row></data></RowSet>";

    private static final String BASE_DIR = "target" + File.separator + "test-classes";
    private static DTEController controller                 = null;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        System.setProperty("gv.app.home", BASE_DIR);
        XMLConfig.setBaseConfigPath(getClass().getClassLoader().getResource(".").getPath());
        XMLUnit.setIgnoreWhitespace(true);
        String cfgFileName = "GVDataTransformation.xml";
        controller = new DTEController(cfgFileName);
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (controller != null) {
            controller.destroy();
        }
        super.tearDown();
    }

    /**
     * Test the ChangeCharTransformer.
     * 
     * @throws Exception
     */
    public void testChangeChar() throws Exception
    {
        Object output = controller.transform("ChangeChar", TEST_STRING.getBytes(), null);
        assertEquals(EXPECTED_CCHAR, new String((byte[]) output));
    }

    /**
     * Test the XSLTransformer.
     * 
     * @throws Exception
     */
    public void testXSLIdentity() throws Exception
    {
        Object output = controller.transform("identityXML", TEST_DOM.getBytes(), null);
        String dom = XMLUtils.serializeDOM_S((Node) output);
        assertXMLEqual("testXSLIdentity failed", TEST_DOM, dom);
    }

    /**
     * Test the XSLTransformer.
     * 
     * @throws Exception
     */
    public void testXSLInclude() throws Exception
    {
        Object output = controller.transform("TestInclude", TEST_DOM.getBytes(), null);
        String dom = XMLUtils.serializeDOM_S((Node) output);
        assertXMLEqual("testXSLInclude failed", EXPECTED_DOM, dom);
    }

    /**
     * Test the XSLTransformer.
     * 
     * @throws Exception
     */
    public void testXSLDefNS() throws Exception
    {
        //Object output = controller.transform("TestDefNs", TEST_DOM_DEFNS.getBytes(), null);
        Object output = controller.transform("TestDefNs", XMLUtils.parseObject_S(TEST_DOM_DEFNS, false, true), null);
        String dom = XMLUtils.serializeDOM_S((Node) output);
        assertXMLEqual("testXSLDefNS failed", EXPECTED_DOM_DEFNS, dom);
    }

    /**
     * Test the XSLTransformer.
     * 
     * @throws Exception
     */
    public void testXSLValidation() throws Exception
    {
        Object output = controller.transform("identityXMLValidating", TEST_DOM_VAL.getBytes(), null);
        String dom = XMLUtils.serializeDOM_S((Node) output);
        assertXMLEqual("testXSLValidation failed", TEST_DOM_VAL, dom);
    }

    /**
     * Test the XSLTransformer.
     * 
     * @throws Exception
     */
    public void testXSLSaxon() throws Exception
    {
        Object output = controller.transform("TestSaxon", readFileFromCP("bib.xml"), null);
        String dom = XMLUtils.serializeDOM_S((Node) output);
        assertXMLEqual("testXSLSaxon failed", EXPECTED_DOM_SAXON_VAL, dom);
    }

    /**
     * Test the SequenceTransformer.
     * 
     * @throws Exception
     */
    public void testText2XML() throws Exception
    {
        Object output = controller.transform("TestTextList2XML", TEST_TEXT2XML.getBytes(), null);
        String dom = XMLUtils.serializeDOM_S((Node) output);
        assertXMLEqual("testText2XML failed", EXPECTED_DOM_TEXT2XML, dom);
    }

    /**
     * Test the Bin2XMLTransformer.
     * 
     * @throws Exception
     */
    public void testBin2XML() throws Exception
    {
        Object output = controller.transform("Bin2XML", TEST_BIN2XML.getBytes(), null);
        String dom = XMLUtils.serializeDOM_S((Node) output);
        assertXMLEqual("testBin2XML failed", EXPECTED_DOM_BIN2XML, dom);
    }

    /**
     * Test the XSLTransformer + Java.
     * 
     * @throws Exception
     */
    public void testExtension() throws Exception
    {
        Object output = controller.transform("TestExtension", TEST_DOM_EXTENSION.getBytes(), null);
        String dom = XMLUtils.serializeDOM_S((Node) output);
        assertXMLEqual("testExtension failed", EXPECTED_DOM_EXTENSION, dom);
    }

    /**
     * Test the CSV2XMLTransformer.
     * 
     * @throws Exception
     */
    public void testCSV2XML() throws Exception
    {
        Object output = controller.transform("CSV2XML", TEST_CSV2XML.getBytes(), null);
        String dom = XMLUtils.serializeDOM_S((Node) output);
        assertXMLEqual("testCSV2XML failed", EXPECTED_DOM_CSV2XML, dom);
    }

    /**
     * Test the CSV2XMLTransformer.
     * 
     * @throws Exception
     */
    public void testCSV2XML_EMPTY() throws Exception
    {
        Object output = controller.transform("CSV2XML", TEST_CSV2XML_EMPTY.getBytes(), null);
        String dom = XMLUtils.serializeDOM_S((Node) output);
        assertXMLEqual("testCSV2XML_EMPTY failed", EXPECTED_DOM_CSV2XML_EMPTY, dom);
    }

    /**
     * Test the CSV2XMLTransformer.
     * 
     * @throws Exception
     */
    public void testCSV2XML_GRP() throws Exception
    {
        Object output = controller.transform("CSV2XML_GRP", TEST_CSV2XML_GRP.getBytes(), null);
        String dom = XMLUtils.serializeDOM_S((Node) output);
        assertXMLEqual("testCSV2XML_GRP failed", EXPECTED_DOM_CSV2XML_GRP, dom);
    }

    /**
     * Test the XQTransformer.
     * 
     * @throws Exception
     */
    public void testXQ() throws Exception
    {
        Object output = controller.transform("TestXQuery", readFileFromCP("bib.xml"), null);
        String dom = XMLUtils.serializeDOM_S((Node) output);
        String outXML = readFileFromCP("bib_filter.out");
        //System.out.println("\nTestXQuery: " + dom);
        assertXMLEqual("testXQ failed", outXML, dom);
    }
    
    /**
     * Test the XML2JSONTransformer.
     * 
     * @throws Exception
     */
    public void testXML2JSON() throws Exception
    {
        Object output = controller.transform("TestXml2Json", readFileFromCP("bib.xj"), null);
        String json = (String) output;
        String outJSON = readFileFromCP("bib.json");
        //System.out.println("\nTestXml2Json: " + json);
        JSONAssert.assertEquals(outJSON, json, true);
    }

    /**
     * Test the JSON2XMLTransformer.
     * 
     * @throws Exception
     */
    public void testJSON2XML() throws Exception
    {
        Object output = controller.transform("TestJson2Xml", readFileFromCP("bib.json"), null);
        String dom = XMLUtils.serializeDOM_S((Node) output);
        String outXML = readFileFromCP("bib.xj");
        //System.out.println("TestJson2Xml: " + dom);
        assertXMLEqual("TestJson2Xml failed", outXML, dom);
    }
    
    /**
     * Test the XML2JSONTransformer BadgerFish.
     * 
     * @throws Exception
     */
    public void testXML2JSON_BadgerFish() throws Exception
    {
        Object output = controller.transform("TestXml2Json_BadgerFish", readFileFromCP("bib.xj"), null);
        String json = (String) output;
        String outJSON = readFileFromCP("bib_BadgerFish.json");
        //System.out.println("\nTestXml2Json_BadgerFish: " + json);
        JSONAssert.assertEquals(outJSON, json, true);
    }

    /**
     * Test the JSON2XMLTransformer BadgerFish.
     * 
     * @throws Exception
     */
    public void testJSON2XML_BadgerFish() throws Exception
    {
        Object output = controller.transform("TestJson2Xml_BadgerFish", readFileFromCP("bib_BadgerFish.json"), null);
        String dom = XMLUtils.serializeDOM_S((Node) output);
        String outXML = readFileFromCP("bib.xj");
        //System.out.println("TestJson2Xml_BadgerFish: " + dom);
        assertXMLEqual("TestJson2Xml_BadgerFish failed", outXML, dom);
    }

    /**
     * Test the JSON2XMLTransformer.
     * 
     * @throws Exception
     */
    public void testJSON2XML_attr() throws Exception
    {
        Object output = controller.transform("TestJson2Xml_attr", readFileFromCP("bib.json"), null);
        String dom = XMLUtils.serializeDOM_S((Node) output);
        String outXML = readFileFromCP("bib_attr.xml");
        //System.out.println("\nTestJson2Xml_attr: " + dom);
        assertXMLEqual("TestJson2Xml_attr failed", outXML, dom);
    }

    /**
     * Test the XML2JSONTransformer.
     * 
     * @throws Exception
     */
    public void testXML2JSON_Xsl() throws Exception
    {
        Object output = controller.transform("TestXml2Json_Xsl", readFileFromCP("bib.xj"), null);
        String json = (String) output;
        String outJSON = readFileFromCP("bib.json");
        //System.out.println("\nTestXml2Json_Xsl: " + json);
        JSONAssert.assertEquals(outJSON, json, true);
    }

    /**
     * Test the JSON2XMLTransformer.
     * 
     * @throws Exception
     */
    public void testJSON2XML_Xsl() throws Exception
    {
        Object output = controller.transform("TestJson2Xml_Xsl", readFileFromCP("bib.json"), null);
        String dom = XMLUtils.serializeDOM_S((Node) output);
        String outXML = readFileFromCP("bib.xj");
        //System.out.println("\nTestJson2Xml_Xsl: " + dom);
        assertXMLEqual("TestJson2Xml_Xsl failed", outXML, dom);
    }
    
    private String readFileFromCP(String filename) throws URISyntaxException, IOException{
    	Path filePath  = Paths.get(ClassLoader.getSystemResource(filename).toURI());
    	return new String(Files.readAllBytes(filePath));
    }

}
