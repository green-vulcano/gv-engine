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
package it.greenvulcano.gvesb.notification;

import it.greenvulcano.configuration.XMLConfigException;

import org.w3c.dom.Node;

/**
 *
 * Interface of notification plug-ins.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface GVNotification
{
    /**
     * Initialization method to read the configuration parameters of
     * NotificationNode
     *
     * @param NotificationNode
     *        The NotificationNode node
     * @throws XMLConfigException
     * @throws GVNotificationException
     */
    public void init(Node NotificationNode) throws XMLConfigException, GVNotificationException;

    /**
     * Execute method to return the Notification message
     *
     * @param input
     *        The input object in the environment it can be:<br>
     *        Map, GVBuffer, Exception.
     * @throws GVNotificationException
     */
    public void execute(Object input) throws GVNotificationException;

    /**
     * This method set the critical parameter for every Notification object
     *
     * @return true if the Notification is critical
     */
    public boolean isCritical();
}
