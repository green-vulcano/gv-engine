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
package it.greenvulcano.gvesb.ws.rampart.policy.pwcb;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.1.0 22/giu/2011
 * @author GreenVulcano Developer Team
 */
public class PWCBManager implements ConfigurationListener
{
    private static Logger            logger          = org.slf4j.LoggerFactory.getLogger(PWCBManager.class);

    private static final String      CFG_FILE        = "GVWebServices.xml";
    private static PWCBManager       instance        = null;

    private Map<String, PWCBHandler> handlers        = new HashMap<String, PWCBHandler>();
    private boolean                  confChangedFlag = true;

    /**
     *
     */
    private PWCBManager()
    {
        // do nothing
    }

    public static synchronized PWCBManager instance() throws Exception
    {
        if (instance == null) {
            try {
                instance = new PWCBManager();
                instance.init();
                XMLConfig.addConfigurationListener(instance, CFG_FILE);
            }
            catch (Exception exc) {
                instance = null;
                throw exc;
            }
        }
        return instance;
    }

    public void init() throws Exception
    {
        logger.debug("PWCBManager - Init Begin");
        NodeList nl = XMLConfig.getNodeList(CFG_FILE, "/GVWebServices/AxisExtra/PasswordCallback/*[@type='pwcb']");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            PWCBHandler hnd = (PWCBHandler) Class.forName(XMLConfig.get(n, "@class")).newInstance();
            hnd.init(n);
            handlers.put(hnd.getType(), hnd);
        }
        confChangedFlag = false;
        logger.debug("PWCBManager - Init End");
    }

    /**
     * @param type
     * @param id
     * @return
     */
    public String resolve(String type, String id) throws Exception
    {
        if (confChangedFlag) {
            init();
        }

        PWCBHandler hnd = handlers.get(type);
        if (hnd != null) {
            return hnd.resolve(id);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.configuration.ConfigurationListener#configurationChanged
     * (it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && (event.getFile().equals(CFG_FILE))) {
            confChangedFlag = true;
        }
    }


}
