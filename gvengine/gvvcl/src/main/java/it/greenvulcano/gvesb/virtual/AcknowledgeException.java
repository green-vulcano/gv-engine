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
 * <code>AcknowledgeException</code> is the exception raised by GVVCL mechanisms
 * in case of errors happened performing acknowledgement of messages dequeued by
 * a <code>DequeueOperation</code>.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class AcknowledgeException extends VCLException
{
    /**
     *
     */
    private static final long serialVersionUID = 210L;

    /**
     * Creates a new AcknowledgeException.
     *
     * @param errorId
     *        error associated to the exception
     */
    public AcknowledgeException(String errorId)
    {
        super(errorId);
    }

    /**
     * Creates a new AcknowledgeException. Uses given parameters in order to
     * format the error code.
     *
     * @param errorId
     *        error associated to the exception
     * @param params
     *        parameters for the error message
     */
    public AcknowledgeException(String errorId, String[][] params)
    {
        super(errorId, params);
    }

    /**
     * Creates a new AcknowledgeException with a nested exception.
     *
     * @param errorId
     *        error associated to the exception
     * @param exc
     *        nested exception
     */
    public AcknowledgeException(String errorId, Throwable exc)
    {
        super(errorId, exc);
    }

    /**
     * Creates a new AcknowledgeException from with a nested exception. Uses
     * given parameters in order to format the error code.
     *
     * @param errorId
     *        error associated to the exception
     * @param params
     *        parameters for the error message
     * @param exc
     *        nested exception
     */
    public AcknowledgeException(String errorId, String[][] params, Throwable exc)
    {
        super(errorId, params, exc);
    }
}
