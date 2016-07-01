/*
 * Copyright 2007-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.greenvulcano.gvesb.interceptor;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.core.GreenVulcano;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tests.unit.gvesb.gvcore.jmx.TestMBean;


@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/application-context.xml"})
public class GVConsoleAspectTestLtw extends TestCase
{
	final Logger logger = LoggerFactory.getLogger(GVConsoleAspectTestLtw.class);

	private static JMXEntryPoint jmx                = null;

	private static Set<String>   result             = new HashSet<String>();

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		if (jmx == null) {
			jmx = JMXEntryPoint.instance();
			MBeanServer server = jmx.getServer();

			Set<ObjectName> set = server.queryNames(new ObjectName("GreenVulcano:*"), null);
			for (ObjectName objectName : set) {
				System.out.println(objectName);
			}
			assertTrue("No JMX object returned in GreenVulcano domain", set != null && !set.isEmpty());
			TestMBean t = new tests.unit.gvesb.gvcore.jmx.Test(result);
			server.registerMBean(t, new ObjectName("GreenVulcano:service=GVTestNotification"));
		}
	}


	//@Autowired
	//private Processor processor = null;

	/**
	 * Tests processor from the Spring context.
	 */
	//    @Test
	//    public void testProcessorFromContext() {
	//        assertNotNull("Processor is null.", processor);
	//        
	//        logger.debug("Running processor from Spring context.");
	//        
	//        processor.process();
	//    }

	/**
	 * Tests processor created outside the Spring context.
	 */
//	@Test
//	public void testProcessor() {
//		Processor processor = new Processor();
//
//		logger.debug("Running processor from outside the Spring context.");
//
//		processor.process();
//	}


	/**
	 * @throws Exception
	 */
	@Test
	public void testGVCoreCallCoreAspectJ() throws Exception
	{
		String SYSTEM_NAME = "GVESB";
		String SERVICE_NAME = "TOUPPER_CALL";
		String TEST_BUFFER = "<Radice><data>pippo</data><data>pluto</data></Radice>";
		//String TEST_BUFFER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><RowSet><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data></RowSet>";
		Id id = new Id();
		Document input = XMLUtils.getParserInstance().parseDOM(TEST_BUFFER);
		//printXmlProps(input);

		GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
		gvBuffer.setObject(input);

// TEST WITH INPUT OBJECT Json.
//		String jSonString = "{'database' : 'mkyongDB','table' : 'hosting', 'detail' : {'records' : 99, 'index' : 'vps_index1', 'active' : 'true'}}}";
//		gvBuffer.setObject(jSonString);
		
		gvBuffer.setProperty("TEST_PROP1", "TEST_PROP1_VALUE");
		gvBuffer.setProperty("TEST_PROP2", "TEST_PROP2_VALUE");

		GreenVulcano greenVulcano = new GreenVulcano();
		GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
		System.out.println("gvBufferout.getSystem(): "+gvBufferout.getSystem());
		System.out.println("gvBufferout.getService(): "+gvBufferout.getService());
		System.out.println("gvBufferout.getId(): "+gvBufferout.getId());
		System.out.println("Id: "+gvBufferout.getId());
		//        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
		//        assertEquals(SERVICE_NAME, gvBufferout.getService());
		//        assertEquals(id, gvBufferout.getId());
	}


	private void printXmlProps(Document input) {
		// get the first element
		Element element = input.getDocumentElement();

		// get all child nodes
		NodeList nodes = element.getChildNodes();

		// print the text content of each child
		for (int i = 0; i < nodes.getLength(); i++) {
			System.out.println("" + nodes.item(i).getTextContent());
		}
	}

}
