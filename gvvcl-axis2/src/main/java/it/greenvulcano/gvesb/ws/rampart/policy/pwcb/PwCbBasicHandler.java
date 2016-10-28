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

import it.greenvulcano.configuration.XMLConfig;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PwCbBasicHandler extends AbstractPWCBHandler
{
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(PwCbBasicHandler.class);

    public static final String TYPE    = "PwCbBasicHandler";
    public Map<String, String> mapping = new HashMap<String, String>();

    /*
     * (non-Javadoc)
     *
     * @see test.rampart.policy.PWCBHandler#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws Exception
    {
        mapping.clear();
        NodeList nl = XMLConfig.getNodeList(node, "UserDef");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            String name = XMLConfig.get(n, "@name");
            String pwd = XMLConfig.getDecrypted(n, "@password");
            logger.debug("PwCbBasicHandler - Insert entry: " + name);// + "/" + pwd);
            mapping.put(name, pwd);
        }
    }

    /* (non-Javadoc)
     * @see test.rampart.policy.pwcb.PWCBHandler#getType()
     */
    @Override
    public String getType()
    {
        return TYPE;
    }

    /*
     * (non-Javadoc)
     *
     * @see test.rampart.policy.PWCBHandler#resolve(java.lang.String)
     */
    @Override
    public String resolve(String name) throws Exception
    {
        return mapping.get(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see test.rampart.policy.PWCBHandler#destroy()
     */
    @Override
    public void destroy()
    {
        mapping.clear();
    }

}
