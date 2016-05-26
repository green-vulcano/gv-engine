/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.debug;

import java.util.StringTokenizer;

/**
 * Version identifier for debugger.
 * 
 * @version 3.3.0 Feb 24, 2013
 * @author GreenVulcano Developer Team
 */
public class Version implements Comparable<Version>
{
    private final int           major;
    private final int           minor;
    private final int           micro;
    private static final String SEPARATOR = ".";

    public Version(String version)
    {
        int maj = 0;
        int min = 0;
        int mic = 0;
        StringTokenizer st = new StringTokenizer(version, SEPARATOR, true);
        maj = Integer.parseInt(st.nextToken());

        if (st.hasMoreTokens()) {
            st.nextToken();
            min = Integer.parseInt(st.nextToken());

            if (st.hasMoreTokens()) {
                st.nextToken();
                mic = Integer.parseInt(st.nextToken());
            }
        }

        major = maj;
        minor = min;
        micro = mic;
    }

    public int getMajor()
    {
        return major;
    }

    public int getMinor()
    {
        return minor;
    }

    public int getMicro()
    {
        return micro;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append(major);
        result.append(SEPARATOR);
        result.append(minor);
        result.append(SEPARATOR);
        result.append(micro);
        return result.toString();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object)
    {
        if (object == this) {
            return true;
        }

        if (!(object instanceof Version)) {
            return false;
        }

        Version other = (Version) object;
        return (major == other.major) && (minor == other.minor) && (micro == other.micro);
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Version other)
    {
        if (other == this) {
            return 0;
        }

        int result = major - other.major;
        if (result != 0) {
            return result;
        }

        result = minor - other.minor;
        if (result != 0) {
            return result;
        }

        return micro - other.micro;
    }
}
