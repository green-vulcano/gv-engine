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

/**
 * This object has the possible oracle type.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ParameterType
{
    /**
     * The string
     */
    public static final String DB_STRING       = "string";

    /**
     * The integer
     */
    public static final String DB_INT          = "int";

    /**
     * The date
     */
    public static final String DB_DATE         = "date";

    /**
     * The long
     */
    public static final String DB_LONG         = "long";

    /**
     * The float
     */
    public static final String DB_FLOAT        = "float";

    /**
     * The raw
     */
    public static final String DB_LONG_RAW     = "binary";

    /**
     * The blob
     */
    public static final String DB_BLOB         = "blob";

    /**
     * The clob
     */
    public static final String DB_CLOB         = "clob";

    /**
     * The java integer
     */
    public static final String JAVA_INT        = "int";

    /**
     * The java long
     */
    public static final String JAVA_LONG       = "long";

    /**
     * The java string
     */
    public static final String JAVA_STRING     = "string";

    /**
     * The java byte array
     */
    public static final String JAVA_BYTE_ARRAY = "byte_array";

    /**
     * The other type
     */
    public static final String OTHER           = "other";
}
