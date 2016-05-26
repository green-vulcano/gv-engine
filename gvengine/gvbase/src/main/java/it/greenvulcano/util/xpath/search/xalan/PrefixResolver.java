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

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * PrefixResolver class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class PrefixResolver implements org.apache.xml.utils.PrefixResolver
{
    private static PrefixResolver _instance;

    private Map<String, String>   namespaces = new HashMap<String, String>();

    private PrefixResolver()
    {
        // do nothing
    }

    /**
     * @return the singleton instance
     */
    public static PrefixResolver instance()
    {
        if (_instance == null) {
            _instance = new PrefixResolver();
        }
        return _instance;
    }

    /**
     * @see org.apache.xml.utils.PrefixResolver#handlesNullPrefixes()
     */
    public boolean handlesNullPrefixes()
    {
        return true;
    }

    /**
     * @see org.apache.xml.utils.PrefixResolver#getBaseIdentifier()
     */
    public String getBaseIdentifier()
    {
        return null;
    }

    /**
     * @see org.apache.xml.utils.PrefixResolver#getNamespaceForPrefix(java.lang.String)
     */
    public String getNamespaceForPrefix(String prefix)
    {
        String namespace = namespaces.get(prefix);
        if (namespace == null) {
            return "";
        }
        return namespace;
    }

    /**
     * @see org.apache.xml.utils.PrefixResolver#getNamespaceForPrefix(java.lang.String,
     *      org.w3c.dom.Node)
     */
    public String getNamespaceForPrefix(String prefix, Node context)
    {
        return getNamespaceForPrefix(prefix);
    }

    /**
     * @param prefix
     * @param namespace
     */
    public void installNamespace(String prefix, String namespace)
    {
        namespaces.put(prefix, namespace);
    }
}
