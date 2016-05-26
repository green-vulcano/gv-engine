/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.greenvulcano.event.internal.shutdown;

import it.greenvulcano.event.EventHandler;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.util.thread.BaseThread;

/**
 *
 * ShutdownHandler class
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
*/
public class ShutdownHandler extends BaseThread {
    /**
     * Sleep timeout.
     */
    private static final long SLEEP         = 5000;
    /**
     * The shutdown event to fire.
     */
    private ShutdownEvent     shutdownEvent = new ShutdownEvent(this);

    public ShutdownHandler()
    {
        super("ShutdownHandler");
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public final void run() {
        try {
            EventHandler.fireEvent("shutdownStarted", shutdownEvent);
            try {
                Thread.sleep(SLEEP);
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        finally {
            EventHandler.getEventLauncher().stopThread();
        }
    }
}
