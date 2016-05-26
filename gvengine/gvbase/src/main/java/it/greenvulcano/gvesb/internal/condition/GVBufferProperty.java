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
package it.greenvulcano.gvesb.internal.condition;

import it.greenvulcano.gvesb.buffer.GVBuffer;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public interface GVBufferProperty
{
    public static int GVB_PROP_PRESENT       = -1;
    public static int GVB_PROP_EQUAL         = 0;
    public static int GVB_PROP_LESSER        = 1;
    public static int GVB_PROP_LESSER_EQUAL  = 2;
    public static int GVB_PROP_GREATER       = 3;
    public static int GVB_PROP_GREATER_EQUAL = 4;
    public static int GVB_PROP_DIFFERENT     = 5;

    /**
     * @return if this is a group of property
     */
    boolean isGroup();

    /**
     * @return the group properties
     */
    String getGroup();

    /**
     * @param gvBuffer
     * @return if condition evaluates to true
     */
    boolean check(GVBuffer gvBuffer);
}
