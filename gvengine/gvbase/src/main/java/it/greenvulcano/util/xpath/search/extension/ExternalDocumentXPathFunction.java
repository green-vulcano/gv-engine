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
package it.greenvulcano.util.xpath.search.extension;

import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xpath.search.XPathFunction;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Allows to reference external XML documents within the XPath expressions.
 *
 * @version 3.2.0 Aug 11, 2011
 * @author GreenVulcano Developer Team
 *
 */
public class ExternalDocumentXPathFunction implements XPathFunction
{
    private class DocumentHolder
    {
        private long     lastUse  = 0;
        private Document document = null;

        public DocumentHolder(Document document)
        {
            this.document = document;
            tick();
        }

        public Document getDocument()
        {
            tick();
            return document;
        }

        public boolean isValid()
        {
            return (System.currentTimeMillis() - lastUse) < 120000;
        }

        private void tick()
        {
            lastUse = System.currentTimeMillis();
        }
    }

    /**
     * The cached elements not accessed for more than 120 seconds are removed by
     * a TimerTask running every 60 seconds.
     *
     * Map[documentPath, DocumentHolder]
     */
    private static Map<String, DocumentHolder> documentCache = Collections.synchronizedMap(new HashMap<String, DocumentHolder>());

    private static Timer timer = null;

    static {
        timer = new Timer("ExternalDocumentXPathFunction cleaner", true);
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run()
            {
                ExternalDocumentXPathFunction.checkUnusedDocument();
            }
        }, 60000, 60000);
    }


    public ExternalDocumentXPathFunction()
    {
        // do nothing
    }

    public static synchronized void checkUnusedDocument()
    {
        System.out.println("ExternalDocumentXPathFunction cleaning");
        Iterator<String> i = documentCache.keySet().iterator();
        while (i.hasNext()) {
            String doc = i.next();
            DocumentHolder dh = documentCache.get(doc);
            if (!dh.isValid()) {
                System.out.println("ExternalDocumentXPathFunction removing: " + doc);
                documentCache.remove(doc);
            }
        }
    }


    public static synchronized void clearCache()
    {
        documentCache.clear();
    }

    public Object evaluate(Node contextNode, Object[] params) throws TransformerException
    {
        String documentPath = params[0].toString();

        Object document = null;
        try {
            document = getExtDocument(documentPath);
        }
        catch (Exception exc) {
            throw new TransformerException(exc);
        }
        return document;
    }

    private Object getExtDocument(String documentPath) throws Exception
    {
        String docPath = PropertiesHandler.expand(documentPath);

        DocumentHolder dh = documentCache.get(docPath);
        if (dh != null) {
            return dh.getDocument();
        }

        Document doc = XMLUtils.parseDOM_S(new FileInputStream(docPath), false, true);
        documentCache.put(docPath, new DocumentHolder(doc));
        System.out.println("ExternalDocumentXPathFunction caching: " + docPath);
        return doc;
    }
}
