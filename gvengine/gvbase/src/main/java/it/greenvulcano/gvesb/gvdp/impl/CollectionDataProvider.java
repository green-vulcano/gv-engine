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
package it.greenvulcano.gvesb.gvdp.impl;

import it.greenvulcano.gvesb.gvdp.AbstractDataProvider;
import it.greenvulcano.gvesb.gvdp.DataProviderException;
import it.greenvulcano.gvesb.gvdp.FieldExpressionKey;

import java.util.Arrays;
import java.util.Collection;

/**
 * This data provider provides a collection of objects.
 *
 * @version 3.0.0 Mar 22, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public class CollectionDataProvider extends AbstractDataProvider
{
    private Collection<?> internalCollection = null;

    /**
     * @see it.greenvulcano.gvesb.gvdp.AbstractDataProvider#getInternalObject()
     */
    @Override
    protected Object getInternalObject() throws Exception
    {
        return internalCollection;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getResult()
     */
    @Override
    public Object getResult() throws Exception
    {
        return internalCollection;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#getValue(it.greenvulcano.gvesb.gvdp.FieldExpressionKey)
     */
    @Override
    public Object getValue(FieldExpressionKey fieldExpressionKey) throws Exception
    {
        return internalCollection;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#reset()
     */
    @Override
    public void reset()
    {
        internalCollection = null;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdp.IDataProvider#setValue(it.greenvulcano.gvesb.gvdp.FieldExpressionKey,
     *      java.lang.Object)
     */
    @Override
    public void setValue(FieldExpressionKey fieldExpressionKey, Object value) throws Exception
    {
        if (value instanceof Collection<?>) {
            internalCollection = (Collection<?>) value;
        }
        else if (value instanceof Object[]) {
            internalCollection = Arrays.asList((Object[]) value);
        }
        else {
            throw new DataProviderException("DP_COLLECTION_BAD_VALUE", new String[][]{
                    {"className", getClass().getName()},
                    {"method", "setValue(FieldExpressionKey fieldExpressionKey, Object value)"},
                    {"cause", "Resulting expression value is not a collection."}});
        }
    }
}
