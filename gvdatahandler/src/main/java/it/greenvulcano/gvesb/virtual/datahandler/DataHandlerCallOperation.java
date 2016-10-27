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
package it.greenvulcano.gvesb.virtual.datahandler;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.datahandling.DHResult;
import it.greenvulcano.gvesb.datahandling.DataHandlerException;
import it.greenvulcano.gvesb.datahandling.IDBOBuilder;
import it.greenvulcano.gvesb.datahandling.factory.DHFactory;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * @version 3.0.0 Mar 31, 2010
 * @author GreenVulcano Developer Team
 */
public class DataHandlerCallOperation implements CallOperation
{
    public static final String DH_SERVICE_NAME = "DH_SERVICE_NAME";
    
    private OperationKey key       = null;
    private DHFactory    dhFactory = null;

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            dhFactory = new DHFactory();
            dhFactory.initialize(node);
        }
        catch (DataHandlerException exc) {
            throw new InitializationException("DataHandlerException Error: ", exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException,
            InterruptedException {
        try {
            String operation = gvBuffer.getProperty(DH_SERVICE_NAME);
            if (operation == null) {
                operation = gvBuffer.getService();
            }
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(GVBuffer.Field.SYSTEM.toString(),
                    GVBufferPropertiesHelper.handleProperty(gvBuffer.getSystem(), true));
            params.put(GVBuffer.Field.SERVICE.toString(),
                    GVBufferPropertiesHelper.handleProperty(gvBuffer.getService(), true));
            params.put(GVBuffer.Field.ID.toString(), GVBufferPropertiesHelper.handleProperty(gvBuffer.getId(), true));
            params.put(GVBuffer.Field.RETCODE.toString(),
                    GVBufferPropertiesHelper.handleProperty(Integer.toString(gvBuffer.getRetCode()), true));
            GVBufferPropertiesHelper.addProperties(params, gvBuffer, true);

            IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
            DHResult result = dboBuilder.EXECUTE(operation, gvBuffer.getObject(), params);

            gvBuffer.setObject(result.getData());
            gvBuffer.setProperty("REC_TOTAL", "" + result.getTotal());
            gvBuffer.setProperty("REC_READ", "" + result.getRead());
            gvBuffer.setProperty("REC_INSERT", "" + result.getInsert());
            gvBuffer.setProperty("REC_UPDATE", "" + result.getUpdate());
            gvBuffer.setProperty("REC_DISCARD", "" + result.getDiscard());
            gvBuffer.setProperty("REC_DISCARD_CAUSE", "" + result.getDiscardCauseListAsString());
            return gvBuffer;
        }
        catch (InterruptedException exc) {
            throw exc;
        }
        catch (DataHandlerException exc) {
            throw new DataHandlerCallException("GVVCL_DATA_HANDLER_EXCEPTION", new String[][] {{"message", exc.getMessage()}}, exc);
        }
        catch (GVException exc) {
            throw new DataHandlerCallException("GVVCL_DATA_HANDLER_EXCEPTION", new String[][] {{"message", exc.getMessage()}}, exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
        if (dhFactory != null) {
            try {
                dhFactory.destroy();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        dhFactory = null;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }


    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    @Override
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    @Override
    public OperationKey getKey()
    {
        return key;
    }
}
