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

import org.jaxen.SimpleNamespaceContext;

/**
 * A dummy namespace context. Always return the default namespace.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class NamespaceContext extends SimpleNamespaceContext
{
    private static final long       serialVersionUID = 210L;
    /**
     *
     */
    public static final String      NAMESPACE_URI    = "urn:Tempuri";
    /**
     *
     */
    public static final String      NAMESPACE_PREFIX = "gvf";

    private static NamespaceContext _instance;

    /**
     * @return the singleton instance
     */
    public static synchronized NamespaceContext instance()
    {
        if (_instance == null) {
            _instance = new NamespaceContext();
        }
        return _instance;
    }

    private NamespaceContext()
    {
        // do nothing
    }
}
