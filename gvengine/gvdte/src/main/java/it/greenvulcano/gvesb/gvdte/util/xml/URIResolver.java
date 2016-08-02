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
package it.greenvulcano.gvesb.gvdte.util.xml;

import it.greenvulcano.gvesb.gvdte.config.DataSource;
import it.greenvulcano.gvesb.gvdte.config.DataSourceFactory;
import java.io.ByteArrayInputStream;
import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;

/**
 * URIResolver class.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class URIResolver implements javax.xml.transform.URIResolver
{
    private static Logger     logger        = org.slf4j.LoggerFactory.getLogger(URIResolver.class);
    private DataSourceFactory dsFactory     = null;
    private String            dataSourceSet = "";

    private final static String PLACEHOLDER = "__GVDTE__/";

    /**
     * @param dsf
     */
    public URIResolver(DataSourceFactory dsf)
    {
        dsFactory = dsf;
        dataSourceSet = "Default";
    }

    /**
     * @param dss
     * @param dsf
     */
    public URIResolver(String dss, DataSourceFactory dsf)
    {
        dsFactory = dsf;
        dataSourceSet = dss;
    }

    /**
     * @see javax.xml.transform.URIResolver#resolve(java.lang.String,
     *      java.lang.String)
     */
    public Source resolve(String href, String base) throws TransformerException
    {
        try {
            String path = "";
            String bpath = "";
            path = removeProtocol(href);
            if (base != null) {
                bpath = removeProtocol(base);
                bpath = removeFileName(bpath);
                if ((href.indexOf(File.separatorChar) != 0) && (bpath.length() > 0)) {
                    path = bpath + "/" + path;
                }
            }

            String canonical = (new File(PLACEHOLDER + path)).getCanonicalPath();
            canonical = canonical.substring(canonical.indexOf(PLACEHOLDER) + PLACEHOLDER.length());
            logger.debug("URIResolver - resolve href: " + href + " with base: " + base + " -> " + canonical);
            DataSource ds = dsFactory.getDataSource(dataSourceSet, path);
            Source source = new StreamSource(new ByteArrayInputStream(ds.getResourceAsByteArray(path)));
            source.setSystemId(path);
            return source;
        }
        catch (Exception exc) {
            throw new TransformerException(
                    "Catched exception resolving " + href + " on dataSourceSet " + dataSourceSet, exc);
        }
    }

    /**
     * @param bpath
     * @return
     */
    private String removeFileName(String path)
    {
        path = handleFileSeparator(path);
        int idx = path.lastIndexOf(File.separatorChar);
        if (idx != -1) {
            path = path.substring(0, idx);
        }
        return path;
    }

    /**
     * @param href
     * @return
     */
    private String removeProtocol(String href)
    {
        String path;
        int idx = href.lastIndexOf("://");
        if (idx != -1) {
            path = href.substring(idx + 3);
        }
        else {
            path = href;
        }
        return path;
    }

    /**
     * In the case of a repository located on a file system, replaces file
     * separator character used into the resource name with the actual file
     * separator in use on the FS
     */
    private String handleFileSeparator(String path)
    {
        String result = path;
        if (path.lastIndexOf('\\') != -1) {
            result = path.replace('\\', File.separatorChar);
        }
        else if (path.lastIndexOf('/') != -1) {
            result = path.replace('/', File.separatorChar);
        }
        return result;
    }
}
