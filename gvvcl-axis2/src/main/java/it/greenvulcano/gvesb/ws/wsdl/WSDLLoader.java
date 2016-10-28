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
package it.greenvulcano.gvesb.ws.wsdl;

import it.greenvulcano.configuration.XMLConfig;
import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * This is the interface for the WSDL file loader.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public abstract class WSDLLoader
{
    String                      nameWSDLFile = "";
    String                      urlWSDLStr   = "";

    /**
     * The log4j logger
     */
    private static final Logger logger       = org.slf4j.LoggerFactory.getLogger(WSDLLoader.class);

    /**
     * It initialized with his configuration node.
     *
     * @param wsdlLoaderConfigNode
     *        Node configuration
     * @throws WSDLLoaderException
     */
    public void init(Node wsdlLoaderConfigNode) throws WSDLLoaderException
    {
        try {
            nameWSDLFile = XMLConfig.get(wsdlLoaderConfigNode, "@wsdl");
            logger.debug("nameWSDLFile '" + nameWSDLFile + "'");
        }
        catch (Exception exc) {
            throw new WSDLLoaderException("GVVCL_XML_CONFIG_ERROR", new String[][]{{"exc", "" + exc}}, exc);
        }
    }

    /**
     * It initialized with WSDL file name.
     *
     * @param nameWSDLFile
     *        Node configuration
     * @throws WSDLLoaderException
     */
    public void init(String nameWSDLFile) throws WSDLLoaderException
    {
        if ((nameWSDLFile == null) || nameWSDLFile.equals("")) {
            throw new WSDLLoaderException("GVVCL_XML_CONFIG_ERROR", new String[][]{{"exc",
                    "" + "The name of wsdl not be null or empty"}});
        }
        this.nameWSDLFile = nameWSDLFile;
        logger.debug("nameWSDLFile '" + nameWSDLFile + "'");
    }

    /**
     * @return Returns the nameWSDLFile.
     */
    public String getNameWSDLFile()
    {
        return nameWSDLFile;
    }

    /**
     * @return Returns the urlWSDLStr.
     * @throws WSDLLoaderException
     */
    public abstract String getUrlWSDLStr() throws WSDLLoaderException;

}
