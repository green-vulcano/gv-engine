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
package it.greenvulcano.gvesb.gvdte.transformers.bin.utils;

/**
 * This static class contains several string constants used within binary
 * buffers configuration
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class FieldConstants
{
    /**
     *
     */
    public final static String BUFFER_FIELD_FIELDTYPE_SINGLE   = "Single";
    /**
     *
     */
    public final static String BUFFER_FIELD_FIELDTYPE_SEQUENCE = "Sequence";
    /**
     *
     */
    public final static String BUFFER_FIELD_ELEMTYPE_ELEMENT   = "Element";
    /**
     *
     */
    public final static String BUFFER_FIELD_ELEMTYPE_CDATA     = "CDATA";
    /**
     *
     */
    public final static String BUFFER_FIELD_ELEMTYPE_TEXT      = "Text";

    /**
     *
     */
    public final static byte   STRING_FIELD                    = 0;
    /**
     *
     */
    public final static byte   NUMERIC_FIELD                   = 1;
    /**
     *
     */
    public final static byte   RAWDATA_FIELD                   = 2;

    /**
     *
     */
    public final static byte   SINGLE_FIELD                    = 3;
    /**
     *
     */
    public final static byte   SEQUENCE_FIELD                  = 4;

}
