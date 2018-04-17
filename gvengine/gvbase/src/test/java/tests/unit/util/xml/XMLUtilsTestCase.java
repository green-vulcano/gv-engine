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
package tests.unit.util.xml;

import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayInputStream;

import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 */
public class XMLUtilsTestCase extends XMLTestCase
{
    private static final String TEST_STR   = "<DOMTest>& \" '";
    private static final String TEST_STR_2 = "&lt;DOMTest&gt;&amp; &quot; &apos;";
    private static final String TEST_XML   = "<DOMTest><firstChild>child value</firstChild><secondChild>second child value</secondChild></DOMTest>";
    private static final String TEST_XML_A = "<documentA><nodeA_1>Pippo</nodeA_1><nodeA_2>Pluto</nodeA_2></documentA>";
    private static final String TEST_XML_B = "<documentB><nodeB_1>Paperino</nodeB_1><nodeB_2>Paperina</nodeB_2></documentB>";
    private static final String TEST_XML_C = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><documentC><documentA><nodeA_1>Pippo</nodeA_1><nodeA_2>Pluto</nodeA_2></documentA><documentB><nodeB_1>Paperino</nodeB_1><nodeB_2>Paperina</nodeB_2></documentB></documentC>";

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#replaceXMLInvalidChars(java.lang.String)}
     * .
     */
    public void testReplaceXMLInvalidChars()
    {
        String result = XMLUtils.replaceXMLInvalidChars(TEST_STR);
        assertEquals("ReplaceXMLInvalidChars Failed", TEST_STR_2, result);
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#replaceXMLEntities(java.lang.String)}
     * .
     */
    public void testReplaceXMLEntities()
    {
        String result = XMLUtils.replaceXMLEntities(TEST_STR_2);
        assertEquals("ReplaceXMLEntities Failed", TEST_STR, result);
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#parseDOM(java.lang.String)}.
     */
    public void testParseDOMString()
    {
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            parser.parseDOM(TEST_XML);
        }
        catch (Exception exc) {
            exc.printStackTrace();
            fail("ParseDOMString: " + exc);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#parseDOM(byte[])}.
     */
    public void testParseDOMByteArray()
    {
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            parser.parseDOM(TEST_XML.getBytes());
        }
        catch (Exception exc) {
            exc.printStackTrace();
            fail("ParseDOMByteArray: " + exc);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#parseDOM(java.io.InputStream)}.
     */
    public void testParseDOMInputStream()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#parseDOM(java.lang.String, boolean, boolean)}
     * .
     */
    public void testParseDOMStringBooleanBoolean()
    {
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            parser.parseDOM(TEST_XML, false, true);
        }
        catch (Exception exc) {
            exc.printStackTrace();
            fail("ParseDOMStringBooleanBoolean: " + exc);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#parseDOM(byte[], boolean, boolean)}
     * .
     */
    public void testParseDOMByteArrayBooleanBoolean()
    {
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            parser.parseDOM(TEST_XML.getBytes(), false, true);
        }
        catch (Exception exc) {
            exc.printStackTrace();
            fail("ParseDOMByteArrayBooleanBoolean: " + exc);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#parseDOM(java.io.InputStream, boolean, boolean)}
     * .
     */
    public void testParseDOMInputStreamBooleanBoolean()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#parseObject(java.lang.Object, boolean, boolean)}
     * .
     */
    public void testParseObject()
    {
        XMLUtils parser = null;
        try {
            Document doc = null;
            parser = XMLUtils.getParserInstance();
            try {
                parser.parseObject(TEST_XML, false, true);
            }
            catch (Exception exc) {
                exc.printStackTrace();
                fail("ParseObject String: " + exc);
            }

            try {
                parser.parseObject(TEST_XML.getBytes(), false, true);
            }
            catch (Exception exc) {
                exc.printStackTrace();
                fail("ParseObject ByteArray: " + exc);
            }

            try {
                doc = (Document) parser.parseObject(new ByteArrayInputStream(TEST_XML.getBytes()), false, true);
            }
            catch (Exception exc) {
                exc.printStackTrace();
                fail("ParseObject InputStream: " + exc);
            }

            try {
                Document doc2 = (Document) parser.parseObject(doc, false, true);
                assertEquals("ParseObject Document Failed", doc, doc2);
            }
            catch (Exception exc) {
                exc.printStackTrace();
                fail("ParseObject Document: " + exc);
            }

            try {
                Node node = parser.parseObject(doc.getDocumentElement().getFirstChild(), false, true);
                assertEquals("ParseObject Node Failed", doc.getDocumentElement().getFirstChild(), node);
            }
            catch (Exception exc) {
                exc.printStackTrace();
                fail("ParseObject Node: " + exc);
            }

            try {
                parser.parseObject(new Integer(2), false, true);
                fail("ParseObject_S Invalid Object");
            }
            catch (Exception exc) {
                System.out.println(exc);
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
            fail("ParseObject: " + exc);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#parseDOM_S(java.lang.String)}.
     */
    public void testParseDOM_SString()
    {
        try {
            XMLUtils.parseDOM_S(TEST_XML);
        }
        catch (Exception exc) {
            exc.printStackTrace();
            fail("ParseDOM_SString: " + exc);
        }
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#parseDOM_S(java.lang.String, boolean, boolean)}
     * .
     */
    public void testParseDOM_SStringBooleanBoolean()
    {
        try {
            XMLUtils.parseDOM_S(TEST_XML, false, true);
        }
        catch (Exception exc) {
            exc.printStackTrace();
            fail("ParseDOM_SStringBooleanBoolean: " + exc);
        }
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#parseDOM_S(java.io.InputStream, boolean, boolean)}
     * .
     */
    public void testParseDOM_SInputStreamBooleanBoolean()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#parseDOM_S(byte[], boolean, boolean)}
     * .
     */
    public void testParseDOM_SByteArrayBooleanBoolean()
    {
        try {
            XMLUtils.parseDOM_S(TEST_XML.getBytes(), false, true);
        }
        catch (Exception exc) {
            exc.printStackTrace();
            fail("ParseDOM_SByteArrayBooleanBoolean: " + exc);
        }
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#parseObject_S(java.lang.Object, boolean, boolean)}
     * .
     */
    public void testParseObject_S()
    {
        try {
            Document doc = null;
            try {
                XMLUtils.parseObject_S(TEST_XML, false, true);
            }
            catch (Exception exc) {
                exc.printStackTrace();
                fail("ParseObject_S String: " + exc);
            }

            try {
                XMLUtils.parseObject_S(TEST_XML.getBytes(), false, true);
            }
            catch (Exception exc) {
                exc.printStackTrace();
                fail("ParseObject_S ByteArray: " + exc);
            }

            try {
                doc = (Document) XMLUtils.parseObject_S(new ByteArrayInputStream(TEST_XML.getBytes()), false, true);
            }
            catch (Exception exc) {
                exc.printStackTrace();
                fail("ParseObject_S InputStream: " + exc);
            }

            try {
                Document doc2 = (Document) XMLUtils.parseObject_S(doc, false, true);
                assertEquals("ParseObject_S Document Failed", doc, doc2);
            }
            catch (Exception exc) {
                exc.printStackTrace();
                fail("ParseObject_S Document: " + exc);
            }

            try {
                Node node = XMLUtils.parseObject_S(doc.getDocumentElement().getFirstChild(), false, true);
                assertEquals("ParseObject_S Node Failed", doc.getDocumentElement().getFirstChild(), node);
            }
            catch (Exception exc) {
                exc.printStackTrace();
                fail("ParseObject_S Node: " + exc);
            }

            try {
                XMLUtils.parseObject_S(new Integer(2), false, true);
                fail("ParseObject_S Invalid Object");
            }
            catch (Exception exc) {
                System.out.println(exc);
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
            fail("ParseObject_S: " + exc);
        }
    }


    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#aggregateXML(String, String, Object[])}
     * .
     */
    public void testAggregateXML()
    {
        XMLUtils parser = null;
        try {
            Document doc = null;
            parser = XMLUtils.getParserInstance();
            try {
                doc = parser.aggregateXML("documentC", null, new Object[]{TEST_XML_A, TEST_XML_B});
            }
            catch (Exception exc) {
                exc.printStackTrace();
                fail("AggregateXML: " + exc);
            }
            assertXMLEqual("AggregateXML Failed", TEST_XML_C, XMLUtils.serializeDOM_S(doc));
        }
        catch (Exception exc) {
            exc.printStackTrace();
            fail("AggregateXML: " + exc);
        }
        finally {
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
        }
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#aggregateXML_S(String, String, Object[])}
     * .
     */
    public void testAggregateXML_S()
    {
        try {
            Document doc = null;
            try {
                doc = XMLUtils.aggregateXML_S("documentC", null, new Object[]{TEST_XML_A, TEST_XML_B});
            }
            catch (Exception exc) {
                exc.printStackTrace();
                fail("AggregateXML_S: " + exc);
            }
            assertXMLEqual("AggregateXML_S Failed", TEST_XML_C, XMLUtils.serializeDOM_S(doc));
        }
        catch (Exception exc) {
            exc.printStackTrace();
            fail("AggregateXML_S: " + exc);
        }
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#parseDOMValidating(java.lang.String)}
     * .
     */
    public void testParseDOMValidatingString()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#parseDOMValidating(java.io.InputStream)}
     * .
     */
    public void testParseDOMValidatingInputStream()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#parseDOMValidating(java.lang.String, java.io.InputStream, org.xml.sax.EntityResolver, org.xml.sax.ErrorHandler)}
     * .
     */
    public void testParseDOMValidatingStringInputStreamEntityResolverErrorHandler()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#serializeDOM(org.w3c.dom.Node)}.
     */
    public void testSerializeDOMNode()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#serializeDOM_S(org.w3c.dom.Node)}
     * .
     */
    public void testSerializeDOM_SNode()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#serializeDOM_S(org.w3c.dom.Node, java.lang.String)}
     * .
     */
    public void testSerializeDOM_SNodeString()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#serializeDOM(org.w3c.dom.Document)}
     * .
     */
    public void testSerializeDOMDocument()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#serializeDOM(org.w3c.dom.Node, java.lang.String)}
     * .
     */
    public void testSerializeDOMNodeString()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#serializeDOM(org.w3c.dom.Document, java.lang.String)}
     * .
     */
    public void testSerializeDOMDocumentString()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#serializeDOM(org.w3c.dom.Node, boolean, boolean)}
     * .
     */
    public void testSerializeDOMNodeBooleanBoolean()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#serializeDOM(org.w3c.dom.Node, java.lang.String, boolean, boolean)}
     * .
     */
    public void testSerializeDOMNodeStringBooleanBoolean()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#serializeDOMToByteArray_S(org.w3c.dom.Node)}
     * .
     */
    public void testSerializeDOMToByteArray_SNode()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#serializeDOMToByteArray_S(org.w3c.dom.Document, java.lang.String)}
     * .
     */
    public void testSerializeDOMToByteArray_SDocumentString()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#serializeDOMToByteArray(org.w3c.dom.Node)}
     * .
     */
    public void testSerializeDOMToByteArrayNode()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#serializeDOMToByteArray(org.w3c.dom.Node, boolean, boolean)}
     * .
     */
    public void testSerializeDOMToByteArrayNodeBooleanBoolean()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#serializeDOMToByteArray(org.w3c.dom.Node, java.lang.String)}
     * .
     */
    public void testSerializeDOMToByteArrayNodeString()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#serializeDOMToByteArray(org.w3c.dom.Document, java.lang.String)}
     * .
     */
    public void testSerializeDOMToByteArrayDocumentString()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#serializeDOMToByteArray(org.w3c.dom.Node, java.lang.String, boolean, boolean)}
     * .
     */
    public void testSerializeDOMToByteArrayNodeStringBooleanBoolean()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#getNodeContent(org.w3c.dom.Node)}
     * .
     */
    public void testGetNodeContent()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#existNode(org.w3c.dom.Node, java.lang.String)}
     * .
     */
    public void testExistNode()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#selectSingleNode(org.w3c.dom.Node, java.lang.String)}
     * .
     */
    public void testSelectSingleNode()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#selectSingleNode_S(org.w3c.dom.Node, java.lang.String)}
     * .
     */
    public void testSelectSingleNode_SNodeString()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#selectSingleNode_S(java.lang.String, java.lang.String)}
     * .
     */
    public void testSelectSingleNode_SStringString()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#selectNodeList(org.w3c.dom.Node, java.lang.String)}
     * .
     */
    public void testSelectNodeList()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#selectNodeList_S(org.w3c.dom.Node, java.lang.String)}
     * .
     */
    public void testSelectNodeList_S()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#selectNodeIterator(org.w3c.dom.Node, java.lang.String)}
     * .
     */
    public void testSelectNodeIterator()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#get(org.w3c.dom.Node, java.lang.String)}
     * .
     */
    public void testGetNodeString()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#get(org.w3c.dom.Node, java.lang.String, java.lang.String)}
     * .
     */
    public void testGetNodeStringString()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#get_S(org.w3c.dom.Node, java.lang.String, java.lang.String)}
     * .
     */
    public void testGet_SNodeStringString()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#get_S(org.w3c.dom.Node, java.lang.String)}
     * .
     */
    public void testGet_SNodeString()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#setDefaultEntityResolver(org.xml.sax.EntityResolver)}
     * .
     */
    public void testSetDefaultEntityResolver()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#setEntityResolver(org.xml.sax.EntityResolver)}
     * .
     */
    public void testSetEntityResolver()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for {@link it.greenvulcano.util.xml.XMLUtils#newDocument()}.
     */
    public void testNewDocument()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#newDocument(java.lang.String)}.
     */
    public void testNewDocumentString()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#newDocument(java.lang.String, java.lang.String)}
     * .
     */
    public void testNewDocumentStringString()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#newDocument(java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    public void testNewDocumentStringStringString()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#createElement(org.w3c.dom.Document, java.lang.String)}
     * .
     */
    public void testCreateElement()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#insertElement(org.w3c.dom.Element, java.lang.String)}
     * .
     */
    public void testInsertElement()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#setAttribute(org.w3c.dom.Element, java.lang.String, java.lang.String)}
     * .
     */
    public void testSetAttribute()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#getDocumentBuilder(boolean, boolean, boolean)}
     * .
     */
    public void testGetDocumentBuilder()
    {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.xml.XMLUtils#applyXPath(org.w3c.dom.Document, java.lang.String)}
     * .
     */
    public void testApplyXPath()
    {
        //fail("Not yet implemented");
    }

}
