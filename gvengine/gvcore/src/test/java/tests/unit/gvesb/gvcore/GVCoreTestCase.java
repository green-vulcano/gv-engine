/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package tests.unit.gvesb.gvcore;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.core.GreenVulcano;
import it.greenvulcano.gvesb.core.jmx.RegisterJMXServiceManager;
import it.greenvulcano.gvesb.core.jmx.RegisterServiceOperationInfoManager;
import it.greenvulcano.gvesb.identity.GVIdentityHelper;
import it.greenvulcano.gvesb.identity.impl.DummyIdentityInfo;
import it.greenvulcano.gvesb.virtual.OperationFactory;
import it.greenvulcano.gvesb.virtual.internal.DTEServiceContextCall;
import it.greenvulcano.gvesb.virtual.internal.ProxyCallOperation;
import it.greenvulcano.gvesb.virtual.internal.ScriptCallOperation;
import it.greenvulcano.gvesb.virtual.internal.TestServiceCall;
import it.greenvulcano.gvesb.virtual.internal.xml.XMLValidationCallOperation;
import it.greenvulcano.gvesb.virtual.pool.OperationManagerPool;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.MBeanServerInitializerFactory;
import it.greenvulcano.util.xml.XMLUtils;
import junit.framework.TestCase;
import tests.unit.gvesb.gvcore.jmx.Test;
import tests.unit.gvesb.gvcore.jmx.TestMBean;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

/**
 * @version 3.0.0 Feb 26, 2010
 * @author GreenVulcano Developer Team
 */
public class GVCoreTestCase extends TestCase
{
    private static JMXEntryPoint jmx                = null;

    private static Set<String>   result             = new HashSet<String>();

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
    	XMLConfig.setBaseConfigPath(getClass().getClassLoader().getResource(".").getPath());
		OperationFactory.registerSupplier("proxy-call", ProxyCallOperation::new);
		OperationFactory.registerSupplier("script-call", ScriptCallOperation::new);
		OperationFactory.registerSupplier("xml-validation-call", XMLValidationCallOperation::new);
		OperationFactory.registerSupplier("gvdte-context-call", DTEServiceContextCall::new);
		OperationFactory.registerSupplier("test-service-call", TestServiceCall::new);
    	
