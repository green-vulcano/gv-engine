/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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

package tests.unit.gvesb.gvcore.jmx;

import java.util.Set;

import javax.management.Notification;
import javax.management.ObjectName;

import org.junit.Ignore;

/**
 * @version 3.3.0 Sep 21, 2012
 * @author GreenVulcano Developer Team
 * 
 */
@Ignore
public class Test implements TestMBean
{

    private Set<String> result = null;

    /**
     * @param result
     */
    public Test(Set<String> result)
    {
        this.result = result;
    }

    /**
     * @see tests.unit.gvesb.gvcore.jmx.TestMBean#sendJMXNotification(javax.management.Notification,
     *      javax.management.ObjectName)
     */
    @Override
    public void sendJMXNotification(Notification notification, ObjectName oName)
    {
        result.add(notification.getMessage());
    }

}
