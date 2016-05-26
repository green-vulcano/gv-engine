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
package tests.unit.gvdp;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.FieldExpressionKey;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import junit.framework.TestCase;

import org.junit.Ignore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Mar 2, 2010
 * @author GreenVulcano Developer Team
 * 
 */
@Ignore
public class DataProviderTestCase extends TestCase
{
    private static final String TEST_SYSTEM          = "TEST_SYSTEM";
    private static final String TEST_SERVICE         = "TEST_SERVICE";
    private static final String TEST_PROPERTY        = "XMLProperty";
    private static final String TEST_XML             = "<ns1:headerNews xmlns:ns1=\"http://www.greenvulcano.it/gvesb\"><ns1:autsign>MAK</ns1:autsign><ns1:credate>2006-06-13T09:44:21.500Z</ns1:credate><ns1:takenr>1</ns1:takenr><ns1:version>1</ns1:version><ns1:title>Title</ns1:title><ns1:subtitle>Subtitle</ns1:subtitle><ns1:priority>1</ns1:priority><ns1:keytitle>Keytitle</ns1:keytitle><ns1:cresign>Cresign</ns1:cresign><ns1:categ>Cat</ns1:categ><ns1:subcateg>sub</ns1:subcateg><ns1:subjrefnr1>subj1</ns1:subjrefnr1><ns1:subjrefnr2>subj2</ns1:subjrefnr2><ns1:subjrefnr3>subj3</ns1:subjrefnr3><ns1:intaddr>INTADDR</ns1:intaddr><ns1:intqbx>QBX</ns1:intqbx><ns1:msg>MSG</ns1:msg><ns1:takeid>1</ns1:takeid><ns1:typesign>typeSign</ns1:typesign></ns1:headerNews>";