		MBeanServerInitializerFactory.registerSupplier("it.greenvulcano.gvesb.core.jmx.RegisterServiceOperationInfoManager", RegisterServiceOperationInfoManager::new);
		MBeanServerInitializerFactory.registerSupplier("it.greenvulcano.gvesb.core.jmx.RegisterJMXServiceManager", RegisterJMXServiceManager::new);
        if (jmx == null) {
        	
            jmx = JMXEntryPoint.instance();
            MBeanServer server = jmx.getServer();

            Set<ObjectName> set = server.queryNames(new ObjectName("GreenVulcano:*"), null);
            for (ObjectName objectName : set) {
                System.out.println(objectName);
            }
            assertTrue("No JMX object returned in GreenVulcano domain", set != null && !set.isEmpty());
                    
            TestMBean t = new Test(result);
            server.registerMBean(t, new ObjectName("GreenVulcano:service=GVTestNotification"));
            
        }
    }

    /**
     * @throws Exception
     */
    public void testGVCoreIteratorXpath() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TOUPPER_ITER";
        String TEST_BUFFER = "<Radice><data>pippo</data><data>pluto</data></Radice>";
        Id id = new Id();
        Document input = XMLUtils.getParserInstance().parseDOM(TEST_BUFFER);
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(input);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
    }

    /**
     * @throws Exception
     */
    public void testGVCoreIteratorRegEx() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TOUPPER_ITER_REGEX";
        String TEST_BUFFER = "ciro,nunzio,gianfranco";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
    }

    public void testJSONObjectLoop() throws GVException {
    	String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TOUPPER_LOOP";
        
        JSONObject TEST_BUFFER = new JSONObject();
        TEST_BUFFER
        	.put("val1", "value1")
        	.put("val2", "value2")
        	.put("val3", "value3")
        	.put("val4", "value4");
        
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.forward(gvBuffer, "loopOnJsonObject");
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        
        for (String key : TEST_BUFFER.keySet()) {
        	assertEquals(TEST_BUFFER.getString(key).toUpperCase(), JSONObject.class.cast(gvBufferout.getObject()).getString(key));
        }
        
        GVBuffer gvBufferString = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBufferString.setObject("{\"val1\":\"value1\", \"val2\":\"value2\", \"val3\":\"value3\", \"val4\":\"value4\"}");
        
        GVBuffer gvBufferoutString = greenVulcano.forward(gvBufferString, "loopOnJsonObject");
        for (String key : TEST_BUFFER.keySet()) {
        	assertEquals(JSONObject.class.cast(gvBufferoutString.getObject()).getString(key), JSONObject.class.cast(gvBufferout.getObject()).getString(key));
        }
        
    }
    
    public void testJSONArrayLoop() throws GVException {
    	String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TOUPPER_LOOP";
        
        JSONArray TEST_BUFFER = new JSONArray();
        TEST_BUFFER
        	.put("value1")
        	.put("value2")
        	.put("value3")
        	.put("value5");
        
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.forward(gvBuffer, "loopOnJsonArray");
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        
        for (int i =0; i<TEST_BUFFER.length(); i++){
        	assertEquals(TEST_BUFFER.getString(i).toUpperCase(), JSONArray.class.cast(gvBufferout.getObject()).getString(i));
        }
        
    }
    
    public void testXMLloop() throws GVException {
    	String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TOUPPER_LOOP";
        
        String TEST_BUFFER = "<RootNode><child1>aaaa</child1><child2>bbbbb</child2></RootNode>";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.forward(gvBuffer, "loopOnXML");
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        
        
    }
    
    public void testCollectionLoop() throws GVException {
       	String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TOUPPER_LOOP";
        
        Collection<?> TEST_BUFFER = Stream.of("alpha","beta", "gamma").collect(Collectors.toSet()); 
        
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.forward(gvBuffer, "loopOnCollection");
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
    }
    
    /**
     * @throws Exception
     */
    public void testGVCoreIteratorRegExPlugIn() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TOUPPER_ITER_REGEX_PLUGIN";
        String TEST_BUFFER = "ciro,nunzio,gianfranco";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
    }

    /**
     * @throws Exception
     */
    public void testGVCoreCallCore() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TOUPPER_CALL";
        String TEST_BUFFER = "<Radice><data>pippo</data><data>pluto</data></Radice>";
        Id id = new Id();
        Document input = XMLUtils.getParserInstance().parseDOM(TEST_BUFFER);
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(input);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
    }    
    

    /**
     * @throws Exception
     */
    public void testGVCoreMultiPartecipants() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TEST_PARTECIPANTS";
        String TEST_BUFFER = "AbCdEfGhIl";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(TEST_BUFFER.toLowerCase(), gvBufferout.getObject());
    }

    /**
     * @throws Exception
     */
    public void testGVCoreIdentityMapCore() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "IDENTITY";
        String TEST_BUFFER = "<Radice><data>pippo</data><data>pluto</data></Radice>";
        Id id = new Id();
        Document input = XMLUtils.getParserInstance().parseDOM(TEST_BUFFER);
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(input);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
    }

    /**
     * @throws Exception
     */
    public void testGVCoreOGNLService() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TOUPPER_OGNL";
        String TEST_BUFFER = "test test test test";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals("PLUTO", gvBufferout.getProperty("PIPPO"));
        assertEquals("PIPPO", gvBufferout.getProperty("PLUTO"));
    }
    
    /**
     * @throws Exception
     */
    public void testGVCoreScriptOGNLService() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TOUPPER_SCRIPT_OGNL";
        String TEST_BUFFER = "test test test test";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals("PLUTO", gvBufferout.getProperty("PIPPO"));
        assertEquals("PIPPO", gvBufferout.getProperty("PLUTO"));
    }
    
    /**
     * @throws Exception
     */
    public void testGVCoreScriptJSService() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TOUPPER_SCRIPT_JS";
        String TEST_BUFFER = "test test test test";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals("PLUTO", gvBufferout.getProperty("PIPPO"));
        assertEquals("PIPPO", gvBufferout.getProperty("PLUTO"));
    }

    /**
     * @throws Exception
     */
    public void testGVSubFlowCall() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TEST_SUBFLOW";
        String TEST_BUFFER = "test input data";
        String OUT_BUFFER = "TEST INPUT DATA";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(OUT_BUFFER, gvBufferout.getObject());

        System.out.println(OperationManagerPool.instance().toString());
    }

    /**
     * @throws Exception
     */
    public void testGVSubFlowCallDyn() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TEST_SUBFLOW_DYNAMIC";
        String TEST_BUFFER = "tEsT iNpUt dAtA";
        String OUT_BUFFER_U = "TEST INPUT DATA";
        String OUT_BUFFER_L = "test input data";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        
        gvBuffer.setProperty("MODE", "TOUPPER");
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(OUT_BUFFER_U, gvBufferout.getObject());

        id = new Id();
        gvBuffer.setId(id);
        gvBuffer.setProperty("MODE", "TOLOWER");
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(OUT_BUFFER_L, gvBufferout.getObject());

        System.out.println(OperationManagerPool.instance().toString());
    }

    /**
     * @throws Exception
     */
    public void testGVCoreIteratorSubFlow() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "SUBFLOW_ITER";
        String TEST_BUFFER = "<Radice><data>pippo</data><data>pluto</data></Radice>";
        Id id = new Id();
        Document input = XMLUtils.getParserInstance().parseDOM(TEST_BUFFER);
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(input);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
    }

    /**
     * @throws Exception
     */
    public void testGVCoreNotification() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TEST_NOTIF";
        String TEST_BUFFER = "AaAaAaAa";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setProperty("PROP_TEST_1", "value 1");
        gvBuffer.setProperty("PROP_TEST_2", "value 2");
        gvBuffer.setProperty("PROP_TEST_3", "value 3");
        gvBuffer.setObject(TEST_BUFFER);

        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(TEST_BUFFER, gvBufferout.getObject());
        assertEquals(2, result.size());
        StringBuilder not1 = new StringBuilder();
        not1.append(SYSTEM_NAME).append('#').append(SERVICE_NAME).append('#').append(id.toString());
        assertTrue(result.contains(not1.toString()));
        assertTrue(result.contains("it.greenvulcano.gvesb.virtual.CallException: BOOM!!!! [it.greenvulcano.gvesb.virtual.CallException]: "));
    }

    /**
     * @throws Exception
     */
    public void testGVIdentityCondition() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "IDENTITY_COND";
        String TEST_BUFFER = "TeSt InPuT DaTa";
        String OUT_BUFFER_TU = "TEST INPUT DATA";
        String OUT_BUFFER_TL = "test input data";
        String OUT_BUFFER_ECHO = "TeSt InPuT DaTa";

        GVIdentityHelper.push(new DummyIdentityInfo("TestUser_1", "ROLE_A", ""));
        try {
            Id id = new Id();
            GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
            gvBuffer.setObject(TEST_BUFFER);
            GreenVulcano greenVulcano = new GreenVulcano();
            GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
            assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
            assertEquals(SERVICE_NAME, gvBufferout.getService());
            assertEquals(id, gvBufferout.getId());
            assertEquals(OUT_BUFFER_TU, gvBufferout.getObject());
        }
        finally {
            GVIdentityHelper.pop();
        }

        GVIdentityHelper.push(new DummyIdentityInfo("TestUser_1", "ROLE_B", ""));
        try {
            Id id = new Id();
            GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
            gvBuffer.setObject(TEST_BUFFER);
            GreenVulcano greenVulcano = new GreenVulcano();
            GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
            assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
            assertEquals(SERVICE_NAME, gvBufferout.getService());
            assertEquals(id, gvBufferout.getId());
            assertEquals(OUT_BUFFER_TL, gvBufferout.getObject());
        }
        finally {
            GVIdentityHelper.pop();
        }

        GVIdentityHelper.push(new DummyIdentityInfo("TestUser_1", "ROLE_C", ""));
        try {
            Id id = new Id();
            GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
            gvBuffer.setObject(TEST_BUFFER);
            GreenVulcano greenVulcano = new GreenVulcano();
            GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
            assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
            assertEquals(SERVICE_NAME, gvBufferout.getService());
            assertEquals(id, gvBufferout.getId());
            assertEquals(OUT_BUFFER_ECHO, gvBufferout.getObject());
        }
        finally {
            GVIdentityHelper.pop();
        }

        // no roles
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        assertEquals(OUT_BUFFER_ECHO, gvBufferout.getObject());

    }

    /**
     * @throws Exception
     */
    public void testGVWaitNode() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TestWaitNode_GVData";

        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME);
        gvBuffer.setProperty("TIMEOUT", "1000");
        GreenVulcano greenVulcano = new GreenVulcano();
        long start = System.currentTimeMillis();
        greenVulcano.requestReply(gvBuffer);
        long timeout = System.currentTimeMillis() - start;
        assertTrue(Math.abs(timeout - 1000) < 200);
    }
}
