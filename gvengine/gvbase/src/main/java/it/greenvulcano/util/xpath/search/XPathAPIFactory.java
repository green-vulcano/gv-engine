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
package it.greenvulcano.util.xpath.search;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * This class is visible only internally and is used to initialize the
 * configured XPath library.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
class XPathAPIFactory
{
    public static final String         DEFAULT_XPATH_FACTORY_IMPL      = "it.greenvulcano.util.xpath.search.jaxen.JaxenXPathAPIFactoryImpl";
    public static final String         XPATH_CONF                      = "gv-xpath.xml";
    public static final String         DEFAULT_NAMESPACE_FOR_FUNCTIONS = "urn:gvesb/functions";

    private static String              baseConfigPath                  = "";

    /**
     * Single instance of the XPath implementation.
     */
    private static XPathAPIFactoryImpl _instance;

    static class DefaultEntityResolver implements EntityResolver
    {
        /**
         * @param publicId
         * @param systemId
         * @return an InputSource for an empty string.
         */
        public InputSource resolveEntity(String publicId, String systemId)
        {
            return new InputSource(new StringReader(""));
        }
    }

    /**
     * Obtains the XPath implementation. The first time, it instantiates and
     * initialize the XPath implementation according with the configuration.
     *
     * @return the single instance of the XPath implementation.
     */
    public static XPathAPIFactoryImpl instance()
    {
        Document document = null;
        Element xpathConf = null;

        try {
            if (_instance == null) {
                String factoryClassName = System.getProperty("it.greenvulcano.xpath-factory",
                        DEFAULT_XPATH_FACTORY_IMPL);

                ClassLoader loader = XPathAPIFactoryImpl.class.getClassLoader();
                if (loader == null) {
                    loader = ClassLoader.getSystemClassLoader();
                }

                URL url = null;
                if (!"".equals(baseConfigPath)) {
                    String fileName = baseConfigPath + File.separatorChar + XPATH_CONF;
                    File xpathConfFile = new File(fileName);
                    if (xpathConfFile.exists()) {
                        url = new URL("file", null, fileName);
                    }
                }

                String cfgFileXPath = System.getProperty("it.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath");
                String cfgFile = (cfgFileXPath != null) ? cfgFileXPath.split("\\|")[0] : null;
                String cfgXPath = (cfgFileXPath != null) ? cfgFileXPath.split("\\|")[1] : null;
                if (url == null) {
                    String file = (cfgFile != null) ? cfgFile : XPATH_CONF;
                    url = loader.getResource(file);
                }

                if (url != null) {
                    InputStream is = url.openStream();
                    BufferedInputStream in = new BufferedInputStream(is, 1024);
                    InputSource source = new InputSource(in);
                    source.setSystemId(url.toString());

                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    db.setEntityResolver(new DefaultEntityResolver());
                    document = db.parse(source);

                    if (cfgXPath != null) {
                        String[] tokens = cfgXPath.split("\\/");
                        xpathConf = document.getDocumentElement();
                        for (String token : tokens) {
                            if ((token.length() != 0) && !(token.equals(xpathConf.getNodeName()))) {
                                xpathConf = (Element) xpathConf.getElementsByTagName(token).item(0);
                            }
                        }
                    }
                    else {
                        xpathConf = (Element) document.getDocumentElement().getElementsByTagName("XPath").item(0);
                    }
                    factoryClassName = xpathConf.getAttribute("xpath-factory");
                }
                System.out.println("Using XPath implementation: " + factoryClassName);

                _instance = (XPathAPIFactoryImpl) loader.loadClass(factoryClassName).newInstance();

                if (xpathConf != null) {
                    installConfiguredNamespaces(xpathConf);
                    installNamespace("gvf", DEFAULT_NAMESPACE_FOR_FUNCTIONS);
                    installConfiguredFunctions(loader, xpathConf);
                }
            }

            return _instance;
        }
        catch (Exception exc) {
            System.out.println("ERROR: cannot instantiate the XPathFactoryImpl");
            exc.printStackTrace();
            return null;
        }
    }

    public static void setBaseConfigPath(String baseConfigPath)
    {
        XPathAPIFactory.baseConfigPath = baseConfigPath;
    }

    /**
     * Private constructor: this cass cannot be instantiated.
     */
    private XPathAPIFactory()
    {
        // do nothing
    }

    /**
     * Install the configured XPath extensions.
     *
     * @param loader
     *        loader used to load the extension classes
     * @param confNode
     *        configuration node for the XPath library
     */
    private static void installConfiguredFunctions(ClassLoader loader, Element confNode)
    {
        NodeList xpathFunctions = confNode.getElementsByTagName("XPathExtension");
        for (int i = 0; i < xpathFunctions.getLength(); ++i) {
            Element node = (Element) xpathFunctions.item(i);
            String functionName = node.getAttribute("function-name");
            String className = node.getAttribute("class");
            String namespace = node.hasAttribute("namespace")
                    ? node.getAttribute("namespace")
                    : DEFAULT_NAMESPACE_FOR_FUNCTIONS;
            try {
                Class<?> functionClass = loader.loadClass(className);
                XPathFunction function = (XPathFunction) functionClass.newInstance();
                XPathAPI.installFunction(namespace, functionName, function);
                System.out.println("### XPath function installed....: " + functionName + ", " + className + " ("
                        + namespace + ")");
            }
            catch (Exception e) {
                System.out.println("ERROR: cannot install XPath function: " + functionName + ", " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Install the configured XPath extensions.
     *
     * @param loader
     *        loader used to load the extension classes
     * @param confNode
     *        configuration node for the XPath library
     */
    private static void installConfiguredNamespaces(Element confNode)
    {
        NodeList xpathNamespaces = confNode.getElementsByTagName("XPathNamespace");
        for (int i = 0; i < xpathNamespaces.getLength(); ++i) {
            Element node = (Element) xpathNamespaces.item(i);
            String prefix = node.getAttribute("prefix");
            String namespace = node.getAttribute("namespace");
            if (namespace == null) {
                namespace = "";
            }
            installNamespace(prefix, namespace);
        }
    }

    /**
     * @param prefix
     * @param namespace
     */
    private static void installNamespace(String prefix, String namespace)
    {
        try {
            XPathAPI.installNamespace(prefix, namespace);
            System.out.println("### XPath namespace installed...: " + prefix + " -> " + namespace);
        }
        catch (Exception e) {
            System.out.println("ERROR: cannot install namespace: " + prefix + "->" + namespace);
            e.printStackTrace();
        }
    }
}
