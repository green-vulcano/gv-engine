/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 */
package it.greenvulcano.scheduler;

/**
 * @version 3.2.0 09/11/2011
 * @author GreenVulcano Developer Team
 */
public class TaskException extends Exception
{
    private static final long serialVersionUID = 4518242731391307497L;

    /**
     * Creates a new TaskException with the given message
     *
     * @param msg
     *        Message associated to the exception
     */
    public TaskException(String msg)
    {
        super(msg);
    }

    /**
     * Creates a new TaskException with a message and a cause.
     *
     * @param msg
     *        Message associated to the exception
     * @param exc
     *        Throwable that caused this exception to get thrown
     */
    public TaskException(String msg, Throwable exc)
    {
        super(msg, exc);
    }

    /**
     * Creates a new TaskException with a cause.
     *
     * @param exc
     *        Throwable that caused this exception to get thrown
     */
    public TaskException(Throwable exc)
    {
        super(exc);
    }

}