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
package tests.unit.vcl.dh;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.datahandler.DataHandlerCallOperation;
import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Mar 31, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class DataHandlerVCLTestCase extends TestCase
{
    private Connection connection;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
    	XMLConfig.setBaseConfigPath(getClass().getClassLoader().getResource(".").getPath());
        Context context = new InitialContext();
        try {
            DataSource ds = (DataSource) context.lookup("openejb:Resource/testDHDataSource");
            connection = ds.getConnection();
        }
        finally {
            context.close();
        }
        createDB();
    }

    /**
     * @throws SQLException
     *
     */
    private void createDB() throws SQLException
    {
        connection.prepareStatement("create table testtable (id INTEGER primary key, name VARCHAR(100));").execute();
        connection.prepareStatement("insert into testtable (id, name) values (1, 'testvalue');").execute();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        clearDB();
        connection.close();
    }

    /**
     * @throws SQLException
     *
     */
    private void clearDB() throws SQLException
    {
        connection.prepareStatement("drop table testtable;").execute();
    }

    /**
     * @throws Exception
     */
    public final void testDHCallSelect() throws Exception
    {
        DataHandlerCallOperation operation = new DataHandlerCallOperation();
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
        "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/dh-call[@name='test-dh-call-select' and @type='call']");
        operation.init(node);
        GVBuffer gvBuffer = new GVBuffer("GVESB", "TestSelect");
        gvBuffer.setObject(null);
        GVBuffer result = operation.perform(gvBuffer);
        assertNotNull(result);
        assertEquals(1, result.getRetCode());
        assertEquals("0", result.getProperty("REC_DISCARD"));
        assertEquals("0", result.getProperty("REC_UPDATE"));
        assertEquals("1", result.getProperty("REC_TOTAL"));
        assertEquals("0", result.getProperty("REC_INSERT"));
        assertEquals("1", result.getProperty("REC_READ"));
        assertEquals("", result.getProperty("REC_DISCARD_CAUSE"));
        Document output = (Document) result.getObject();
        assertNotNull(output);
        assertTrue(output.getDocumentElement().hasChildNodes());
        Node data = output.getDocumentElement().getChildNodes().item(0);
        assertTrue(data.hasChildNodes());
        Node row = data.getChildNodes().item(0);
        assertTrue(row.hasChildNodes());
        NodeList cols = row.getChildNodes();
        assertEquals(2, cols.getLength());
        String id = cols.item(0).getTextContent();
        assertEquals("1", id);
        String name = cols.item(1).getTextContent();
        assertEquals("testvalue", name);
    }

    /**
     * @throws Exception
     */
    public final void testDHCallInsertOrUpdate() throws Exception
    {
        DataHandlerCallOperation operation = new DataHandlerCallOperation();
        Node node = XMLConfig.getNode(
                "GVSystems.xml",
        "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/dh-call[@name='test-dh-call-insert' and @type='call']");
        operation.init(node);
        GVBuffer gvBuffer = new GVBuffer("GVESB", "TestInsert");
        gvBuffer.setObject(createInsertMessage());
        GVBuffer result = operation.perform(gvBuffer);
        assertEquals(1, result.getRetCode());
        assertEquals("0", result.getProperty("REC_DISCARD"));
        assertEquals("0", result.getProperty("REC_UPDATE"));
        assertEquals("2", result.getProperty("REC_TOTAL"));
        assertEquals("2", result.getProperty("REC_INSERT"));
        assertEquals("0", result.getProperty("REC_READ"));
        assertEquals("", result.getProperty("REC_DISCARD_CAUSE"));

        operation = new DataHandlerCallOperation();
        node = XMLConfig.getNode(
                "GVSystems.xml",
        "/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/dh-call[@name='test-dh-call-insert-or-update' and @type='call']");
        operation.init(node);
        gvBuffer = new GVBuffer("GVESB", "TestInsertOrUpdate");
        Document insertOrUpdateMessage = createInsertOrUpdateMessage();
        gvBuffer.setObject(insertOrUpdateMessage);
        result = operation.perform(gvBuffer);
        assertEquals(1, result.getRetCode());
        assertEquals("0", result.getProperty("REC_DISCARD"));
        assertEquals("1", result.getProperty("REC_UPDATE"));
        assertEquals("2", result.getProperty("REC_TOTAL"));
        assertEquals("1", result.getProperty("REC_INSERT"));
        assertEquals("0", result.getProperty("REC_READ"));
        assertEquals("", result.getProperty("REC_DISCARD_CAUSE"));
    }

    private Document createInsertMessage() throws Exception
    {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = documentFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element root = doc.createElement("RowSet");
        doc.appendChild(root);
        Element data = doc.createElement("data");
        root.appendChild(data);
        Element row = doc.createElement("row");
        data.appendChild(row);
        Element col1 = doc.createElement("col");
        col1.setAttribute("type", "integer");
        col1.appendChild(doc.createTextNode("2"));
        row.appendChild(col1);
        Element col2 = doc.createElement("col");
        col2.setAttribute("type", "string");
        col2.appendChild(doc.createTextNode("testvalue2"));
        row.appendChild(col2);
        row = doc.createElement("row");
        data.appendChild(row);
        col1 = doc.createElement("col");
        col1.setAttribute("type", "integer");
        col1.appendChild(doc.createTextNode("3"));
        row.appendChild(col1);
        col2 = doc.createElement("col");
        col2.setAttribute("type", "string");
        col2.appendChild(doc.createTextNode("testvalue3"));
        row.appendChild(col2);
        return doc;
    }

    private Document createInsertOrUpdateMessage() throws Exception
    {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = documentFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element root = doc.createElement("RowSet");
        doc.appendChild(root);
        Element data = doc.createElement("data");
        root.appendChild(data);

        // row
        Element row = doc.createElement("row");
        data.appendChild(row);

        // column
        Element col1 = doc.createElement("col");
        col1.setAttribute("type", "integer");
        col1.appendChild(doc.createTextNode("2"));
        row.appendChild(col1);
        Element col2 = doc.createElement("col");
        col2.setAttribute("type", "string");
        col2.appendChild(doc.createTextNode("testvalue2-new"));
        row.appendChild(col2);

        // column update
        Element col_update2 = doc.createElement("col-update");
        col_update2.setAttribute("type", "string");
        col_update2.appendChild(doc.createTextNode("testvalue2-new"));
        row.appendChild(col_update2);
        Element col_update1 = doc.createElement("col-update");
        col_update1.setAttribute("type", "integer");
        col_update1.appendChild(doc.createTextNode("2"));
        row.appendChild(col_update1);

        // row
        row = doc.createElement("row");
        data.appendChild(row);

        // column
        col1 = doc.createElement("col");
        col1.setAttribute("type", "integer");
        col1.appendChild(doc.createTextNode("4"));
        row.appendChild(col1);
        col2 = doc.createElement("col");
        col2.setAttribute("type", "string");
        col2.appendChild(doc.createTextNode("testvalue4"));
        row.appendChild(col2);

        // column update
        col_update2 = doc.createElement("col-update");
        col_update2.setAttribute("type", "string");
        col_update2.appendChild(doc.createTextNode("testvalue4"));
        row.appendChild(col_update2);
        col_update1 = doc.createElement("col-update");
        col_update1.setAttribute("type", "integer");
        col_update1.appendChild(doc.createTextNode("4"));
        row.appendChild(col_update1);

        return doc;
    }

}
