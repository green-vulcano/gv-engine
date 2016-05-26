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
package it.greenvulcano.gvesb.buffer;

/**
 * <code>GVPublicException</code> is the only exception propagated at clients of the
 * GreenVulcano core.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public final class GVPublicException extends GVException
{
    private static final long serialVersionUID = -3013079638918069021L;

    /**
     * Creates a new GVPublicException from an GVErrorId.
     *
     * @param idMessage
     *        error associated to the exception
     */
    public GVPublicException(String idMessage)
    {
        super(idMessage);
    }

    /**
     * Creates a new GVPublicException from an GVErrorId.
     *
     * @param idMessage
     *        error associated to the exception
     * @param params
     *        Parameters for the error message
     */
    public GVPublicException(String idMessage, String[][] params)
    {
        super(idMessage, params);
    }
}
