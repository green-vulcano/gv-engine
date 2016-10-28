/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.adapter.http;

import it.greenvulcano.gvesb.adapter.http.exc.InboundHttpResponseException;
import it.greenvulcano.gvesb.adapter.http.formatters.FormatterManager;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpInitializationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Node;

/**
 * HttpServletMapping class
 * 
 * @version 3.1.0 Feb 07, 2011
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public interface HttpServletMapping
{

    /**
     * @param transactionManager
     * @param formatterMgr
     * @param configurationNode
     * @param configurationFile
     * @throws AdapterHttpInitializationException
     */
    public void init(HttpServletTransactionManager transactionManager, FormatterManager formatterMgr,
            Node configurationNode) throws AdapterHttpInitializationException;

    /**
     * @param req
     * @param resp
     * @return if request handling was successful
     * @throws InboundHttpResponseException
     */
    public void handleRequest(String methodName, HttpServletRequest req, HttpServletResponse resp) throws InboundHttpResponseException;

    public boolean isDumpInOut();
    
    /**
     *
     */
    public void destroy();

    /**
     * @return the servlet action
     */
    public String getAction();

}
