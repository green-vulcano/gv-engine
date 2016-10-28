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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.adapter.http.exc.HttpServletTransactionException;
import it.greenvulcano.gvesb.adapter.http.formatters.handlers.AdapterHttpConfigurationException;
import it.greenvulcano.gvesb.adapter.http.formatters.handlers.GVTransactionInfo;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpConstants;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.j2ee.XAHelper;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * HttpServletTransactionManager class
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class HttpServletTransactionManager
{
    private static Logger                       logger       = org.slf4j.LoggerFactory.getLogger(HttpServletTransactionManager.class);

    private Map<String, HttpServletTransaction> transactions = new HashMap<String, HttpServletTransaction>();

    private XAHelper                            xaHelper     = null;

    public HttpServletTransactionManager() throws AdapterHttpConfigurationException
    {
        init();
    }

    /**
     * @param data
     * @throws HttpServletTransactionException
     */
    public void begin(GVBuffer data) throws HttpServletTransactionException
    {
        String key = getKey(data);
        begin(key);
    }


    /**
     * @param data
     * @param beforeResponse
     * @throws HttpServletTransactionException
     */
    public void commit(GVBuffer data, boolean beforeResponse) throws HttpServletTransactionException
    {
        String key = getKey(data);
        commit(key, beforeResponse);
    }

    /**
     * @param data
     * @param beforeResponse
     * @throws HttpServletTransactionException
     */
    public void rollback(GVBuffer data, boolean beforeResponse) throws HttpServletTransactionException
    {
        String key = getKey(data);
        rollback(key, beforeResponse);
    }

    /**
     * @param transInfo
     * @throws HttpServletTransactionException
     */
    public void begin(GVTransactionInfo transInfo) throws HttpServletTransactionException
    {
        String key = getKey(transInfo);
        begin(key);
    }

    /**
     * @param transInfo
     * @param beforeResponse
     * @throws HttpServletTransactionException
     */
    public void commit(GVTransactionInfo transInfo, boolean beforeResponse) throws HttpServletTransactionException
    {
        String key = getKey(transInfo);
        commit(key, beforeResponse);
    }

    /**
     * @param transInfo
     * @param beforeResponse
     * @throws HttpServletTransactionException
     */
    public void rollback(GVTransactionInfo transInfo, boolean beforeResponse) throws HttpServletTransactionException
    {
        String key = getKey(transInfo);
        rollback(key, beforeResponse);
    }


    /**
     * @param key
     * @throws HttpServletTransactionException
     */
    public void begin(String key) throws HttpServletTransactionException
    {
        HttpServletTransaction servletTransaction = null;
        if (key.equals("::")) {
            return;
        }
        try {
            servletTransaction = getServletTransaction(key);
            if ((servletTransaction == null) && (key.indexOf("::") != -1)) {
                servletTransaction = transactions.get(key.substring(0, key.indexOf("::")));                
            }
            if (servletTransaction == null) {
                return;
                //throw new HttpServletTransactionException("GVHTTP_TRANSACTION_MISSING_ERROR", new String[][]{{"key", key}});
            }
            if (servletTransaction.isTransacted()) {
                logger.debug("begin - Executing Transaction begin for key '" + key + "'");
                xaHelper.begin();
                xaHelper.setTransactionTimeout(servletTransaction.getTimeout());
            }
        }
        /*catch (HttpServletTransactionException exc) {
            throw exc;
        }*/
        catch (Exception exc) {
            throw new HttpServletTransactionException("GVHTTP_TRANSACTION_BEGIN_ERROR", new String[][]{{"key", key},
                    {"msg", "" + exc}}, exc);
        }
    }

    /**
     * @param key
     * @param beforeReply
     * @throws HttpServletTransactionException
     */
    public void commit(String key, boolean beforeReply) throws HttpServletTransactionException
    {
        HttpServletTransaction servletTransaction = null;
        if (key.equals("::")) {
            return;
        }
        try {
            servletTransaction = getServletTransaction(key);
            if (servletTransaction == null) {
                return;
                //throw new HttpServletTransactionException("GVHTTP_TRANSACTION_MISSING_ERROR", new String[][]{{"key", key}});
            }
            if (servletTransaction.isTransacted() && (servletTransaction.isCloseBeforeReply() == beforeReply)) {
                logger.debug("commit - Executing Transaction commit for key '" + key + "'");
                xaHelper.commit();
            }
        }
        /*catch (HttpServletTransactionException exc) {
            throw exc;
        }*/
        catch (Exception exc) {
            throw new HttpServletTransactionException("GVHTTP_TRANSACTION_COMMIT_ERROR", new String[][]{{"key", key},
                    {"msg", "" + exc}}, exc);
        }
    }

    /**
     * @param key
     * @param beforeResponse
     * @throws HttpServletTransactionException
     */
    public void rollback(String key, boolean beforeResponse) throws HttpServletTransactionException
    {
        HttpServletTransaction servletTransaction = null;
        if (key.equals("::")) {
            return;
        }
        try {
            servletTransaction = getServletTransaction(key);
            if (servletTransaction == null) {
                return;
                //throw new HttpServletTransactionException("GVHTTP_TRANSACTION_MISSING_ERROR", new String[][]{{"key", key}});
            }
            if (servletTransaction.isTransacted()) {
                logger.debug("rollback - Executing Transaction rollback for key '" + key + "'");
                xaHelper.rollback();
            }
        }
        /*catch (HttpServletTransactionException exc) {
            throw exc;
        }*/
        catch (Exception exc) {
            throw new HttpServletTransactionException("GVHTTP_TRANSACTION_ROLLBACK_ERROR", new String[][]{{"key", key},
                    {"msg", "" + exc}}, exc);
        }
    }

    /**
     * @param key
     * @return
     */
    private HttpServletTransaction getServletTransaction(String key) {
        HttpServletTransaction servletTransaction = transactions.get(key);
        if (servletTransaction == null) {
            key = key.substring(0, key.indexOf("::")) + "::ALL";
            servletTransaction = transactions.get(key);
        }
        return servletTransaction;
    }

    /**
     * Reads XML configuration to retrieve services transaction configuration.
     * 
     * @throws AdapterHttpConfigurationException
     *         if any error occurs during initialization.
     */
    public void init() throws AdapterHttpConfigurationException
    {
        try {
            NodeList transactionNodes = XMLConfig.getNodeList(AdapterHttpConstants.CFG_FILE,
                    "/GVAdapterHttpConfiguration/InboundConfiguration/InboundTransactions/Transaction");
            if ((transactionNodes != null) && (transactionNodes.getLength() > 0)) {
                for (int i = 0; i < transactionNodes.getLength(); i++) {
                    Node transactionNode = transactionNodes.item(i);
                    HttpServletTransaction transaction = new HttpServletTransaction();
                    transaction.init(transactionNode);
                    transactions.put(transaction.getKey(), transaction);
                    logger.debug("init - adding Transaction control for '" + transaction.getKey() + "'");
                }
            }

            xaHelper = new XAHelper();
        }
        catch (Exception exc) {
            transactions.clear();
            throw new AdapterHttpConfigurationException("GVHTTP_TRANSACTION_MANAGER_INITIALIZATION_ERROR",
                    new String[][]{{"errorName", "error initializing Transaction list"}}, exc);
        }
    }

    /**
     *
     */
    public void destroy()
    {
        transactions.clear();
    }
    
    /**
     * @param data
     * @return
     */
    private String getKey(GVBuffer data) {
        return data.getService() + "::" + data.getSystem();
    }

    /**
     * @param transInfo
     * @return
     */
    private String getKey(GVTransactionInfo transInfo) {
        return transInfo.getService() + "::" + transInfo.getSystem();
    }

}