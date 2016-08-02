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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

/**
 * This class provides utility method to manage files on the local file system.
 *
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
*/
public final class FileManager {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(FileManager.class);

    /**
     *
     */
    private FileManager()
    {
        // do nothing
    }

    /**
     * Check if the file actually exists on the local file system.
     * The filename may be a regular expression. The return value
     * is a <code>java.util.Set</code> of <code>FileProperties</code> objects,
     * holding informations about the matching files.
     *
     * @param parentDirectory
     *            Absolute pathname of the parent directory of the file to be searched.
     * @param filePattern
     *            the name of the file. May be a regular expression.
     * @return a <code>java.util.Set</code> of
     *         <code>FileProperties</code> objects.
     * @throws IOException
     */
    public static Set<FileProperties> ls(String parentDirectory, String filePattern) throws IOException {
        File targetDir = new File(parentDirectory);
        if (!targetDir.isAbsolute()) {
            throw new IllegalArgumentException("The pathname of the parent directory is NOT absolute: "
                    + parentDirectory);
        }
        if (!targetDir.exists()) {
            throw new IllegalArgumentException("Parent directory (" + targetDir.getAbsolutePath()
                    + ") NOT found on local file system.");
        }
        if (!targetDir.isDirectory()) {
            throw new IllegalArgumentException("Parent directory (" + targetDir.getAbsolutePath()
                    + ") is NOT a directory.");
        }
        File[] files = targetDir.listFiles(new RegExFileFilter(filePattern, RegExFileFilter.ALL));
        Set<FileProperties> matchingFiles = new HashSet<FileProperties>(files.length);
        for (File file : files) {
            matchingFiles.add(new FileProperties(file));
        }
        return matchingFiles;
    }

    /**
     * Delete the file/directory from the local file system.<br>
     * The filePattern may be a regular expression: in this case, all
     * the file names matching the pattern will be deleted.<br>
     *
     * @param targetPath
     *            Absolute pathname of the target file/directory.
     * @param filePattern
     *            A regular expression defining the name of the files to be deleted. Used only if
     *            targetPath identify a directory.
     * @throws Exception
     */
    public static void rm(String targetPath, String filePattern) throws Exception {
        File target = new File(targetPath);
        if (!target.isAbsolute()) {
            throw new IllegalArgumentException("The pathname of the target file is NOT absolute: " + targetPath);
        }
        if (target.isDirectory()) {
            if ((filePattern == null) || filePattern.equals("")) {
                logger.debug("Removing directory " + target.getAbsolutePath());
                FileUtils.deleteDirectory(target);
            }
            else if (filePattern.equals(".*")) {
                logger.debug("Removing directory " + target.getAbsolutePath() + " content.");
                FileUtils.cleanDirectory(target);
            }
            else {
                Set<FileProperties> files = ls(targetPath, filePattern);
                for (FileProperties file : files) {
                    logger.debug("Removing file/directory " + file.getName() + " from directory " + target.getAbsolutePath());
                    FileUtils.forceDelete(new File(target, file.getName()));
                }
            }
        }
        else {
            logger.debug("Removing file " + target.getAbsolutePath());
            FileUtils.forceDelete(target);
        }
    }

