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
package it.greenvulcano.util.crypto;

import java.security.AlgorithmParameters;
import java.util.Base64;

/**
 * @version 3.3.0 20/lug/2012
 * @author GreenVulcano Developer Team
 */
public class AlgorithmParametersHolder
{
    private AlgorithmParameters params = null;

    public AlgorithmParameters getAlgorithmParameters()
    {
        return this.params;
    }

    public void setAlgorithmParameters(AlgorithmParameters params)
    {
        this.params = params;
    }

    public void setAlgorithmParameters(String type, String provider, byte[] params) throws CryptoUtilsException
    {
        this.params = createAlgorithmParameters(type, provider, params);
    }

    public String getAlgorithmParameters64() throws CryptoUtilsException
    {
        try {
            return new String(Base64.getEncoder().encode(this.params.getEncoded()));
        }
        catch (Exception exc) {
            throw new CryptoUtilsException("Error converting AlgorithmParameters", exc);
        }
    }

    public void setAlgorithmParameters(String type, String provider, String params) throws CryptoUtilsException
    {
        this.params = createAlgorithmParameters(type, provider, params);
    }

    public static AlgorithmParameters createAlgorithmParameters(String type, String provider, byte[] params)
            throws CryptoUtilsException
    {
        try {
            AlgorithmParameters ap = null;
            if (provider == null) {
                ap = AlgorithmParameters.getInstance(type);
            }
            else {
                ap = AlgorithmParameters.getInstance(type, provider);
            }
            ap.init(params);
            return ap;
        }
        catch (Exception exc) {
            throw new CryptoUtilsException("Error initializing AlgorithmParameters", exc);
        }
    }

    public static AlgorithmParameters createAlgorithmParameters(String type, String provider, String params)
            throws CryptoUtilsException
    {
        try {
            AlgorithmParameters ap = null;
            if (provider == null) {
                ap = AlgorithmParameters.getInstance(type);
            }
            else {
                ap = AlgorithmParameters.getInstance(type, provider);
            }
            ap.init(Base64.getDecoder().decode(params));
            return ap;
        }
        catch (Exception exc) {
            throw new CryptoUtilsException("Error initializing AlgorithmParameters", exc);
        }
    }


    public String getType()
    {
        return this.params.getAlgorithm();
    }

    public String getProvider()
    {
        return this.params.getProvider().getName();
    }
}
