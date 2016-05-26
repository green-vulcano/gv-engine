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

import javax.xml.transform.TransformerException;

/**
 * The actual implementation must implement this interface. To use a particular
 * implementation, you must configure a class implementing this interface.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface XPathAPIFactoryImpl
{
    /**
     * @return the XPathAPIImpl that executes the XPath using a particular
     *         implementation.
     */
    XPathAPIImpl newXPathAPIImpl();

    /**
     * Creates a new implementation XPath. The actual object type depends on the
     * Underlying implementation.
     *
     * @param xpath
     *        a string representation of the XPath
     * @return the low-level object used by the implementation.
     * @throws TransformerException
     *         if an error occurs.
     */
    Object newXPath(String xpath) throws TransformerException;

    /**
     * Install an extension function.
     *
     * @param namespace
     *
     * @param name
     *        name of the function. The function is into the given namespace.
     * @param function
     *        function
     */
    void installFunction(String namespace, String name, XPathFunction function);

    /**
     * Installs a namespace that can be used in XPath expressions.
     *
     * @param prefix
     * @param namespace
     *        the empty string or null specify the default namespace
     */
    void installNamespace(String prefix, String namespace);
}
