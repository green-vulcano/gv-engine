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


import java.util.Comparator;
import java.util.Date;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class FileTimeSorter implements Comparator<FileProperties>
{
    private boolean ascending = false;

    public FileTimeSorter(boolean ascending)
    {
        this.ascending = ascending;
    }

    @Override
    public int compare(FileProperties fp1, FileProperties fp2)
    {
        Date d1 = new Date(fp1.getLastModified());
        Date d2 = new Date(fp2.getLastModified());

        if (ascending) {
            return d1.compareTo(d2);
        }
        return d2.compareTo(d1);
    }
}
