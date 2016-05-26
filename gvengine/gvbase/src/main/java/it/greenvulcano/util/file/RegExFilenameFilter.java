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
package it.greenvulcano.util.file;

import it.greenvulcano.configuration.XMLConfig;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

/**
 * Class implementing the <code>FilenameFilter</code> interface and implementing a
 * filter action based on a regexp pattern matching for the file name
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class RegExFilenameFilter implements FilenameFilter
{
    private final Pattern   pattern;

    public static RegExFilenameFilter buildFileFilter(Node node) throws Exception
    {
        String namePattern = XMLConfig.get(node, "@file-mask", "");
        return new RegExFilenameFilter(namePattern);
    }


    public RegExFilenameFilter(String namePattern)
    {
        if ((namePattern != null) && (namePattern.length() > 0)) {
            this.pattern = Pattern.compile(namePattern);
        }
        else {
            this.pattern = null;
        }
    }

    /* (non-Javadoc)
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    @Override
    public boolean accept(File dir, String name)
    {
        if (pattern != null) {
            Matcher m = pattern.matcher(name);
            return m.matches();
        }
        return true;
    }

    public boolean accept(String name)
    {
        if (pattern != null) {
            Matcher m = pattern.matcher(name);
            return m.matches();
        }
        return true;
    }

}
