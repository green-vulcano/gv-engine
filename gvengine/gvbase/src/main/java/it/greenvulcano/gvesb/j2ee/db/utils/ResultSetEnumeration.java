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
package it.greenvulcano.gvesb.j2ee.db.utils;

import java.sql.ResultSet;

/**
 *
 * ResultSetEnumeration enumeration
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public enum ResultSetEnumeration {

    /**
     * @see ResultSet#TYPE_FORWARD_ONLY
     */
    TYPE_FORWARD_ONLY_ID(ResultSet.TYPE_FORWARD_ONLY),
    /**
     * @see ResultSet#TYPE_SCROLL_INSENSITIVE
     */
    TYPE_SCROLL_INSENSITIVE_ID(ResultSet.TYPE_SCROLL_INSENSITIVE),
    /**
     * @see ResultSet#TYPE_SCROLL_SENSITIVE
     */
    TYPE_SCROLL_SENSITIVE_ID(ResultSet.TYPE_SCROLL_SENSITIVE),
    /**
     * @see ResultSet#CONCUR_READ_ONLY
     */
    CONCUR_READ_ONLY_ID(ResultSet.CONCUR_READ_ONLY),
    /**
     * @see ResultSet#CONCUR_UPDATABLE
     */
    CONCUR_UPDATABLE_ID(ResultSet.CONCUR_UPDATABLE),
    /**
     * @see ResultSet#CLOSE_CURSORS_AT_COMMIT
     */
    CLOSE_CURSOR_AT_COMMIT_ID(ResultSet.CLOSE_CURSORS_AT_COMMIT),
    /**
     * @see ResultSet#HOLD_CURSORS_OVER_COMMIT
     */
    HOLD_CURSOR_OVER_COMMIT_ID(ResultSet.HOLD_CURSORS_OVER_COMMIT);

    private int id;

    private ResultSetEnumeration(int id)
    {
        this.id = id;
    }

    /**
     * @return the corresponding value
     */
    public int getId()
    {
        return id;
    }
}
