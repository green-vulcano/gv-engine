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
package it.greenvulcano.gvesb.gvdte.config;

import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xpath.XPathFinder;
import it.greenvulcano.util.xpath.search.XPath;
import it.greenvulcano.util.xpath.search.XPathAPI;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The class reads configuration information from a XML string resource.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XMLLoader implements IConfigLoader
{
    private static final Logger logger   = org.slf4j.LoggerFactory.getLogger(XMLLoader.class);

    private Document            document;

    private static XPathAPI     xpathAPI = new XPathAPI();

    /**
     * XML String.
     */
    private String              source;

    public XMLLoader()
    {
        // do nothing
    }

    /**
     * Initialize the instance.
     *
     * @param n_source
     * @throws ConfigException
     */
    public void init(String resource) throws ConfigException
    {
        if ((resource == null) || (resource.length() == 0)) {
            logger.error("The resource parameter length canno't be null or empty.");
            throw new ConfigException("GVDTE_INITIALIZATION_ERROR", new String[][]{{"class", "XMLLoader"},
                    {"key", "The resource parameter length canno't be null or empty."}});
        }
        source = new String(resource);

        try {
            document = XMLUtils.parseDOM_S(source, false, false);
            if (document == null) {
                throw new ConfigException("GVDTE_INITIALIZATION_ERROR", new String[][]{{"class", "XMLLoader"},
                        {"key", " Error: Parse error occurred."}});
            }
        }
        catch (ConfigException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Parse error occurred", exc);
            throw new ConfigException("GVDTE_INITIALIZATION_ERROR", new String[][]{{"class", "XMLLoader"},
                    {"key", " Parse error occurred."}}, exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.config.IConfigLoader#getData(java.lang.String)
     */
    public synchronized Object getData(String name) throws ConfigException
    {
        NodeList nl = null;

        xpathAPI.reset();

        try {
            nl = (NodeList) xpathAPI.selectNodeList(document, new XPath(name));

            if ((nl == null) || (nl.getLength() == 0)) {
                logger.error("Resource '" + name + "' not found");
                throw new ConfigException("GVDTE_XPATH_NOT_FOUND", new String[][]{{"name", name}});
            }
            return nl.item(0).getNodeValue();
        }
        catch (ConfigException exc) {
            throw exc;
        }
        catch (TransformerException exc) {
            logger.error("Resource '" + name + "' not found", exc);
            throw new ConfigException("GVDTE_XPATH_NOT_FOUND", new String[][]{{"name", name}}, exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new ConfigException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", " Unexpected error."}}, exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.config.IConfigLoader#getSectionList(java.lang.String)
     */
    public synchronized String[] getSectionList(String name) throws ConfigException
    {
        String lName = "";
        boolean lastSelAll = false;
        boolean addAttrName = false;
        boolean reverseXPath = false;
        Node n = null;
        List<String> al = new ArrayList<String>();

        xpathAPI.reset();

        try {
            lastSelAll = (name.charAt(name.length() - 1) == '*');
            addAttrName = ((name.indexOf("@") > 0) && lastSelAll);
            reverseXPath = (name.indexOf("=") > 0) || addAttrName;

            if (lastSelAll) {
                if (addAttrName) {
                    lName = name.substring(0, name.length() - 2); // es. name = 'element/child/@*'
                }
                else {
                    lName = name.substring(0, name.length() - 1); // es. name = 'element/*'
                }
            }
            else {
                lName = name.substring(0, name.lastIndexOf("/") + 1); // es. name = 'element/child'
            }

            NodeList nl = null;
            try {
                if (addAttrName) {
                    nl = (NodeList) xpathAPI.selectNodeList(document, new XPath(lName));
                }
                else {
                    nl = (NodeList) xpathAPI.selectNodeList(document, new XPath(name));
                }
            }
            catch (TransformerException exc) {
                logger.error("Error while looking up for resource '" + name + "'", exc);
                throw new ConfigException("GVDTE_XPATH_UTILS_ERROR", new String[][]{{"cause",
                        "looking up for resource '" + name + "'"}}, exc);
            }
            if ((nl == null) || (nl.getLength() == 0)) {
                logger.error("Resource '" + name + "' not found");
                throw new ConfigException("GVDTE_XPATH_NOT_FOUND", new String[][]{{"name", name}});
            }

            int nn = nl.getLength();
            String[] secList = new String[nn];

            for (int i = 0; i < nn; i++) {
                n = nl.item(i);
                if (reverseXPath) {
                    secList[i] = XPathFinder.buildXPath(n);
                }
                else {
                    secList[i] = lName + n.getNodeName();
                }
            }

            if (reverseXPath && !addAttrName) {
                return secList;
            }

            if (addAttrName) {
                int nS = secList.length;
                NodeList nl1 = null;
                for (int i = 0; i < nS; i++) {
                    try {
                        nl1 = (NodeList) xpathAPI.selectNodeList(document, new XPath(secList[i] + "@*"));
                    }
                    catch (TransformerException exc) {
                        logger.error("Error while looking up for resource '" + secList[i] + "@*'",
                                exc);
                        throw new ConfigException("GVDTE_XPATH_UTILS_ERROR", new String[][]{{"cause",
                                "looking up for resource '" + secList[i] + "@*'"}}, exc);
                    }
                    if ((nl1 == null) || (nl1.getLength() == 0)) {
                        logger.error("Resource '" + secList[i] + "@*' not found");
                        throw new ConfigException("GVDTE_XPATH_NOT_FOUND", new String[][]{{"name", secList[i] + "@*"}});
                    }

                    int nn1 = nl1.getLength();
                    for (int j = 0; j < nn1; j++) {
                        n = nl1.item(j);
                        al.add(secList[i] + "@" + n.getNodeName());
                    }
                }

                int sl = al.size();
                String[] secListFin = new String[sl];
                for (int s = 0; s < sl; s++) {
                    secListFin[s] = al.get(s);
                }
                return secListFin;
            }

            int sl = secList.length;
            String[] secListFin = new String[sl];

            for (int i = 0; i < sl; i++) {
                String s = secList[i];
                if (s.length() > 0) {
                    int y = 1;
                    secListFin[i] = s + "[" + y + "]";
                    secList[i] = "";
                    int ii = i + 1;
                    while (ii < sl) {
                        String ss = secList[ii];
                        if (ss.equals(s)) {
                            y++;
                            secListFin[ii] = s + "[" + y + "]";
                            secList[ii] = "";
                        }
                        ii++;
                    }
                }
            }
            return secListFin;
        }
        catch (ConfigException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new ConfigException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", " Unexpected error."}}, exc);
        }
    }
}
