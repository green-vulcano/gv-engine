/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 */
package tests.unit.datahandler;

import java.sql.Connection;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @version 3.5.0 16/11/2015
 * @author GreenVulcano Developer Team
 */
public class CommonsNeo4j
{
    /**
     * @throws SQLException
     *
     */
    public static void createDB(Connection connection) throws SQLException
    {
        connection.prepareStatement(
                "CREATE (TheMatrix:Movie {title:'The Matrix', released:1999, tagline:'Welcome to the Real World'})\n" +
                ", (TheMatrixReloaded:Movie {title:'The Matrix Reloaded', released:2003, tagline:'Free your mind'})\n" +
                ", (Keanu:Person {name:'Keanu Reeves', born:1964})\n" +
                ", (Carrie:Person {name:'Carrie-Anne Moss', born:1967})\n" +
                ", (Keanu)-[:ACTED_IN {roles:['Neo']}]->(TheMatrix)\n" +
                ", (Keanu)-[:ACTED_IN {roles:['Neo']}]->(TheMatrixReloaded)\n" +
                ", (Carrie)-[:ACTED_IN {roles:['Trinity']}]->(TheMatrixReloaded)").execute();
    }

    public static void main(String[] args) throws Exception {
    	Document doc = CommonsNeo4j.createInsertXmlInputData();
    	System.out.println("doc: "+doc);
	}

    /**
     * @throws SQLException
     *
     */
    public static void clearDB(Connection connection) throws SQLException
    {
        System.out.println("Cleaning-up DB...");
        connection.prepareStatement("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n, r;").execute();
        System.out.println("Cleaning-up DB completed.");
    }

    public static Document createInsertXmlInputData() throws Exception
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
		col1.setAttribute("type", "string");
		col1.appendChild(doc.createTextNode("Film1"));
		row.appendChild(col1);

		Element col2 = doc.createElement("col");
		col2.setAttribute("type", "string");
		col2.appendChild(doc.createTextNode("1800"));
		row.appendChild(col2);

		Element col3 = doc.createElement("col");
		col3.setAttribute("type", "string");
		col3.appendChild(doc.createTextNode("TEST"));
		row.appendChild(col3);


		Element row2 = doc.createElement("row");
		data.appendChild(row2);
		col1 = doc.createElement("col");
		col1.setAttribute("type", "string");
		col1.appendChild(doc.createTextNode("Film2"));
		row2.appendChild(col1);

		col2 = doc.createElement("col");
		col2.setAttribute("type", "string");
		col2.appendChild(doc.createTextNode("1800"));
		row2.appendChild(col2);

		col3 = doc.createElement("col");
		col3.setAttribute("type", "string");
		col3.appendChild(doc.createTextNode("TEST"));
		row2.appendChild(col3);

		return doc;
	}


    public static Document createUpdateXmlInputData() throws Exception
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
		col1.setAttribute("type", "string");
		col1.appendChild(doc.createTextNode("Film1"));
		row.appendChild(col1);

		Element col2 = doc.createElement("col");
		col2.setAttribute("type", "string");
		col2.appendChild(doc.createTextNode("TEST_UPDATED"));
		row.appendChild(col2);

		return doc;
	}
}
