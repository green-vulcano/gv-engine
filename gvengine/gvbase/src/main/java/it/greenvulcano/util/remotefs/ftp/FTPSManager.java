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
package it.greenvulcano.util.remotefs.ftp;

import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.remotefs.RemoteManagerException;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

/**
 * @version 3.0.0 Apr 24, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 * 
 */
public class FTPSManager extends FTPManager
{

    public FTPSManager() throws RemoteManagerException
    {
        super();
    }

    /**
     * Create a new FTPSClient object
     * 
     * @see it.greenvulcano.util.remotefs.ftp.FTPManager#createFTPClient()
     */
    @Override
    protected FTPClient createFTPClient() throws RemoteManagerException
    {
        try {
            return new FTPSClient();
        }
        catch (NoSuchAlgorithmException exc) {
            throw new RemoteManagerException("Error instantiating FTPSClient", exc);
        }
    }

    /*
     * (non-Javadoc)
     * @see it.greenvulcano.util.remotefs.RemoteManager#getManagerKey()
     */
    @Override
    public String getManagerKey()
    {
        return "ftps://" + username + "@" + hostname + ":" + port;
    }

    @Override
    public String getManagerKey(Map<String, String> optProperties) throws RemoteManagerException
    {
        try {
            Map<String, Object> localProps = MapUtils.convertToHMStringObject(optProperties);
            return "ftps://" + PropertiesHandler.expand(username, localProps) + "@"
                    + PropertiesHandler.expand(hostname, localProps) + ":" + port;
        }
        catch (Exception exc) {
            throw new RemoteManagerException(exc);
        }
    }
}
