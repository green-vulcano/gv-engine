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

package tests.unit.datahandler;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.datahandling.DHResult;
import it.greenvulcano.gvesb.datahandling.utils.dao.DataAccessObject;
import junit.framework.TestCase;

import java.sql.Connection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.1.0 Feb 17, 2011
 * @author GreenVulcano Developer Team
 */
public class DataHandlerPoolTestCase extends TestCase
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
        Commons.createDB(connection);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        Commons.clearDB(connection);
        connection.close();
    }

    /**
     * @throws Exception
     *
     */
    public void testDHCallSelect() throws Exception
    {
        String operation = "GVESB::TestSelect";
        DHResult result = DataAccessObject.execute(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(1, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(1, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
        Document output = (Document) result.getData();
        assertNotNull(output);
        assertTrue(output.getDocumentElement().hasChildNodes());
        Node data = output.getDocumentElement().getChildNodes().item(0);
        assertTrue(data.hasChildNodes());
        Node row = data.getChildNodes().item(0);
        assertTrue(row.hasChildNodes());
        NodeList cols = row.getChildNodes();
        assertEquals(4, cols.getLength());
        String id = cols.item(0).getTextContent();
        assertEquals("1", id);
        String field1 = cols.item(1).getTextContent();
        assertEquals("testvalue", field1);
        String field2 = cols.item(2).getTextContent();
        assertEquals("2000-01-01 12:30:45", field2);
        String field3 = cols.item(3).getTextContent();
        assertEquals("123,45", field3);
    }

    /**
     * @throws Exception
     */
    public final void testDHCallInsertOrUpdate() throws Exception
    {
        String operation = "GVESB::TestInsert";
        DHResult result = DataAccessObject.execute(operation, Commons.createInsertMessage(), null);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getInsert());
        assertEquals(0, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());

        operation = "GVESB::TestInsertOrUpdate";
        result = DataAccessObject.execute(operation, Commons.createInsertOrUpdateMessage(), null);
        assertEquals(0, result.getDiscard());
        assertEquals(1, result.getUpdate());
        assertEquals(2, result.getTotal());
        assertEquals(1, result.getInsert());
        assertEquals(0, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
    }
}
