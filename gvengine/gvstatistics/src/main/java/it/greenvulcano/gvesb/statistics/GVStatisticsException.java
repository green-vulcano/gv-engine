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
package it.greenvulcano.gvesb.statistics;

import it.greenvulcano.gvesb.internal.GVInternalException;

/**
 * <code>GVStatisticsException</code> is the exception raised by the statistics
 * mechanisms in case of errors.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVStatisticsException extends GVInternalException
{
    /**
     *
     */
    private static final long serialVersionUID = 69229345673263283L;

    /**
     *
     * @param idMessage
     *        message associated to an error in the error catalog
     */
    public GVStatisticsException(String idMessage)
    {
        super(idMessage);
    }

    /**
     *
     * @param idMessage
     *        message associated to an error in the error catalog
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     */
    public GVStatisticsException(String idMessage, String[][] params)
    {
        super(idMessage, params);
    }

    /**
     *
     * @param idMessage
     *        message associated to the exception
     * @param cause
     *        Throwable that caused this exception to get thrown
     */
    public GVStatisticsException(String idMessage, Throwable cause)
    {
        super(idMessage, cause);
    }

    /**
     *
     * @param idMessage
     *        message associated to the exception
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     * @param cause
     *        Throwable that caused this exception to get thrown
     */
    public GVStatisticsException(String idMessage, String[][] params, Throwable cause)
    {
        super(idMessage, params, cause);
    }
}
