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

import it.greenvulcano.script.ScriptExecutor;
import it.greenvulcano.script.ScriptExecutorFactory;
import it.greenvulcano.util.txt.TextUtils;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;


/**
 * JavaScriptRetriever class
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JavaScriptRetriever extends AbstractRetriever
{
    private static final Logger logger  = org.slf4j.LoggerFactory.getLogger(JavaScriptRetriever.class);

    private static String       jsScope = "JavaScriptRetriever";

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
            JavaScriptRetriever retr = AbstractRetriever.javaScriptRetrieverInstance();
            Map<String, String> resultsCache = retr.getMethodCache(method);
            boolean cacheable = false;
            if (resultsCache != null){
                cacheable = true;
                if (resultsCache.containsKey(paramList)){
                    String result = resultsCache.get(paramList);
                    logger.debug("Result Function JavaScriptRetriever[" + method + "] from cache: " + result);
                    return result;
                }
            }
    
            List<String> paramL = TextUtils.splitByStringSeparator(paramList, paramSep);
            String jsFunction = retr.getDataRetriever(method, paramL);
            String result = null;
            if (jsFunction != null) {
                ScriptExecutor script = ScriptExecutorFactory.createSE("js",jsFunction, null, jsScope);
                Map<String, Object> params = retr.getMethodParamMap(method, paramL);
                handleArguments(script, jsFunction, params);
                Object obj = script.execute(null);
                result = (obj == null) ? "" : obj.toString();
            }
    
            if (cacheable){
                resultsCache.put(paramList, result);
            }
    
            logger.debug("Result Function JavaScriptRetriever[" + method + "] calculated: " + result);
    
            return result;
        }
        catch (Exception exc) {
            logger.error("Cannot execute JavaScriptRetriever method: {" + method + "} with parameters(" + paramSep + ") {" + paramList + "}.", exc);
            throw exc;
        }
    }

    private static void handleArguments(ScriptExecutor script, String jsFunction, Map<String, Object> params)
            throws Exception
    {
        StringTokenizer st = new StringTokenizer(jsFunction, "\"'=!<>()+*-%\\/,; ");
        while (st.hasMoreTokens()) {
            String varName = st.nextToken();
            if (params.containsKey(varName)) {
                Object varValue = params.get(varName);
                script.putProperty(varName, varValue);
            }
        }
        script.putProperty("props", params);
    }
}
