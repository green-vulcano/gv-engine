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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @version 3.0.0 Nov 23, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class OMXMLUtils
{

    /**
     * @param reader
     * @param writer
     * @param nsMap
     * @throws XMLUtilsException
     *
     */
    public static void copyXMLFromPullEvents(XMLStreamReader reader, XMLStreamWriter writer, Map<String, String> nsMap)
            throws XMLUtilsException
    {
        try {
            boolean enough = false;
            String localName = reader.getLocalName();
            String nsURI = reader.getNamespaceURI();
            String prefix = reader.getPrefix();
            int occurences = 1;
            writeStartElement(reader, writer);
            Set<Entry<String, String>> entrySet = nsMap.entrySet();
            for (Entry<String, String> entry : entrySet) {
                writer.writeNamespace(entry.getKey(), entry.getValue());
            }
            while (reader.hasNext() && !enough) {
                int next = reader.next();
                switch (next) {
                    case XMLStreamReader.START_ELEMENT :{
                        String currLocalName = reader.getLocalName();
                        String currNSURI = reader.getNamespaceURI();
                        String currPrefix = reader.getPrefix();
                        writeStartElement(reader, writer);
                        if ((prefix != null && prefix.equals(currPrefix) || (prefix == null && currPrefix == null))
                                && (nsURI != null && nsURI.equals(currNSURI) || (nsURI == null && currNSURI == null))
                                && (localName != null && localName.equals(currLocalName) || (localName == null && currLocalName == null))) {
                            occurences++;
                        }
                    }
                        break;
                    case XMLStreamReader.CHARACTERS :{
                        if (!reader.isWhiteSpace()) {
                            writeCharacters(reader, writer);
                        }
                    }
                        break;
                    case XMLStreamReader.END_ELEMENT :{
                        String currLocalName = reader.getLocalName();
                        String currNSURI = reader.getNamespaceURI();
                        String currPrefix = reader.getPrefix();
                        writeEndElement(writer);
                        if ((prefix != null && prefix.equals(currPrefix) || (prefix == null && currPrefix == null))
                                && (nsURI != null && nsURI.equals(currNSURI) || (nsURI == null && currNSURI == null))
                                && (localName != null && localName.equals(currLocalName) || (localName == null && currLocalName == null))) {
                            occurences--;
                            enough = occurences == 0;
                        }
                    }
                        break;
                }
            }
        }
        catch (Exception exc) {
            throw new XMLUtilsException(exc.getMessage(), exc);
        }
    }

    /**
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    private static void writeCharacters(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException
    {
        writer.writeCharacters(reader.getText());
    }

    /**
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    private static void writeEndElement(XMLStreamWriter writer) throws XMLStreamException
    {
        writer.writeEndElement();
    }

    private static void writeStartElement(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException
    {
        String localName = reader.getLocalName();
        String nsURI = reader.getNamespaceURI();
        String prefix = reader.getPrefix();
        writer.writeStartElement(prefix, localName, nsURI);
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            if (reader.getAttributeNamespace(i) == null) {
                writer.writeAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
            }
            else {
                writer.writeAttribute(reader.getAttributeNamespace(i), reader.getAttributeLocalName(i),
                        reader.getAttributeValue(i));
            }
        }

    }
}