    /**
     * Move a file/directory on the local file system.<br>
     *
     * @param oldPath
     *            Absolute pathname of the file/directory to be renamed
     * @param newPath
     *            Absolute pathname of the new file/directory
     * @param filePattern
     *            A regular expression defining the name of the files to be copied. Used only if
     *            srcPath identify a directory. Null or "" disable file filtering.
     * @throws Exception
     */
    public static void mv(String oldPath, String newPath, String filePattern) throws Exception {
        File src = new File(oldPath);
        if (!src.isAbsolute()) {
            throw new IllegalArgumentException("The pathname of the source is NOT absolute: " + oldPath);
        }
        File target = new File(newPath);
        if (!target.isAbsolute()) {
            throw new IllegalArgumentException("The pathname of the destination is NOT absolute: " + newPath);
        }
        if (target.isDirectory()) {
            if ((filePattern == null) || filePattern.equals("")) {
                FileUtils.deleteQuietly(target);
            }
        }
        if (src.isDirectory()) {
            if ((filePattern == null) || filePattern.equals("")) {
                logger.debug("Moving directory " + src.getAbsolutePath() + " to " + target.getAbsolutePath());
                FileUtils.moveDirectory(src, target);
            }
            else {
                Set<FileProperties> files = ls(oldPath, filePattern);
                for (FileProperties file : files) {
                    logger.debug("Moving file " + file.getName() + " from directory " + src.getAbsolutePath() + " to directory "
                        + target.getAbsolutePath());
                    File finalTarget = new File(target, file.getName());
                    FileUtils.deleteQuietly(finalTarget);
                    if (file.isDirectory()) {
                        FileUtils.moveDirectoryToDirectory(new File(src, file.getName()), finalTarget, true);
                    }
                    else  {
                        FileUtils.moveFileToDirectory(new File(src, file.getName()), target, true);
                    }
                }
            }
        }
        else {
            if (target.isDirectory()) {
                File finalTarget = new File(target, src.getName());
                FileUtils.deleteQuietly(finalTarget);
                logger.debug("Moving file " + src.getAbsolutePath() + " to directory " + target.getAbsolutePath());
                FileUtils.moveFileToDirectory(src, target, true);
            }
            else {
                logger.debug("Moving file " + src.getAbsolutePath() + " to " + target.getAbsolutePath());
                FileUtils.moveFile(src, target);
            }
        }
    }

    /**
     * Copy the file/directory to a new one.<br>
     * The filePattern may be a regular expression: in this case, all the filenames
     * matching the pattern will be copied to the target directory.<br>
     * If a file, whose name matches the <code>filePattern</code> pattern, already
     * exists in the same target directory, it will be overwritten.<br>
     *
     * @param sourcePath
     *            Absolute pathname of the source file/directory.
     * @param targetPath
     *            Absolute pathname of the target file/directory.
     * @param filePattern
     *            A regular expression defining the name of the files to be copied. Used only if
     *            srcPath identify a directory. Null or "" disable file filtering.
     * @throws Exception
     */
    public static void cp(String sourcePath, String targetPath, String filePattern) throws Exception {
        File src = new File(sourcePath);
        if (!src.isAbsolute()) {
            throw new IllegalArgumentException("The pathname of the source file is NOT absolute: " + sourcePath);
        }
        File target = new File(targetPath);
        if (!target.isAbsolute()) {
            throw new IllegalArgumentException("The pathname of the target file is NOT absolute: " + targetPath);
        }

        if (src.isDirectory()) {
            if ((filePattern == null) || filePattern.equals("") || filePattern.equals(".*")) {
                logger.debug("Copying directory " + src.getAbsolutePath() + " to directory " + target.getAbsolutePath());
                FileUtils.copyDirectory(src, new File(targetPath));
            }
            else {
                Set<FileProperties> files = ls(sourcePath, filePattern);
                for (FileProperties file : files) {
                    logger.debug("Copying file " + file.getName() + " from directory " + src.getAbsolutePath() + " to directory "
                        + target.getAbsolutePath());
                    if (file.isDirectory()) {
                        FileUtils.copyDirectoryToDirectory(new File(src, file.getName()), target);
                    }
                    else  {
                        FileUtils.copyFileToDirectory(new File(src, file.getName()), target);
                    }
                }
            }
        }
        else {
            if (target.isDirectory()) {
                logger.debug("Copying file " + src.getAbsolutePath() + " to directory " + target.getAbsolutePath());
                FileUtils.copyFileToDirectory(src, target);
            }
            else {
                logger.debug("Copying file " + src.getAbsolutePath() + " to " + target.getAbsolutePath());
                FileUtils.copyFile(src, target);
            }
        }
    }
}
