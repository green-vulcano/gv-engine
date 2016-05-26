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
package it.greenvulcano.event.util.debug;

import it.greenvulcano.event.interfaces.Event;
import it.greenvulcano.event.util.DefaultEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DebuggerEventListener class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DebuggerEventListener implements DefaultEventListener
{
    /**
     * Date formatter.
     */
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.S");

    /**
     * @param event
     * @see it.greenvulcano.event.util.DefaultEventListener#onEvent(Event)
     */
    public void onEvent(Event event)
    {
        System.out.println("------> Event Begin - " + sdf.format(new Date()));
        System.out.println("---> Type        : " + event.getType());
        System.out.println("---> Name        : " + event.getName());
        System.out.println("---> Code        : " + event.getCode());
        System.out.println("---> Source      : " + event.getSource());
        System.out.println("---> Description : " + event.getDescription());
        System.out.println("------> Event End");
    }

}
