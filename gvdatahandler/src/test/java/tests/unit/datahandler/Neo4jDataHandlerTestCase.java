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

import it.greenvulcano.gvesb.datahandling.DHResult;
import it.greenvulcano.gvesb.datahandling.IDBOBuilder;
import it.greenvulcano.gvesb.datahandling.factory.DHFactory;
import junit.framework.TestCase;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.junit.Ignore;

/**
 * @version 3.5.0 Nov 07, 2015
 * @author GreenVulcano Developer Team
 *
 *
 */
@Ignore
public class Neo4jDataHandlerTestCase extends TestCase
{

	private Connection connection;

	private DHFactory  dhFactory;

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
			
			/*DatabaseMetaData dbmd = connection.getMetaData();  
			System.out.println("Connection Class: " + connection.getClass().getName());
            System.out.println("=====  Database info =====");  
            System.out.println("DatabaseProductName: " + dbmd.getDatabaseProductName() );  
            System.out.println("DatabaseProductVersion: " + dbmd.getDatabaseProductVersion() );  
            System.out.println("DatabaseMajorVersion: " + dbmd.getDatabaseMajorVersion() );  
            System.out.println("DatabaseMinorVersion: " + dbmd.getDatabaseMinorVersion() );  
            System.out.println("=====  Driver info =====");  
            System.out.println("DriverName: " + dbmd.getDriverName() );  
            System.out.println("DriverVersion: " + dbmd.getDriverVersion() );  
            System.out.println("DriverMajorVersion: " + dbmd.getDriverMajorVersion() );  
            System.out.println("DriverMinorVersion: " + dbmd.getDriverMinorVersion() ); 
            
            System.out.println("Check JDBC Driver:");
            Enumeration<java.sql.Driver> driversRegistered = DriverManager.getDrivers();
            while(driversRegistered.hasMoreElements()){
                java.sql.Driver driverSelected = (java.sql.Driver) driversRegistered.nextElement();
                System.out.println("Driver Class: " + driverSelected.getClass().getName());
                System.out.println("Driver Version: " + driverSelected.getMajorVersion() + "/" + driverSelected.getMinorVersion());
            }*/

		}
		finally {
			context.close();
		}
		CommonsNeo4j.createDB(connection);
		dhFactory = new DHFactory();
		dhFactory.initialize(null);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		if (dhFactory != null) {
			dhFactory.destroy();
		}
		CommonsNeo4j.clearDB(connection);
		connection.close();
	}

	/**
	 *
	 * 1. INSERT-SELECT WITH PROPS
	 * @throws Exception
	 *
	 */
	public void testDHCallInsertSelectWithPropsN4J() throws Exception
	{
		String operation = null;
		IDBOBuilder dboBuilder = null;
		Map<String, Object> params = new HashMap<String, Object>();
		DHResult result = null;

		//a. SELECT
		operation = "GVESB::TestSelectWithPropsN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		//MATCH (n {title: '@{{PARAM_TITLE}}'}) RETURN n
		params.put("PARAM_TITLE", "The Matrix");

		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("GVESB::TestSelectWithPropsN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(1, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		//b. INSERT
		operation = "GVESB::TestInsertWithPropsN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		//CREATE (TheMatrixNew : Movie {title : 'The Matrix', released: 2015, tagline: 'New Matrix film'})
		params.put("PARAM_TITLE", "The Matrix");
		params.put("PARAM_RELEASED", "2015");
		params.put("PARAM_TAG_LINE", "New Matrix film");

		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("GVESB::TestInsertWithPropsN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(1, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		//c. SELECT
		operation = "GVESB::TestSelectWithPropsN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		//MATCH (n {title: '@{{PARAM_TITLE}}'}) RETURN n
		params.put("PARAM_TITLE", "The Matrix");

		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("GVESB::TestSelectWithPropsN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(2, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
		System.out.println("DH_RESULT_SELECT: "+ result);
	}


	/**
	 *
	 * 2. INSER-SELECT WITH INPUT DATA PARAMS
	 * @throws Exception
	 *
	 */
	public void testDHCallInsertSelectWithInputDataParamsN4J() throws Exception
	{
		String operation = null;
		IDBOBuilder dboBuilder = null;
		Map<String, Object> params = new HashMap<String, Object>();
		DHResult result = null;

		//a. Insert with InputData CREATE (Movie: Movie {title: {1}, released: {2}, tagline: {3}} )
		operation = "GVESB::TestInsertWithInputDataN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);

		result = dboBuilder.EXECUTE(operation, CommonsNeo4j.createInsertXmlInputData(), null);
		System.out.println("GVESB::TestInsertWithInputDataN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(2, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());


		//b. SELECT
		operation = "GVESB::TestSelectWithPropsTagLineN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		//MATCH (n {title: '@{{PARAM_TITLE}}'}) RETURN n
		params.put("TAG_LINE_TEST_INPUT", "TEST");

		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("GVESB::TestSelectWithPropsTagLineN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(2, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
	}


	/**
	 *
	 * 3. INSERT-UPDATE-SELECT WITH PROPS
	 * @throws Exception
	 *
	 */
	public void testDHCallInsertUpdateSelectWithPropsN4J() throws Exception
	{
		String operation = null;
		IDBOBuilder dboBuilder = null;
		Map<String, Object> params = new HashMap<String, Object>();
		DHResult result = null;

		//a. Insert with InputData
		operation = "GVESB::TestInsertWithInputDataN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);

		result = dboBuilder.EXECUTE(operation, CommonsNeo4j.createInsertXmlInputData(), null);
		System.out.println("GVESB::TestInsertWithInputDataN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(2, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());


		//b. Update with props
		//<statement id="0" type="update">MATCH (n { title: '@{{TITLE_INPUT}}' }) SET n.tagline = '@{{TAG_LINE_TEST_INPUT}}'</statement>
		operation = "GVESB::TestUpdateWithPropsN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		params.put("TITLE_INPUT", "Film1");
		params.put("TAG_LINE_TEST_INPUT", "TEST_UPDATED");

		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("GVESB::TestUpdateWithPropsN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(1, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());


		//b. SELECT
		operation = "GVESB::TestSelectWithPropsTagLineN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		//MATCH (n {title: '@{{PARAM_TITLE}}'}) RETURN n
		params.put("TAG_LINE_TEST_INPUT", "TEST_UPDATED");

		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("GVESB::TestSelectWithPropsTagLineN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(1, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
	}

	/*
	 * 4. INSERT-UPDATE-SELECT WITH INPUT DATA PARAMS
	 * @throws Exception
	 *
	 */
	public void testDHCallInsertUpdateSelectWithInputDataN4J() throws Exception
	{
		String operation = null;
		IDBOBuilder dboBuilder = null;
		Map<String, Object> params = new HashMap<String, Object>();
		DHResult result = null;

		//a. Insert with InputData
		operation = "GVESB::TestInsertWithInputDataN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);

		result = dboBuilder.EXECUTE(operation, CommonsNeo4j.createInsertXmlInputData(), null);
		System.out.println("GVESB::TestInsertWithInputDataN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(2, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		//b. Update with input data
		//<statement id="0" type="update">MATCH (n { title: {1} }) SET n.tagline = {2}</statement>
		operation = "GVESB::TestUpdateWithInputDataN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);

		result = dboBuilder.EXECUTE(operation, CommonsNeo4j.createUpdateXmlInputData(), null);
		System.out.println("GVESB::TestUpdateWithInputDataN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(1, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		//b. SELECT
		operation = "GVESB::TestSelectWithPropsTagLineN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		//MATCH (n {title: '@{{PARAM_TITLE}}'}) RETURN n
		params.put("TAG_LINE_TEST_INPUT", "TEST_UPDATED");

		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("GVESB::TestSelectWithPropsTagLineN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(1, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
	}


	/*
	 * 5. DELETE-SELECT WITH PARAMS
	 * @throws Exception
	 *
	 */
	public void testDHCallDeleteSelectWithParamsN4J() throws Exception
	{
		String operation = null;
		IDBOBuilder dboBuilder = null;
		Map<String, Object> params = new HashMap<String, Object>();
		DHResult result = null;

		//a. Insert with input data params
		operation = "GVESB::TestInsertWithInputDataN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);

		result = dboBuilder.EXECUTE(operation, CommonsNeo4j.createInsertXmlInputData(), null);
		System.out.println("GVESB::TestInsertWithInputDataN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(2, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		//b. SELECT
		operation = "GVESB::TestSelectWithPropsTagLineN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		//MATCH (n {title: '@{{PARAM_TITLE}}'}) RETURN n
		params.put("TAG_LINE_TEST_INPUT", "TEST");

		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("GVESB::TestSelectWithPropsTagLineN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(2, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(2, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());


		//c. DELETE
		operation = "GVESB::TestDeleteWithPropsN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		//MATCH (n { title: '@{{TITLE_INPUT}}' }) OPTIONAL MATCH (n)-[r]-() DELETE n return n
		params.put("TITLE_INPUT", "Film1");

		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("GVESB::TestDeleteWithPropsN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(1, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		//b. SELECT
		operation = "GVESB::TestSelectWithPropsTagLineN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		//MATCH (n {title: '@{{PARAM_TITLE}}'}) RETURN n
		params.put("TAG_LINE_TEST_INPUT", "TEST");

		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("GVESB::TestSelectWithPropsTagLineN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(1, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
	}


	/*
	 * 6. MERGE modify element and SELECT.
	 * @throws Exception
	 *
	 */
	public void testDHCallMergeSelectWithParamsN4J() throws Exception
	{
		String operation = null;
		IDBOBuilder dboBuilder = null;
		Map<String, Object> params = new HashMap<String, Object>();
		DHResult result = null;

		//a. Insert with input data params
		operation = "GVESB::TestMergeWithPropsN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		//MERGE (keanu:Person { name:'Keanu Reeves' }) ON CREATE SET keanu.new_field = 'NEW_FIELD', keanu.created = timestamp() ON MATCH SET keanu.lastSeen = timestamp(), keanu.new_field = 'UPDATE_INSERT'  RETURN keanu.name, keanu.new_field, keanu.created, keanu.lastSeen</statement>
		params.put("NAME_ACTOR_INPUT", "Keanu Reeves");
		params.put("INPUT_NEW_FIELD", "NEW_FIELD");
		params.put("INPUT_NEW_FIELD_UPDATED", "UPDATE_INSERT");

		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("GVESB::TestMergeWithPropsN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(1, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(0, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());

		//b. SELECT
		operation = "GVESB::TestSelectWithPropsMergeN4J";
		dboBuilder = dhFactory.getDBOBuilder(operation);
		//MATCH (n {title: '@{{PARAM_TITLE}}'}) RETURN n
		params.put("INPUT_NEW_FIELD_UPDATED", "UPDATE_INSERT");

		result = dboBuilder.EXECUTE(operation, null, params);
		System.out.println("GVESB::TestSelectWithPropsMergeN4J = " + result);
		assertNotNull(result);
		assertEquals(0, result.getDiscard());
		assertEquals(0, result.getUpdate());
		assertEquals(1, result.getTotal());
		assertEquals(0, result.getInsert());
		assertEquals(1, result.getRead());
		assertEquals("", result.getDiscardCauseListAsString());
	}

}
