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

import it.greenvulcano.util.txt.DateUtils;

import java.io.File;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Utility bean class encapsulating informations about a file/directory.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class FileProperties implements Comparable<FileProperties>
{
    private String    name         = "";
    private long      length       = 0L;
    private long      lastModified = -1L;
    private boolean   isDirectory  = false;
    private boolean[] permission   = new boolean[3]; // canRead, canWrite, canExecute

    /**
     * @param name
     * @param lastModified
     * @param length
     * @param isDirectory
     */
    public FileProperties(String name, long lastModified, long length, boolean isDirectory)
    {
        this.name = name;
        this.lastModified = lastModified;
        this.length = length;
        this.isDirectory = isDirectory;
    }

    /**
     * @param name
     * @param lastModified
     * @param length
     * @param isDirectory
     * @param canRead
     * @param canWrite
     * @param canExecute
     */
    public FileProperties(String name, long lastModified, long length, boolean isDirectory,
                          boolean canRead, boolean canWrite, boolean canExecute)
    {
        this.name = name;
        this.lastModified = lastModified;
        this.length = length;
        this.isDirectory = isDirectory;
        this.permission[0] = canRead;
        this.permission[1] = canWrite;
        this.permission[2] = canExecute;
    }

    /**
     * @param file
     */
    public FileProperties(File file)
    {
        this.name = file.getName();
        this.lastModified = file.lastModified();
        this.length = file.length();
        this.isDirectory = file.isDirectory();
        this.permission[0] = file.canRead();
        this.permission[1] = file.canWrite();
        this.permission[2] = file.canExecute();
    }

    /**
     * Factory method which creates a <code>FileProperties</code> object
     * starting from its serialized version. Returns <code>null</code> on
     * parsing error.
     *
     * @param serialization
     *        the serialized version of a <code>FileProperties</code> object.
     * @return a new <code>FileProperties</code> object parsed from its
     *         serialized version.
     */
    public static FileProperties parse(String serialization)
    {
        FileProperties result = null;
        StringTokenizer st = new StringTokenizer(serialization, "::");
        if (st.countTokens() == 7) {
            result = new FileProperties();
            result.name   = st.nextToken();
            result.length = Long.parseLong(st.nextToken());
            result.lastModified  = Long.parseLong(st.nextToken());
            result.isDirectory   = st.nextToken().equals("X");
            result.permission[0] = st.nextToken().equals("X");
            result.permission[1] = st.nextToken().equals("X");
            result.permission[2] = st.nextToken().equals("X");
        }
        return result;
    }

    /**
     * Returns the name of the file. This is just the last name in the
     * pathname's name sequence.
     *
     * @return the name of the file.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns a <code>long</code> value representing the time the file was last
     * modified.
     *
     * @return a <code>long</code> value representing the time the file was last
     *         modified.
     */
    public long getLastModified()
    {
        return lastModified;
    }

    /**
     * Returns the size, in bytes, of the file.
     *
     * @return the size, in bytes, of the file.
     */
    public long getLength()
    {
        return length;
    }

    /**
     * Returns <code>true</code> if the file is actually a directory;
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if the file is actually a directory;
     *         <code>false</code> otherwise.
     */
    public boolean isDirectory()
    {
        return isDirectory;
    }

    /**
     * Returns <code>true</code> if the file can be read; <code>false</code> otherwise.
     *
     * @return
     */
    public boolean canRead()
    {
        return permission[0];
    }

    /**
     * Returns <code>true</code> if the file can be write; <code>false</code> otherwise.
     *
     * @return
     */
    public boolean canWrite()
    {
        return permission[1];
    }

    /**
     * Returns <code>true</code> if the file is executable; <code>false</code> otherwise.
     *
     * @return
     */
    public boolean canExecute()
    {
        return permission[2];
    }

    /**
     * Returns a <code>String</code> representation of this
     * <code>FileProperties</code> object in the format
     * 'filename::size::lastmodified::isDirectory::canRead::canWrite::canExecute'.
     *
     * @return a <code>String</code> representation of this
     *         <code>FileProperties</code> object.
     */
    public String serialize()
    {
        return name + "::" + length + "::" + lastModified + "::" + (isDirectory ? "X" : "0") + "::"
        + (permission[0] ? "X" : "0") + "::" + (permission[1] ? "X" : "0") + "::" + (permission[2] ? "X" : "0");
    }

    /**
     * Returns a <code>String</code> representation of this
     * <code>FileProperties</code> object.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "[" + name + ", " + length + " bytes, MT " + DateUtils.dateToString(new Date(lastModified), "yyyyMMdd HHmmss.SSS") +
               ", DRWX:" + (isDirectory ? "X" : "0") + (permission[0] ? "X" : "0") + (permission[1] ? "X" : "0") + (permission[2] ? "X" : "0") +"]";
    }

    /**
     * Private default constructor.
     */
    private FileProperties()
    {
        name = "";
        lastModified = 0L;
        length = 0L;
        isDirectory = true;
        permission   = new boolean[3];
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return (((name == null) ? 0 : name.hashCode()) + "::" + isDirectory).hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (! (obj instanceof FileProperties)) {
            return false;
        }

        return compareTo((FileProperties) obj) == 0;
    }

    /**
     * Compare files. Directory are less than files.
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(FileProperties other)
    {
        if (isDirectory == other.isDirectory) {
            return name.compareTo(other.name);
        }
        if (isDirectory) {
            return -1;
        }
        return 1;
    }
}
