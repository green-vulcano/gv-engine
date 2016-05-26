/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.utils;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.util.bin.Dump;
import it.greenvulcano.util.xml.XMLUtils;

import org.w3c.dom.Node;

/**
 * This class access to GVBuffer object and get the field value for the given
 * name field
 * 
 * @version 3.3.0 20/lug/2012
 * @author GreenVulcano Developer Team
 */
public class GVBufferAccess
{

    /**
     * GVBuffer Object.
     */
    private GVBuffer gvBuffer = null;

    // ----------------------------------------------------------------------------
    // METHOD
    // ----------------------------------------------------------------------------

    /**
     * Initialize the GVBuffer class object.
     * 
     * @param gvBuffer
     *        gvBuffer object
     */
    public GVBufferAccess(GVBuffer gvBuffer)
    {
        this.gvBuffer = gvBuffer;
    }

    /**
     * This method get the GVBuffer field value getting the fieldName in input
     * All
     * values are String.
     * 
     * @param fieldName
     *        the GVBuffer field to get Value
     * @return retValue the GVBuffer field value
     */
    public String getFieldAsString(String fieldName)
    {
        String retValue = "";

        if (isGVBufferField(fieldName)) {
            // OBJECT
            //
            if (fieldName.equals("GVBuffer.object")) {
                Object buffer = getFieldAsObject(fieldName);
                if (buffer instanceof byte[]) {
                    Dump dump = new Dump((byte[]) buffer, Dump.UNBOUNDED);
                    retValue = dump.toString();
                }
                else if (buffer instanceof Node) {
                    try {
                        retValue = XMLUtils.serializeDOM_S((Node) buffer);
                    }
                    catch (Exception exc) {
                        retValue = ("[DUMP ERROR!!!!!].");
                    }
                }
                else {
                    if (buffer == null) {
                        retValue = "";
                    }
                    else {
                        retValue = "" + buffer;
                    }
                }
            }
            else {
                Object oValue = getFieldAsObject(fieldName);
                if (oValue == null) {
                    retValue = "";
                }
                else {
                    retValue = "" + oValue;
                }
            }
        }
        else {
            retValue = "NoValid";
        }

        return retValue;
    }

    /**
     * @see it.greenvulcano.gvesb.utils.AccessInterface#getFieldAsObject(java.lang.String)
     */
    public Object getFieldAsObject(String fieldName)
    {
        Object retValue = null;

        if (isGVBufferField(fieldName)) {
            // SYSTEM
            //
            if (fieldName.equals("GVBuffer.system")) {
                retValue = gvBuffer.getSystem();
            }
            // SERVICE
            //
            else if (fieldName.equals("GVBuffer.service")) {
                retValue = gvBuffer.getService();
            }
            // ID
            //
            else if (fieldName.equals("GVBuffer.id")) {
                retValue = gvBuffer.getId().toString();
            }
            // OBJECT
            //
            else if (fieldName.equals("GVBuffer.object")) {
                retValue = gvBuffer.getObject();
            }
            // RET_CODE
            //
            else if (fieldName.equals("GVBuffer.retCode")) {
                retValue = new Integer(gvBuffer.getRetCode());
            }
            // PROPERTIES
            else if (fieldName.equals("GVBuffer.properties")) {
                // Tutti le properties del GVBuffer
                //
                StringBuffer buffer = new StringBuffer();
                String[] properties = gvBuffer.getPropertyNames();
                if (properties != null) {
                    for (int i = 0; i < properties.length; i++) {
                        String value = gvBuffer.getProperty(properties[i]);
                        if (i != 0) {
                            buffer.append("\n");
                        }
                        buffer.append(fill(properties[i])).append(": '").append(value).append("'");
                    }
                }

                retValue = buffer.toString();
            }
            // separated PROPERTIES Value
            else if (fieldName.equals("GVBuffer.separatedproperties")) {
                // All GVBuffer properties
                //
                String[] properties = gvBuffer.getPropertyNames();
                retValue = new Object[properties.length];
                for (int i = 0; i < properties.length; i++) {
                    ((Object[]) retValue)[i] = gvBuffer.getProperty(properties[i]);
                }
            }
            else {
                // Nome della properties da leggere
                //
                String property = getGVBufferPropertyName(fieldName);
                if (property != null) {
                    retValue = gvBuffer.getProperty(property);
                }
            }
        }
        else {
            retValue = "NoValid";
        }

        return retValue;
    }

    /**
     * /** The string to be used as fill pattern.
     */
    protected static final String FILLSTRING = "..............................";

    /**
     * 
     * @param str
     *        the string to normalize length
     * @return the normalized string
     */
    protected String fill(String str)
    {
        if (str.length() >= FILLSTRING.length()) {
            return str;
        }
        return (str + FILLSTRING).substring(0, FILLSTRING.length());
    }

    /**
     * Set a GVBuffer field value.
     * 
     * @param fieldName
     *        the field name
     * @param value
     *        the field value
     * @throws GVException
     *         if error occurs
     */
    public void setField(String fieldName, Object value) throws GVException
    {
        if (isGVBufferField(fieldName)) {
            // SYSTEM
            //
            if (fieldName.equals("GVBuffer.system")) {
                gvBuffer.setSystem(value.toString());
            }
            // SERVICE
            //
            else if (fieldName.equals("GVBuffer.service")) {
                gvBuffer.setService(value.toString());
            }
            // ID
            //
            else if (fieldName.equals("GVBuffer.id")) {
                if (value != null && !"".equals(value)) {
                    gvBuffer.setId(new Id((value.toString()).trim()));
                }
            }
            // OBJECT
            //
            else if (fieldName.equals("GVBuffer.object")) {
                gvBuffer.setObject(value);
            }
            // RET_CODE
            //
            else if (fieldName.equals("GVBuffer.retCode")) {
                gvBuffer.setRetCode(Integer.parseInt((value.toString()).trim()));
            }
            // PROPERTY
            else {
                // fieldName = Nome della property da settare
                //
                if (fieldName.startsWith("GVBuffer.property:")) {
                    String extField = fieldName.substring(fieldName.indexOf(":") + 1);
                    gvBuffer.setProperty(extField, value.toString());
                }
            }
        }
        else {
            throw new IllegalArgumentException("The field name '" + fieldName
                    + "' passed to GVBufferAccess.setField method is not valid.");
        }
    }

    /**
     * Check if the field is a GVBuffer field.
     * 
     * @param fieldName
     *        field name to check
     * @return the check result
     */
    public static boolean isGVBufferField(String fieldName)
    {
        if (fieldName == null) {
            return false;
        }
        return fieldName.startsWith("GVBuffer.");
    }

    /**
     * Check if the field is a GVBuffer property.
     * 
     * @param fieldName
     *        field name to check
     * @return the check result
     */
    public static boolean isGVBufferProperty(String fieldName)
    {
        return fieldName.startsWith("GVBuffer.property:");
    }

    /**
     * Return the GVBuffer property name.
     * 
     * @param fieldName
     *        field name to parse
     * @return the field name
     */
    public static String getGVBufferPropertyName(String fieldName)
    {
        String propertyName = null;
        if (isGVBufferProperty(fieldName)) {
            int idx = fieldName.indexOf(":");
            propertyName = fieldName.substring(idx + 1);
        }
        return propertyName;
    }

    /**
     * @return Returns the gvBuffer.
     */
    public GVBuffer getGVBuffer()
    {
        return gvBuffer;
    }
}
