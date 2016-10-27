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
package it.greenvulcano.gvesb.adapter.http.formatters;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * 
 * Formatter class
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public interface Formatter
{
    /**
     * The initialization method
     * 
     * @param configurationNode
     * @throws FormatterInitializationException
     */
    public void init(Node configurationNode) throws FormatterInitializationException;

    /**
     * This method creates an HttpMethod from the configured parameters, and put
     * it into the given environment.
     * 
     * @param environment
     * @throws FormatterExecutionException
     */
    public void marshall(Map<String, Object> environment) throws FormatterExecutionException;

    /**
     * This method fills the environment with the parameters obtained by an
     * httpMethod.
     * 
     * @param environment
     * @throws FormatterExecutionException
     */
    public void unMarshall(Map<String, Object> environment) throws FormatterExecutionException;

    /**
     * This method returns the ID of the formatter.
     * 
     * Classes implementing this interface should have a "ID" String field, to
     * be returned when this method is invoked.
     * 
     * @return String ID
     */
    public String getId();
}