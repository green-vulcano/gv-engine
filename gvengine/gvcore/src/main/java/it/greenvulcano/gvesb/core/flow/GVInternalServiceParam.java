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
package it.greenvulcano.gvesb.core.flow;

import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.virtual.Operation;
import it.greenvulcano.gvesb.virtual.pool.OperationManagerPool;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>GVInternalServiceParam</code> contains all the informations to be used
 * for retrieving and create operations available at Virtual Communication Layer
 * level. The Operationkey is build concatenating the filename and the absolute
 * XPath of the node from which the configuration item will be initialized.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVInternalServiceParam
{
    /**
     * the VCLOperation instance
     */
    private Operation           vclOperation         = null;
    /**
     * the VCLOperation key
     */
    private GVCoreOperationKey  coreOperationKey     = null;

    /**
     * the service name
     */
    private String              name                 = "";
    /**
     * Contains optional/internal fields that will transit into GreenVulcano
     * Core transparently. This field is allocated only if necessary.
     *
     * @see #getparameters()
     */
    private Map<String, String> parameters           = null;
    /**
     * If this flag is "true", all parameters will removed to the end of the
     * internal service.
     */
    private boolean             isToRemoveparameters = true;
    /**
     * If this flag is "true", the execution of this service is need for the
     * correct execution of the business GreenVulcano service.
     */
    private boolean             isCriticalService    = true;

    /**
     *
     */
    public GVInternalServiceParam()
    {
        // do nothing
    }

    /**
     *
     * @return the value of flag criticalService.
     */
    public boolean isCritical()
    {
        return isCriticalService;
    }

    /**
     *
     * @return the value of flag removeparameters.
     */
    public boolean isToRemove()
    {
        return isToRemoveparameters;
    }

    /**
     *
     * @return the VCLOperation.
     */
    public Operation getVCLOperation(OperationManagerPool operationManager) throws GVCoreException
    {
        try {
            vclOperation = operationManager.getOperation(coreOperationKey, coreOperationKey.getType());
        }
        catch (GVException exc) {
            throw new GVCoreException("GVCORE_VCL_OPERATION_INIT_ERROR", new String[][]{{"node",
                    XPathFinder.buildXPath(coreOperationKey.getNode())}}, exc);
        }
        return vclOperation;
    }

    /**
     * Get the Map that contains the Parameter of the Service. <br>
     *
     * @return the parameters Map.
     */
    public Map<String, String> getParameters()
    {
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }
        return parameters;
    }

    /**
     * Return an internal parameter value
     *
     * @param pName
     *        the parameter name
     * @return the parameter value
     */
    public String getParam(String pName)
    {
        return getParameters().get(pName);
    }

    /**
     * @return the parameters iterator
     */
    public Iterator<String> getParamNamesIterator()
    {
        return getParameters().keySet().iterator();
    }

    /**
     * @return the parameter names set
     */
    public Set<String> getParamNamesSet()
    {
        return getParameters().keySet();
    }

    /**
     * @return a string array of parameter names
     */
    public String[] getParamNames()
    {
        Set<String> namesSet = getParameters().keySet();
        String[] names = new String[namesSet.size()];
        namesSet.toArray(names);
        return names;
    }

    /**
     * Set the criticalService flag.
     *
     * @param criticalValue
     *        the flag value.
     */
    public void setCritical(String criticalValue)
    {
        if (criticalValue.equalsIgnoreCase("no")) {
            isCriticalService = false;
        }
    }

    /**
     * Set the removeparameters flag.
     *
     * @param removeparametersValue
     *        The flag value.
     */
    public void setRemoveFields(String removeparametersValue)
    {
        if (removeparametersValue.equalsIgnoreCase("no")) {
            isToRemoveparameters = false;
        }
    }

    /**
     * This method set the CoreOperationkey.
     *
     * @param coreOperationKey
     *        the VCLOperation key instance
     */
    public void setVCLOperationKey(GVCoreOperationKey coreOperationKey)
    {
        this.coreOperationKey = coreOperationKey;
    }

    /**
     * This method set the parameters into the Map.
     *
     * @param param
     *        The Parameter name utilized as key
     * @param value
     *        The Parameter value inserted into the Map
     */
    public void setParam(String param, String value)
    {
        getParameters().put(param, value);
    }

    /**
     * This method clean the parameters Map.
     */
    public void clearParameters()
    {
        if (parameters != null) {
            parameters.clear();
        }
    }

    /**
     * Provide a message to identify the Core Operation Key.
     *
     * @return The String The key of the operation in string form
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder("[GVInternalServiceParam]\n");

        buf.append("\tName     = ").append(name).append("\n");
        buf.append("\tCritical = ").append("" + isCriticalService).append("\n");
        buf.append("\tRemoveparameters = ").append("" + isToRemoveparameters).append("\n");

        if (parameters != null) {
            Iterator<String> paramIter = getParamNamesIterator();
            while (paramIter.hasNext()) {
                String currParamName = paramIter.next();
                String currParamValue = getParam(currParamName);
                buf.append("\t").append(currParamName).append(" = ").append(currParamValue).append("\n");
            }
        }
        buf.append("[GVInternalServiceParam]\n");

        return buf.toString();
    }

    /**
     * @return the service name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param n
     *        the service name
     */
    public void setName(String n)
    {
        name = n;
    }

    /**
     * Call cleanUp() of VCLOperations.
     *
     */
    public void cleanUp(OperationManagerPool operationManager) throws GVCoreException
    {
        if (vclOperation != null) {
            vclOperation.cleanUp();
            try {
                operationManager.releaseOperation(vclOperation);
            }
            catch (Exception exc) {
                throw new GVCoreException("GVCORE_GVVCL_RELEASE_ERROR", exc);
            }
            finally {
                vclOperation = null;
            }
        }
    }
}