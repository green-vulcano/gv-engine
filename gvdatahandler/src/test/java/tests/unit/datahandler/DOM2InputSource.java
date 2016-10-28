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

import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @version 3.0.0 Mar 31, 2010
 * @author nunzio
 *
 *
 */
public class DOM2InputSource extends DefaultHandler
{
    private static String          xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><RowSet><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data><data id=\"0\"><row id=\"0\"><col type=\"numeric\">1</col><col type=\"string\">testvalue</col></row></data></RowSet>";
    private static Transformer     transformer;
    private static XMLReader       xr;

    private static TransformThread tt  = new TransformThread();

    private static final class TransformThread extends Thread
    {
        private boolean         stopped = false;

        private Stack<Object[]> queue   = new Stack<Object[]>();

        @Override
        public void run()
        {
            while (!stopped) {
                synchronized (queue) {
                    try {
                        queue.wait(100);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                while (!queue.isEmpty()) {
                    Object[] current = queue.pop();
                    if (current != null) {
                        Document doc = (Document) current[0];
                        OutputStream os = (OutputStream) current[1];
                        try {
                            transform(doc, os);
                        }
                        catch (TransformerException _ex) {
                            throw new RuntimeException("Failed to tranform org.w3c.dom.Document to OutputStream", _ex);
                        }
                        finally {
                            try {
                                os.close();
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        private void transform(Document doc, OutputStream pos) throws TransformerException
        {
            transformer.transform(new DOMSource(doc), new StreamResult(pos));
        }

    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        transformer = tFactory.newTransformer();
        xr = XMLReaderFactory.createXMLReader();

        Document doc = XMLUtils.getParserInstance().parseDOM(xml);
        // tt.setDaemon(true);
        tt.start();
        int n = 1000;
        for (int i = 0; i < n; i++) {
            testTransformCustom(doc, new DOM2InputSource());
        }
        for (int i = 0; i < n; i++) {
            testTransformWithBytesArray(doc);
        }
        for (int i = 0; i < n; i++) {
            testTransformWithPipes(doc);
        }
        Thread.sleep(2000);
        long pipesTime = 0;
        long baTime = 0;
        long customTime = 0;

        DOM2InputSource handler = new DOM2InputSource();

        doc = XMLUtils.getParserInstance().parseDOM(xml);
        xr.setContentHandler(handler);

        for (int i = 0; i < n; i++) {
            long start = System.currentTimeMillis();
            testTransformWithPipes(doc);
            pipesTime += (System.currentTimeMillis() - start);
        }
        System.out.println("pipes handler start element count: " + handler.startElementCounter);
        System.out.println("pipes handler characters count: " + handler.charactersCounter);
        System.out.println("pipes handler end element count: " + handler.endElementCounter);


        doc = XMLUtils.getParserInstance().parseDOM(xml);
        handler = new DOM2InputSource();
        xr.setContentHandler(handler);

        for (int i = 0; i < n; i++) {
            long start = System.currentTimeMillis();
            testTransformWithBytesArray(doc);
            baTime += (System.currentTimeMillis() - start);
        }
        System.out.println("ba handler start element count: " + handler.startElementCounter);
        System.out.println("ba handler characters count: " + handler.charactersCounter);
        System.out.println("ba handler end element count: " + handler.endElementCounter);

        doc = XMLUtils.getParserInstance().parseDOM(xml);
        handler = new DOM2InputSource();
        xr.setContentHandler(handler);


        for (int i = 0; i < n; i++) {
            long start = System.currentTimeMillis();
            testTransformCustom(doc, handler);
            customTime += (System.currentTimeMillis() - start);
        }
        System.out.println("custom handler start element count: " + handler.startElementCounter);
        System.out.println("custom handler characters count: " + handler.charactersCounter);
        System.out.println("custom handler end element count: " + handler.endElementCounter);

        @SuppressWarnings("resource")
		PrintStream ps = pipesTime > baTime ? System.err : System.out;
        ps.println("Time pipes: " + pipesTime);
        ps = baTime > pipesTime ? System.err : System.out;
        ps.println("Time ba: " + baTime);

        System.err.println("CUSTOM: " + customTime);

        tt.stopped = true;
    }

    private static void testTransformWithBytesArray(Document doc) throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Result outputTarget = new StreamResult(baos);
        transformer.transform(new DOMSource(doc), outputTarget);
        baos.close();
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        InputSource source = new InputSource(is);
        xr.parse(source);
        is.close();
    }

    private static void testTransformWithPipes(Document doc) throws Exception
    {
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos);
        tt.queue.push(new Object[]{doc, pos});
        synchronized (tt.queue) {
            tt.queue.notify();
        }
        InputSource source = new InputSource(pis);
        xr.parse(source);
        pis.close();
    }

    private static void testTransformCustom(Document doc, DOM2InputSource handler) throws Exception
    {
        Element root = doc.getDocumentElement();
        parseElement(handler, root);
    }

    /**
     * @param handler
     * @param element
     * @throws SAXException
     */
    private static void parseElement(DOM2InputSource handler, Element element) throws SAXException
    {
        String elementLocalPart = getLocalName(element);
        String elementNS = element.getNamespaceURI();
        QName qName = new QName(elementNS, elementLocalPart);
        AttributesImpl attrs = new AttributesImpl();
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            String namespaceURI = attr.getNamespaceURI();
            String attrLocalPart = getLocalName(attr);
            QName attrQName = new QName(namespaceURI, attrLocalPart);
            attrs.addAttribute(namespaceURI, attrLocalPart, attrQName.toString(), "CDATA", attr.getNodeValue());
        }

        handler.startElement(elementNS, element.getLocalName(), qName.toString(), attrs);
        NodeList childNodes = element.getChildNodes();
        if (childNodes != null) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node currentNode = childNodes.item(i);
                switch (currentNode.getNodeType()) {
                    case Node.TEXT_NODE :
                        String text = currentNode.getNodeValue();
                        if (text != null) {
                            handler.characters(text.toCharArray(), 0, text.length());
                        }
                        break;
                    case Node.ELEMENT_NODE :
                        parseElement(handler, (Element) currentNode);
                        break;
                    default :

                }
            }
        }
        handler.endElement(elementNS, element.getLocalName(), qName.toString());
    }

    /**
     * @param element
     * @return
     */
    private static String getLocalName(Node node)
    {
        String name = node.getLocalName();
        if (name == null) {
            name = node.getNodeName();
        }
        return name;
    }

    private int charactersCounter   = 0;

    private int startElementCounter = 0;

    private int endElementCounter   = 0;

    /**
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        charactersCounter++;
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        startElementCounter++;
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        endElementCounter++;
    }
}
