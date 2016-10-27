/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.adapter.http;

import it.greenvulcano.configuration.XMLConfig;

import org.w3c.dom.Node;

/**
 *
 * HttpServletTransaction class
 *
 * @version 3.1.0 Feb 07, 2011
 * @author GreenVulcano Developer Team
 *
 *
 */
public class HttpServletTransaction
{
    private String  system           = "";
    private String  service          = "";
    private String  operation        = "";
    private boolean transacted       = false;
    private int     timeout          = 30;
    private boolean closeBeforeReply = true;

    /**
     * @param node
     */
    public void init(Node node)
    {
        service = XMLConfig.get(node, "@service", "");
        system = XMLConfig.get(node, "@system", "ALL");
        operation = XMLConfig.get(node, "@operation", "");
        transacted = XMLConfig.getBoolean(node, "@transacted", false);
        timeout = XMLConfig.getInteger(node, "@timeout", 30);
        closeBeforeReply = XMLConfig.get(node, "@close-on-response", "before").equals("before");
    }

    /**
     * @return the timeout
     */
    public int getTimeout()
    {
        return timeout;
    }

    /**
     * @return if transacted
     */
    public boolean isTransacted()
    {
        return transacted;
    }

    /**
     * @return if should close before sending the response
     */
    public boolean isCloseBeforeReply()
    {
        return closeBeforeReply;
    }

    /**
     * @return the key
     */
    public String getKey()
    {
        if ("".equals(system) && "".equals(operation)) {
            return service;
        }
        else if ("".equals(operation)) {
            return (service + "::" + system);
        }
        else if ("".equals(system)) {
            return (service + "::" + operation);
        }
        return (service + "::" + system + "::" + operation);
    }
}
