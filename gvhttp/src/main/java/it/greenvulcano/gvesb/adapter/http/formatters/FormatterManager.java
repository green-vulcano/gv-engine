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
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpConstants;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * This is the Manager for the formatters.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class FormatterManager
{
    private static Logger          logger     = org.slf4j.LoggerFactory.getLogger(FormatterManager.class);

    /**
     * This Map contains the created formatters
     */
    private Map<String, Formatter> formatters = new HashMap<String, Formatter>();

    public FormatterManager() throws FormatterInitializationException
    {
        init();
    }

    /**
     * @throws FormatterInitializationException
     */
    public void init() throws FormatterInitializationException
    {
        logger.debug("FormatterManager: BEGIN - Operation(init)");

        logger.debug("FormatterManager: END - Operation(init)");
    }

    /**
     * This method returns a Formatter, given its ID
     * 
     * @param formatterID
     * @return Formatter
     * @throws FormatterInitializationException
     */
    public Formatter getFormatter(String formatterID) throws FormatterInitializationException
    {
        logger.debug("getFormatter start");
        Formatter formatter = formatters.get(formatterID);
        if (formatter == null) {
            try {
                Node formatterConfig = XMLConfig.getNode(AdapterHttpConstants.CFG_FILE,
                        "/GVAdapterHttpConfiguration/Formatters/*[@Type='FormatterPlugin' and @ID='" + formatterID
                                + "']");
                String formatterClassname = null;
                formatterClassname = XMLConfig.get(formatterConfig, "@Class");
                logger.debug("getFormatter - this formatter is an instance of " + formatterClassname);
                formatter = (Formatter) Class.forName(formatterClassname).newInstance();
                formatter.init(formatterConfig);
            }
            catch (FormatterInitializationException exc) {
                logger.error("getFormatter - Error configuring Formatter: " + formatterID, exc);
                throw exc;
            }
            catch (Exception exc) {
                logger.error("getFormatter - Error configuring Formatter: " + formatterID, exc);
                throw new FormatterInitializationException("GVHA_FORMATTER_INITIALIZATION_ERROR", new String[][]{{
                        "errorName", "" + exc}}, exc);
            }
            formatters.put(formatterID, formatter);
        }
        logger.debug("getFormatter stop");
        return formatter;
    }

    /**
     * This method is used to inform the class that the configuration has
     * changed.
     * 
     * This information should be propagated to any dependent object.
     * 
     * When the configuration changes, any configurable object must check if it
     * has to repeat its initialization
     * 
     * @throws FormatterInitializationException
     */
    public void reloadConfiguration() throws FormatterInitializationException
    {
        logger.debug("FormatterManager: BEGIN - Operation(Reload Configuration)");

        formatters.clear();
        init();

        logger.debug("FormatterManager: END - Operation(Reload Configuration)");
    }

    /**
     * This method has to be invoked before deleting references to the object.
     */
    public void destroy()
    {
        logger.info("FormatterManager: BEGIN destroy");
        formatters.clear();
        logger.info("FormatterManager: END destroy");
    }
}
