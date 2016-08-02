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
package it.greenvulcano.gvesb.j2ee.db.formatter;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * This class encapsulates informations concerning a parameter of CallSPFormatter.
 *
 *  @version     3.0.0 Feb 17, 2010
 *  @author     GreenVulcano Developer Team
 *
 *
*/
public class SPOutputParam {
    private static Logger logger   = org.slf4j.LoggerFactory.getLogger(SPOutputParam.class);

    /**
     * Type of parameter
     */
    private String        type     = null;

    /**
     * Position of parameter in the SQL statement.
     */
    private int           position = 0;

    /**
     * Constructor
     *
     * @param node
     *            the configuration node
     * @throws GVDBException
     *             if an error occurred
     */
    public SPOutputParam(Node node) throws GVDBException {
        logger.debug("Constructor start");

        try {
            type = XMLConfig.get(node, "@type");
            logger.debug("Attribute type : " + type);
            position = Integer.parseInt(XMLConfig.get(node, "@position"));
            logger.debug("Attribute position : " + position);
        }
        catch (XMLConfigException exc) {
            logger.error("init - Error while accessing configuration informations via XMLConfig: ", exc);
            throw new GVDBException("EB_GENERIC_ERROR", new String[][] { { "msg",
                    "Error while accessing configuration informations via XMLConfig" + exc } }, exc);
        }
        catch (Throwable exc) {
            logger.error("init - Generic Error : ", exc);
            throw new GVDBException("EB_GENERIC_ERROR", new String[][] { { "msg", exc.toString() } }, exc);
        }
    }

    /**
     * Set the type.
     *
     * @param type
     *            The value to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Set the position of parameter into the SQL Statement.
     *
     * @param position
     *            The value to set.
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Get the type attribute.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Get the position of parameter into the SQL Statement.
     *
     * @return the position
     */
    public int getPosition() {
        return position;
    }
}
