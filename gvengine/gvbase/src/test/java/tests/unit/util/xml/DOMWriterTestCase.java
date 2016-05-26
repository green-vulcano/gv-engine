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
package tests.unit.util.xml;

import it.greenvulcano.util.xml.DOMWriter;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class DOMWriterTestCase extends XMLTestCase
{
    private static final String lsep            = System.getProperty("line.separator");
    private static final String TEST_XML        = "<DOMWriterTest><firstChild>child value</firstChild></DOMWriterTest>";
    private static final String EXPECTED_RESULT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lsep + lsep + lsep
                                                        + "<DOMWriterTest>" + lsep
                                                        + "    <firstChild>child value</firstChild>" + lsep
                                                        + "</DOMWriterTest>";

    /**
     * @throws Exception
     */
    public void testDOMWriter() throws Exception
    {
        InputSource source = new InputSource(new StringReader(TEST_XML));
        int pw = 132;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(source);
        DOMWriter writer = new DOMWriter();
        writer.setPreferredWidth(pw);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.write(doc, baos);
        baos.close();
        assertXMLEqual("testDOMWriter failed", EXPECTED_RESULT, baos.toString());
    }
}
