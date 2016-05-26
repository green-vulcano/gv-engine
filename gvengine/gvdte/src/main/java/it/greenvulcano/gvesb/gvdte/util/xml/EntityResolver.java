/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.gvdte.util.xml;

import it.greenvulcano.gvesb.gvdte.config.DataSource;
import it.greenvulcano.gvesb.gvdte.config.DataSourceFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An EntityResolver that use DataSource to retrieve resources.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class EntityResolver implements org.xml.sax.EntityResolver
{
    private static Logger     logger        = org.slf4j.LoggerFactory.getLogger(EntityResolver.class);
    private DataSourceFactory dsFactory     = null;
    private String            dataSourceSet = "";

    /**
     * @param dsf
     */
    public EntityResolver(DataSourceFactory dsf)
    {
        dsFactory = dsf;
        dataSourceSet = "Default";
    }

    /**
     * @param dss
     * @param dsf
     */
    public EntityResolver(String dss, DataSourceFactory dsf)
    {
        dsFactory = dsf;
        dataSourceSet = dss;
    }

    /**
     * Retrieve the requested resource through a DataSource.
     *
     * @param publicId
     *        The public identifier of the external entity being referenced, or
     *        null if none was supplied.
     * @param systemId
     *        The system identifier of the external entity being referenced.
     * @return An InputSource object describing the new input source, or null to
     *         request that the parser open a regular URI connection to the
     *         system identifier.
     * @throws SAXException
     *         Any SAX exception, possibly wrapping another exception.
     * @throws IOException
     *         A Java-specific IO exception, possibly the result of creating a
     *         new InputStream or Reader for the InputSource.
     */
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
    {
        logger.debug("Resolve Entity - systemId = " + systemId + " - publicId = " + publicId);
        if (systemId == null) {
            return null;
        }

        try {
            String path = "";
            int idx = systemId.lastIndexOf("://");
            if (idx != -1) {
                path = systemId.substring(idx + 3);
            }
            else {
                path = systemId;
            }
            DataSource ds = dsFactory.getDataSource(dataSourceSet, path);
            return new InputSource(new ByteArrayInputStream(ds.getResourceAsByteArray(path)));
        }
        catch (Exception exc) {
            throw new IOException("Catched exception resolving " + systemId + " - exception: " + exc, exc);
        }
    }
}
