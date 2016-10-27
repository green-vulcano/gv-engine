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
package it.greenvulcano.gvesb.virtual.j2ee;

import it.greenvulcano.gvesb.virtual.CallException;

/**
 * <code>J2EECallException</code> is the exception raised by the J2EE GVVL
 * implementation during calls.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public class J2EECallException extends CallException
{
    private static final long serialVersionUID = 210L;

    /**
     * Creates a new J2EECallException.
     *
     * @param id
     *        error associated to the exception
     */
    public J2EECallException(String id)
    {
        super(id);
    }

    /**
     * Creates a new J2EECallException. Uses given parameters in order to format
     * the error code.
     *
     * @param id
     *        error associated to the exception
     * @param parameter
     *        parameters for the error message
     */
    public J2EECallException(String id, String[][] parameter)
    {
        super(id, parameter);
    }

    /**
     * Creates a new J2EECallException with a nested exception.
     *
     * @param id
     *        error associated to the exception
     * @param exc
     *        nested exception
     */
    public J2EECallException(String id, Throwable exc)
    {
        super(id, exc);
    }

    /**
     * Creates a new J2EECallException from with a nested exception. Uses given
     * parameters in order to format the error code.
     *
     * @param id
     *        error associated to the exception
     * @param parameter
     *        parameters for the error message
     * @param exc
     *        nested exception
     */
    public J2EECallException(String id, String[][] parameter, Throwable exc)
    {
        super(id, parameter, exc);
    }
}
