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
package it.greenvulcano.util.xpath.search.xalan;

import it.greenvulcano.util.xpath.search.XPathAPIFactoryImpl;
import it.greenvulcano.util.xpath.search.XPathAPIImpl;
import it.greenvulcano.util.xpath.search.XPathFunction;

import javax.xml.transform.TransformerException;

import org.apache.xpath.XPath;

/**
 * Creates classes that encapsulate Xalan XPath implementation.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XalanXPathAPIFactoryImpl implements XPathAPIFactoryImpl
{

    /**
     * Build a new it.greenvulcano.util.xpath.search.XPathAPIImpl.
     *
     * @return an instance of
     *         it.greenvulcano.util.xpath.search.xalan.XalanXPathAPIImpl
     */
    public XPathAPIImpl newXPathAPIImpl()
    {
        return new XalanXPathAPIImpl();
    }

    /**
     * @param xpath
     *        string representation of the XPath
     * @return an new instance of org.apache.xpath.XPath
     * @throws TransformerException
     */
    public Object newXPath(String xpath) throws TransformerException
    {
        return new XPath(xpath, null, PrefixResolver.instance(), XPath.SELECT, null);
    }

    /**
     * Install an extension function.
     *
     * @param namespace
     *
     * @param name
     *        . Name of the function.
     * @param function
     *        the implementation of the function.
     */
    public void installFunction(String namespace, String name, XPathFunction function)
    {
        ExtensionsManager.instance().installFunction(namespace, name, function);
    }

    /**
     * @see it.greenvulcano.util.xpath.search.XPathAPIFactoryImpl#installNamespace(java.lang.String,
     *      java.lang.String)
     */
    public void installNamespace(String prefix, String namespace)
    {
        PrefixResolver.instance().installNamespace(prefix, namespace);
    }
}
