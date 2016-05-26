package tests.unit.vcl.internal.xml;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.internal.xml.XMLValidationCallOperation;
import it.greenvulcano.util.xml.XMLUtils;
import junit.framework.TestCase;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

/**
 *
 * @version 3.0.0 Jun 2, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XMLValidationCallTestCase extends TestCase
{
    private static final String CONF_NULL_XSD             = "<ChangeGVBufferNode class=\"it.greenvulcano.gvesb.core.flow.ChangeGVBufferNode\" op-type=\"change GVBuffer\""
                                                                  + " id=\"validateXML\" input=\"xml\" next-node-id=\"check_status\" output=\"validxml\" type=\"flow-node\">"
                                                                  + "<OutputServices>"
                                                                  + "<xml-validation-service critical=\"yes\" internal=\"yes\" remove-fields=\"yes\" type=\"service\">"
                                                                  + "<xml-validation-call class=\"it.greenvulcano.gvesb.virtual.internal.xml.XMLValidationCallOperation\""
                                                                  + " name=\"validation\" type=\"call\" xsd-policy=\"null-xsd\">"
                                                                  + "</xml-validation-call>"
                                                                  + "</xml-validation-service>"
                                                                  + "</OutputServices></ChangeGVBufferNode>";
    private static final String CONF_FORCE_DEFAULT        = "<ChangeGVBufferNode class=\"it.greenvulcano.gvesb.core.flow.ChangeGVBufferNode\" op-type=\"change GVBuffer\""
                                                                  + " id=\"validateXML\" input=\"xml\" next-node-id=\"check_status\" output=\"validxml\" type=\"flow-node\">"
                                                                  + "<OutputServices>"
                                                                  + "<xml-validation-service critical=\"yes\" internal=\"yes\" remove-fields=\"yes\" type=\"service\">"
                                                                  + "<xml-validation-call class=\"it.greenvulcano.gvesb.virtual.internal.xml.XMLValidationCallOperation\""
                                                                  + " name=\"validation\" type=\"call\" xsd-policy=\"force-default\" default-xsd=\"test_validator.xsd\">"
                                                                  + "</xml-validation-call>"
                                                                  + "</xml-validation-service>"
                                                                  + "</OutputServices></ChangeGVBufferNode>";

    private static final String TNS                       = "http://www.greenvulcano.org/test_validator/";
    private static final String XSI_NS                    = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String TEST_XML_NOT_VALID        = "<test xmlns=\"" + TNS + "\" xmlns:xsi=\"" + XSI_NS
                                                                  + "\" xsi:schemaLocation=\"" + TNS
                                                                  + " wrongxsd.xsd\">firstName=nunzio</test>";
    private static final String TEST_XML_VALID_SCH_LOC    = "<ns0:test xmlns:ns0=\""
                                                                  + TNS
                                                                  + "\" xmlns:xsi=\""
                                                                  + XSI_NS
                                                                  + "\" xsi:schemaLocation=\""
                                                                  + TNS
                                                                  + " wrongxsd.xsd\"><ns0:firstName>nunzio</ns0:firstName>"
                                                                  + "<ns0:lastName>La Riviera</ns0:lastName>"
                                                                  + "<ns0:address><ns0:street>Colombo</ns0:street><ns0:city>Rome</ns0:city></ns0:address>"
                                                                  + "</ns0:test>";
    private static final String TEST_XML_VALID_NO_SCH_LOC = "<ns0:test xmlns:ns0=\""
                                                                  + TNS
                                                                  + "\"><ns0:firstName>nunzio</ns0:firstName>"
                                                                  + "<ns0:lastName>La Riviera</ns0:lastName>"
                                                                  + "<ns0:address><ns0:street>Colombo</ns0:street><ns0:city>Rome</ns0:city></ns0:address>"
                                                                  + "</ns0:test>";

    private static final String BASE_DIR = "target" + File.separator + "test-classes";
        
    @Override
	protected void setUp() throws Exception {		
		super.setUp();
		System.setProperty("gv.app.home", BASE_DIR);
	}

	/**
     * @throws Exception
     */
    public void testNotValidXMLNullXSDAsString() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer("GVESB", "XML_VALIDATION");
        XMLValidationCallOperation validationCall = createValidationCall(CONF_NULL_XSD, TEST_XML_NOT_VALID, false,
                gvBuffer);
        try {
            validationCall.perform(gvBuffer);
            fail("Validation on non valid XML successful!");
        }
        catch (CallException exc) {
            assertTrue(exc.getCause() instanceof SAXParseException);        
        }
    }

    /**
     * @throws Exception
     */
    public void testNotValidXMLForceXSDAsString() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer("GVESB", "XML_VALIDATION");
        XMLValidationCallOperation validationCall = createValidationCall(CONF_FORCE_DEFAULT, TEST_XML_NOT_VALID, false,
                gvBuffer);
        try {
            validationCall.perform(gvBuffer);
            fail("Validation on non valid XML successful!");
        }
        catch (CallException exc) {
            assertTrue(exc.getCause() instanceof SAXParseException);
        }
    }

    /**
     * @throws Exception
     */
    public void testNotValidXMLNullXSDAsDOM() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer("GVESB", "XML_VALIDATION");
        XMLValidationCallOperation validationCall = createValidationCall(CONF_NULL_XSD, TEST_XML_NOT_VALID, true,
                gvBuffer);
        try {
            validationCall.perform(gvBuffer);
            fail("Validation on non valid XML successful!");
        }
        catch (CallException exc) {
            assertTrue(exc.getCause() instanceof SAXParseException);
        }
    }

    /**
     * @throws Exception
     */
    public void testNotValidXMLForceXSDAsDOM() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer("GVESB", "XML_VALIDATION");
        XMLValidationCallOperation validationCall = createValidationCall(CONF_FORCE_DEFAULT, TEST_XML_NOT_VALID, true,
                gvBuffer);
        try {
            validationCall.perform(gvBuffer);
            fail("Validation on non valid XML successful!");
        }
        catch (CallException exc) {
            assertTrue(exc.getCause() instanceof SAXParseException);
        }
    }

    /**
     * @throws Exception
     */
    public void testValidXMLNullXSDAsString() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer("GVESB", "XML_VALIDATION");
        XMLValidationCallOperation validationCall = createValidationCall(CONF_NULL_XSD, TEST_XML_VALID_SCH_LOC, false,
                gvBuffer);
        try {
            validationCall.perform(gvBuffer);
            fail("Validation on non valid XML Schema successful!");
        }
        catch (CallException exc) {
            assertTrue(exc.getCause() instanceof SAXParseException);
        }
    }

    /**
     * @throws Exception
     */
    public void testValidXMLForceXSDAsString() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer("GVESB", "XML_VALIDATION");
        XMLValidationCallOperation validationCall = createValidationCall(CONF_FORCE_DEFAULT, TEST_XML_VALID_NO_SCH_LOC,
                false, gvBuffer);
        validationCall.perform(gvBuffer);
    }

    /**
     * @throws Exception
     */
    public void testValidXMLNullXSDAsDOM() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer("GVESB", "XML_VALIDATION");
        XMLValidationCallOperation validationCall = createValidationCall(CONF_NULL_XSD, TEST_XML_VALID_SCH_LOC, true,
                gvBuffer);
        try {
            validationCall.perform(gvBuffer);
            fail("Validation on non valid XML Schema successful!");
        }
        catch (CallException exc) {
            assertTrue(exc.getCause() instanceof SAXParseException);
        }
    }

    /**
     * @throws Exception
     */
    public void testValidXMLForceXSDAsDOM() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer("GVESB", "XML_VALIDATION");
        XMLValidationCallOperation validationCall = createValidationCall(CONF_FORCE_DEFAULT, TEST_XML_VALID_SCH_LOC,
                true, gvBuffer);
        validationCall.perform(gvBuffer);
    }

    private XMLValidationCallOperation createValidationCall(String confStr, String xml, boolean parse, GVBuffer gvBuffer)
            throws Exception
    {
        Document conf = XMLUtils.parseDOM_S(confStr, false, false);
        Node node = XMLConfig.getNode(conf,
                "/ChangeGVBufferNode/OutputServices/xml-validation-service/xml-validation-call");
        XMLValidationCallOperation validationCall = new XMLValidationCallOperation();
        validationCall.init(node);
        Object object = xml;
        if (parse) {
            object = XMLUtils.parseDOM_S(xml, false, false);
        }
        gvBuffer.setObject(object);
        return validationCall;
    }

}
