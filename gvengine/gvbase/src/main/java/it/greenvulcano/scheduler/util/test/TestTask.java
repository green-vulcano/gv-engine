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
package it.greenvulcano.scheduler.util.test;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.scheduler.Task;
import it.greenvulcano.scheduler.TaskException;
import it.greenvulcano.util.txt.DateUtils;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * @version 3.2.0 09/11/2011
 * @author GreenVulcano Developer Team
 */
public class TestTask extends Task
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(TestTask.class);
    private String        message = "";
    private long          sleep   = 0;

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.scheduler.Task#getLogger()
     */
    /**
     * @return
     */
    @Override
    protected Logger getLogger()
    {
        return logger;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.scheduler.Task#initTask(org.w3c.dom.Node)
     */
    @Override
    protected void initTask(Node node) throws TaskException
    {
        message = XMLConfig.get(node, "@message", "");
        sleep = XMLConfig.getLong(node, "@sleep", 0);
        System.out.println("-----INITIALIZING (" + getFullName() + ")");
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.scheduler.Task#executeTask(java.lang.String, Date, java.util.Map<java.lang.String, java.lang.String>, booolean)
     */
    @Override
    protected boolean executeTask(String name, Date fireTime, Map<String, String> locProperties, boolean isLast)
    {
        System.out.println("-----BEGIN (" + getFullName() + ")-(" + name + ") - ("
                + DateUtils.nowToString(DateUtils.DEFAULT_FORMAT_TIMESTAMP) + ") - " + Thread.currentThread());
        System.out.println("Timestamp: " + DateUtils.nowToString(DateUtils.DEFAULT_FORMAT_DATETIME) + " - isLast "
                + isLast + " - " + message + " - properties: " + locProperties);
        try {
            Thread.sleep(sleep);
        }
        catch (Exception exc) {
            // do nothing
        }
        System.out.println("-----END   (" + getFullName() + ")-(" + name + ") - ("
                + DateUtils.nowToString(DateUtils.DEFAULT_FORMAT_TIMESTAMP) + ") - " + Thread.currentThread());
        
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.scheduler.Task#destroyTask()
     */
    @Override
    protected void destroyTask()
    {
        System.out.println("-----DESTROING (" + getFullName() + ")");
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.scheduler.Task#sendHeartBeat()
     */
    @Override
    protected boolean sendHeartBeat()
    {
        return true;
    }
}
