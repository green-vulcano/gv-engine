/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
import it.greenvulcano.gvesb.buffer.GVPublicException;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.core.GreenVulcano;
import it.greenvulcano.gvesb.virtual.OperationFactory;
import it.greenvulcano.gvesb.virtual.internal.DTEServiceContextCall;
import it.greenvulcano.gvesb.virtual.internal.ProxyCallOperation;
import it.greenvulcano.gvesb.virtual.internal.ScriptCallOperation;
import it.greenvulcano.gvesb.virtual.internal.TestServiceCall;
import it.greenvulcano.gvesb.virtual.internal.xml.XMLValidationCallOperation;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;

/**
 * @version 3.4.0 Jun 17, 2013
 * @author GreenVulcano Developer Team
 */
@SuppressWarnings("unchecked")
public class GVCoreParallelTestCase extends XMLTestCase
{

	static {
		XMLConfig.setBaseConfigPath(GVCoreParallelTestCase.class.getClassLoader().getResource(".").getPath());
    	OperationFactory.registerSupplier("proxy-call", ProxyCallOperation::new);
		OperationFactory.registerSupplier("script-call", ScriptCallOperation::new);
		OperationFactory.registerSupplier("xml-validation-call", XMLValidationCallOperation::new);
		OperationFactory.registerSupplier("gvdte-context-call", DTEServiceContextCall::new);
		OperationFactory.registerSupplier("test-service-call", TestServiceCall::new);
        XMLUnit.setIgnoreWhitespace(true);
	}    

