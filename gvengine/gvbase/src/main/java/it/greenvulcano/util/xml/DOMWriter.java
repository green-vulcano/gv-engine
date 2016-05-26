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
package it.greenvulcano.util.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Write a DOM in a output stream using UTF-8 encoding.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DOMWriter
{
    private boolean writeDoctype   = true;
    private int     preferredWidth = 90;

    /**
     *
     */
    public DOMWriter()
    {
        writeDoctype = true;
    }

    /**
     * @return the field <code>writeDoctype</code>
     */
    public boolean getWriteDoctype()
    {
        return writeDoctype;
    }

    /**
     * @param wdt
     */
    public void setWriteDoctype(boolean wdt)
    {
        writeDoctype = wdt;
    }

    /**
     * @return the field <code>preferredWidth</code>
     */
    public int getPreferredWidth()
    {
        return preferredWidth;
    }

    /**
     * @param v
     */
    public void setPreferredWidth(int v)
    {
        preferredWidth = v;
    }

    /**
     * @param nodeList
     * @param out
     * @throws IOException
     */
    public void write(NodeList nodeList, OutputStream out) throws IOException
    {
        write(nodeList, createPrintWriter(out));
    }

    /**
     * @param node
     * @param out
     * @throws IOException
     */
    public void write(Node node, OutputStream out) throws IOException
    {
        write(node, createPrintWriter(out), "");
    }

    /**
     * @param stream
     * @return the <code>PrintWriter</code> object
     * @throws IOException
     */
    protected PrintWriter createPrintWriter(OutputStream stream) throws IOException
    {
        OutputStreamWriter osw = new OutputStreamWriter(stream, "UTF-8");
        return new PrintWriter(osw);
    }

    /**
     * @param nodeList
     * @param out
     * @throws IOException
     */
    protected void write(NodeList nodeList, PrintWriter out) throws IOException
    {
        for (int i = 0; i < nodeList.getLength(); ++i) {
            write(nodeList.item(i), out, "");
        }
    }

    /**
     * @param node
     * @param out
     * @throws IOException
     */
    protected void write(Node node, PrintWriter out) throws IOException
    {
        write(node, out, "");
    }

    /**
     * @param node
     * @param out
     * @param indent
     * @throws IOException
     */
    protected void write(Node node, PrintWriter out, String indent) throws IOException
    {
        if (node == null) {
            return;
        }

        boolean hasChildren = false;
        boolean isEmpty = false;
        node.normalize();
        int type = node.getNodeType();
        switch (type) {
            case Node.DOCUMENT_NODE :{
                out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                out.println("");
                if (writeDoctype) {
                    Document doc = (Document) node;
                    DocumentType docType = doc.getDoctype();
                    if (docType != null) {
                        out.print("<!DOCTYPE ");
                        out.print(docType.getName());
                        String publicId = docType.getPublicId();
                        if (publicId != null) {
                            out.print(" PUBLIC \"");
                            out.print(publicId);
                            out.print("\" \"");
                            out.print(docType.getSystemId());
                            out.println("\">");
                        }
                        else {
                            out.print(" SYSTEM \"");
                            out.print(docType.getSystemId());
                            out.println("\">");
                        }
                    }
                }
                NodeList children = node.getChildNodes();
                for (int iChild = 0; iChild < children.getLength(); iChild++) {
                    write(children.item(iChild), out);
                }
                out.flush();
                break;
            }
            case Node.ELEMENT_NODE :{
                out.println("");
                out.print(indent);
                out.print('<');
                out.print(node.getNodeName());
                Attr[] attrs = sortAttributes(node.getAttributes());
                int sp = indent.length() + 1 + node.getNodeName().length();
                int cp = sp;
                String indentAtt = indent + " " + spaces(node.getNodeName().length());
                for (Attr attr : attrs) {
                    StringBuilder bf = new StringBuilder();
                    bf.append(" ").append(attr.getNodeName()).append("=\"");
                    bf.append(normalize(attr.getNodeValue(), true));
                    bf.append("\"");
                    String attrStr = bf.toString();
                    int len = attrStr.length();
                    if (cp > sp) {
                        if (cp + len > preferredWidth) {
                            out.println("");
                            out.print(indentAtt);
                            cp = sp;
                        }
                    }
                    out.print(attrStr);
                    cp += len;
                }
                isEmpty = empty(node);
                if (isEmpty) {
                    out.print("/>");
                }
                else {
                    out.print(">");
                }
                NodeList children = node.getChildNodes();
                if (children != null) {
                    int len = children.getLength();
                    for (int i = 0; i < len; i++) {
                        hasChildren |= (children.item(i) instanceof Element);
                        hasChildren |= (children.item(i) instanceof Comment);
                        write(children.item(i), out, indent + "    ");
                    }
                }
                out.flush();
                break;
            }
            case Node.DOCUMENT_FRAGMENT_NODE :{
                NodeList children = node.getChildNodes();
                if (children != null) {
                    int len = children.getLength();
                    for (int i = 0; i < len; i++) {
                        write(children.item(i), out, indent);
                    }
                }
                break;
            }
            case Node.COMMENT_NODE :{
                out.println("");
                out.print(indent);
                out.print("<!--");
                out.print(node.getNodeValue());
                out.print("-->");
                break;
            }
            case Node.ENTITY_REFERENCE_NODE :{
                out.print('&');
                out.print(node.getNodeName());
                out.print(';');
                break;
            }
            case Node.CDATA_SECTION_NODE :{
                out.print(normalize(node.getNodeValue(), false));
                break;
            }
            case Node.TEXT_NODE :{
                out.print(normalize(node.getNodeValue().trim(), false));
                break;
            }
            case Node.PROCESSING_INSTRUCTION_NODE :{
                out.println("");
                out.print("<?");
                out.print(node.getNodeName());
                String data = node.getNodeValue();
                if ((data != null) && (data.length() > 0)) {
                    out.print(' ');
                    out.print(data);
                }
                out.print("?>");
                break;
            }
        }

        if (type == Node.ELEMENT_NODE) {
            if (hasChildren) {
                out.println("");
                out.print(indent);
            }
            if (!isEmpty) {
                out.print("</");
                out.print(node.getNodeName());
                out.print('>');
            }
        }

        out.flush();
    }

    /**
     * @param node
     * @return if the node is empty
     */
    protected boolean empty(Node node)
    {
        NodeList list = node.getChildNodes();
        int l = list.getLength();
        return l == 0;
    }

    /**
     * Returns a sorted list of attributes.
     *
     * @param attrs
     * @return a sorted list of attributes.
     */
    protected Attr[] sortAttributes(NamedNodeMap attrs)
    {
        int len = (attrs != null) ? attrs.getLength() : 0;
        Attr[] array = new Attr[len];
        for (int i = 0; i < len; i++) {
            array[i] = (Attr) attrs.item(i);
        }
        for (int i = 0; i < len - 1; i++) {
            String name = array[i].getNodeName();
            int index = i;
            for (int j = i + 1; j < len; j++) {
                String curName = array[j].getNodeName();
                if (curName.compareTo(name) < 0) {
                    name = curName;
                    index = j;
                }
            }
            if (index != i) {
                Attr temp = array[i];
                array[i] = array[index];
                array[index] = temp;
            }
        }

        return (array);
    }

    /**
     * Normalizes the given string.
     *
     * @param s
     * @param isAttribute
     * @return the string normalized
     */
    protected String normalize(String s, boolean isAttribute)
    {
        StringBuilder str = new StringBuilder();

        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '<' :{
                    str.append("&lt;");
                    break;
                }
                case '>' :{
                    str.append("&gt;");
                    break;
                }
                case '&' :{
                    str.append("&amp;");
                    break;
                }
                case '"' :{
                    str.append("&quot;");
                    break;
                }
                case '\'' :{
                    str.append("&apos;");
                    break;
                }
                case '\r' :
                case '\n' :
                    if (isAttribute) {
                        str.append("&#");
                        str.append(Integer.toString(ch));
                        str.append(';');
                        break;
                    }
                default :
                    str.append(ch);
            }
        }

        return (str.toString());
    }

    private static final String spcs = "                                                  ";

    /**
     * @param len
     * @return spaces
     */
    public String spaces(int len)
    {
        StringBuilder ret = new StringBuilder();
        while (len > 0) {
            if (len > 50) {
                len -= 50;
                ret.append(spcs);
            }
            else {
                ret.append(spcs.substring(0, len));
                len = 0;
            }
        }
        return ret.toString();
    }
}