    private DataProviderManager dataProvidersManager = null;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        dataProvidersManager = DataProviderManager.instance();
    }

    private GVBuffer getTestBuffer() throws Exception
    {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = documentFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Node e, radice = doc.createElement("Radice");
        doc.appendChild(radice);

        for (int i = 1; i < 10; i++) {
            radice.appendChild(doc.createElement("nodo" + i));
        }

        e = radice.getFirstChild().getNextSibling();
        radice.insertBefore(doc.createComment("numero 2"), e);
        e.appendChild(doc.createTextNode("Nodo numero due"));
        radice.appendChild(doc.createProcessingInstruction("ciao", "echo('pippo');"));
        GVBuffer gvBuffer = new GVBuffer();
        gvBuffer.setSystem(TEST_SYSTEM);
        gvBuffer.setService(TEST_SERVICE);
        gvBuffer.setObject(radice);
        gvBuffer.setProperty(TEST_PROPERTY, TEST_XML);
        return gvBuffer;
    }

    /**
     * Tests map data provider
     * 
     * @throws Exception
     *         if any error occurs
     */
    public final void testMapDataProvider() throws Exception
    {
        System.out.println("Start: testMapDataProvider");
        String refDP = "mapDataProvider";
        IDataProvider iDataProvider = dataProvidersManager.getDataProvider(refDP);
        try {
            iDataProvider.setObject(getTestBuffer());

            FieldExpressionKey fieldKey = iDataProvider.getFieldKey("system");
            assertNotNull(fieldKey);
            assertEquals(TEST_SYSTEM, iDataProvider.getValue(fieldKey));
            assertEquals(TEST_SERVICE, iDataProvider.getValue("service"));
        }
        finally {
            dataProvidersManager.releaseDataProvider(refDP, iDataProvider);
        }
    }

    /**
     * Tests map data provider
     * 
     * @throws Exception
     *         if any error occurs
     */
    public final void testMapDataProvider_JS() throws Exception
    {
        System.out.println("Start: testMapDataProvider_JS");
        String refDP = "JS-mapDataProvider";
        IDataProvider iDataProvider = dataProvidersManager.getDataProvider(refDP);
        try {
            iDataProvider.setObject(getTestBuffer());

            FieldExpressionKey fieldKey = iDataProvider.getFieldKey("system");
            assertNotNull(fieldKey);
            assertEquals(TEST_SYSTEM, iDataProvider.getValue(fieldKey));
            assertEquals(TEST_SERVICE, iDataProvider.getValue("service"));
        }
        finally {
            dataProvidersManager.releaseDataProvider(refDP, iDataProvider);
        }
    }

    /**
     * Tests array data provider
     * 
     * @throws Exception
     */
    public final void testArrayDataProvider() throws Exception
    {
        System.out.println("Start: testArrayDataProvider");
        String refDP = "arrayDataProvider";
        IDataProvider iDataProvider = dataProvidersManager.getDataProvider(refDP);
        try {
            iDataProvider.setObject(getTestBuffer());

            FieldExpressionKey fieldKey = iDataProvider.getFieldKey("0");
            assertNotNull(fieldKey);
            assertEquals(TEST_SYSTEM, iDataProvider.getValue(fieldKey));
            assertEquals(TEST_SERVICE, iDataProvider.getValue("1"));
        }
        finally {
            dataProvidersManager.releaseDataProvider(refDP, iDataProvider);
        }
    }

    /**
     * Tests array data provider
     * 
     * @throws Exception
     */
    public final void testArrayDataProvider_JS() throws Exception
    {
        System.out.println("Start: testArrayDataProvider_JS");
        String refDP = "JS-arrayDataProvider";
        IDataProvider iDataProvider = dataProvidersManager.getDataProvider(refDP);
        try {
            iDataProvider.setObject(getTestBuffer());

            FieldExpressionKey fieldKey = iDataProvider.getFieldKey("0");
            assertNotNull(fieldKey);
            assertEquals(TEST_SYSTEM, iDataProvider.getValue(fieldKey));
            assertEquals(TEST_SERVICE, iDataProvider.getValue("1"));
        }
        finally {
            dataProvidersManager.releaseDataProvider(refDP, iDataProvider);
        }
    }

    /**
     * Tests the object data provider
     * 
     * @throws Exception
     */
    public final void testObjectDataProvider() throws Exception
    {
        System.out.println("Start: testObjectDataProvider");
        String refDP = "objectDataProvider";
        IDataProvider iDataProvider = dataProvidersManager.getDataProvider(refDP);
        try {
            GVBuffer testBuffer = getTestBuffer();
            String input = "test";
            testBuffer.setObject(input.getBytes("UTF-8"));
            iDataProvider.setObject(testBuffer);

            FieldExpressionKey fieldKey = iDataProvider.getFieldKey("buffer");
            assertNotNull(fieldKey);
            assertEquals(input, iDataProvider.getValue(fieldKey));
        }
        finally {
            dataProvidersManager.releaseDataProvider(refDP, iDataProvider);
        }
    }

    /**
     * Tests the object data provider
     * 
     * @throws Exception
     */
    public final void testObjectDataProvider_JS() throws Exception
    {
        System.out.println("Start: testObjectDataProvider_JS");
        String refDP = "JS-objectDataProvider";
        IDataProvider iDataProvider = dataProvidersManager.getDataProvider(refDP);
        try {
            GVBuffer testBuffer = getTestBuffer();
            String input = "test";
            testBuffer.setObject(input.getBytes("UTF-8"));
            iDataProvider.setObject(testBuffer);

            FieldExpressionKey fieldKey = iDataProvider.getFieldKey("buffer");
            assertNotNull(fieldKey);
            assertEquals(input, iDataProvider.getValue(fieldKey));
        }
        finally {
            dataProvidersManager.releaseDataProvider(refDP, iDataProvider);
        }
    }

    /**
     * Tests the string data provider
     * 
     * @throws Exception
     */
    public final void testStringDataProvider() throws Exception
    {
        System.out.println("Start: testStringDataProvider");
        String refDP = "stringDataProvider";
        IDataProvider iDataProvider = dataProvidersManager.getDataProvider(refDP);
        try {
            GVBuffer testBuffer = getTestBuffer();
            String input = "test";
            testBuffer.setObject(input.getBytes("UTF-8"));
            iDataProvider.setObject(testBuffer);

            FieldExpressionKey fieldKey = iDataProvider.getFieldKey("buffer");
            assertNotNull(fieldKey);
            assertEquals(input, iDataProvider.getValue(fieldKey));
        }
        finally {
            dataProvidersManager.releaseDataProvider(refDP, iDataProvider);
        }
    }

    /**
     * Tests the string data provider
     * 
     * @throws Exception
     */
    public final void testStringDataProvider_JS() throws Exception
    {
        System.out.println("Start: testStringDataProvider_JS");
        String refDP = "JS-stringDataProvider";
        IDataProvider iDataProvider = dataProvidersManager.getDataProvider(refDP);
        try {
            GVBuffer testBuffer = getTestBuffer();
            String input = "test";
            testBuffer.setObject(input.getBytes("UTF-8"));
            iDataProvider.setObject(testBuffer);

            FieldExpressionKey fieldKey = iDataProvider.getFieldKey("buffer");
            assertNotNull(fieldKey);
            assertEquals(input, iDataProvider.getValue(fieldKey));
        }
        finally {
            dataProvidersManager.releaseDataProvider(refDP, iDataProvider);
        }
    }

    /**
     * Tests the collection data provider
     * 
     * @throws Exception
     */
    public final void testCollectionDataProvider() throws Exception
    {
        System.out.println("Start: testCollectionDataProvider");
        String refDP = "xpathNodeCollectionDataProvider";
        IDataProvider iDataProvider = dataProvidersManager.getDataProvider(refDP);
        try {
            GVBuffer testBuffer = getTestBuffer();
            iDataProvider.setObject(testBuffer);

            FieldExpressionKey fieldKey = iDataProvider.getFieldKey("buffer");
            assertNotNull(fieldKey);
            boolean found = false;
            Collection<?> values = (Collection<?>) iDataProvider.getValue(fieldKey);
            for (Object object : values) {
                Node current = (Node) object;
                if ("nodo2".equals(current.getNodeName())) {
                    found = true;
                    assertEquals(1, current.getChildNodes().getLength());
                }
            }
            assertTrue("Testing element not found", found);
        }
        finally {
            dataProvidersManager.releaseDataProvider(refDP, iDataProvider);
        }
        refDP = "regexCollectionDataProvider";
        iDataProvider = dataProvidersManager.getDataProvider(refDP);
        try {
            GVBuffer testBuffer = getTestBuffer();
            testBuffer.setObject("nodo1,nodo2,nodo3");
            iDataProvider.setObject(testBuffer);

            FieldExpressionKey fieldKey = iDataProvider.getFieldKey("buffer");
            assertNotNull(fieldKey);
            boolean found = false;
            Collection<?> values = (Collection<?>) iDataProvider.getValue(fieldKey);
            for (Object object : values) {
                String current = (String) object;
                if ("nodo2".equals(current)) {
                    found = true;
                }
            }
            assertTrue("Testing element not found", found);
        }
        finally {
            dataProvidersManager.releaseDataProvider(refDP, iDataProvider);
        }
        refDP = "ognlCollectionDataProvider";
        iDataProvider = dataProvidersManager.getDataProvider(refDP);
        try {
            GVBuffer testBuffer = getTestBuffer();
            testBuffer.setObject("nodo1,nodo2,nodo3");
            iDataProvider.setObject(testBuffer);

            FieldExpressionKey fieldKey = iDataProvider.getFieldKey("buffer");
            assertNotNull(fieldKey);
            boolean found = false;
            Collection<?> values = (Collection<?>) iDataProvider.getValue(fieldKey);
            for (Object object : values) {
                String current = (String) object;
                if ("nodo2".equals(current)) {
                    found = true;
                }
            }
            assertTrue("Testing element not found", found);
        }
        finally {
            dataProvidersManager.releaseDataProvider(refDP, iDataProvider);
        }
    }   

    /**
     * Tests the GVBuffer data provider
     * 
     * @throws Exception
     */
    public final void testGVBufferDataProvider() throws Exception
    {
        System.out.println("Start: testGVBufferDataProvider");
        String refDP = "gvBufferChangeDataProvider";
        IDataProvider iDataProvider = dataProvidersManager.getDataProvider(refDP);
        try {
            String buffer = "testTESTtest";
            String[] input = new String[]{"PIPPO", "PLUTO"};
            GVBuffer gvBuffer = new GVBuffer();
            gvBuffer.setSystem(TEST_SYSTEM);
            gvBuffer.setService(TEST_SERVICE);
            gvBuffer.setObject(buffer);

            iDataProvider.setContext(gvBuffer);
            iDataProvider.setObject(input);

            GVBuffer gvBufferOut = (GVBuffer) iDataProvider.getResult();
            assertNotNull(gvBufferOut);
            assertEquals(input[0], gvBufferOut.getSystem());
            assertEquals(input[1], gvBufferOut.getService());
            assertEquals(buffer, new String((byte[]) gvBufferOut.getObject()));
        }
        finally {
            dataProvidersManager.releaseDataProvider(refDP, iDataProvider);
        }
    }

    /**
     * Tests the GVBuffer data provider
     * 
     * @throws Exception
     */
    public final void testGVBufferDataProvider_JS() throws Exception
    {
        System.out.println("Start: testGVBufferDataProvider_JS");
        String refDP = "JS-gvBufferChangeDataProvider";
        IDataProvider iDataProvider = dataProvidersManager.getDataProvider(refDP);
        try {
            String buffer = "testTESTtest";
            String[] input = new String[]{"PIPPO", "PLUTO"};
            GVBuffer gvBuffer = new GVBuffer();
            gvBuffer.setSystem(TEST_SYSTEM);
            gvBuffer.setService(TEST_SERVICE);
            gvBuffer.setObject(buffer);

            iDataProvider.setContext(gvBuffer);
            iDataProvider.setObject(input);

            GVBuffer gvBufferOut = (GVBuffer) iDataProvider.getResult();
            assertNotNull(gvBufferOut);
            assertEquals(input[0], gvBufferOut.getSystem());
            assertEquals(input[1], gvBufferOut.getService());
            assertEquals(buffer, new String((byte[]) gvBufferOut.getObject()));
        }
        finally {
            dataProvidersManager.releaseDataProvider(refDP, iDataProvider);
        }
    }
}
