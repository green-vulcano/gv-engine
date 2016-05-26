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
package it.greenvulcano.gvesb.security.callers;

import javax.servlet.http.HttpServletRequest;

/**
 * ServletCaller class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 **/
public class ServletCaller implements Caller
{

    /**
     * HttpServletRequest.
     */
    private HttpServletRequest httpServletRequest = null;

    /**
     * Constructor with parameter.
     *
     * @param httpServletRequest
     *        HttpServletRequest
     */
    public ServletCaller(HttpServletRequest httpServletRequest)
    {
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * @see it.greenvulcano.gvesb.security.callers.Caller#getCallerName()
     */
    public String getCallerName()
    {
        return httpServletRequest.getRemoteUser();
    }

    /**
     * Returns a boolean indicating whether the authenticated user is included
     * in the specified logical "role".
     *
     * @param role
     * @return a boolean indicating whether the authenticated user is included
     *         in the specified logical "role".
     */
    public boolean isCallerInRole(String role)
    {
        return httpServletRequest.isUserInRole(role);
    }

    /**
     * Returns a boolean indicating whether this request was made using a secure
     * channel, such as HTTPS.
     *
     * @return a boolean indicating whether this request was made using a secure
     *         channel, such as HTTPS.
     */
    public boolean isSecure()
    {
        return httpServletRequest.isSecure();
    }
}
