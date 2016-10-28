/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
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
 *******************************************************************************/
package it.greenvulcano.gvesb.virtual.ws.utils;

import org.slf4j.Logger;


/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class NormalizeUtil
{
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(NormalizeUtil.class);

    /**
     *
     * @param baseURI
     * @param schemaLocation
     * @return the normalized URI
     */
    public static String normalizeUri(String baseURI, String schemaLocation)
    {
        String normalizedURI;

        if (baseURI == null) {
            normalizedURI = schemaLocation;
        }
        else {
            if ((schemaLocation != null) && (schemaLocation.length() > 0)) {
                if (schemaLocation.startsWith("http") || schemaLocation.startsWith("file")) {
                    normalizedURI = schemaLocation;
                }
                else {
                    int counter = -1;
                    int idx = counter = -1;

                    do {
                        counter++;
                        idx = schemaLocation.indexOf("../", idx + 1);
                    }
                    while (idx != -1);

                    idx = baseURI.length();

                    for (; counter >= 0; counter--) {
                        idx = baseURI.lastIndexOf("/", idx - 1);
                    }

                    if (idx == -1) {
                        idx = 0;
                    }

                    logger.debug("schemaLocation: " + schemaLocation.lastIndexOf("./"));
                    int x = schemaLocation.lastIndexOf("./");

                    if (x != -1) {
                        normalizedURI = baseURI.substring(0, idx) + schemaLocation.substring(x + 1);
                    }
                    else {
                        if (schemaLocation.startsWith("/")) {
                            normalizedURI = baseURI.substring(0, idx) + schemaLocation;
                        }
                        else {
                            normalizedURI = baseURI.substring(0, idx + 1) + schemaLocation;
                        }
                    }
                }
            }
            else {
                normalizedURI = baseURI;
            }
        }

        return normalizedURI;
    }
}
