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
package it.greenvulcano.management.component;

import it.greenvulcano.configuration.XMLConfig;

import java.io.Serializable;

import org.w3c.dom.Node;

/**
 * Basic domain component data.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 **/
public abstract class ComponentData implements Serializable
{
    private static final long serialVersionUID    = 2631667844464743365L;

    /**
     * Component inactive status.
     */
    public static final int   INACTIVE            = 0;
    /**
     * Component active status.
     */
    public static final int   ACTIVE              = 1;
    /**
     * Component stopped status.
     */
    public static final int   STOPPED             = 2;
    /**
     * Domain application name.
     */
    private String            application         = null;
    /**
     * Component name.
     */
    private String            name                = null;
    /**
     * Timestamp of last start/stop action.
     */
    protected long            lastActionTimestamp = 0;
    /**
     * If true the component stopped can be started automatically.
     */
    protected boolean         autoEnable          = false;

    /**
     * Initialize the instance.
     *
     * @param node
     *        the configuration node
     * @throws Exception
     *         if error occurs
     */
    public final void init(Node node) throws Exception
    {
        name = XMLConfig.get(node, "@name");
        application = XMLConfig.get(node, "@application");
        autoEnable = XMLConfig.getBoolean(node, "@auto-enable", false);
        internalInit(node);
    }

    /**
     * @return the component name
     */
    public final String getName()
    {
        return name;
    }

    /**
     * @return the application name
     */
    public final String getApplication()
    {
        return application;
    }

    /**
     * @return the last action timestamp
     */
    public final long getLastActionTimestamp()
    {
        return lastActionTimestamp;
    }

    /**
     * @return the autoenable status
     */
    public final boolean isAutoEnable()
    {
        return autoEnable;
    }

    /**
     * Set the autoenable status.
     *
     * @param autoEnable
     *        the status to set
     */
    public final void setAutoEnable(boolean autoEnable)
    {
        this.autoEnable = autoEnable;
    }

    /**
     * @return the component status
     */
    public abstract int getStatus();

    /**
     * Initialize the instance.
     *
     * @param node
     *        the configuration node
     * @throws Exception
     *         if error occurs
     */
    protected abstract void internalInit(Node node) throws Exception;
}
