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
package it.greenvulcano.gvesb.core.jmx;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;
import it.greenvulcano.jmx.JMXUtils;
import org.slf4j.Logger;
import org.w3c.dom.NodeList;

/**
 * GroupInfo class.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class GroupInfo
{
    private static final Logger logger          = org.slf4j.LoggerFactory.getLogger(GroupInfo.class);

    /**
     * the object JMX descriptor
     */
    public static final String  DESCRIPTOR_NAME = "GroupInfo";
    /**
     * the group name
     */
    private String              name            = "";
    /**
     * the jmx filter for inter-instances communication
     */
    private String              jmxFilter       = "";
    /**
     * the list of service member of the group
     */
    private String[]            serviceImpact   = null;
    /**
     * the status of the group isActive.
     */
    private boolean             isActive      = true;

    /**
     * Constructor
     *
     * @param gName
     *        the group name
     * @param act
     *        the isActive flag
     */
    public GroupInfo(String gName, boolean act)
    {
        name = gName;
        isActive = act;
        jmxFilter = "GreenVulcano:*,Component=" + ServiceOperationInfo.DESCRIPTOR_NAME
        + ",Group=management,Internal=Yes,IDGroup=" + name;

        initServiceImpact();
    }

    /**
     * Initialize the member services list
     */
    private void initServiceImpact()
    {
        try {
            serviceImpact = null;
            NodeList services = XMLConfig.getNodeList(GreenVulcanoConfig.getServicesConfigFileName(),
                    "/GVServices/Services/Service[@id-group='" + name + "']");

            int num = services.getLength();
            serviceImpact = new String[num];
            for (int i = 0; i < num; i++) {
                serviceImpact[i] = XMLConfig.get(services.item(i), "@id-service");
            }
        }
        catch (XMLConfigException exc) {
            logger.error("Unable to initialize serviceImpact list.", exc);
            serviceImpact = null;
        }
    }

    /**
     * Get the status of the group isActive. <br/>
     * <br/>
     *
     * @return The Group isActive flag
     */
    public boolean getIsActive()
    {
        return isActive;
    }

    /**
     * @param act
     *        the isActive flag
     * @exception Exception
     *            if errors occurs
     */
    public void setIsActive(boolean act) throws Exception
    {
        isActive = act;
        JMXUtils.set(jmxFilter, "groupisActive", new Boolean(isActive), false, logger);
    }

    /**
     * @return the member services list
     */
    public String[] getServiceImpact()
    {
        return serviceImpact;
    }

    /**
     * @return the group name
     */
    public String getName()
    {
        return name;
    }

}