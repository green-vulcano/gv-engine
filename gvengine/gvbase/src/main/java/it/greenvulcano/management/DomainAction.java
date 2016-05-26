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
package it.greenvulcano.management;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.io.Serializable;
import java.util.HashMap;

import org.w3c.dom.Node;

/**
 * DomainAction class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 **/
public abstract class DomainAction implements Serializable
{
    private static final long         serialVersionUID = 2432033356479991567L;

    /**
     * Action parameters.
     */
    protected HashMap<String, Object> params           = new HashMap<String, Object>();
    /**
     * Action name.
     */
    private String                    name             = "";

    /**
     * Constructor.
     */
    public DomainAction()
    {
        // do nothing
    }

    /**
     * Constructor.
     *
     * @param name
     *        the action name
     */
    public DomainAction(String name)
    {
        this.name = name;
    }

    /**
     * Initialize the instance.
     *
     * @param node
     *        the configuration node
     * @throws XMLConfigException
     *         if error occurs
     */
    public final void init(Node node) throws XMLConfigException
    {
        name = XMLConfig.get(node, "@name");
        internalInit(node);
    }

    /**
     * @return the action name
     */
    public final String getName()
    {
        return name;
    }

    /**
     * Set the given parameter.
     *
     * @param key
     *        parameter key
     * @param value
     *        parameter value
     */
    public final void setParam(String key, Object value)
    {
        params.put(key, value);
    }

    /**
     * @param key
     *        parameter key
     * @return the parameter value
     */
    public final Object getParam(String key)
    {
        return params.get(key);
    }

    /**
     * Initialize the instance.
     *
     * @param node
     *        the configuration node
     * @throws XMLConfigException
     *         if error occurs
     */
    protected abstract void internalInit(Node node) throws XMLConfigException;
}
