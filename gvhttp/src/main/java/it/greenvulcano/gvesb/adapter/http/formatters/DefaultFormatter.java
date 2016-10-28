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
package it.greenvulcano.gvesb.adapter.http.formatters;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.adapter.http.formatters.handlers.InterfaceParametersHandler;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpConstants;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * 
 * DefaultFormatter class
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class DefaultFormatter implements Formatter
{
    private static Logger                                 logWriter                   = org.slf4j.LoggerFactory.getLogger(DefaultFormatter.class);

    private List<String>                                  paramList                   = null;

    private Map<String, List<InterfaceParametersHandler>> paramHandlers               = null;

    private String                                        characterEncoding           = null;

    private boolean                                       urlEncoding                 = false;

    private String                                        Id                          = null;

    /**
     * Mapping between HTTP parameter names and strings containing how the
     * parameter name should appear into request query string (e.g. if the name
     * of the parameter 'param' MUST appear within request query string, the
     * corresponding value will be 'param=', while if the name of the parameter
     * 'param' MUST NOT appear within request query string, the corresponding
     * value will be an empty string).
     */
    private Map<String, String>                           paramNamesWithinQueryString = null;

    /**
     * @see it.greenvulcano.gvesb.adapter.http.formatters.Formatter#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node configurationNode) throws FormatterInitializationException
    {
        paramHandlers = new HashMap<String, List<InterfaceParametersHandler>>();
        paramNamesWithinQueryString = new HashMap<String, String>();
        paramList = new ArrayList<String>();
        try {
            characterEncoding = XMLConfig.get(configurationNode, "@CharacterEncoding");
            urlEncoding = XMLConfig.getBoolean(configurationNode, "@URLEncoding", false);
            Id = XMLConfig.get(configurationNode, "@ID");
        }
        catch (XMLConfigException exc) {
            throw new FormatterInitializationException("GVHTTP_FORMATTER_INITIALIZATION_ERROR", exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.adapter.http.formatters.Formatter#marshall(java.util.Map)
     */
    @Override
    public void marshall(Map<String, Object> environment) throws FormatterExecutionException {
        List<String> paramEntries = null;
        logWriter.debug("DefaultFormatter: BEGIN marshall");
        
        if (!environment.containsKey(AdapterHttpConstants.ENV_KEY_GVBUFFER_INPUT)) {
        	   logWriter.error("manageRequest - Error: GVBuffer object received from GreenVulcano ESB is null");
               throw new FormatterExecutionException("GVHTTP_INPUT_GVBUFFER_MISSING");
        }
        
        GVBuffer inputGVBuffer = (GVBuffer) environment.get(AdapterHttpConstants.ENV_KEY_GVBUFFER_INPUT);
        logWriter.debug("DefaultFormatter: Id received from GreenVulcanoESB is: " + inputGVBuffer.getId().toString());
        try {
            String requestString = null;
         
            paramEntries = new ArrayList<String>();
            for (String currReqParamName : paramList) {
                String currParamEntry = null;
                String currReqParamValue = null;
                List<InterfaceParametersHandler> currParamHandlerList = paramHandlers.get(currReqParamName);
                if (currParamHandlerList != null) {
                    for (InterfaceParametersHandler currHandler : currParamHandlerList) {
                        currHandler.setCharEncoding(characterEncoding);
                        logWriter.debug("DefaultFormatter - request parameter '" + currReqParamName
                                + "' handler is a " + currHandler.getClass().getName());
                        currReqParamValue = (String) currHandler.build(inputGVBuffer, currReqParamValue);
                        logWriter.debug("DefaultFormatter - request parameter '" + currReqParamName
                                + "' value set to: " + currReqParamValue + " by handler");
                    }

                    if (urlEncoding) {
                        currReqParamValue = URLEncoder.encode(currReqParamValue, characterEncoding);
                        logWriter.debug("manageRequest - request parameter '" + currReqParamName
                                + "' value URLEncoded to: " + currReqParamValue);
                    }
                    currParamEntry = paramNamesWithinQueryString.get(currReqParamName) + currReqParamValue;
                }
                paramEntries.add(currParamEntry);
            }

            requestString = buildRequestString(paramEntries);
      
            logWriter.debug("manageRequest - Request string = " + requestString);

            logWriter.debug("OUTBOUND_REQUEST_HANDLING_END");
        
        } catch (FormatterExecutionException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new FormatterExecutionException("GVHTTP_ERROR", exc);
        }
    }

    /**
     * @param paramEntries
     * @return
     */
    private String buildRequestString(List<String> paramEntries)
    {
        return null;
    }

    /**
     * @see it.greenvulcano.gvesb.adapter.http.formatters.Formatter#unMarshall(java.util.Map)
     */
    @Override
    public void unMarshall(Map<String, Object> environment) throws FormatterExecutionException
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.adapter.http.formatters.Formatter#getId()
     */
    @Override
    public String getId()
    {
        return Id;
    }
}
