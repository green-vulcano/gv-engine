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
package it.greenvulcano.util.file;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPFile;
import org.w3c.dom.Node;

/**
 * Class implementing the <code>FileFilter</code> interface and implementing a
 * filter action based on:
 * <ul>
 * <li>a) a regexp pattern matching for the file name
 * <li>b) a file change occurred after the given timestamp
 * <ul>
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class RegExFileFilter implements FileFilter
{
    /**
     * Search for <code>files-only</code>
     */
    public static final int FILES_ONLY          = 0;

    /**
     * Search for <code>directories-only</code>
     */
    public static final int DIRECTORIES_ONLY    = 1;

    /**
     * Search for <code>all</code>
     */
    public static final int ALL                 = 2;

    private String          namePattern         = "";
    private Pattern         pattern             = null;
    private boolean         checkLastModified   = false;
    private boolean         selectModifiedSince = true;
    private long            lastTimestamp       = -1;
    private int             fileType;

    public static RegExFileFilter buildFileFilter(Node node) throws Exception {
        String fileType = XMLConfig.get(node, "@file-type", "files-only");
        String namePattern = XMLConfig.get(node, "@file-mask", "");
        int type = -1;
        if (fileType.equals("all")) {
            type = ALL;
        }
        else if (fileType.equals("directories-only")) {
            type = DIRECTORIES_ONLY;
        }
        else {
            type = FILES_ONLY;
        }
        return new RegExFileFilter(namePattern, type);
    }

    public static RegExFileFilter buildFileFilter(Node node, Map<String, String> properties) throws Exception {
        try {
            PropertiesHandler.enableExceptionOnErrors();
            String fileType = XMLConfig.get(node, "@file-type", "files-only");
            String namePattern = PropertiesHandler.expand(XMLConfig.get(node, "@file-mask", ""),
                    MapUtils.convertToHMStringObject(properties));
            int type = -1;
            if (fileType.equals("all")) {
                type = ALL;
            }
            else if (fileType.equals("directories-only")) {
                type = DIRECTORIES_ONLY;
            }
            else {
                type = FILES_ONLY;
            }
            return new RegExFileFilter(namePattern, type);
        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
        }
    }

    public static RegExFileFilter buildFileFilter(Node node, Object propsObj) throws Exception {
        try {
            PropertiesHandler.enableExceptionOnErrors();
            String fileType = XMLConfig.get(node, "@file-type", "files-only");
            String namePattern = PropertiesHandler.expand(XMLConfig.get(node, "@file-mask", ""), null, propsObj);
            int type = -1;
            if (fileType.equals("all")) {
                type = ALL;
            }
            else if (fileType.equals("directories-only")) {
                type = DIRECTORIES_ONLY;
            }
            else {
                type = FILES_ONLY;
            }
            return new RegExFileFilter(namePattern, type);
        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
        }
    }

    public RegExFileFilter(String namePattern, int fileType) {
        this.namePattern = namePattern;
        if ((namePattern != null) && (namePattern.length() > 0)) {
            if (PropertiesHandler.isExpanded(namePattern)) {
                this.pattern = Pattern.compile(namePattern);
            }
        }
        else {
            this.pattern = null;
        }

        this.fileType = fileType;
    }

    public RegExFileFilter(String namePattern, int fileType, long lastTimestamp) {
        this.namePattern = namePattern;
        if ((namePattern != null) && (namePattern.length() > 0)) {
            if (PropertiesHandler.isExpanded(namePattern)) {
                this.pattern = Pattern.compile(namePattern);
            }
        }
        else {
            this.pattern = null;
        }

        this.fileType = fileType;
        if (lastTimestamp > 0) {
            this.checkLastModified = true;
            this.lastTimestamp = lastTimestamp;
        }
    }

    public RegExFileFilter(String namePattern, int fileType, long timestamp, boolean selectModifiedSince) {
        this.namePattern = namePattern;
        if ((namePattern != null) && (namePattern.length() > 0)) {
            if (PropertiesHandler.isExpanded(namePattern)) {
                this.pattern = Pattern.compile(namePattern);
            }
        }
        else {
            this.pattern = null;
        }

        this.fileType = fileType;
        if (timestamp > 0) {
            this.checkLastModified = true;
            this.lastTimestamp = timestamp;
            this.selectModifiedSince = selectModifiedSince;
        }
    }

    /**
     * Defines if the file search must use file timestamp.
     * 
     * @param value
     * @param lastTimestamp
     */
    public void setCheckLastModified(boolean value, long timestamp, boolean selectModifiedSince) {
        checkLastModified = value;
        this.lastTimestamp = timestamp;
        this.selectModifiedSince = selectModifiedSince;
    }

    /**
     * Resolves the meatadata in search file pattern definition.
     * 
     * @param properties
     * @throws Exception
     */
    public void compileNamePattern(Map<String, String> properties) throws Exception {
        String locNamePattern = PropertiesHandler.expand(this.namePattern, MapUtils.convertToHMStringObject(properties));
        this.pattern = Pattern.compile(locNamePattern);
    }

    /**
     * Resolves the meatadata in search file pattern definition.
     * 
     * @param properties
     * @throws Exception
     */
    public void compileNamePattern(Object propsObj) throws Exception {
        String locNamePattern = PropertiesHandler.expand(this.namePattern, null, propsObj);
        this.pattern = Pattern.compile(locNamePattern);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.FileFilter#accept(java.io.File)
     */
    @Override
    public boolean accept(File file) {
        boolean fileTypeMatches = false;
        boolean nameMatches = false;
        boolean isModified = false;

        boolean isFile = !file.isDirectory();
        fileTypeMatches = ((fileType == ALL) || ((fileType == FILES_ONLY) && isFile) || ((fileType == DIRECTORIES_ONLY) && !isFile));

        if (fileTypeMatches) {
            if (pattern != null) {
                Matcher m = pattern.matcher(file.getName());
                nameMatches = m.matches();
            }
            else {
                nameMatches = true;
            }

            if (nameMatches) {
                if (checkLastModified) {
                    isModified = selectModifiedSince ? (file.lastModified() > lastTimestamp) : (file.lastModified() <= lastTimestamp);
                }
                else {
                    isModified = true;
                }
            }
        }

        return fileTypeMatches && nameMatches && isModified;
    }

    public boolean accept(FTPFile file) {
        boolean fileTypeMatches = false;
        boolean nameMatches = false;
        boolean isModified = false;

        boolean isFile = !file.isDirectory();
        fileTypeMatches = ((fileType == ALL) || ((fileType == FILES_ONLY) && isFile) || ((fileType == DIRECTORIES_ONLY) && !isFile));

        if (fileTypeMatches) {
            if (pattern != null) {
                Matcher m = pattern.matcher(file.getName());
                nameMatches = m.matches();
            }
            else {
                nameMatches = true;
            }

            if (nameMatches) {
                if (checkLastModified) {
                    isModified = selectModifiedSince ? (file.getTimestamp().getTimeInMillis() > lastTimestamp) : (file.getTimestamp().getTimeInMillis() <= lastTimestamp);
                }
                else {
                    isModified = true;
                }
            }
        }

        return fileTypeMatches && nameMatches && isModified;
    }    

    public static int getFileType(String fileType) {
        if (fileType.equals("all")) {
            return ALL;
        }
        else if (fileType.equals("directories-only")) {
            return DIRECTORIES_ONLY;
        }
        else {
            return FILES_ONLY;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String regExp = (pattern != null) ? pattern.pattern() : namePattern;
        if (fileType == ALL) {
            return regExp + "||all";
        }
        else if (fileType == DIRECTORIES_ONLY) {
            return regExp + "||directories-only";
        }
        return regExp + "||files-only";
    }
}
