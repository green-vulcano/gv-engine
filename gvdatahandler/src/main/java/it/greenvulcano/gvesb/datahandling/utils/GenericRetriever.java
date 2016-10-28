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
package it.greenvulcano.gvesb.datahandling.utils;

import it.greenvulcano.util.txt.TextUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;

/**
 * GenericRetriever class
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GenericRetriever extends AbstractRetriever
{
    private static final Logger            logger             = org.slf4j.LoggerFactory.getLogger(GenericRetriever.class);

    private Map<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();

    /**
     * @see it.greenvulcano.gvesb.datahandling.utils.AbstractRetriever#getDataRetriever()
     */
    @Override
    protected String getDataRetriever()
    {
        return null;
    }

    /**
     * @param method
     * @param paramList
     * @return the retrieved data
     * @throws Exception
     */
    public static String getData(String method, String paramList) throws Exception
    {
        return getData(method, paramList, ",");
    }
    
    /**
     * @param method
     * @param paramList
     * @param paramSep
     * @return the retrieved data
     * @throws Exception
     */
    public static String getData(String method, String paramList, String paramSep) throws Exception
    {
        try {
            GenericRetriever retr = AbstractRetriever.genericRetrieverInstance();
            Map<String, String> resultsCache = retr.getMethodCache(method);
            boolean cacheable = false;
            if (resultsCache != null) {
                cacheable = true;
                if (resultsCache.containsKey(paramList)) {
                    String result = resultsCache.get(paramList);
                    logger.debug("Result Function GenericRetriever[" + method + "] from cache: " + result);
                    return result;
                }
            }

            PreparedStatement stmt = retr.getInternalStmt(method, TextUtils.splitByStringSeparator(paramList, paramSep));
            String result = null;
            if (stmt != null) {
                ResultSet rs = null;
                try {
                    rs = stmt.executeQuery();
                    if ((rs != null) && rs.next()) {
                        result = rs.getString(1);
                    }
                }
                finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        }
                        catch (Exception exc) {
                            // do nothing
                        }
                    }
                }
            }

            if (cacheable) {
                resultsCache.put(paramList, result);
            }

            logger.debug("Result Function GenericRetriever[" + method + "] calculated: " + result);

            return result;
        }
        catch (Exception exc) {
            logger.error("Cannot execute GenericRetriever method: {" + method + "} with parameters(" + paramSep + ") {" + paramList + "}.", exc);
            throw exc;
        }
    }

    /**
     * @param method
     * @param paramList
     * @param separator
     * @return the retrieved data
     * @throws Exception
     */
    public static String getPreparedData(String method, String paramList) throws Exception
    {
        return getPreparedData(method, paramList, ",");
    }

    /**
     * @param method
     * @param paramList
     * @param separator
     * @return the retrieved data
     * @throws Exception
     */
    public static String getPreparedData(String method, String paramList, String separator) throws Exception
    {
        try {
            GenericRetriever retr = AbstractRetriever.genericRetrieverInstance();
            Map<String, String> resultsCache = retr.getMethodCache(method);
            boolean cacheable = false;
            if (resultsCache != null) {
                cacheable = true;
                if (resultsCache.containsKey(paramList)) {
                    String result = resultsCache.get(paramList);
                    logger.debug("Result Function GenericRetriever[" + method + "] from cache: " + result);
                    return result;
                }
            }

            StringTokenizer st = new StringTokenizer(paramList, separator, true);
            List<String> parameterList = new ArrayList<String>(st.countTokens());
            boolean lastSeparatorFound = true;
            while (st.hasMoreTokens()) {
                String value = st.nextToken();
                if (separator.equals(value)) {
                    if (lastSeparatorFound) {
                        parameterList.add("");
                    }
                    lastSeparatorFound = true;
                }
                else {
                    lastSeparatorFound = false;
                    parameterList.add(value);
                }
            }
            if (lastSeparatorFound) {
                parameterList.add("");
            }
            logger.debug("Executing GenericRetriever method " + method);
            PreparedStatement stmt = retr.getPreparedStatement(method);
            for (int i = 0; i < parameterList.size(); i++) {
                String param = parameterList.get(i);
                int pos = i + 1;
                if (param == null || param.trim().length() == 0) {
                    logger.debug("Setting null to position " + pos);
                    stmt.setNull(pos, Types.VARCHAR);
                }
                else {
                    logger.debug("Setting '" + param + "' to position " + pos);
                    stmt.setString(pos, param);
                }
            }
            String result = null;
            if (stmt != null) {
                ResultSet rs = null;
                try {
                    rs = stmt.executeQuery();
                    if ((rs != null) && rs.next()) {
                        result = rs.getString(1);
                    }
                }
                finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        }
                        catch (Exception exc) {
                            // do nothing
                        }
                    }
                }
            }

            if (cacheable) {
                resultsCache.put(paramList, result);
            }

            logger.debug("Result Function GenericRetriever[" + method + "] calculated: " + result);

            return result;
        }
        catch (Exception exc) {
            logger.error("Cannot execute GenericRetriever method: {" + method + "} with parameters {" + paramList + "}.", exc);
            throw exc;
        }
    }

    /**
     * @param method
     * @return
     * @throws Exception
     */
    private synchronized PreparedStatement getPreparedStatement(String method) throws Exception
    {
        PreparedStatement stmt = preparedStatements.get(method);
        if (stmt == null) {
            stmt = getInternalStmt(method);
            preparedStatements.put(method, stmt);
        }
        return stmt;
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.utils.AbstractRetriever#cleanup()
     */
    @Override
    protected synchronized void cleanup()
    {
        for (PreparedStatement statement : preparedStatements.values()) {
            try {
                if (statement != null) {
                    statement.close();
                }
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        preparedStatements.clear();
        super.cleanup();
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.utils.AbstractRetriever#getInternalStmt(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected PreparedStatement getInternalStmt(String method) throws Exception
    {
        Connection internalConn = getConnection();
        if (internalConn == null) {
            throw new SQLException("Invalid connection!");
        }
        return internalConn.prepareStatement(getDataRetriever(method, Collections.EMPTY_LIST));
    }

}
