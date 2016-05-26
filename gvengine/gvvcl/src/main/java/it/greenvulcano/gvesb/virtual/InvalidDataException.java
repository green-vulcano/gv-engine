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
package it.greenvulcano.gvesb.virtual;

/**
 * <code>InvalidDataException</code> is the exception raised by GVVCL mechanisms
 * in case of errors caused by wrong data. <br/>
 * GreenVulcano ESB should not retry to perform operation with the same data.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class InvalidDataException extends VCLException
{
    private static final long serialVersionUID = 210L;

    /**
     * Creates a new InvalidDataException.
     *
     * @param errorId
     *        error associated to the exception
     */
    public InvalidDataException(String errorId)
    {
        super(errorId);
    }

    /**
     * Creates a new InvalidDataException. Uses given parameters in order to
     * format the error code.
     *
     * @param errorId
     *        error associated to the exception
     * @param params
     *        parameters for the error message
     */
    public InvalidDataException(String errorId, String[][] params)
    {
        super(errorId, params);
    }

    /**
     * Creates a new InvalidDataException with a nested exception.
     *
     * @param errorId
     *        error associated to the exception
     * @param exc
     *        nested exception
     */
    public InvalidDataException(String errorId, Throwable exc)
    {
        super(errorId, exc);
    }

    /**
     * Creates a new InvalidDataException from with a nested exception. Uses
     * given parameters in order to format the error code.
     *
     * @param errorId
     *        error associated to the exception
     * @param params
     *        parameters for the error message
     * @param exc
     *        nested exception
     */
    public InvalidDataException(String errorId, String[][] params, Throwable exc)
    {
        super(errorId, params, exc);
    }
}
