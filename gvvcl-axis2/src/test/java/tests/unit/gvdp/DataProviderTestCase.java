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
package tests.unit.gvdp;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayInputStream;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import tests.unit.http.HttpCallTestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
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
    	XMLConfig.setBaseConfigPath(HttpCallTestCase.class.getClassLoader().getResource(".").getPath());
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
     * Tests the event collection data provider
     * 
     * @throws Exception
     */
    public final void testEventCollectionDataProvider() throws Exception
    {
        System.out.println("Start: testEventCollectionDataProvider");
        String refDP = "eventCollectionDataProvider";
        IDataProvider iDataProvider = dataProvidersManager.getDataProvider(refDP);
        try {
            GVBuffer testBuffer = getTestBuffer();
            //<?xml version="1.0" encoding="UTF-8"?><Radice><nodo1/><!--numero 2--><nodo2>Nodo numero due</nodo2><nodo3/><nodo4/><nodo5/><nodo6/><nodo7/><nodo8/><nodo9/><?ciao echo('pippo');?></Radice>
            byte[] buf = XMLUtils.serializeDOMToByteArray_S((Node) testBuffer.getObject());
            ByteArrayInputStream stream = new ByteArrayInputStream(buf);
            XMLStreamReader streamReader = StAXUtils.createXMLStreamReader(stream);
            StAXOMBuilder builder = new StAXOMBuilder(OMAbstractFactory.getOMFactory(), streamReader);
            OMDocument document = builder.getDocument();
            OMElement documentElement = document.getOMDocumentElement();
            System.out.println("---------------------------------");
            System.out.println("XMLStreamReader: " + documentElement.getXMLStreamReaderWithoutCaching().getClass());
            System.out.println("---------------------------------");
            testBuffer.setObject(documentElement);
            iDataProvider.setObject(testBuffer);

            Object result = iDataProvider.getResult();
            boolean found = result instanceof Collection<?>;
            assertTrue("Testing element not found", found);

            Collection<?> output = (Collection<?>) result;
            assertTrue("Bad output collection size", (output != null) && (output.size() == 9));
        }
        finally {
            dataProvidersManager.releaseDataProvider(refDP, iDataProvider);
        }
    }


}
