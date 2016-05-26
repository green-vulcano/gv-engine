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
package it.greenvulcano.util.xpath.search.jaxen;

import it.greenvulcano.util.xpath.search.XPathAPIFactoryImpl;
import it.greenvulcano.util.xpath.search.XPathAPIImpl;
import it.greenvulcano.util.xpath.search.XPathFunction;

import javax.xml.transform.TransformerException;

import org.jaxen.JaxenException;
import org.jaxen.XPathFunctionContext;

/**
 * Creates classes that encapsulate Jaxen XPath implementation.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JaxenXPathAPIFactoryImpl implements XPathAPIFactoryImpl
{
    /**
     * FunctionContext for the build-in and extended XPath functions.
     */
    private XPathFunctionContext functionContext;

    /**
     * Creates a
     * it.greenvulcano.util.xpath.search.jaxen.JaxenXPathAPIFactoryImpl.
     * Moreover registers the current() XPath function (not provided by Jaxen
     * itself).
     */
    public JaxenXPathAPIFactoryImpl()
    {
        functionContext = (XPathFunctionContext) XPathFunctionContext.getInstance();

        functionContext.registerFunction(null, "current", new CurrentFunction());
    }

    /**
     * Build a new XPathAPIImpl.
     *
     * @return an instance of
     *         it.greenvulcano.util.xpath.search.jaxen.JaxenXPathAPIImpl
     */
    public XPathAPIImpl newXPathAPIImpl()
    {
        return new JaxenXPathAPIImpl();
    }

    /**
     * @param xpath
     *        string representation of the XPath
     * @return an new instance of XPath evaluator
     * @throws TransformerException
     */
    public Object newXPath(String xpath) throws TransformerException
    {
        try {
            JaxenXPath jaxenXPath = new JaxenXPath(xpath);
            jaxenXPath.setFunctionContext(XPathFunctionContext.getInstance());
            jaxenXPath.setNamespaceContext(NamespaceContext.instance());
            return jaxenXPath;
        }
        catch (JaxenException exc) {
            exc.printStackTrace();
            System.out.println("XPATH that caused the exception: " + xpath);
            throw new TransformerException(exc);
        }
    }

    /**
     * Install an extension function.
     *
     * @param namespace
     *        namespace for the function
     * @param name
     *        Name of the function
     * @param function
     *        the implementation of the function.
     */
    public void installFunction(String namespace, String name, XPathFunction function)
    {
        functionContext.registerFunction(namespace, name, new JaxenXPathFunction(function));
    }

    /**
     * @see it.greenvulcano.util.xpath.search.XPathAPIFactoryImpl#installNamespace(java.lang.String,
     *      java.lang.String)
     */
    public void installNamespace(String prefix, String namespace)
    {
        NamespaceContext.instance().addNamespace(prefix, namespace);
    }
}
