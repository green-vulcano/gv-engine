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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.junit.Ignore;
import org.w3c.dom.Node;

import tests.unit.datahandler.CommonsNeo4j;

/**
 * @version 3.5.0 Nov 11, 2015
 * @author GreenVulcano Developer Team
 *
 */
@Ignore
public class Neo4jDataHandlerVCLTestCase extends TestCase
{
	private Connection connection;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		Context context = new InitialContext();
		try {
			DataSource ds = (DataSource) context.lookup("openejb:Resource/testDHDataSourceN4J");
			connection = ds.getConnection();
		}
		finally {
			context.close();
		}
		CommonsNeo4j.createDB(connection);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		CommonsNeo4j.clearDB(connection);
		connection.close();
	}

	/**
	 * @throws Exception
	 */
	public final void testDHCallInsertUpdateTransactionN4J() throws Exception
	{
		DataHandlerCallOperation operation = new DataHandlerCallOperation();
		Node node = XMLConfig.getNode(
				"GVSystems.xml",
				"/GVSystems/Systems/System[@id-system='GVESB']/Channel[@id-channel='TEST_CHANNEL']/dh-call[@name='test-dh-call-merge-N4J' and @type='call']");
		operation.init(node);
		GVBuffer gvBuffer = new GVBuffer("GVESB", "TestMergeWithPropsN4J");
		//gvBuffer.setObject(createInsertMessage());
		gvBuffer.setProperty("NAME_ACTOR_INPUT", "Keanu Reeves");
		gvBuffer.setProperty("INPUT_NEW_FIELD", "NEW_FIELD");
		gvBuffer.setProperty("INPUT_NEW_FIELD_UPDATED", "UPDATE_INSERT");

		GVBuffer result = operation.perform(gvBuffer);
		assertEquals(1, result.getRetCode());
		assertEquals("0", result.getProperty("REC_DISCARD"));
		assertEquals("1", result.getProperty("REC_UPDATE"));
		assertEquals("1", result.getProperty("REC_TOTAL"));
		assertEquals("0", result.getProperty("REC_INSERT"));
		assertEquals("0", result.getProperty("REC_READ"));
		assertEquals("", result.getProperty("REC_DISCARD_CAUSE"));

		// TBD: MERGE RESULT AS AN UPDATE BECAUSE IN THE XML IS AN update Statement.
		gvBuffer = new GVBuffer("GVESB", "TestMergeWithPropsN4J");
		gvBuffer.setProperty("NAME_ACTOR_INPUT", "Keanu Reeves2");
		gvBuffer.setProperty("INPUT_NEW_FIELD", "NEW_FIELD");
		gvBuffer.setProperty("INPUT_NEW_FIELD_UPDATED", "UPDATE_INSERT");

		result = operation.perform(gvBuffer);
		assertEquals(1, result.getRetCode());
		assertEquals("0", result.getProperty("REC_DISCARD"));
		assertEquals("1", result.getProperty("REC_UPDATE"));
		assertEquals("1", result.getProperty("REC_TOTAL"));
		assertEquals("0", result.getProperty("REC_INSERT"));
		assertEquals("0", result.getProperty("REC_READ"));
		assertEquals("", result.getProperty("REC_DISCARD_CAUSE"));
	}

}
