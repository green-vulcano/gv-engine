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

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Random;

/**
 * This class encapsulate a transaction Id.
 * <p>
 *
 * The Id is an opaque data of 24 hexadecimal digits, used to identify a
 * particular data flow into GreenVulcano. Requests and responses will match
 * using Id.<br>
 * Each pair request/response, from a given service, must have a Id different
 * from others.
 * <p>
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class Id implements Serializable
{
    private static final long serialVersionUID = 4635958004002609200L;

    static {
        calculateIP();
    }

    transient private static final String sValidChar       = "0123456789ABCDEF";

    private String                        idStr            = null;

    /**
     * Initialize this object with 24 random HEX-adecimal characters.
     */
    public Id()
    {
        idStr = getId();
    }

    /**
     * Initialize this object with the given String. The string is checked for
     * correctness.
     *
     * @param id
     *        24 hexadecimal uppercase characters.
     *
     * @exception GVException
     */
    public Id(String id) throws GVException
    {
        int len = id.length();
        if (len != 24) {
            throw new GVException("INVALID_ID_FORMAT", new String[][]{{"Id", id}});
        }

        for (int i = 0; i < len; i++) {
            if (sValidChar.indexOf(id.charAt(i)) == -1) {
                throw new GVException("INVALID_ID_FORMAT", new String[][]{{"Id", id}});
            }
        }
        idStr = id;
    }

    /**
     * Copy constructor. If it is null, then the new object is initialized with
     * a new Id.
     *
     * @param id
     *        Id to copy.
     */
    public Id(Id id)
    {
        if (id == null) {
            idStr = getId();
        }
        else {
            idStr = id.idStr;
        }
    }

    /**
     * Check for equality with an Object.
     */
    @Override
    public boolean equals(Object id)
    {
        if (id == null) {
            return false;
        }
        if (id == this) {
            return true;
        }
        if (id instanceof Id) {
            return idStr.equals(((Id) id).idStr);
        }
        return false;
    }

    /**
     * Check for equality with a Id.
     *
     * @param id
     * @return if true the given id is equal to this id
     */
    public boolean equals(Id id)
    {
        if (id == null) {
            return false;
        }
        if (id == this) {
            return true;
        }
        return idStr.equals(id.idStr);
    }

    /**
     * Convert this Id to a String.
     */
    @Override
    public String toString()
    {
        return idStr;
    }

    /**
     * Increased each time a Id is calculated.
     */
    transient private static int          counter = 0;

    /**
     * Used to calculate random component.
     */
    transient private static Random       random  = new Random();

    /**
     * Contains the IP component. Calculated only once.
     */
    transient private static String       sIP;

    /**
     * Used in toHexString().
     *
     * @see #toHexString(int)
     */
    transient private static final String fill    = "00000000";

    /**
     * Formats an integer to a String, left filled with 0.
     * The String have a length of 8 hex digits.
     */
    private static String toHexString(int number)
    {
        String hex = Integer.toHexString(number).toUpperCase();
        return fill.substring(hex.length()) + hex;
    }

    /**
     * Calculate the Id.
     *
     * @see it.greenvulcano.gvesb.buffer.Id
     */
    private static synchronized String getId()
    {
        int rand = random.nextInt(0xFFFF) + 1; // excludes 0

        counter = (counter + 1) & 0xFFFF;
        if (counter == 0) {
            counter = 1;
        }
        String sCounterAndRandom = toHexString((counter << 16) | rand);

        int time = (int) (System.currentTimeMillis() / 1000);
        String sTimeStamp = toHexString(time);

        return sIP + sTimeStamp + sCounterAndRandom;
    }

    /**
     * Calculate the IP. Select the first IP address available that is
     * not localhost IP or multicast IP.
     */
    private static void calculateIP()
    {
        // Default value
        sIP = "7F000001";

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            InetAddress[] allInetAddress = InetAddress.getAllByName(localHost.getHostName());
            if (allInetAddress != null) {
                boolean found = false;

                for (int i = 0; (i < allInetAddress.length) && !found; ++i) {
                    String sAddress = allInetAddress[i].getHostAddress();
                    if (!sAddress.equals("127.0.0.1") && !allInetAddress[i].isMulticastAddress()) {
                        sIP = addressToString(allInetAddress[i]);
                        found = true;
                    }
                }
            }
        }
        catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Set the sIP field.
     */
    private static String addressToString(InetAddress inetAddress)
    {
        byte[] buff = inetAddress.getAddress();
        int address = 0;
        for (int i = 0; i < buff.length; ++i) {
            address <<= 8;
            address |= (buff[i] & 0xFF);
        }
        return toHexString(address);
    }
}
