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
package tests.unit.vcl.axis2;

import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Endpoint;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.context.MessageContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.ws.WSCallOperation;
import junit.framework.TestCase;
import tests.unit.http.HttpCallTestCase;
import tests.unit.vcl.axis2.ws.GVAxis2TestWSImpl;

/**
 * @version 3.0.0 Mar 25, 2010
 * @author GreenVulcano Developer Team
 * 
 */
@SuppressWarnings("deprecation")
@RunWith(BlockJUnit4ClassRunner.class)
public class Axis2PluginTestCase extends TestCase
{
    private static final String EXPECTED_RESULT = "test web services";
    private static final String XML_MESSAGE     = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><test><with>attachment</with></test>";
    private static Context initialContext;

    static {
    	XMLConfig.setBaseConfigPath(HttpCallTestCase.class.getClassLoader().getResource(".").getPath());
    	Endpoint.publish("http://127.0.0.1:4204/GVAxis2TestWSImpl", new GVAxis2TestWSImpl());
    }
    
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @BeforeClass
    public static void init() throws Exception  {    	
            
        initialContext = new InitialContext();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @AfterClass
    public static void destroy() throws Exception {
        initialContext.close();      
    }

    /**
     * @throws Exception
     */
    @Ignore @Test
    public final void testWSCall() throws Exception
    {
        WSCallOperation wsCall = new WSCallOperation();
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/ws-call[@name='test-ws-call-echo' and @type='call']");
        wsCall.init(node);
        GVBuffer gvBuffer = getTestBuffer(createEchoMessage());
        GVBuffer output = wsCall.perform(gvBuffer);
        assertNotNull(output);
        MessageContext mc = (MessageContext) output.getObject();
        String result = null;
        for (Iterator<?> iterator = mc.getEnvelope().getBody().getChildElements(); iterator.hasNext();) {
            OMElement response = (OMElement) iterator.next();
            OMElement returnChild = response.getFirstChildWithName(new QName("return"));
            if (returnChild != null) {
                result = returnChild.getText();
            }
        }
        assertEquals(EXPECTED_RESULT, result);
    }

    /**
     * @throws Exception
     */
    @Ignore @Test
    public final void testWSCallSwA() throws Exception
    {
        WSCallOperation wsCall = new WSCallOperation();
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/ws-call[@name='test-ws-call-echo-with-attachment' and @type='call']");
        wsCall.init(node);
        GVBuffer gvBuffer = getTestBuffer("SOME BYTES");
        gvBuffer.setProperty("fileName", "test.odt");
        gvBuffer.setProperty("xmlMessage", XML_MESSAGE);
        GVBuffer output = wsCall.perform(gvBuffer);
        assertNotNull(output);
        SOAPMessage soapMessage = (SOAPMessage) output.getObject();
        String result = null;
        for (Iterator<?> iterator = soapMessage.getSOAPPart().getEnvelope().getBody().getChildElements(); iterator.hasNext();) {
            SOAPElement response = (SOAPElement) iterator.next();
            NodeList returnChilds = response.getElementsByTagName("return");
            if ((returnChilds != null) && (returnChilds.getLength() > 0)) {
                result = returnChilds.item(0).getTextContent();
            }
        }
        assertEquals(EXPECTED_RESULT, result);
    }

    /**
     * @throws Exception
     */
    @Ignore @Test
    public final void testWSCallOM() throws Exception
    {
        WSCallOperation wsCall = new WSCallOperation();
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
                "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/ws-call[@name='test-ws-call-echo-om' and @type='call']");
        wsCall.init(node);
        GVBuffer gvBuffer = getTestBuffer(createEchoMessageOM());
        GVBuffer output = wsCall.perform(gvBuffer);
        assertNotNull(output);
        MessageContext mc = (MessageContext) output.getObject();
        String result = null;
        for (Iterator<?> iterator = mc.getEnvelope().getBody().getChildElements(); iterator.hasNext();) {
            OMElement response = (OMElement) iterator.next();
            OMElement returnChild = response.getFirstChildWithName(new QName("return"));
            if (returnChild != null) {
                result = returnChild.getText();
            }
        }
        assertEquals(EXPECTED_RESULT, result);
    }

    /**
     * @throws Exception
     */
    /*
     * public final void testDUMMY() throws Exception { assertTrue(true); }
     */

    private static final String ns = "http://www.greenvulcano.com/gvesb/webservices";

    private GVBuffer getTestBuffer(Object object) throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer("TEST", "WS-TEST-CALL");
        gvBuffer.setObject(object);
        return gvBuffer;
    }

    private Element createEchoMessage() throws Exception
    {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = documentFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element root = doc.createElementNS(ns, "echo");
        root.setPrefix("ns1");
        doc.appendChild(root);
        Element arg0 = doc.createElementNS("", "arg0");
        root.appendChild(arg0);
        arg0.appendChild(doc.createTextNode(EXPECTED_RESULT));
        return root;
    }

    private SOAPEnvelope createEchoMessageOM() throws Exception
    {
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope soapEnvelope = factory.createSOAPEnvelope();
        SOAPBody soapBody = factory.createSOAPBody(soapEnvelope);
        OMElement root = factory.createOMElement("echo", new OMNamespaceImpl(ns, "ns1"));
        soapBody.addChild(root);
        OMElement arg0 = factory.createOMElement(new QName("arg0"));
        root.addChild(arg0);
        arg0.setText(EXPECTED_RESULT);
        return soapEnvelope;
    }

}
