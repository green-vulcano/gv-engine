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

package tests.unit.datahandler;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.datahandling.DHResult;
import it.greenvulcano.gvesb.datahandling.IDBOBuilder;
import it.greenvulcano.gvesb.datahandling.factory.DHFactory;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;
import junit.framework.TestCase;

import java.io.File;
import java.sql.Connection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Dec 13, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DataHandlerTestCase extends TestCase {
	private static final String BASE_DIR = "target" + File.separator + "test-classes";

	private Connection connection;

	private DHFactory dhFactory;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		XMLConfig.setBaseConfigPath(getClass().getClassLoader().getResource(".").getPath());
		Context context = new InitialContext();
		try {
			DataSource ds = (DataSource) context.lookup("openejb:Resource/testDHDataSource");
			connection = ds.getConnection();
		} finally {
			context.close();
		}
		Commons.createDB(connection);
		dhFactory = new DHFactory();
		dhFactory.initialize(null);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		if (dhFactory != null) {
			dhFactory.destroy();
		}
		Commons.clearDB(connection);
		connection.close();
	}

	/**
	 * @throws Exception
	 *
	 */
	public void testDHCallSelect() throws Exception {
		String operation = "GVESB::TestSelect";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		DHResult result = dboBuilder.EXECUTE(operation, null, null);
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
	 *
	 */
	public void testDHCallSelectMulti() throws Exception {
		String operation = "GVESB::TestSelectMulti";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		DHResult result = dboBuilder.EXECUTE(operation, null, null);
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
		assertEquals(11, cols.getLength());
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
	 *
	 */
	public void testDHCallThreadSelect() throws Exception {
		String operation = "GVESB::TestThreadSelect";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		DHResult result = dboBuilder.EXECUTE(operation, null, null);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(2, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
		Document output = (Document) result.getData();
		assertNotNull(output);
		assertTrue(output.getDocumentElement().hasChildNodes());

		NodeList datas = output.getDocumentElement().getChildNodes();
		assertEquals(2, datas.getLength());
		for (int i = 0; i < datas.getLength(); i++) {
			Element data = (Element) datas.item(i);
			if ("0".equals(data.getAttribute("id"))) {
				testData0(data);
			} else {
				testData1(data);
			}
		}
	}

	private void testData0(Node data) {
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

	private void testData1(Node data) {
		assertTrue(data.hasChildNodes());
		Node row = data.getChildNodes().item(0);
		assertTrue(row.hasChildNodes());
		NodeList cols = row.getChildNodes();
		assertEquals(4, cols.getLength());
		String id = cols.item(0).getTextContent();
		assertEquals("1", id);
		String field3 = cols.item(1).getTextContent();
		assertEquals("123,45", field3);
		String field1 = cols.item(2).getTextContent();
		assertEquals("testvalue", field1);
		String field2 = cols.item(3).getTextContent();
		assertEquals("2000-01-01 12:30:45", field2);
	}

	/**
	 * @throws Exception
	 */
	public void testDHCallSelectMerge() throws Exception {
		String operation = "GVESB::TestSelectMerge";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		DHResult result = dboBuilder.EXECUTE(operation, null, null);
		assertNotNull(result);
		Document output = (Document) result.getData();
		assertNotNull(output);
		System.out.println("TestSelectMerge: " + XMLUtils.serializeDOM_S(output));
	}

	/**
	 * @throws Exception
	 */
	public final void testDHCallInsertOrUpdate() throws Exception {
		String operation = "GVESB::TestInsert";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		DHResult result = dboBuilder.EXECUTE(operation, Commons.createInsertMessage(), null);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(2, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		operation = "GVESB::TestInsertNP";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		result = dboBuilder.EXECUTE(operation, Commons.createInsertNPMessage(), null);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(2, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		operation = "GVESB::TestUpdateNP";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		result = dboBuilder.EXECUTE(operation, Commons.createUpdateNPMessage(), null);
		assertEquals(0, result.getDiscard());
		assertEquals(2, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		operation = "GVESB::TestInsertMixNP";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		result = dboBuilder.EXECUTE(operation, Commons.createInsertMixNPMessage(), null);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(4, result.getTotal());
		assertEquals(4, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		operation = "GVESB::TestUpdateMixNP";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		result = dboBuilder.EXECUTE(operation, Commons.createUpdateMixNPMessage(), null);
		assertEquals(0, result.getDiscard());
		assertEquals(4, result.getUpdate());
		assertEquals(4, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		operation = "GVESB::TestInsertOrUpdate";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		result = dboBuilder.EXECUTE(operation, Commons.createInsertOrUpdateMessage(), null);
		assertEquals(0, result.getDiscard());
		assertEquals(1, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(1, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
	}

	/**
	 * @throws Exception
	 */
	public final void testDHCallMultiInsertOrUpdate() throws Exception {
		String operation = "GVESB::TestInsertMulti";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		DHResult result = dboBuilder.EXECUTE(operation, Commons.createInsertMultiMessage(), null);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(2, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		operation = "GVESB::TestInsertMultiNP";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		result = dboBuilder.EXECUTE(operation, Commons.createInsertMultiNPMessage(), null);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(2, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		operation = "GVESB::TestUpdateMultiNP";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		result = dboBuilder.EXECUTE(operation, Commons.createUpdateMultiNPMessage(), null);
		assertEquals(0, result.getDiscard());
		assertEquals(2, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		operation = "GVESB::TestInsertMultiMixNP";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		result = dboBuilder.EXECUTE(operation, Commons.createInsertMultiMixNPMessage(), null);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(4, result.getTotal());
		assertEquals(4, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		operation = "GVESB::TestUpdateMultiMixNP";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		result = dboBuilder.EXECUTE(operation, Commons.createUpdateMultiMixNPMessage(), null);
		assertEquals(0, result.getDiscard());
		assertEquals(4, result.getUpdate());
		assertEquals(4, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		operation = "GVESB::TestInsertOrUpdateMulti";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		result = dboBuilder.EXECUTE(operation, Commons.createInsertOrUpdateMultiMessage(), null);
		assertEquals(0, result.getDiscard());
		assertEquals(1, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(1, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
	}

	/**
	 * @throws Exception
	 *
	 */
	public void testDHCallFlatSelect() throws Exception {
		String operation = "GVESB::TestFlatSelect";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		DHResult result = dboBuilder.EXECUTE(operation, null, null);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(1, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
		Object out = result.getData();
		assertNotNull(out);
		String output = new String((byte[]) out);
		assertEquals("1@testvalue.....................@20000101 123045@123,45@\n", output);
	}

	/**
	 * @throws Exception
	 *
	 */
	public void testDHCallFlatTZoneSelect() throws Exception {
		String operation = "GVESB::TestFlatTZoneSelect";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		DHResult result = dboBuilder.EXECUTE(operation, null, null);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(1, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
		Object out = result.getData();
		assertNotNull(out);
		String output = new String((byte[]) out);
		assertEquals("1@testvalue.....................@20000101 113045@123,45@\n", output);
	}

	public void testDHCallFlatSelectFile() throws Exception {
		System.setProperty("gv.app.home", BASE_DIR);
		String operation = "GVESB::TestFlatSelectFile";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		DHResult result = dboBuilder.EXECUTE(operation, null, null);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(1, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
		Object out = result.getData();
		assertNotNull(out);
		String output = TextUtils.readFile(PropertiesHandler.expand("sp{{gv.app.home}}/log/TestFlatSelectFile.csv"));
		assertEquals("1@testvalue.....................@20000101 123045@123,45@\n", output);
	}

	public void testDHCallMultiFlatSelectFile() throws Exception {
		System.setProperty("gv.app.home", BASE_DIR);
		String operation = "GVESB::TestMultiFlatSelectFile";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		DHResult result = dboBuilder.EXECUTE(operation, null, null);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(2, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
		Object out = result.getData();
		assertNotNull(out);
		String output = TextUtils
				.readFile(PropertiesHandler.expand("sp{{gv.app.home}}/log/TestMultiFlatSelectFile.csv"));
		assertEquals("id@field1@field2@field3@\n1@testvalue.....................@20000101 123045@123,45@\n", output);
	}

	public void testDHCallFlatTZoneSelectFile() throws Exception {
		System.setProperty("gv.app.home", BASE_DIR);
		String operation = "GVESB::TestFlatTZoneSelectFile";
		IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
		DHResult result = dboBuilder.EXECUTE(operation, null, null);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(1, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
		Object out = result.getData();
		assertNotNull(out);
		String output = TextUtils
				.readFile(PropertiesHandler.expand("sp{{gv.app.home}}/log/TestFlatTZoneSelectFile.csv"));
		assertEquals("1@testvalue.....................@20000101 113045@123,45@\n", output);
	}

}
