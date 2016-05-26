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
package it.greenvulcano.util.txt;

/**
 * Utility class representing Date differences.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DateDiff {
    public int dateDiff   = 0;
    public int weekOffset = 0;
    public int weekDiff   = 0;
    public int yearDiff   = 0;
    public int monthDiff  = 0;

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Date difference : ").append(dateDiff).append("\n");
        sb.append("Week difference : ").append(weekDiff).append("\n");
        sb.append("Month difference: ").append(monthDiff).append("\n");
        sb.append("Year difference : ").append(yearDiff);
        return sb.toString();
    }
}
