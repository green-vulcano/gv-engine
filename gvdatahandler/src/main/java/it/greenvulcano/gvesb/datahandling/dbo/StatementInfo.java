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

package it.greenvulcano.gvesb.datahandling.dbo;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 3.0.0 Dec 14, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class StatementInfo
{
    private String    id;
    private String    sqlStatement;
    private Statement statement;
    private Map<String, List<Integer>> sqlStatementParams;
    private int       sqlStatementParamCount = 0;

    /**
     * @param id
     * @param sqlStatement
     * @param statement
     */
    public StatementInfo(String id, String sqlStatement, Statement statement)
    {
        super();
        this.id = id;
        this.sqlStatement = sqlStatement;
        this.statement = statement;
        this.sqlStatementParams = Collections.unmodifiableMap(new HashMap<String, List<Integer>>());
    }
    
    /**
     * @param id
     * @param sqlStatement
     * @param statement
     * @param sqlStatementParams
     */
    public StatementInfo(String id, String sqlStatement, Statement statement, 
           Map<String, List<Integer>> sqlStatementParams, int sqlStatementParamCount)
    {
        super();
        this.id = id;
        this.sqlStatement = sqlStatement;
        this.statement = statement;
        this.sqlStatementParams = Collections.unmodifiableMap(sqlStatementParams);
        this.sqlStatementParamCount = sqlStatementParamCount;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @return the sqlStatement
     */
    public String getSqlStatement()
    {
        return sqlStatement;
    }

    /**
     * @return the statement
     */
    public Statement getStatement()
    {
        return statement;
    }

    /**
     * @return the statement's named parameters
     */
    public Map<String, List<Integer>> getSqlStatementParams()
    {
        return sqlStatementParams;
    }
    
    public int getSqlStatementParamCount() {
        return this.sqlStatementParamCount;
    }
    
    public boolean usesNamedParams() {
        return !sqlStatementParams.isEmpty();
    }

    /**
     * @throws SQLException
     *
     */
    public void close() throws SQLException
    {
        if (statement != null) {
            statement.close();
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "StatementInfo [id=" + id + ", sqlStatement=" + sqlStatement + (sqlStatementParams.isEmpty() ? "" : 
            ", sqlStatementParams=" + sqlStatementParams) + ", statement=" + statement + "]";
    }
}
