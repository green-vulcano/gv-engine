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
package it.greenvulcano.gvesb.gvdp;

import it.greenvulcano.configuration.XMLConfigException;

import java.util.Collection;

import org.w3c.dom.Node;

/**
 * IDataProvider is an interface of classes that provides data identified by a
 * {@link FieldExpressionKey}.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface IDataProvider
{
    public String getName();

    /**
     * Returns the {@link FieldExpressionKey} associated with the specified id,
     * otherwise <code>null</code>.
     *
     * @param fieldId
     *        id of the requested {@link FieldExpressionKey}
     * @return the {@link FieldExpressionKey} associated with the specified id,
     *         otherwise <code>null</code>
     */
    public FieldExpressionKey getFieldKey(String fieldId);

    /**
     * Returns the {@link FieldExpressionKey} array.
     *
     * @return a {@link Collection} of possible {@link FieldExpressionKey}
     *         objects.
     */
    public Collection<FieldExpressionKey> getFieldKeys();

    /**
     * Returns value of the data element referenced by the specified
     * {@link FieldExpressionKey}.
     *
     * @param fieldExpressionKey
     *        <code>FieldExpressionKey</code> identifying the element whose
     *        value will be returned
     * @return value of the element referenced by the specified
     *         {@link FieldExpressionKey}
     * @throws Exception
     */
    public Object getValue(FieldExpressionKey fieldExpressionKey) throws Exception;

    /**
     * Returns value of the data element referenced by the specified
     * {@link FieldExpressionKey} identified by the parameter
     * <code>fieldExpressionKey</code>.
     *
     * @param fieldExpressionKey
     *        identifies the element whose value will be returned
     * @return value of the element referenced by the specified
     *         {@link FieldExpressionKey}
     * @throws Exception
     */
    public Object getValue(String fieldExpressionKey) throws Exception;

    /**
     * Set the value of the element represented by the specified
     * {@link FieldExpressionKey} to the specified new value.
     *
     * @param fieldExpressionKey
     *        <code>FieldExpressionKey</code> identifying the element whose
     *        value will be changed
     * @param value
     *        the new value for the element identified by the specified
     *        {@link FieldExpressionKey}
     * @throws Exception
     */
    public void setValue(FieldExpressionKey fieldExpressionKey, Object value) throws Exception;

    /**
     * You can pass an object containing specific properties of the
     * <code>IDataProvider</code> implementation.
     *
     * @param object
     *        the object containing specific properties of the data provider
     *        implementation.
     * @throws Exception
     *         if any error occurs.
     */
    public void setContext(Object object) throws Exception;

    /**
     * Prepare the <code>IDataProvider</code> implementation to make a new
     * elaboration.
     */
    public void reset();

    /**
     * Set the object from which to get/set values.
     *
     * @param object
     *        the object from which to get/set values.
     * @throws Exception
     *         if any error occurs.
     */
    public void setObject(Object object) throws Exception;

    /**
     * Returns the object resulting from this IDataProvider.
     *
     * @return the object resulting from this IDataProvider.
     * @throws Exception
     *         if any error occurs.
     */
    public Object getResult() throws Exception;

    /**
     * Initializes this IDataProvider reading configuration from a {@link Node}
     *
     * @param dpConfigNode
     * @throws XMLConfigException
     */
    public void init(Node dpConfigNode) throws XMLConfigException;
}
