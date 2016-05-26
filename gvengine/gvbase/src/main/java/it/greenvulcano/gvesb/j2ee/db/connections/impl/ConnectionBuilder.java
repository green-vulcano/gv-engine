/*
 * Copyright (c) 2009-2011 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.j2ee.db.connections.impl;

import it.greenvulcano.gvesb.j2ee.db.GVDBException;

import java.sql.Connection;

import org.w3c.dom.Node;

/**
 *
 * @version 3.1.0 Gen 29, 2011
 * @author GreenVulcano Developer Team
 *
 */
public interface ConnectionBuilder {
    /**
     *
     * @param node
     * @throws GVDBException
     */
    void init(Node node) throws GVDBException;

    /**
     *
     * @return
     * @throws GVDBException
     */
    Connection getConnection() throws GVDBException;

    /**
     *
     * @param conn
     * @throws GVDBException
     */
    void releaseConnection(Connection conn) throws GVDBException;

    /**
     *
     */
    void destroy();
}
