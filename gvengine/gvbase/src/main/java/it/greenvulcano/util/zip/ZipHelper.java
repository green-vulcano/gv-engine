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
package it.greenvulcano.util.zip;

import it.greenvulcano.util.file.RegExFilenameFilter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * ZipHelper class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ZipHelper
{
    /**
     * Compression level for best compression.
     */
    public static final String              BEST_COMPRESSION_S    = "best-compression";
    /**
     * Compression level for fastest compression.
     */
    public static final String              BEST_SPEED_S          = "best-speed";
    /**
     * Default compression level.
     */
    public static final String              DEFAULT_COMPRESSION_S = "default-compression";
    /**
     * Compression level for no compression.
     */
    public static final String              NO_COMPRESSION_S      = "no-compression";
    /**
     * Compression level for best compression.
     */
    public static final int                 BEST_COMPRESSION      = Deflater.BEST_COMPRESSION;
    /**
     * Compression level for fastest compression.
     */
    public static final int                 BEST_SPEED            = Deflater.BEST_SPEED;
    /**
     * Default compression level.
     */
    public static final int                 DEFAULT_COMPRESSION   = Deflater.DEFAULT_COMPRESSION;
    /**
     * Compression level for no compression.
     */
    public static final int                 NO_COMPRESSION        = Deflater.NO_COMPRESSION;
    /**
     * Default inflating temporary buffer size.
     */
    private static final int                DEFAULT_SIZE          = 1024;

    /**
     * Compression level
     */
    private int                             compressionLevel      = BEST_SPEED;
    /**
     * Compresser instance
     */
    private Deflater                        compresser            = null;
    /**
     * Decompresser instance
     */
    private Inflater                        decompresser          = null;
    /**
     * Map for compression level
     */
    private static HashMap<String, Integer> cLevelMap             = null;

    static {
        if (cLevelMap == null) {
            cLevelMap = new HashMap<String, Integer>();
            cLevelMap.put(BEST_COMPRESSION_S, Integer.valueOf(BEST_COMPRESSION));
            cLevelMap.put(BEST_SPEED_S, Integer.valueOf(BEST_SPEED));
            cLevelMap.put(DEFAULT_COMPRESSION_S, Integer.valueOf(DEFAULT_COMPRESSION));
            cLevelMap.put(NO_COMPRESSION_S, Integer.valueOf(NO_COMPRESSION));
        }
    }

    /**
     * Constructor
     *
     * Create a ZipHelper with default values.
     */
    public ZipHelper()
    {
        // do nothing
    }

    /**
     * Set the compression level to use.
     *
     * @param cLevel
     *        the compression level to use
     * @exception ZipHelperException
     *            if error occurs
     */
    public void setCompressionLevel(int cLevel) throws ZipHelperException
    {
        if ((cLevel != DEFAULT_COMPRESSION) && (cLevel != BEST_COMPRESSION) && (cLevel != BEST_SPEED)
                && (cLevel != NO_COMPRESSION)) {
            throw new ZipHelperException("Invalid compression level: " + cLevel);
        }
        compressionLevel = cLevel;
    }

    /**
     * Set the compression level to use.
     *
     * @param cLevel
     *        the compression level to use
     * @exception ZipHelperException
     *            if error occurs
     */
    public void setCompressionLevel(String cLevel) throws ZipHelperException
    {
        Integer iLevel = cLevelMap.get(cLevel);
        if (iLevel == null) {
            throw new ZipHelperException("Invalid compression level: " + cLevel);
        }
        compressionLevel = iLevel.intValue();
    }

    /**
     * Compress the input buffer.
     *
     * @param input
     *        the data to compress
     * @return the compressed data
     * @throws ZipHelperException
     *         if error occurs
     */
    public byte[] zip(byte[] input) throws ZipHelperException
    {
        try {
            if (compresser == null) {
                compresser = new Deflater(compressionLevel, true);
            }

            byte[] output = new byte[input.length];
            ByteArrayOutputStream outstream = new ByteArrayOutputStream(input.length);
            compresser.setInput(input);
            compresser.finish();
            while (!compresser.needsInput()) {
                int compressedDataLength = compresser.deflate(output, 0, output.length);
                if (compressedDataLength > 0) {
                    outstream.write(output, 0, compressedDataLength);
                }
            }
            if(!compresser.finished()) {
                int compressedDataLength = compresser.deflate(output, 0, output.length);
                if (compressedDataLength > 0) {
                    outstream.write(output, 0, compressedDataLength);
                }
            }
            return outstream.toByteArray();
        }
        catch (Exception exc) {
            throw new ZipHelperException("Error occurred compressing data: " + exc.getMessage(), exc);
        }
        finally {
            if (compresser != null) {
                compresser.reset();
            }
        }
    }

    /**
     * Uncompress the input buffer.
     *
     * @param input
     *        the data to uncompress
     * @return the uncompressed data
     * @throws ZipHelperException
     *         if error occurs
     */
    public byte[] unzip(byte[] input) throws ZipHelperException
    {
        try {
            if (decompresser == null) {
                decompresser = new Inflater(true);
            }

            byte[] output = new byte[DEFAULT_SIZE];
            ByteArrayOutputStream outstream = new ByteArrayOutputStream(input.length);
            decompresser.setInput(input);
            while (!decompresser.needsInput()) {
                int decompressedDataLength = decompresser.inflate(output, 0, output.length);
                if (decompressedDataLength > 0) {
                    outstream.write(output, 0, decompressedDataLength);
                }
            }
            return outstream.toByteArray();
        }
        catch (Exception exc) {
            throw new ZipHelperException("Error occurred decompressing data: " + exc.getMessage(), exc);
        }
        finally {
            if (decompresser != null) {
                decompresser.reset();
            }
        }
    }

    /**
     * Performs the <code>ZIP</code> compression of a file/directory, whose name
     * and parent directory are passed as arguments, on the local filesystem.
     * The result is written into a target file with the <code>zip</code>
     * extension.<br>
     * The source filename may contain a regualr expression: in this
     * case, all the filenames matching the pattern will be compressed and put
     * in the same target <code>zip</code> file.<br>
     *
     *
     * @param srcDirectory
     *            the source parent directory of the file/s to be zipped. Must
     *            be an absolute pathname.
     * @param fileNamePattern
     *            the name of the file to be zipped. May contain a regular expression,
     *            possibly matching multiple files/directories.
     *            If matching a directory, the directory is zipped with all its content as well.
     * @param targetDirectory
     *            the target parent directory of the created <code>zip</code>
     *            file. Must be an absolute pathname.
     * @param zipFilename
     *            the name of the zip file to be created. Cannot be
     *            <code>null</code>, and must have the <code>.zip</code>
     *            extension. If a target file already exists with the same name
     *            in the same directory, it will be overwritten.
     * @throws IOException
     *             If any error occurs during file compression.
     * @throws IllegalArgumentException
     *             if the arguments are invalid.
     */
    public void zipFile(String srcDirectory, String fileNamePattern, String targetDirectory, String zipFilename)
            throws IOException {

        File srcDir = new File(srcDirectory);

        if (!srcDir.isAbsolute()) {
            throw new IllegalArgumentException("The pathname of the source parent directory is NOT absolute: "
                    + srcDirectory);
        }

        if (!srcDir.exists()) {
            throw new IllegalArgumentException("Source parent directory " + srcDirectory
                    + " NOT found on local filesystem.");
        }

        if (!srcDir.isDirectory()) {
            throw new IllegalArgumentException("Source parent directory " + srcDirectory + " is NOT a directory.");
        }

        File targetDir = new File(targetDirectory);

        if (!targetDir.isAbsolute()) {
            throw new IllegalArgumentException("The pathname of the target parent directory is NOT absolute: "
                    + targetDirectory);
        }

        if (!targetDir.exists()) {
            throw new IllegalArgumentException("Target parent directory " + targetDirectory
                    + " NOT found on local filesystem.");
        }

        if (!targetDir.isDirectory()) {
            throw new IllegalArgumentException("Target parent directory " + targetDirectory + " is NOT a directory.");
        }

        if ((zipFilename == null) || (zipFilename.length() == 0)) {
            throw new IllegalArgumentException("Target zip file name is missing.");
        }

        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(new File(targetDir, zipFilename)));
            zos.setLevel(compressionLevel);

            URI base = srcDir.toURI();
            File[] files = srcDir.listFiles(new RegExFilenameFilter(fileNamePattern));

            for (File file : files) {
                internalZipFile(file, zos, base);
            }
        }
        finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            }
            catch (Exception exc) {
                // Do nothing
            }
        }
    }

    /**
     * Performs the uncompression of a <code>zip</code> file, whose name and
     * parent directory are passed as arguments, on the local filesystem. The
     * content of the <code>zip</code> file will be uncompressed within a
     * specified target directory.<br>
     *
     * @param srcDirectory
     *            the source parent directory of the file/s to be unzipped. Must
     *            be an absolute pathname.
     * @param zipFilename
     *            the name of the file to be unzipped. Cannot contain wildcards.
     * @param targetDirectory
     *            the target directory in which the content of the
     *            <code>zip</code> file will be unzipped. Must be an absolute
     *            pathname.
     * @throws IOException
     *             If any error occurs during file uncompression.
     * @throws IllegalArgumentException
     *             if the arguments are invalid.
     */
    public void unzipFile(String srcDirectory, String zipFilename, String targetDirectory) throws IOException {

        File srcDir = new File(srcDirectory);

        if (!srcDir.isAbsolute()) {
            throw new IllegalArgumentException("The pathname of the source parent directory is NOT absolute: "
                    + srcDirectory);
        }

        if (!srcDir.exists()) {
            throw new IllegalArgumentException("Source parent directory " + srcDirectory
                    + " NOT found on local filesystem.");
        }

        if (!srcDir.isDirectory()) {
            throw new IllegalArgumentException("Source parent directory " + srcDirectory + " is NOT a directory.");
        }

        File srcZipFile = new File(srcDirectory, zipFilename);
        if (!srcZipFile.exists()) {
            throw new IllegalArgumentException("File to be unzipped (" + srcZipFile.getAbsolutePath()
                    + ") NOT found on local filesystem.");
        }

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(srcZipFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry currEntry = entries.nextElement();
                if (currEntry.isDirectory()) {
                    String targetSubdirPathname = currEntry.getName();
                    File dir = new File(targetDirectory, targetSubdirPathname);
                    FileUtils.forceMkdir(dir);
                    dir.setLastModified(currEntry.getTime());
                }
                else {
                    InputStream is = null;
                    OutputStream os = null;
                    File file = null;
                    try {
                        is = zipFile.getInputStream(currEntry);
                        FileUtils.forceMkdir(new File(targetDirectory, currEntry.getName()).getParentFile());
                        file = new File(targetDirectory, currEntry.getName());
                        os = new FileOutputStream(file);
                        IOUtils.copy(is, os);
                    }
                    finally {
                        try {
                            if (is != null) {
                                is.close();
                            }
    
                            if (os != null) {
                                os.close();
                            }
                        }
                        catch (IOException exc) {
                            // Do nothing
                        }
                        
                        if (file != null) {
                            file.setLastModified(currEntry.getTime());
                        }
                    }
                }
            }
        }
        finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (Exception exc) {
                // do nothing
            }
        }
    }

    private void internalZipFile(File srcFile, ZipOutputStream zos, URI base) throws IOException {
        String zipEntryName = base.relativize(srcFile.toURI()).getPath();
        if (srcFile.isDirectory()) {
            zipEntryName = zipEntryName.endsWith("/") ? zipEntryName : zipEntryName + "/";
            ZipEntry anEntry = new ZipEntry(zipEntryName);
            anEntry.setTime(srcFile.lastModified());
            zos.putNextEntry(anEntry);

            File[] dirList = srcFile.listFiles();
            for (File file : dirList) {
                internalZipFile(file, zos, base);
            }
        }
        else {
            ZipEntry anEntry = new ZipEntry(zipEntryName);
            anEntry.setTime(srcFile.lastModified());
            zos.putNextEntry(anEntry);

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(srcFile);
                IOUtils.copy(fis, zos);
                zos.closeEntry();
            }
            finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                }
                catch (IOException exc) {
                    // Do nothing
                }
            }
        }
    }
}
