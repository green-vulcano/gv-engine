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
package it.greenvulcano.util.file.monitor;

import it.greenvulcano.util.file.FileNameSorter;
import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.file.FileTimeSorter;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class BasicAnalysisReport implements AnalysisReport
{
    private static final String DATE_FORMAT       = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    private String              analysisDirectory = null;
    private String              analysisFilter    = null;
    private Date                analysisTime      = null;
    private String              sortMode          = null;
    private boolean             ascending         = true;
    private int                 existingFilesCount;
    private int                 createdFilesCount;
    private int                 modifiedFilesCount;
    private int                 deletedFilesCount;
    private Document            xmlReport         = null;
    private boolean             created           = false;

    public BasicAnalysisReport(String analysisDirectory, String analysisFilter, String sortMode, boolean ascending)
    {
        this.analysisDirectory = analysisDirectory;
        this.analysisFilter = analysisFilter;
        this.sortMode = sortMode;
        this.ascending = ascending;
    }

    public void addExistingFileSet(Set<FileProperties> fileSet) throws MonitorException
    {
        XMLUtils parser = null;
        try {
            if (fileSet.size() == 0) {
                return;
            }
            parser = XMLUtils.getParserInstance();
            xmlReport = initXMLReport(parser);
            Element root = xmlReport.getDocumentElement();

            // if already present, remove the list
            Node list = parser.selectSingleNode(root, "FileList[@type='existing']");
            if (list != null) {
                root.removeChild(list);
            }

            Element files = parser.insertElement(root, "FileList");
            parser.setAttribute(files, "type", "existing");

            addFiles(files, fileSet, parser);
            existingFilesCount = fileSet.size();
            created = true;
        }
        catch (MonitorException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new MonitorException("Error updating AnalysisReport", exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }

    }

    public void addCreatedFileSet(Set<FileProperties> fileSet) throws MonitorException
    {
        XMLUtils parser = null;
        try {
            if (fileSet.size() == 0) {
                return;
            }
            parser = XMLUtils.getParserInstance();
            xmlReport = initXMLReport(parser);
            Element root = xmlReport.getDocumentElement();

            // if already present, remove the list
            Node list = parser.selectSingleNode(root, "FileList[@type='created']");
            if (list != null) {
                root.removeChild(list);
            }

            Element files = parser.insertElement(root, "FileList");
            parser.setAttribute(files, "type", "created");

            addFiles(files, fileSet, parser);
            createdFilesCount = fileSet.size();
            created = true;
        }
        catch (MonitorException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new MonitorException("Error adding Created FileSet AnalysisReport", exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }

    }

    public void addModifiedFileSet(Set<FileProperties> fileSet) throws MonitorException
    {
        XMLUtils parser = null;
        try {
            if (fileSet.size() == 0) {
                return;
            }
            parser = XMLUtils.getParserInstance();
            xmlReport = initXMLReport(parser);
            Element root = xmlReport.getDocumentElement();

            // if already present, remove the list
            Node list = parser.selectSingleNode(root, "FileList[@type='modified']");
            if (list != null) {
                root.removeChild(list);
            }

            Element files = parser.insertElement(root, "FileList");
            parser.setAttribute(files, "type", "modified");

            addFiles(files, fileSet, parser);
            modifiedFilesCount = fileSet.size();
            created = true;
        }
        catch (MonitorException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new MonitorException("Error adding Modified FileSet AnalysisReport", exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    public void addDeletedFileSet(Set<FileProperties> fileSet) throws MonitorException
    {
        XMLUtils parser = null;
        try {
            if (fileSet.size() == 0) {
                return;
            }
            parser = XMLUtils.getParserInstance();
            xmlReport = initXMLReport(parser);
            Element root = xmlReport.getDocumentElement();

            // if already present, remove the list
            Node list = parser.selectSingleNode(root, "FileList[@type='deleted']");
            if (list != null) {
                root.removeChild(list);
            }

            Element files = parser.insertElement(root, "FileList");
            parser.setAttribute(files, "type", "deleted");

            addFiles(files, fileSet, parser);
            deletedFilesCount = fileSet.size();
            created = true;
        }
        catch (MonitorException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new MonitorException("Error adding Deleted FileSet AnalysisReport", exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }

    }


    /**
     * @param files
     * @param fileSet
     * @param parser
     * @throws MonitorException
     */
    private void addFiles(Element files, Set<FileProperties> fileSet, XMLUtils parser) throws MonitorException
    {
        try {
            List<FileProperties> fileList = new ArrayList<FileProperties>(fileSet);
            if (sortMode.equals(ORDER_BY_NAME)) {
                Collections.sort(fileList, new FileNameSorter(ascending));
            }
            else {
                Collections.sort(fileList, new FileTimeSorter(ascending));
            }

            for (FileProperties currFile : fileList) {
                Element fileElem = parser.insertElement(files, "File");
                fileElem.setAttribute("name", currFile.getName());
                fileElem.setAttribute("isDirectory", String.valueOf(currFile.isDirectory()));
                fileElem.setAttribute("size", String.valueOf(currFile.getLength()));
                if (currFile.getLastModified() > 0) {
                    fileElem.setAttribute("modified",
                            DateUtils.dateToString(new Date(currFile.getLastModified()), DATE_FORMAT));
                }
                fileElem.setAttribute("canRead", String.valueOf(currFile.canRead()));
                fileElem.setAttribute("canWrite", String.valueOf(currFile.canWrite()));
                fileElem.setAttribute("canExecute", String.valueOf(currFile.canExecute()));
            }
        }
        catch (Exception exc) {
            throw new MonitorException("Error writing FileProperties on AnalysisReport", exc);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.util.file.monitor.AnalysisReport#resultsAvailable()
     */
    @Override
    public boolean resultsAvailable()
    {
        return xmlReport != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.util.file.monitor.AnalysisReport#getAnalysisDirectory()
     */
    @Override
    public String getAnalysisDirectory()
    {
        return analysisDirectory;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.util.file.monitor.AnalysisReport#getAnalysisTime()
     */
    @Override
    public Date getAnalysisTime()
    {
        return analysisTime;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.util.file.monitor.AnalysisReport#getFilter()
     */
    @Override
    public String getFilter()
    {
        return analysisFilter;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.util.file.monitor.AnalysisReport#getExistingFilesCount()
     */
    @Override
    public int getExistingFilesCount()
    {
        return existingFilesCount;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.util.file.monitor.AnalysisReport#getCreatedFilesCount()
     */
    @Override
    public int getCreatedFilesCount()
    {
        return createdFilesCount;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.util.file.monitor.AnalysisReport#getModifiedFilesCount()
     */
    @Override
    public int getModifiedFilesCount()
    {
        return modifiedFilesCount;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.util.file.monitor.AnalysisReport#getDeletedFilesCount()
     */
    @Override
    public int getDeletedFilesCount()
    {
        return deletedFilesCount;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.util.file.monitor.AnalysisReport#toXML()
     */
    @Override
    public Document toXML() throws MonitorException
    {
        if (created) {
            return xmlReport;
        }
        throw new MonitorException("Analysis report NOT available");
    }

    @Override
    public String toString()
    {
        if (!created) {
            return "[NO REPORT AVAILABLE]";
        }
        StringBuilder buf = new StringBuilder("[START REPORT]\n");
        buf.append("\tAnalysis Directory = ").append(analysisDirectory).append('\n');
        if (existingFilesCount != -1) {
            buf.append("\tExisting files = ").append(existingFilesCount).append('\n');
        }
        if (createdFilesCount != -1) {
            buf.append("\tCreated  files = ").append(createdFilesCount).append('\n');
        }
        if (modifiedFilesCount != -1) {
            buf.append("\tModified files = ").append(modifiedFilesCount).append('\n');
        }
        if (deletedFilesCount != -1) {
            buf.append("\tDeleted  files = ").append(deletedFilesCount).append('\n');
        }
        buf.append("[END REPORT]\n");
        return buf.toString();
    }

    private Document initXMLReport(XMLUtils parser) throws MonitorException
    {
        if (xmlReport != null) {
            return xmlReport;
        }
        try {
            analysisTime = DateUtils.createCalendar().getTime();
            xmlReport = parser.newDocument("AnalysisReport");
            Element root = xmlReport.getDocumentElement();
            parser.setAttribute(root, "created", DateUtils.dateToString(analysisTime, DATE_FORMAT));

            Element dir = parser.insertElement(root, "AnalysisDirectory");
            parser.setAttribute(dir, "path", analysisDirectory);
            Element f = parser.insertElement(root, "AnalysisFilter");
            parser.setAttribute(f, "filter", analysisFilter);

            return xmlReport;
        }
        catch (Exception exc) {
            throw new MonitorException("Error initializing AnalysisReport", exc);
        }
    }
}