    /**
     * @throws Exception
     */
    public void testTestSplitterNormalEnd() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TestSplitterNormalEnd";
        String TEST_BUFFER = "ciro,nunzio,gianfranco,antonio";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 1----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 1----SPL");
        assertEquals("[CIRO, NUNZIO, GIANFRANCO, ANTONIO]", gvBufferout.getObject().toString());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 2----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 2----SPL");
        assertEquals("[CIRO, NUNZIO, GIANFRANCO, ANTONIO]", gvBufferout.getObject().toString());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 3----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 3----SPL");
        assertEquals("[CIRO, NUNZIO, GIANFRANCO, ANTONIO]", gvBufferout.getObject().toString());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

    }

    /**
     * @throws Exception
     */
    public void testTestSplitterNormalEnd_OutJS() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TestSplitterNormalEnd_OutJS";
        String TEST_BUFFER = "ciro,nunzio,gianfranco,antonio";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 1----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 1----SPL");
        assertEquals("[ciro -> CIRO];[nunzio -> NUNZIO];[gianfranco -> GIANFRANCO];[antonio -> ANTONIO]", gvBufferout.getObject().toString());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 2----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 2----SPL");
        assertEquals("[ciro -> CIRO];[nunzio -> NUNZIO];[gianfranco -> GIANFRANCO];[antonio -> ANTONIO]", gvBufferout.getObject().toString());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 3----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 3----SPL");
        assertEquals("[ciro -> CIRO];[nunzio -> NUNZIO];[gianfranco -> GIANFRANCO];[antonio -> ANTONIO]", gvBufferout.getObject().toString());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

    }

    /**
     * @throws Exception
     */
    public void testTestSplitterNormalEnd_OutXML() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TestSplitterNormalEnd_OutXML";
        String TEST_BUFFER = "<root><element>el1</element><element>el2</element></root>";
        String OUT_BUFFER = "<Aggregate xmlns=\"http://www.greenvulcano.it/greenvulcano\"><element>el1</element><element>el2</element></Aggregate>";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 1----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 1----SPL");
        String res = XMLUtils.serializeDOM_S((Document) gvBufferout.getObject());
        assertXMLEqual("XMLAggregate Failed", OUT_BUFFER, res);
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 2----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 2----SPL");
        res = XMLUtils.serializeDOM_S((Document) gvBufferout.getObject());
        assertXMLEqual("XMLAggregate Failed", OUT_BUFFER, res);
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 3----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 3----SPL");
        res = XMLUtils.serializeDOM_S((Document) gvBufferout.getObject());
        assertXMLEqual("XMLAggregate Failed", OUT_BUFFER, res);
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

    }

    /**
     * @throws Exception
     */
    public void testTestSplitterNormalEnd_OutOgnl() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TestSplitterNormalEnd_OutOgnl";
        String TEST_BUFFER = "ciro,nunzio,gianfranco,antonio";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 1----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 1----SPL");
        assertEquals("[ciro -> CIRO];[nunzio -> NUNZIO];[gianfranco -> GIANFRANCO];[antonio -> ANTONIO]", gvBufferout.getObject().toString());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 2----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 2----SPL");
        assertEquals("[ciro -> CIRO];[nunzio -> NUNZIO];[gianfranco -> GIANFRANCO];[antonio -> ANTONIO]", gvBufferout.getObject().toString());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 3----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 3----SPL");
        assertEquals("[ciro -> CIRO];[nunzio -> NUNZIO];[gianfranco -> GIANFRANCO];[antonio -> ANTONIO]", gvBufferout.getObject().toString());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

    }

    /**
     * @throws Exception
     */
    public void testTestSplitterFirstEnd() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TestSplitterFirstEnd";
        String TEST_BUFFER = "ciro,gianfranco,nunzio,antonio";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 1----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 1----SPL");
        assertEquals("[NUNZIO]", gvBufferout.getObject().toString());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 2----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 2----SPL");
        assertEquals("[NUNZIO]", gvBufferout.getObject().toString());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 3----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 3----SPL");
        assertEquals("[NUNZIO]", gvBufferout.getObject().toString());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));
    }
    
    /**
     * @throws Exception
     */
    public void testTestSplitterFirstError() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TestSplitterFirstError";
        String TEST_BUFFER = "ciro,gianfranco,nunzio,antonio";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 1----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 1----SPL");
        assertEquals("[NUNZIO, CIRO]", gvBufferout.getObject().toString());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));
        
        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 2----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 2----SPL");
        assertEquals("[NUNZIO, CIRO]", gvBufferout.getObject().toString());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 3----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 3----SPL");
        assertEquals("[NUNZIO, CIRO]", gvBufferout.getObject().toString());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));
    }

    /**
     * @throws Exception
     */
    public void testTestSplitterTimeout() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TestSplitterTimeout";
        String TEST_BUFFER = "ciro,gianfranco,nunzio,antonio";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 1----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 1----SPL");
        assertEquals("[CIRO, NUNZIO]", gvBufferout.getObject().toString());
        assertEquals("TIMEOUT", gvBufferout.getProperty("END"));
        
        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 2----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 2----SPL");
        assertEquals("[CIRO, NUNZIO]", gvBufferout.getObject().toString());
        assertEquals("TIMEOUT", gvBufferout.getProperty("END"));
        
        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 3----SPL");
        System.out.println(gvBufferout);
        System.out.println("TEST 3----SPL");
        assertEquals("[CIRO, NUNZIO]", gvBufferout.getObject().toString());
        assertEquals("TIMEOUT", gvBufferout.getProperty("END"));
    }
    
    /**
     * @throws Exception
     */
    @SuppressWarnings("unused")
	public void testTestSplitterInterrupt() throws Exception
    {
        Timer t = new Timer();
        final Thread thd = Thread.currentThread();
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TestSplitterTimeout";
        String TEST_BUFFER = "ciro,gianfranco,nunzio,antonio";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                thd.interrupt();
            }
        }, 2000);
        try {
            GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
            fail("Missing InterruptedException");
        }
        catch (Exception exc) {
            assertTrue(exc instanceof GVPublicException);
            assertTrue(exc.getMessage().indexOf("GV_INTERRUPTED_ERROR") != -1);
        }

        t.schedule(new TimerTask() {
            @Override
            public void run() {
                thd.interrupt();
            }
        }, 2000);
        try {
            id = new Id();
            gvBuffer.setId(id);
            GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
            fail("Missing InterruptedException");
        }
        catch (Exception exc) {
            assertTrue(exc instanceof GVPublicException);
            assertTrue(exc.getMessage().indexOf("GV_INTERRUPTED_ERROR") != -1);
        }

        t.schedule(new TimerTask() {
            @Override
            public void run() {
                thd.interrupt();
            }
        }, 2000);
        try {
            id = new Id();
            gvBuffer.setId(id);
            GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
            fail("Missing InterruptedException");
        }
        catch (Exception exc) {
            assertTrue(exc instanceof GVPublicException);
            assertTrue(exc.getMessage().indexOf("GV_INTERRUPTED_ERROR") != -1);
        }
    }

    
    /**
     * @throws Exception
     */
    public void testTestParallelNormalEnd() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TestParallelNormalEnd";
        String TEST_BUFFER = "CiRo,NuNzIo,GiAnFrAnCo,AnToNiO";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        gvBuffer.setProperty("P1", "V1");
        gvBuffer.setProperty("P2", "V2");
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 1----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 1----PAR");
        List<Object> outData = (List<Object>) gvBufferout.getObject();
        assertEquals(2, outData.size());
        assertEquals(TEST_BUFFER.toUpperCase(), outData.get(0));
        assertEquals(TEST_BUFFER.toLowerCase(), outData.get(1));
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBuffer.setProperty("P1", "V1");
        gvBuffer.setProperty("P2", "X");
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 2----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 2----PAR");
        outData = (List<Object>) gvBufferout.getObject();
        assertEquals(1, outData.size());
        assertEquals(TEST_BUFFER.toUpperCase(), outData.get(0));
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBuffer.setProperty("P1", "X");
        gvBuffer.setProperty("P2", "V2");
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 3----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 3----PAR");
        outData = (List<Object>) gvBufferout.getObject();
        assertEquals(1, outData.size());
        assertEquals(TEST_BUFFER.toLowerCase(), outData.get(0));
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBuffer.setProperty("P1", "X");
        gvBuffer.setProperty("P2", "X");
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 4----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 4----PAR");
        assertEquals(TEST_BUFFER, gvBufferout.getObject());
        assertEquals("SKIP", gvBufferout.getProperty("END"));
    }

    /**
     * @throws Exception
     */
    public void testTestParallelNormalEnd_OutJS() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TestParallelNormalEnd_OutJS";
        String TEST_BUFFER = "CiRo,NuNzIo,GiAnFrAnCo,AnToNiO";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        gvBuffer.setProperty("P1", "V1");
        gvBuffer.setProperty("P2", "V2");
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 1----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 1----PAR");
        assertEquals("[CiRo,NuNzIo,GiAnFrAnCo,AnToNiO -> CIRO,NUNZIO,GIANFRANCO,ANTONIO];[CiRo,NuNzIo,GiAnFrAnCo,AnToNiO -> ciro,nunzio,gianfranco,antonio]", gvBufferout.getObject());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBuffer.setProperty("P1", "V1");
        gvBuffer.setProperty("P2", "X");
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 2----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 2----PAR");
        assertEquals("[CiRo,NuNzIo,GiAnFrAnCo,AnToNiO -> CIRO,NUNZIO,GIANFRANCO,ANTONIO]", gvBufferout.getObject());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBuffer.setProperty("P1", "X");
        gvBuffer.setProperty("P2", "V2");
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 3----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 3----PAR");
        assertEquals("[CiRo,NuNzIo,GiAnFrAnCo,AnToNiO -> ciro,nunzio,gianfranco,antonio]", gvBufferout.getObject());
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBuffer.setProperty("P1", "X");
        gvBuffer.setProperty("P2", "X");
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 4----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 4----PAR");
        assertEquals(TEST_BUFFER, gvBufferout.getObject());
        assertEquals("SKIP", gvBufferout.getProperty("END"));
    }

    /**
     * @throws Exception
     */
    public void testTestParallelFirstEnd() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TestParallelFirstEnd";
        String TEST_BUFFER = "CiRo,NuNzIo";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 1----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 1----PAR");
        
		List<Object> outData = (List<Object>) gvBufferout.getObject();
        assertEquals(1, outData.size());
        assertEquals("nunzio", outData.get(0));
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 2----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 2----PAR");
        outData = (List<Object>) gvBufferout.getObject();
        assertEquals(1, outData.size());
        assertEquals("nunzio", outData.get(0));
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 3----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 3----PAR");
        outData = (List<Object>) gvBufferout.getObject();
        assertEquals(1, outData.size());
        assertEquals("nunzio", outData.get(0));
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

    }

    /**
     * @throws Exception
     */
    public void testTestParallelFirstError() throws Exception
    {
        String SYSTEM_NAME = "GVESB";
        String SERVICE_NAME = "TestParallelFirstError";
        String TEST_BUFFER = "GiAnFrAnCo,NuNzIo";
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setObject(TEST_BUFFER);
        GreenVulcano greenVulcano = new GreenVulcano();
        GVBuffer gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 1----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 1----PAR");
        List<Object> outData = (List<Object>) gvBufferout.getObject();
        assertEquals(1, outData.size());
        assertEquals("nunzio", outData.get(0));
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 2----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 2----PAR");
        outData = (List<Object>) gvBufferout.getObject();
        assertEquals(1, outData.size());
        assertEquals("nunzio", outData.get(0));
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

        id = new Id();
        gvBuffer.setId(id);
        gvBufferout = greenVulcano.requestReply(gvBuffer);
        assertEquals(SYSTEM_NAME, gvBufferout.getSystem());
        assertEquals(SERVICE_NAME, gvBufferout.getService());
        assertEquals(id, gvBufferout.getId());
        System.out.println("TEST 3----PAR");
        System.out.println(gvBufferout);
        System.out.println("TEST 3----PAR");
        outData = (List<Object>) gvBufferout.getObject();
        assertEquals(1, outData.size());
        assertEquals("nunzio", outData.get(0));
        assertEquals("DEFAULT", gvBufferout.getProperty("END"));

    }

}
