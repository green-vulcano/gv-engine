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
package it.greenvulcano.scheduler.util.quartz.impl;

import it.greenvulcano.gvesb.j2ee.db.connections.impl.ConnectionBuilder;

import java.sql.Connection;
import java.sql.SQLException;

import org.quartz.utils.ConnectionProvider;

/**
 * @version 3.2.0 20/nov/2011
 * @author GreenVulcano Developer Team
 */
public class GVQuartzConnectionProvider implements ConnectionProvider
{
    private ConnectionBuilder cBuilder = null;

    public GVQuartzConnectionProvider(ConnectionBuilder cBuilder)
    {
        this.cBuilder = cBuilder;
    }

    /* (non-Javadoc)
     * @see org.quartz.utils.ConnectionProvider#getConnection()
     */
    @Override
    public Connection getConnection() throws SQLException
    {
        try {
            return cBuilder.getConnection();
        }
        catch (Exception exc) {
            throw new SQLException(exc);
        }
    }

    /* (non-Javadoc)
     * @see org.quartz.utils.ConnectionProvider#shutdown()
     */
    @Override
    public void shutdown() throws SQLException
    {
        try {
            cBuilder.destroy();
        }
        catch (Exception exc) {
            throw new SQLException(exc);
        }
    }

}
