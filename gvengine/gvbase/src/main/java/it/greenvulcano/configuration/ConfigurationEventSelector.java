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
package it.greenvulcano.configuration;

import it.greenvulcano.event.interfaces.Event;
import it.greenvulcano.event.interfaces.EventSelector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * ConfigurationEventSelector class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ConfigurationEventSelector implements EventSelector
{
    /**
     * The file list on which filter configuration events.
     */
    HashSet<String> filesSet = new HashSet<String>();

    /**
     * Constructor.
     */
    public ConfigurationEventSelector()
    {
        // do nothing
    }

    /**
     * Constructor.
     *
     * @param file
     *        the file on which filter events
     */
    public ConfigurationEventSelector(String file)
    {
        addFile(file);
    }

    /**
     * Constructor.
     *
     * @param files
     *        the file list on which filter events
     */
    public ConfigurationEventSelector(List<String> files)
    {
        addFiles(files);
    }

    /**
     * Add a file to the list.
     *
     * @param file
     *        the file to add
     */
    public final void addFile(String file)
    {
        filesSet.add(file);
    }

    /**
     * Remove a file from the list.
     *
     * @param file
     *        the file to remove
     */
    public final void removeFile(String file)
    {
        filesSet.remove(file);
    }

    /**
     * Add a list of files to the list.
     *
     * @param files
     *        the list of files to add
     */
    public final void addFiles(List<String> files)
    {
        for (String file : files) {
            filesSet.add(file);
        }
    }

    /**
     * Remove a list of files to the list.
     *
     * @param files
     *        the files to remove
     */
    public final void removeFiles(List<String> files)
    {
        for (String file : files) {
            filesSet.remove(file);
        }
    }

    /**
     * @see it.greenvulcano.event.interfaces.EventSelector#select(it.greenvulcano.event.interfaces.Event)
     */
    public final boolean select(Event event)
    {
        boolean selected = false;

        if (event instanceof ConfigurationEvent) {
            if (filesSet.isEmpty()) {
                selected = true;
            }
            else {
                String file = ((ConfigurationEvent) event).getFile();

                Iterator<String> i = filesSet.iterator();
                while (i.hasNext() && !selected) {
                    selected = file.equals(i.next());
                }
            }
        }
        return selected;
    }

    /**
     * Implements equality check.
     *
     * @param obj
     *        the object to compare
     * @return true if the instances are equals
     */
    @Override
    public final boolean equals(Object obj)
    {
        if (obj instanceof ConfigurationEventSelector) {
            return filesSet.equals(((ConfigurationEventSelector) obj).filesSet);
        }
        return false;
    }

    /**
     * Implements hash code generation algorithm.
     *
     * @return the hash code
     */
    @Override
    public final int hashCode()
    {
        return filesSet.hashCode();
    }
}
